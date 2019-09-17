package cn.wuxia.cas.configuration;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.DefaultLoginWebflowConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

public class WeChatAuthenticationWebflowConfigurer extends DefaultLoginWebflowConfigurer {

    public WeChatAuthenticationWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                 final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                 final ApplicationContext applicationContext,
                                                 final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        final Flow flow = getLoginFlow();
        if (flow != null) {
            //新建的action
            this.createInitialAuthenticationRequestValidationCheckAction(flow);
            final ActionState actionState = createActionState(flow, "weChatAuthenticationCheck",
                    createEvaluateAction("weChatAuthenticationAction"));
            //增加判断，根据结果不同进行不同的处理逻辑
            //这里可以自己新增action进行不同的处理
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
//                    CasWebflowConstants.STATE_ID_SEND_TICKET_GRANTING_TICKET));
                    CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET));
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_NO, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM));
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM));
            actionState.getExitActionList().add(createEvaluateAction("clearWebflowCredentialsAction"));
            registerMultifactorProvidersStateTransitionsIntoWebflow(actionState);

            createStateDefaultTransition(actionState, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
            setStartState(flow, "initialAuthenticationRequestValidationCheck");
        }
    }
}
