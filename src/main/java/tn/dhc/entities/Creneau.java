package tn.dhc.entities;

import java.time.LocalDate;
import java.time.LocalTime;

public class Creneau {

    private int id;
    private LocalDate dateCreneau;
    private LocalTime hdebut;
    private LocalTime hfin;
    private String statut;
    private int userId;

<<<<<<< HEAD
    public Creneau() {
    }

=======
    // 🔹 Constructeur vide
    public Creneau() {
    }

    // 🔹 Constructeur sans id
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
    public Creneau(LocalDate dateCreneau, LocalTime hdebut, LocalTime hfin,
                   String statut, int userId) {
        this.dateCreneau = dateCreneau;
        this.hdebut = hdebut;
        this.hfin = hfin;
        this.statut = statut;
        this.userId = userId;
    }

<<<<<<< HEAD
=======
    // 🔹 Constructeur avec id
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
    public Creneau(int id, LocalDate dateCreneau, LocalTime hdebut, LocalTime hfin,
                   String statut, int userId) {
        this.id = id;
        this.dateCreneau = dateCreneau;
        this.hdebut = hdebut;
        this.hfin = hfin;
        this.statut = statut;
        this.userId = userId;
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

    public LocalDate getDateCreneau() {
        return dateCreneau;
    }

    public void setDateCreneau(LocalDate dateCreneau) {
        this.dateCreneau = dateCreneau;
    }

    public LocalTime getHdebut() {
        return hdebut;
    }

    public void setHdebut(LocalTime hdebut) {
        this.hdebut = hdebut;
    }

    public LocalTime getHfin() {
        return hfin;
    }

    public void setHfin(LocalTime hfin) {
        this.hfin = hfin;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

<<<<<<< HEAD
=======
    // 🔹 toString
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
    @Override
    public String toString() {
        return "📅 " + dateCreneau +
                " | 🕐 " + hdebut + " - " + hfin +
<<<<<<< HEAD
                " | 📌 " + statut;
=======
                " | 📌 " + statut +
                " | 👤 User ID: " + userId;
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
    }
}