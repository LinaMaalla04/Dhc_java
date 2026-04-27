package tn.dhc.services;

import tn.dhc.entities.NoteMedecin;
import tn.dhc.utils.MyConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NoteMedecinService {

    private final Connection cnx = MyConnection.getInstance().getConnection();

    /**
     * Ajoute ou met à jour la note d'un patient pour un médecin (UPSERT).
     */
    public void ajouterOuModifier(int medecinId, int patientId, int note, String commentaire) {
        try {
            String sql = """
                INSERT INTO note_medecin (medecin_id, patient_id, note, commentaire, created_at)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    note        = VALUES(note),
                    commentaire = VALUES(commentaire),
                    created_at  = VALUES(created_at)
                """;
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, medecinId);
            ps.setInt(2, patientId);
            ps.setInt(3, note);
            ps.setString(4, commentaire);
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[NoteMedecinService] " + e.getMessage());
        }
    }

    /**
     * Retourne toutes les notes pour un médecin donné.
     */
    public List<NoteMedecin> getByMedecin(int medecinId) {
        List<NoteMedecin> list = new ArrayList<>();
        try {
            String sql = "SELECT * FROM note_medecin WHERE medecin_id = ? ORDER BY created_at DESC";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, medecinId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            System.out.println("[NoteMedecinService] " + e.getMessage());
        }
        return list;
    }

    /**
     * Note moyenne d'un médecin (0.0 si aucune note).
     */
    public double getMoyenne(int medecinId) {
        try {
            String sql = "SELECT AVG(note) FROM note_medecin WHERE medecin_id = ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, medecinId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getObject(1) != null) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.out.println("[NoteMedecinService] " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Nombre de notes pour un médecin.
     */
    public int getNbNotes(int medecinId) {
        try {
            String sql = "SELECT COUNT(*) FROM note_medecin WHERE medecin_id = ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, medecinId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("[NoteMedecinService] " + e.getMessage());
        }
        return 0;
    }

    /**
     * Note existante d'un patient pour un médecin (null si pas encore noté).
     */
    public NoteMedecin getByMedecinAndPatient(int medecinId, int patientId) {
        try {
            String sql = "SELECT * FROM note_medecin WHERE medecin_id = ? AND patient_id = ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, medecinId);
            ps.setInt(2, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) {
            System.out.println("[NoteMedecinService] " + e.getMessage());
        }
        return null;
    }

    private NoteMedecin map(ResultSet rs) throws SQLException {
        NoteMedecin n = new NoteMedecin();
        n.setId(rs.getInt("id"));
        n.setMedecinId(rs.getInt("medecin_id"));
        n.setPatientId(rs.getInt("patient_id"));
        n.setNote(rs.getInt("note"));
        n.setCommentaire(rs.getString("commentaire"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) n.setCreatedAt(ts.toLocalDateTime());
        return n;
    }
}
