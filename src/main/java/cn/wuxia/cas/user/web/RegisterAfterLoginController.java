/**
 * 后台登录
 */
package cn.wuxia.cas.user.web;

import cn.wuxia.cas.user.bean.MyUsernamePasswordCredential;
import cn.wuxia.common.util.EncodeUtils;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.*;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName: RegisterController
 * @Description: TODO
 * @author songlin.li
 * @date
 * 
 */
@Controller
public class RegisterAfterLoginController extends AbstractController {

    private CentralAuthenticationService centralAuthenticationService;

    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
    @Resource
    private AuthenticationSystemSupport authenticationSystemSupport;
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
            logger.error("密码有误！", e);
        }
        bindTicketGrantingTicket(username, password, request, response);
        String viewName = getSignInView(request);
        signinView.setViewName(getSignInView(request));

        return signinView;
    }

    /**
     * Invoke generate validate Tickets and add the TGT to cookie.
     * 
     * @param loginName
     *            the user login name.
     * @param loginPassword
     *            the user login password.
     * @param request
     *            the HttpServletRequest object.
     * @param response
     *            the HttpServletResponse object.
     */
    protected void bindTicketGrantingTicket(String loginName, String loginPassword, HttpServletRequest request, HttpServletResponse response) {
        try {
          RequestContext context = RequestContextHolder.getRequestContext();
            MyUsernamePasswordCredential credentials = new MyUsernamePasswordCredential();
            credentials.setUsername(loginName);
            credentials.setPassword(loginPassword);
            Service service = WebUtils.getService(context);
            AuthenticationResultBuilder authenticationResultBuilder = WebUtils.getAuthenticationResultBuilder(context);
            authenticationResultBuilder.collect(credentials);
//            LOGGER.debug("Finalizing authentication transactions and issuing ticket-granting ticket");
            AuthenticationResult authenticationResult = this.authenticationSystemSupport.finalizeAllAuthenticationTransactions(authenticationResultBuilder, service);

            Authentication authentication = this.buildFinalAuthentication(authenticationResult);



            String ticketGrantingTicket = centralAuthenticationService.createTicketGrantingTicket(authenticationResult).getId();
            ticketGrantingTicketCookieGenerator.addCookie(request, response, ticketGrantingTicket);
        } catch (Exception e) {
            logger.error("Validate the login name " + loginName + " failure, can't bind the TGT!", e);
        }
    }

    /**
     * Get the signIn view URL.
     * 
     * @param request
     *            the HttpServletRequest object.
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
}
