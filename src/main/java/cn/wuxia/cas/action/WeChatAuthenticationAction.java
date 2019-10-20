package cn.wuxia.cas.action;

import cn.wuxia.cas.user.bean.WeChatCredential;
import cn.wuxia.common.util.JsonUtil;
import cn.wuxia.common.util.ServletUtils;
import cn.wuxia.common.util.StringUtil;
import cn.wuxia.wechat.BasicAccount;
import cn.wuxia.wechat.WeChatException;
import cn.wuxia.wechat.oauth.bean.AuthUserInfoBean;
import cn.wuxia.wechat.oauth.bean.OAuthTokeVo;
import cn.wuxia.wechat.oauth.util.LoginUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.actions.AbstractNonInteractiveCredentialsAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author songlin
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class WeChatAuthenticationAction extends AbstractNonInteractiveCredentialsAction {
    Logger logger = LoggerFactory.getLogger(WeChatAuthenticationAction.class);
//    @Value("${cas.authn.pac4j.oauth2[0].id}")
    private String appid="wx98022f921a4c511a";
//    @Value("${cas.authn.pac4j.oauth2[0].secret}")
    private String secret="f0fbbf51b1ea3bdd3baff7a766b3f9ef";

    public WeChatAuthenticationAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                      final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                      final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
    }

    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext requestContext) {
        logger.info("income WxAction....");
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        Map<String, Object> map = ServletUtils.getParametersMap(request);
        if (MapUtils.isNotEmpty(map)) {
            System.out.println("=============" + JsonUtil.toJson(map));
        }
        if (StringUtil.isNotBlank(MapUtils.getString(map, "appid")) && StringUtil.isNotBlank(MapUtils.getString(map, "code")) && StringUtil.isNotBlank(MapUtils.getString(map, "state"))) {
            OAuthTokeVo oauthToken = null;
            BasicAccount account = new BasicAccount(appid, secret);
            AuthUserInfoBean authUserInfoBean = null;
            try {
                oauthToken = LoginUtil.authUser(account, MapUtils.getString(map, "code"));
                logger.info("wx login info:{}", oauthToken);
                System.out.println(""+ToStringBuilder.reflectionToString(oauthToken));
//                if (StringUtil.isNotBlank(oauthToken.getUnionId())) {
//                    authUserInfoBean.setUnionid(oauthToken.getUnionId());
//                } else {
                authUserInfoBean = LoginUtil.getAuthUserInfo(oauthToken);
//                }
                authUserInfoBean.setAppid(account.getAppid());
            } catch (WeChatException e) {
                logger.error("获取用户信息失败", e);
                e.printStackTrace();
            }
            logger.info("auth info {}", authUserInfoBean);
            return new WeChatCredential(authUserInfoBean);
        } else {
            logger.info("goto normal login...");
            return null;
        }
    }
}
