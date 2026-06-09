package app.photogear.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Añade cabeceras de seguridad a todas las respuestas (defensa en profundidad,
 * complementa al WAF mod_security). Alineado con OWASP Secure Headers Project.
 */
public class SecurityHeadersFilter implements Filter {

    private static final String CSP = String.join("; ",
        "default-src 'self'",
        // Scripts propios + Google Identity Services (sin unsafe-inline)
        "script-src 'self' https://accounts.google.com https://www.w3schools.com",
        // Estilos propios + W3.CSS + Google Fonts (sin unsafe-inline)
        "style-src 'self' https://www.w3schools.com https://fonts.googleapis.com",
        "font-src 'self' https://fonts.gstatic.com",
        // Avatares de usuario de Google
        "img-src 'self' data: https://*.googleusercontent.com",
        // Llamadas a la propia API y endpoints de Google
        "connect-src 'self' https://accounts.google.com",
        // iframe del botón / One Tap de Google
        "frame-src https://accounts.google.com",
        // Nadie puede embebernos (anti-clickjacking)
        "frame-ancestors 'none'",
        "base-uri 'self'",
        "form-action 'self'",
        "object-src 'none'"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;

        resp.setHeader("Content-Security-Policy", CSP);
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "DENY");
        resp.setHeader("Referrer-Policy", "no-referrer");
        resp.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
        resp.setHeader("Cross-Origin-Opener-Policy", "same-origin-allow-popups");

        // HSTS solo tiene sentido sobre TLS (la terminación TLS puede estar en Apache/WAF)
        if (req.isSecure() || "https".equalsIgnoreCase(req.getHeader("X-Forwarded-Proto"))) {
            resp.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }

        // No cachear respuestas de la API (pueden contener datos sensibles)
        if (req.getRequestURI() != null && req.getRequestURI().contains("/api/")) {
            resp.setHeader("Cache-Control", "no-store");
        }

        chain.doFilter(request, response);
    }
}
