package tn.dhc.controllers;

import javafx.collections.FXCollections;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import tn.dhc.entities.User;
import tn.dhc.utils.UserEngagementHeuristics;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Graphiques d'engagement (segments, récence, moyennes par rôle, scatter) — partagés entre le dashboard et la page dédiée.
 */
public final class UserEngagementCharts {

    private UserEngagementCharts() {
    }

    public static void populate(
            List<User> users,
            PieChart userEngagementPie,
            BarChart<String, Number> userRecencyBarChart,
            LineChart<String, Number> userAvgLoginsLineChart,
            ScatterChart<Number, Number> userLoginScatterChart) {
        if (userEngagementPie == null) {
            return;
        }
        List<User> data = users != null ? users : List.of();

        Map<String, Integer> segments = new LinkedHashMap<>();
        segments.put("Très actif", 0);
        segments.put("Actif", 0);
        segments.put("Modéré", 0);
        segments.put("À risque", 0);
        segments.put("Inactif / churn probable", 0);
        for (User u : data) {
            String seg = UserEngagementHeuristics.segment(u);
            segments.put(seg, segments.getOrDefault(seg, 0) + 1);
        }
        userEngagementPie.setData(FXCollections.observableArrayList(
                segments.entrySet().stream()
                        .filter(e -> e.getValue() > 0)
                        .map(e -> new PieChart.Data(e.getKey(), e.getValue()))
                        .toList()
        ));

        if (userRecencyBarChart != null) {
            userRecencyBarChart.getData().clear();
            Map<String, Integer> buckets = new LinkedHashMap<>();
            buckets.put("Sans date", 0);
            buckets.put("0-7 j", 0);
            buckets.put("8-30 j", 0);
            buckets.put("31-60 j", 0);
            buckets.put("61+ j", 0);
            for (User u : data) {
                String b = UserEngagementHeuristics.recencyBucket(u);
                buckets.put(b, buckets.getOrDefault(b, 0) + 1);
            }
            XYChart.Series<String, Number> barSeries = new XYChart.Series<>();
            barSeries.setName("Utilisateurs");
            for (Map.Entry<String, Integer> e : buckets.entrySet()) {
                barSeries.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
            }
            userRecencyBarChart.getData().add(barSeries);
        }

        if (userAvgLoginsLineChart != null) {
            userAvgLoginsLineChart.getData().clear();
            Map<String, long[]> sumCount = new LinkedHashMap<>();
            for (User u : data) {
                String k = normalizeRoleKey(u.getRole());
                long[] sc = sumCount.computeIfAbsent(k, x -> new long[2]);
                sc[0] += u.getLoginCount();
                sc[1] += 1;
            }
            XYChart.Series<String, Number> lineSeries = new XYChart.Series<>();
            lineSeries.setName("Moyenne login_count");
            List<String> roleOrder = List.of("Admin", "Médecin", "Patient", "Autre");
            for (String rk : roleOrder) {
                long[] sc = sumCount.get(rk);
                if (sc != null && sc[1] > 0) {
                    double avg = (double) sc[0] / sc[1];
                    lineSeries.getData().add(new XYChart.Data<>(rk, avg));
                }
            }
            userAvgLoginsLineChart.getData().add(lineSeries);
        }

        if (userLoginScatterChart != null) {
            userLoginScatterChart.getData().clear();
            XYChart.Series<Number, Number> scat = new XYChart.Series<>();
            scat.setName("Utilisateurs");
            for (User u : data) {
                double x;
                if (u.getLastLoginAt() == null) {
                    x = 120;
                } else {
                    x = Math.min(120, ChronoUnit.DAYS.between(u.getLastLoginAt().toLocalDate(), LocalDate.now()));
                }
                scat.getData().add(new XYChart.Data<>(x, u.getLoginCount()));
            }
            userLoginScatterChart.getData().add(scat);
        }
    }

    private static String normalizeRoleKey(String role) {
        if (role == null || role.isBlank()) {
            return "Autre";
        }
        String r = role.toLowerCase(Locale.ROOT);
        if (r.contains("admin")) {
            return "Admin";
        }
        if (r.contains("medecin") || r.contains("médecin")) {
            return "Médecin";
        }
        if (r.contains("patient")) {
            return "Patient";
        }
        return "Autre";
    }
}
