package uni.project.a.b.service;

import uni.project.a.b.domain.AppMessage;
import uni.project.a.b.domain.AppSession;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageService {


    List<AppMessage> findBySession(Long sessionId);

    List<AppMessage> findBySession(Long sessionId, String senderUser);

    List<AppMessage> findBySession(Long sessionId, LocalDateTime time);

    void saveMessage(AppMessage message, Long sessionId);



}
