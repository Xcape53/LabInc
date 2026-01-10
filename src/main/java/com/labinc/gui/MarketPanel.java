package com.labinc.gui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.labinc.model.GameState;
import com.labinc.model.Resource;
import com.labinc.util.Formatter;
import com.labinc.util.GameColors;
import com.labinc.util.UIFactory;

/**
 * Panel rynku - sprzedaż surowców
 */
public class MarketPanel extends JPanel {
    private final GameState gameState;
    private PeriodicTablePanel periodicTable;
    private final Map<String, AutoSellConfig> autoSellConfigs = new HashMap<>();

    // Panel listy auto-sprzedaży
    private JPanel autoSellListPanel;

    // ComboBox z listą produkowanych pierwiastków
    private JComboBox<String> resourceCombo;

    // Lista animowanych tekstów
    private final List<FloatingText> floatingTexts = new ArrayList<>();
    private final Timer floatingTextTimer;

    // Kolory motywu
    private static final Color BG_DARK = GameColors.BG_DARK;
    private static final Color METAL_DARK = new Color(38, 43, 48);
    private static final Color ORANGE = GameColors.ORANGE;
    private static final Color ORANGE_HOVER = new Color(185, 90, 25);
    private static final Color TEXT_LIGHT = GameColors.TEXT_LIGHT;
    private static final Color TEXT_DIM = GameColors.TEXT_DIM;
    private static final Color INPUT_BG = new Color(45, 48, 52);
    private static final Color INPUT_BORDER = new Color(60, 60, 60);

    private static class AutoSellConfig {
        boolean enabled;
        double threshold;

        AutoSellConfig(boolean enabled, double threshold) {
            this.enabled = enabled;
            this.threshold = threshold;
        }
    }

    /**
     * Klasa dla animowanego tekstu (particle) pokazującego kwotę sprzedaży
     */
    private static class FloatingText {
        String text;
        int x, y;
        float alpha;

        FloatingText(String text, int x, int y) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.alpha = 1.0f;
        }

