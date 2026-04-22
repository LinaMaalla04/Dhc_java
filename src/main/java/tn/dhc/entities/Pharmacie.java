package tn.dhc.entities;

import java.util.Objects;

public class Pharmacie {
    private int id;
    private String nom;
    private String adresse;
    private String telephone;
    private String responsable;
    private String hopital;

    public Pharmacie() {
    }

    public Pharmacie(String nom, String adresse, String telephone, String responsable, String hopital) {
        this.nom = nom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.responsable = responsable;
        this.hopital = hopital;
    }

    public Pharmacie(int id, String nom, String adresse, String telephone, String responsable, String hopital) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.responsable = responsable;
        this.hopital = hopital;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public String getHopital() {
        return hopital;
    }

    public void setHopital(String hopital) {
        this.hopital = hopital;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pharmacie pharmacie)) return false;
        return id == pharmacie.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Pharmacie{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", adresse='" + adresse + '\'' +
                ", telephone='" + telephone + '\'' +
                ", responsable='" + responsable + '\'' +
                ", hopital='" + hopital + '\'' +
                '}';
    }
}
