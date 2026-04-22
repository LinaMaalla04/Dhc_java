package tn.dhc.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "reset_password_request")
public class ResetPasswordRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Code OTP (ex: 483921)
    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "user_id", nullable = false)
    private int userId;

    public ResetPasswordRequest() {}

    public ResetPasswordRequest(String code,
                                LocalDateTime requestedAt,
                                LocalDateTime expiresAt,
                                int userId) {
        this.code = code;
        this.requestedAt = requestedAt;
        this.expiresAt = expiresAt;
        this.userId = userId;
    }

    // getters & setters

    public int getId() { return id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}