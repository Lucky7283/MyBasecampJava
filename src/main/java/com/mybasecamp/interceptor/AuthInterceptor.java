package com.mybasecamp.interceptor;

import org.springframework.stereotype.Component;

import com.mybasecamp.model.entity.Session;
import com.mybasecamp.model.entity.User;
import com.mybasecamp.model.service.SessionService;
import com.mybasecamp.model.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final SessionService sessionService;
    private final UserRepository userRepository;

    public AuthInterceptor(SessionService sessionService, UserRepository userRepository) {
        this.sessionService = sessionService;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String uri = request.getRequestURI();

        // Allow POST to /sessions and /users (login & registration) without auth
        if ("POST".equalsIgnoreCase(request.getMethod()) && ("/sessions".equals(uri) || "/users".equals(uri))) {
            return true;
        }

        // Static resources and public pages exclusions
        if (uri.equals("/login") || uri.equals("/register") || uri.equals("/sessions")
                || uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/images/")
                || uri.startsWith("/uploads/")) {
            return true;
        }

        String token = getTokenFromRequest(request);
        if (token == null) {
            if ("/admin".equals(uri)) {
                return true;
            }
            response.sendRedirect("/login");
            return false;
        }

        if (!sessionService.isSessionValid(token)) {
            if ("/admin".equals(uri)) {
                return true;
            }
            response.sendRedirect("/login");
            return false;
        }

        Session session = sessionService.getSessionByToken(token).orElse(null);
        if (session == null) {
            if ("/admin".equals(uri)) {
                return true;
            }
            response.sendRedirect("/login");
            return false;
        }

        User user = userRepository.findById(session.getUserId()).orElse(null);
        if (user == null) {
            if ("/admin".equals(uri)) {
                return true;
            }
            response.sendRedirect("/login");
            return false;
        }

        request.setAttribute("user", user);
        request.setAttribute("token", token);
        return true;
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String cookieHeader = request.getHeader("Cookie");
        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split("; ");
            for (String cookie : cookies) {
                if (cookie.startsWith("token=")) {
                    return cookie.substring(6);
                }
            }
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
