package tn.dhc.services;

import tn.dhc.entities.Commentaire;
import tn.dhc.utils.MyConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceCommentaire {

    private final Connection cnx;
    private static final String QUEUE_PREFIX = "commentaire_annonce_";

    public ServiceCommentaire() {
        cnx = MyConnection.getInstance().getConnection();
    }

    // ── CREATE ────────────────────────────────────────────────────────────────
    public void ajouter(Commentaire c) throws SQLException {
        String sql = "INSERT INTO messenger_messages (body, headers, queue_name, created_at, available_at) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, c.getBody());
            ps.setString(2, c.getHeaders());
            ps.setString(3, c.getQueue_name());
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        }
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    public void modifier(Commentaire c) throws SQLException {
        String sql = "UPDATE messenger_messages SET body=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, c.getBody());
            ps.setLong(2, c.getId());
            ps.executeUpdate();
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    public void supprimer(long id) throws SQLException {
        String sql = "DELETE FROM messenger_messages WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    // ── DELETE ALL COMMENTS FOR ONE ANNONCE ───────────────────────────────────
    public void supprimerParAnnonce(int annonceId) throws SQLException {
        String sql = "DELETE FROM messenger_messages WHERE queue_name=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, QUEUE_PREFIX + annonceId);
            ps.executeUpdate();
        }
    }

    // ── GET BY ANNONCE ────────────────────────────────────────────────────────
    public List<Commentaire> getByAnnonce(int annonceId) throws SQLException {
        List<Commentaire> list = new ArrayList<>();
        String sql = "SELECT * FROM messenger_messages WHERE queue_name=? ORDER BY created_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, QUEUE_PREFIX + annonceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    // ── GET COMMENT OF SPECIFIC USER ON SPECIFIC ANNONCE ─────────────────────
    public Commentaire getUserComment(int annonceId, int userId) throws SQLException {
        String sql = "SELECT * FROM messenger_messages WHERE queue_name=? AND headers=? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, QUEUE_PREFIX + annonceId);
            ps.setString(2, "user_" + userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    // ── MAPPER ────────────────────────────────────────────────────────────────
    private Commentaire map(ResultSet rs) throws SQLException {
        Commentaire c = new Commentaire();
        c.setId(rs.getLong("id"));
        c.setBody(rs.getString("body"));
        c.setHeaders(rs.getString("headers"));
        c.setQueue_name(rs.getString("queue_name"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) c.setCreated_at(ca.toLocalDateTime());
        Timestamp aa = rs.getTimestamp("available_at");
        if (aa != null) c.setAvailable_at(aa.toLocalDateTime());
        Timestamp da = rs.getTimestamp("delivered_at");
        if (da != null) c.setDelivered_at(da.toLocalDateTime());
        return c;
    }
}
