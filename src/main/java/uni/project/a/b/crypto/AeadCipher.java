package uni.project.a.b.crypto;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;

/**
 * This class represent the use of the use Authenticated encryption with associated data (AEAD).
 */

@Slf4j
public class AeadCipher {

    private final Cipher cipher;

    private static final byte[] nonce = "secret".getBytes(StandardCharsets.UTF_8);

    @SneakyThrows
    public AeadCipher() {
        this.cipher = Cipher.getInstance("AES/GCM/NoPadding");
    }



    public byte[] encrypt(byte[] key, byte[] plaintext, byte[] associatedData) throws InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Key aesKey = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(128, nonce));
        cipher.updateAAD(associatedData);
        return cipher.doFinal(plaintext);
    }


    public byte[] decrypt(byte[] key, byte[] ciphertext, byte[] associatedData) throws InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Key aesKey = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(128, nonce));
        cipher.updateAAD(associatedData);
        return cipher.doFinal(ciphertext);

    }

}
