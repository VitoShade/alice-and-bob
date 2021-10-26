package uni.project.a.b.service.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uni.project.a.b.domain.AppMessage;
import uni.project.a.b.domain.AppSession;
import uni.project.a.b.repo.SessionRepo;
import uni.project.a.b.service.MessageService;
import uni.project.a.b.service.SessionService;
import uni.project.a.b.service.UserService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
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
    public void establishSession(String user1, String user2) {
        log.info("Establishing sessions");
        sessionRepo.save(user1, user2);

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
