/**
 * 后台登录
 */
package cn.wuxia.cas.user.web;

import cn.wuxia.cas.user.bean.MyUsernamePasswordCredential;
import cn.wuxia.common.util.EncodeUtils;
import cn.wuxia.common.util.JsonUtil;
import cn.wuxia.common.util.StringUtil;
import cn.wuxia.wechat.BasicAccount;
import cn.wuxia.wechat.WeChatException;
import cn.wuxia.wechat.oauth.bean.AuthUserInfoBean;
import cn.wuxia.wechat.oauth.bean.OAuthTokeVo;
import cn.wuxia.wechat.oauth.util.LoginUtil;
import com.google.common.collect.Lists;
import nl.captcha.Captcha;
import nl.captcha.backgrounds.GradiatedBackgroundProducer;
import nl.captcha.gimpy.BlockGimpyRenderer;
import nl.captcha.gimpy.DropShadowGimpyRenderer;
import nl.captcha.gimpy.FishEyeGimpyRenderer;
import nl.captcha.noise.StraightLineNoiseProducer;
import nl.captcha.servlet.CaptchaServletUtil;
import nl.captcha.text.renderer.DefaultWordRenderer;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

//import org.springframework.webflow.execution.RequestContext;
//import org.springframework.webflow.execution.RequestContextHolder;

/**
 * @author songlin.li
 * @ClassName: RegisterController
 * @Description: TODO
 * @date
 */
@Controller
public class RegisterAfterLoginController extends AbstractController {

    private CentralAuthenticationService centralAuthenticationService;

    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
    @Resource
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Override
    @RequestMapping("/registerLogin")
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView signinView = new ModelAndView();
        String username = request.getParameter("username");
        /**
         * hex加密后再Base64后的字符串
         */
        String password = request.getParameter("password");
        try {
            password = new String(EncodeUtils.base64Decode(password));
            password = new String(EncodeUtils.hexDecode(password));
        } catch (Exception e) {
//            logger.error("密码有误！", e);
        }
        bindTicketGrantingTicket("18588648559", "qwe123", request, response);
        String viewName = getSignInView(request);
        System.out.println("viewName=====" + viewName);
        signinView.setViewName(getSignInView(request));

