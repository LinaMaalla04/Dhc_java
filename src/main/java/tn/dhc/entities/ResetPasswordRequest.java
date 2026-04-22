package tn.dhc.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reset_password_request")
public class ResetPasswordRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Code OTP à 6 chiffres (ex: 483921)
    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "user_id", nullable = false)
    private int userId;

    public ResetPasswordRequest() {}

    // Constructeur utilisé dans PasswordResetService.initiate()
    public ResetPasswordRequest(String code,
                                LocalDateTime requestedAt,
                                LocalDateTime expiresAt,
                                int userId) {
        this.code        = code;
        this.requestedAt = requestedAt;
        this.expiresAt   = expiresAt;
        this.userId      = userId;
    }

    // Getters & Setters

    public int getId()                              { return id; }
    public void setId(int id)                       { this.id = id; }

    public String getCode()                         { return code; }
    public void setCode(String code)                { this.code = code; }

    public LocalDateTime getRequestedAt()           { return requestedAt; }
    public void setRequestedAt(LocalDateTime v)     { this.requestedAt = v; }

    public LocalDateTime getExpiresAt()             { return expiresAt; }
    public void setExpiresAt(LocalDateTime v)       { this.expiresAt = v; }

    public int getUserId()                          { return userId; }
    public void setUserId(int userId)               { this.userId = userId; }
}