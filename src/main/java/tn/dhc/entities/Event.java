package tn.dhc.entities;

import java.time.LocalDate;
import java.time.LocalTime;

public class Event {

    private int id;
    private String titreEvent;
    private String themeSante;
    private String description;
    private LocalDate dateEvent;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private Integer nbParticipant;
    private int userId;
    private int eventLieuId;
    private Lieu lieu;



    public Event() {
    }

    public Event(String titreEvent, String themeSante, String description,
                 LocalDate dateEvent, LocalTime heureDebut, LocalTime heureFin,
                 Integer nbParticipant, int userId, int eventLieuId) {
        this.titreEvent = titreEvent;
        this.themeSante = themeSante;
        this.description = description;
        this.dateEvent = dateEvent;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.nbParticipant = nbParticipant;
        this.userId = userId;
        this.eventLieuId = eventLieuId;
    }

    public Event(int id, String titreEvent, String themeSante, String description,
                 LocalDate dateEvent, LocalTime heureDebut, LocalTime heureFin,
                 Integer nbParticipant, int userId, int eventLieuId) {
        this.id = id;
        this.titreEvent = titreEvent;
        this.themeSante = themeSante;
        this.description = description;
        this.dateEvent = dateEvent;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.nbParticipant = nbParticipant;
        this.userId = userId;
        this.eventLieuId = eventLieuId;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitreEvent() {
        return titreEvent;
    }

    public void setTitreEvent(String titreEvent) {
        this.titreEvent = titreEvent;
    }

    public String getThemeSante() {
        return themeSante;
    }

    public void setThemeSante(String themeSante) {
        this.themeSante = themeSante;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDateEvent() {
        return dateEvent;
    }

    public void setDateEvent(LocalDate dateEvent) {
        this.dateEvent = dateEvent;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }

    public Integer getNbParticipant() {
        return nbParticipant;
    }

    public void setNbParticipant(Integer nbParticipant) {
        this.nbParticipant = nbParticipant;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getEventLieuId() {
        return eventLieuId;
    }

    public void setEventLieuId(int eventLieuId) {
        this.eventLieuId = eventLieuId;
    }

    public Lieu getLieu() {
        return lieu;
    }

    public void setLieu(Lieu lieu) {
        this.lieu = lieu;
    }

    @Override
    public String toString() {
        return "📌 Titre: " + titreEvent + " | " +
                "🧠 Thème: " + themeSante + " | " +
                "📝 Description: " + description + " | " +
                "📅 Date: " + dateEvent + " | " +
                "⏰ Horaire: " + heureDebut + " - " + heureFin + " | " +
<<<<<<< HEAD
                "👥 Participants: " + nbParticipant  ;
//                "+ " | " +📍 Lieu ID: " + (lieu != null ? lieu.getId() : eventLieuId);
=======
                "👥 Participants: " + nbParticipant + " | " +
                "👤 Organisateur (ID): " + userId;
>>>>>>> d47e962 (Events CRUD)
    }
}