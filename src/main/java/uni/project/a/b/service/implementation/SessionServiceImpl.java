package uni.project.a.b.service.implementation;

import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uni.project.a.b.crypto.DoubleRatchet;
import uni.project.a.b.crypto.KDF;
import uni.project.a.b.crypto.SessionState;
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
import java.util.*;
import java.util.stream.Stream;

@Service
@Transactional
@Slf4j
public class SessionServiceImpl implements SessionService, MessageService {

    @Autowired
    private SessionRepo sessionRepo;

    @Autowired
    private UserService userService;


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

        if (aliceMessages.size() == bobMessages.size() || aliceMessages.size() == 0) {
            // if 0 then, the invoker of the method become alice
            Triplet<byte[], byte[], AppHeader> keys = DoubleRatchet.aliceKeyAgr(userService.getUser(user1), userService.getUser(user2));

            // Actually we use the kdf to compress the SK into a 32 byte key.
            byte[] k = kdf.deriveKey(keys.getValue0());

            byte[] ciphertext = DoubleRatchet.encrypt(k, "first message", keys.getValue1());
            AppMessage message = new AppMessage(sess.getId(), ciphertext, LocalDateTime.now(), user1, keys.getValue2());

            //sendMessage(message, sess.getId());
            sess.addMessage(message, "alice");

            sess.setAliceState(new SessionState(keys.getValue0(), keys.getValue1()));
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
                SessionState state = DoubleRatchet.bobRatchetInit(sess,keys[0]);
                state.setAD(keys[1]);
                sess.setBobState(state);
                byte[] secondMessage = "Session established, first message decrypted correctly".getBytes(StandardCharsets.UTF_8);
                AppMessage message = new AppMessage(sess.getId(), secondMessage, LocalDateTime.now(), user1);

                //sendMessage(message, sess.getId());
                sess.addMessage(message, "bob");
                sessionRepo.update(sess);
            }
        } else {
            log.info("Session already established");
        }




    }




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

        // TODO: Error, bob first message and alice first message need to be exited
        mess.forEach(message -> {
            try {
                Pair <byte[], SessionState> pair = DoubleRatchet.ratchetDecrypt(sess.get().getAliceState(),
                        message.getBody(), message.getHeader());
                message.setBody(pair.getValue0());
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
            if ((aliceMessages.size() + bobMessages.size()) < 2) {
                log.error("Session need finish the initialization, run an establish session before sending messages");
                return;
            } else if (sender.equals(session.getBobUser()) || aliceMessages.size() == 1){
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
            SessionState state = session.getAliceState();
            state.updateState(DoubleRatchet.aliceRatchetInit(session));
            state.setFlag(true);
            session.setAliceState(state);
        }

        //encryption and send
        byte[] plaintext = message.getBody();
        Triplet<byte[], AppHeader, SessionState> triplet = DoubleRatchet.ratchetEncrypt(session.getAliceState(), plaintext);
        message.setBody(triplet.getValue0());
        message.setHeader(triplet.getValue1());
        session.setAliceState(triplet.getValue2());
        session.addMessage(message, "alice");
        return session;

    }

    private AppSession sendBob(AppMessage message, AppSession session) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        //preliminaries check
        if (!session.getBobState().isFlag()){
           AppMessage aliceInitMessage =  session.getMessages("alice").get(1);
           Pair<byte[], SessionState> pair = DoubleRatchet.ratchetDecrypt(session.getBobState(),
                   aliceInitMessage.getBody(), aliceInitMessage.getHeader());
           log.info(new String(pair.getValue0()));
           SessionState state = pair.getValue1();
           state.setFlag(true);
           session.setBobState(state);

        }

        //encryption and send
        byte[] plaintext = message.getBody();
        Triplet<byte[], AppHeader, SessionState> triplet = DoubleRatchet.ratchetEncrypt(session.getBobState(), plaintext);
        message.setBody(triplet.getValue0());
        message.setHeader(triplet.getValue1());
        session.setBobState(triplet.getValue2());
        session.addMessage(message, "bob");
        return session;

    }

}
