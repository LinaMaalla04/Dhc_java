package tn.dhc.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import java.io.*;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Service Google Calendar — OAuth2 + opérations CRUD.
 *
 * Flux :
 *  1. Premier lancement → ouvre le navigateur pour autoriser l'accès
 *  2. Le token est sauvegardé dans tokens/ → les prochains lancements sont silencieux
 *  3. addEvent() / getEvents() / deleteEvent() sont utilisables partout
 */
public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "DHC Platform";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    // Token sauvegardé localement (ne pas committer dans git !)
    private static final String TOKENS_DIR = "tokens";

    // Scope : lecture + écriture du calendrier
    private static final List<String> SCOPES = Collections.singletonList(
            CalendarScopes.CALENDAR
    );

    // Chemin vers credentials.json dans les resources
    private static final String CREDENTIALS_FILE = "/credentials.json";

    // Singleton
    private static GoogleCalendarService instance;
    private Calendar calendarService;

    private GoogleCalendarService() {}

    public static GoogleCalendarService getInstance() {
        if (instance == null) {
            instance = new GoogleCalendarService();
        }
        return instance;
    }

    // ── Authentification ──────────────────────────────────────────────────

    /**
     * Initialise le service Google Calendar.
     * Ouvre le navigateur la première fois pour demander l'autorisation OAuth.
     */
    public void connect() throws IOException, GeneralSecurityException {
        final NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();

        // Charger credentials.json depuis resources
        InputStream in = GoogleCalendarService.class.getResourceAsStream(CREDENTIALS_FILE);
        if (in == null) {
            throw new FileNotFoundException("credentials.json introuvable dans resources/");
        }
        GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Flow OAuth2
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                transport, JSON_FACTORY, secrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIR)))
                .setAccessType("offline")
                .build();

        // LocalServerReceiver écoute sur localhost:8888 pour recevoir le code OAuth
        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(8888).build();

        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        calendarService = new Calendar.Builder(transport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        System.out.println("[Google Calendar] Connecté ✓");
    }

    /**
     * Vérifie si le service est connecté.
     */
    public boolean isConnected() {
        return calendarService != null;
    }

    // ── Opérations calendrier ─────────────────────────────────────────────

    /**
     * Ajoute un événement DHC dans le Google Calendar de l'utilisateur.
     *
     * @param titre       Titre de l'événement
     * @param description Description
     * @param date        Date de l'événement
     * @param heureDebut  Heure de début (peut être null → événement journée entière)
     * @param heureFin    Heure de fin (peut être null)
     * @return ID Google de l'événement créé (pour pouvoir le supprimer plus tard)
     */
    public String addEvent(String titre, String description,
                           LocalDate date, LocalTime heureDebut, LocalTime heureFin) {
        if (!isConnected()) {
            System.out.println("[Google Calendar] Non connecté.");
            return null;
        }
        try {
            Event event = new Event()
                    .setSummary(titre)
                    .setDescription(description != null ? description : "");

            if (heureDebut != null && heureFin != null) {
                // Événement avec horaire précis
                ZoneId zone = ZoneId.of("Africa/Tunis");
                DateTime start = new DateTime(
                        ZonedDateTime.of(date, heureDebut, zone).toInstant().toEpochMilli());
                DateTime end = new DateTime(
                        ZonedDateTime.of(date, heureFin, zone).toInstant().toEpochMilli());
                event.setStart(new EventDateTime().setDateTime(start).setTimeZone("Africa/Tunis"));
                event.setEnd(new EventDateTime().setDateTime(end).setTimeZone("Africa/Tunis"));
            } else {
                // Événement journée entière
                DateTime day = new DateTime(date.toString()); // "2026-05-10"
                event.setStart(new EventDateTime().setDate(day));
                event.setEnd(new EventDateTime().setDate(day));
            }

            Event created = calendarService.events()
                    .insert("primary", event)
                    .execute();

            System.out.println("[Google Calendar] Événement ajouté : " + created.getId());
            return created.getId();

        } catch (IOException e) {
            System.err.println("[Google Calendar] Erreur ajout : " + e.getMessage());
            return null;
        }
    }

    /**
     * Récupère les événements Google Calendar du mois donné.
     */
    public List<Event> getEventsForMonth(LocalDate monthStart) {
        if (!isConnected()) return List.of();
        try {
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
            ZoneId zone = ZoneId.of("Africa/Tunis");

            DateTime timeMin = new DateTime(
                    ZonedDateTime.of(monthStart.atStartOfDay(), zone).toInstant().toEpochMilli());
            DateTime timeMax = new DateTime(
                    ZonedDateTime.of(monthEnd.atTime(23, 59), zone).toInstant().toEpochMilli());

            Events events = calendarService.events().list("primary")
                    .setTimeMin(timeMin)
                    .setTimeMax(timeMax)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            return events.getItems() != null ? events.getItems() : List.of();

        } catch (IOException e) {
            System.err.println("[Google Calendar] Erreur lecture : " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Supprime un événement Google Calendar par son ID.
     */
    public void deleteEvent(String googleEventId) {
        if (!isConnected() || googleEventId == null) return;
        try {
            calendarService.events().delete("primary", googleEventId).execute();
            System.out.println("[Google Calendar] Événement supprimé : " + googleEventId);
        } catch (IOException e) {
            System.err.println("[Google Calendar] Erreur suppression : " + e.getMessage());
        }
    }
}
