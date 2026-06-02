package app.photogear.filter;

import app.photogear.util.AppConfig;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Filtro CORS configurable en tres niveles (orden de prioridad):
 *   1. Propiedad JVM:    -Dcors.allowed.origins=https://tuapp.com
 *   2. config.properties: cors.allowed.origins=https://tuapp.com
 *   3. web.xml init-param: allowedOrigins (fallback para sobreescritura por despliegue)
 *   4. Default: * (solo para desarrollo; no usar en producción)
 */
public class CorsFilter implements Filter {

    private String allowedOrigins;

    @Override
    public void init(FilterConfig fc) {
        String fromSystem = System.getProperty("cors.allowed.origins");
        String fromConfig = AppConfig.get("cors.allowed.origins");
        String fromParam  = fc.getInitParameter("allowedOrigins");

        allowedOrigins = fromSystem != null ? fromSystem
                       : fromConfig != null ? fromConfig
                       : fromParam  != null ? fromParam
                       : "*";
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

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
