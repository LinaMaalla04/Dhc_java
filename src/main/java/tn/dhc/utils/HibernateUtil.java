package tn.dhc.utils;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class HibernateUtil {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("dhcPU");

    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public static void shutdown() {
        emf.close();
    }
}