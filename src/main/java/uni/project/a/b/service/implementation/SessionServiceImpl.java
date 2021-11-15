package uni.project.a.b.service.implementation;

import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.header.Header;
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
        AppSession sess = sessionRepo.save(user1, user2);
        List<AppMessage> messages = sess.getMessages();
        KDF kdf = new KDF();

        if (messages.size() == 0) {
            // if 0 then, the invoker of the method become alice
            Triplet<byte[], byte[], AppHeader> keys = DoubleRatchet.aliceKeyAgr(userService.getUser(user1), userService.getUser(user2));

            // Actually we use the kdf to compress the SK into a 32 byte key.
            byte[] k = kdf.deriveKey(keys.getValue0());

            byte[] ciphertext = DoubleRatchet.encrypt(k, "first message", keys.getValue1());
            AppMessage message = new AppMessage(sess.getId(), ciphertext, LocalDateTime.now(), user1, keys.getValue2());

            //sendMessage(message, sess.getId());
            sess.addMessage(message);

            sess.setAliceState(new SessionState(keys.getValue0(), keys.getValue1()));

        } else if (messages.size() == 1){
            // if 1 then the invoker become bob
            AppMessage firstMessage = messages.get(0);
            byte[][] keys  = DoubleRatchet.bobKeyAgr(userService.getUser(user1), firstMessage.getHeader());
            keys[0] = kdf.deriveKey(keys[0]);
            byte[] plaintext = DoubleRatchet.decrypt(keys[0],firstMessage.getBody(), keys[1]);

            if (!new String(plaintext, StandardCharsets.UTF_8).equals("first message")){
                log.error("Decryption failed, aborting session");
                sessionRepo.delete(sess.getId());
            } else {
                //TODO: Init session state!
                SessionState state = DoubleRatchet.bobRatchetInit(sess,keys[0]);
                sess.setBobState(state);
                byte[] secondMessage = "Session established, first message decrypted correctly".getBytes(StandardCharsets.UTF_8);
                AppMessage message = new AppMessage(sess.getId(), secondMessage, LocalDateTime.now(), user1);

                //sendMessage(message, sess.getId());
                sess.addMessage(message);
            }
        } else {
            log.info("Session already established");
        }




    }


    @Override
    public List<AppMessage> findBySession(Long sessionId) {
        Optional<AppSession> sess = getSession(sessionId);
        return sess.map(AppSession::getMessages).orElse(null);
    }

    @Override
    public List<AppMessage> findBySession(Long sessionId, String senderUser) {
        Optional<AppSession> sess = getSession(sessionId);
        List<AppMessage> mess = sess.get().getMessages();

        return mess.stream().filter(message -> message.getSenderUser().equals(senderUser)).toList();

    }

    @Override
    public List<AppMessage> findBySession(Long sessionId, LocalDateTime time) {
        Optional<AppSession> sess = getSession(sessionId);
        List<AppMessage> mess = sess.get().getMessages();

        return mess.stream().filter(message -> message.getTime().isAfter(time)).toList();
    }

    @Override
    public void sendMessage(AppMessage message, Long sessionId) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        Optional<AppSession> sess = sessionRepo.findById(sessionId);

        if (sess.isPresent()) {

            List<AppMessage> messages = sess.get().getMessages();
            if (messages.size() < 2){
                log.error("Session need finish the initialization, run an establish session before sending messages");
                return;
            } else if (messages.size() == 2  && Objects.equals(message.getSenderUser(), sess.get().getAliceUser())) {
                //If messages.size == 2 then alice session need to be init with bob's ratchet key
                sess.get().setAliceState(DoubleRatchet.aliceRatchetInit(sess.get()));
            } else if (messages.size() == 2  && Objects.equals(message.getSenderUser(), sess.get().getBobUser())) {
                log.error("Session need finish the initialization, Alice need to send the first message!");
                return;
            }


            byte[] plaintext = message.getBody();


            if (Objects.equals(message.getSenderUser(), sess.get().getAliceUser())){
                Triplet<byte[], AppHeader, SessionState> triplet = DoubleRatchet.ratchetEncrypt(sess.get().getAliceState(),plaintext, message.getSenderUser());
                message.setBody(triplet.getValue0());
                message.setHeader(triplet.getValue1());
                sess.get().setAliceState(triplet.getValue2());
            }else{
                Triplet<byte[], AppHeader, SessionState> triplet = DoubleRatchet.ratchetEncrypt(sess.get().getBobState(),plaintext, message.getSenderUser());
                message.setBody(triplet.getValue0());
                message.setHeader(triplet.getValue1());
                sess.get().setBobState(triplet.getValue2());
            }
            sess.get().addMessage(message);
        }

    }


}
