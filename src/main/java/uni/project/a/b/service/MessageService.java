package uni.project.a.b.service;

import uni.project.a.b.domain.AppMessage;
import uni.project.a.b.domain.AppSession;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.time.LocalDateTime;
import java.util.List;

public interface MessageService {



    List<AppMessage> findBySession(Long sessionId, String senderUser);

    List<AppMessage> findBySession(Long sessionId, LocalDateTime time, String sender);

    void sendMessage(AppMessage message, Long sessionId) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException;



}
