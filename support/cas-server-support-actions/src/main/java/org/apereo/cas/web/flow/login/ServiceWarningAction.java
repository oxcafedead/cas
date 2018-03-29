package org.apereo.cas.web.flow.login;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link ServiceWarningAction}. Populates the view
 * with the target url of the application after the warning
 * screen is displayed.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@AllArgsConstructor
public class ServiceWarningAction extends AbstractAction {

    private final CentralAuthenticationService centralAuthenticationService;
    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final TicketRegistrySupport ticketRegistrySupport;
    private final CookieGenerator warnCookieGenerator;

    @Override
    protected Event doExecute(final RequestContext context) {
        final var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final var response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);

        final Service service = WebUtils.getService(context);
        final var ticketGrantingTicket = WebUtils.getTicketGrantingTicketId(context);

        final var authentication = this.ticketRegistrySupport.getAuthenticationFrom(ticketGrantingTicket);
        if (authentication == null) {
            throw new InvalidTicketException(
                    new AuthenticationException("No authentication found for ticket " + ticketGrantingTicket), ticketGrantingTicket);
        }

        final var credential = WebUtils.getCredential(context);
        final var authenticationResultBuilder =
                authenticationSystemSupport.establishAuthenticationContextFromInitial(authentication, credential);
        final var authenticationResult = authenticationResultBuilder.build(service);

        final var serviceTicketId = this.centralAuthenticationService.grantServiceTicket(ticketGrantingTicket, service, authenticationResult);
        WebUtils.putServiceTicketInRequestScope(context, serviceTicketId);

        if (request.getParameterMap().containsKey("ignorewarn")) {
            if (Boolean.parseBoolean(request.getParameter("ignorewarn"))) {
                this.warnCookieGenerator.removeCookie(response);
            }
        }
        return new Event(this, CasWebflowConstants.STATE_ID_REDIRECT);
    }
}
