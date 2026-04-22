package tn.dhc.entities;

import java.time.LocalDate;

public class Annonce {

    private int idAnnonce;
    private String titre_annonce;
    private String description;
    private LocalDate date_publication;
    private String urgence;        // VERT | ORANGE | ROUGE
    private String etat_annonce;   // DISPONIBLE | NON_DISPONIBLE
    private int idU;

    public Annonce() {}

    public Annonce(String titre_annonce, String description, LocalDate date_publication,
                   String urgence, String etat_annonce, int idU) {
        this.titre_annonce = titre_annonce;
        this.description = description;
        this.date_publication = date_publication;
        this.urgence = urgence;
        this.etat_annonce = etat_annonce;
        this.idU = idU;
    }

    public Annonce(int idAnnonce, String titre_annonce, String description, LocalDate date_publication,
                   String urgence, String etat_annonce, int idU) {
        this.idAnnonce = idAnnonce;
        this.titre_annonce = titre_annonce;
        this.description = description;
        this.date_publication = date_publication;
        this.urgence = urgence;
        this.etat_annonce = etat_annonce;
        this.idU = idU;
    }

    public int getIdAnnonce() { return idAnnonce; }
    public void setIdAnnonce(int idAnnonce) { this.idAnnonce = idAnnonce; }

    public String getTitre_annonce() { return titre_annonce; }
    public void setTitre_annonce(String titre_annonce) { this.titre_annonce = titre_annonce; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDate_publication() { return date_publication; }
    public void setDate_publication(LocalDate date_publication) { this.date_publication = date_publication; }

    public String getUrgence() { return urgence; }
    public void setUrgence(String urgence) { this.urgence = urgence; }

    public String getEtat_annonce() { return etat_annonce; }
    public void setEtat_annonce(String etat_annonce) { this.etat_annonce = etat_annonce; }

    public int getIdU() { return idU; }
    public void setIdU(int idU) { this.idU = idU; }

    @Override
    public String toString() {
        return titre_annonce + " | " + urgence + " | " + etat_annonce + " | " + date_publication;
    }
}
