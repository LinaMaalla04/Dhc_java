package tn.dhc.services;

import tn.dhc.entities.Event;
import tn.dhc.utils.MyConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
<<<<<<< HEAD
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tn.dhc.entities.EventStatistics;
=======
import java.util.List;
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064

public class EventService implements IService<Event> {

    private Connection cnx;
<<<<<<< HEAD
    private Integer cachedDescriptionMaxLength;
=======
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064

    public EventService() {
        cnx = MyConnection.getInstance().getConnection();
    }

<<<<<<< HEAD
=======
    // ✅ CREATE
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
    @Override
    public void ajouter(Event e) {
        try {
            String sql = "INSERT INTO event (titre_event, theme_sante, description, date_event, heure_debut, heure_fin, nb_participant, user_id, event_lieu_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setString(1, e.getTitreEvent());
            ps.setString(2, e.getThemeSante());
<<<<<<< HEAD
            ps.setString(3, normalizeDescriptionForDb(e.getDescription()));
=======
            ps.setString(3, e.getDescription());
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064

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
<<<<<<< HEAD
            throw new RuntimeException("Insertion event impossible: " + ex.getMessage(), ex);
        }
    }

=======
            System.out.println(ex.getMessage());
        }
    }

    // ✅ DELETE
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
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

<<<<<<< HEAD
=======
    // ✅ UPDATE
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
    @Override
    public void modifier(Event e) {
        try {
            String sql = "UPDATE event SET titre_event=?, theme_sante=?, description=?, date_event=?, heure_debut=?, heure_fin=?, nb_participant=?, user_id=?, event_lieu_id=? WHERE id=?";

            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setString(1, e.getTitreEvent());
            ps.setString(2, e.getThemeSante());
<<<<<<< HEAD
            ps.setString(3, normalizeDescriptionForDb(e.getDescription()));
=======
            ps.setString(3, e.getDescription());
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064

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

<<<<<<< HEAD
=======
    // ✅ GET ALL
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
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

<<<<<<< HEAD
    /**
     * Agrégats pour le tableau de bord (totaux, thèmes, fenêtre de mois, lieux).
     */
    public EventStatistics computeStatistics() {
        return EventStatistics.fromEvents(getAll());
    }

    /** Nombre total d'événements (requête SQL légère). */
    public int countEventsSql() {
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM event")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return 0;
    }

    /** Somme des participants enregistrés ({@code nb_participant}). */
    public long sumParticipantsSql() {
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(SUM(nb_participant), 0) FROM event")) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return 0L;
    }

    /**
     * Répartition par thème directement en base (libellé affiché = valeur stockée ou « sans thème »).
     */
    public Map<String, Long> countByThemeSql() {
        Map<String, Long> map = new LinkedHashMap<>();
        String sql = """
                SELECT COALESCE(NULLIF(TRIM(theme_sante), ''), '(sans thème)') AS theme, COUNT(*) AS c
                FROM event
                GROUP BY COALESCE(NULLIF(TRIM(theme_sante), ''), '(sans thème)')
                ORDER BY c DESC
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("theme"), rs.getLong("c"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return map;
    }

=======
    // ✅ GET ONE
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
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

<<<<<<< HEAD
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

=======
    // 🔁 MAPPING
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
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
<<<<<<< HEAD

    private String normalizeDescriptionForDb(String description) {
        if (description == null) {
            return null;
        }
        String clean = description.trim();
        if (clean.isEmpty()) {
            return clean;
        }
        int max = resolveDescriptionMaxLength();
        if (max > 0 && clean.length() > max) {
            return clean.substring(0, max);
        }
        return clean;
    }

    private int resolveDescriptionMaxLength() {
        if (cachedDescriptionMaxLength != null) {
            return cachedDescriptionMaxLength;
        }
        int fallback = 120;
        try {
            String sql = """
                    SELECT CHARACTER_MAXIMUM_LENGTH
                    FROM INFORMATION_SCHEMA.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'event'
                      AND COLUMN_NAME = 'description'
                    """;
            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int len = rs.getInt(1);
                cachedDescriptionMaxLength = len > 0 ? len : fallback;
            } else {
                cachedDescriptionMaxLength = fallback;
            }
        } catch (SQLException ex) {
            cachedDescriptionMaxLength = fallback;
        }
        return cachedDescriptionMaxLength;
    }
=======
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
}