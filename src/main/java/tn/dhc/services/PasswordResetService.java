package tn.dhc.services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.mindrot.jbcrypt.BCrypt;
import tn.dhc.entities.ResetPasswordRequest;
import tn.dhc.entities.User;
import tn.dhc.utils.HibernateUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Properties;

    public class PasswordResetService {

        // ── Config mail ── à externaliser dans config.properties en prod
        private static final String MAIL_HOST     = "smtp.gmail.com";
        private static final int    MAIL_PORT     = 587;
        private static final String MAIL_USER     = "linamaalla15@gmail.com";   // <-- changer
        private static final String MAIL_PASSWORD = "omffqhjkvgqllwlw";      // <-- mot de passe d'application Gmail
        private static final String MAIL_FROM     = "Digital Health Care <linamaalla15@gmail.com>";

        private static final int CODE_TTL_MINUTES = 10;

        private final SecureRandom secureRandom = new SecureRandom();

        // ──────────────────────────────────────────────────────────────────────
        // 1. INITIATE — génère un code OTP et l'envoie par email
        // ──────────────────────────────────────────────────────────────────────
        public void initiate(String email) {

            EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();

            try {
                User user = findUserByEmail(em, email);

                if (user == null) {
                    // Ne pas révéler si l'email existe : on lève quand même
                    // mais le controller affiche un message générique
                    throw new IllegalArgumentException("Aucun compte associé à cet email.");
                }

                // FIX : on passe l'EntityManager à deleteExistingCodes
                // pour éviter l'ouverture d'une 2e transaction conflictuelle
                deleteExistingCodes(em, user.getId());

                // Générer le code OTP à 6 chiffres
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

                // Envoi réel par email (plus de System.out.println !)
                sendEmail(user, code);

            } finally {
                em.close();
            }
        }

        // ──────────────────────────────────────────────────────────────────────
        // 2. VALIDATE — vérifie email + code OTP
        // ──────────────────────────────────────────────────────────────────────
        public int validate(String email, String code) {

            EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();

            try {
                ResetPasswordRequest req = findByEmail(em, email);

                if (req == null) {
                    throw new IllegalArgumentException("Code invalide.");
                }
                if (req.getExpiresAt().isBefore(LocalDateTime.now())) {
                    throw new IllegalArgumentException("Code expiré. Faites une nouvelle demande.");
                }
                if (!req.getCode().equals(code.trim())) {
                    throw new IllegalArgumentException("Code incorrect.");
                }

                return req.getUserId();

            } finally {
                em.close();
            }
        }

        // ──────────────────────────────────────────────────────────────────────
        // 3. RESET PASSWORD — change le mot de passe après validation du code
        // ──────────────────────────────────────────────────────────────────────
        public void resetPassword(String email, String code, String newPassword) {

            // Valide d'abord (lève une exception si invalide/expiré)
            int userId = validate(email, code);

            EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();

            try {
                User user = em.find(User.class, userId);
                if (user == null) {
                    throw new IllegalStateException("Utilisateur introuvable.");
                }

                em.getTransaction().begin();
                user.setMdp(hashPassword(newPassword));
                em.merge(user);
                em.getTransaction().commit();

                // Supprimer le code utilisé (après le commit du mdp)
                deleteExistingCodes(em, userId);

            } finally {
                em.close();
            }
        }

        // ──────────────────────────────────────────────────────────────────────
        // EMAIL — envoi réel via JavaMail + Gmail SMTP
        // ──────────────────────────────────────────────────────────────────────
        private void sendEmail(User user, String code) {

            Properties props = new Properties();
            props.put("mail.smtp.auth",            "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host",            MAIL_HOST);
            props.put("mail.smtp.port",            String.valueOf(MAIL_PORT));
            props.put("mail.smtp.ssl.trust",       MAIL_HOST);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(MAIL_USER, MAIL_PASSWORD);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(MAIL_FROM));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(user.getMail()));
                message.setSubject("Votre code de réinitialisation - DHC Platform");
                message.setContent(buildEmailHtml(user.getPrenom(), code),
                        "text/html; charset=UTF-8");

                Transport.send(message);

            } catch (MessagingException e) {
                throw new RuntimeException("Échec de l'envoi du mail.", e);
            }
        }

        private String buildEmailHtml(String prenom, String code) {
            // Affiche le code en gros, bien lisible
            return """
            <!DOCTYPE html>
            <html lang="fr">
            <head><meta charset="UTF-8"></head>
            <body style="font-family: Arial, sans-serif; background:#f4f4f4; padding:20px; margin:0;">
              <div style="max-width:480px; margin:auto; background:#fff;
                          border-radius:10px; padding:36px 32px;
                          box-shadow:0 2px 12px rgba(0,0,0,.08);">

                <h2 style="color:#1a73e8; margin-top:0; font-size:20px;">DHC Platform</h2>

                <p style="color:#333; font-size:15px;">
                  Bonjour <strong>%s</strong>,
                </p>
                <p style="color:#555; font-size:14px; line-height:1.6;">
                  Vous avez demandé la réinitialisation de votre mot de passe.<br>
                  Voici votre code de vérification, valable <strong>10 minutes</strong> :
                </p>

                <!-- Code OTP bien visible -->
                <div style="text-align:center; margin:28px 0;">
                  <span style="display:inline-block; background:#f0f4ff;
                               border:2px dashed #1a73e8; border-radius:10px;
                               padding:16px 36px; font-size:36px; font-weight:bold;
                               letter-spacing:10px; color:#1a237e;">
                    %s
                  </span>
                </div>

                <p style="color:#888; font-size:12px; line-height:1.6; margin-top:24px;">
                  Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.<br>
                  Ce code expirera automatiquement dans 10 minutes.
                </p>
              </div>
            </body>
            </html>
            """.formatted(prenom, code);
        }

        // ──────────────────────────────────────────────────────────────────────
        // UTILITAIRES DB
        // ──────────────────────────────────────────────────────────────────────
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
                        .setParameter("email", email.trim().toLowerCase())
                        .getSingleResult();
            } catch (NoResultException e) {
                return null;
            }
        }

        // FIX : ne commence une transaction que si aucune n'est active
        private void deleteExistingCodes(EntityManager em, int userId) {
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }
            em.createQuery("DELETE FROM ResetPasswordRequest r WHERE r.userId = :uid")
                    .setParameter("uid", userId)
                    .executeUpdate();
            em.getTransaction().commit();
        }

        // ──────────────────────────────────────────────────────────────────────
        // CRYPTO
        // ──────────────────────────────────────────────────────────────────────
        private String hashPassword(String password) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                return BCrypt.hashpw(password, BCrypt.gensalt());
            } catch (Exception e) {
                throw new RuntimeException("Erreur hash", e);
            }
        }
    }