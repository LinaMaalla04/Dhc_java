package tn.dhc.services;

import tn.dhc.entities.Lieu;
import tn.dhc.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LieuService implements IService<Lieu> {

    private Connection cnx;

    public LieuService() {
        cnx = MyConnection.getInstance().getConnection();
    }

<<<<<<< HEAD
=======
    // ✅ CREATE
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
    @Override
    public void ajouter(Lieu l) {
        try {
            String sql = "INSERT INTO lieu (nom_lieu, adresse, ville, capacite_max, disponible) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setString(1, l.getNomLieu());
            ps.setString(2, l.getAdresse());
            ps.setString(3, l.getVille());

            if (l.getCapaciteMax() != null) {
                ps.setInt(4, l.getCapaciteMax());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }

            ps.setBoolean(5, l.isDisponible());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

<<<<<<< HEAD
=======
    // ✅ DELETE
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
    @Override
    public void supprimer(Lieu l) {
        try {
            String sql = "DELETE FROM lieu WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, l.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

<<<<<<< HEAD
=======
    // ✅ UPDATE
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
    @Override
    public void modifier(Lieu l) {
        try {
            String sql = "UPDATE lieu SET nom_lieu=?, adresse=?, ville=?, capacite_max=?, disponible=? WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setString(1, l.getNomLieu());
            ps.setString(2, l.getAdresse());
            ps.setString(3, l.getVille());

            if (l.getCapaciteMax() != null) {
                ps.setInt(4, l.getCapaciteMax());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }

            ps.setBoolean(5, l.isDisponible());
            ps.setInt(6, l.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

<<<<<<< HEAD
=======
    // ✅ GET ALL
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
    @Override
    public List<Lieu> getAll() {
        List<Lieu> lieux = new ArrayList<>();

        try {
            String sql = "SELECT * FROM lieu";
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                lieux.add(mapResultSetToLieu(rs));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return lieux;
    }

<<<<<<< HEAD
=======
    // ✅ GET ONE
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
    @Override
    public Lieu getOneById(int id) {
        try {
            String sql = "SELECT * FROM lieu WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToLieu(rs);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

<<<<<<< HEAD
=======
    // 🔁 MAPPING
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
    private Lieu mapResultSetToLieu(ResultSet rs) throws SQLException {
        return new Lieu(
                rs.getInt("id"),
                rs.getString("nom_lieu"),
                rs.getString("adresse"),
                rs.getString("ville"),
                rs.getObject("capacite_max") != null ? rs.getInt("capacite_max") : null,
                rs.getBoolean("disponible")
        );
    }
}