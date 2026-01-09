package com.labinc.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.OverlayLayout;
import javax.swing.Timer;

import com.labinc.model.Factory;
import com.labinc.model.GameState;
import com.labinc.util.GameColors;
import com.labinc.util.IconLoader;
import com.labinc.util.UIFactory;

/**
 * Nowoczesny panel wydobycia z grafiką 3D
 */
public class ModernMiningPanel extends JPanel {
    private final GameState gameState;
    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0");
    private final DecimalFormat sciFormat = new DecimalFormat("0.##E0");
    private final DecimalFormat numberFormat = new DecimalFormat("#,##0.##");
    private final Map<String, BufferedImage> factoryImages = new HashMap<>();

    // Referencje do labelów i przycisków do aktualizacji
    private JLabel moneyValueLabel;
    private JLabel incomeValueLabel;
    private JLabel infoLabel;
    private final Map<String, JButton> upgradeButtons = new HashMap<>();
    private final Map<String, JLabel> tierLabels = new HashMap<>();
    private final Map<String, Boolean> factoryUnlockStatus = new HashMap<>();
    private final Map<String, Integer> factoryTierStatus = new HashMap<>();

    private JScrollPane scrollPane;
    private JPanel factoriesContainer; // Panel z kartami
    private BufferedImage bgLandscape; // BG1 - krajobraz (najniżej)
    private BufferedImage bgLandscapeAlt; // BG1.5 - alternatywny krajobraz
    private BufferedImage bgClouds; // BG2 - chmury (najwyżej)
    private final java.util.List<JPanel> cloudsPanels = new java.util.ArrayList<>(); // Panele chmur do parallax
    private int maxWindowHeight = 0; // Maksymalna wysokość okna
    private int cloudsOffsetY = 0; // Przesunięcie chmur (parallax)

    // Kolory motywu
    private static final Color BG_DARK = GameColors.BG_DARK;
    private static final Color ORANGE = GameColors.ORANGE;
    private static final Color TEXT_LIGHT = GameColors.TEXT_LIGHT;
    private static final Color TEXT_DISABLED = new Color(70, 70, 70); // Szary dla wyłączonych przycisków

    public ModernMiningPanel(GameState gameState) {
        this.gameState = gameState;
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_DARK);

