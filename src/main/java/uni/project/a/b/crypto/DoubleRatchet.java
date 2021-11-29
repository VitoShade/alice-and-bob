package uni.project.a.b.crypto;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.whispersystems.curve25519.Curve25519KeyPair;
import uni.project.a.b.domain.AppHeader;
import uni.project.a.b.domain.AppSession;
import uni.project.a.b.domain.AppSessionState;
import uni.project.a.b.domain.AppUser;
import uni.project.a.b.utils.CryptoUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * This class represent the full Double Ratchet protocol with the use of X3DH as key agreement method.
 * https://signal.org/docs/
 */

public class DoubleRatchet {

    private static final KDF kdf = new KDF();

    private static final AeadCipher cipher = new AeadCipher();





    public static Triplet <byte[], byte[], AppHeader> aliceKeyAgr(AppUser alice, AppUser bob) throws InvalidKeyException, IOException{

        byte[] aliceIdentityKey = alice.getKeys().getIdentityKey();

        byte[] bobIdentityKey = bob.getKeys().getIdentityKey();
        byte[] bobSignedPreKey = bob.getKeys().getSignedPreKey();
        byte[] bobPreKeySignature = bob.getKeys().getPreKeySignature();

        // Verify of the key
        if (!PKCrypto.verify(bobPreKeySignature, bobSignedPreKey, bobIdentityKey)) {
            throw new InvalidKeyException("Failed first verification of keys");
        }
        Curve25519KeyPair ephemeralKeyPair = PKCrypto.generateKeyPair();
        byte[] ephemeralPrivateKey = ephemeralKeyPair.getPrivateKey();

        // SK Calculus
        ByteArrayOutputStream tmpSK = new ByteArrayOutputStream();
        tmpSK.write(PKCrypto.keyExchange(bobSignedPreKey, alice.getKeys().getIdentityPrivateKey()));
        tmpSK.write(PKCrypto.keyExchange(bobIdentityKey, ephemeralPrivateKey));
        tmpSK.write(PKCrypto.keyExchange(bobSignedPreKey, ephemeralPrivateKey));
        byte[] sharedKey = tmpSK.toByteArray();


        // Associated Data
        ByteArrayOutputStream tmpAD = new ByteArrayOutputStream();
        tmpAD.write(aliceIdentityKey);
        tmpAD.write(bobIdentityKey);
        byte[] associateData = tmpAD.toByteArray();

        List<byte[]> headerList = new ArrayList<>();
        headerList.add(aliceIdentityKey);
        headerList.add(ephemeralKeyPair.getPublicKey());
        AppHeader header = new AppHeader("first message", headerList);

        return new Triplet<>(sharedKey, associateData, header);

    }


    public static byte[][] bobKeyAgr(AppUser bob, AppHeader header) throws IOException {


        byte[] aliceIdentityKey = header.getHeaderValues().get(0);
        byte[] aliceEphemeralKey = header.getHeaderValues().get(1);


        ByteArrayOutputStream tmpSK = new ByteArrayOutputStream();
        tmpSK.write(PKCrypto.keyExchange(aliceIdentityKey, bob.getKeys().getSignedPrivatePreKey()));
        tmpSK.write(PKCrypto.keyExchange(aliceEphemeralKey, bob.getKeys().getIdentityPrivateKey()));
        tmpSK.write(PKCrypto.keyExchange(aliceEphemeralKey, bob.getKeys().getSignedPrivatePreKey()));
        byte[] sharedKey = tmpSK.toByteArray();


        // Associated Data
        ByteArrayOutputStream tmpAD = new ByteArrayOutputStream();
        tmpAD.write(aliceIdentityKey);
        tmpAD.write(bob.getKeys().getIdentityKey());
        byte[] associateData = tmpAD.toByteArray();


        byte[][] ret = new byte[3][];
        ret[0] = sharedKey;
        ret[1] = associateData;

        return ret;
    }


