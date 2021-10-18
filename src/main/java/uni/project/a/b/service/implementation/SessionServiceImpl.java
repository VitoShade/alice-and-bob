package uni.project.a.b.service.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uni.project.a.b.domain.AppMessage;
import uni.project.a.b.domain.AppSession;
import uni.project.a.b.repo.SessionRepo;
import uni.project.a.b.service.MessageService;
import uni.project.a.b.service.SessionService;
import uni.project.a.b.service.UserService;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SessionServiceImpl implements SessionService, MessageService {


    private SessionRepo sessionRepo;

    private UserService userService;


    @Override
    public Optional<AppSession> getSession(Long id) {
        log.info("Getting Session by id");
        return sessionRepo.findById(id);
    }

    @Override
    public Stream<AppSession> getByUsers(Long id) {
        log.info("Getting Session by user id");
        return sessionRepo.findByUsers(id);
    }

    @Override
    public Optional<AppSession> getByUsers(Long id1, Long id2) {
        log.info("Getting Session by user id");
        return sessionRepo.findByUsers(id1, id2);
    }


    @Override
    public void establishSession(Long id1, Long id2) {
        log.info("Establishing sessions");
        sessionRepo.save(id1, id2);

    }


    @Override
    public List<AppMessage> findBySession(Long sessionId) {
        Optional<AppSession> sess = getSession(sessionId);
        return sess.map(AppSession::getMessages).orElse(null);
    }

    @Override
    public List<AppMessage> findBySession(Long sessionId, Long senderId) {
        Optional<AppSession> sess = getSession(sessionId);
        List<AppMessage> mess = sess.get().getMessages();

        return mess.stream().filter(message -> message.getSenderId().equals(senderId)).toList();


    }

    @Override
    public void saveMessage(AppMessage message, Long sessionId) {
        Optional<AppSession> sess = sessionRepo.findById(sessionId);
        sess.get().addMessage(message);


    }
}
