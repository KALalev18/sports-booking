package com.kalalev.sports.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

/**
 * After login, goes back to the page given in the "next" form field (for example the shop, so the
 * user can finish checking out). Only paths on this site are accepted, so the field cannot be used
 * to send users to another website. Without it, Spring's default behaviour applies.
 */
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws ServletException, IOException {
        String next = request.getParameter("next");
        if (isLocalPath(next)) {
            getRedirectStrategy().sendRedirect(request, response, next);
            return;
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }

    static boolean isLocalPath(String path) {
        return path != null && path.startsWith("/") && !path.startsWith("//") && !path.contains("\\");
    }
}
