package com.labinc.model;

import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;

/**
 * Klasa reprezentująca fabrykę/kopalnię
 */
public class Factory {
    private final String shortName; // Skrót (np. "KW", "SN")
    private final String fullName; // Pełna nazwa
    private final int maxTier; // Maksymalny tier
    private int currentTier; // Aktualny tier (0 = nieaktywna)
    private double multiplier; // Aktualny mnożnik produkcji

    // Konfiguracja kosztów
    private final double baseCost; // Koszt bazowy
    private final double costGrowth; // Współczynnik wzrostu kosztu
    private final int baseCostTier; // Tier dla kosztu bazowego

    // Konfiguracja mnożników
    private final double baseMultiplier; // Mnożnik bazowy
    private final double multiplierGrowth; // Współczynnik wzrostu mnożnika

    // Produkcja bazowa: surowiec -> (ilość/s, tier odblokowujący)
    private final Map<String, ProductionData> baseProduction;

    // Warunek odblokowania
    private Function<Map<String, Factory>, Boolean> unlockCondition;
    private String unlockRequirement; // Tekst wymagania np. "Wymaga: KW T3"

    public static class ProductionData {
        public final double amountPerSecond;
        public final int unlockTier;

        public ProductionData(double amountPerSecond, int unlockTier) {
            this.amountPerSecond = amountPerSecond;
            this.unlockTier = unlockTier;
        }
    }

    public Factory(String shortName, String fullName, int maxTier,
            double baseCost, double costGrowth, int baseCostTier,
            double baseMultiplier, double multiplierGrowth,
            Map<String, ProductionData> baseProduction,
            Function<Map<String, Factory>, Boolean> unlockCondition,
            String unlockRequirement) {
        this.shortName = shortName;
        this.fullName = fullName;
        this.maxTier = maxTier;
        this.currentTier = 0;
        this.multiplier = 1.0;

        this.baseCost = baseCost;
        this.costGrowth = costGrowth;
        this.baseCostTier = baseCostTier;

        this.baseMultiplier = baseMultiplier;
        this.multiplierGrowth = multiplierGrowth;

        this.baseProduction = baseProduction;
        this.unlockCondition = unlockCondition;
        this.unlockRequirement = unlockRequirement;
    }

    public boolean isUnlocked(Map<String, Factory> factories) {
        if (unlockCondition == null) {
            return true;
        }
        return unlockCondition.apply(factories);
    }

    public double getUpgradeCost() {
        if (currentTier >= maxTier) {
            return Double.POSITIVE_INFINITY;
        }
        int nextTier = currentTier + 1;
        return baseCost * Math.pow(costGrowth, nextTier - baseCostTier);
    }

    public boolean canAfford(double money) {
        double cost = getUpgradeCost();
        if (Double.isInfinite(cost))
            return false;
        // Prosta logika: jeśli masz wystarczająco pieniędzy (zaokrąglonych w dół),
        // kupujesz
        return Math.floor(money) >= Math.ceil(cost);
    }

    public void upgrade() {
        currentTier++;
        if (currentTier == 1) {
            multiplier = baseMultiplier;
        } else {
            multiplier *= multiplierGrowth;
        }
    }

    public Map<String, Double> getCurrentProduction() {
        Map<String, Double> production = new HashMap<>();
        if (currentTier == 0) {
            return production;
        }

        for (Map.Entry<String, ProductionData> entry : baseProduction.entrySet()) {
            ProductionData data = entry.getValue();
            if (currentTier >= data.unlockTier) {
                production.put(entry.getKey(), data.amountPerSecond * multiplier);
            }
        }

        return production;
    }

    // Getters
    public String getShortName() {
        return shortName;
    }

    public String getFullName() {
        return fullName;
    }

    public int getCurrentTier() {
        return currentTier;
    }

    public int getMaxTier() {
        return maxTier;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setCurrentTier(int tier) {
        this.currentTier = tier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public String getUnlockRequirement() {
        return unlockRequirement;
    }
}
