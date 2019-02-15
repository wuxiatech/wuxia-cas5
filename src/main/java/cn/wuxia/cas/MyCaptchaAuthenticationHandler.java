package cn.wuxia.cas;

import cn.wuxia.cas.user.bean.MyUsernamePasswordCredential;
import cn.wuxia.common.util.MapUtil;
import cn.wuxia.common.util.StringUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.ConfigurableHashService;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.HashRequest;
import org.apache.shiro.util.ByteSource;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.nutz.dao.Sqls;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

public class MyCaptchaAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private static final String DEFAULT_PASSWORD_FIELD = "password";

    private static final String DEFAULT_SALT_FIELD = "salt";

    private static final long DEFAULT_ITERATIONS = 1024;

    //    @Value("${cas.authn.jdbc.custom.numberOfIterations}")
    private Long numberOfIterations = DEFAULT_ITERATIONS;
    //该列名的值可替代上面的值，但对密码加密时必须取该值进行处理
//    @Value("${cas.authn.jdbc.custom.numberOfIterationsFieldName}")
    private String numberOfIterationsFieldName;

    //    @Value("${cas.authn.jdbc.custom.saltFieldName}")
    private String saltFieldName;
    //对处理盐值后的算法
//    @Value("${cas.authn.jdbc.custom.algorithmName}")
    protected String algorithmName = "MD5";
    //    @Value("${cas.authn.jdbc.custom.sql}")
    protected String sql;
    //    @Value("${cas.authn.jdbc.custom.passwordFieldName}")
    protected String passwordFieldName;
    //    @Value("${cas.authn.jdbc.custom.expiredFieldName}")
    protected String expiredFieldName;
    //    @Value("${cas.authn.jdbc.custom.disabledFieldName}")
    protected String disabledFieldName;
    //静态盐值
