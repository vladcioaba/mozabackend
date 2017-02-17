package com.mozaicgames.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;


public class AdvancedEncryptionStandard {

	private String encryptionKey;
	private String encryptionAlgorithm = "SHA";
    
    public AdvancedEncryptionStandard(String encryptionKey, String encryptionAlgorithm)
    {
        this.encryptionKey = encryptionKey;
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    public String encrypt(String plainText) throws Exception
    {
        Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.encodeBase64String(encryptedBytes);
    }

    public String decrypt(String encrypted) throws Exception
    {
        Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
        byte[] plainBytes = cipher.doFinal(Base64.decodeBase64(encrypted));
        return new String(plainBytes);
    }

    private Cipher getCipher(int cipherMode) throws Exception
    {
        SecretKeySpec keySpecification = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), encryptionAlgorithm);
        Cipher cipher = Cipher.getInstance(encryptionAlgorithm);
        cipher.init(cipherMode, keySpecification);

        return cipher;
    }
    
}
