package cn.wuxia.cas.user.web;

import cn.wuxia.cas.user.bean.MyUsernamePasswordCredential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.DefaultLoginWebflowConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * @author: songlin.li
 * @date: 2019-2-14
 * @description: 重新定义 Credential model
 */
public class MyCaptchaWebflowConfigurer extends DefaultLoginWebflowConfigurer {
    /**
     * Instantiates a new Default webflow configurer.
     *
     * @param flowBuilderServices    the flow builder services
     * @param flowDefinitionRegistry the flow definition registry
     * @param applicationContext     the application context
     * @param casProperties          the cas properties
     */
    public MyCaptchaWebflowConfigurer(FlowBuilderServices flowBuilderServices, FlowDefinitionRegistry flowDefinitionRegistry, ApplicationContext applicationContext, CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }


    /**
     * Create remember me authn webflow config.
     *
     * @param flow the flow
     */
    @Override
    protected void createRememberMeAuthnWebflowConfig(Flow flow) {
        createFlowVariable(flow, CasWebflowConstants.VAR_ID_CREDENTIAL, MyUsernamePasswordCredential.class);
        ViewState state = getState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, ViewState.class);
        BinderConfiguration cfg = getViewStateBinderConfiguration(state);
        if (casProperties.getTicket().getTgt().getRememberMe().isEnabled()) {
            cfg.addBinding(new BinderConfiguration.Binding("rememberMe", null, false));
        }
        cfg.addBinding(new BinderConfiguration.Binding("platform", null, false));
        cfg.addBinding(new BinderConfiguration.Binding("captcha", null, false));
    }
}
