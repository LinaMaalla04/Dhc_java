package tn.dhc.entities;

import java.sql.Date;
import java.sql.Timestamp;

public class Ordonnance {
    private int id;
    private String posologie;
    private String frequence;
    private int dureeTraitement;
    private Date date;
    private int ficheId;
    private Integer medecinUserId;
    private String signatureEnvelopeId;
    private String signatureCeremonyUrl;
    private String signatureDeliverableUrl;
    private String signatureStatus;
    private Timestamp signatureEmailSentAt;

    public Ordonnance() {
    }

    public Ordonnance(String posologie, String frequence, int dureeTraitement, Date date, int ficheId) {
        this.posologie = posologie;
        this.frequence = frequence;
        this.dureeTraitement = dureeTraitement;
        this.date = date;
        this.ficheId = ficheId;
    }

    public Ordonnance(int id, String posologie, String frequence, int dureeTraitement, Date date, int ficheId) {
        this.id = id;
        this.posologie = posologie;
        this.frequence = frequence;
        this.dureeTraitement = dureeTraitement;
        this.date = date;
        this.ficheId = ficheId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPosologie() {
        return posologie;
    }

    public void setPosologie(String posologie) {
        this.posologie = posologie;
    }

    public String getFrequence() {
        return frequence;
    }

    public void setFrequence(String frequence) {
        this.frequence = frequence;
    }

    public int getDureeTraitement() {
        return dureeTraitement;
    }

    public void setDureeTraitement(int dureeTraitement) {
        this.dureeTraitement = dureeTraitement;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getFicheId() {
        return ficheId;
    }

    public void setFicheId(int ficheId) {
        this.ficheId = ficheId;
    }

    public Integer getMedecinUserId() {
        return medecinUserId;
    }

    public void setMedecinUserId(Integer medecinUserId) {
        this.medecinUserId = medecinUserId;
    }

    public String getSignatureEnvelopeId() {
        return signatureEnvelopeId;
    }

    public void setSignatureEnvelopeId(String signatureEnvelopeId) {
        this.signatureEnvelopeId = signatureEnvelopeId;
    }

    public String getSignatureCeremonyUrl() {
        return signatureCeremonyUrl;
    }

    public void setSignatureCeremonyUrl(String signatureCeremonyUrl) {
        this.signatureCeremonyUrl = signatureCeremonyUrl;
    }

    public String getSignatureDeliverableUrl() {
        return signatureDeliverableUrl;
    }

    public void setSignatureDeliverableUrl(String signatureDeliverableUrl) {
        this.signatureDeliverableUrl = signatureDeliverableUrl;
    }

    public String getSignatureStatus() {
        return signatureStatus;
    }

    public void setSignatureStatus(String signatureStatus) {
        this.signatureStatus = signatureStatus;
    }

    public Timestamp getSignatureEmailSentAt() {
        return signatureEmailSentAt;
    }

    public void setSignatureEmailSentAt(Timestamp signatureEmailSentAt) {
        this.signatureEmailSentAt = signatureEmailSentAt;
    }

    @Override
    public String toString() {
        return "Ordonnance{" +
                "id=" + id +
                ", posologie='" + posologie + '\'' +
                ", frequence='" + frequence + '\'' +
                ", dureeTraitement=" + dureeTraitement +
                ", date=" + date +
                ", ficheId=" + ficheId +
                ", medecinUserId=" + medecinUserId +
                ", signatureEnvelopeId='" + signatureEnvelopeId + '\'' +
                ", signatureStatus='" + signatureStatus + '\'' +
                ", signatureEmailSentAt=" + signatureEmailSentAt +
                '}';
    }
}
