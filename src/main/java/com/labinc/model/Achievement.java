package com.labinc.model;

import java.io.Serializable;
import java.util.function.Predicate;

/**
 * Klasa reprezentująca osiągnięcie
 */
public class Achievement implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String id;
    private final String name;
    private final String description;
    private final String icon;
    private transient Predicate<GameState> condition;
    private boolean unlocked;
    private long unlockedTime;
    
    public Achievement(String id, String name, String description, String icon,
                      Predicate<GameState> condition) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.condition = condition;
        this.unlocked = false;
        this.unlockedTime = 0;
    }
    
    /**
     * Sprawdza czy osiągnięcie zostało właśnie odblokowane
     */
    public boolean check(GameState state) {
        if (!unlocked && condition != null && condition.test(state)) {
            unlocked = true;
            unlockedTime = System.currentTimeMillis();
            return true;  // Nowo odblokowane!
        }
        return false;
    }
    
    public void forceUnlock() {
        if (!unlocked) {
            unlocked = true;
            unlockedTime = System.currentTimeMillis();
        }
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public boolean isUnlocked() {
        return unlocked;
    }
    
    public long getUnlockedTime() {
        return unlockedTime;
    }
    
    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
    
    public void setCondition(Predicate<GameState> condition) {
        this.condition = condition;
    }
}
