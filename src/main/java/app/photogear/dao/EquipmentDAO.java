package app.photogear.dao;

import app.photogear.model.Equipment;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Acceso a datos para la tabla equipment.
 * Todas las operaciones usan PreparedStatement; ningún parámetro
 * se concatena directamente en SQL (prevención de SQL Injection).
 */
public class EquipmentDAO {

    // ── CREATE ────────────────────────────────────────────────

    public Equipment create(Equipment e) throws SQLException {
        if (e.getId() == null || e.getId().isBlank()) {
            e.setId(UUID.randomUUID().toString());
        }
        LocalDateTime now = LocalDateTime.now();
        e.setCreatedAt(now);
        e.setUpdatedAt(now);

        final String sql =
            "INSERT INTO equipment " +
            "(id, category, brand, model, serial_number, " +
            " purchase_date, purchase_price, equipment_condition, status, " +
            " warranty_has, warranty_expiry, warranty_provider, " +
            " insurance_has, insurance_provider, insurance_policy_number, insurance_expiry, " +
            " report_date, report_details, notes, photos, created_at, updated_at) " +
            "VALUES (?,?,?,?,?, ?,?,?,?, ?,?,?, ?,?,?,?, ?,?,?,?,?,?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1,  e.getId());
            ps.setString(2,  e.getCategory());
            ps.setString(3,  e.getBrand());
            ps.setString(4,  e.getModel());
            ps.setString(5,  e.getSerialNumber());
            ps.setObject(6,  e.getPurchaseDate());       // JDBC 4.2: acepta LocalDate
            ps.setBigDecimal(7, e.getPurchasePrice());
            ps.setString(8,  e.getCondition());
            ps.setString(9,  e.getStatus());
            ps.setBoolean(10, e.isWarrantyHas());
            ps.setObject(11, e.getWarrantyExpiry());
            ps.setString(12, e.getWarrantyProvider());
            ps.setBoolean(13, e.isInsuranceHas());
            ps.setString(14, e.getInsuranceProvider());
            ps.setString(15, e.getInsurancePolicyNumber());
            ps.setObject(16, e.getInsuranceExpiry());
            ps.setObject(17, e.getReportDate());
            ps.setString(18, e.getReportDetails());
            ps.setString(19, e.getNotes());
            ps.setString(20, e.getPhotos());
            ps.setObject(21, e.getCreatedAt());
            ps.setObject(22, e.getUpdatedAt());

            ps.executeUpdate();
        }
        return e;
    }

    // ── READ ALL ──────────────────────────────────────────────

    /**
     * Lista equipos con filtros opcionales.
     *
     * @param category  filtro por categoría  (null o "" = todos)
     * @param status    filtro por estado      (null o "" = todos)
     * @param search    búsqueda en brand/model/serial (null o "" = sin filtro)
     */
    public List<Equipment> findAll(String category, String status, String search)
            throws SQLException {

        StringBuilder sql = new StringBuilder(
            "SELECT * FROM equipment WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (category != null && !category.isBlank() && !"all".equals(category)) {
            sql.append(" AND category = ?");
            params.add(category);
        }
        if (status != null && !status.isBlank() && !"all".equals(status)) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(brand) LIKE ? OR LOWER(model) LIKE ? OR LOWER(serial_number) LIKE ?)");
            String q = "%" + search.toLowerCase() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
        }

        sql.append(" ORDER BY created_at DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                return mapList(rs);
            }
        }
    }

    // ── READ ONE ──────────────────────────────────────────────

    public Optional<Equipment> findById(String id) throws SQLException {
        final String sql = "SELECT * FROM equipment WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                List<Equipment> list = mapList(rs);
                return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
            }
        }
    }

    // ── UPDATE ────────────────────────────────────────────────

    public boolean update(Equipment e) throws SQLException {
        e.setUpdatedAt(LocalDateTime.now());

        final String sql =
            "UPDATE equipment SET " +
            "  category = ?, brand = ?, model = ?, serial_number = ?, " +
            "  purchase_date = ?, purchase_price = ?, equipment_condition = ?, status = ?, " +
            "  warranty_has = ?, warranty_expiry = ?, warranty_provider = ?, " +
            "  insurance_has = ?, insurance_provider = ?, insurance_policy_number = ?, insurance_expiry = ?, " +
            "  report_date = ?, report_details = ?, notes = ?, photos = ?, updated_at = ? " +
            "WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1,  e.getCategory());
            ps.setString(2,  e.getBrand());
            ps.setString(3,  e.getModel());
            ps.setString(4,  e.getSerialNumber());
            ps.setObject(5,  e.getPurchaseDate());
            ps.setBigDecimal(6, e.getPurchasePrice());
            ps.setString(7,  e.getCondition());
            ps.setString(8,  e.getStatus());
            ps.setBoolean(9,  e.isWarrantyHas());
            ps.setObject(10, e.getWarrantyExpiry());
            ps.setString(11, e.getWarrantyProvider());
            ps.setBoolean(12, e.isInsuranceHas());
            ps.setString(13, e.getInsuranceProvider());
            ps.setString(14, e.getInsurancePolicyNumber());
            ps.setObject(15, e.getInsuranceExpiry());
            ps.setObject(16, e.getReportDate());
            ps.setString(17, e.getReportDetails());
            ps.setString(18, e.getNotes());
            ps.setString(19, e.getPhotos());
            ps.setObject(20, e.getUpdatedAt());
            ps.setString(21, e.getId());

            return ps.executeUpdate() > 0;
        }
    }

    // ── DELETE ────────────────────────────────────────────────

    public boolean delete(String id) throws SQLException {
        final String sql = "DELETE FROM equipment WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ── REPORT LOST / STOLEN ──────────────────────────────────

    /**
     * Actualiza el estado del equipo a 'lost' o 'stolen' y registra el reporte.
     *
     * @param id          ID del equipo
     * @param status      "lost" o "stolen"
     * @param reportDate  Fecha del incidente
     * @param details     Descripción del incidente (folio MP, detalles, etc.)
     */
    public boolean report(String id, String status, LocalDate reportDate, String details)
            throws SQLException {

        final String sql =
            "UPDATE equipment " +
            "SET status = ?, report_date = ?, report_details = ?, updated_at = ? " +
            "WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setObject(2, reportDate);
            ps.setString(3, details);
            ps.setObject(4, LocalDateTime.now());
            ps.setString(5, id);

            return ps.executeUpdate() > 0;
        }
    }

    // ── STATS ─────────────────────────────────────────────────

    /** Retorna el conteo de equipos agrupado por estado. */
    public List<Object[]> countByStatus() throws SQLException {
        final String sql = "SELECT status, COUNT(*) as total FROM equipment GROUP BY status";
        List<Object[]> result = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Object[]{ rs.getString("status"), rs.getLong("total") });
            }
        }
        return result;
    }

    // ── MAPPING ───────────────────────────────────────────────

    private List<Equipment> mapList(ResultSet rs) throws SQLException {
        List<Equipment> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }

    private Equipment mapRow(ResultSet rs) throws SQLException {
        Equipment e = new Equipment();

        e.setId(rs.getString("id"));
        e.setCategory(rs.getString("category"));
        e.setBrand(rs.getString("brand"));
        e.setModel(rs.getString("model"));
        e.setSerialNumber(rs.getString("serial_number"));

        Date pd = rs.getDate("purchase_date");
        if (pd != null) e.setPurchaseDate(pd.toLocalDate());

        BigDecimal price = rs.getBigDecimal("purchase_price");
        e.setPurchasePrice(price);

        e.setCondition(rs.getString("equipment_condition"));
        e.setStatus(rs.getString("status"));

        e.setWarrantyHas(rs.getBoolean("warranty_has"));
        Date we = rs.getDate("warranty_expiry");
        if (we != null) e.setWarrantyExpiry(we.toLocalDate());
        e.setWarrantyProvider(rs.getString("warranty_provider"));

        e.setInsuranceHas(rs.getBoolean("insurance_has"));
        e.setInsuranceProvider(rs.getString("insurance_provider"));
        e.setInsurancePolicyNumber(rs.getString("insurance_policy_number"));
        Date ie = rs.getDate("insurance_expiry");
        if (ie != null) e.setInsuranceExpiry(ie.toLocalDate());

        Date rd = rs.getDate("report_date");
        if (rd != null) e.setReportDate(rd.toLocalDate());
        e.setReportDetails(rs.getString("report_details"));

        e.setNotes(rs.getString("notes"));
        e.setPhotos(rs.getString("photos"));

        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) e.setCreatedAt(ca.toLocalDateTime());

        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) e.setUpdatedAt(ua.toLocalDateTime());

        return e;
    }
}
