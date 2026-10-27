package com.kalalev.booking.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

/**
 * After login, returns to the page in the "next" form field (for example the search the user came from).
 * Only paths on this site are accepted, so the field cannot send users to another website.
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
