package cn.wuxia.cas.user.bean;


import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.DefaultAuthenticationResult;
import org.apereo.cas.authentication.RememberMeUsernamePasswordCredential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Service;

public class MyUsernamePasswordCredential extends RememberMeUsernamePasswordCredential {
    private static final long serialVersionUID = 3687037369706159929L;

    String captcha;

    String platform;


    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
