package uni.project.a.b.domain;

import lombok.Data;
import org.javatuples.Pair;
import org.whispersystems.curve25519.Curve25519KeyPair;

import java.util.*;

/**
 * TODO: THE FOLLOWING KEYS NEED TO BE STORED ON THE CLIENT SIDE. This implementation is used ONLY for development purpose.
 * The session state is the domain used to retrieve all the keys used for sending and receiving messages.
 *
 * DHs: DH Ratchet key pair (the "sending" or "self" ratchet key)
 *
 * DHr: DH Ratchet public key (the "received" or "remote" key)
 *
 * RK: 32-byte Root Key
 *
 * CKs, CKr: 32-byte Chain Keys for sending and receiving
 *
 * Ns, Nr: Message numbers for sending and receiving
 *
 * PN: Number of messages in previous sending chain
 *
 * MKSKIPPED: Dictionary of skipped-over message keys, indexed by ratchet public key and message number. Raises an exception if too many elements are stored.
 */

@Data
public class AppSessionState {

    private boolean flag = false;

    private byte[] sharedKey;

    private byte[] AD;

    private Curve25519KeyPair selfRatchetKeyPair;

    private byte[] receiverRatchetKey;

    private byte[] rootKey;

    private byte[] sendingChainKey;

    private Integer nSendingMessages;

    private byte[] receivingChainKey;

    private Integer nReceivingMessages;

    private Integer nPreviousMessages;


    private Map<Pair<byte[], Integer>, byte[]> skippedMessageKeys = new HashMap<>();


    public AppSessionState(Curve25519KeyPair selfRatchetKeyPair, byte[] receiverRatchetKey, byte[] rootKey, byte[] sendingChainKey, int nSendingMessages, byte[] receivingChainKey, int nReceivingMessages, int nPreviousMessages) {
        this.selfRatchetKeyPair = selfRatchetKeyPair;
        this.receiverRatchetKey = receiverRatchetKey;
        this.rootKey = rootKey;
        this.sendingChainKey = sendingChainKey;
        this.nSendingMessages = nSendingMessages;
        this.receivingChainKey = receivingChainKey;
        this.nReceivingMessages = nReceivingMessages;
        this.nPreviousMessages = nPreviousMessages;
    }


    public void updateState(AppSessionState state) {
        this.selfRatchetKeyPair = state.getSelfRatchetKeyPair();
        this.receiverRatchetKey = state.getReceiverRatchetKey();
        this.rootKey = state.getRootKey();
        this.sendingChainKey = state.getSendingChainKey();
        this.nSendingMessages = state.getNSendingMessages();
        this.receivingChainKey = state.getReceivingChainKey();
        this.nReceivingMessages = state.getNReceivingMessages();
        this.nPreviousMessages = state.getNPreviousMessages();
        this.skippedMessageKeys = state.getSkippedMessageKeys();
    }


    public AppSessionState(byte[] sharedKey, byte[] AD) {
        this.sharedKey = sharedKey;
        this.AD = AD;
    }



    public void addMessageKey (Pair<byte[], Integer> pair, byte[] mk){
        skippedMessageKeys.put(pair, mk);
    }
}
