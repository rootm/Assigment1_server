package server;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Base64;

public class AuthServer {
    public static String encrypt(String key,  String value) {
        try {
        	SecureRandom srandom = new SecureRandom(key.getBytes());
            KeyGenerator keygen = KeyGenerator.getInstance("RC4");
            keygen.init(srandom);
            SecretKey secret = keygen.generateKey();
        
            
            Cipher cipher = Cipher.getInstance("RC4");
        
            
            cipher.init(Cipher.ENCRYPT_MODE, secret);
        
            
            byte[] encrypted = cipher.doFinal(value.getBytes());
            System.out.println("encrypted string: "
                    + Base64.encodeBase64String(encrypted));

            return Base64.encodeBase64String(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static String decrypt(String key,  String encrypted) {
        try {
        	SecureRandom srandom = new SecureRandom(key.getBytes());
            KeyGenerator keygen = KeyGenerator.getInstance("RC4");
            keygen.init(srandom);
            SecretKey secret = keygen.generateKey();
        
            // do the decryption with that key
            Cipher cipher = Cipher.getInstance("RC4");
            cipher.init(Cipher.DECRYPT_MODE, secret);
            byte[] decrypted = cipher.doFinal(Base64.decodeBase64(encrypted));
;
            //byte[] original = cipher.doFinal(Base64.decodeBase64(decrypted));

            return new String(decrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