        return signinView;
    }

    /**
     * Invoke generate validate Tickets and add the TGT to cookie.
     *
     * @param loginName     the user login name.
     * @param loginPassword the user login password.
     * @param request       the HttpServletRequest object.
     * @param response      the HttpServletResponse object.
     */
    protected void bindTicketGrantingTicket(String loginName, String loginPassword, HttpServletRequest request, HttpServletResponse response) {
        try {
            RequestContext context = RequestContextHolder.getRequestContext();
            System.out.println("context============" + context);
            MyUsernamePasswordCredential credentials = new MyUsernamePasswordCredential();
            credentials.setUsername(loginName);
            credentials.setPassword(loginPassword);
            Service service = WebUtils.getService(context);
            System.out.println("service==========" + service);
            AuthenticationResultBuilder authenticationResultBuilder = WebUtils.getAuthenticationResultBuilder(context);
            authenticationResultBuilder.collect(credentials);
//            LOGGER.debug("Finalizing authentication transactions and issuing ticket-granting ticket");
            AuthenticationResult authenticationResult = this.authenticationSystemSupport.finalizeAllAuthenticationTransactions(authenticationResultBuilder, service);

            Authentication authentication = this.buildFinalAuthentication(authenticationResult);


            String ticketGrantingTicket = centralAuthenticationService.createTicketGrantingTicket(authenticationResult).getId();
            System.out.println("ticketGrantingTicket===" + ticketGrantingTicket);
            ticketGrantingTicketCookieGenerator.addCookie(request, response, ticketGrantingTicket);
        } catch (Exception e) {
//            logger.error("Validate the login name " + loginName + " failure, can't bind the TGT!", e);
        }
    }

    /**
     * Get the signIn view URL.
     *
     * @param request the HttpServletRequest object.
     * @return redirect URL
     */
    protected String getSignInView(HttpServletRequest request) {
        String service = ServletRequestUtils.getStringParameter(request, "service", "");
        return ("redirect:login" + (service.length() > 0 ? "?service=" + service : ""));
    }


    public CentralAuthenticationService getCentralAuthenticationService() {
        return centralAuthenticationService;
    }

    @Autowired
    @Qualifier("centralAuthenticationService")
    public void setCentralAuthenticationService(CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    public CookieRetrievingCookieGenerator getTicketGrantingTicketCookieGenerator() {
        return ticketGrantingTicketCookieGenerator;
    }

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    public void setTicketGrantingTicketCookieGenerator(CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator) {
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
    }

    protected Authentication buildFinalAuthentication(AuthenticationResult authenticationResult) {
        return authenticationResult.getAuthentication();
    }

    @Value("${cas.authn.pac4j.oauth2[0].id}")
    private String appid;
    @Value("${cas.authn.pac4j.oauth2[0].secret}")
    private String secret;

    //进入绑定手机页面
    @RequestMapping(value = "/pc/login")
    public String bind(String code) {
        OAuthTokeVo oauthToken = null;
        BasicAccount account = new BasicAccount(appid, secret);
        try {
            oauthToken = LoginUtil.authUser(account, code);
            AuthUserInfoBean authUserInfoBean = LoginUtil.getAuthUserInfo(oauthToken);
            System.out.println("lllllllllllll==" + JsonUtil.toJson(authUserInfoBean));
        } catch (WeChatException e) {
            e.printStackTrace();
        }

        String callbackUrl = "http://admin.doctorm.cn";
//        try {
//            callbackUrl = new String(EncodeUtils.base64Decode(callbackUrl), "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        return "redirect:/registerLogin?service=" + callbackUrl;
        return "redirect:/weChatAuthenticationCheck";
    }


    /**
     * 注册的验证码
     *
     * @param session
     * @param resp
     * @author Wind.Zhao
     * @date 2015/06/12
     */
    @RequestMapping(value = "/loginVerifyCode", method = RequestMethod.GET)
    public void registerVerifyCode(HttpSession session, HttpServletResponse resp) {
        Captcha.Builder builder = new Captcha.Builder(160, 50);
        GradiatedBackgroundProducer gbp = new GradiatedBackgroundProducer();
        gbp.setFromColor(new Color(37, 224, 199));
        gbp.setToColor(new Color(119, 211, 39));
        builder.addBackground(gbp);
        builder.addNoise(new StraightLineNoiseProducer(new Color(55, 210, 38), 3));
        // 字体边框齿轮效果 默认是3
        builder.gimp(new BlockGimpyRenderer(-20));
        //加网--第一个参数是横线颜色，第二个参数是竖线颜色
        builder.gimp(new FishEyeGimpyRenderer(Color.gray, Color.green));
        //加入阴影效果 默认3，75
        builder.gimp(new DropShadowGimpyRenderer());

        //自定义设置字体颜色和大小 最简单的效果 多种字体随机显示
        List<Font> fontList = new ArrayList<Font>();
        fontList.add(new Font("Arial", Font.HANGING_BASELINE, 40));//可以设置斜体之类的
        fontList.add(new Font("Courier", Font.BOLD, 40));
        DefaultWordRenderer dwr = new DefaultWordRenderer(Lists.newArrayList(Color.LIGHT_GRAY, Color.GRAY, Color.green), fontList);
        Captcha captcha = builder.addText(dwr).build();
        CaptchaServletUtil.writeImage(resp, captcha.getImage());
        session.setAttribute("login_captcha", captcha.getAnswer());
    }


    /**
     * 检测注册验证码是否正确
     *
     * @param verifycode 用户填写的验证码
     * @return 成功返回true，是否返回false
     * @author Wind.Zhao
     * @date 2015/06/12
     */
    @RequestMapping(value = "/checkVerifyCode", method = RequestMethod.POST)
    public @ResponseBody
    boolean checkRegisterVerifyCode(HttpSession httpSession, String verifycode) {
        String cap = (String) httpSession.getAttribute("login_captcha");
        boolean checkResult = false;
        if (cap != null) {
            checkResult = StringUtil.equalsIgnoreCase(cap.toLowerCase(), verifycode.toLowerCase());
        }
        return checkResult;
    }
}
