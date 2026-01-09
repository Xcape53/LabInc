package com.labinc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import com.labinc.gui.AchievementsPanel;
import com.labinc.gui.FactoryPanel;
import com.labinc.gui.InfoPanel;
import com.labinc.gui.MarketPanel;
import com.labinc.gui.ModernMiningPanel;
import com.labinc.gui.SettingsPanel;
import com.labinc.gui.SidebarPanel;
import com.labinc.model.AchievementManager;
import com.labinc.model.GameState;
import com.labinc.model.SaveGame;
import com.labinc.util.SoundManager;
import com.labinc.util.WindowsThemeUtil;

/**
 * Główna klasa gry LabInc
 */
public class LabIncGame extends JFrame {
    private final GameState gameState;
    private final AchievementManager achievementManager;
    private final SoundManager soundManager;

    private Timer gameTimer;
    private long playTime = 0; // Czas gry w sekundach
    private final long startTime;
    private int prestigeLevel = 0;
    private double prestigeMultiplier = 1.0;

    // Autosave settings
    private int autoSaveInterval = 5; // 0 = OFF, 1 = 1 min, 5 = 5 min (domyślnie 5)
    private long lastAutoSaveTime = 0;
    private final String currentSaveFileName;

    private ModernMiningPanel modernMiningPanel;
    private FactoryPanel factoryPanel;
    private MarketPanel marketPanel;
    private AchievementsPanel achievementsPanel;
    private SettingsPanel settingsPanel;
    private InfoPanel infoPanel;
    private JPanel contentPanel;
    private SidebarPanel sidebarPanel;
    private com.labinc.gui.HoverOverlayPanel hoverOverlay;

    public LabIncGame(SaveGame loadedSave, String loadedFileName) {
        super("LabInc - Chemical Tycoon Game");

        gameState = new GameState();
        achievementManager = new AchievementManager();
        soundManager = SoundManager.getInstance();
        startTime = System.currentTimeMillis();
        lastAutoSaveTime = startTime;

        setupUI();
        setupAchievements();
        setupFactoryUpgradeListener();

        if (loadedSave != null) {
            this.currentSaveFileName = loadedFileName;
            loadGameFromSave(loadedSave);
        } else {
            // Generuj unikalną nazwę pliku dla nowej gry
            String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new java.util.Date());
            this.currentSaveFileName = "labinc_save_" + timestamp + ".dat";
        }

