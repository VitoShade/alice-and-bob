package uni.project.a.b.service;

import uni.project.a.b.domain.AppMessage;
import uni.project.a.b.domain.AppSession;

import java.util.List;

public interface MessageService {


    List<AppMessage> findBySession(Long sessionId);

    List<AppMessage> findBySession(Long sessionId, Long senderId);

    void saveMessage(AppMessage message, Long sessionId);



}
