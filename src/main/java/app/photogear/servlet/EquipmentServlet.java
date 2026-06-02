package app.photogear.servlet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import app.photogear.dao.EquipmentDAO;
import app.photogear.model.Equipment;
import app.photogear.util.GsonConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet REST para gestión de equipo fotográfico.
 *
 * Endpoints:
 *   GET    /api/equipment                  → listar (con filtros opcionales)
 *   GET    /api/equipment/{id}             → obtener uno
 *   POST   /api/equipment                  → crear
 *   POST   /api/equipment/{id}/report      → reportar perdido/robado
 *   PUT    /api/equipment/{id}             → actualizar
 *   DELETE /api/equipment/{id}             → eliminar
 *
 * Filtros disponibles para GET /api/equipment:
 *   ?category=lens&status=active&search=canon
 */
public class EquipmentServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(EquipmentServlet.class.getName());
    private final EquipmentDAO dao = new EquipmentDAO();

    // ── GET ───────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String[] segments = pathSegments(req);

        if (segments.length == 0) {
            // GET /api/equipment → lista con filtros opcionales
            String category = req.getParameter("category");
            String status   = req.getParameter("status");
            String search   = req.getParameter("search");
            try {
                List<Equipment> list = dao.findAll(category, status, search);
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("success", true);
                body.put("data",    list);
                body.put("total",   list.size());
                writeJson(resp, 200, body);
            } catch (SQLException e) {
                serverError(resp, "Error al consultar equipos", e);
            }

        } else if (segments.length == 1) {
            // GET /api/equipment/{id}
            String id = segments[0];
            try {
                Optional<Equipment> opt = dao.findById(id);
                if (opt.isPresent()) {
                    writeJson(resp, 200, ok(opt.get()));
                } else {
                    writeJson(resp, 404, error("Equipo no encontrado: " + id));
                }
            } catch (SQLException e) {
                serverError(resp, "Error al buscar el equipo", e);
            }

        } else {
            writeJson(resp, 400, error("Ruta no válida"));
        }
    }

    // ── POST ──────────────────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String[] segments = pathSegments(req);
        String body = readBody(req);

        if (segments.length == 0) {
            // POST /api/equipment → crear nuevo equipo
            Equipment e = GsonConfig.get().fromJson(body, Equipment.class);
            if (e == null || isBlank(e.getBrand()) || isBlank(e.getModel())) {
                writeJson(resp, 400, error("Los campos 'brand' y 'model' son obligatorios"));
                return;
            }
            try {
                Equipment created = dao.create(e);
                writeJson(resp, 201, ok(created));
            } catch (SQLException ex) {
                serverError(resp, "Error al crear el equipo", ex);
            }

        } else if (segments.length == 2 && "report".equals(segments[1])) {
            // POST /api/equipment/{id}/report → reportar perdido/robado
            String id = segments[0];
            try {
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                String status  = json.has("status")  ? json.get("status").getAsString()  : "lost";
                String details = json.has("details")  ? json.get("details").getAsString() : "";
                String dateStr = json.has("reportDate") ? json.get("reportDate").getAsString() : null;

                if (!"lost".equals(status) && !"stolen".equals(status)) {
                    writeJson(resp, 400, error("El estado del reporte debe ser 'lost' o 'stolen'"));
                    return;
                }

                LocalDate reportDate = (dateStr != null && !dateStr.isBlank())
                        ? LocalDate.parse(dateStr)
                        : LocalDate.now();

                boolean updated = dao.report(id, status, reportDate, details);
                if (updated) {
                    writeJson(resp, 200, okMsg("Reporte registrado correctamente"));
                } else {
                    writeJson(resp, 404, error("Equipo no encontrado: " + id));
                }

            } catch (Exception ex) {
                serverError(resp, "Error al registrar el reporte", ex);
            }

        } else {
            writeJson(resp, 400, error("Ruta no válida"));
        }
    }

    // ── PUT ───────────────────────────────────────────────────

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String[] segments = pathSegments(req);

        if (segments.length != 1) {
            writeJson(resp, 400, error("Se requiere un ID: PUT /api/equipment/{id}"));
            return;
        }

        String id   = segments[0];
        String body = readBody(req);
        Equipment e = GsonConfig.get().fromJson(body, Equipment.class);

        if (e == null || isBlank(e.getBrand()) || isBlank(e.getModel())) {
            writeJson(resp, 400, error("Los campos 'brand' y 'model' son obligatorios"));
            return;
        }

        e.setId(id); // Garantizar que el ID sea el de la URL
        try {
            boolean updated = dao.update(e);
            if (updated) {
                writeJson(resp, 200, ok(e));
            } else {
                writeJson(resp, 404, error("Equipo no encontrado: " + id));
            }
        } catch (SQLException ex) {
            serverError(resp, "Error al actualizar el equipo", ex);
        }
    }

    // ── DELETE ────────────────────────────────────────────────

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String[] segments = pathSegments(req);

        if (segments.length != 1) {
            writeJson(resp, 400, error("Se requiere un ID: DELETE /api/equipment/{id}"));
            return;
        }

        String id = segments[0];
        try {
            boolean deleted = dao.delete(id);
            if (deleted) {
                writeJson(resp, 200, okMsg("Equipo eliminado"));
            } else {
                writeJson(resp, 404, error("Equipo no encontrado: " + id));
            }
        } catch (SQLException e) {
            serverError(resp, "Error al eliminar el equipo", e);
        }
    }

    // ── HELPERS ───────────────────────────────────────────────

    /** Extrae los segmentos del pathInfo. Ej: "/abc/report" → ["abc", "report"] */
    private String[] pathSegments(HttpServletRequest req) {
        String path = req.getPathInfo();
        if (path == null || path.equals("/")) return new String[0];
        return Arrays.stream(path.substring(1).split("/"))
                     .filter(s -> !s.isEmpty())
                     .toArray(String[]::new);
    }

    private String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    private void writeJson(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(GsonConfig.get().toJson(body));
        }
    }

    private Map<String, Object> ok(Object data) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", true);
        m.put("data",    data);
        return m;
    }

    private Map<String, Object> okMsg(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", true);
        m.put("message", message);
        return m;
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", false);
        m.put("message", message);
        return m;
    }

    private void serverError(HttpServletResponse resp, String msg, Exception e) throws IOException {
        LOG.log(Level.SEVERE, msg, e);
        writeJson(resp, 500, error(msg + ": " + e.getMessage()));
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
