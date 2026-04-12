package tn.dhc.services;

import tn.dhc.entities.Medicament;
import tn.dhc.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceMedicament {

    private final Connection conn;

    public ServiceMedicament() {
        conn = MyConnection.getInstance().getConnection();
    }

    public void ajouter(Medicament m) {
        String req = "INSERT INTO medicament (nom_medicament, categorie, dosage, forme, date_expiration, stock) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, m.getNomMedicament());
            ps.setString(2, m.getCategorie());
            ps.setString(3, m.getDosage());
            ps.setString(4, m.getForme());
            ps.setDate(5, m.getDateExpiration());
            ps.setInt(6, Math.max(0, m.getStock()));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void modifier(Medicament m) {
        String req = "UPDATE medicament SET nom_medicament=?, categorie=?, dosage=?, forme=?, date_expiration=?, stock=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, m.getNomMedicament());
            ps.setString(2, m.getCategorie());
            ps.setString(3, m.getDosage());
            ps.setString(4, m.getForme());
            ps.setDate(5, m.getDateExpiration());
            ps.setInt(6, Math.max(0, m.getStock()));
            ps.setInt(7, m.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void supprimer(Medicament m) {
        String req = "DELETE FROM medicament WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, m.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public List<Medicament> getAll() {
        List<Medicament> list = new ArrayList<>();
        String sql = "SELECT * FROM medicament";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return list;
    }

    public Medicament getOneById(int id) {
        String sql = "SELECT * FROM medicament WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    private static Medicament mapRow(ResultSet rs) throws SQLException {
        Medicament m = new Medicament();
        m.setId(rs.getInt("id"));
        m.setNomMedicament(rs.getString("nom_medicament"));
        m.setCategorie(rs.getString("categorie"));
        m.setDosage(rs.getString("dosage"));
        m.setForme(rs.getString("forme"));
        m.setDateExpiration(rs.getDate("date_expiration"));
        int stock = 0;
        try {
            stock = rs.getInt("stock");
            if (rs.wasNull()) {
                stock = 0;
            }
        } catch (SQLException ignored) {
            stock = 0;
        }
        m.setStock(stock);
        return m;
    }
}
