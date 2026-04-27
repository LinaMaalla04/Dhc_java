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

    /**
     * Incrémente le nombre de participants d'un événement.
     */
    public void incrementerParticipant(int eventId) {
        try {
            String sql = "UPDATE event SET nb_participant = nb_participant + 1 WHERE id = ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, eventId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }


    /**
     * Retourne les événements auxquels un utilisateur participe (via table participant).
     */
    public List<Event> getEventsByParticipant(int userId) {
        List<Event> events = new ArrayList<>();
        try {
            String sql = "SELECT e.* FROM event e " +
                    "INNER JOIN participant p ON p.event_id = e.id " +
                    "WHERE p.user_id = ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                events.add(mapResultSetToEvent(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return events;
    }


    /**
     * Insère un participant dans la table participant.
     * Retourne false si déjà inscrit (évite les doublons).
     */
    public boolean ajouterParticipant(int eventId, int userId) {
        if (isDejaParticipant(eventId, userId)) return false;
        try {
            String sql = "INSERT INTO participant (event_id, user_id) VALUES (?, ?)";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si l'utilisateur participe déjà à cet événement.
     */
    public boolean isDejaParticipant(int eventId, int userId) {
        try {
            String sql = "SELECT COUNT(*) FROM participant WHERE event_id = ? AND user_id = ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return false;
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