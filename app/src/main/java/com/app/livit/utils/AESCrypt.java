package com.app.livit.utils;

import android.util.Base64;

import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Rémi OLLIVIER on 09/07/2018.
 */

public class AESCrypt {
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final String KEY = "of-iezLivvitK48A";

    /**
     * This method is used to encrypt the passwords to save in shared preferences
     * @param value the password to encrypt
     * @return the encrypted password
     * @throws Exception the type can change
     */
    public static String encrypt(String value) throws Exception {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(AESCrypt.ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte [] encryptedByteValue = cipher.doFinal(value.getBytes("utf-8"));
        return Base64.encodeToString(encryptedByteValue, Base64.DEFAULT);

    }

    /**
     * This method is used to decrypt the passwords saved in shared preferences
     * @param value the password to decrypt
     * @return the decrypted password
     * @throws Exception the type can change
     */
    public static String decrypt(String value) throws Exception {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(AESCrypt.ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedValue64 = Base64.decode(value, Base64.DEFAULT);
        byte [] decryptedByteValue = cipher.doFinal(decryptedValue64);
        return new String(decryptedByteValue,"utf-8");

    }

    /**
     * This method generates a key
     * @return the generated key
     */
    private static Key generateKey() {
        return new SecretKeySpec(AESCrypt.KEY.getBytes(), AESCrypt.ALGORITHM);
    }
}
