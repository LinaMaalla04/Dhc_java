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
<<<<<<< HEAD
    private Integer ficheId;
    private Creneau creneau;


    public Rdv() {
    }

    public Rdv(String motif, String priorite, String statut,
               LocalDate dateRdv, int creneauId, int userId) {
        this(motif, priorite, statut, dateRdv, creneauId, userId, null);
    }

    public Rdv(String motif, String priorite, String statut,
               LocalDate dateRdv, int creneauId, int userId, Integer ficheId) {
=======

    // 🔹 Constructeur vide
    public Rdv() {
    }

    // 🔹 Constructeur sans id
    public Rdv(String motif, String priorite, String statut,
               LocalDate dateRdv, int creneauId, int userId) {
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
        this.motif = motif;
        this.priorite = priorite;
        this.statut = statut;
        this.dateRdv = dateRdv;
        this.creneauId = creneauId;
        this.userId = userId;
<<<<<<< HEAD
        this.ficheId = ficheId;
    }

    public Rdv(int id, String motif, String priorite, String statut,
               LocalDate dateRdv, int creneauId, int userId) {
        this(id, motif, priorite, statut, dateRdv, creneauId, userId, null);
    }

    public Rdv(int id, String motif, String priorite, String statut,
               LocalDate dateRdv, int creneauId, int userId, Integer ficheId) {
=======
    }

    // 🔹 Constructeur avec id
    public Rdv(int id, String motif, String priorite, String statut,
               LocalDate dateRdv, int creneauId, int userId) {
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
        this.id = id;
        this.motif = motif;
        this.priorite = priorite;
        this.statut = statut;
        this.dateRdv = dateRdv;
        this.creneauId = creneauId;
        this.userId = userId;
<<<<<<< HEAD
        this.ficheId = ficheId;
    }

=======
    }

    // 🔹 Getters & Setters
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064

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
<<<<<<< HEAD
    public Integer getFicheId() {
        return ficheId;
    }

    public void setFicheId(Integer ficheId) {
        this.ficheId = ficheId;
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
=======

    // 🔹 toString
    @Override
    public String toString() {
        return "Rdv{" +
                "id=" + id +
                ", motif='" + motif + '\'' +
                ", priorite='" + priorite + '\'' +
                ", statut='" + statut + '\'' +
                ", dateRdv=" + dateRdv +
                ", creneauId=" + creneauId +
                ", userId=" + userId +
                '}';
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
    }
}