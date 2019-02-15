/*
* Created on :15 May, 2015
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 武侠科技 All right reserved.
*/
package cn.wuxia.cas.user.web;

import cn.wuxia.common.util.EncodeUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.util.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author Ethan
 * @version 2014/8/15
 */
public class JoinLoginAction{ //extends AbstractAction implements MessageSourceAware {
//    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
//
//    private CentralAuthenticationService centralAuthenticationService;
//
//    private MessageSource messageSource;
//
//    private boolean pathPopulated;
//
//    private String callbackUrl;
//
//    @Override
//    protected Event doExecute(final RequestContext context) {
//        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
//        String username = request.getParameter("username");
//        String password = request.getParameter("password");
//        password = new String(EncodeUtils.hexDecode(new String(EncodeUtils.base64Decode(password))));
//        // if (userInfo == null) {
//        //putMessageInScope("common.session.expire", "join", "会话已过期，请手动登录", messageSource, context);
//        //return newEvent(this, "login");
//        //}
//        if(messageSource  != null){}
//        final UsernamePasswordCredential credential = new UsernamePasswordCredential(username, password);
//        try {
//            if (!this.pathPopulated) {
//                final String contextPath = context.getExternalContext().getContextPath();
//                final String cookiePath = StringUtils.hasText(contextPath) ? contextPath + "/" : "/";
//                logger.info("Setting path for cookies to: " + cookiePath);
//                this.ticketGrantingTicketCookieGenerator.setCookiePath(cookiePath);
//                this.pathPopulated = true;
//            }
//            final String ticketGrantingTicketId = this.centralAuthenticationService.createTicketGrantingTicket(credential).getId();
//            final String ticketGrantingTicketValueFromCookie = (String) context.getFlowScope().get("ticketGrantingTicketId");
//            if ((ticketGrantingTicketValueFromCookie != null) && !ticketGrantingTicketId.equals(ticketGrantingTicketValueFromCookie)) {
//                this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicketValueFromCookie);
//            }
//            this.ticketGrantingTicketCookieGenerator.addCookie(request, WebUtils.getHttpServletResponse(context), ticketGrantingTicketId);
//            WebUtils.putTicketGrantingTicketInScopes(context, this.centralAuthenticationService.createTicketGrantingTicket(credential));
//            /*
//             * Constructor<SimpleWebApplicationServiceImpl> constructor = SimpleWebApplicationServiceImpl.class
//             * .getDeclaredConstructor(String.class, String.class, String.class, ResponseType.class);
//             * ReflectionUtils.makeAccessible(constructor);
//             */
//            /**
//             * id=clean后的service origin=service arti=ticket参数
//             * 1、http://test.eteams.cn:9080/j_spring_cas_security_check
//             * 2、http://test.eteams.cn:9080/j_spring_cas_security_check;jsessionid=86446ECD2A6ED0FBAEF11229967F1A1A
//             * 3、null
//             */
//            /*
//             * Service service = constructor.newInstance(callbackUrl,
//             * callbackUrl,
//             * null, ResponseType.REDIRECT);
//             */
//            Service service = new SimpleWebApplicationServiceImpl(callbackUrl);
//            context.getFlowScope().put("service", service);
//
//            final String serviceTicketId = this.centralAuthenticationService.grantServiceTicket(ticketGrantingTicketId, service).getId();
//            final String successUrl = callbackUrl + "?ticket=" + serviceTicketId;
//            context.getFlowScope().put("successUrl", successUrl);
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            //putMessageInScope("common.account.error", "tenant", "账号错误，请重新登录", messageSource, context);
//            //return ActionUtils.newEvent(this, "login");
//        }
//        return success();
//    }
//
//    public void setTicketGrantingTicketCookieGenerator(final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator) {
//        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
//    }
//
//    public void setCentralAuthenticationService(final CentralAuthenticationService centralAuthenticationService) {
//        this.centralAuthenticationService = centralAuthenticationService;
//    }
//
//    @Override
//    public void setMessageSource(MessageSource messageSource) {
//        this.messageSource = messageSource;
//    }
//
//    public void setCallbackUrl(String callbackUrl) {
//        this.callbackUrl = callbackUrl;
//    }
}
