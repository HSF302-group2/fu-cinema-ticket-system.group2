
package com.fu.cinema.util;

import com.fu.cinema.dto.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class SessionUtils {

    public static final String CURRENT_USER = "currentUser";

    private SessionUtils() {
    }

    public static SessionUser getCurrentUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        HttpSession session = request.getSession(false);
        return getCurrentUser(session);
    }

    public static SessionUser getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }

        Object user = session.getAttribute(CURRENT_USER);

        return user instanceof SessionUser
                ? (SessionUser) user
                : null;
    }

    /**
     * Luôn lấy customerId từ session, KHÔNG lấy từ form/URL.
     */
    public static Long getCurrentCustomerId(HttpSession session) {
        SessionUser user = getCurrentUser(session);
        return user == null ? null : user.getCustomerId();
    }

    public static void setCurrentUser(
            HttpSession session,
            SessionUser user) {
        if (session != null) {
            session.setAttribute(CURRENT_USER, user);
        }
    }

    public static void clearSession(HttpServletRequest request) {
        if (request == null) {
            return;
        }

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }
    }
}
