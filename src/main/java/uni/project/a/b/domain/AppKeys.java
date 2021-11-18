package uni.project.a.b.domain;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.whispersystems.curve25519.Curve25519KeyPair;
import uni.project.a.b.crypto.PKCrypto;

import javax.persistence.*;

/**
 * TODO: THE FOLLOWING KEYS NEED TO BE STORED ON THE CLIENT SIDE.
 * This implementation is used ONLY for development purpose.
 */
@Entity
@AllArgsConstructor
@Slf4j
public class AppKeys {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private byte[] identityPrivateKey;

    private byte[] identityKey;

    private byte[] signedPreKey;

    private byte[] signedPrivatePreKey;

    private byte[] preKeySignature;


    //TODO: SET SECURITY METHOD FOR PRIVATE KEYS

    public AppKeys(){
        Curve25519KeyPair identity = PKCrypto.generateKeyPair();
        Curve25519KeyPair preKey = PKCrypto.generateKeyPair();
        identityPrivateKey = identity.getPrivateKey();
        identityKey = identity.getPublicKey();
        signedPrivatePreKey = preKey.getPrivateKey();
        signedPreKey = preKey.getPublicKey();
        preKeySignature = PKCrypto.sign(signedPreKey, identityPrivateKey);


    }

    public byte[] getIdentityPrivateKey() {
        return identityPrivateKey;
    }

    public void setIdentityPrivateKey(byte[] identityPrivateKey) {
        this.identityPrivateKey = identityPrivateKey;
    }

    public byte[] getIdentityKey() {
        return identityKey;
    }

    public void setIdentityKey(byte[] identityKey) {
        this.identityKey = identityKey;
    }

    public byte[] getSignedPreKey() {
        return signedPreKey;
    }

    public void setSignedPreKey(byte[] signedPreKey) {
        this.signedPreKey = signedPreKey;
    }

    public byte[] getSignedPrivatePreKey() {
        return signedPrivatePreKey;
    }

    public void setSignedPrivatePreKey(byte[] signedPrivatePreKey) {
        this.signedPrivatePreKey = signedPrivatePreKey;
    }

    public byte[] getPreKeySignature() {
        return preKeySignature;
    }

    public void setPreKeySignature(byte[] preKeySignature) {
        this.preKeySignature = preKeySignature;
    }
}
