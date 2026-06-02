package app.photogear.dao;

import app.photogear.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class UserDAO {

    public Optional<User> findByGoogleId(String googleId) throws SQLException {
        final String sql = "SELECT * FROM users WHERE google_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, googleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        }
    }

    public User create(User user) throws SQLException {
        if (user.getId() == null) user.setId(UUID.randomUUID().toString());
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setLastLogin(now);

        final String sql =
            "INSERT INTO users (id, google_id, email, name, picture, created_at, last_login) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getId());
            ps.setString(2, user.getGoogleId());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getName());
            ps.setString(5, user.getPicture());
            ps.setObject(6, user.getCreatedAt());
            ps.setObject(7, user.getLastLogin());
            ps.executeUpdate();
        }
        return user;
    }

    public void updateLastLogin(String id) throws SQLException {
        final String sql = "UPDATE users SET last_login = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, LocalDateTime.now());
            ps.setString(2, id);
            ps.executeUpdate();
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getString("id"));
        u.setGoogleId(rs.getString("google_id"));
        u.setEmail(rs.getString("email"));
        u.setName(rs.getString("name"));
        u.setPicture(rs.getString("picture"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) u.setCreatedAt(ca.toLocalDateTime());
        Timestamp ll = rs.getTimestamp("last_login");
        if (ll != null) u.setLastLogin(ll.toLocalDateTime());
        return u;
    }
}
