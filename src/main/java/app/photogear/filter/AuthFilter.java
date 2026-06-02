package app.photogear.filter;

import app.photogear.util.GsonConfig;
import app.photogear.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Los preflight de CORS no llevan token; dejarlos pasar
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            unauthorized(resp, "Se requiere autenticación");
            return;
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = JwtUtil.validateToken(token);
            req.setAttribute("userId",    claims.getSubject());
            req.setAttribute("userEmail", claims.get("email",   String.class));
            req.setAttribute("userName",  claims.get("name",    String.class));
            chain.doFilter(request, response);
        } catch (JwtException e) {
            unauthorized(resp, "Token inválido o expirado. Inicia sesión nuevamente.");
        }
    }

    private void unauthorized(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", message);
        try (PrintWriter out = resp.getWriter()) {
            out.print(GsonConfig.get().toJson(body));
        }
    }
}
