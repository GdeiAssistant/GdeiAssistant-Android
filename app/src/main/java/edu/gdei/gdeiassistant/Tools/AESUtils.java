package edu.gdei.gdeiassistant.Tools;

import android.util.Base64;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {

    /**
     * 生成AES密钥
     *
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static byte[] GenerateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(256);
        return kgen.generateKey().getEncoded();
    }

    /**
     * 加密字节数组
     *
     * @param secretKeyByte
     * @param content
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public static byte[] EncryptByte(byte[] secretKeyByte, byte[] content) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        //通过secretKeyByte生成SecretKeySpec
        SecretKeySpec key = new SecretKeySpec(secretKeyByte, "AES");
        //Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        //用密匙初始化Cipher对象
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(Base64.encodeToString(secretKeyByte, Base64.NO_WRAP)
                .substring(0, 16).getBytes()));
        //执行加密操作
        return cipher.doFinal(content);
    }

    /**
     * 解密字节数组
     *
     * @param secretKeyByte
     * @param content
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public static byte[] DecryptByte(byte[] secretKeyByte, byte[] content) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {

        //通过secretKeyByte生成SecretKeySpec
        SecretKeySpec key = new SecretKeySpec(secretKeyByte, "AES");
        //Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        //用密匙初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(Base64.encodeToString(secretKeyByte, Base64.NO_WRAP)
                .substring(0, 16).getBytes()));
        //执行加密操作
        return cipher.doFinal(content);
    }
}
