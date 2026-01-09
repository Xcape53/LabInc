package com.labinc.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * System zapisywania i wczytywania gry
 */
public class SaveGame implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String SAVE_FILE = "labinc_save.dat";

    public double money;
    public Map<String, Double> resources;
    public Map<String, FactorySaveData> factories;
    public Map<String, RecipeSaveData> recipes; // Fabryki chemiczne
    public Map<String, AutoSellData> autoSellConfigs; // Auto-sell ustawienia
    public java.util.Set<String> unlockedAchievements; // Odblokowane osiągnięcia
    public long playTime; // Czas gry w sekundach
    public long timestamp; // Kiedy zapisano
    public int prestigeLevel;
    public double prestigeMultiplier;

    public static class AutoSellData implements Serializable {
        private static final long serialVersionUID = 1L;
        public boolean enabled;
        public double threshold;

        public AutoSellData(boolean enabled, double threshold) {
            this.enabled = enabled;
            this.threshold = threshold;
        }
    }

    public static class FactorySaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        public int tier;
        public double multiplier;

        public FactorySaveData(int tier, double multiplier) {
            this.tier = tier;
            this.multiplier = multiplier;
        }
    }

    public static class RecipeSaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        public int level;
        public boolean unlocked;

        public RecipeSaveData(int level, boolean unlocked) {
            this.level = level;
            this.unlocked = unlocked;
        }
    }

    public SaveGame() {
        this.resources = new HashMap<>();
        this.factories = new HashMap<>();
        this.recipes = new HashMap<>();
        this.autoSellConfigs = new HashMap<>();
        this.unlockedAchievements = new java.util.HashSet<>();
        this.playTime = 0;
        this.timestamp = System.currentTimeMillis();
        this.prestigeLevel = 0;
        this.prestigeMultiplier = 1.0;
    }

    /**
     * Zapisuje stan gry z GameState
     */
    public static SaveGame createFromGameState(GameState state, long playTime, int prestigeLevel,
            double prestigeMultiplier, java.util.Set<String> unlockedAchievements) {
        SaveGame save = new SaveGame();
        save.money = state.getMoney();
        save.playTime = playTime;
        save.prestigeLevel = prestigeLevel;
        save.prestigeMultiplier = prestigeMultiplier;
        if (unlockedAchievements != null) {
            save.unlockedAchievements = new java.util.HashSet<>(unlockedAchievements);
        }

        // Zapisz surowce
        for (Map.Entry<String, Resource> entry : state.getResources().entrySet()) {
            save.resources.put(entry.getKey(), entry.getValue().getAmount());
        }

        // Zapisz fabryki
        for (Map.Entry<String, Factory> entry : state.getFactories().entrySet()) {
            Factory f = entry.getValue();
            save.factories.put(entry.getKey(),
                    new FactorySaveData(f.getCurrentTier(), f.getMultiplier()));
        }

        // Zapisz ustawienia auto-sell
        for (Map.Entry<String, Resource> entry : state.getResources().entrySet()) {
            Resource r = entry.getValue();
            if (r.isAutoSellEnabled()) {
                save.autoSellConfigs.put(entry.getKey(),
                        new AutoSellData(true, r.getAutoSellThreshold()));
            }
        }

        // Zapisz fabryki chemiczne (recipes)
        for (Map.Entry<String, Recipe> entry : state.getRecipes().entrySet()) {
            Recipe r = entry.getValue();
            save.recipes.put(entry.getKey(),
                    new RecipeSaveData(r.getLevel(), r.isUnlocked()));
        }

        return save;
    }

    /**
     * Stosuje zapisany stan do GameState
     */
    public void applyToGameState(GameState state) {
        // Przywróć pieniądze
        state.setMoney(this.money);

        // Przywróć surowce
        for (Map.Entry<String, Double> entry : this.resources.entrySet()) {
            Resource resource = state.getResources().get(entry.getKey());
            if (resource != null) {
                resource.setAmount(entry.getValue());
            }
        }

        // Przywróć fabryki
        for (Map.Entry<String, FactorySaveData> entry : this.factories.entrySet()) {
            Factory factory = state.getFactories().get(entry.getKey());
            if (factory != null) {
                FactorySaveData data = entry.getValue();
                factory.setCurrentTier(data.tier);
                factory.setMultiplier(data.multiplier);
            }
        }

        // Przywróć ustawienia auto-sell
        if (this.autoSellConfigs != null) {
            for (Map.Entry<String, AutoSellData> entry : this.autoSellConfigs.entrySet()) {
                Resource resource = state.getResources().get(entry.getKey());
                if (resource != null) {
                    AutoSellData data = entry.getValue();
                    resource.setAutoSell(data.enabled, data.threshold);
                }
            }
        }

        // Przywróć fabryki chemiczne (recipes)
        if (this.recipes != null) {
            for (Map.Entry<String, RecipeSaveData> entry : this.recipes.entrySet()) {
                Recipe recipe = state.getRecipes().get(entry.getKey());
                if (recipe != null) {
                    RecipeSaveData data = entry.getValue();
                    recipe.setLevel(data.level);
                    recipe.setUnlocked(data.unlocked);
                }
            }
        }
    }

    /**
     * Zapisuje grę do pliku
     */
    public static boolean save(SaveGame saveGame, String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(filename))) {
            oos.writeObject(saveGame);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Wczytuje grę z pliku
     */
    public static SaveGame load(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(filename))) {
            return (SaveGame) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Szybki zapis do domyślnego pliku
     */
    public static boolean quickSave(SaveGame saveGame) {
        return save(saveGame, SAVE_FILE);
    }

    /**
     * Szybkie wczytanie z domyślnego pliku
     */
    public static SaveGame quickLoad() {
        return load(SAVE_FILE);
    }

    /**
     * Sprawdza czy istnieje zapis
     */
    public static boolean saveExists() {
        return new File(SAVE_FILE).exists();
    }

    /**
     * Usuwa zapis
     */
    public static boolean deleteSave() {
        File file = new File(SAVE_FILE);
        return file.exists() && file.delete();
    }

    /**
     * Zwraca informację o zapisie
     */
    public String getSaveInfo() {
        Date date = new Date(timestamp);
        long hours = playTime / 3600;
        long minutes = (playTime % 3600) / 60;

        return String.format("Pieniądze: $%.2e | Czas gry: %dh %dm | Prestige: x%.1f | Zapisano: %tF %tT",
                money, hours, minutes, prestigeMultiplier, date, date);
    }
}
