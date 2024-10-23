package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Main {

    private static final String API_KEY = "200b3919-3b33-4f59-9fd9-1d52199690db";
    private static final String LATITUDE = "55.735";
    private static final String LONGITUDE = "37.6426";
    public static final int LIMIT = 5;

    public static void main(String[] args) {
        try {
            // Выполнение запроса и получение данных
            String response = getWeatherData();

            if (response != null) {
                // Обработка JSON-ответа
                JSONObject jsonResponse = new JSONObject(response);

                if (!jsonResponse.has("message") || !jsonResponse.getString("message").equals("forbidden")) {
                    // Вывод всех данных от сервера в формате JSON
                    System.out.println("Все данные от сервера:");
                    System.out.println(response);
                    // System.out.println(jsonResponse.toString(4));    // Вывод JSON данных в удобочитаемом виде

                    // Вывод текущей температуры
                    getCurrentTemperature(jsonResponse);

                    // Вычисление и вывод средней температуры
                    getAverageTemperature(jsonResponse);

                } else {
                    System.err.println("Доступ запрещен: " + response);
                }

            } else {
                System.err.println("Не удалось получить данные о погоде.");
            }

        } catch (JSONException e) {
            // Исключение при работе с JSON
            System.err.println("Ошибка при обработке JSON: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Исключение при возникновении сторонней ошибки
            System.err.println("Произошла непредвиденная ошибка: " + e.getMessage());
            e.printStackTrace();
        }

    }

    // Метод для выполнения запроса и получения данных от сервера
    private static String getWeatherData() {

        // Создание HTTP клиента
        HttpClient client = HttpClient.newHttpClient();

        // Формирование URL с указанными координатами точки
        String url = String.format("https://api.weather.yandex.ru/v2/forecast?lat=%s&lon=%s", LATITUDE, LONGITUDE);

        try {
            // Формирование GET запроса по заголовку ключа и сформированным URL точки
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("X-Yandex-Weather-Key", API_KEY)
                    .GET().build();

            // Отправка запроса и получение ответа от сервиса (строка)
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();

        } catch (IOException | InterruptedException e) {
            // Исключение при работе с HTTP запросами
            System.err.println("Ошибка при получении данных о погоде (работа с HTTP запросами): " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Метод для вывода текущей температуры
    private static void getCurrentTemperature(JSONObject jsonResponse) {
        try {
            int currentTemperature = jsonResponse.getJSONObject("fact").getInt("temp");
            System.out.println("Текущая температура: " + currentTemperature + "°C");

        } catch (JSONException e) {
            // Исключение при работе с JSON
            System.err.println("Ошибка при получении текущей температуры (работа с JSON): " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Метод для вычисления и вывода средней температуры за несколько дней (LIMIT)
    private static void getAverageTemperature(JSONObject jsonResponse) {
        try {
            JSONArray forecasts = jsonResponse.getJSONArray("forecasts");
            double totalAvgTemp = 0;
            int limit = Math.min(LIMIT, forecasts.length());    // В данных отображается прогноз только на 7 дней

            for (int i = 0; i < limit; i++) {
                double avgTemp = forecasts.getJSONObject(i)
                        .getJSONObject("parts")
                        .getJSONObject("day")
                        .getDouble("temp_avg");
                totalAvgTemp += avgTemp;
            }

            System.out.printf("Средняя температура за %d дней: %.2f°C%n", limit, totalAvgTemp / limit);

        } catch (JSONException e) {
            // Исключение при извлечении средней температуры
            System.err.println("Ошибка при вычислении средней температуры (работа с JSON): " + e.getMessage());
            e.printStackTrace();
        }
    }

}