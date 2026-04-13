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

    // 🔹 Constructeur vide
    public Creneau() {
    }

    // 🔹 Constructeur sans id
    public Creneau(LocalDate dateCreneau, LocalTime hdebut, LocalTime hfin,
                   String statut, int userId) {
        this.dateCreneau = dateCreneau;
        this.hdebut = hdebut;
        this.hfin = hfin;
        this.statut = statut;
        this.userId = userId;
    }

    // 🔹 Constructeur avec id
    public Creneau(int id, LocalDate dateCreneau, LocalTime hdebut, LocalTime hfin,
                   String statut, int userId) {
        this.id = id;
        this.dateCreneau = dateCreneau;
        this.hdebut = hdebut;
        this.hfin = hfin;
        this.statut = statut;
        this.userId = userId;
    }

    // 🔹 Getters & Setters

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

    // 🔹 toString
    @Override
    public String toString() {
        return "📅 " + dateCreneau +
                " | 🕐 " + hdebut + " - " + hfin +
                " | 📌 " + statut +
                " | 👤 User ID: " + userId;
    }
}