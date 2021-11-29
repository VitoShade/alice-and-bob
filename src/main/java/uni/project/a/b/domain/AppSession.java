package uni.project.a.b.domain;


import lombok.Data;
import uni.project.a.b.crypto.KDF;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
public class AppSession {

    /**
     * Pre-Protocol
     */
    private Long id;

    private String aliceUser;

    private String bobUser;

    private LocalDateTime startedTime;

    private List<AppMessage> aliceMessages;

    private List<AppMessage> bobMessages;

    /**
     * Post-Protocol
     */

    private KDF kdf;

    private AppSessionState aliceState;

    private AppSessionState bobState;

    private boolean established;






    public AppSession(Long id, String aliceUser, String bobUser) {
        this.id = id;
        this.aliceUser = aliceUser;
        this.bobUser = bobUser;
        this.startedTime = LocalDateTime.now();
        this.aliceMessages = new CopyOnWriteArrayList<>();
        this.bobMessages = new CopyOnWriteArrayList<>();
        this.kdf = new KDF();
        this.established = false;

    }

    public List<AppMessage> getMessages(String username) {
        if (username.equals("alice")){
            return aliceMessages;
        }
        else {
            return bobMessages;
        }
    }

    public void addMessage(AppMessage message, String username) {
        if (username.equals("alice")){
            aliceMessages.add(message);
        }
        else {
            bobMessages.add(message);
        }
    }

    public void deleteMessage(AppMessage message, String username){
        if (username.equals("alice")){
            aliceMessages.remove(message);
        }
        else {
            bobMessages.remove(message);
        }
    }



}
