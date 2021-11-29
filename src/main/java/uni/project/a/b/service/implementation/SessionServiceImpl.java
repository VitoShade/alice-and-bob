package uni.project.a.b.service.implementation;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uni.project.a.b.crypto.DoubleRatchet;
import uni.project.a.b.crypto.KDF;
import uni.project.a.b.domain.AppSessionState;
import uni.project.a.b.domain.AppHeader;
import uni.project.a.b.domain.AppMessage;
import uni.project.a.b.domain.AppSession;
import uni.project.a.b.repo.SessionRepo;
import uni.project.a.b.service.MessageService;
import uni.project.a.b.service.SessionService;
import uni.project.a.b.service.UserService;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class SessionServiceImpl implements SessionService, MessageService {


    private SessionRepo sessionRepo;

    private UserService userService;

    /**
     * Scheduled service that every minute check if the sessions are opened by more than 30 minute.
     * In that case calls the delete method on that specific sessions.
     */

    @Scheduled(cron = "0 * * * * *")
    public void sessionGC(){
        log.info("Session GC");
        List<AppSession> sessions = sessionRepo.getAll();
        sessions.forEach(session -> {
            long minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), session.getStartedTime());
            if (minutes >= (long) 30){
                sessionRepo.delete(session.getId());
            }
        });
    }

    /**
     * Various method for getting the sessions.
     */


    @Override
    public Optional<AppSession> getSession(Long id) {
        log.info("Getting Session by id");
        return sessionRepo.findById(id);
    }

    @Override
    public Stream<AppSession> getByUsers(String username) {
        log.info("Getting Session by user id");
        return sessionRepo.findByUsers(username);
    }

    @Override
    public Optional<AppSession> getByUsers(String user1, String user2) {
        log.info("Getting Session by user id");
        return sessionRepo.findByUsers(user1, user2);
    }


    @Override
    public void establishSession(String user1, String user2) throws IOException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        log.info("Establishing sessions");
        AppSession sess = sessionRepo.create(user1, user2);
        List<AppMessage> aliceMessages = sess.getMessages("alice");
        List<AppMessage> bobMessages = sess.getMessages("bob");

        KDF kdf = new KDF();

        if (sess.isEstablished()){
            log.info("Session already established");
            return;
        }

        if (aliceMessages.size() == bobMessages.size() || aliceMessages.size() == 0) {
            // if 0 then, the invoker of the method become alice
            Triplet<byte[], byte[], AppHeader> keys = DoubleRatchet.aliceKeyAgr(userService.getUser(user1), userService.getUser(user2));

            // Actually we use the kdf to compress the SK into a 32 byte key.
            byte[] k = kdf.deriveKey(keys.getValue0());

            byte[] ciphertext = DoubleRatchet.encrypt(k, "first message", keys.getValue1());
            AppMessage message = new AppMessage(sess.getId(), ciphertext, LocalDateTime.now(), user1, keys.getValue2());

            //sendMessage(message, sess.getId());
            sess.addMessage(message, "alice");

            sess.setAliceState(new AppSessionState(keys.getValue0(), keys.getValue1()));
            sessionRepo.update(sess);

        } else if (aliceMessages.size() == 1 || bobMessages.size() == 0 ){
            if (user1.equals(sess.getAliceUser())) {
                log.error("Session need finish the initialization, wait Bob process of initialization");
                return;
            }
            // else the invoker become bob, he picks the first message and decode the SK
            AppMessage firstMessage = aliceMessages.get(0);
            byte[][] keys  = DoubleRatchet.bobKeyAgr(userService.getUser(user1), firstMessage.getHeader());
            byte[] plaintext = DoubleRatchet.decrypt(kdf.deriveKey(keys[0]),firstMessage.getBody(), keys[1]);

            if (!new String(plaintext, StandardCharsets.UTF_8).equals("first message")){
                log.error("Decryption failed, aborting session");
                sessionRepo.delete(sess.getId());
            } else {
                AppSessionState state = DoubleRatchet.bobRatchetInit(keys[0]);
                state.setAD(keys[1]);
                sess.setBobState(state);

                sess.deleteMessage(firstMessage, "alice");
                sess.setEstablished(true);
                sessionRepo.update(sess);

            }
        } else {
            log.info("Session already established");
        }


    }


    /**
     * Beginning of MessageService methods.
     */

    // TODO: Un solo findBySession!

    @Override
    public List<AppMessage> findBySession(Long sessionId, String username) {
        Optional<AppSession> sess = getSession(sessionId);
        if (sess.isEmpty()) {
            return Collections.emptyList();
        }

        if (username.equals(sess.get().getAliceUser())){
            return sess.get().getMessages("bob");
        } else {
            return sess.get().getMessages("alice");
        }

    }

    @Override
    public List<AppMessage> findBySession(Long sessionId, LocalDateTime time, String username) {
        Optional<AppSession> sess = getSession(sessionId);
        if (sess.isEmpty()) {
            return Collections.emptyList();
        }

        List<AppMessage> mess;
        if (username.equals(sess.get().getAliceUser())){
            mess = sess.get().getMessages("bob");

        } else {
            mess = sess.get().getMessages("alice");
        }

        List<AppMessage> out = new ArrayList<>();

        mess.forEach(message -> {
            try {
                if (username.equals(sess.get().getAliceUser())){
                Pair <byte[], AppSessionState> pair = DoubleRatchet.ratchetDecrypt(sess.get().getAliceState(),
                        message.getBody(), message.getHeader());
                message.setBody(pair.getValue0());

                    sess.get().setAliceState(pair.getValue1());
                    sess.get().deleteMessage(message, "bob");

                } else {

                    if (message.getHeader() != null) {
                        Pair<byte[], AppSessionState> pair = DoubleRatchet.ratchetDecrypt(sess.get().getBobState(),
                                message.getBody(), message.getHeader());
                        message.setBody(pair.getValue0());
                        sess.get().setBobState(pair.getValue1());
                    }
                    sess.get().deleteMessage(message, "alice");
                }
                out.add(message);
            } catch (InvalidAlgorithmParameterException | BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        });
        return out;





    }

    @Override
    public void sendMessage(AppMessage message, Long sessionId) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        Optional<AppSession> sess = sessionRepo.findById(sessionId);

        if (sess.isPresent()) {
            AppSession session = sess.get();

            List<AppMessage> aliceMessages = session.getMessages("alice");
            List<AppMessage> bobMessages = session.getMessages("bob");
            String sender = message.getSenderUser();
            log.info(String.valueOf(aliceMessages.size()));
            log.info(String.valueOf(bobMessages.size()));


            if (!session.isEstablished()){
                log.error("Session need finish the initialization, run an establish session before sending messages");
                return;
            }else if (sender.equals(session.getBobUser()) && !session.getAliceState().isFlag()){
                log.error("Alice need to send the first message for being able to send messages");
                return;
            }
            
            AppSession newSession;
            if (Objects.equals(sender, session.getAliceUser())) {
                newSession = sendAlice(message, session);
            } else {
                newSession = sendBob(message, session);
            }

            sessionRepo.update(newSession);

            }


    }

    private AppSession sendAlice(AppMessage message, AppSession session) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        //preliminaries check
        if (!session.getAliceState().isFlag()){
            AppSessionState state = session.getAliceState();
            state.updateState(DoubleRatchet.aliceRatchetInit(session));
            state.setFlag(true);
            session.setAliceState(state);
        }

        //encryption and send
        byte[] plaintext = message.getBody();
        Triplet<byte[], AppHeader, AppSessionState> triplet = DoubleRatchet.ratchetEncrypt(session.getAliceState(), plaintext);
        message.setBody(triplet.getValue0());
        message.setHeader(triplet.getValue1());
        session.setAliceState(triplet.getValue2());
        session.addMessage(message, "alice");
        return session;

    }

    private AppSession sendBob(AppMessage message, AppSession session) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        //preliminaries check
        if (!session.getBobState().isFlag()) {
            AppSessionState state = session.getBobState();
            if (session.getMessages("alice").size() != 0) {
                AppMessage aliceInitMessage = session.getMessages("alice").get(0);
                Pair<byte[], AppSessionState> pair = DoubleRatchet.ratchetDecrypt(state,
                        aliceInitMessage.getBody(), aliceInitMessage.getHeader());
                AppSessionState newState = pair.getValue1();
                session.deleteMessage(aliceInitMessage, "alice");
                aliceInitMessage.setBody(pair.getValue0());
                aliceInitMessage.setHeader(null);
                session.addMessage(aliceInitMessage, "alice");
                newState.setFlag(true);
                session.setBobState(newState);
            } else{
                state.setFlag(true);
                session.setBobState(state);
            }
        }
        //encryption and send
        byte[] plaintext = message.getBody();
        Triplet<byte[], AppHeader, AppSessionState> triplet = DoubleRatchet.ratchetEncrypt(session.getBobState(), plaintext);
        message.setBody(triplet.getValue0());
        message.setHeader(triplet.getValue1());
        session.setBobState(triplet.getValue2());
        session.addMessage(message, "bob");
        return session;

    }

}
