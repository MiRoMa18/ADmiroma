package org.example.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ClimaService {
    private static final String API_KEY = "7dde4f8c5ecd90757d4febf887562348";
    private static final String CIUDAD = "Alzira,ES";
    private static final String URL_BASE = "http://api.openweathermap.org/data/2.5/weather";

    public String obtenerClima() throws Exception {
        if (API_KEY.equals("7dde4f8c5ecd90757d4febf887562348")) {
            System.out.println("⚠️  API Key de clima no configurada");
            return "No disponible";
        }

        String urlCompleta = String.format(
                "%s?q=%s&appid=%s&lang=es&units=metric",
                URL_BASE, CIUDAD, API_KEY
        );

        System.out.println("🌤️  Consultando clima...");

        URL url = new URL(urlCompleta);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JsonObject json = JsonParser.parseString(response.toString())
                    .getAsJsonObject();

            String descripcion = json.getAsJsonArray("weather")
                    .get(0)
                    .getAsJsonObject()
                    .get("description")
                    .getAsString();

            // Mayúscula primera letra
            descripcion = descripcion.substring(0, 1).toUpperCase() +
                    descripcion.substring(1);

            System.out.println("✅ Clima obtenido: " + descripcion);
            return descripcion;

        } else {
            System.err.println("⚠️  Error HTTP " + responseCode + " al obtener clima");
            return "No disponible";
        }
    }

    public String obtenerClimaConDetalles() throws Exception {
        if (API_KEY.equals("TU_API_KEY_AQUI")) {
            System.out.println("⚠️  API Key de clima no configurada");
            return "No disponible";
        }

        String urlCompleta = String.format(
                "%s?q=%s&appid=%s&lang=es&units=metric",
                URL_BASE, CIUDAD, API_KEY
        );

        URL url = new URL(urlCompleta);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JsonObject json = JsonParser.parseString(response.toString())
                    .getAsJsonObject();

            String descripcion = json.getAsJsonArray("weather")
                    .get(0)
                    .getAsJsonObject()
                    .get("description")
                    .getAsString();

            double temperatura = json.getAsJsonObject("main")
                    .get("temp")
                    .getAsDouble();

            descripcion = descripcion.substring(0, 1).toUpperCase() +
                    descripcion.substring(1);

            // Formato: "Soleado (22°C)"
            String climaCompleto = String.format("%s (%.0f°C)", descripcion, temperatura);

            System.out.println("✅ Clima con detalles obtenido: " + climaCompleto);
            return climaCompleto;

        } else {
            System.err.println("⚠️  Error HTTP " + responseCode + " al obtener clima");
            return "No disponible";
        }
    }
}