        startGameLoop();
    }

    private void setupAchievements() {
        achievementManager.addListener(achievement -> {
            soundManager.playAchievement();
            SwingUtilities.invokeLater(() -> {
                if (hoverOverlay != null) {
                    hoverOverlay.showNotification(
                            "Osiągnięcie Odblokowane!",
                            achievement.getName());
                }
            });
        });
    }

    private void setupFactoryUpgradeListener() {
        gameState.addListener(new GameState.GameEventListener() {
            @Override
            public void onFactoryUpgraded(String factoryName, int newTier) {
                // Gdy fabryka jest odblokowana po raz pierwszy (tier 1)
                if (newTier == 1) {
                    // Opóźnienie 500ms przed zmianą panelu
                    javax.swing.Timer delayBeforeSwitch = new javax.swing.Timer(500, e1 -> {
                        ((javax.swing.Timer) e1.getSource()).stop();
                        SwingUtilities.invokeLater(() -> {
                            showPanel("informacje");
                            sidebarPanel.selectButton("informacje");

                            // Opóźnienie 500ms po zmianie panelu przed scrollowaniem
                            javax.swing.Timer delayAfterSwitch = new javax.swing.Timer(500, e2 -> {
                                ((javax.swing.Timer) e2.getSource()).stop();
                                infoPanel.scrollToFactory(factoryName);
                            });
                            delayAfterSwitch.setRepeats(false);
                            delayAfterSwitch.start();

                            // Pokaż powiadomienie flash
                            if (hoverOverlay != null) {
                                hoverOverlay.showNotification("Nowa Kopalnia!", factoryName);
                            }
                        });
                    });
                    delayBeforeSwitch.setRepeats(false);
                    delayBeforeSwitch.start();
                }
            }
        });
    }

    private void setupUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 900);
        setMinimumSize(new Dimension(1280, 720));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Start zmaksymalizowany

        // Ustaw ciemny pasek tytułowy Windows
        WindowsThemeUtil.setDarkTitleBar(this);

        // Layout główny
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(new Color(35, 35, 35));

        // Inicjalizuj panele (przed tworzeniem top panelu żeby mieć dostęp do labelów)
        modernMiningPanel = new ModernMiningPanel(gameState);
        factoryPanel = new FactoryPanel(gameState);
        marketPanel = new MarketPanel(gameState);
        achievementsPanel = new AchievementsPanel(achievementManager);
        settingsPanel = new SettingsPanel();
        infoPanel = new InfoPanel(gameState);

        // Globalny top panel na całej szerokości
        JPanel globalTopPanel = modernMiningPanel.createGlobalTopPanel();
        add(globalTopPanel, BorderLayout.NORTH);

        // Panel dolny zawierający sidebar i content
        JPanel lowerPanel = new JPanel(new BorderLayout(0, 0));
        lowerPanel.setBackground(new Color(35, 35, 35));

        // Lewy sidebar z menu
        sidebarPanel = new SidebarPanel(
                e -> showPanel("wydobycie"),
                e -> showPanel("fabryki"),
                e -> showPanel("rynek"),
                e -> showPanel("informacje"),
                e -> showPanel("osiagniecia"),
                e -> openSettings(), // Ustawienia
                e -> saveGame(), // Zapis
                e -> showHelp() // Pomoc
        );
        lowerPanel.add(sidebarPanel, BorderLayout.WEST);

        // Główny panel zawartości (wrapper z content i dolnym panelem)
        JPanel contentWrapper = new JPanel(new BorderLayout(0, 0));
        contentWrapper.setBackground(new Color(35, 35, 35));

        contentPanel = new JPanel(new BorderLayout(0, 0));
        contentPanel.setBackground(new Color(35, 35, 35));

        // Domyślnie pokaż wydobycie
        contentPanel.add(modernMiningPanel, BorderLayout.CENTER);

        contentWrapper.add(contentPanel, BorderLayout.CENTER);

        // Globalny dolny panel (dźwięk, auto, wersja - tylko pod content, nie pod
        // sidebarem)
        JPanel globalBottomPanel = modernMiningPanel.createGlobalBottomPanel(
                () -> {
                    // Sound Toggle Action - managed internally by VolumeControlPanel now
                },
                (interval) -> {
                    // Autosave Change Action
                    this.autoSaveInterval = interval;
                    this.lastAutoSaveTime = System.currentTimeMillis(); // Reset timer on change
                });
        contentWrapper.add(globalBottomPanel, BorderLayout.SOUTH);

        lowerPanel.add(contentWrapper, BorderLayout.CENTER);

        add(lowerPanel, BorderLayout.CENTER);

        // Ustaw HoverOverlayPanel jako GlassPane (overlay nad wszystkim)
        hoverOverlay = new com.labinc.gui.HoverOverlayPanel(gameState);
        setGlassPane(hoverOverlay);
        hoverOverlay.setVisible(true);

        // Przekaż hoverOverlay do MarketPanel
        marketPanel.setHoverOverlay(hoverOverlay, this);

        // Przekaż hoverOverlay do SettingsPanel
        settingsPanel.setHoverOverlay(hoverOverlay);
    }

    private void showPanel(String panelName) {
        // Ukryj panel autosell przy zmianie zakładki
        if (hoverOverlay != null) {
            hoverOverlay.forceHideAutoSellInput();
        }

        contentPanel.removeAll();

        switch (panelName) {
            case "wydobycie":
                contentPanel.add(modernMiningPanel, BorderLayout.CENTER);
                break;
            case "fabryki":
                contentPanel.add(factoryPanel, BorderLayout.CENTER);
                break;
            case "rynek":
                contentPanel.add(marketPanel, BorderLayout.CENTER);
                break;
            case "osiagniecia":
                achievementsPanel.updateDisplay();
                contentPanel.add(achievementsPanel, BorderLayout.CENTER);
                break;
            case "informacje":
                infoPanel.updateDisplay();
                contentPanel.add(infoPanel, BorderLayout.CENTER);
                break;
            case "ustawienia":
                settingsPanel.refresh();
                contentPanel.add(settingsPanel, BorderLayout.CENTER);
                break;
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void startGameLoop() {
        // Timer działający co 100ms (10 ticków na sekundę)
        gameTimer = new Timer(100, e -> {
            // Produkuj surowce (0.1 sekundy)
            gameState.produceResources(0.2 * prestigeMultiplier); // 2x szybkość

            // Aktualizuj wyświetlacz
            updateDisplay();

            // Sprawdź osiągnięcia co sekundę
            long currentTime = System.currentTimeMillis();
            if (currentTime % 1000 < 100) {
                achievementManager.checkAchievements(gameState);
            }

            // Auto-save check
            if (autoSaveInterval > 0) {
                if (currentTime - lastAutoSaveTime >= autoSaveInterval * 60 * 1000) {
                    saveGame(true); // Silent save
                    lastAutoSaveTime = currentTime;
                }
            }
        });
        gameTimer.start();
    }

    private void updateDisplay() {
        // Aktualizuj ModernMiningPanel
        if (modernMiningPanel != null) {
            modernMiningPanel.updateDisplay();
        }
    }

    private void saveGame() {
        saveGame(false);
    }

    private void saveGame(boolean silent) {
        playTime = (System.currentTimeMillis() - startTime) / 1000;
        SaveGame save = SaveGame.createFromGameState(gameState, playTime, prestigeLevel, prestigeMultiplier,
                achievementManager.getUnlockedIds());

        if (SaveGame.save(save, currentSaveFileName)) {
            if (!silent && hoverOverlay != null) {
                // Wyciągnij tylko nazwę pliku (bez ścieżki)
                String fileName = new java.io.File(currentSaveFileName).getName();
                // Szare powiadomienie u góry
                hoverOverlay.showNotification("Gra zapisana!", fileName,
                        new java.awt.Color(60, 65, 70), new java.awt.Color(80, 85, 90));
            }
        } else {
            if (!silent && hoverOverlay != null) {
                String fileName = new java.io.File(currentSaveFileName).getName();
                // Czerwone powiadomienie o błędzie
                hoverOverlay.showNotification("Błąd zapisu!", fileName,
                        new java.awt.Color(180, 40, 40), new java.awt.Color(120, 30, 30));
            }
        }
    }

    private void loadGameFromSave(SaveGame save) {
        if (save != null) {
            save.applyToGameState(gameState);
            playTime = save.playTime;
            prestigeLevel = save.prestigeLevel;
            prestigeMultiplier = save.prestigeMultiplier;

            // Przywróć osiągnięcia (bez powiadomień)
            achievementManager.restoreUnlockedAchievements(save.unlockedAchievements);

            // Synchronizuj ustawienia auto-sell w GUI
            if (marketPanel != null) {
                marketPanel.syncAutoSellFromResources();
            }

            // Cicha synchronizacja osiągnięć na podstawie wczytanego stanu (dla starych
            // zapisów)
            achievementManager.checkAchievementsSilent(gameState);
        }
    }

    private void openSettings() {
        showPanel("ustawienia");
    }

    private void showHelp() {
        String helpText = "=== LABINC - POMOC ===\n\n" +
                "CEL GRY:\n" +
                "Zarządzaj kopalniami i fabrykami, wydobywaj\n" +
                "pierwiastki chemiczne i sprzedawaj je za zysk!\n\n" +
                "WYDOBYCIE:\n" +
                "• Kliknij 'Ulepsz' aby zwiększyć poziom kopalni\n" +
                "• Wyższy poziom = szybsze wydobycie surowców\n" +
                "• Odblokuj kolejne kopalnie osiągając wymagane poziomy\n\n" +
                "RYNEK:\n" +
                "• Najedź na pierwiastek aby zobaczyć szczegóły\n" +
                "• Kliknij aby sprzedać surowce\n" +
                "• Auto-Sell: ustaw automatyczną sprzedaż przy progu\n" +
                "• Kolory: zielony=mało, czerwony=dużo wartości\n\n" +
                "INFORMACJE:\n" +
                "• Przeglądaj szczegóły odblokowanych kopalni\n" +
                "• Dowiedz się jakie pierwiastki produkują\n\n" +
                "AUTO-ZAPIS:\n" +
                "Domyślnie co 5 minut (można zmienić w dolnym panelu)";

        JOptionPane.showMessageDialog(this, helpText, "Pomoc",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        // Użyj System Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new com.labinc.gui.MainMenuFrame().setVisible(true);
        });
    }
}
