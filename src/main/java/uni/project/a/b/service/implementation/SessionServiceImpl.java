package uni.project.a.b.service.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.header.Header;
import org.springframework.stereotype.Service;
import uni.project.a.b.crypto.DoubleRatchet;
import uni.project.a.b.crypto.SessionState;
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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

        if (messages.size() == 0) {
            // if 0 then, the invoker of the method become alice
            Triplet<byte[], byte[], Header> keys = DoubleRatchet.aliceKeyAgr(userService.getUser(user1), userService.getUser(user2));
            byte[] ciphertext = DoubleRatchet.encrypt(keys.getValue0(), "first message", keys.getValue1());
            AppMessage message = new AppMessage(sess.getId(), ciphertext, LocalDateTime.now(), user1, keys.getValue2());

            //TODO: Init session state!

            saveMessage(message, sess.getId());

        } else if (messages.size() == 1){
            // if 1 then the invoker become bob
            AppMessage firstMessage = messages.get(0);
            byte[][] keys  = DoubleRatchet.bobKeyAgr(userService.getUser(user1), firstMessage.getHeader());
            byte[] plaintext = DoubleRatchet.decrypt(keys[0],firstMessage.getBody(), keys[1]);

            if (!Arrays.toString(plaintext).equals("first message")){
                log.error("Decryption failed, aborting session");
                sessionRepo.delete(sess.getId());
            } else {
                //TODO: Init session state!
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
    public void saveMessage(AppMessage message, Long sessionId) {
        Optional<AppSession> sess = sessionRepo.findById(sessionId);
        sess.get().addMessage(message);


    }


}
