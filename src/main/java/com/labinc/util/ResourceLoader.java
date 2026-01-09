package com.labinc.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

import com.labinc.model.Resource;

/**
 * Loader do wczytywania danych surowców z pliku JSON
 */
public class ResourceLoader {

    /**
     * Wczytuje surowce z pliku resources.json
     * 
     * @return Mapa symbol -> Resource
     */
    public static Map<String, Resource> loadResources() {
        Map<String, Resource> resources = new LinkedHashMap<>();

        try {
            InputStream is = ResourceLoader.class.getResourceAsStream("/resources.json");
            if (is == null) {
                System.err.println("[ResourceLoader] Nie znaleziono resources.json");
                return resources;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            String json = sb.toString();

            // Prosty parser JSON (bez zewnętrznej biblioteki)
            int pos = 0;
            while ((pos = json.indexOf("{", pos)) != -1) {
                int endPos = json.indexOf("}", pos);
                if (endPos == -1)
                    break;

                String obj = json.substring(pos, endPos + 1);

                String symbol = extractString(obj, "symbol");
                String name = extractString(obj, "name");
                double price = extractDouble(obj, "price");

                if (symbol != null && name != null) {
                    resources.put(symbol, new Resource(symbol, name, price));
                }

                pos = endPos + 1;
            }

            System.out.println("[ResourceLoader] Załadowano " + resources.size() + " surowców z JSON");

        } catch (Exception e) {
            System.err.println("[ResourceLoader] Błąd ładowania resources.json: " + e.getMessage());
            e.printStackTrace();
        }

        return resources;
    }

    private static String extractString(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1)
            return null;

        int colonIdx = json.indexOf(":", idx);
        if (colonIdx == -1)
            return null;

        int startQuote = json.indexOf("\"", colonIdx);
        if (startQuote == -1)
            return null;
        startQuote++;

        int endQuote = json.indexOf("\"", startQuote);
        if (endQuote == -1)
            return null;

        return json.substring(startQuote, endQuote);
    }

    private static double extractDouble(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1)
            return 0;

        int colonIdx = json.indexOf(":", idx);
        if (colonIdx == -1)
            return 0;

        // Znajdź początek liczby (po dwukropku, pomijając spacje)
        int numStart = colonIdx + 1;
        while (numStart < json.length() && (json.charAt(numStart) == ' ' || json.charAt(numStart) == '\t')) {
            numStart++;
        }

        // Znajdź koniec liczby (przecinek, nawias lub koniec)
        int numEnd = numStart;
        while (numEnd < json.length()) {
            char c = json.charAt(numEnd);
            if (c == ',' || c == '}' || c == ']')
                break;
            numEnd++;
        }

        String numStr = json.substring(numStart, numEnd).trim();
        try {
            return Double.parseDouble(numStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
