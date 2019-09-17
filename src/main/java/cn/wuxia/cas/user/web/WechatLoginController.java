
package cn.wuxia.cas.user.web;

import cn.wuxia.common.util.EncodeUtils;
import cn.wuxia.common.util.StringUtil;
import cn.wuxia.wechat.oauth.util.LoginUtil;
import cn.wuxia.wechat.open.util.ProxyLoginUtil;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author songlin.li
 * @ClassName: WechatLoginController
 * @Description: TODO
 * @date
 */
@Controller
public class WechatLoginController extends AbstractController {
    @Value("${cas.server.name}")
    private String ctx;

    /**
     * 因微信局限了oauth请求授权的页面，故需要借助cas来做跳板获取openid从而获取当前用户
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     * @author songlin
     */
    @Override
    @RequestMapping("wechatOauth")
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String code = request.getParameter("code");
        /**
         * 如果为非空，则是由微信授权成功并返回的参数，否则是由代授权系统请求
         */
        if (StringUtil.isNotBlank(code)) {
            String callback = request.getParameter("callback");
            logger.info("===============encode callback=" + callback + " ; wxcode=" + code);
            /**
             * 因为授权前callback已被加密，则需要解密才货到原始返回url
             */
            callback = new String(EncodeUtils.base64Decode(callback));
            logger.info("===============decode callback=" + callback + " ; wxcode=" + code);
            if (callback.indexOf("?") > 0) {
                callback += "&wxcode=" + code;
            } else {
                callback += "?wxcode=" + code;
            }
            logger.info("返回来源地址：" + callback);
            response.addHeader("wxcode", code);
            return new ModelAndView("redirect:" + callback);
        } else {
            String callback = request.getParameter("callback");
            String appid = request.getParameter("wxappid");
            String wxopenappid = request.getParameter("wxopenappid");
            /**
             * 是否非静默
             */
            boolean scope = StringUtil.isBlank(request.getParameter("scope")) ? false : BooleanUtils.toBoolean(request.getParameter("scope"));
            /**
             * 微信授权仅接受域名与80端口
             */
            final String uri = request.getScheme() + "://" + request.getServerName() + "/wechatOauth";
            logger.info("微信授权方式：false静默授权，true非静默授权 -------》" + scope + ",===" + uri);

            /**
             * 微信授权的具体url，带加密的参数，代授权成功后需要解密
             * 当前只需要传递返回来源url
             */
            String shouquanUrl = new StringBuffer(uri).append("?").append("callback=" + callback).toString();
            logger.info("授权url-------->>>" + shouquanUrl.toString());

            if (StringUtil.isNotBlank(wxopenappid)) {
                shouquanUrl = ProxyLoginUtil.oauth2(appid, wxopenappid, shouquanUrl, scope);
            } else {
                shouquanUrl = LoginUtil.oauth2(appid, shouquanUrl, scope);
            }
            logger.info("跳转到微信授权完整url-----》》》》》" + shouquanUrl);
            return new ModelAndView("redirect:" + shouquanUrl);
        }
    }
}
