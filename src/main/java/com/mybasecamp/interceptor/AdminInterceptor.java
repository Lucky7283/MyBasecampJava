package com.mybasecamp.interceptor;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public @Component class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("POST".equalsIgnoreCase(request.getMethod()) && "/users".equals(request.getRequestURI())) {
            return true;
        }
        Object userAttr = request.getAttribute("user");
        if (userAttr == null) {
            response.sendRedirect("/login");
            return false;
        }
        com.mybasecamp.model.entity.User user = (com.mybasecamp.model.entity.User) userAttr;
        if (user.getAdmin() != 1) {
            request.setAttribute("errorMessage", "Access denied. You do not have administrator privileges to access this page.");
            request.getRequestDispatcher("/error").forward(request, response);
            return false;
        }
        return true;
    }
}
