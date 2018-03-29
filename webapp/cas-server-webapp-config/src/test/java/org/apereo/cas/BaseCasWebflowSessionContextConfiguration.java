package org.apereo.cas;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasApplicationContextConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasFiltersConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasPropertiesConfiguration;
import org.apereo.cas.config.CasWebAppConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logging.config.CasLoggingConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.config.CasSupportActionsConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.junit.Assert.*;

/**
 * This is {@link BaseCasWebflowSessionContextConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {CasApplicationContextConfiguration.class,
        CasThemesConfiguration.class,
        CasFiltersConfiguration.class,
        CasPropertiesConfiguration.class,
        CasWebAppConfiguration.class,
        CasWebflowServerSessionContextConfigurationTests.TestWebflowContextConfiguration.class,
        CasWebflowContextConfiguration.class,
        CasDefaultServiceTicketIdGeneratorsConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasCoreWebflowConfiguration.class,
        CasCoreAuthenticationConfiguration.class, CasCoreServicesAuthenticationConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasLoggingConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasSupportActionsConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCookieConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreValidationConfiguration.class,
        CasCoreConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreAuditConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        AopAutoConfiguration.class,
        RefreshAutoConfiguration.class
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@TestPropertySource(properties = "spring.aop.proxy-target-class=true")
@Slf4j
public abstract class BaseCasWebflowSessionContextConfiguration {

    @Test
    public void verifyExecutorsAreBeans() {
        assertNotNull(getFlowExecutor());
    }

    @Test
    public void verifyFlowExecutorByClient() {
        final var ctx = getMockRequestContext();
        final var map = new LocalAttributeMap<>();
        getFlowExecutor().launchExecution("login", map, ctx.getExternalContext());
    }

    private RequestContext getMockRequestContext() {
        final var ctx = new MockRequestContext();
        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();
        final var sCtx = new MockServletContext();
        ctx.setExternalContext(new ServletExternalContext(sCtx, request, response));
        return ctx;
    }

    public abstract FlowExecutor getFlowExecutor();

    /**
     * The type Test webflow context configuration.
     */
    @TestConfiguration("testWebflowContextConfiguration")
    public static class TestWebflowContextConfiguration {

        private static final String TEST = "test";

        @Bean
        public Action testWebflowSerialization() {
            //CHECKSTYLE:OFF
            return new AbstractAction() {
                @Override
                protected Event doExecute(final RequestContext requestContext) {
                    requestContext.getFlowScope().put("test0", Collections.singleton(TEST));
                    requestContext.getFlowScope().put("test1", Collections.singletonList(TEST));
                    requestContext.getFlowScope().put("test2", Collections.singletonMap(TEST, TEST));
                    requestContext.getFlowScope().put("test3", Arrays.asList(TEST, TEST));
                    requestContext.getFlowScope().put("test4", new ConcurrentSkipListSet());
                    requestContext.getFlowScope().put("test5", Collections.unmodifiableList(Arrays.asList("test1")));
                    requestContext.getFlowScope().put("test6", Collections.unmodifiableSet(Collections.singleton(1)));
                    requestContext.getFlowScope().put("test7", Collections.unmodifiableMap(new HashMap<>()));
                    requestContext.getFlowScope().put("test8", Collections.emptyMap());
                    requestContext.getFlowScope().put("test9", new TreeMap<>());
                    requestContext.getFlowScope().put("test10", Collections.emptySet());
                    requestContext.getFlowScope().put("test11", Collections.emptyList());
                    return success();
                }
            };
            //CHECKSTYLE:ON
        }
    }
}
