package app.photogear.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Filtro CORS para permitir que el frontend (cuando corre en un origen distinto,
 * p. ej. durante desarrollo) se comunique con la API.
 *
 * En producción, reemplaza allowedOrigins='*' por el dominio real del frontend
 * en web.xml: <init-param><param-name>allowedOrigins</param-name><param-value>https://tusitioweb.com</param-value>
 */
public class CorsFilter implements Filter {

    private String allowedOrigins;

    @Override
    public void init(FilterConfig fc) {
        allowedOrigins = fc.getInitParameter("allowedOrigins");
        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            allowedOrigins = "*";
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;

        resp.setHeader("Access-Control-Allow-Origin",  allowedOrigins);
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, Authorization");
        resp.setHeader("Access-Control-Max-Age",       "3600");

        // Las peticiones OPTIONS (preflight) se responden inmediatamente
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
