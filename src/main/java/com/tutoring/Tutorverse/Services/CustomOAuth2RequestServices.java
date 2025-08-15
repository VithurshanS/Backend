package com.tutoring.Tutorverse.Services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


@Component
public class CustomOAuth2RequestServices implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String AUTHORIZATION_REQUEST_ATTR_NAME = "oauth2_auth_request";

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {

        return this.getAuthorizationRequest(request);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            this.removeAuthorizationRequest(request, response);
            return;
        }
        String role = request.getParameter("role");
        if (StringUtils.hasText(role)) {
            HttpSession session = request.getSession(true);
            session.setAttribute("signup_role", role);
        }

        HttpSession session = request.getSession(true);
        session.setAttribute(AUTHORIZATION_REQUEST_ATTR_NAME, authorizationRequest);

    }
    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = this.getAuthorizationRequest(request);
        if (authorizationRequest != null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute(AUTHORIZATION_REQUEST_ATTR_NAME);
            }
        }
        return authorizationRequest;
    }
    private OAuth2AuthorizationRequest getAuthorizationRequest(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null ?
                (OAuth2AuthorizationRequest) session.getAttribute(AUTHORIZATION_REQUEST_ATTR_NAME) : null;
    }

}
