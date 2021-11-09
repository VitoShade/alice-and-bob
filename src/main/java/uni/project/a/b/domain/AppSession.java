package uni.project.a.b.domain;


import lombok.Data;
import uni.project.a.b.crypto.KDF;
import uni.project.a.b.crypto.SessionState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Main idea is that the session should not be saved in the db, is currently possible???
// First try is that we saved it and use jpa for simplicity

/*@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

 */
@Data
public class AppSession {

    /**
     * Pre-Protocol
     */
    private Long id;

    private String aliceUser;

    private String bobUser;

    private LocalDateTime startedTime;

    private List<AppMessage> messages;

    /**
     * Post-Protocol
     */

    private KDF kdf;

    private SessionState aliceState;

    private SessionState bobState;





    public AppSession(Long id, String aliceUser, String bobUser) {
        this.id = id;
        this.aliceUser = aliceUser;
        this.bobUser = bobUser;
        this.messages = new ArrayList<>();
        this.kdf = new KDF();

    }

    public List<AppMessage> getMessages() {
        return messages;
    }

    public void addMessage(AppMessage messages) {
  this.messages.add(messages);
 }
/*
    public void establishSession(byte[] bobIdentityKey,
                                 Curve25519KeyPair aliceRatchetKeypair,
                                 byte[] rootKey,
                                 byte[] bobSignedPreKey,
                                 byte[] sendingChainKey,
                                 byte[] receivingChainKey) {
        this.bobIdentityKey = bobIdentityKey;
        this.aliceRatchetKeypair = aliceRatchetKeypair;
        this.rootKey = rootKey;
        this.bobSignedPreKey = bobSignedPreKey;
        this.sendingChainKey = sendingChainKey;
        this.receivingChainKey = receivingChainKey;

    }

 */


}
