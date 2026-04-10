package tn.dhc.entities;

import java.sql.Date;

public class Fiche {
    private int id;
    private double poids;
    private double taille;
    private String grpSanguin;
    private String allergie;
    private String maladieChronique;
    private String tension;
    private double glycemie;
    private Date date;
    private String libelleMaladie;
    private String gravite;
    private String recommandation;
    private int userId;

    public Fiche() {
    }

    public Fiche(double poids, double taille, String grpSanguin, String allergie, String maladieChronique,
                 String tension, double glycemie, Date date, String libelleMaladie, String gravite,
                 String recommandation, int userId) {
        this.poids = poids;
        this.taille = taille;
        this.grpSanguin = grpSanguin;
        this.allergie = allergie;
        this.maladieChronique = maladieChronique;
        this.tension = tension;
        this.glycemie = glycemie;
        this.date = date;
        this.libelleMaladie = libelleMaladie;
        this.gravite = gravite;
        this.recommandation = recommandation;
        this.userId = userId;
    }

    public Fiche(int id, double poids, double taille, String grpSanguin, String allergie, String maladieChronique,
                 String tension, double glycemie, Date date, String libelleMaladie, String gravite,
                 String recommandation, int userId) {
        this.id = id;
        this.poids = poids;
        this.taille = taille;
        this.grpSanguin = grpSanguin;
        this.allergie = allergie;
        this.maladieChronique = maladieChronique;
        this.tension = tension;
        this.glycemie = glycemie;
        this.date = date;
        this.libelleMaladie = libelleMaladie;
        this.gravite = gravite;
        this.recommandation = recommandation;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getPoids() {
        return poids;
    }

    public void setPoids(double poids) {
        this.poids = poids;
    }

    public double getTaille() {
        return taille;
    }

    public void setTaille(double taille) {
        this.taille = taille;
    }

    public String getGrpSanguin() {
        return grpSanguin;
    }

    public void setGrpSanguin(String grpSanguin) {
        this.grpSanguin = grpSanguin;
    }

    public String getAllergie() {
        return allergie;
    }

    public void setAllergie(String allergie) {
        this.allergie = allergie;
    }

    public String getMaladieChronique() {
        return maladieChronique;
    }

    public void setMaladieChronique(String maladieChronique) {
        this.maladieChronique = maladieChronique;
    }

    public String getTension() {
        return tension;
    }

    public void setTension(String tension) {
        this.tension = tension;
    }

    public double getGlycemie() {
        return glycemie;
    }

    public void setGlycemie(double glycemie) {
        this.glycemie = glycemie;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLibelleMaladie() {
        return libelleMaladie;
    }

    public void setLibelleMaladie(String libelleMaladie) {
        this.libelleMaladie = libelleMaladie;
    }

    public String getGravite() {
        return gravite;
    }

    public void setGravite(String gravite) {
        this.gravite = gravite;
    }

    public String getRecommandation() {
        return recommandation;
    }

    public void setRecommandation(String recommandation) {
        this.recommandation = recommandation;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Fiche{" +
                "id=" + id +
                ", poids=" + poids +
                ", taille=" + taille +
                ", grpSanguin='" + grpSanguin + '\'' +
                ", allergie='" + allergie + '\'' +
                ", maladieChronique='" + maladieChronique + '\'' +
                ", tension='" + tension + '\'' +
                ", glycemie=" + glycemie +
                ", date=" + date +
                ", libelleMaladie='" + libelleMaladie + '\'' +
                ", gravite='" + gravite + '\'' +
                ", recommandation='" + recommandation + '\'' +
                ", userId=" + userId +
                '}';
    }
}
