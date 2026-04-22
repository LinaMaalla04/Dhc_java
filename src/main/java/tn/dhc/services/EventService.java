package tn.dhc.services;

import tn.dhc.entities.Event;
import tn.dhc.utils.MyConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class EventService implements IService<Event> {

    private Connection cnx;

    public EventService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Event e) {
        try {
            String sql = "INSERT INTO event (titre_event, theme_sante, description, date_event, heure_debut, heure_fin, nb_participant, user_id, event_lieu_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setString(1, e.getTitreEvent());
            ps.setString(2, e.getThemeSante());
            ps.setString(3, e.getDescription());

            ps.setDate(4, Date.valueOf(e.getDateEvent()));

            if (e.getHeureDebut() != null)
                ps.setTime(5, Time.valueOf(e.getHeureDebut()));
            else
                ps.setNull(5, Types.TIME);

            if (e.getHeureFin() != null)
                ps.setTime(6, Time.valueOf(e.getHeureFin()));
            else
                ps.setNull(6, Types.TIME);

            ps.setInt(7, e.getNbParticipant() != null ? e.getNbParticipant() : 0);

            ps.setInt(8, e.getUserId());
            ps.setInt(9, e.getEventLieuId());

            ps.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void supprimer(Event e) {
        try {
            String sql = "DELETE FROM event WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setInt(1, e.getId());
            ps.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void modifier(Event e) {
        try {
            String sql = "UPDATE event SET titre_event=?, theme_sante=?, description=?, date_event=?, heure_debut=?, heure_fin=?, nb_participant=?, user_id=?, event_lieu_id=? WHERE id=?";

            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setString(1, e.getTitreEvent());
            ps.setString(2, e.getThemeSante());
            ps.setString(3, e.getDescription());

            ps.setDate(4, Date.valueOf(e.getDateEvent()));

            if (e.getHeureDebut() != null)
                ps.setTime(5, Time.valueOf(e.getHeureDebut()));
            else
                ps.setNull(5, Types.TIME);

            if (e.getHeureFin() != null)
                ps.setTime(6, Time.valueOf(e.getHeureFin()));
            else
                ps.setNull(6, Types.TIME);

            if (e.getNbParticipant() != null)
                ps.setInt(7, e.getNbParticipant());
            else
                ps.setNull(7, Types.INTEGER);

            ps.setInt(8, e.getUserId());
            ps.setInt(9, e.getEventLieuId());
            ps.setInt(10, e.getId());

            ps.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public List<Event> getAll() {
        List<Event> events = new ArrayList<>();

        try {
            String sql = "SELECT * FROM event";
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                events.add(mapResultSetToEvent(rs));
            }

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        return events;
    }

    @Override
    public Event getOneById(int id) {
        try {
            String sql = "SELECT * FROM event WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToEvent(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        return null;
    }

    private Event mapResultSetToEvent(ResultSet rs) throws SQLException {
        return new Event(
                rs.getInt("id"),
                rs.getString("titre_event"),
                rs.getString("theme_sante"),
                rs.getString("description"),
                rs.getDate("date_event").toLocalDate(),
                rs.getTime("heure_debut") != null ? rs.getTime("heure_debut").toLocalTime() : null,
                rs.getTime("heure_fin") != null ? rs.getTime("heure_fin").toLocalTime() : null,
                rs.getObject("nb_participant") != null ? rs.getInt("nb_participant") : null,
                rs.getInt("user_id"),
                rs.getInt("event_lieu_id")
        );
    }
}