package com.example.gdrivepasswordvault;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Encryption {


    public static String md5(byte [] s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s);
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static byte[] blowfish(byte[] message, String key, boolean encryptordecrypt){

        byte[] data = null;
        try {
            KeyGenerator keygenerator = KeyGenerator.getInstance("Blowfish");
            SecretKeySpec KS = new SecretKeySpec(key.getBytes(), "Blowfish");
            Cipher cipher = Cipher.getInstance("Blowfish");

            if(encryptordecrypt) {
                cipher.init(Cipher.ENCRYPT_MODE, KS);
            }else{
                cipher.init(Cipher.DECRYPT_MODE, KS);
            }

            data = cipher.doFinal(message);

            System.out.println("encrypt ok");
        } catch (Exception e){
            System.out.println("encrypt error");
        }

        return data;
    }
}
