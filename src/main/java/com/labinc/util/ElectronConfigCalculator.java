package com.labinc.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Kalkulator konfiguracji elektronowej pierwiastków.
 * Oblicza rozkład elektronów na powłokach dla danego numeru atomowego.
 */
public class ElectronConfigCalculator {
    
    // Maksymalna liczba powłok (dla pierwiastków do Z=118)
    private static final int MAX_SHELLS = 7;
    
    // Kolejność zapełniania podpowłok wg zasady Aufbau
    // Format: {numer powłoki, typ podpowłoki (0=s, 1=p, 2=d, 3=f), max elektronów}
    private static final int[][] AUFBAU_ORDER = {
        {1, 0, 2},   // 1s - 2
        {2, 0, 2},   // 2s - 2
        {2, 1, 6},   // 2p - 6
        {3, 0, 2},   // 3s - 2
        {3, 1, 6},   // 3p - 6
        {4, 0, 2},   // 4s - 2
        {3, 2, 10},  // 3d - 10
        {4, 1, 6},   // 4p - 6
        {5, 0, 2},   // 5s - 2
        {4, 2, 10},  // 4d - 10
        {5, 1, 6},   // 5p - 6
        {6, 0, 2},   // 6s - 2
        {4, 3, 14},  // 4f - 14
        {5, 2, 10},  // 5d - 10
        {6, 1, 6},   // 6p - 6
        {7, 0, 2},   // 7s - 2
        {5, 3, 14},  // 5f - 14
        {6, 2, 10},  // 6d - 10
        {7, 1, 6},   // 7p - 6
    };
    
    // Mapa symbol -> numer atomowy, ładowana z pliku JSON
    private static final Map<String, Integer> ELEMENT_MAP = new HashMap<>();
    
    static {
        loadElementsFromJson();
    }
    
    private static void loadElementsFromJson() {
        try {
            InputStream is = ElectronConfigCalculator.class.getResourceAsStream("/elements.json");
            if (is == null) {
                System.err.println("[ElectronConfigCalculator] Nie znaleziono elements.json");
                return;
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            
            String json = sb.toString();
            // Proste parsowanie JSON (bez zewnętrznej biblioteki)
            int idx = 0;
            while ((idx = json.indexOf("\"symbol\":", idx)) != -1) {
                // Znajdź symbol
                int symStart = json.indexOf("\"", idx + 9) + 1;
                int symEnd = json.indexOf("\"", symStart);
                String symbol = json.substring(symStart, symEnd);
                
                // Znajdź atomicNumber
                int numIdx = json.indexOf("\"atomicNumber\":", symEnd);
                if (numIdx == -1 || numIdx > json.indexOf("}", symEnd)) {
                    idx = symEnd;
                    continue;
                }
                int numStart = numIdx + 15;
                while (numStart < json.length() && !Character.isDigit(json.charAt(numStart))) numStart++;
                int numEnd = numStart;
                while (numEnd < json.length() && Character.isDigit(json.charAt(numEnd))) numEnd++;
                
                if (numEnd > numStart) {
                    int atomicNumber = Integer.parseInt(json.substring(numStart, numEnd));
                    ELEMENT_MAP.put(symbol.toUpperCase(), atomicNumber);
                }
                
                idx = numEnd;
            }
            System.out.println("[ElectronConfigCalculator] Załadowano " + ELEMENT_MAP.size() + " pierwiastków");
        } catch (Exception e) {
            System.err.println("[ElectronConfigCalculator] Błąd ładowania elements.json: " + e.getMessage());
        }
    }
    
    /**
     * Oblicza liczbę elektronów na każdej powłoce dla danego numeru atomowego.
     */
    public static int[] getElectronShells(int atomicNumber) {
        if (atomicNumber <= 0) {
            return new int[0];
        }
        
        int[] shells = new int[MAX_SHELLS];
        int electronsRemaining = atomicNumber;
        
        for (int[] subshell : AUFBAU_ORDER) {
            if (electronsRemaining <= 0) break;
            
            int shellIndex = subshell[0] - 1;
            int maxElectrons = subshell[2];
            
            int electronsToAdd = Math.min(electronsRemaining, maxElectrons);
            shells[shellIndex] += electronsToAdd;
            electronsRemaining -= electronsToAdd;
        }
        
        int actualShells = 0;
        for (int i = MAX_SHELLS - 1; i >= 0; i--) {
            if (shells[i] > 0) {
                actualShells = i + 1;
                break;
            }
        }
        
        int[] result = new int[actualShells];
        System.arraycopy(shells, 0, result, 0, actualShells);
        return result;
    }
    
    /**
     * Zwraca liczbę elektronów walencyjnych (na ostatniej powłoce).
     */
    public static int getValenceElectrons(int atomicNumber) {
        int[] shells = getElectronShells(atomicNumber);
        if (shells.length == 0) return 0;
        return shells[shells.length - 1];
    }
    
    /**
     * Zwraca maksymalną liczbę elektronów dla danej powłoki wg wzoru 2n².
     */
    public static int getMaxElectronsForShell(int shellNumber) {
        return 2 * shellNumber * shellNumber;
    }
    
    /**
     * Formatuje konfigurację elektronową jako string (np. "2,8,6").
     */
    public static String formatConfiguration(int atomicNumber) {
        int[] shells = getElectronShells(atomicNumber);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < shells.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(shells[i]);
        }
        return sb.toString();
    }
    
    /**
     * Zwraca numer atomowy dla danego symbolu pierwiastka.
     * Dane ładowane z pliku elements.json.
     */
    public static int getAtomicNumber(String symbol) {
        if (symbol == null) return 0;
        Integer num = ELEMENT_MAP.get(symbol.toUpperCase());
        return num != null ? num : 0;
    }
}