        loadFactoryImages();
        loadBackgroundImages();
        initComponents();
    }

    private void loadFactoryImages() {
        String basePath = "C:\\Users\\user\\Documents\\GitHub\\LabIncTest\\assets\\main\\factory\\";
        for (int i = 1; i <= 16; i++) {
            try {
                BufferedImage img = ImageIO.read(new File(basePath + i + ".png"));
                factoryImages.put(String.valueOf(i), img);
            } catch (Exception e) {
                System.err.println("Nie można załadować obrazu fabryki " + i + ": " + e.getMessage());
            }
        }
    }

    private void loadBackgroundImages() {
        String basePath = "C:\\Users\\user\\Documents\\GitHub\\LabIncTest\\assets\\main\\bg\\";
        try {
            bgLandscape = ImageIO.read(new File(basePath + "bg1.png"));
        } catch (Exception e) {
            System.err.println("Nie można załadować bg1.png: " + e.getMessage());
        }
        try {
            bgLandscapeAlt = ImageIO.read(new File(basePath + "bg1.5.png"));
        } catch (Exception e) {
            System.err.println("Nie można załadować bg1.5.png: " + e.getMessage());
        }
        try {
            bgClouds = ImageIO.read(new File(basePath + "bg2.png"));
        } catch (Exception e) {
            System.err.println("Nie można załadować bg2.png: " + e.getMessage());
        }
    }

    private String formatMass(double val) {
        if (val >= 1_000_000_000)
            return sciFormat.format(val / 1000.0) + " t/s";
        if (val >= 1000)
            return numberFormat.format(val / 1000.0) + " t/s";
        return numberFormat.format(val) + " kg/s";
    }

    private String formatCost(double amount) {
        if (amount >= 1e3) {
            int exp = (int) Math.floor(Math.log10(amount));
            double mantissa = amount / Math.pow(10, exp);
            return String.format("%.1f", mantissa).replace('.', ',') + "e" + exp;
        }
        return moneyFormat.format(amount);
    }

    private void initComponents() {
        // Top panel jest teraz globalny - tworzony w LabIncGame

        factoriesContainer = new JPanel();
        factoriesContainer.setLayout(new BoxLayout(factoriesContainer, BoxLayout.X_AXIS));
        factoriesContainer.setBackground(new Color(0, 153, 255)); // Niebieskie tło

        int factoryIndex = 1;
        for (Map.Entry<String, Factory> entry : gameState.getFactories().entrySet()) {
            factoriesContainer.add(createFactoryCard(entry.getKey(), entry.getValue(), factoryIndex));
            factoryIndex++;
        }

        scrollPane = new GradientScrollPane(factoriesContainer);
        scrollPane.setBackground(new Color(0, 153, 255));
        scrollPane.getViewport().setBackground(new Color(0, 153, 255));
        scrollPane.getHorizontalScrollBar().setUnitIncrement(30);

        // Karty wypełniają całą wysokość viewportu
        scrollPane.getViewport().addChangeListener(e -> {
            int viewportHeight = scrollPane.getViewport().getHeight();
            if (viewportHeight > 0) {
                Dimension newSize = new Dimension(factoriesContainer.getPreferredSize().width, viewportHeight);
                factoriesContainer.setPreferredSize(newSize);

                // Aktualizuj wysokość każdej karty
                for (Component comp : factoriesContainer.getComponents()) {
                    if (comp instanceof JPanel) {
                        comp.setPreferredSize(new Dimension(420, viewportHeight));
                        ((JPanel) comp).setMaximumSize(new Dimension(420, viewportHeight));
                        ((JPanel) comp).setMinimumSize(new Dimension(420, viewportHeight));
                    }
                }
                factoriesContainer.revalidate();
            }
        });

        add(scrollPane, BorderLayout.CENTER);

        // Listener do resize - parallax chmur
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int currentHeight = getHeight();
                if (maxWindowHeight == 0) {
                    maxWindowHeight = currentHeight;
                }
                if (currentHeight > maxWindowHeight) {
                    maxWindowHeight = currentHeight;
                }

                // Oblicz przesunięcie chmur (parallax)
                int heightDiff = maxWindowHeight - currentHeight;
                cloudsOffsetY = (int) (heightDiff * 0.8);

                // Odśwież wszystkie panele chmur
                for (JPanel panel : cloudsPanels) {
                    panel.repaint();
                }
            }

            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                // Ustaw maxWindowHeight na start
                if (maxWindowHeight == 0) {
                    maxWindowHeight = getHeight();
                }
            }
        });
        // Dolny panel jest teraz globalny - tworzony w LabIncGame
    }

    /**
     * Tworzy globalny top panel do użycia w głównym oknie gry
     * (rozciąga się na całą szerokość, nad sidebarem)
     */
    public JPanel createGlobalTopPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                UIFactory.enableAntialiasing(g2);

                // Metalowe tło z gradientem
                GradientPaint metalGrad = new GradientPaint(0, 0, new Color(50, 55, 60), 0, getHeight(),
                        new Color(35, 40, 45));
                g2.setPaint(metalGrad);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Dolna krawędź (cień)
                g2.setColor(new Color(25, 25, 25));
                g2.fillRect(0, getHeight() - 3, getWidth(), 3);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 25, 12, 25)); // Mniejszy padding

        // Statystyki na środku - większy odstęp
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 120, 0));
        statsPanel.setOpaque(false);

        // Panel Pieniędzy
        JPanel moneyBox = createStatBox(IconLoader.ICON_MONEY, "PIENIĄDZE", moneyValueLabel = new JLabel("$0.00"),
                new Color(74, 222, 128));
        statsPanel.add(moneyBox);

        // Panel Przychodu
        JPanel incomeBox = createStatBox(IconLoader.ICON_INCOME, "PRZYCHÓD", incomeValueLabel = new JLabel("$0.00/s"),
                new Color(96, 165, 250));
        statsPanel.add(incomeBox);

        panel.add(statsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatBox(String iconName, String title, JLabel valueLabel, Color valueColor) {
        final ImageIcon loadedIcon = IconLoader.loadIcon(iconName, 22);
        final String fallbackText = IconLoader.getFallbackText(iconName);

        JPanel box = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                UIFactory.enableAntialiasing(g2);

                // Tło okienka (ciemniejsze, wgłębione)
                g2.setColor(new Color(30, 32, 35));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Wewnętrzna ramka (efekt wgłębienia)
                g2.setColor(new Color(20, 22, 25));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);

                // Górny blask
                g2.setColor(new Color(60, 65, 70));
                g2.drawLine(5, 2, getWidth() - 5, 2);
            }
        };
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        box.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        box.setPreferredSize(new Dimension(200, 80));

        // Tytuł z ikoną (panel żeby móc wyrównać ikonę i tekst)
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        titlePanel.setOpaque(false);

        if (loadedIcon != null) {
            JLabel iconLabel = new JLabel(loadedIcon);
            titlePanel.add(iconLabel);
        } else {
            JLabel iconLabel = new JLabel(fallbackText);
            iconLabel.setFont(new Font("Arial", Font.BOLD, 16));
            iconLabel.setForeground(valueColor);
            titlePanel.add(iconLabel);
        }

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 13));
        titleLabel.setForeground(new Color(140, 145, 150));
        titlePanel.add(titleLabel);

        titlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Wartość - większa czcionka
        valueLabel.setFont(new Font("Arial", Font.BOLD, 26));
        valueLabel.setForeground(valueColor);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(titlePanel);
        box.add(Box.createVerticalStrut(5));
        box.add(valueLabel);

        return box;
    }

    private JPanel createFactoryCard(String factoryKey, Factory factory, int factoryIndex) {
        boolean unlocked = factory.isUnlocked(gameState.getFactories());
        final int CARD_WIDTH = 420;

        // Główny wrapper - rozciąga się na całą wysokość
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false); // Przezroczyste żeby BG1 było widoczne
        wrapper.setPreferredSize(new Dimension(CARD_WIDTH, 650));
        wrapper.setMaximumSize(new Dimension(CARD_WIDTH, Integer.MAX_VALUE));
        wrapper.setMinimumSize(new Dimension(CARD_WIDTH, 400));

        // Panel treści z BorderLayout - CENTER rozciąga się, SOUTH jest stały
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // === SEKCJA ŚRODKOWA: OBRAZEK (wyrównany do dołu) ===
        JPanel imageSection = new JPanel();
        imageSection.setLayout(new BoxLayout(imageSection, BoxLayout.Y_AXIS));
        imageSection.setOpaque(false);

        // Elastyczny spacer na górze (pcha obrazek na dół)
        imageSection.add(Box.createVerticalGlue());

        // Nazwa fabryki nad zdjęciem (szara, ciemniejsza)
        JLabel factoryNameLabel = new JLabel(factory.getShortName());
        factoryNameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        factoryNameLabel.setForeground(new Color(100, 100, 100));
        factoryNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageSection.add(factoryNameLabel);
        imageSection.add(Box.createVerticalStrut(5));

        BufferedImage factoryImg = factoryImages.get(String.valueOf(factoryIndex));
        if (factoryImg != null) {
            int maxWidth = 380;
            int originalWidth = factoryImg.getWidth();
            int originalHeight = factoryImg.getHeight();
            double scale = (double) maxWidth / originalWidth;
            int scaledHeight = (int) (originalHeight * scale);
            Image scaledImg = factoryImg.getScaledInstance(maxWidth, scaledHeight, Image.SCALE_SMOOTH);
            JLabel imgLabel = new JLabel(new ImageIcon(scaledImg));
            imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            imageSection.add(imgLabel);
        } else {
            JLabel placeholderLabel = new JLabel(unlocked ? "🏭" : "🔒");
            placeholderLabel.setFont(new Font("Arial", Font.PLAIN, 120));
            placeholderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            imageSection.add(placeholderLabel);
        }

        // 10px odstępu pod obrazkiem
        imageSection.add(Box.createVerticalStrut(10));

        contentPanel.add(imageSection, BorderLayout.CENTER);

        // === SEKCJA DOLNA: INFO + PRZYCISK (stała wysokość) ===
        JPanel bottomSection = new JPanel();
        bottomSection.setLayout(new BoxLayout(bottomSection, BoxLayout.Y_AXIS));
        bottomSection.setOpaque(false);

        // -- Info: tylko tier, 5px nad przyciskiem --
        if (unlocked) {
            JLabel tierLabel = new JLabel("tier: " + factory.getCurrentTier() + "/" + factory.getMaxTier());
            tierLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            tierLabel.setForeground(TEXT_LIGHT);
            tierLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            bottomSection.add(tierLabel);
            tierLabels.put(factoryKey, tierLabel);

            bottomSection.add(Box.createVerticalStrut(5)); // 5px nad przyciskiem
        } else {
            // Zablokowana - wymagania
            String requirement = factory.getUnlockRequirement();
            if (requirement != null) {
                JLabel reqLabel = new JLabel("<html><div style='text-align: center;'>" + requirement + "</div></html>");
                reqLabel.setFont(new Font("Arial", Font.BOLD, 16));
                reqLabel.setForeground(new Color(255, 140, 0));
                reqLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                bottomSection.add(reqLabel);
            }
            bottomSection.add(Box.createVerticalStrut(5)); // 5px nad przyciskiem
        }

        // -- Przycisk --
        if (!unlocked) {
            bottomSection.add(Box.createVerticalStrut(50));
        } else if (factory.getCurrentTier() == 0) {
            double cost = factory.getUpgradeCost();
            String costStr = formatCost(cost);

            boolean canAfford = factory.canAfford(gameState.getMoney());
            String btnText = "Aktywuj ($" + costStr + ")";
            JButton activateBtn = createStyledUpgradeButton(btnText,
                    new Color(34, 197, 94), Color.BLACK, unlocked && canAfford);

            activateBtn.addActionListener(e -> {
                com.labinc.util.SoundManager.getInstance().playClick1();
                gameState.upgradeFactory(factoryKey);
            });

            bottomSection.add(activateBtn);
            if (unlocked)
                upgradeButtons.put(factoryKey, activateBtn);

        } else if (factory.getCurrentTier() < factory.getMaxTier()) {
            double cost = factory.getUpgradeCost();
            String costStr = formatCost(cost);

            String btnText = "Ulepsz ($" + costStr + ")";
            JButton upgradeBtn = createStyledUpgradeButton(btnText,
                    ORANGE, Color.BLACK, factory.canAfford(gameState.getMoney()));

            upgradeBtn.addActionListener(e -> {
                com.labinc.util.SoundManager.getInstance().playClick1();
                gameState.upgradeFactory(factoryKey);
            });

            bottomSection.add(upgradeBtn);
            upgradeButtons.put(factoryKey, upgradeBtn);

        } else {
            // MAX TIER - szare tło z zielonym tekstem
            JButton maxBtn = createStyledUpgradeButton("MAX TIER",
                    new Color(70, 75, 80), new Color(34, 197, 94), true); // enabled=true żeby zachować kolory
            maxBtn.setCursor(new Cursor(Cursor.DEFAULT_CURSOR)); // Usuń kursor ręki
            maxBtn.removeMouseListener(maxBtn.getMouseListeners()[0]); // Usuń hover
            bottomSection.add(maxBtn);
        }

        contentPanel.add(bottomSection, BorderLayout.SOUTH);

        // === WARSTWY KARTY ===
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));

        final boolean flipHorizontally = (factoryIndex % 2 == 0); // Co druga fabryka flip (dla chmur)

        // WARSTWA 0: BG1 - krajobraz (wyrównany do dołu)
        // Zamiast odbicia lustrzanego, używamy naprzemiennie bg1 i bg1.5
        final BufferedImage currentBg = (factoryIndex % 2 == 0 && bgLandscapeAlt != null) ? bgLandscapeAlt
                : bgLandscape;

        JPanel bg1Panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (currentBg != null) {
                    int y = getHeight() - currentBg.getHeight();
                    g.drawImage(currentBg, 0, y, null);
                }
            }
        };
        bg1Panel.setOpaque(false);
        bg1Panel.setAlignmentX(0.5f);
        bg1Panel.setAlignmentY(0.5f);

        // WARSTWA 1: Mgła
        FogPanel fog = new FogPanel((factoryIndex - 1) * CARD_WIDTH);
        fog.setAlignmentX(0.5f);
        fog.setAlignmentY(0.5f);

        // WARSTWA 2: Zawartość (fabryka, przyciski)
        contentPanel.setAlignmentX(0.5f);
        contentPanel.setAlignmentY(0.5f);

        // WARSTWA 3: BG2 - chmury (u góry z parallax)
        JPanel bg2Panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgClouds != null) {
                    if (flipHorizontally) {
                        // Flip horyzontalny
                        g.drawImage(bgClouds, bgClouds.getWidth(), -cloudsOffsetY, 0,
                                bgClouds.getHeight() - cloudsOffsetY,
                                0, 0, bgClouds.getWidth(), bgClouds.getHeight(), null);
                    } else {
                        g.drawImage(bgClouds, 0, -cloudsOffsetY, null);
                    }
                }
            }
        };
        bg2Panel.setOpaque(false);
        bg2Panel.setAlignmentX(0.5f);
        bg2Panel.setAlignmentY(0.5f);
        cloudsPanels.add(bg2Panel); // Dodaj do listy do aktualizacji parallax

        // Dodaj warstwy w kolejności (od najniższej do najwyższej)
        layeredPane.add(bg1Panel, Integer.valueOf(0)); // Krajobraz na dole
        layeredPane.add(contentPanel, Integer.valueOf(1)); // Zawartość (fabryki)
        layeredPane.add(bg2Panel, Integer.valueOf(2)); // Chmury
        layeredPane.add(fog, Integer.valueOf(3)); // Mgła na samej górze

        wrapper.add(layeredPane, BorderLayout.CENTER);
        return wrapper;
    }

    /**
     * Tworzy globalny dolny panel do użycia w głównym oknie gry
     * (rozciąga się na całą szerokość, pod wszystkimi zakładkami)
     */
    public JPanel createGlobalBottomPanel(Runnable onSoundToggle,
            java.util.function.Consumer<Integer> onAutosaveChange) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                UIFactory.enableAntialiasing(g2);

                // Metalowe tło - identyczne z dolną sekcją sidebara
                GradientPaint metalGrad = new GradientPaint(0, 0, new Color(50, 55, 60), 0, getHeight(),
                        new Color(35, 40, 45));
                g2.setPaint(metalGrad);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Górna krawędź (cień)
                g2.setColor(new Color(25, 25, 25));
                g2.fillRect(0, 0, getWidth(), 2);

                // Blask pod cieniem
                g2.setColor(new Color(70, 75, 80));
                g2.drawLine(0, 2, getWidth(), 2);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        panel.setPreferredSize(new Dimension(0, 44));

        // Lewa strona - ikony kontroli (wycentrowane w pionie)
        JPanel leftWrapper = new JPanel(new GridBagLayout());
        leftWrapper.setOpaque(false);
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);

        // --- Sound Button & Slider (Custom Component) ---
        leftPanel.add(new VolumeControlPanel());

        // --- Autosave ComboBox ---
        String[] autoOptions = { "OFF", "Co minutę", "Co 5 minut" };
        JComboBox<String> autoBox = UIFactory.createStyledComboBox(UIFactory.ComboBoxStyle.COMPACT, autoOptions);
        autoBox.setSelectedIndex(2); // Domyślnie: Co 5 minut
        autoBox.setToolTipText("Częstotliwość automatycznego zapisu");
        autoBox.addActionListener(e -> {
            com.labinc.util.SoundManager.getInstance().playClick2();
            int idx = autoBox.getSelectedIndex();
            int minutes = 0;
            if (idx == 1)
                minutes = 1;
            if (idx == 2)
                minutes = 5;
            if (onAutosaveChange != null)
                onAutosaveChange.accept(minutes);
        });

        // Labelka dla autosave
        JLabel autoLabel = new JLabel("Auto-zapis:");
        autoLabel.setFont(new Font("Arial", Font.BOLD, 12));
        autoLabel.setForeground(new Color(150, 155, 160));

        leftPanel.add(Box.createHorizontalStrut(10));
        leftPanel.add(autoLabel);
        leftPanel.add(autoBox);

        leftWrapper.add(leftPanel);

        // Prawa strona - info i wersja (wycentrowane w pionie)
        JPanel rightWrapper = new JPanel(new GridBagLayout());
        rightWrapper.setOpaque(false);
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        rightPanel.setOpaque(false);

        // Info label (uproszczone)
        infoLabel = new JLabel("");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(120, 125, 130));

        // Wersja
        JLabel versionLabel = new JLabel("v2.0");
        versionLabel.setFont(new Font("Arial", Font.BOLD, 11));
        versionLabel.setForeground(new Color(100, 105, 110));

        rightPanel.add(infoLabel);
        rightPanel.add(versionLabel);
        rightWrapper.add(rightPanel);

        panel.add(leftWrapper, BorderLayout.WEST);
        panel.add(rightWrapper, BorderLayout.EAST);

        return panel;
    }

    // --- Inner Class for Expanding Volume Control ---
    private class VolumeControlPanel extends JPanel {
        private final JButton iconBtn;
        private final JSlider volumeSlider;
        private final Timer expandTimer;
        private final Timer shrinkTimer;

        // Target widths
        private final int COLLAPSED_WIDTH = 34;
        private final int EXPANDED_WIDTH = 140;
        private int currentWidth = 34;
        private int previousVolume = 50;

        public VolumeControlPanel() {
            setLayout(null); // Absolute layout for animation
            setOpaque(false);
            setPreferredSize(new Dimension(COLLAPSED_WIDTH, 34));

            // Initial volume from settings
            int initVol = com.labinc.model.GameSettings.getInstance().getMusicVolume();
            previousVolume = initVol > 0 ? initVol : 50;

            // 1. Initialize Icon Button (without listener)
            iconBtn = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    UIFactory.enableAntialiasing(g2);

                    // Background
                    if (getModel().isRollover()) {
                        g2.setColor(new Color(60, 65, 70));
                    } else {
                        g2.setColor(new Color(40, 45, 50));
                    }
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

                    // Border
                    g2.setColor(new Color(55, 60, 65));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);

                    // Icon
                    ImageIcon icon = IconLoader.loadIcon(IconLoader.ICON_SOUND_ON, 20);
                    int iconX = (getWidth() - 20) / 2;
                    int iconY = (getHeight() - 20) / 2;
                    if (icon != null) {
                        g2.drawImage(icon.getImage(), iconX, iconY, 20, 20, null);
                    }

                    // Muted cross
                    Boolean muted = (Boolean) getClientProperty("isMuted");
                    if (muted != null && muted) {
                        g2.setColor(new Color(255, 60, 60));
                        g2.setStroke(new BasicStroke(2.5f));
                        g2.drawLine(iconX, iconY, iconX + 20, iconY + 20);
                        g2.drawLine(iconX, iconY + 20, iconX + 20, iconY);
                    }
                }
            };
            iconBtn.setBounds(0, 0, 34, 34);
            iconBtn.setOpaque(false);
            iconBtn.setContentAreaFilled(false);
            iconBtn.setBorderPainted(false);
            iconBtn.setFocusPainted(false);
            iconBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Set initial mute state
            iconBtn.putClientProperty("isMuted", initVol == 0);

            // 2. Initialize Slider (without listener)
            volumeSlider = new JSlider(0, 100, initVol);
            volumeSlider.setOpaque(false);
            volumeSlider.setFocusable(false);
            volumeSlider.setBounds(34, 2, 100, 30);

            // Custom UI for slider to look industrial
            volumeSlider.setUI(new javax.swing.plaf.basic.BasicSliderUI(volumeSlider) {
                @Override
                public void paintThumb(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    UIFactory.enableAntialiasing(g2);
                    Rectangle thumbBounds = thumbRect;
                    g2.setColor(ORANGE);
                    g2.fillOval(thumbBounds.x, thumbBounds.y + thumbBounds.height / 2 - 6, 12, 12);
                    g2.setColor(Color.WHITE);
                    g2.drawOval(thumbBounds.x, thumbBounds.y + thumbBounds.height / 2 - 6, 12, 12);
                }

                @Override
                public void paintTrack(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    UIFactory.enableAntialiasing(g2);
                    Rectangle trackBounds = trackRect;
                    g2.setColor(new Color(30, 30, 30));
                    g2.fillRoundRect(trackBounds.x, trackBounds.y + trackBounds.height / 2 - 2, trackBounds.width, 4, 4,
                            4);

                    // Filled part
                    int fillW = thumbRect.x - trackBounds.x;
                    if (fillW > 0) {
                        g2.setColor(new Color(255, 120, 15));
                        g2.fillRoundRect(trackBounds.x, trackBounds.y + trackBounds.height / 2 - 2, fillW, 4, 4, 4);
                    }
                }
            });

            // 3. Add Listeners now that both are initialized
            iconBtn.addActionListener(e -> {
                com.labinc.util.SoundManager.getInstance().playClick2();
                Boolean muted = (Boolean) iconBtn.getClientProperty("isMuted");
                boolean isNowMuted = (muted == null || !muted);

                if (isNowMuted) {
                    // Mute -> Slider to 0 (Left)
                    volumeSlider.setValue(0);
                } else {
                    // Unmute -> Restore volume
                    volumeSlider.setValue(previousVolume);
                }
            });

            volumeSlider.addChangeListener(e -> {
                int vol = volumeSlider.getValue();
                com.labinc.model.GameSettings.getInstance().setMusicVolume(vol);
                // Also save if user stops dragging? For now just update manager
                com.labinc.util.SoundManager.getInstance().setMusicVolume(vol);

                // Update icon if 0
                boolean isMuted = (vol == 0);
                iconBtn.putClientProperty("isMuted", isMuted);
                iconBtn.repaint();

                if (vol > 0)
                    previousVolume = vol;

                // Fix ghosting: repaint the whole panel
                repaint();
            });

            add(volumeSlider);
            add(iconBtn);

            // --- Animation Logic ---
            expandTimer = new Timer(10, e -> {
                if (currentWidth < EXPANDED_WIDTH) {
                    currentWidth += 5;
                    if (currentWidth > EXPANDED_WIDTH)
                        currentWidth = EXPANDED_WIDTH;
                    setPreferredSize(new Dimension(currentWidth, 34));
                    revalidate();
                    repaint(); // Parent repaint might be needed
                } else {
                    ((Timer) e.getSource()).stop();
                }
            });

            shrinkTimer = new Timer(10, e -> {
                if (currentWidth > COLLAPSED_WIDTH) {
                    currentWidth -= 5;
                    if (currentWidth < COLLAPSED_WIDTH)
                        currentWidth = COLLAPSED_WIDTH;
                    setPreferredSize(new Dimension(currentWidth, 34));
                    revalidate();
                    repaint();
                } else {
                    ((Timer) e.getSource()).stop();
                }
            });

            // Hover listeners
            java.awt.event.MouseAdapter ma = new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    shrinkTimer.stop();
                    expandTimer.start();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    // Check if mouse really exited the whole panel
                    if (!getVisibleRect().contains(e.getPoint())) {
                        expandTimer.stop();
                        shrinkTimer.start();
                    }
                }
            };

            addMouseListener(ma);
            iconBtn.addMouseListener(ma);
            volumeSlider.addMouseListener(ma);
        }

        @Override
        public void paint(Graphics g) {
            // Fill background behind slider when expanded
            if (currentWidth > COLLAPSED_WIDTH) {
                Graphics2D g2 = (Graphics2D) g;
                UIFactory.enableAntialiasing(g2);
                g2.setColor(new Color(45, 50, 55));
                g2.fillRoundRect(15, 0, currentWidth - 15, 34, 6, 6);
                g2.setColor(new Color(60, 65, 70));
                g2.drawRoundRect(15, 0, currentWidth - 15 - 1, 33, 6, 6);
            }
            super.paint(g);
        }
    }

    /**
     * Tworzy przycisk upgrade ze stylem identycznym jak w ChemicalColumn
     */
    private JButton createStyledUpgradeButton(String text, Color bgColor, Color fgColor, boolean enabled) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                UIFactory.enableAntialiasing(g2);

                Color bg = getBackground();
                Color fg = getForeground();

                // Efekt hover
                if (isEnabled() && getModel().isRollover()) {
                    bg = bg.brighter();
                }

                // Tło przycisku
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // Obramowanie
                g2.setColor(new Color(0, 0, 0, 100));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                // Tekst przycisku
                g2.setColor(fg);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                String txt = getText();
                int textX = (getWidth() - fm.stringWidth(txt)) / 2;
                int textY = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(txt, textX, textY);
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setBackground(enabled ? bgColor : new Color(100, 100, 100));
        btn.setForeground(enabled ? fgColor : TEXT_DISABLED);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setEnabled(enabled);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(350, 50));
        btn.setPreferredSize(new Dimension(350, 50));
        return btn;
    }

    public void updateDisplay() {
        double money = gameState.getMoney();
        double income = gameState.calculateAutoSellIncomePerSecond(); // Przychód tylko z autosell

        // Aktualizacja strefy mgły (tylko na zablokowanym terenie)
        int firstLockedIndex = -1;
        int idx = 1;
        for (Factory f : gameState.getFactories().values()) {
            if (!f.isUnlocked(gameState.getFactories())) {
                firstLockedIndex = idx;
                break; // Pierwsza zablokowana wystarczy
            }
            idx++;
        }

        if (firstLockedIndex != -1) {
            // Przesunięcie startu mgły dokładnie na linię pierwszej zablokowanej fabryki
            FogPanel.setLockedZoneStartX((firstLockedIndex - 1) * 420);
        } else {
            FogPanel.setLockedZoneStartX(100000); // Daleko poza światem
        }

        for (Map.Entry<String, Factory> entry : gameState.getFactories().entrySet()) {
            String key = entry.getKey();
            Factory factory = entry.getValue();

            boolean currentlyUnlocked = factory.isUnlocked(gameState.getFactories());
            Boolean previouslyUnlocked = factoryUnlockStatus.get(key);
            boolean statusChanged = false;

            if (previouslyUnlocked == null) {
                factoryUnlockStatus.put(key, currentlyUnlocked);
                statusChanged = true; // Wymuś odświeżenie przy starcie/po wczytaniu
            } else if (previouslyUnlocked != currentlyUnlocked) {
                statusChanged = true;
                factoryUnlockStatus.put(key, currentlyUnlocked);
            }

            int currentTier = factory.getCurrentTier();
            Integer previousTier = factoryTierStatus.get(key);
            if (previousTier == null) {
                factoryTierStatus.put(key, currentTier);
                statusChanged = true; // Wymuś odświeżenie przy starcie
            } else {
                boolean wasActive = previousTier > 0;
                boolean isActive = currentTier > 0;
                boolean wasMax = previousTier >= factory.getMaxTier();
                boolean isMax = currentTier >= factory.getMaxTier();
                if (wasActive != isActive || wasMax != isMax) {
                    statusChanged = true;
                }
                factoryTierStatus.put(key, currentTier);
            }

            if (statusChanged) {
                replaceFactoryCard(key);
            }
        }

        if (money >= 1_000_000)
            moneyValueLabel.setText("$" + sciFormat.format(money));
        else
            moneyValueLabel.setText("$" + moneyFormat.format(money));

        if (income >= 1_000_000)
            incomeValueLabel.setText("$" + sciFormat.format(income) + "/s");
        else
            incomeValueLabel.setText("$" + moneyFormat.format(income) + "/s");

        for (Map.Entry<String, Factory> entry : gameState.getFactories().entrySet()) {
            String factoryKey = entry.getKey();
            Factory factory = entry.getValue();

            JLabel tierLabel = tierLabels.get(factoryKey);
            if (tierLabel != null)
                tierLabel.setText("tier: " + factory.getCurrentTier() + "/" + factory.getMaxTier());

            JButton upgradeBtn = upgradeButtons.get(factoryKey);

            // Aktualizuj przycisk
            if (upgradeBtn != null) {
                double cost = factory.getUpgradeCost();
                boolean canAfford = factory.canAfford(money);
                String costStr = formatCost(cost);

                // WAŻNE: Aktualizuj tekst przycisku z nowym kosztem!
                String btnPrefix = factory.getCurrentTier() == 0 ? "Aktywuj" : "Ulepsz";
                String newBtnText = btnPrefix + " ($" + costStr + ")";
                if (!upgradeBtn.getText().equals(newBtnText)) {
                    upgradeBtn.setText(newBtnText);
                }

                if (upgradeBtn.isEnabled() != canAfford) {
                    upgradeBtn.setEnabled(canAfford);

                    if (canAfford) {
                        upgradeBtn.setBackground(ORANGE);
                        upgradeBtn.setForeground(Color.BLACK);
                        if (factory.getCurrentTier() == 0) {
                            upgradeBtn.setBackground(new Color(34, 197, 94));
                            upgradeBtn.setForeground(Color.WHITE);
                        }
                    } else {
                        upgradeBtn.setBackground(new Color(100, 100, 100));
                        upgradeBtn.setForeground(TEXT_DISABLED);
                    }
                }
            }
        }

        double totalProduction = 0;
        for (Factory f : gameState.getFactories().values()) {
            totalProduction += f.getCurrentProduction().values().stream().mapToDouble(Double::doubleValue).sum();
        }
        String moneyStr = money >= 1_000_000 ? "$" + sciFormat.format(money) : "$" + moneyFormat.format(money);
        String incomeStr = income >= 1_000_000 ? "$" + sciFormat.format(income) : "$" + moneyFormat.format(income);
        infoLabel.setText("Pieniądze: " + moneyStr + " | Przychód: " + incomeStr + "/s | Produkcja: "
                + formatMass(totalProduction));
    }

    private void replaceFactoryCard(String key) {
        int index = -1;
        int i = 0;
        for (String k : gameState.getFactories().keySet()) {
            if (k.equals(key)) {
                index = i;
                break;
            }
            i++;
        }

        if (index != -1 && index < factoriesContainer.getComponentCount()) {
            // Usuń stare mapowania
            upgradeButtons.remove(key);
            tierLabels.remove(key);

            Factory factory = gameState.getFactories().get(key);
            JPanel newCard = createFactoryCard(key, factory, index + 1);

            // Po prostu podmień kartę. Jeśli nowa jest odblokowana, nie będzie miała mgły.
            // Jeśli stara miała mgłę (LayeredPane), to zniknie razem z nią.
            factoriesContainer.remove(index);
            factoriesContainer.add(newCard, index);

            factoriesContainer.revalidate();
            factoriesContainer.repaint();
        }
    }
}