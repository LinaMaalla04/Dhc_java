package tn.dhc.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import tn.dhc.entities.ResetPasswordRequest;
import tn.dhc.entities.User;
import tn.dhc.utils.HibernateUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Properties;

/**
 * Gère le cycle complet "mot de passe oublié" :
 *  1. initiate()  → génère un token, l'envoie par mail
 *  2. validate()  → vérifie selector + token → retourne l'userId
 *  3. resetPassword() → hash + sauvegarde le nouveau mot de passe
 */
public class PasswordResetService {

    private static final int CODE_TTL_MINUTES = 10;

    private final SecureRandom secureRandom = new SecureRandom();

    // ── 1. INITIATE ──
    public void initiate(String email) {

        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();

        try {
            User user = findUserByEmail(em, email);

            if (user == null) {
                throw new IllegalArgumentException("Aucun compte associé à cet email.");
            }

            deleteExistingCodes(em, user.getId());

            // 🔥 GENERATE OTP CODE
            String code = String.valueOf(100000 + secureRandom.nextInt(900000));

            ResetPasswordRequest req = new ResetPasswordRequest(
                    code,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusMinutes(CODE_TTL_MINUTES),
                    user.getId()
            );

            em.getTransaction().begin();
            em.persist(req);
            em.getTransaction().commit();

            sendEmail(user, code);

        } finally {
            em.close();
        }
    }

    // ── 2. VALIDATE ──
    public int validate(String email, String code) {

        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();

        try {
            ResetPasswordRequest req = findByEmail(em, email);

            if (req == null) {
                throw new IllegalArgumentException("Code invalide.");
            }

            if (req.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Code expiré.");
            }

            if (!req.getCode().equals(code)) {
                throw new IllegalArgumentException("Code incorrect.");
            }

            return req.getUserId();

        } finally {
            em.close();
        }
    }

    // ── 3. RESET PASSWORD ──
    public void resetPassword(String email, String code, String newPassword) {

        int userId = validate(email, code);

        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();

        try {
            User user = em.find(User.class, userId);

            em.getTransaction().begin();

            user.setMdp(hashPassword(newPassword));

            em.merge(user);

            deleteExistingCodes(em, userId);

            em.getTransaction().commit();

        } finally {
            em.close();
        }
    }

    // ── EMAIL ──
    private void sendEmail(User user, String code) {
        System.out.println("Code reset envoyé: " + code);
        // ou JavaMail comme tu fais déjà
    }

    // ── DB ──
    private User findUserByEmail(EntityManager em, String email) {
        try {
            return em.createQuery(
                            "SELECT u FROM User u WHERE u.mail = :email", User.class)
                    .setParameter("email", email.trim().toLowerCase())
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private ResetPasswordRequest findByEmail(EntityManager em, String email) {
        try {
            return em.createQuery(
                            "SELECT r FROM ResetPasswordRequest r WHERE r.userId = " +
                                    "(SELECT u.id FROM User u WHERE u.mail = :email)",
                            ResetPasswordRequest.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private void deleteExistingCodes(EntityManager em, int userId) {
        em.getTransaction().begin();

        em.createQuery("DELETE FROM ResetPasswordRequest r WHERE r.userId = :uid")
                .setParameter("uid", userId)
                .executeUpdate();

        em.getTransaction().commit();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(
                    md.digest(password.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
