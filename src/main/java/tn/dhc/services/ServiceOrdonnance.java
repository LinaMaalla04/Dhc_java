package tn.dhc.services;

import tn.dhc.entities.Ordonnance;
import tn.dhc.utils.MyConnection;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class ServiceOrdonnance implements IService<Ordonnance> {

    private final Connection connection = MyConnection.getInstance().getConnection();

    @Override
    public void ajouter(Ordonnance o) {
        String req = "INSERT INTO `ordonnance` (`posologie`, `frequence`, `duree_traitement`, `date`, `fiche_id`) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, o.getPosologie());
            ps.setString(2, o.getFrequence());
            ps.setInt(3, o.getDureeTraitement());
            ps.setDate(4, o.getDate());
            ps.setInt(5, o.getFicheId());
            ps.executeUpdate();
            System.out.println("Ordonnance ajoutée.");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(Ordonnance o) {
        String req = "DELETE FROM `ordonnance` WHERE `id` = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, o.getId());
            int n = ps.executeUpdate();
            System.out.println(n > 0 ? "Ordonnance supprimée." : "Aucune ordonnance avec cet id.");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void modifier(Ordonnance o) {
        String req = "UPDATE `ordonnance` SET `posologie`=?, `frequence`=?, `duree_traitement`=?, `date`=?, `fiche_id`=? WHERE `id`=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, o.getPosologie());
            ps.setString(2, o.getFrequence());
            ps.setInt(3, o.getDureeTraitement());
            ps.setDate(4, o.getDate());
            ps.setInt(5, o.getFicheId());
            ps.setInt(6, o.getId());
            int n = ps.executeUpdate();
            System.out.println(n > 0 ? "Ordonnance modifiée." : "Aucune ordonnance avec cet id.");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public List<Ordonnance> getAll() {
        List<Ordonnance> list = new ArrayList<>();
        String req = "SELECT * FROM `ordonnance`";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return list;
    }

    @Override
    public Ordonnance getOneById(int id) {
        String req = "SELECT * FROM `ordonnance` WHERE `id` = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                } else {
                    System.out.println("Aucune ordonnance avec id=" + id);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    private static Ordonnance mapRow(ResultSet rs) throws SQLException {
        Ordonnance o = new Ordonnance();
        o.setId(rs.getInt("id"));
        o.setPosologie(rs.getString("posologie"));
        o.setFrequence(rs.getString("frequence"));
        o.setDureeTraitement(rs.getInt("duree_traitement"));
        o.setDate(rs.getDate("date"));
        o.setFicheId(rs.getInt("fiche_id"));
        return o;
    }
}
