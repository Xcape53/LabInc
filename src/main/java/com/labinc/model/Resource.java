package com.labinc.model;

/**
 * Klasa reprezentująca surowiec chemiczny
 */
public class Resource {
    private final String symbol;      // Symbol chemiczny (np. "C", "He")
    private final String name;        // Pełna nazwa
    private double amount;            // Ilość posiadana
    private final double basePrice;   // Cena bazowa za kg
    
    // Stan autosell
    private boolean autoSellEnabled = false;
    private double autoSellThreshold = 1000.0;
    
    public Resource(String symbol, String name, double basePrice) {
        this.symbol = symbol;
        this.name = name;
        this.amount = 0.0;
        this.basePrice = basePrice;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public String getName() {
        return name;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public void addAmount(double delta) {
        this.amount += delta;
    }
    
    public double getBasePrice() {
        return basePrice;
    }
    
    public double getTotalValue() {
        return amount * basePrice;
    }
    
    // --- Auto-sell ---
    public boolean isAutoSellEnabled() {
        return autoSellEnabled;
    }
    
    public void setAutoSellEnabled(boolean enabled) {
        this.autoSellEnabled = enabled;
    }
    
    public double getAutoSellThreshold() {
        return autoSellThreshold;
    }
    
    public void setAutoSellThreshold(double threshold) {
        this.autoSellThreshold = threshold;
    }
    
    public void setAutoSell(boolean enabled, double threshold) {
        this.autoSellEnabled = enabled;
        this.autoSellThreshold = threshold;
    }
}
