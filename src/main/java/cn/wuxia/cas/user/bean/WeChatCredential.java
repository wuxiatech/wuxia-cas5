package cn.wuxia.cas.user.bean;

import cn.wuxia.wechat.oauth.bean.AuthUserInfoBean;
import org.apereo.cas.authentication.AbstractCredential;

/**
 * 微信 web 扫码 凭据
 * @author songlin
 */
public class WeChatCredential extends AbstractCredential {
    private static final long serialVersionUID = -5267305577597473795L;
    /**
     * 微信API配置属性
     */
    private AuthUserInfoBean wxUserInfo;

    public WeChatCredential(AuthUserInfoBean wxUserInfo) {
        this.wxUserInfo = wxUserInfo;
    }

    public AuthUserInfoBean getWxUserInfo() {
        return wxUserInfo;
    }

    public void setWxUserInfo(AuthUserInfoBean wxUserInfo) {
        this.wxUserInfo = wxUserInfo;
    }

    @Override
    public String getId() {
        return wxUserInfo.getOpenid();
    }

}
