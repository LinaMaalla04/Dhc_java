package tn.dhc.services;

import tn.dhc.entities.Rdv;
import tn.dhc.utils.MyConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RdvService {

    private Connection cnx = MyConnection.getInstance().getConnection();

    // 🔹 Ajouter
    public void add(Rdv r) {
        String sql = "INSERT INTO rdv (motif, priorite, statut, date_rdv, creneau_id, user_id) VALUES (?,?,?,?,?,?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, r.getMotif());
            ps.setString(2, r.getPriorite());
            ps.setString(3, r.getStatut());
            ps.setDate(4, Date.valueOf(r.getDateRdv()));
            ps.setInt(5, r.getCreneauId());
            ps.setInt(6, r.getUserId());

            ps.executeUpdate();
            System.out.println("RDV ajouté !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // 🔹 Afficher tout
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
                        rs.getInt("user_id")
                );
                list.add(r);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }

    // 🔹 Delete
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
}