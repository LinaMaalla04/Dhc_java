package tn.dhc.services;

import tn.dhc.entities.Rdv;
import tn.dhc.utils.MyConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RdvService {

    private Connection cnx = MyConnection.getInstance().getConnection();

<<<<<<< HEAD
    public void add(Rdv r) {
        String sql = "INSERT INTO rdv (motif, priorite, statut, date_rdv, creneau_id, user_id, fiche_id) VALUES (?,?,?,?,?,?,?)";
=======
    // 🔹 Ajouter
    public void add(Rdv r) {
        String sql = "INSERT INTO rdv (motif, priorite, statut, date_rdv, creneau_id, user_id) VALUES (?,?,?,?,?,?)";
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, r.getMotif());
            ps.setString(2, r.getPriorite());
            ps.setString(3, r.getStatut());
            ps.setDate(4, Date.valueOf(r.getDateRdv()));
            ps.setInt(5, r.getCreneauId());
            ps.setInt(6, r.getUserId());
<<<<<<< HEAD
            if (r.getFicheId() != null) {
                ps.setInt(7, r.getFicheId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
=======
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064

            ps.executeUpdate();
            System.out.println("RDV ajouté !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

<<<<<<< HEAD
=======
    // 🔹 Afficher tout
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
    public List<Rdv> getAll() {
        List<Rdv> list = new ArrayList<>();
        String sql = "SELECT * FROM rdv";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Rdv r = new Rdv(
                        rs.getInt("id"),
                        rs.getString("motif"),
                        rs.getString("priorite"),
                        rs.getString("statut"),
                        rs.getDate("date_rdv").toLocalDate(),
                        rs.getInt("creneau_id"),
<<<<<<< HEAD
                        rs.getInt("user_id"),
                        rs.getObject("fiche_id") != null ? rs.getInt("fiche_id") : null
=======
                        rs.getInt("user_id")
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
                );
                list.add(r);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }

<<<<<<< HEAD
    public List<Rdv> findByFicheId(int ficheId) {
        List<Rdv> list = new ArrayList<>();
        String sql = "SELECT * FROM rdv WHERE fiche_id = ? ORDER BY date_rdv DESC, id DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, ficheId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Rdv r = new Rdv(
                            rs.getInt("id"),
                            rs.getString("motif"),
                            rs.getString("priorite"),
                            rs.getString("statut"),
                            rs.getDate("date_rdv").toLocalDate(),
                            rs.getInt("creneau_id"),
                            rs.getInt("user_id"),
                            rs.getObject("fiche_id") != null ? rs.getInt("fiche_id") : null
                    );
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public List<Rdv> findByDoctorUserId(int doctorUserId) {
        List<Rdv> list = new ArrayList<>();
        String sql = "SELECT r.* FROM rdv r INNER JOIN creneau c ON c.id = r.creneau_id "
                + "WHERE c.user_id = ? ORDER BY r.date_rdv DESC, r.id DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, doctorUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Rdv r = new Rdv(
                            rs.getInt("id"),
                            rs.getString("motif"),
                            rs.getString("priorite"),
                            rs.getString("statut"),
                            rs.getDate("date_rdv").toLocalDate(),
                            rs.getInt("creneau_id"),
                            rs.getInt("user_id"),
                            rs.getObject("fiche_id") != null ? rs.getInt("fiche_id") : null
                    );
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public void updateStatut(int rdvId, String statut) {
        String sql = "UPDATE rdv SET statut = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, rdvId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

=======
    // 🔹 Delete
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
    public void delete(int id) {
        String sql = "DELETE FROM rdv WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("RDV supprimé !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
<<<<<<< HEAD
    public void modifier(Rdv r) {

        String sql = "UPDATE rdv SET motif=?, priorite=?, statut=?, date_rdv=?, creneau_id=?, user_id=?, fiche_id=? WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, r.getMotif());
            ps.setString(2, r.getPriorite());
            ps.setString(3, r.getStatut());
            ps.setDate(4, Date.valueOf(r.getDateRdv()));
            ps.setInt(5, r.getCreneauId());
            ps.setInt(6, r.getUserId());
            if (r.getFicheId() != null) {
                ps.setInt(7, r.getFicheId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setInt(8, r.getId());

            ps.executeUpdate();
            System.out.println("RDV modifié !");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
=======
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
}