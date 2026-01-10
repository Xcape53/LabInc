package com.labinc.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Globalne ustawienia gry
 */
public class GameSettings {
    private static final String SETTINGS_FILE = "labinc_settings.properties";
    private static GameSettings instance;
    
    // Ustawienia FPS (-1 = bez limitu)
    private int fogFps = 60;          // FPS dla mgły (10-300 lub -1)
    private int animationFps = 60;    // FPS dla animacji hover (10-300 lub -1)
    private int factoryFps = 60;      // FPS dla animacji fabryk (10-300 lub -1)
    
    // Ustawienia dźwięku
    private boolean soundEnabled = true;
    private boolean musicEnabled = true;
    private int soundVolume = 100;    // 0-100
    private int musicVolume = 50;     // 0-100 (domyślnie 50%)
    
    // Ustawienia grafiki
    private boolean cloudsEnabled = true;
    private int cloudDensity = 100;   // 10-100 (procent)
    
    private GameSettings() {
        load();
    }
    
    public static GameSettings getInstance() {
        if (instance == null) {
            instance = new GameSettings();
        }
        return instance;
    }
    
    // Gettery i settery
    public int getFogFps() { return fogFps; }
    public void setFogFps(int fps) { 
        if (fps == -1) this.fogFps = -1;
        else this.fogFps = Math.max(10, Math.min(300, fps)); 
    }
    
    public int getAnimationFps() { return animationFps; }
    public void setAnimationFps(int fps) { 
        if (fps == -1) this.animationFps = -1;
        else this.animationFps = Math.max(10, Math.min(300, fps)); 
    }
    
    public int getFactoryFps() { return factoryFps; }
    public void setFactoryFps(int fps) { 
        if (fps == -1) this.factoryFps = -1;
        else this.factoryFps = Math.max(10, Math.min(300, fps)); 
    }
    
    public boolean isSoundEnabled() { return soundEnabled; }
    public void setSoundEnabled(boolean enabled) { this.soundEnabled = enabled; }
    
    public boolean isMusicEnabled() { return musicEnabled; }
    public void setMusicEnabled(boolean enabled) { this.musicEnabled = enabled; }

    public int getSoundVolume() { return soundVolume; }
    public void setSoundVolume(int volume) { this.soundVolume = Math.max(0, Math.min(100, volume)); }

    public int getMusicVolume() { return musicVolume; }
    public void setMusicVolume(int volume) { this.musicVolume = Math.max(0, Math.min(100, volume)); }
    
    public boolean isCloudsEnabled() { return cloudsEnabled; }
    public void setCloudsEnabled(boolean enabled) { this.cloudsEnabled = enabled; }
    
    public int getCloudDensity() { return cloudDensity; }
    public void setCloudDensity(int density) { this.cloudDensity = Math.max(10, Math.min(100, density)); }
    
    // Konwersja FPS na interwał timera (ms)
    public int getFogTimerInterval() { return fogFps == -1 ? 1 : 1000 / fogFps; }
    public int getAnimationTimerInterval() { return animationFps == -1 ? 1 : 1000 / animationFps; }
    public int getFactoryTimerInterval() { return factoryFps == -1 ? 1 : 1000 / factoryFps; }
    
    public void save() {
        Properties props = new Properties();
        props.setProperty("fogFps", String.valueOf(fogFps));
        props.setProperty("animationFps", String.valueOf(animationFps));
        props.setProperty("factoryFps", String.valueOf(factoryFps));
        props.setProperty("soundEnabled", String.valueOf(soundEnabled));
        props.setProperty("musicEnabled", String.valueOf(musicEnabled));
        props.setProperty("soundVolume", String.valueOf(soundVolume));
        props.setProperty("musicVolume", String.valueOf(musicVolume));
        props.setProperty("cloudsEnabled", String.valueOf(cloudsEnabled));
        props.setProperty("cloudDensity", String.valueOf(cloudDensity));
        
        try (FileOutputStream out = new FileOutputStream(SETTINGS_FILE)) {
            props.store(out, "LabInc Game Settings");
        } catch (IOException e) {
            System.err.println("Nie można zapisać ustawień: " + e.getMessage());
        }
    }
    
    public void load() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(SETTINGS_FILE)) {
            props.load(in);
            fogFps = Integer.parseInt(props.getProperty("fogFps", "60"));
            animationFps = Integer.parseInt(props.getProperty("animationFps", "60"));
            factoryFps = Integer.parseInt(props.getProperty("factoryFps", "60"));
            soundEnabled = Boolean.parseBoolean(props.getProperty("soundEnabled", "true"));
            musicEnabled = Boolean.parseBoolean(props.getProperty("musicEnabled", "true"));
            soundVolume = Integer.parseInt(props.getProperty("soundVolume", "100"));
            musicVolume = Integer.parseInt(props.getProperty("musicVolume", "50"));
            cloudsEnabled = Boolean.parseBoolean(props.getProperty("cloudsEnabled", "true"));
            cloudDensity = Integer.parseInt(props.getProperty("cloudDensity", "100"));
        } catch (IOException | NumberFormatException e) {
            // Użyj domyślnych wartości
        }
    }
}
