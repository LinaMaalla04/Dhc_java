package tn.dhc.services;

import tn.dhc.entities.Creneau;
import tn.dhc.utils.MyConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CreneauService {

    private Connection cnx = MyConnection.getInstance().getConnection();

    // 🔹 Ajouter
    public void add(Creneau c) {
        String sql = "INSERT INTO creneau (date_creneau, hdebut, hfin, statut, user_id) VALUES (?,?,?,?,?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(c.getDateCreneau()));
            ps.setTime(2, Time.valueOf(c.getHdebut()));
            ps.setTime(3, Time.valueOf(c.getHfin()));
            ps.setString(4, c.getStatut());
            ps.setInt(5, c.getUserId());

            ps.executeUpdate();
            System.out.println("Créneau ajouté !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // 🔹 Afficher tout
    public List<Creneau> getAll() {
        List<Creneau> list = new ArrayList<>();
        String sql = "SELECT * FROM creneau";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Creneau c = new Creneau(
                        rs.getInt("id"),
                        rs.getDate("date_creneau").toLocalDate(),
                        rs.getTime("hdebut").toLocalTime(),
                        rs.getTime("hfin").toLocalTime(),
                        rs.getString("statut"),
                        rs.getInt("user_id")
                );
                list.add(c);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }

    // 🔹 Delete
    public void delete(int id) {
        String sql = "DELETE FROM creneau WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Créneau supprimé !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}