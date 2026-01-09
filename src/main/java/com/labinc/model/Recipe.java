package com.labinc.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Reprezentuje recepturę chemiczną (reakcję)
 */
public class Recipe {
    private final String id;
    private final String name;
    private final Map<String, Double> inputResources; // Symbol -> Ilość wymagana
    private final String outputResource;              // Symbol produktu
    private final double outputAmount;                // Ilość produktu
    private final double productionTime;              // Czas bazowy w sekundach
    private boolean unlocked;
    private int level; // Poziom budynku (zamiast assignedReactors)
    
    public Recipe(String id, String name, String outputResource, double outputAmount, double productionTime) {
        this.id = id;
        this.name = name;
        this.outputResource = outputResource;
        this.outputAmount = outputAmount;
        this.productionTime = productionTime;
        this.inputResources = new HashMap<>();
        this.unlocked = false;
        this.level = 0;
    }
    
    public void addInput(String resourceSymbol, double amount) {
        inputResources.put(resourceSymbol, amount);
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public Map<String, Double> getInputs() { return inputResources; }
    public String getOutputResource() { return outputResource; }
    public double getOutputAmount() { return outputAmount; }
    public double getProductionTime() { return productionTime; }
    
    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
    
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    
    public double getUpgradeCost() {
        // Koszt bazowy 1000, rośnie o 50% co poziom
        return 1000 * Math.pow(1.5, level);
    }
    
    public double getProductionSpeed() {
        // Poziom 0 = brak produkcji. Poziom 1 = 1x, Poziom 2 = 2x itd.
        // Można zmienić na wykładnicze, ale liniowe jest bezpieczniejsze na start
        return (double) level / productionTime;
    }
}
