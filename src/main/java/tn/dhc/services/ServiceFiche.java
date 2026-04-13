package tn.dhc.services;

import tn.dhc.entities.Fiche;
import tn.dhc.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceFiche implements IService<Fiche> {

    private final Connection connection = MyConnection.getInstance().getConnection();

    @Override
    public void ajouter(Fiche f) {
        String req = "INSERT INTO `fiche` (`poids`, `taille`, `grp_sanguin`, `allergie`, `maladie_chronique`, `tension`, `glycemie`, `date`, `libelle_maladie`, `gravite`, `recommandation`, `user_id`, `medecin_user_id`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setDouble(1, f.getPoids());
            ps.setDouble(2, f.getTaille());
            ps.setString(3, f.getGrpSanguin());
            ps.setString(4, f.getAllergie());
            ps.setString(5, f.getMaladieChronique());
            ps.setString(6, f.getTension());
            ps.setDouble(7, f.getGlycemie());
            ps.setDate(8, f.getDate());
            ps.setString(9, f.getLibelleMaladie());
            ps.setString(10, f.getGravite());
            ps.setString(11, f.getRecommandation());
            ps.setInt(12, f.getUserId());
            if (f.getMedecinUserId() != null) {
                ps.setInt(13, f.getMedecinUserId());
            } else {
                ps.setNull(13, Types.INTEGER);
            }
            ps.executeUpdate();
            System.out.println("Fiche ajoutée.");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(Fiche f) {
        String req = "DELETE FROM `fiche` WHERE `id` = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, f.getId());
            int n = ps.executeUpdate();
            System.out.println(n > 0 ? "Fiche supprimée." : "Aucune fiche avec cet id.");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void modifier(Fiche f) {
        String req = "UPDATE `fiche` SET `poids`=?, `taille`=?, `grp_sanguin`=?, `allergie`=?, `maladie_chronique`=?, `tension`=?, `glycemie`=?, `date`=?, `libelle_maladie`=?, `gravite`=?, `recommandation`=?, `user_id`=?, `medecin_user_id`=? WHERE `id`=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setDouble(1, f.getPoids());
            ps.setDouble(2, f.getTaille());
            ps.setString(3, f.getGrpSanguin());
            ps.setString(4, f.getAllergie());
            ps.setString(5, f.getMaladieChronique());
            ps.setString(6, f.getTension());
            ps.setDouble(7, f.getGlycemie());
            ps.setDate(8, f.getDate());
            ps.setString(9, f.getLibelleMaladie());
            ps.setString(10, f.getGravite());
            ps.setString(11, f.getRecommandation());
            ps.setInt(12, f.getUserId());
            if (f.getMedecinUserId() != null) {
                ps.setInt(13, f.getMedecinUserId());
            } else {
                ps.setNull(13, Types.INTEGER);
            }
            ps.setInt(14, f.getId());
            int n = ps.executeUpdate();
            System.out.println(n > 0 ? "Fiche modifiée." : "Aucune fiche avec cet id.");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public List<Fiche> getAll() {
        List<Fiche> list = new ArrayList<>();
        String req = "SELECT * FROM `fiche`";
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
    public Fiche getOneById(int id) {
        String req = "SELECT * FROM `fiche` WHERE `id` = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
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

    /** Fiches d'un patient (ordonnances rattachées via {@code fiche_id}). */
    public List<Fiche> findByPatientUserId(int userId) {
        List<Fiche> list = new ArrayList<>();
        String req = "SELECT * FROM `fiche` WHERE `user_id` = ? ORDER BY `date` DESC, `id` DESC";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return list;
    }

    public List<Fiche> findByMedecinUserId(int medecinUserId) {
        List<Fiche> list = new ArrayList<>();
        String req = "SELECT * FROM `fiche` WHERE `medecin_user_id` = ? ORDER BY `date` DESC, `id` DESC";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, medecinUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("findByMedecinUserId: " + e.getMessage());
            return new ArrayList<>();
        }
        return list;
    }

    private static Fiche mapRow(ResultSet rs) throws SQLException {
        Fiche f = new Fiche();
        f.setId(rs.getInt("id"));
        f.setPoids(rs.getDouble("poids"));
        f.setTaille(rs.getDouble("taille"));
        f.setGrpSanguin(rs.getString("grp_sanguin"));
        f.setAllergie(rs.getString("allergie"));
        f.setMaladieChronique(rs.getString("maladie_chronique"));
        f.setTension(rs.getString("tension"));
        f.setGlycemie(rs.getDouble("glycemie"));
        f.setDate(rs.getDate("date"));
        f.setLibelleMaladie(rs.getString("libelle_maladie"));
        f.setGravite(rs.getString("gravite"));
        f.setRecommandation(rs.getString("recommandation"));
        f.setUserId(rs.getInt("user_id"));
        try {
            int mid = rs.getInt("medecin_user_id");
            f.setMedecinUserId(rs.wasNull() ? null : mid);
        } catch (SQLException ignored) {
            f.setMedecinUserId(null);
        }
        return f;
    }
}
