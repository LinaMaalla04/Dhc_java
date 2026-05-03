package tn.dhc.entities;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Agrégats calculés côté service pour le tableau de bord admin (événements).
 */
public final class EventStatistics {

    private final int totalEvents;
    private final int upcomingCount;
    private final int pastCount;
    private final int todayCount;
    private final long sumParticipants;
    private final double averageParticipants;
    private final Map<String, Long> countByTheme;
    /** Clé {@code YearMonth} ISO (ex. 2025-04), ordre chronologique. */
    private final Map<String, Long> countByMonth;
    private final Map<Integer, Long> countByLieuId;

    public EventStatistics(
            int totalEvents,
            int upcomingCount,
            int pastCount,
            int todayCount,
            long sumParticipants,
            double averageParticipants,
            Map<String, Long> countByTheme,
            Map<String, Long> countByMonth,
            Map<Integer, Long> countByLieuId) {
        this.totalEvents = totalEvents;
        this.upcomingCount = upcomingCount;
        this.pastCount = pastCount;
        this.todayCount = todayCount;
        this.sumParticipants = sumParticipants;
        this.averageParticipants = averageParticipants;
        this.countByTheme = Map.copyOf(countByTheme);
        this.countByMonth = Map.copyOf(countByMonth);
        this.countByLieuId = Map.copyOf(countByLieuId);
    }

    public static EventStatistics fromEvents(List<Event> events) {
        LocalDate today = LocalDate.now();
        int total = events.size();
        int upcoming = 0;
        int past = 0;
        int todayN = 0;
        long sumPart = 0;

        Map<String, Long> byTheme = new LinkedHashMap<>();
        Map<String, Long> byMonth = new LinkedHashMap<>();
        Map<Integer, Long> byLieu = new LinkedHashMap<>();

        YearMonth monthStart = YearMonth.from(today).minusMonths(5);
        YearMonth monthEnd = YearMonth.from(today).plusMonths(6);

        for (Event e : events) {
            LocalDate d = e.getDateEvent();
            if (d != null) {
                if (d.isAfter(today)) {
                    upcoming++;
                } else if (d.isBefore(today)) {
                    past++;
                } else {
                    todayN++;
                }
                YearMonth ym = YearMonth.from(d);
                if (!ym.isBefore(monthStart) && !ym.isAfter(monthEnd)) {
                    String key = ym.toString();
                    byMonth.merge(key, 1L, Long::sum);
                }
            }

            String theme = e.getThemeSante();
            if (theme == null || theme.isBlank()) {
                theme = "(sans thème)";
            } else {
                theme = theme.trim();
            }
            byTheme.merge(theme, 1L, Long::sum);

            int nb = e.getNbParticipant() != null ? e.getNbParticipant() : 0;
            sumPart += nb;

            int lid = e.getEventLieuId();
            if (lid > 0) {
                byLieu.merge(lid, 1L, Long::sum);
            }
        }

        for (YearMonth m = monthStart; !m.isAfter(monthEnd); m = m.plusMonths(1)) {
            byMonth.putIfAbsent(m.toString(), 0L);
        }
        Map<String, Long> monthOrdered = byMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));

        Map<String, Long> themeSorted = byTheme.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));

        double avgPart = total > 0 ? (double) sumPart / total : 0;

        return new EventStatistics(
                total,
                upcoming,
                past,
                todayN,
                sumPart,
                avgPart,
                themeSorted,
                monthOrdered,
                byLieu);
    }

    public int getTotalEvents() {
        return totalEvents;
    }

    public int getUpcomingCount() {
        return upcomingCount;
    }

    public int getPastCount() {
        return pastCount;
    }

    public int getTodayCount() {
        return todayCount;
    }

    public long getSumParticipants() {
        return sumParticipants;
    }

    public double getAverageParticipants() {
        return averageParticipants;
    }

    public Map<String, Long> getCountByTheme() {
        return countByTheme;
    }

    public Map<String, Long> getCountByMonth() {
        return countByMonth;
    }

    public Map<Integer, Long> getCountByLieuId() {
        return countByLieuId;
    }
}
