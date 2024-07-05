package de.tub.web;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Scanner;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WebAPICall {

    private static final String OPENWEATHERMAP_API_KEY = "api-key";
    private static final String SPOTIFY_CLIENT_ID = "api-key";
    private static final String SPOTIFY_CLIENT_SECRET = "api-key";


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Geben Sie Ihre Stadt ein: ");
        String city = scanner.nextLine();

        String weather = getWeather(city);
        if (weather != null) {
            System.out.println("Aktuelles Wetter in " + city + ": " + weather + ", Temperatur: " + getTemperature(city));
            String playlists = getPlaylists(weather);
            System.out.println("Empfohlene Playlists:\n" + playlists);
        } else {
            System.out.println("Wetterinformationen konnten nicht abgerufen werden.");
        }
    }

    private static String getWeather(String city) {
        String urlString = String.format("http://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s", city, OPENWEATHERMAP_API_KEY);
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
                JsonArray weatherArray = jsonObject.getAsJsonArray("weather");
                return weatherArray.get(0).getAsJsonObject().get("description").getAsString();
            } else {
                System.out.println("Response code: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getTemperature(String city) {
        String urlString = String.format("http://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric", city, OPENWEATHERMAP_API_KEY);
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
                JsonObject main = jsonObject.getAsJsonObject("main");
                return main.get("temp").getAsString();
            } else {
                System.out.println("Response code: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getPlaylists(String weather) {
        String token = getSpotifyAccessToken();

        if (token == null) {
            return "Zugangstoken konnte nicht abgerufen werden.";
        }

        String query;
        if (weather.contains("rain")) {
            query = "rainy day";
        } else if (weather.contains("clear")) {
            query = "sunny day";
        } else {
            query = "chill";
        }

        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String urlString = String.format("https://api.spotify.com/v1/search?q=%s&type=playlist&limit=10", encodedQuery);
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
                JsonArray playlistsArray = jsonObject.getAsJsonObject("playlists").getAsJsonArray("items");

                StringBuilder playlists = new StringBuilder();
                for (int i = 0; i < playlistsArray.size(); i++) {
                    JsonObject playlist = playlistsArray.get(i).getAsJsonObject();
                    String name = playlist.get("name").getAsString();
                    String urlSpotify = playlist.getAsJsonObject("external_urls").get("spotify").getAsString();
                    playlists.append(name).append(" - ").append(urlSpotify).append("\n");
                }

                return playlists.toString();
            } else {
                System.out.println("Response code: " + responseCode);
                return "Playlists konnten nicht abgerufen werden.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Playlists konnten nicht abgerufen werden.";
        }
    }

    private static String getSpotifyAccessToken() {
        String urlString = "https://accounts.spotify.com/api/token";
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            String auth = SPOTIFY_CLIENT_ID + ":" + SPOTIFY_CLIENT_SECRET;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

            conn.setDoOutput(true);
            String postData = "grant_type=client_credentials";
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = postData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
                return jsonObject.get("access_token").getAsString();
            } else {
                System.out.println("Response code: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
