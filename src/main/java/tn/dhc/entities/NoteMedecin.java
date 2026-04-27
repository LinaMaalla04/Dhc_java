package tn.dhc.entities;

import java.time.LocalDateTime;

public class NoteMedecin {

    private int           id;
    private int           medecinId;
    private int           patientId;
    private int           note;          // 1–5
    private String        commentaire;
    private LocalDateTime createdAt;

    public NoteMedecin() {}

    public NoteMedecin(int medecinId, int patientId, int note, String commentaire) {
        this.medecinId   = medecinId;
        this.patientId   = patientId;
        this.note        = note;
        this.commentaire = commentaire;
    }

    // Getters & Setters
    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public int getMedecinId()                   { return medecinId; }
    public void setMedecinId(int medecinId)     { this.medecinId = medecinId; }

    public int getPatientId()                   { return patientId; }
    public void setPatientId(int patientId)     { this.patientId = patientId; }

    public int getNote()                        { return note; }
    public void setNote(int note)               { this.note = note; }

    public String getCommentaire()              { return commentaire; }
    public void setCommentaire(String c)        { this.commentaire = c; }

    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime t)   { this.createdAt = t; }
}
