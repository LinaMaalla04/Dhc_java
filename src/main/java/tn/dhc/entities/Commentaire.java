package tn.dhc.entities;

import java.time.LocalDateTime;

/**
 * Commentaire - maps to the messenger_messages table.
 * queue_name = "commentaire_annonce_{idAnnonce}"
 * headers    = "user_{idUser}"
 * body       = comment text
 */
public class Commentaire {

    private long id;
    private String body;
    private String headers;
    private String queue_name;
    private LocalDateTime created_at;
    private LocalDateTime available_at;
    private LocalDateTime delivered_at;

    public Commentaire() {}

    public Commentaire(String body, int annonceId, int userId) {
        this.body = body;
        this.queue_name = "commentaire_annonce_" + annonceId;
        this.headers = "user_" + userId;
        this.created_at = LocalDateTime.now();
        this.available_at = LocalDateTime.now();
    }

    // -------- derived helpers --------
    public int getAnnonceId() {
        if (queue_name == null) return -1;
        try { return Integer.parseInt(queue_name.replace("commentaire_annonce_", "")); }
        catch (NumberFormatException e) { return -1; }
    }

    public int getUserId() {
        if (headers == null) return -1;
        try { return Integer.parseInt(headers.replace("user_", "")); }
        catch (NumberFormatException e) { return -1; }
    }

    // -------- getters/setters --------
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getHeaders() { return headers; }
    public void setHeaders(String headers) { this.headers = headers; }

    public String getQueue_name() { return queue_name; }
    public void setQueue_name(String queue_name) { this.queue_name = queue_name; }

    public LocalDateTime getCreated_at() { return created_at; }
    public void setCreated_at(LocalDateTime created_at) { this.created_at = created_at; }

    public LocalDateTime getAvailable_at() { return available_at; }
    public void setAvailable_at(LocalDateTime available_at) { this.available_at = available_at; }

    public LocalDateTime getDelivered_at() { return delivered_at; }
    public void setDelivered_at(LocalDateTime delivered_at) { this.delivered_at = delivered_at; }
}
