package tn.dhc.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nom;
    private String prenom;
    private String mail;
    private int tel;
    private String mdp;
    private String role;
    private String specialite;
    @Column(name = "login_count", nullable = false, columnDefinition = "int DEFAULT 0")
    private int loginCount = 0;  // ← initialisation Java aussi

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    public User() {
    }

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

    @PrePersist
    private void prePersist() {
        if (loginCount <= 0) loginCount = 0;
        if (lastLoginAt == null) lastLoginAt = LocalDateTime.now();
    }
    public int getLoginCount() { return loginCount; }
    public void setLoginCount(int loginCount) { this.loginCount = loginCount; }

    @Override
    public String toString() {
        return "👤 " + nom + " " + prenom +
                " | 📧 " + mail +
                " | 📞 " + tel +
                " | 🎭 " + role;
    }
}