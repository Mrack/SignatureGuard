package cn.plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode;

import static org.objectweb.asm.Opcodes.ASM5;

/**
 * @author Mrack
 * @date 2021/1/31
 */

public class SignatureGuardVisitor extends ClassVisitor {

    private final String clazz;
    private final SignatureConfig signatureGuard;

    public SignatureGuardVisitor(ClassWriter classWriter, String clazz, SignatureConfig signatureGuard) {
        super(ASM5, classWriter);
        this.clazz = clazz;
        this.signatureGuard = signatureGuard;
    }

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

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = new MethodVisitor(ASM5, super.visitMethod(access, name, desc, signature, exceptions)) {
            @Override
            public void visitCode() {
                String encrypt = "";
                if (name.equals("onCreate") && clazz.equals("androidx/appcompat/app/AppCompatActivity")) {
                    System.out.println("clazz = " + clazz);
                    try (FileInputStream in = new FileInputStream(signatureGuard.getPath())) {

                        KeyStore ks = KeyStore.getInstance("JKS");
                        ks.load(in, signatureGuard.getPassword().toCharArray());
                        Certificate certificate = ks.getCertificate(signatureGuard.getAlias());
                        encrypt = encrypt(certificate.getEncoded());
                        System.out.println("MD5 = " + encrypt);

                        mv.visitVarInsn(Opcodes.ALOAD, 0);
                        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cn/mrack/demo1/SignatureUtils", "getSignMd5", "(Landroid/content/Context;)Ljava/lang/String;", false);
                        mv.visitLdcInsn(encrypt);
                        //SignatureUtils.getSignMd5(this)

                        Label l2 = new Label();
                        Label l3 = new Label();
                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
                        mv.visitJumpInsn(Opcodes.IFNE, l3);
                        //if equal

                        // L2
                        mv.visitLabel(l2);
                        mv.visitVarInsn(Opcodes.ALOAD, 0);
                        mv.visitLdcInsn("\u7b7e\u540d\u5f02\u5e38");
                        mv.visitInsn(Opcodes.ICONST_0);
                        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "android/widget/Toast", "makeText", "(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;", false);
                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/widget/Toast", "show", "()V", false);

                        //  L3
                        mv.visitLabel(l3);
                    } catch (IOException | KeyStoreException e) {
                        e.printStackTrace();

                    } catch (CertificateException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                }

                super.visitCode();
            }
        };
        return mv;
    }

}
