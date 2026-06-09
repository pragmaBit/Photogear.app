package app.photogear.filter;

import app.photogear.util.AppConfig;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Filtro CORS basado en lista blanca de orígenes.
 *
 * Configuración (orden de prioridad):
 *   1. Propiedad JVM:     -Dcors.allowed.origins=https://app1.com,https://app2.com
 *   2. config.properties: cors.allowed.origins
 *   3. web.xml init-param: allowedOrigins
 *   4. Default: * (solo desarrollo)
 *
 * Seguridad: con una lista explícita, el filtro solo refleja el Origin de la
 * petición si está en la lista (nunca un valor arbitrario), añade 'Vary: Origin'
 * y habilita Allow-Credentials. El comodín '*' se reserva para desarrollo y es
 * incompatible con credenciales según la especificación CORS.
 */
public class CorsFilter implements Filter {

    private Set<String> allowedOrigins;
    private boolean allowAll;

    @Override
    public void init(FilterConfig fc) {
        String configured = firstNonBlank(
            System.getProperty("cors.allowed.origins"),
            AppConfig.get("cors.allowed.origins"),
            fc.getInitParameter("allowedOrigins"),
            "*"
        );

        allowAll = "*".equals(configured.trim());
        allowedOrigins = allowAll
            ? new HashSet<>()
            : Arrays.stream(configured.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String origin = req.getHeader("Origin");

        if (allowAll) {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        } else if (origin != null && allowedOrigins.contains(origin)) {
            resp.setHeader("Access-Control-Allow-Origin", origin);
            resp.setHeader("Access-Control-Allow-Credentials", "true");
            resp.addHeader("Vary", "Origin");
        }
        // Si el origen no está permitido, no se emite ACAO y el navegador bloquea.

        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, Authorization");
        resp.setHeader("Access-Control-Max-Age", "3600");

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(request, response);
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return "*";
    }

    @Override
    public void destroy() {}
}