    public static byte[] encrypt (byte[] messageKey, String plaintext, byte[] AD) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        return cipher.encrypt(messageKey, plaintext.getBytes(StandardCharsets.UTF_8), AD);
    }

    public static byte[] decrypt (byte[] messageKey, byte[] ciphertext, byte[] AD) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        return cipher.decrypt(messageKey, ciphertext, AD);
    }

    public static AppSessionState aliceRatchetInit(AppSession session) throws InvalidKeyException {

        byte[] sharedKey = session.getAliceState().getSharedKey();

        Curve25519KeyPair selfRatchetKeyPair = PKCrypto.generateKeyPair();
        byte[] receiverRatchetKey = session.getBobState().getSelfRatchetKeyPair().getPublicKey();
        byte[] kdfOutput = kdf.deriveKey(sharedKey, PKCrypto.keyExchange(receiverRatchetKey, selfRatchetKeyPair.getPrivateKey()));
        byte[][] tmpkeys = CryptoUtils.split(kdf.expand(kdfOutput,64) ,32);
        byte[] rootKey = tmpkeys[0];
        byte[] sendingChainKey = tmpkeys[1];
        return new AppSessionState(selfRatchetKeyPair, receiverRatchetKey, rootKey, sendingChainKey,
                0, null, 0, 0);

    }

    public static AppSessionState bobRatchetInit(byte[] sharedKey) {

        Curve25519KeyPair selfRatchetKeyPair = PKCrypto.generateKeyPair();

        return new AppSessionState(selfRatchetKeyPair, null, sharedKey, null,
                0, null, 0, 0);
    }

    public static Triplet<byte[], AppHeader, AppSessionState> ratchetEncrypt(AppSessionState state, byte[] plaintext) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] AD = state.getAD();
        byte[][] tmpkeys = CryptoUtils.split(kdf.expand(kdf.deriveKey(state.getSendingChainKey()), 64), 32);
        state.setSendingChainKey(tmpkeys[0]);
        byte[] messageKey = tmpkeys[1];

        List<byte[]> headerList = new ArrayList<>();
        headerList.add(state.getSelfRatchetKeyPair().getPublicKey());

        List<Integer> headerList2 = new ArrayList<>();
        headerList2.add(state.getNPreviousMessages());
        headerList2.add(state.getNSendingMessages());


        AppHeader header = new AppHeader("Encryption header", headerList, headerList2);



        state.setNSendingMessages(state.getNSendingMessages() + 1);



        return new Triplet<>(cipher.encrypt(messageKey, plaintext, AD), header, state);




    }

    public static Pair<byte[], AppSessionState> ratchetDecrypt(AppSessionState state, byte[] ciphertext, AppHeader header) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        byte[] ratchetKey = header.getHeaderValues().get(0);
        int nPrevious = header.getHeaderValues2().get(0);
        int nSending = header.getHeaderValues2().get(1);

        //trySkippedMessageKeys
        Map<Pair<byte[], Integer>, byte[]> map = state.getSkippedMessageKeys();
        if (!map.isEmpty()) {
            for (Pair<byte[], Integer> key : map.keySet()) {
                if (key.getValue0() == ratchetKey && Objects.equals(key.getValue1(), nPrevious)) {
                    byte[] messageKey = map.get(key);
                    map.remove(key);
                    state.setSkippedMessageKeys(map);
                    byte[] plaintext = cipher.decrypt(messageKey, ciphertext, state.getAD());
                    return new Pair<>(plaintext, state);
                }
            }

        }

        if (ratchetKey != state.getReceiverRatchetKey()) {
            AppSessionState state1 = skipMessageKeys(state, nPrevious);

            //RATCHET DH
            state1.setNPreviousMessages(state.getNSendingMessages());
            state1.setNSendingMessages(0);
            state1.setNReceivingMessages(0);
            state1.setReceiverRatchetKey(ratchetKey);
            byte[] kdfOutput = kdf.deriveKey(state.getRootKey(), PKCrypto.keyExchange(state.getReceiverRatchetKey(), state.getSelfRatchetKeyPair().getPrivateKey()));
            byte[][] tmpkeys = CryptoUtils.split(kdf.expand(kdfOutput,64) ,32);
            state1.setRootKey(tmpkeys[0]);
            state1.setReceivingChainKey(tmpkeys[1]);
            state1.setSelfRatchetKeyPair(PKCrypto.generateKeyPair());
            kdfOutput = kdf.deriveKey(state.getRootKey(), PKCrypto.keyExchange(state.getReceiverRatchetKey(), state.getSelfRatchetKeyPair().getPrivateKey()));
            tmpkeys = CryptoUtils.split(kdf.expand(kdfOutput,64) ,32);
            state1.setRootKey(tmpkeys[0]);
            state1.setSendingChainKey(tmpkeys[1]);

        }

        AppSessionState state2 = skipMessageKeys(state, nSending);

        byte[][] tmp = CryptoUtils.split(kdf.expand(kdf.deriveKey(state.getReceivingChainKey()), 64), 32) ;
        state2.setReceivingChainKey(tmp[0]);
        byte[] messageKey = tmp[1];
        state2.setNReceivingMessages(state.getNReceivingMessages() + 1);
        byte[] plaintext = cipher.decrypt(messageKey, ciphertext, state2.getAD());
        return new Pair<>(plaintext, state2);
    }

    private static AppSessionState skipMessageKeys(AppSessionState state, Integer until) throws InvalidKeyException {
        if (state.getReceivingChainKey() != null) {
            while (state.getNReceivingMessages() < until) {
                byte[][] tmp = CryptoUtils.split(kdf.expand(kdf.deriveKey(state.getReceivingChainKey()), 64),32) ;
                state.setReceivingChainKey(tmp[0]);
                byte[] messageKey = tmp[1];
                state.addMessageKey(new Pair<>(state.getReceiverRatchetKey(), state.getNReceivingMessages()), messageKey);
                state.setNReceivingMessages(state.getNReceivingMessages() + 1);

            }
            return state;
        }
        return state;
    }




}