        boolean update() {
            y -= 2; // Lewituj do góry
            alpha -= 0.03f; // Zanikaj
            return alpha > 0;
        }
    }

    public MarketPanel(GameState gameState) {
        this.gameState = gameState;
        setLayout(new BorderLayout(10, 10));
        setBackground(BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();

        // Timer do animacji floating text
        floatingTextTimer = new Timer(30, e -> {
            if (!floatingTexts.isEmpty()) {
                floatingTexts.removeIf(ft -> !ft.update());
                repaint();
            }
        });
        floatingTextTimer.start();

        // Listener do odświeżania comboboxa przy upgrade fabryk
        gameState.addListener(new GameState.GameEventListener() {
            @Override
            public void onFactoryUpgraded(String factoryName, int newTier) {
                refreshResourceCombo();
            }
        });
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);

        // Rysuj floating texts na wierzchu wszystkiego
        if (!floatingTexts.isEmpty()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(new Font("Arial", Font.BOLD, 18));

            for (FloatingText ft : floatingTexts) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ft.alpha));

                // Cień tekstu
                g2.setColor(new Color(0, 0, 0, (int) (150 * ft.alpha)));
                g2.drawString(ft.text, ft.x + 2, ft.y + 2);

                // Tekst główny - zielony
                g2.setColor(new Color(50, 220, 80, (int) (255 * ft.alpha)));
                g2.drawString(ft.text, ft.x, ft.y);
            }
            g2.dispose();
        }
    }

    /**
     * Dodaje animowany tekst pokazujący kwotę sprzedaży
     */
    private void showFloatingText(String text, java.awt.Point location) {
        FloatingText ft = new FloatingText(text, location.x, location.y);
        floatingTexts.add(ft);
    }

    private void initComponents() {
        // Nagłówek
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BG_DARK);
        JLabel header = new JLabel("RYNEK - Tablica Mendelejewa", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setForeground(ORANGE);
        topPanel.add(header, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Tablica Mendelejewa z wycentrowaniem
        periodicTable = new PeriodicTablePanel(gameState);

        // Panel wrapper do wycentrowania tablicy - bez scrolla, 100% kontenera
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(BG_DARK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH; // Wypełnij całą dostępną przestrzeń
        centerWrapper.add(periodicTable, gbc);

        // Dodaj bezpośrednio bez JScrollPane
        add(centerWrapper, BorderLayout.CENTER);

        // Panel kontrolny na dole - industrialny styl
        JPanel controlPanel = createMassActionsPanel();
        add(controlPanel, BorderLayout.SOUTH);

        // Ustaw listener na PeriodicTable do aktualizacji listy auto-sell
        periodicTable.setAutoSellChangeListener((symbol, enabled, threshold) -> {
            // Aktualizuj wewnętrzną mapę
            if (enabled) {
                autoSellConfigs.put(symbol, new AutoSellConfig(true, threshold));
            } else {
                autoSellConfigs.remove(symbol);
            }
            updateAutoSellList();
        });

        // Ustaw listener sprzedaży na PeriodicTable
        periodicTable.setSellListener((symbol, value, screenLocation) -> {
            com.labinc.util.SoundManager.getInstance().playClick1();
            try {
                java.awt.Point panelLoc = getLocationOnScreen();
                int x = screenLocation.x - panelLoc.x - 40;
                int y = screenLocation.y - panelLoc.y;
                showFloatingText("+$" + Formatter.formatMoney(value), new java.awt.Point(x, y));
            } catch (Exception e) {
                // Ignoruj jeśli panel nie jest widoczny
            }
        });

        // Listener do aktualizacji
        gameState.addListener(new GameState.GameEventListener() {
            @Override
            public void onMoneyChanged(double newMoney) {
                // Auto-refresh
            }

            @Override
            public void onResourceChanged(String resourceSymbol, double newAmount) {
                SwingUtilities.invokeLater(() -> {
                    checkAutoSell(resourceSymbol, newAmount);
                });
            }

            @Override
            public void onFactoryUpgraded(String factoryName, int newTier) {
                // Not needed here
            }
        });
    }

    @SuppressWarnings("unused")
    private void checkAutoSell(String symbol, double amount) {
        AutoSellConfig config = autoSellConfigs.get(symbol);
        if (config != null && config.enabled) {
            Resource resource = gameState.getResources().get(symbol);
            if (resource != null && resource.getTotalValue() >= config.threshold) {
                gameState.sellResource(symbol, resource.getAmount());
            }
        }
    }

    /**
     * Ustaw HoverOverlay Panel (wywoływane z LabIncGame)
     */
    public void setHoverOverlay(HoverOverlayPanel hoverOverlay, javax.swing.JFrame frame) {
        if (periodicTable != null) {
            periodicTable.setHoverOverlay(hoverOverlay, frame);
        }

        // Ustaw listener sprzedaży na HoverOverlay dla floating text
        if (hoverOverlay != null) {
            hoverOverlay.setSellListener((symbol, value, screenLocation) -> {
                try {
                    java.awt.Point panelLoc = getLocationOnScreen();
                    int x = screenLocation.x - panelLoc.x - 40;
                    int y = screenLocation.y - panelLoc.y;
                    showFloatingText("+$" + Formatter.formatMoney(value), new java.awt.Point(x, y));
                } catch (Exception e) {
                    // Ignoruj jeśli panel nie jest widoczny
                }
            });
        }
    }

    /**
     * Synchronizuje ustawienia auto-sell z Resource (po wczytaniu zapisu)
     */
    public void syncAutoSellFromResources() {
        // Synchronizuj ElementTile w PeriodicTable
        if (periodicTable != null) {
            periodicTable.syncAutoSellFromResources();
        }

        // Synchronizuj wewnętrzną mapę autoSellConfigs ze wszystkich zasobów w grze
        autoSellConfigs.clear();
        for (Map.Entry<String, Resource> entry : gameState.getResources().entrySet()) {
            Resource r = entry.getValue();
            if (r.isAutoSellEnabled()) {
                autoSellConfigs.put(entry.getKey(), new AutoSellConfig(true, r.getAutoSellThreshold()));
            }
        }

        // Zaktualizuj listę
        updateAutoSellList();
    }

    /**
     * Tworzy panel akcji masowych w industrialnym stylu
     */
    private JPanel createMassActionsPanel() {
        // Główny panel z metalowym tłem
        JPanel panel = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int bevel = 8;

                // Górny trapez (jasny - światło od góry)
                int[] topTrapX = { 0, w, w - bevel, bevel };
                int[] topTrapY = { 0, 0, bevel, bevel };
                GradientPaint topGrad = new GradientPaint(0, 0, new Color(100, 105, 110), 0, bevel,
                        new Color(60, 65, 70));
                g2.setPaint(topGrad);
                g2.fillPolygon(topTrapX, topTrapY, 4);

                // Lewy trapez (jasny)
                int[] leftTrapX = { 0, bevel, bevel, 0 };
                int[] leftTrapY = { 0, bevel, h - bevel, h };
                GradientPaint leftGrad = new GradientPaint(0, 0, new Color(90, 95, 100), bevel, 0,
                        new Color(50, 55, 60));
                g2.setPaint(leftGrad);
                g2.fillPolygon(leftTrapX, leftTrapY, 4);

                // Prawy trapez (ciemny - cień)
                int[] rightTrapX = { w, w, w - bevel, w - bevel };
                int[] rightTrapY = { 0, h, h - bevel, bevel };
                GradientPaint rightGrad = new GradientPaint(w - bevel, 0, new Color(35, 38, 42), w, 0,
                        new Color(20, 23, 27));
                g2.setPaint(rightGrad);
                g2.fillPolygon(rightTrapX, rightTrapY, 4);

                // Dolny trapez (ciemny - cień)
                int[] bottomTrapX = { 0, w, w - bevel, bevel };
                int[] bottomTrapY = { h, h, h - bevel, h - bevel };
                GradientPaint bottomGrad = new GradientPaint(0, h - bevel, new Color(35, 38, 42), 0, h,
                        new Color(15, 17, 20));
                g2.setPaint(bottomGrad);
                g2.fillPolygon(bottomTrapX, bottomTrapY, 4);

                // Środkowa płyta (główna powierzchnia)
                g2.setColor(METAL_DARK);
                g2.fillRect(bevel, bevel, w - 2 * bevel, h - 2 * bevel);

                // Linie oddzielające trapezy
                g2.setColor(new Color(25, 27, 30));
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(bevel, bevel, w - bevel, bevel);
                g2.drawLine(bevel, h - bevel, w - bevel, h - bevel);

                // Nity w rogach
                drawRivet(g2, bevel + 12, bevel + 12);
                drawRivet(g2, w - bevel - 12, bevel + 12);
                drawRivet(g2, bevel + 12, h - bevel - 12);
                drawRivet(g2, w - bevel - 12, h - bevel - 12);
            }
        };
        panel.setBackground(METAL_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        // Zawartość - dwa rzędy
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Górny rząd - przycisk sprzedaży
        JPanel sellRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        sellRow.setOpaque(false);

        JButton sellAllButton = createStyledButton("SPRZEDAJ WSZYSTKO", true);
        sellAllButton.addActionListener(e -> {
            com.labinc.util.SoundManager.getInstance().playClick1();
            // Oblicz sumę przed sprzedażą
            double totalValue = 0;
            for (Resource resource : gameState.getResources().values()) {
                totalValue += resource.getTotalValue();
            }

            if (totalValue > 0) {
                gameState.sellAllResources();
                periodicTable.repaint();

                // Pokaż animowany tekst z kwotą
                java.awt.Point buttonLoc = sellAllButton.getLocationOnScreen();
                java.awt.Point panelLoc = getLocationOnScreen();
                int x = buttonLoc.x - panelLoc.x + sellAllButton.getWidth() / 2 - 40;
                int y = buttonLoc.y - panelLoc.y;
                showFloatingText("+$" + Formatter.formatMoney(totalValue), new java.awt.Point(x, y));
            }
        });
        sellRow.add(sellAllButton);
        contentPanel.add(sellRow);

        // Separator
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(createSeparatorLine());
        contentPanel.add(Box.createVerticalStrut(8));

        // Dolny rząd - auto-sprzedaż
        JPanel autoSellRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        autoSellRow.setOpaque(false);

        JLabel autoSellLabel = new JLabel("Auto-sprzedaż:");
        autoSellLabel.setFont(new Font("Arial", Font.BOLD, 13));
        autoSellLabel.setForeground(TEXT_LIGHT);

        // Stylizowany ComboBox - pierwiastki produkowane przez fabryki
        this.resourceCombo = UIFactory.createStyledComboBox(UIFactory.ComboBoxStyle.LARGE, new String[0]);
        refreshResourceCombo();

        JLabel atLabel = new JLabel("przy wartości:");
        atLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        atLabel.setForeground(TEXT_DIM);

        // Stylizowane pole tekstowe
        JTextField thresholdField = createStyledTextField("1000", 7);

        JLabel dollarLabel = new JLabel("$");
        dollarLabel.setFont(new Font("Arial", Font.BOLD, 14));
        dollarLabel.setForeground(ORANGE);

        JButton configAutoSellButton = createStyledButton("USTAW", false);
        configAutoSellButton.addActionListener(e -> {
            com.labinc.util.SoundManager.getInstance().playClick2();
            String selected = (String) resourceCombo.getSelectedItem();
            if (selected != null) {
                String symbol = selected.substring(selected.indexOf("[") + 1, selected.indexOf("]"));
                try {
                    double threshold = Double.parseDouble(thresholdField.getText());
                    autoSellConfigs.put(symbol, new AutoSellConfig(true, threshold));
                    periodicTable.setAutoSell(symbol, true, threshold);
                    updateAutoSellList();

                    // Pokaż animowany tekst potwierdzający
                    java.awt.Point buttonLoc = configAutoSellButton.getLocationOnScreen();
                    java.awt.Point panelLoc = getLocationOnScreen();
                    int x = buttonLoc.x - panelLoc.x + configAutoSellButton.getWidth() / 2 - 30;
                    int y = buttonLoc.y - panelLoc.y;
                    showFloatingText(symbol + " @$" + formatThreshold(threshold), new java.awt.Point(x, y));
                } catch (NumberFormatException ex) {
                    // Ignoruj błędne wartości
                }
            }
        });

        autoSellRow.add(autoSellLabel);
        autoSellRow.add(resourceCombo);
        autoSellRow.add(atLabel);
        autoSellRow.add(thresholdField);
        autoSellRow.add(dollarLabel);
        autoSellRow.add(configAutoSellButton);

        contentPanel.add(autoSellRow);
        panel.add(contentPanel, BorderLayout.CENTER);

        // Panel listy auto-sprzedaży po prawej stronie
        autoSellListPanel = createAutoSellListPanel();
        panel.add(autoSellListPanel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Tworzy panel z listą auto-sprzedaży (scrollowalny)
     */
    private JPanel createAutoSellListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(160, 80));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 10));

        // Nagłówek
        JLabel header = new JLabel("AUTO-SPRZEDAŻ", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 11));
        header.setForeground(ORANGE);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        panel.add(header, BorderLayout.NORTH);

        // Lista
        JPanel listContent = new JPanel();
        listContent.setLayout(new BoxLayout(listContent, BoxLayout.Y_AXIS));
        listContent.setBackground(INPUT_BG);

        JScrollPane scrollPane = new JScrollPane(listContent);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setBackground(INPUT_BG);
        scrollPane.setBorder(BorderFactory.createLineBorder(INPUT_BORDER, 1));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);

        // Ukryj scrollbar ale zachowaj funkcjonalność
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));

        panel.add(scrollPane, BorderLayout.CENTER);

        // Przechowaj referencję do listContent
        panel.putClientProperty("listContent", listContent);

        return panel;
    }

    /**
     * Aktualizuje listę auto-sprzedaży
     */
    private void updateAutoSellList() {
        if (autoSellListPanel == null)
            return;

        JPanel listContent = (JPanel) autoSellListPanel.getClientProperty("listContent");
        if (listContent == null)
            return;

        listContent.removeAll();

        for (Map.Entry<String, AutoSellConfig> entry : autoSellConfigs.entrySet()) {
            if (entry.getValue().enabled) {
                JPanel row = new JPanel(new BorderLayout());
                row.setOpaque(false);
                row.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

                JLabel symbolLabel = new JLabel(entry.getKey());
                symbolLabel.setFont(new Font("Arial", Font.BOLD, 12));
                symbolLabel.setForeground(TEXT_LIGHT);

                // Container for value + delete button
                JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
                rightPanel.setOpaque(false);

                JLabel thresholdLabel = new JLabel("$" + formatThreshold(entry.getValue().threshold));
                thresholdLabel.setFont(new Font("Arial", Font.PLAIN, 11));
                thresholdLabel.setForeground(ORANGE);

                // Remove button (X)
                JButton removeBtn = new JButton("X");
                removeBtn.setFont(new Font("Arial", Font.BOLD, 10));
                removeBtn.setForeground(new Color(255, 80, 80)); // Reddish
                removeBtn.setBackground(new Color(60, 30, 30));
                removeBtn.setContentAreaFilled(false); // Make it blend better or use setOpaque(false) with custom paint
                                                       // if needed, but simple is ok
                removeBtn.setBorder(BorderFactory.createLineBorder(new Color(100, 50, 50)));
                removeBtn.setFocusPainted(false);
                removeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                removeBtn.setPreferredSize(new Dimension(18, 18));
                removeBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));

                // Custom painting for a small nice button
                removeBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
                    @Override
                    public void paint(Graphics g, javax.swing.JComponent c) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        boolean isHover = false;
                        if (c instanceof javax.swing.AbstractButton) {
                            isHover = ((javax.swing.AbstractButton) c).getModel().isRollover();
                        }

                        g2.setColor(isHover ? new Color(200, 60, 60) : new Color(60, 30, 30));
                        g2.fillRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 4, 4);

                        g2.setColor(isHover ? new Color(255, 100, 100) : new Color(100, 50, 50));
                        g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 4, 4);

                        super.paint(g, c);
                    }
                });

                removeBtn.addActionListener(e -> {
                    com.labinc.util.SoundManager.getInstance().playClick2();
                    // Remove auto-sell
                    String symbol = entry.getKey();
                    autoSellConfigs.remove(symbol);
                    periodicTable.setAutoSell(symbol, false, 0); // Disable in logic
                    updateAutoSellList(); // Refresh list
                });

                rightPanel.add(thresholdLabel);
                rightPanel.add(Box.createHorizontalStrut(8));
                rightPanel.add(removeBtn);

                row.add(symbolLabel, BorderLayout.WEST);
                row.add(rightPanel, BorderLayout.EAST);

                listContent.add(row);
            }
        }

        // Placeholder jeśli pusta
        if (autoSellConfigs.isEmpty()) {
            JLabel emptyLabel = new JLabel("(brak)", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 11));
            emptyLabel.setForeground(TEXT_DIM);
            listContent.add(emptyLabel);
        }

        listContent.revalidate();
        listContent.repaint();
    }

    /**
     * Formatuje próg do wyświetlenia (format naukowy bez zer dziesiętnych)
     */
    private String formatThreshold(double value) {
        if (value == 0)
            return "0";

        // Znajdź wykładnik
        int exp = (int) Math.floor(Math.log10(Math.abs(value)));
        double mantissa = value / Math.pow(10, exp);

        // Jeśli mantissa jest całkowita, nie pokazuj części dziesiętnej
        if (mantissa == Math.floor(mantissa)) {
            if (exp == 0) {
                return String.format("%.0f", value);
            } else {
                return String.format("%.0fE%d", mantissa, exp);
            }
        } else {
            // Mantissa z częścią dziesiętną - usuń zbędne zera
            String mantStr = String.valueOf(mantissa);
            // Usuń końcowe zera po kropce
            mantStr = mantStr.replaceAll("(\\.\\d*?)0+$", "$1").replaceAll("\\.$", "");
            if (exp == 0) {
                return mantStr;
            } else {
                return mantStr + "E" + exp;
            }
        }
    }

    /**
     * Tworzy stylizowany przycisk w stylu sidebara
     */
    private JButton createStyledButton(String text, boolean isPrimary) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                boolean isHover = getModel().isRollover();
                boolean isPressed = getModel().isPressed();

                // Tło metalowe - identyczne jak w sidebarze
                Color bgTop = isPrimary
                        ? (isPressed ? ORANGE_HOVER.darker() : (isHover ? ORANGE : ORANGE_HOVER))
                        : (isPressed ? new Color(45, 50, 55)
                                : (isHover ? new Color(70, 75, 80) : new Color(55, 60, 65)));
                Color bgBottom = isPrimary
                        ? (isPressed ? ORANGE_HOVER.darker().darker()
                                : (isHover ? ORANGE_HOVER : ORANGE_HOVER.darker()))
                        : (isPressed ? new Color(30, 35, 40)
                                : (isHover ? new Color(50, 55, 60) : new Color(40, 45, 50)));

                GradientPaint bgGrad = new GradientPaint(0, 0, bgTop, 0, h, bgBottom);
                g2.setPaint(bgGrad);
                g2.fillRoundRect(2, 2, w - 4, h - 4, 8, 8);

                // Obramowanie
                g2.setColor(new Color(25, 25, 25));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(2, 2, w - 4, h - 4, 8, 8);

                // Nit po lewej stronie
                drawSmallRivet(g2, 14, h / 2);
                // Nit po prawej stronie
                drawSmallRivet(g2, w - 14, h / 2);

                // Tekst - wyśrodkowany
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textX = (w - fm.stringWidth(getText())) / 2;
                int textY = (h + fm.getAscent() - fm.getDescent()) / 2;

                // Cień tekstu
                g2.setColor(new Color(0, 0, 0, 150));
                g2.drawString(getText(), textX + 1, textY + 1);

                // Tekst główny
                g2.setColor(isPrimary ? Color.WHITE : TEXT_LIGHT);
                g2.drawString(getText(), textX, textY);
            }

            private void drawSmallRivet(Graphics2D g2, int x, int y) {
                g2.setColor(new Color(30, 30, 30));
                g2.fillOval(x - 4, y - 4, 8, 8);
                g2.setColor(new Color(70, 75, 80));
                g2.fillOval(x - 3, y - 3, 6, 6);
            }
        };

        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(isPrimary ? Color.WHITE : TEXT_LIGHT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(isPrimary ? 220 : 100, 40));
        return button;
    }

    /**
     * Tworzy stylizowane pole tekstowe
     */
    private JTextField createStyledTextField(String defaultText, int columns) {
        JTextField field = new JTextField(defaultText, columns);
        field.setBackground(new Color(45, 48, 52));
        field.setForeground(TEXT_LIGHT);
        field.setCaretColor(ORANGE);
        field.setFont(new Font("Arial", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60), 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        field.setPreferredSize(new Dimension(80, 30));
        return field;
    }

    /**
     * Tworzy linię separatora w stylu metalowej rury
     */
    private JPanel createSeparatorLine() {
        JPanel sep = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int y = getHeight() / 2;
                int margin = 50;

                // Rura (gradient metalowy)
                GradientPaint pipeGrad = new GradientPaint(margin, y - 2, new Color(60, 65, 70), margin, y + 2,
                        new Color(35, 40, 45));
                g2.setPaint(pipeGrad);
                g2.fillRoundRect(margin, y - 2, getWidth() - 2 * margin, 4, 2, 2);

                // Blask na górze rury
                g2.setColor(new Color(255, 255, 255, 25));
                g2.drawLine(margin + 5, y - 1, getWidth() - margin - 5, y - 1);
            }
        };
        sep.setOpaque(false);
        sep.setPreferredSize(new Dimension(100, 10));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        return sep;
    }

    /**
     * Rysuje nit dekoracyjny
     */
    private void drawRivet(Graphics2D g2, int x, int y) {
        // Cień
        g2.setColor(new Color(15, 15, 15));
        g2.fillOval(x - 5, y - 5, 10, 10);
        // Nit
        GradientPaint rivetGrad = new GradientPaint(x - 3, y - 3, new Color(90, 95, 100), x + 3, y + 3,
                new Color(45, 50, 55));
        g2.setPaint(rivetGrad);
        g2.fillOval(x - 4, y - 4, 8, 8);
        // Blask
        g2.setColor(new Color(255, 255, 255, 35));
        g2.fillOval(x - 2, y - 3, 3, 2);
    }

    /**
     * Odświeża listę pierwiastków w comboboxie auto-sell
     * na podstawie aktywnych fabryk
     */
    private void refreshResourceCombo() {
        if (resourceCombo == null)
            return;

        String selectedBefore = (String) resourceCombo.getSelectedItem();
        resourceCombo.removeAllItems();

        java.util.Set<String> addedSymbols = new java.util.HashSet<>();
        for (com.labinc.model.Factory factory : gameState.getFactories().values()) {
            for (String symbol : factory.getCurrentProduction().keySet()) {
                if (!addedSymbols.contains(symbol)) {
                    Resource resource = gameState.getResources().get(symbol);
                    if (resource != null) {
                        resourceCombo.addItem(resource.getName() + " [" + resource.getSymbol() + "]");
                        addedSymbols.add(symbol);
                    }
                }
            }
        }

        // Jeśli brak produkcji, dodaj wszystkie zasoby bazowe
        if (resourceCombo.getItemCount() == 0) {
            for (Resource resource : gameState.getResources().values()) {
                String sym = resource.getSymbol();
                if (sym.length() <= 2 && !sym.matches(".*\\d.*")) {
                    resourceCombo.addItem(resource.getName() + " [" + sym + "]");
                }
            }
        }

        // Przywróć poprzedni wybór jeśli możliwe
        if (selectedBefore != null) {
            for (int i = 0; i < resourceCombo.getItemCount(); i++) {
                if (resourceCombo.getItemAt(i).equals(selectedBefore)) {
                    resourceCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

}
