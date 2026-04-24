package tn.dhc.entities;

public class Lieu {

    private int id;
    private String nomLieu;
    private String adresse;
    private String ville;
    private Integer capaciteMax; // nullable
    private boolean disponible;

<<<<<<< HEAD
    public Lieu() {
    }

=======
    // 🔹 Constructeur vide
    public Lieu() {
    }

    // 🔹 Constructeur sans id
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
    public Lieu(String nomLieu, String adresse, String ville,
                Integer capaciteMax, boolean disponible) {
        this.nomLieu = nomLieu;
        this.adresse = adresse;
        this.ville = ville;
        this.capaciteMax = capaciteMax;
        this.disponible = disponible;
    }

<<<<<<< HEAD
=======
    // 🔹 Constructeur avec id
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
    public Lieu(int id, String nomLieu, String adresse, String ville,
                Integer capaciteMax, boolean disponible) {
        this.id = id;
        this.nomLieu = nomLieu;
        this.adresse = adresse;
        this.ville = ville;
        this.capaciteMax = capaciteMax;
        this.disponible = disponible;
    }

<<<<<<< HEAD
=======
    // 🔹 Getters & Setters
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomLieu() {
        return nomLieu;
    }

    public void setNomLieu(String nomLieu) {
        this.nomLieu = nomLieu;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public Integer getCapaciteMax() {
        return capaciteMax;
    }

    public void setCapaciteMax(Integer capaciteMax) {
        this.capaciteMax = capaciteMax;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

<<<<<<< HEAD
    @Override
    public String toString() {
        return "📍 Nom: " + nomLieu + " | " +
                "🏠 Adresse: " + adresse + " | " +
                "🌆 Ville: " + ville + " | " +
                "👥 Capacité: " + (capaciteMax != null ? capaciteMax : "Non définie") + " | " +
                "✅ Disponible: " + (disponible ? "Oui" : "Non");
=======
    // 🔹 toString
    @Override
    public String toString() {
        return "Lieu{" +
                "id=" + id +
                ", nomLieu='" + nomLieu + '\'' +
                ", adresse='" + adresse + '\'' +
                ", ville='" + ville + '\'' +
                ", capaciteMax=" + capaciteMax +
                ", disponible=" + disponible +
                '}';
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
    }
}