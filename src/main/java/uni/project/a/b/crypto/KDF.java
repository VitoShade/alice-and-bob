package uni.project.a.b.crypto;
import lombok.SneakyThrows;
import uni.project.a.b.utils.KeySpec;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class KDF {

    private final Mac mac;

    @SneakyThrows
    public KDF(){
        this.mac = Mac.getInstance("HmacSHA256");
    }

    public byte[] deriveKey(byte[] key, byte[] seed) throws InvalidKeyException
    {
        mac.init(new SecretKeySpec(key,"HmacSHA256"));
        return mac.doFinal(seed);
    }

    public byte[] deriveKey(byte[] key) throws InvalidKeyException
    {
        mac.init(new SecretKeySpec(key,"HmacSHA256"));
        // TODO: implement secure keystore!!!
        byte[] seed = "secret".getBytes(StandardCharsets.UTF_8);
        return mac.doFinal(seed);
    }

    //TODO: We need an EXPAND function!

    public byte[] expand(byte[] derivedKey, int outputSize) throws InvalidKeyException {
        int iterations = (int) Math.ceil((double) outputSize / (double) 32);
        byte[] mixin = new byte[0];
        ByteArrayOutputStream results = new ByteArrayOutputStream();
        int remainingBytes = outputSize;

        for (int i = 1; i < iterations + 1; i++) {
            mac.init(new SecretKeySpec(derivedKey, "HmacSHA256"));
            mac.update(mixin);
            mac.update((byte) i);
            byte[] stepResult = mac.doFinal();
            int stepSize = Math.min(remainingBytes, stepResult.length);
            results.write(stepResult, 0, stepSize);
            mixin = stepResult;
            remainingBytes -= stepSize;
        }
        return results.toByteArray();
    }

}
