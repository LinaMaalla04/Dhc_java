package tn.dhc.entities;

import java.time.LocalDateTime;
public class User {
    private int id;
    private String nom;
    private String prenom;
    private String mail;
    private int tel;
    private String mdp;
    private String role;
    private String specialite;
    private LocalDateTime lastLoginAt;
    private int loginCount;

    // 🔹 Constructeur vide
    public User() {
    }

    // 🔹 Constructeur sans id
    public User(String nom, String prenom, String mail, int tel, String mdp,
                String role, String specialite, LocalDateTime lastLoginAt, int loginCount) {
        this.nom = nom;
        this.prenom = prenom;
        this.mail = mail;
        this.tel = tel;
        this.mdp = mdp;
        this.role = role;
        this.specialite = specialite;
        this.lastLoginAt = lastLoginAt;
        this.loginCount = loginCount;
    }

    // 🔹 Constructeur avec id
    public User(int id, String nom, String prenom, String mail, int tel, String mdp,
                String role, String specialite, LocalDateTime lastLoginAt, int loginCount) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.mail = mail;
        this.tel = tel;
        this.mdp = mdp;
        this.role = role;
        this.specialite = specialite;
        this.lastLoginAt = lastLoginAt;
        this.loginCount = loginCount;
    }

    // 🔹 Getters & Setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public int getTel() { return tel; }
    public void setTel(int tel) { this.tel = tel; }

    public String getMdp() { return mdp; }
    public void setMdp(String mdp) { this.mdp = mdp; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public int getLoginCount() { return loginCount; }
    public void setLoginCount(int loginCount) { this.loginCount = loginCount; }

    // 🔹 toString
    @Override
    public String toString() {
        return "👤 " + nom + " " + prenom +
                " | 📧 " + mail +
                " | 📞 " + tel +
                " | 🎭 " + role;
    }
}