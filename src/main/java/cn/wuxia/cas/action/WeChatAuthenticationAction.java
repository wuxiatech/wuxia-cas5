package cn.wuxia.cas.action;

import cn.wuxia.cas.user.bean.WeChatCredential;
import cn.wuxia.common.util.ServletUtils;
import cn.wuxia.common.util.StringUtil;
import cn.wuxia.wechat.BasicAccount;
import cn.wuxia.wechat.WeChatException;
import cn.wuxia.wechat.oauth.bean.AuthUserInfoBean;
import cn.wuxia.wechat.oauth.bean.OAuthTokeVo;
import cn.wuxia.wechat.oauth.util.LoginUtil;
import org.apache.commons.collections4.MapUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.actions.AbstractNonInteractiveCredentialsAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@EnableConfigurationProperties(CasConfigurationProperties.class)
public class WeChatAuthenticationAction extends AbstractNonInteractiveCredentialsAction {

    @Value("${cas.authn.pac4j.oauth2[0].id}")
    private String appid;
    @Value("${cas.authn.pac4j.oauth2[0].secret}")
    private String secret;

    public WeChatAuthenticationAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                      final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                      final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
    }

    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext requestContext) {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        Map<String, Object> map = ServletUtils.getParametersMap(request);
        if (StringUtil.isNotBlank(MapUtils.getString(map, "code")) && StringUtil.isNotBlank(MapUtils.getString(map, "status"))) {
            OAuthTokeVo oauthToken = null;
            BasicAccount account = new BasicAccount(appid, secret);
            AuthUserInfoBean authUserInfoBean = new AuthUserInfoBean();
            try {
                oauthToken = LoginUtil.authUser(account, MapUtils.getString(map, "code"));
                if (StringUtil.isNotBlank(oauthToken.getUnionId())) {
                    authUserInfoBean.setUnionid(oauthToken.getUnionId());
                } else {
                    authUserInfoBean = LoginUtil.getAuthUserInfo(oauthToken);
                }
            } catch (WeChatException e) {
                e.printStackTrace();
            }
            return new WeChatCredential(authUserInfoBean);
        } else {
            return null;
        }
    }
}
