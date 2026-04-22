package tn.dhc.entities;

import java.time.LocalDate;

public class Rdv {

    private int id;
    private String motif;
    private String priorite;
    private String statut;
    private LocalDate dateRdv;
    private int creneauId;
    private int userId;
    private Creneau creneau;


    public Rdv() {
    }

    public Rdv(String motif, String priorite, String statut,
               LocalDate dateRdv, int creneauId, int userId) {
        this.motif = motif;
        this.priorite = priorite;
        this.statut = statut;
        this.dateRdv = dateRdv;
        this.creneauId = creneauId;
        this.userId = userId;
    }

    public Rdv(int id, String motif, String priorite, String statut,
               LocalDate dateRdv, int creneauId, int userId) {
        this.id = id;
        this.motif = motif;
        this.priorite = priorite;
        this.statut = statut;
        this.dateRdv = dateRdv;
        this.creneauId = creneauId;
        this.userId = userId;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getPriorite() {
        return priorite;
    }

    public void setPriorite(String priorite) {
        this.priorite = priorite;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public LocalDate getDateRdv() {
        return dateRdv;
    }

    public void setDateRdv(LocalDate dateRdv) {
        this.dateRdv = dateRdv;
    }

    public int getCreneauId() {
        return creneauId;
    }

    public void setCreneauId(int creneauId) {
        this.creneauId = creneauId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
    public Creneau getCreneau() {
        return creneau;
    }

    public void setCreneau(Creneau creneau) {
        this.creneau = creneau;
    }

    @Override
    public String toString() {
        return "📌 Motif: " + motif + " | " +
                "⚡ Priorité: " + priorite + " | " +
                "📊 Statut: " + statut + " | " +
                "📅 Date: " + dateRdv ;
//                "  + | 🕐 " + (creneau != null ?
//                creneau.getHdebut() + "-" + creneau.getHfin() : creneauId) ;
    }
}