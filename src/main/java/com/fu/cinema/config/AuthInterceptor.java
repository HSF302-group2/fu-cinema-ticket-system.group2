
package com.fu.cinema.config;

import com.fu.cinema.dto.SessionUser;
import com.fu.cinema.util.SessionUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws IOException {

        String uri = request.getRequestURI()
                .substring(request.getContextPath().length());

        boolean adminPath = uri.equals("/admin")
                || uri.startsWith("/admin/");

        boolean customerPath = uri.equals("/customer")
                || uri.startsWith("/customer/");

        // Các đường dẫn khác không thuộc phạm vi TODO 15.
        if (!adminPath && !customerPath) {
            return true;
        }

        SessionUser user = SessionUtils.getCurrentUser(request);

        // Chưa đăng nhập: chuyển đến trang login.
        if (user == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return false;
        }

        // /admin/** chỉ dành cho ADMIN.
        if (adminPath && !user.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        // /customer/** chỉ dành cho CUSTOMER.
        if (customerPath && !user.isCustomer()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }
}
