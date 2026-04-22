package tn.dhc.services;

import tn.dhc.entities.Annonce;
import tn.dhc.utils.MyConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceAnnonce {

    private final Connection cnx;

    public ServiceAnnonce() {
        cnx = MyConnection.getInstance().getConnection();
    }

    // ── CREATE ────────────────────────────────────────────────────────────────
    public void ajouter(Annonce a) throws SQLException {
        String sql = "INSERT INTO annonce (titre_annonce, description, date_publication, urgence, etat_annonce, idU) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, a.getTitre_annonce());
            ps.setString(2, a.getDescription());
            ps.setDate(3, Date.valueOf(a.getDate_publication()));
            ps.setString(4, a.getUrgence());
            ps.setString(5, a.getEtat_annonce());
            ps.setInt(6, a.getIdU());
            ps.executeUpdate();
        }
    }

    // ── READ ALL ──────────────────────────────────────────────────────────────
    public List<Annonce> getAll() throws SQLException {
        List<Annonce> list = new ArrayList<>();
        String sql = "SELECT * FROM annonce ORDER BY date_publication DESC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // ── READ BY ID ────────────────────────────────────────────────────────────
    public Annonce getById(int id) throws SQLException {
        String sql = "SELECT * FROM annonce WHERE idAnnonce = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    public void modifier(Annonce a) throws SQLException {
        String sql = "UPDATE annonce SET titre_annonce=?, description=?, date_publication=?, " +
                     "urgence=?, etat_annonce=? WHERE idAnnonce=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, a.getTitre_annonce());
            ps.setString(2, a.getDescription());
            ps.setDate(3, Date.valueOf(a.getDate_publication()));
            ps.setString(4, a.getUrgence());
            ps.setString(5, a.getEtat_annonce());
            ps.setInt(6, a.getIdAnnonce());
            ps.executeUpdate();
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM annonce WHERE idAnnonce=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ── SEARCH ────────────────────────────────────────────────────────────────
    public List<Annonce> rechercher(String keyword) throws SQLException {
        List<Annonce> list = new ArrayList<>();
        String sql = "SELECT * FROM annonce WHERE titre_annonce LIKE ? OR description LIKE ? ORDER BY date_publication DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            ps.setString(1, k);
            ps.setString(2, k);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    // ── SORT ──────────────────────────────────────────────────────────────────
    public List<Annonce> trier(String critere) throws SQLException {
        String orderBy;
        switch (critere) {
            case "DATE_DESC":  orderBy = "date_publication DESC"; break;
            case "DATE_ASC":   orderBy = "date_publication ASC";  break;
            case "URGENCE_HIGH": orderBy = "FIELD(urgence,'ROUGE','ORANGE','VERT')"; break;
            case "URGENCE_LOW":  orderBy = "FIELD(urgence,'VERT','ORANGE','ROUGE')"; break;
            case "DISPO":      orderBy = "FIELD(etat_annonce,'DISPONIBLE','NON_DISPONIBLE')"; break;
            default:           orderBy = "date_publication DESC";
        }
        List<Annonce> list = new ArrayList<>();
        String sql = "SELECT * FROM annonce ORDER BY " + orderBy;
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // ── MAPPER ────────────────────────────────────────────────────────────────
    private Annonce map(ResultSet rs) throws SQLException {
        return new Annonce(
            rs.getInt("idAnnonce"),
            rs.getString("titre_annonce"),
            rs.getString("description"),
            rs.getDate("date_publication").toLocalDate(),
            rs.getString("urgence"),
            rs.getString("etat_annonce"),
            rs.getInt("idU")
        );
    }
}
