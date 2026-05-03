package tn.dhc.services;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import tn.dhc.entities.User;

import java.util.Properties;

public class OrdonnanceSignedMailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final String SMTP_FROM = "benmiledisslem@gmail.com";
    private static final String SMTP_APP_PASSWORD = "pncg tllm jgts ilhz";

    public void sendSignedOrdonnance(User patient, byte[] pdfBytes, String fileName) throws MessagingException {
        if (patient == null || patient.getMail() == null || patient.getMail().isBlank()) {
            throw new MessagingException("Patient email is missing.");
        }
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
        props.put("mail.smtp.ssl.trust", SMTP_HOST);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_FROM, SMTP_APP_PASSWORD);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SMTP_FROM));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(patient.getMail().trim()));
        message.setSubject("Votre ordonnance signée est disponible");

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText("Bonjour " + safe(patient.getPrenom()) + ",\n\nVeuillez trouver en pièce jointe votre ordonnance signée.\n\nCordialement,\nDHC", "UTF-8");

        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.setFileName(fileName != null ? fileName : "ordonnance-signee.pdf");
        attachmentPart.setContent(pdfBytes, "application/pdf");

        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(attachmentPart);

        message.setContent(multipart);
        Transport.send(message);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
