package org.example.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ClimaService {

    private static final String API_KEY = "7dde4f8c5ecd90757d4febf887562348"; // Reemplaza con tu clave
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String CIUDAD = "Alzira,ES";

    public String obtenerClimaActual() {
        try {
            String url = BASE_URL + "?q=" + CIUDAD + "&appid=" + API_KEY + "&units=metric&lang=es";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                String descripcion = json.getAsJsonArray("weather")
                        .get(0).getAsJsonObject()
                        .get("description").getAsString();

                return traducirClima(descripcion);
            } else {
                System.err.println("⚠️ Error al obtener clima: " + response.statusCode());
                return "Desconocido";
            }

        } catch (Exception e) {
            System.err.println("❌ Error al consultar API del clima: " + e.getMessage());
            return "Desconocido";
        }
    }

    private String traducirClima(String descripcion) {
        descripcion = descripcion.toLowerCase();

        if (descripcion.contains("despejado") || descripcion.contains("cielo claro")) {
            return "Despejado";
        } else if (descripcion.contains("nubes") || descripcion.contains("nublado")) {
            return "Nublado";
        } else if (descripcion.contains("lluvia")) {
            return "Lluvia";
        } else if (descripcion.contains("tormenta")) {
            return "Tormenta";
        } else if (descripcion.contains("nieve")) {
            return "Nieve";
        } else if (descripcion.contains("niebla")) {
            return "Niebla";
        } else if (descripcion.contains("viento")) {
            return "Ventoso";
        } else {
            return "Parcialmente nublado";
        }
    }

    public String obtenerClimaConDetalles() {
        try {
            String url = BASE_URL + "?q=" + CIUDAD + "&appid=" + API_KEY + "&units=metric&lang=es";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

                String descripcion = json.getAsJsonArray("weather")
                        .get(0).getAsJsonObject()
                        .get("description").getAsString();

                double temperatura = json.getAsJsonObject("main")
                        .get("temp").getAsDouble();

                String clima = traducirClima(descripcion);
                return clima + " (" + Math.round(temperatura) + "°C)";

            } else {
                return "Desconocido";
            }

        } catch (Exception e) {
            System.err.println("❌ Error al consultar API del clima: " + e.getMessage());
            return "Desconocido";
        }
    }
}