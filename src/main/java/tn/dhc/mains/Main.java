package tn.dhc.mains;

import tn.dhc.utils.MyConnection;

import tn.dhc.services.UserService;

import tn.dhc.services.ServiceFiche;

import tn.dhc.services.ServiceOrdonnance;

import tn.dhc.entities.Lieu;
import tn.dhc.services.LieuService;

import tn.dhc.services.EventService;

import tn.dhc.entities.Creneau;
import tn.dhc.services.CreneauService;

import tn.dhc.entities.Rdv;
import tn.dhc.services.RdvService;

import java.time.LocalDate;
import java.time.LocalTime;

public class Main {
    public static void main(String[] args) {
        MyConnection.getInstance();

        UserService UserService = new UserService();
        ServiceFiche serviceFiche = new ServiceFiche();
        ServiceOrdonnance serviceOrdonnance = new ServiceOrdonnance();
        LieuService LieuService = new LieuService();
        EventService Eventservice = new EventService();
        CreneauService CreneauService = new CreneauService();
        RdvService RdvService = new RdvService();


        // ========== FICHE — décommenter une ligne à la fois pour tester ==========

        // --- Create (ajouter) : user_id doit exister dans la table `user` (ex. 1) ---
        // serviceFiche.ajouter(new Fiche(70, 175, "O+", "aucune", "non", "12/8", 5.2, Date.valueOf("2026-04-06"), "grippe", "Légère", "repos", 1));

        // --- Read liste ---
        // serviceFiche.getAll();

        // --- Read un par id ---
        // serviceFiche.getOneById(6);

        // --- Update : id doit exister ---
        //Fiche fMod = new Fiche(1, 72, 176, "O+", "aucune", "non", "12/8", 5.3, Date.valueOf("2026-04-06"), "grippe", "Légère", "repos + vitamines", 1);
        //serviceFiche.modifier(fMod);

        // --- Delete : id doit exister (attention FK : supprimer d'abord les ordonnances liées à cette fiche) ---
        //Fiche fDel = new Fiche();
        // fDel.setId(6);
         //serviceFiche.supprimer(fDel);

        // ========== ORDONNANCE — décommenter une ligne à la fois pour tester ==========

        // --- Create (ajouter) : fiche_id doit exister dans la table `fiche` ---
        // serviceOrdonnance.ajouter(new Ordonnance("1 cp matin", "1x/jour", 7, Date.valueOf("2026-04-06"), 1));

        // --- Read liste ---
        // serviceOrdonnance.getAll();

        // --- Read un par id ---
        // serviceOrdonnance.getOneById(2);

        // --- Update : id doit exister ---
        // Ordonnance oMod = new Ordonnance(2, "2 cp matin", "2x/jour", 10, Date.valueOf("2026-04-07"), 2);
        // serviceOrdonnance.modifier(oMod);

        // --- Delete : id doit exister ---
        // Ordonnance oDel = new Ordonnance();
        // oDel.setId(99);
        // serviceOrdonnance.supprimer(oDel);

//________________________________________USER_______________________________________________________________

//        // 🔹 1. AJOUT
//        User u1 = new User(
//                "Ali",
//                "Ben Ali",
//                "ali@gmail.com",
//                12345678,
//                "1234",
//                "ADMIN",
//                "IT",
//                LocalDateTime.now(),
//                0
//        );

//        UserService.ajouter(u1);
//        System.out.println("✅ Utilisateur ajouté !");
//
//        // 🔹 2. AFFICHAGE
//        System.out.println("\n📋 Liste des utilisateurs :");
//        for (User u : UserService.getAll()) {
//            System.out.println(u);
//        }
//
//        // 🔹 3. MODIFIER
//        User lastUser = UserService.getAll().get(UserService.getAll().size() - 1);
//        lastUser.setNom("Modifié");
//        UserService.modifier(lastUser);
//        System.out.println("✏️ Utilisateur modifié !");
//
//        // 🔹 4. SUPPRESSION
//        UserService.supprimer(lastUser);
//        System.out.println("🗑️ Utilisateur supprimé !");

//________________________________________EVENT______________________________________________________________
//        // ✅ AJOUT EVENT
//        Event e1 = new Event(
//                "Yoga Day",
//                "Bien-être",
//                "Séance de yoga collective",
//                LocalDate.of(2026, 4, 15),
//                LocalTime.of(10, 0),
//                LocalTime.of(12, 0),
//                30,
//                1,  // user_id (existant dans ta DB)
//                1   // event_lieu_id (existant dans ta DB)
//        );
//
//        Eventservice.ajouter(e1);
//        System.out.println("✅ Event ajouté avec succès !");
//
//        // 📌 AFFICHAGE
//        System.out.println("\n📌 Liste des events :");
//        for (Event e : Eventservice.getAll()) {
//            System.out.println(e);
//        }
//
//        // ❌ SUPPRESSION
//
//        Event eDelete = Eventservice.getOneById(1); // ID à adapter
//        if (eDelete != null) {
//            Eventservice.supprimer(eDelete);
//            System.out.println("❌ Event supprimé !");
//        }
// ________________________________________LIEU______________________________________________________________
//        // ✅ AJOUT LIEU
//        Lieu l1 = new Lieu(
//                "Salle Fitness",
//                "Rue Habib Bourguiba",
//                "Tunis",
//                120,
//                true
//        );
//
//        LieuService.ajouter(l1);
//        System.out.println("✅ Lieu ajouté avec succès !");
//
//        // 📌 AFFICHAGE
//        System.out.println("\n📌 Liste des lieux :");
//
//        for (Lieu l : LieuService.getAll()) {
//            System.out.println(l);
//        }
//
//        // ❌ SUPPRESSION
//        Lieu lDelete = LieuService.getOneById(1); // ID à adapter
//        if (lDelete != null) {
//            LieuService.supprimer(lDelete);
//            System.out.println("❌ Lieu supprimé !");
//        }
//
//        // ✏️ MODIFICATION
//        Lieu lUpdate = LieuService.getOneById(1);
//        if (lUpdate != null) {
//            lUpdate.setNomLieu("Salle Yoga");
//            lUpdate.setCapaciteMax(200);
//
//            LieuService.modifier(lUpdate);
//            System.out.println("✏️ Lieu modifié !");
//        }

// ________________________________________CRENEAU______________________________________________________________
//        // 🔹 1. Ajouter un créneau
//        Creneau c1 = new Creneau(
//                LocalDate.of(2026, 4, 10),
//                LocalTime.of(9, 0),
//                LocalTime.of(10, 0),
//                "DISPONIBLE",
//                1 // user_id existant dans ta DB
//        );
//
//        CreneauService.add(c1);
//
//        // 🔹 2. Ajouter un deuxième créneau
//        Creneau c2 = new Creneau(
//                LocalDate.of(2026, 4, 10),
//                LocalTime.of(10, 0),
//                LocalTime.of(11, 0),
//                "RESERVE",
//                2
//        );
//
//        CreneauService.add(c2);
//
//        // 🔹 3. Afficher tous les créneaux
//        System.out.println("📅 Liste des créneaux :");
//        for (Creneau c : CreneauService.getAll()) {
//            System.out.println(c);
//        }
//
//        // 🔹 4. Supprimer un créneau (ex: id = 1)
//        CreneauService.delete(1);
// ________________________________________RDV______________________________________________________________
//            // 🔹 1. Ajouter un RDV
//                    Rdv r1 = new Rdv(
//                            "Consultation générale",
//                            "HAUTE",
//                            "EN_ATTENTE",
//                            LocalDate.of(2026, 4, 10),
//                            1, // creneau_id (doit exister)
//                            1  // user_id (doit exister)
//                    );
//
//                    RdvService.add(r1);
//
//                    // 🔹 2. Ajouter un deuxième RDV
//                    Rdv r2 = new Rdv(
//                            "Contrôle médical",
//                            "MOYENNE",
//                            "CONFIRME",
//                            LocalDate.of(2026, 4, 11),
//                            2,
//                            1
//                    );
//
//                    RdvService.add(r2);
//
//                    // 🔹 3. Afficher tous les RDV
//                    System.out.println("📋 Liste des RDV :");
//                    for (Rdv r : RdvService.getAll()) {
//                        System.out.println(r);
//                    }
//
//                    // 🔹 4. Supprimer un RDV (ex: id = 1)
//                    // RdvService.delete(1);
    }

}
