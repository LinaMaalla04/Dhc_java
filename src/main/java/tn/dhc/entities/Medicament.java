package tn.dhc.entities;

import java.sql.Date;
import java.util.Objects;

public class Medicament {
    private int id;
    private String nomMedicament;
    private String categorie;
    private String dosage;
    private String forme;
    private Date dateExpiration;
    private int stock;

    public Medicament() {
    }

    public Medicament(String nomMedicament, String categorie, String dosage, String forme, Date dateExpiration, int stock) {
        this.nomMedicament = nomMedicament;
        this.categorie = categorie;
        this.dosage = dosage;
        this.forme = forme;
        this.dateExpiration = dateExpiration;
        this.stock = stock;
    }

    public Medicament(int id, String nomMedicament, String categorie, String dosage, String forme, Date dateExpiration, int stock) {
        this.id = id;
        this.nomMedicament = nomMedicament;
        this.categorie = categorie;
        this.dosage = dosage;
        this.forme = forme;
        this.dateExpiration = dateExpiration;
        this.stock = stock;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomMedicament() {
        return nomMedicament;
    }

    public void setNomMedicament(String nomMedicament) {
        this.nomMedicament = nomMedicament;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getForme() {
        return forme;
    }

    public void setForme(String forme) {
        this.forme = forme;
    }

    public Date getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(Date dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Medicament that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Medicament{" +
                "id=" + id +
                ", nomMedicament='" + nomMedicament + '\'' +
                ", categorie='" + categorie + '\'' +
                ", dosage='" + dosage + '\'' +
                ", forme='" + forme + '\'' +
                ", dateExpiration=" + dateExpiration +
                ", stock=" + stock +
                '}';
    }
}
