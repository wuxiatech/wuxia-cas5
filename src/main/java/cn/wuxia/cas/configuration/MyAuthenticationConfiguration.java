package cn.wuxia.cas.configuration;

import cn.wuxia.cas.MyCaptchaAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration("myAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MyAuthenticationConfiguration implements AuthenticationEventExecutionPlanConfigurer {



    private static final long DEFAULT_ITERATIONS = 1024;

    @Value("${custom.cas.authn.jdbc.numberOfIterations}")
    private Long numberOfIterations = DEFAULT_ITERATIONS;
    //该列名的值可替代上面的值，但对密码加密时必须取该值进行处理
    @Value("${custom.cas.authn.jdbc.numberOfIterationsFieldName}")
    private String numberOfIterationsFieldName;

    @Value("${custom.cas.authn.jdbc.saltFieldName:salt}")
    private String saltFieldName;
    //对处理盐值后的算法
    @Value("${custom.cas.authn.jdbc.algorithmName}")
    protected String algorithmName = "MD5";
    @Value("${custom.cas.authn.jdbc.sql}")
    protected String sql;
    @Value("${custom.cas.authn.jdbc.passwordFieldName:password}")
    protected String passwordFieldName;
    @Value("${custom.cas.authn.jdbc.expiredFieldName}")
    protected String expiredFieldName;
    @Value("${custom.cas.authn.jdbc.disabledFieldName}")
    protected String disabledFieldName;
    //静态盐值
    @Value("${custom.cas.authn.jdbc.staticSalt}")
    String staticSalt;
    @Value("${custom.cas.authn.jdbc.url}")
    String url;
    @Value("${custom.cas.authn.jdbc.dialect}")
    String dialect;
    @Value("${custom.cas.authn.jdbc.user}")
    String user;
    @Value("${custom.cas.authn.jdbc.password}")
    String password;
    @Value("${custom.cas.authn.jdbc.driverClass}")
    String driverClass;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    /**
     * 将自定义验证器注册为Bean
     *
     * @return
     */
    @Bean
    public AuthenticationHandler myAuthenticationHandler() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClass);
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        MyCaptchaAuthenticationHandler handler = new MyCaptchaAuthenticationHandler(MyCaptchaAuthenticationHandler.class.getSimpleName(), servicesManager, new DefaultPrincipalFactory(), 1, dataSource);
        handler.setAlgorithmName(algorithmName);
        handler.setDisabledFieldName(disabledFieldName);
        handler.setExpiredFieldName(expiredFieldName);
        handler.setNumberOfIterations(numberOfIterations);
        handler.setNumberOfIterationsFieldName(numberOfIterationsFieldName);
        handler.setPasswordFieldName(passwordFieldName);
        handler.setSaltFieldName(saltFieldName);
        handler.setSql(sql);
        handler.setStaticSalt(staticSalt);
        return handler;
    }

    /**
     * 注册验证器
     *
     * @param plan
     */
    @Override
    public void configureAuthenticationExecutionPlan(AuthenticationEventExecutionPlan plan) {
        plan.registerAuthenticationHandler(myAuthenticationHandler());
    }
}
