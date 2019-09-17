package cn.wuxia.cas.configuration;

import cn.wuxia.cas.WeChatCredentialsAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
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

@Configuration("WeChatAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class WeChatAuthenticationEventExecutionPlanConfiguration
        implements AuthenticationEventExecutionPlanConfigurer {

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
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Override
    public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
        plan.registerAuthenticationHandler(weChatCredentialsAuthenticationHandler());
    }

    @Bean
    public WeChatCredentialsAuthenticationHandler weChatCredentialsAuthenticationHandler() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClass);
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        WeChatCredentialsAuthenticationHandler handler = new WeChatCredentialsAuthenticationHandler(WeChatCredentialsAuthenticationHandler.class.getSimpleName(), servicesManager, new DefaultPrincipalFactory(), 1, dataSource);
        handler.setDisabledFieldName(disabledFieldName);
        handler.setSql(sql);
        return handler;
    }
}
