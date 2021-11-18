package uni.project.a.b.crypto;


import lombok.extern.slf4j.Slf4j;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

@Slf4j
public class PKCrypto {

    private static final int PUBLIC_KEY_SIZE = 32;

    private static final int SIGNATURE_SIZE = 64;

    private static final Curve25519 curve = Curve25519.getInstance(Curve25519.BEST);

    public static Curve25519KeyPair generateKeyPair(){
        return curve.generateKeyPair();
    }

    public static byte[] keyExchange(byte[] pubKey, byte[] privKey){
        return curve.calculateAgreement(pubKey, privKey);
    }

    public static byte[] sign(byte[] message, byte[] privateKey){
        return curve.calculateSignature(privateKey, message);
    }

    public static boolean verify(byte[] signature, byte[] message, byte[] publicKey){
        return curve.verifySignature(publicKey, message, signature);
    }

    public static int getPublicKeySize(){
        return PUBLIC_KEY_SIZE;
    }

    public static int getSignatureSize(){
        return SIGNATURE_SIZE;
    }

}
