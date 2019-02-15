/*
 * Created on :2013-6-21 Author :songlin.li Change History Version Date Author
 * Reason <Ver.No> <date> <who modify> <reason>
 */
package cn.wuxia.cas;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.ConfigurableHashService;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.HashRequest;
import org.apache.shiro.util.ByteSource;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.AbstractPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class MyPasswordEncoder implements PasswordEncoder {

    /**
     * create a salt
     * 
     * @author songlin
     * @return
     */
    public String getGenerateSalt() {
        return KeyGenerators.string().generateKey();
    }



    public static void main(String[] args) {
        MyPasswordEncoder encoder = new MyPasswordEncoder();
        //String salt = encoder.getGenerateSalt();
        //System.out.println("      "+encoder.encodePassword("qwe123", "00fd0a874365621f"));
//        System.out.println(encoder.isPasswordValid("271094cddf97f5e1cfd90ccdd4fa8f7d", "8888", "dU+Kmb1x1maHPJ7yJX3uZH4jJc12Q47SPj8uFpcJYtg="));
        // System.out.println(encoder.matches(encoder.encode("1234"), "1234"));
        // System.out.println(encoder.matches("12345", "1234"));
    }



    @Override
    public String encode(CharSequence encodePassword) {
        ConfigurableHashService hashService = new DefaultHashService();
        hashService.setHashAlgorithmName("MD5");
        hashService.setHashIterations(1024);
        hashService.setPrivateSalt(ByteSource.Util.bytes("."));
        HashRequest request = (new HashRequest.Builder()).setSource(encodePassword).build();
        return hashService.computeHash(request).toHex();
    }

    @Override
    public boolean matches(CharSequence charSequence, String s) {
        return StringUtils.equals(encode(charSequence), s);
    }
}
