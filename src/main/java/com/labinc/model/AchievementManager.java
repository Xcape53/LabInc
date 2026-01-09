package com.labinc.model;

import java.util.*;

/**
 * Manager zarządzający osiągnięciami
 */
public class AchievementManager {
    private final List<Achievement> achievements;
    private final List<AchievementListener> listeners;
    
    public interface AchievementListener {
        void onAchievementUnlocked(Achievement achievement);
    }
    
    public AchievementManager() {
        this.achievements = new ArrayList<>();
        this.listeners = new ArrayList<>();
        initializeAchievements();
    }
    
    private void initializeAchievements() {
        // Osiągnięcia finansowe
        achievements.add(new Achievement(
            "FIRST_THOUSAND", "Pierwsze Tysiące", 
            "Uzbieraj $1,000", "$",
            state -> state.getMoney() >= 1000
        ));
        
        achievements.add(new Achievement(
            "MILLIONAIRE", "Milioner", 
            "Uzbieraj $1,000,000", "$$",
            state -> state.getMoney() >= 1_000_000
        ));
        
        achievements.add(new Achievement(
            "BILLIONAIRE", "Miliarder", 
            "Uzbieraj $1,000,000,000", "$$$",
            state -> state.getMoney() >= 1_000_000_000
        ));
        
        achievements.add(new Achievement(
            "TRILLIONAIRE", "Trylioner", 
            "Uzbieraj $1,000,000,000,000", "$$$$",
            state -> state.getMoney() >= 1_000_000_000_000.0
        ));
        
        // Osiągnięcia fabryk
        achievements.add(new Achievement(
            "CHEMIST", "Chemik", 
            "Odblokuj Kriogeniczną Destylację Powietrza", "KDA",
            state -> state.getFactories().get("KDA").getCurrentTier() > 0
        ));
        
        achievements.add(new Achievement(
            "SALT_BUSINESS", "Solny Biznes", 
            "Osiągnij Salina T13", "SAL",
            state -> state.getFactories().get("SAL").getCurrentTier() >= 13
        ));
        
        achievements.add(new Achievement(
            "IRON_MAN", "Żelazny", 
            "Odblokuj Kopalnię Rudy Żelaza", "Fe",
            state -> state.getFactories().get("KRZ").getCurrentTier() > 0
        ));
        
        achievements.add(new Achievement(
            "GOLD_RUSH", "Złota Gorączka", 
            "Wyprodukuj 1000 kg złota", "Au",
            state -> state.getResources().get("Au").getAmount() >= 1000
        ));
        
        achievements.add(new Achievement(
            "PLATINUM", "Platynowy", 
            "Odblokuj Kopalnię Platynowców", "Pt",
            state -> state.getFactories().get("KP").getCurrentTier() > 0
        ));
        
        achievements.add(new Achievement(
            "RARE_EARTH", "Ziemie Rzadkie", 
            "Odblokuj Kopalnię Ziem Rzadkich", "RE",
            state -> state.getFactories().get("KZR").getCurrentTier() > 0
        ));
        
        achievements.add(new Achievement(
            "NUCLEAR", "Nuklearny", 
            "Odblokuj Reaktor Jądrowy", "U",
            state -> state.getFactories().get("RJ").getCurrentTier() > 0
        ));
        
        achievements.add(new Achievement(
            "SYNTHETIC", "Syntetyk", 
            "Wyprodukuj pierwiastek syntetyczny", "Pu",
            state -> state.getResources().get("Pu").getAmount() > 0
        ));
        
        achievements.add(new Achievement(
            "TYCOON", "Magnat", 
            "Odblokuj wszystkie 16 fabryk", "***",
            state -> {
                for (Factory f : state.getFactories().values()) {
                    if (f.getCurrentTier() == 0) return false;
                }
                return true;
            }
        ));
        
        achievements.add(new Achievement(
            "MASTER", "Mistrz", 
            "Osiągnij Reaktor Jądrowy T35", "RJ35",
            state -> state.getFactories().get("RJ").getCurrentTier() >= 35
        ));
        
        // Osiągnięcia produkcji
        achievements.add(new Achievement(
            "COAL_MINER", "Górnik", 
            "Wyprodukuj 1,000,000 kg węgla", "C",
            state -> state.getResources().get("C").getAmount() >= 1_000_000
        ));
        
        achievements.add(new Achievement(
            "SPEEDRUN", "Speedrun", 
            "Osiągnij $1M w pierwszych 10 minutach", "FAST",
            state -> false // To będzie sprawdzane z timerem
        ));
        
        // Osiągnięcia specjalne
        achievements.add(new Achievement(
            "COLLECTOR", "Kolekcjoner", 
            "Posiadaj jednocześnie 50 różnych surowców", "50+",
            state -> {
                int count = 0;
                for (Resource r : state.getResources().values()) {
                    if (r.getAmount() > 1) count++;
                }
                return count >= 50;
            }
        ));
        
        achievements.add(new Achievement(
            "RICH_ELEMENTS", "Bogactwo Pierwiastków", 
            "Posiadaj wszystkie 118 surowców jednocześnie", "118",
            state -> {
                for (Resource r : state.getResources().values()) {
                    if (r.getAmount() <= 0) return false;
                }
                return true;
            }
        ));
    }
    
    /**
     * Sprawdza wszystkie osiągnięcia
     */
    public void checkAchievements(GameState state) {
        for (Achievement achievement : achievements) {
            if (achievement.check(state)) {
                notifyAchievementUnlocked(achievement);
            }
        }
    }
    
    public void restoreUnlockedAchievements(java.util.Set<String> unlockedIds) {
        if (unlockedIds == null) return;
        for (Achievement a : achievements) {
            if (unlockedIds.contains(a.getId())) {
                a.setUnlocked(true);
            }
        }
    }

    /**
     * Sprawdza wszystkie osiągnięcia bez powiadamiania (do wczytywania gry)
     */
    public void checkAchievementsSilent(GameState state) {
        for (Achievement achievement : achievements) {
            achievement.check(state);
        }
    }

    public void addListener(AchievementListener listener) {
        listeners.add(listener);
    }
    
    private void notifyAchievementUnlocked(Achievement achievement) {
        for (AchievementListener listener : listeners) {
            listener.onAchievementUnlocked(achievement);
        }
    }
    
    public List<Achievement> getAchievements() {
        return achievements;
    }
    
    public java.util.Set<String> getUnlockedIds() {
        java.util.Set<String> ids = new java.util.HashSet<>();
        for (Achievement a : achievements) {
            if (a.isUnlocked()) {
                ids.add(a.getId());
            }
        }
        return ids;
    }
    
    public int getUnlockedCount() {
        return (int) achievements.stream().filter(Achievement::isUnlocked).count();
    }
    
    public int getTotalCount() {
        return achievements.size();
    }
    
    public double getCompletionPercentage() {
        return (getUnlockedCount() * 100.0) / getTotalCount();
    }
}