//    @Value("${cas.authn.jdbc.custom.staticSalt}")
    String staticSalt;

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final DataSource dataSource;

    public MyCaptchaAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory, final Integer order, final DataSource dataSource) {
        super(name, servicesManager, principalFactory, order);
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(this.jdbcTemplate);
    }

    protected JdbcTemplate getJdbcTemplate() {
        return this.jdbcTemplate;
    }

    protected NamedParameterJdbcTemplate getNamedJdbcTemplate() {
        return this.namedParameterJdbcTemplate;
    }

    protected DataSource getDataSource() {
        return this.dataSource;
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {

        if (supports(credential))
            return authenticateUsernamePasswordInternal((MyUsernamePasswordCredential) credential, null);
        else
            throw new RuntimeException("配置错误");
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential transformedCredential, final String originalPassword) throws GeneralSecurityException, PreventedException {
        final MyUsernamePasswordCredential myCredential = (MyUsernamePasswordCredential) transformedCredential;
        final String username = myCredential.getUsername();
        final String encodedPsw = this.getPasswordEncoder().encode(myCredential.getPassword());
        final String platform = myCredential.getPlatform();
        final String requestCaptcha = myCredential.getCaptcha();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Object attribute = attributes.getRequest().getSession().getAttribute("captcha");

        String realCaptcha = attribute == null ? null : attribute.toString();

        if (StringUtils.isBlank(requestCaptcha) || !requestCaptcha.toUpperCase().equals(realCaptcha)) {
//            throw new FailedLoginException("验证码错误");
        }

        String[] platforms = StringUtil.split(platform, ",");
        String sql = this.sql;
        if (ArrayUtils.isNotEmpty(platforms) && platforms.length > 1) {
            sql += " and (platform=" + Sqls.formatSqlFieldValue(platforms[platforms.length - 1]) + " or platform is null)";
        } else {
            sql += " and (platform=" + Sqls.formatSqlFieldValue(platform) + " or platform is null)";
        }
        Map<String, Object> returnMap = new HashMap<>();
        try {

            System.out.println(sql);
            final Map<String, Object> values = getJdbcTemplate().queryForMap(sql, username);

            if (MapUtil.isEmpty(values)) {
                // 删除
                throw new AccountNotFoundException();
            }
            final Object status = values.get(disabledFieldName);
            if (status != null) {
                // 禁用
                throw new AccountDisabledException();
            }
            // 效验密码
            //final String digestedPassword = digestEncodedPassword(encodedPsw, values);
//            final String digestedPassword = new MyPasswordEncoder().encode(encodedPsw, values.get(DEFAULT_SALT_FIELD));
            final String digestedPassword = digestEncodedPassword(encodedPsw, values);
            /**
             * 这里是为了无密码登录情况使用加密后的密码直接和数据库密码对比（如：微信无密码登录情况）
             */
            if (!values.get(passwordFieldName).equals(digestedPassword) && !values.get(saltFieldName).equals(encodedPsw)) {
                throw new FailedLoginException("Password does not match value on record.");
            }
            returnMap.put("casAccountId", values.get("id"));
            returnMap.put("accountName", username);
        } catch (final IncorrectResultSizeDataAccessException e) {
            if (e.getActualSize() == 0) {
                throw new AccountNotFoundException(username + " not found with SQL query");
            } else {
                throw new FailedLoginException("Multiple records found for " + username);
            }
        }
        return createHandlerResult(transformedCredential, new DefaultPrincipalFactory().createPrincipal(username, returnMap));
    }


    @Override
    public boolean supports(Credential credential) {
        return credential instanceof MyUsernamePasswordCredential;
    }


    protected String digestEncodedPassword(final String encodedPassword, final Map<String, Object> values) {
        ConfigurableHashService hashService = new DefaultHashService();
        if (StringUtils.isNotBlank(this.staticSalt)) {
            hashService.setPrivateSalt(ByteSource.Util.bytes(this.staticSalt));
        }

        hashService.setHashAlgorithmName(this.algorithmName);
        Long numOfIterations = this.numberOfIterations;
        String dynaSalt;
        if (values.containsKey(this.numberOfIterationsFieldName)) {
            dynaSalt = values.get(this.numberOfIterationsFieldName).toString();
            numOfIterations = Long.valueOf(dynaSalt);
        }

        hashService.setHashIterations(numOfIterations.intValue());
        if (!values.containsKey(this.saltFieldName)) {
            throw new IllegalArgumentException("Specified field name for salt does not exist in the results");
        } else {
            dynaSalt = values.get(this.saltFieldName).toString();
            HashRequest request = (new HashRequest.Builder()).setSalt(dynaSalt).setSource(encodedPassword).build();
            return hashService.computeHash(request).toHex();
        }
    }


    public Long getNumberOfIterations() {
        return numberOfIterations;
    }

    public void setNumberOfIterations(Long numberOfIterations) {
        this.numberOfIterations = numberOfIterations;
    }

    public String getNumberOfIterationsFieldName() {
        return numberOfIterationsFieldName;
    }

    public void setNumberOfIterationsFieldName(String numberOfIterationsFieldName) {
        this.numberOfIterationsFieldName = numberOfIterationsFieldName;
    }

    public String getSaltFieldName() {
        return saltFieldName;
    }

    public void setSaltFieldName(String saltFieldName) {
        this.saltFieldName = saltFieldName;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getPasswordFieldName() {
        return passwordFieldName;
    }

    public void setPasswordFieldName(String passwordFieldName) {
        this.passwordFieldName = passwordFieldName;
    }

    public String getExpiredFieldName() {
        return expiredFieldName;
    }

    public void setExpiredFieldName(String expiredFieldName) {
        this.expiredFieldName = expiredFieldName;
    }

    public String getDisabledFieldName() {
        return disabledFieldName;
    }

    public void setDisabledFieldName(String disabledFieldName) {
        this.disabledFieldName = disabledFieldName;
    }

    public String getStaticSalt() {
        return staticSalt;
    }

    public void setStaticSalt(String staticSalt) {
        this.staticSalt = staticSalt;
    }

}

