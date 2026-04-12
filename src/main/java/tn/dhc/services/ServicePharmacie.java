package tn.dhc.services;

import tn.dhc.entities.Pharmacie;
import tn.dhc.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePharmacie implements IService<Pharmacie> {

    private final Connection conn = MyConnection.getInstance().getConnection();

    @Override
    public void ajouter(Pharmacie p) {
        String req = "INSERT INTO pharmacie (nom, adresse, telephone, responsable, hopital) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getAdresse());
            ps.setString(3, p.getTelephone());
            ps.setString(4, p.getResponsable());
            if (p.getHopital() != null) {
                ps.setString(5, p.getHopital());
            } else {
                ps.setNull(5, Types.VARCHAR);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void modifier(Pharmacie p) {
        String req = "UPDATE pharmacie SET nom=?, adresse=?, telephone=?, responsable=?, hopital=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getAdresse());
            ps.setString(3, p.getTelephone());
            ps.setString(4, p.getResponsable());
            if (p.getHopital() != null) {
                ps.setString(5, p.getHopital());
            } else {
                ps.setNull(5, Types.VARCHAR);
            }
            ps.setInt(6, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(Pharmacie p) {
        String req = "DELETE FROM pharmacie WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public List<Pharmacie> getAll() {
        List<Pharmacie> list = new ArrayList<>();
        String sql = "SELECT * FROM pharmacie";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return list;
    }

    @Override
    public Pharmacie getOneById(int id) {
        String sql = "SELECT * FROM pharmacie WHERE id=?";
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

    private static Pharmacie mapRow(ResultSet rs) throws SQLException {
        Pharmacie p = new Pharmacie();
        p.setId(rs.getInt("id"));
        p.setNom(rs.getString("nom"));
        p.setAdresse(rs.getString("adresse"));
        p.setTelephone(rs.getString("telephone"));
        p.setResponsable(rs.getString("responsable"));
        p.setHopital(rs.getString("hopital"));
        return p;
    }
}
