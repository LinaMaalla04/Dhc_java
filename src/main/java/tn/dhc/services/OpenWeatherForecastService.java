package tn.dhc.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OpenWeatherForecastService {

    private static final String API_KEY = "032deea26ddb1a3e4b931d851d50fade";
    private static final String FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast";
    private static final String OPEN_METEO_GEO = "https://geocoding-api.open-meteo.com/v1/search";
    private static final String OPEN_METEO_FORECAST = "https://api.open-meteo.com/v1/forecast";

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final DateTimeFormatter OWM_DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public record DailyForecast(LocalDate date, String summary, double tempC) {}

    public String getForecastSummary(String city, LocalDate eventDate) {
        try {
            return getForecastSummaryOpenWeather(city, eventDate);
        } catch (Exception e) {
            try {
                return getForecastSummaryOpenMeteo(city, eventDate);
            } catch (Exception ignored) {
                return "🌤 Prévision météo indisponible.";
            }
        }
    }

    public List<DailyForecast> getFiveDayForecast(String city) {
        try {
            return getFiveDayForecastOpenWeather(city);
        } catch (Exception e) {
            try {
                return getFiveDayForecastOpenMeteo(city);
            } catch (Exception ignored) {
                return List.of();
            }
        }
    }

    private String getForecastSummaryOpenWeather(String city, LocalDate eventDate) throws IOException, InterruptedException {
        if (city == null || city.isBlank()) {
            city = "Tunis";
        }
        if (eventDate == null) {
            return "🌤 Prévision météo: date d'événement indisponible.";
        }

        String encodedCity = URLEncoder.encode(city.trim(), StandardCharsets.UTF_8);
        String url = FORECAST_URL + "?q=" + encodedCity + "&appid=" + API_KEY + "&units=metric&lang=fr";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("OpenWeather HTTP " + response.statusCode());
        }

        JsonNode root = mapper.readTree(response.body());
        JsonNode list = root.path("list");
        if (!list.isArray() || list.isEmpty()) {
            throw new IOException("OpenWeather: liste de previsions vide");
        }

        JsonNode best = null;
        long bestDist = Long.MAX_VALUE;
        LocalDateTime target = eventDate.atTime(12, 0);

        for (JsonNode item : list) {
            String dtTxt = item.path("dt_txt").asText("");
            if (dtTxt.isBlank()) {
                continue;
            }
            LocalDateTime point;
            try {
                point = LocalDateTime.parse(dtTxt, OWM_DTF);
            } catch (Exception ignored) {
                continue;
            }
            if (!point.toLocalDate().equals(eventDate)) {
                continue;
            }
            long dist = Math.abs(java.time.Duration.between(target, point).toMinutes());
            if (dist < bestDist) {
                bestDist = dist;
                best = item;
            }
        }

        if (best == null) {
            throw new IOException("OpenWeather: aucune tranche pour cette date");
        }

        String desc = best.path("weather").isArray() && !best.path("weather").isEmpty()
                ? best.path("weather").get(0).path("description").asText("conditions inconnues")
                : "conditions inconnues";
        double temp = best.path("main").path("temp").asDouble(Double.NaN);
        double feels = best.path("main").path("feels_like").asDouble(Double.NaN);
        int humidity = best.path("main").path("humidity").asInt(-1);

        String tempTxt = Double.isNaN(temp) ? "?" : String.format("%.1f", temp);
        String feelsTxt = Double.isNaN(feels) ? "?" : String.format("%.1f", feels);
        String humTxt = humidity < 0 ? "?" : String.valueOf(humidity);
        return "🌤 Prévision météo (" + city + "): " + desc + " · " + tempTxt + "°C (ressenti " + feelsTxt + "°C) · humidité " + humTxt + "%";
    }

    private List<DailyForecast> getFiveDayForecastOpenWeather(String city) throws IOException, InterruptedException {
        if (city == null || city.isBlank()) {
            city = "Tunis";
        }
        String encodedCity = URLEncoder.encode(city.trim(), StandardCharsets.UTF_8);
        String url = FORECAST_URL + "?q=" + encodedCity + "&appid=" + API_KEY + "&units=metric&lang=fr";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("OpenWeather HTTP " + response.statusCode());
        }
        JsonNode root = mapper.readTree(response.body());
        JsonNode list = root.path("list");
        if (!list.isArray() || list.isEmpty()) {
            return List.of();
        }
        Map<LocalDate, DailyForecast> byDay = new LinkedHashMap<>();
        for (JsonNode item : list) {
            String dtTxt = item.path("dt_txt").asText("");
            if (dtTxt.isBlank()) {
                continue;
            }
            LocalDateTime point;
            try {
                point = LocalDateTime.parse(dtTxt, OWM_DTF);
            } catch (Exception ignored) {
                continue;
            }
            LocalDate d = point.toLocalDate();
            String desc = item.path("weather").isArray() && !item.path("weather").isEmpty()
                    ? item.path("weather").get(0).path("description").asText("n/a")
                    : "n/a";
            double temp = item.path("main").path("temp").asDouble(Double.NaN);
            DailyForecast candidate = new DailyForecast(d, desc, temp);
            DailyForecast current = byDay.get(d);
            if (current == null || Math.abs(point.getHour() - 12) < 3) {
                byDay.put(d, candidate);
            }
        }
        return byDay.values().stream()
                .sorted(Comparator.comparing(DailyForecast::date))
                .limit(5)
                .toList();
    }

    private String getForecastSummaryOpenMeteo(String city, LocalDate eventDate) throws IOException, InterruptedException {
        List<DailyForecast> rows = getFiveDayForecastOpenMeteo(city);
        return rows.stream()
                .filter(r -> r.date().equals(eventDate))
                .findFirst()
                .map(r -> "🌤 Prévision météo (" + city + "): " + r.summary() + " · " + String.format("%.1f", r.tempC()) + "°C")
                .orElse("🌤 Prévision météo non disponible pour cette date.");
    }

    private List<DailyForecast> getFiveDayForecastOpenMeteo(String city) throws IOException, InterruptedException {
        if (city == null || city.isBlank()) {
            city = "Tunis";
        }
        String geoUrl = OPEN_METEO_GEO + "?name=" + URLEncoder.encode(city, StandardCharsets.UTF_8) + "&count=1&language=fr";
        HttpResponse<String> geoRes = http.send(HttpRequest.newBuilder().uri(URI.create(geoUrl)).GET().build(), HttpResponse.BodyHandlers.ofString());
        if (geoRes.statusCode() < 200 || geoRes.statusCode() >= 300) {
            throw new IOException("OpenMeteo geo HTTP " + geoRes.statusCode());
        }
        JsonNode geoRoot = mapper.readTree(geoRes.body());
        JsonNode first = geoRoot.path("results").isArray() && !geoRoot.path("results").isEmpty() ? geoRoot.path("results").get(0) : null;
        if (first == null) {
            if (!city.trim().equalsIgnoreCase("Tunis")) {
                return getFiveDayForecastOpenMeteo("Tunis");
            }
            return List.of();
        }
        double lat = first.path("latitude").asDouble();
        double lon = first.path("longitude").asDouble();
        String forecastUrl = OPEN_METEO_FORECAST
                + "?latitude=" + lat
                + "&longitude=" + lon
                + "&daily=weathercode,temperature_2m_max"
                + "&forecast_days=16"
                + "&timezone=auto";
        HttpResponse<String> forecastRes = http.send(HttpRequest.newBuilder().uri(URI.create(forecastUrl)).GET().build(), HttpResponse.BodyHandlers.ofString());
        if (forecastRes.statusCode() < 200 || forecastRes.statusCode() >= 300) {
            throw new IOException("OpenMeteo forecast HTTP " + forecastRes.statusCode());
        }
        JsonNode root = mapper.readTree(forecastRes.body()).path("daily");
        JsonNode dates = root.path("time");
        JsonNode temps = root.path("temperature_2m_max");
        JsonNode codes = root.path("weathercode");
        if (!dates.isArray() || !temps.isArray() || !codes.isArray()) {
            return List.of();
        }
        List<DailyForecast> out = new ArrayList<>();
        int size = Math.min(dates.size(), Math.min(temps.size(), codes.size()));
        for (int i = 0; i < size && out.size() < 5; i++) {
            LocalDate d = LocalDate.parse(dates.get(i).asText());
            double t = temps.get(i).asDouble(Double.NaN);
            int code = codes.get(i).asInt(-1);
            out.add(new DailyForecast(d, weatherCodeToLabel(code), t));
        }
        return out;
    }

    private static String weatherCodeToLabel(int code) {
        return switch (code) {
            case 0 -> "ciel clair";
            case 1, 2, 3 -> "partiellement nuageux";
            case 45, 48 -> "brouillard";
            case 51, 53, 55, 56, 57 -> "bruine";
            case 61, 63, 65, 66, 67 -> "pluie";
            case 71, 73, 75, 77 -> "neige";
            case 80, 81, 82 -> "averses";
            case 95, 96, 99 -> "orage";
            default -> "conditions variables";
        };
    }
}
