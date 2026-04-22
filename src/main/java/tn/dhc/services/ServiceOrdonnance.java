package tn.dhc.services;

import tn.dhc.entities.Medicament;
import tn.dhc.entities.Ordonnance;
import tn.dhc.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServiceOrdonnance implements IService<Ordonnance> {

    private final Connection connection = MyConnection.getInstance().getConnection();

    @Override
    public void ajouter(Ordonnance o) {
        insertOrdonnanceReturningId(o);
    }

    /**
     * Insère l'ordonnance et les lignes {@code ordonnance_medicament}.
     */
    public int ajouterAvecMedicaments(Ordonnance o, List<Integer> medicamentIds) {
        int id = insertOrdonnanceReturningId(o);
        if (id > 0) {
            o.setId(id);
            replaceOrdonnanceMedicaments(id, medicamentIds);
            decrementMedicamentsStock(medicamentIds);
        }
        return id;
    }

    /**
     * Met à jour l'ordonnance et remplace les médicaments liés.
     */
    public void modifierAvecMedicaments(Ordonnance o, List<Integer> medicamentIds) {
        modifier(o);
        replaceOrdonnanceMedicaments(o.getId(), medicamentIds);
    }

    private int insertOrdonnanceReturningId(Ordonnance o) {
        String req = "INSERT INTO `ordonnance` (`posologie`, `frequence`, `duree_traitement`, `date`, `fiche_id`, `medecin_user_id`, `signature_envelope_id`, `signature_ceremony_url`, `signature_deliverable_url`, `signature_status`, `signature_email_sent_at`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, o.getPosologie());
            ps.setString(2, o.getFrequence());
            ps.setInt(3, o.getDureeTraitement());
            ps.setDate(4, o.getDate());
            ps.setInt(5, o.getFicheId());
            if (o.getMedecinUserId() != null) {
                ps.setInt(6, o.getMedecinUserId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setString(7, o.getSignatureEnvelopeId());
            ps.setString(8, o.getSignatureCeremonyUrl());
            ps.setString(9, o.getSignatureDeliverableUrl());
            ps.setString(10, o.getSignatureStatus());
            ps.setTimestamp(11, o.getSignatureEmailSentAt());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return -1;
    }

    public void replaceOrdonnanceMedicaments(int ordonnanceId, List<Integer> medicamentIds) {
        deleteOrdonnanceMedicaments(ordonnanceId);
        if (medicamentIds == null || medicamentIds.isEmpty()) {
            return;
        }
        String ins = "INSERT INTO ordonnance_medicament (ordonnance_id, medicament_id) VALUES (?,?)";
        try (PreparedStatement ps = connection.prepareStatement(ins)) {
            for (Integer mid : medicamentIds) {
                if (mid == null) {
                    continue;
                }
                ps.setInt(1, ordonnanceId);
                ps.setInt(2, mid);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void deleteOrdonnanceMedicaments(int ordonnanceId) {
        String del = "DELETE FROM ordonnance_medicament WHERE ordonnance_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(del)) {
            ps.setInt(1, ordonnanceId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private void decrementMedicamentsStock(List<Integer> medicamentIds) {
        if (medicamentIds == null || medicamentIds.isEmpty()) {
            return;
        }
        Set<Integer> unique = new HashSet<>();
        for (Integer id : medicamentIds) {
            if (id != null) {
                unique.add(id);
            }
        }
        String req = "UPDATE medicament SET stock = stock - 1 WHERE id = ? AND stock > 0";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            for (Integer id : unique) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public List<Integer> findMedicamentIdsByOrdonnance(int ordonnanceId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT medicament_id FROM ordonnance_medicament WHERE ordonnance_id = ? ORDER BY medicament_id";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, ordonnanceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("medicament_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return ids;
    }

    /**
     * Libellés des médicaments liés (pour affichage carte).
     */
    public String getMedicamentsSummaryForOrdonnance(int ordonnanceId) {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT m.nom_medicament FROM ordonnance_medicament om "
                + "INNER JOIN medicament m ON om.medicament_id = m.id "
                + "WHERE om.ordonnance_id = ? ORDER BY m.nom_medicament";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, ordonnanceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(rs.getString("nom_medicament"));
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return sb.length() > 0 ? sb.toString() : "—";
    }

    @Override
    public void supprimer(Ordonnance o) {
        deleteOrdonnanceMedicaments(o.getId());
        String req = "DELETE FROM `ordonnance` WHERE `id` = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, o.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void modifier(Ordonnance o) {
        String req = "UPDATE `ordonnance` SET `posologie`=?, `frequence`=?, `duree_traitement`=?, `date`=?, `fiche_id`=?, `medecin_user_id`=?, `signature_envelope_id`=?, `signature_ceremony_url`=?, `signature_deliverable_url`=?, `signature_status`=?, `signature_email_sent_at`=? WHERE `id`=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, o.getPosologie());
            ps.setString(2, o.getFrequence());
            ps.setInt(3, o.getDureeTraitement());
            ps.setDate(4, o.getDate());
            ps.setInt(5, o.getFicheId());
            if (o.getMedecinUserId() != null) {
                ps.setInt(6, o.getMedecinUserId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setString(7, o.getSignatureEnvelopeId());
            ps.setString(8, o.getSignatureCeremonyUrl());
            ps.setString(9, o.getSignatureDeliverableUrl());
            ps.setString(10, o.getSignatureStatus());
            ps.setTimestamp(11, o.getSignatureEmailSentAt());
            ps.setInt(12, o.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void updateSignatureInfo(int ordonnanceId, String envelopeId, String ceremonyUrl, String deliverableUrl, String signatureStatus) {
        String req = "UPDATE `ordonnance` SET `signature_envelope_id`=?, `signature_ceremony_url`=?, `signature_deliverable_url`=?, `signature_status`=? WHERE `id`=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, envelopeId);
            ps.setString(2, ceremonyUrl);
            ps.setString(3, deliverableUrl);
            ps.setString(4, signatureStatus);
            ps.setInt(5, ordonnanceId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void markSignedEmailSent(int ordonnanceId) {
        String req = "UPDATE `ordonnance` SET `signature_email_sent_at` = CURRENT_TIMESTAMP WHERE `id` = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, ordonnanceId);
            ps.executeUpdate();
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
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    /** Ordonnances liées aux fiches du patient donné. */
    public List<Ordonnance> findByPatientUserId(int patientUserId) {
        List<Ordonnance> list = new ArrayList<>();
        String req = "SELECT o.* FROM `ordonnance` o "
                + "INNER JOIN `fiche` f ON o.`fiche_id` = f.`id` WHERE f.`user_id` = ? "
                + "ORDER BY o.`date` DESC, o.`id` DESC";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, patientUserId);
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

    /** Médicaments prescrits sur cette ordonnance (ordre base). */
    public List<Medicament> findMedicamentsByOrdonnance(int ordonnanceId) {
        List<Medicament> out = new ArrayList<>();
        ServiceMedicament medicamentService = new ServiceMedicament();
        for (Integer mid : findMedicamentIdsByOrdonnance(ordonnanceId)) {
            Medicament m = medicamentService.getOneById(mid);
            if (m != null) {
                out.add(m);
            }
        }
        return out;
    }

    public List<Ordonnance> findByMedecinUserId(int medecinUserId) {
        List<Ordonnance> list = new ArrayList<>();
        String req = "SELECT * FROM `ordonnance` WHERE `medecin_user_id` = ? ORDER BY `date` DESC, `id` DESC";
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

    private static Ordonnance mapRow(ResultSet rs) throws SQLException {
        Ordonnance o = new Ordonnance();
        o.setId(rs.getInt("id"));
        o.setPosologie(rs.getString("posologie"));
        o.setFrequence(rs.getString("frequence"));
        o.setDureeTraitement(rs.getInt("duree_traitement"));
        o.setDate(rs.getDate("date"));
        o.setFicheId(rs.getInt("fiche_id"));
        try {
            int mid = rs.getInt("medecin_user_id");
            o.setMedecinUserId(rs.wasNull() ? null : mid);
        } catch (SQLException ignored) {
            o.setMedecinUserId(null);
        }
        o.setSignatureEnvelopeId(rs.getString("signature_envelope_id"));
        o.setSignatureCeremonyUrl(rs.getString("signature_ceremony_url"));
        o.setSignatureDeliverableUrl(rs.getString("signature_deliverable_url"));
        o.setSignatureStatus(rs.getString("signature_status"));
        o.setSignatureEmailSentAt(rs.getTimestamp("signature_email_sent_at"));
        return o;
    }
}
