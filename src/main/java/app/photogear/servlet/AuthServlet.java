package app.photogear.servlet;

import app.photogear.dao.UserDAO;
import app.photogear.model.User;
import app.photogear.util.AppConfig;
import app.photogear.util.GsonConfig;
import app.photogear.util.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(AuthServlet.class.getName());
    private final UserDAO userDAO = new UserDAO();
    private GoogleIdTokenVerifier verifier;

    @Override
    public void init() {
        String clientId = AppConfig.get("google.client.id", "");
        if (!clientId.isBlank() && !clientId.startsWith("TU_")) {
            verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
            LOG.info("Google Sign-In inicializado con client ID: " + clientId);
        } else {
            LOG.warning("google.client.id no configurado. Google Sign-In no estará disponible.");
        }
    }

    // GET /api/auth/config → devuelve el client ID para que el frontend inicialice GSI
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String clientId = AppConfig.get("google.client.id", "");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("googleClientId", (clientId.isBlank() || clientId.startsWith("TU_")) ? null : clientId);
        writeJson(resp, 200, body);
    }

    // POST /api/auth/google → verifica el credential de Google y emite un JWT
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if ("/google".equals(path)) {
            handleGoogleAuth(req, resp);
        } else {
            writeJson(resp, 400, error("Ruta no válida"));
        }
    }

    private void handleGoogleAuth(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (verifier == null) {
            writeJson(resp, 503, error("Google Sign-In no está configurado. Agrega google.client.id en config.properties."));
            return;
        }

        String rawBody = new String(req.getInputStream().readAllBytes());
        JsonObject json = JsonParser.parseString(rawBody).getAsJsonObject();
        String credential = json.has("credential") ? json.get("credential").getAsString() : null;

        if (credential == null || credential.isBlank()) {
            writeJson(resp, 400, error("Credencial de Google no proporcionada"));
            return;
        }

        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(credential);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error verificando token de Google", e);
            writeJson(resp, 401, error("No se pudo verificar el token de Google"));
            return;
        }

        if (idToken == null) {
            writeJson(resp, 401, error("Token de Google inválido o expirado"));
            return;
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String googleId = payload.getSubject();
        String email    = payload.getEmail();
        String name     = (String) payload.get("name");
        String picture  = (String) payload.get("picture");

        try {
            Optional<User> existing = userDAO.findByGoogleId(googleId);
            User user;
            if (existing.isPresent()) {
                user = existing.get();
                // Actualizar datos en caso de que cambien en Google
                user.setName(name != null ? name : user.getName());
                user.setPicture(picture != null ? picture : user.getPicture());
                userDAO.updateLastLogin(user.getId());
            } else {
                user = new User();
                user.setGoogleId(googleId);
                user.setEmail(email);
                user.setName(name != null ? name : email);
                user.setPicture(picture);
                user = userDAO.create(user);
            }

            String token = JwtUtil.createToken(user);

            Map<String, Object> userMap = new LinkedHashMap<>();
            userMap.put("id",      user.getId());
            userMap.put("email",   user.getEmail());
            userMap.put("name",    user.getName());
            userMap.put("picture", user.getPicture());

            Map<String, Object> responseBody = new LinkedHashMap<>();
            responseBody.put("success", true);
            responseBody.put("token",   token);
            responseBody.put("user",    userMap);
            writeJson(resp, 200, responseBody);

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error de BD al procesar login de Google", e);
            writeJson(resp, 500, error("Error interno del servidor"));
        }
    }

    private void writeJson(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(GsonConfig.get().toJson(body));
        }
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", false);
        m.put("message", message);
        return m;
    }
}
