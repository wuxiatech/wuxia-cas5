
import org.apache.shiro.crypto.hash.ConfigurableHashService;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.HashRequest;
import org.apache.shiro.crypto.hash.HashRequest.Builder;
import org.apache.shiro.util.ByteSource.Util;
public class MainTest {
    public static void main(String[] args) {
        ConfigurableHashService hashService = new DefaultHashService();
        hashService.setHashAlgorithmName("MD5");
        hashService.setHashIterations(1024);
        HashRequest request = (new Builder()).setSalt("88888888").setSource("qwe123").build();
        System.out.println(hashService.computeHash(request).toHex());
    }
}
