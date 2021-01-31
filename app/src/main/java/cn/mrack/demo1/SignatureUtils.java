package cn.mrack.demo1;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.security.MessageDigest;

/**
 * @author Mrack
 * @date 2021/1/31
 */
public class SignatureUtils {
    public static String encrypt(byte[] data) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(data);
            byte s[] = m.digest();
            String result = "";
            for (int i = 0; i < s.length; i++) {
                result += Integer.toHexString((0x000000FF & s[i]) | 0xFFFFFF00).substring(6);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String getSignMd5(Context context) {
        String encrypt = "";
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            encrypt = encrypt(cert);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encrypt;
    }
}
