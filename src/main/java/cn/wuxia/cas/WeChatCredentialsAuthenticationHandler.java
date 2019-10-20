package cn.wuxia.cas;

import cn.wuxia.cas.user.bean.WeChatCredential;
import cn.wuxia.common.util.reflection.BeanUtil;
import org.apache.commons.collections4.MapUtils;
import org.apereo.cas.authentication.*;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Map;

public class WeChatCredentialsAuthenticationHandler extends AbstractAuthenticationHandler {
    Logger logger = LoggerFactory.getLogger(WeChatCredentialsAuthenticationHandler.class);
    protected String sql;
    protected String disabledFieldName;
    private final JdbcTemplate jdbcTemplate;


    public WeChatCredentialsAuthenticationHandler(final DataSource dataSource) {
        super(null, null, null, Integer.MIN_VALUE);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public WeChatCredentialsAuthenticationHandler(String simpleName, ServicesManager servicesManager, DefaultPrincipalFactory defaultPrincipalFactory, int order, DriverManagerDataSource dataSource) {
        super(simpleName, servicesManager, defaultPrincipalFactory, order);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @Override
    public AuthenticationHandlerExecutionResult authenticate(final Credential credential) throws GeneralSecurityException {
        logger.info("income authenticate.......");
        if (credential == null) {
            logger.warn("非微信登录走普通登录路径");
            throw new FailedLoginException();
        }
        final WeChatCredential weChatCredential = (WeChatCredential) credential;

        Map<String, Object> returnMap = BeanUtil.beanToMap(weChatCredential.getWxUserInfo());
        logger.info("用户的登录信息为{}", returnMap);
        if (MapUtils.isEmpty(returnMap)) {
            throw new FailedLoginException("无法获取用户信息");
        }
//        String sql = this.sql;
//        Map<String, Object> returnMap = new HashMap<>();
//        try {
//
//            System.out.println(sql);
//            final Map<String, Object> values = getJdbcTemplate().queryForMap(sql, weChatCredential.getId());
//
//            if (MapUtil.isEmpty(values)) {
//                // 删除
//                throw new AccountNotFoundException();
//            }
//            final Object status = values.get(disabledFieldName);
//            if (status != null) {
//                // 禁用
//                throw new AccountDisabledException();
//            }
//            returnMap.put("casAccountId", values.get("id"));
//            returnMap.put("accountName", values.get("accountName"));
//        } catch (final IncorrectResultSizeDataAccessException e) {
//            if (e.getActualSize() == 0) {
//                throw new AccountNotFoundException(weChatCredential.getId() + " not found with SQL query");
//            } else {
//                throw new FailedLoginException("Multiple records found for " + weChatCredential.getId());
//            }
//        }
        logger.info("返回用户信息：{}", weChatCredential.getId());
        Principal principal = principalFactory.createPrincipal(weChatCredential.getId(), returnMap);
        return new DefaultAuthenticationHandlerExecutionResult(this, new BasicCredentialMetaData(credential), principal, new ArrayList(0));
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof WeChatCredential;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getDisabledFieldName() {
        return disabledFieldName;
    }

    public void setDisabledFieldName(String disabledFieldName) {
        this.disabledFieldName = disabledFieldName;
    }
}
