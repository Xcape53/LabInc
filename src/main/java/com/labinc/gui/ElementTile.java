package com.labinc.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.labinc.model.GameState;
import com.labinc.model.Resource;
import com.labinc.util.Formatter;
import com.labinc.util.GameColors;

/**
 * Pojedynczy kafelek pierwiastka w tablicy Mendelejewa
 */
public class ElementTile extends JPanel {
    private final GameState gameState;
    private final String symbol;
    private final int atomicNumber;
    private boolean hovered = false;

    private static final int REFERENCE_SIZE = 60;

    private static final int CORNER_RADIUS = 8;

    // Kolory
    private static final Color BG_INACTIVE = new Color(35, 35, 35);
    private static final Color BG_GREEN_DARK = new Color(15, 60, 25); // Ciemnozielony (1% pieniędzy)
    private static final Color BG_RED_DARK = new Color(100, 15, 15); // Ciemnoczerwony (100% pieniędzy)
    private static final Color TEXT_COLOR = new Color(220, 220, 220);
    private static final Color ORANGE = GameColors.ORANGE;

    // Auto-sell config (na podstawie WARTOŚCI w dolarach, nie kg)
    private boolean autoSellEnabled = false;
    private double autoSellThreshold = 1000.0; // Wartość w $

    // Listener zmiany auto-sell
    private AutoSellChangeListener autoSellChangeListener;

    // Listener sprzedaży
    private SellListener sellListener;

    /**
     * Interfejs dla callbacku zmiany auto-sell
     */
    public interface AutoSellChangeListener {
        void onAutoSellChanged(String symbol, boolean enabled, double threshold);
    }

    /**
     * Interfejs dla callbacku sprzedaży
     */
    public interface SellListener {
        void onSell(String symbol, double value, java.awt.Point screenLocation);
    }

    // OPTYMALIZACJA: Cache koloru tła
    private Color cachedBgColor = BG_INACTIVE;
    private double lastCachedValue = -1;
    private double lastCachedMoney = -1;

    public ElementTile(GameState gameState, String symbol, int atomicNumber) {
        this.gameState = gameState;
        this.symbol = symbol;
        this.atomicNumber = atomicNumber;

        // Synchronizuj auto-sell z Resource (dla wczytanych zapisów)
        syncAutoSellFromResource();

        updateSize(REFERENCE_SIZE);
        setOpaque(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                // Hover effect jest teraz obsługiwany przez PeriodicTablePanel (JLayeredPane)
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                // Hover effect jest teraz obsługiwany przez PeriodicTablePanel (JLayeredPane)
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // Kliknięcie obsługiwane przez hover tile w PeriodicTablePanel
                Resource resource = gameState.getResources().get(symbol);
                if (resource == null)
                    return;

                // Szybkie menu z opcjami
                String[] options;
                if (resource.getAmount() > 0) {
                    options = autoSellEnabled
                            ? new String[] { "Sprzedaj", "Ustaw Auto-sell", "Usuń Auto-sell", "Anuluj" }
                            : new String[] { "Sprzedaj", "Ustaw Auto-sell", "Anuluj" };
                } else {
                    options = autoSellEnabled ? new String[] { "Ustaw Auto-sell", "Usuń Auto-sell", "Anuluj" }
                            : new String[] { "Ustaw Auto-sell", "Anuluj" };
                }

                int choice = JOptionPane.showOptionDialog(ElementTile.this,
                        resource.getName() + " (" + symbol + ")\nIlość: " + Formatter.formatAmount(resource.getAmount())
                                + " kg\nWartość: $" + Formatter.formatAmount(resource.getTotalValue()),
                        "Opcje pierwiastka",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        options,
                        options[0]);

                if (choice == -1)
                    return; // Zamknięto okno

                String selected = options[choice];
                if (selected.equals("Sprzedaj")) {
                    double value = resource.getTotalValue();
                    gameState.sellResource(symbol, resource.getAmount());

                    // Powiadom listener o sprzedaży
                    if (sellListener != null) {
                        java.awt.Point loc = getLocationOnScreen();
                        loc.x += getWidth() / 2;
                        loc.y += getHeight() / 2;
                        sellListener.onSell(symbol, value, loc);
                    }
                } else if (selected.equals("Ustaw Auto-sell")) {
                    String input = JOptionPane.showInputDialog(ElementTile.this,
                            "Ustaw próg auto-sprzedaży (wartość w $):", autoSellThreshold);
                    if (input != null) {
                        try {
                            double threshold = Double.parseDouble(input);
                            setAutoSell(true, threshold);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(ElementTile.this,
                                    "Nieprawidłowa wartość!", "Błąd", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else if (selected.equals("Usuń Auto-sell")) {
                    setAutoSell(false, 1000.0);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Resource resource = gameState.getResources().get(symbol);
        if (resource == null)
            return;

        int w = getWidth();
        int h = getHeight();
        float scale = Math.min(w, h) / (float) REFERENCE_SIZE; // Współczynnik skalowania

        // Oblicz kolor na podstawie wartości względem pieniędzy gracza
        Color bgColor = calculateColor(resource);

        // Rysuj zaokrąglony prostokąt (skalowany do rozmiaru komponentu)
        int radius = (int) (CORNER_RADIUS * scale);
        RoundRectangle2D rect = new RoundRectangle2D.Double(0, 0, w - 1, h - 1, radius, radius);
        g2d.setColor(bgColor);
        g2d.fill(rect);

        // Obramowanie
        g2d.setColor(hovered ? ORANGE : new Color(60, 60, 60));
        g2d.setStroke(new BasicStroke(hovered ? 2 : 1));
        g2d.draw(rect);

        // Skalowane czcionki
        int atomicFontSize = Math.max(7, (int) (9 * scale));
        int symbolFontSize = Math.max(10, (int) (18 * scale));
        int amountFontSize = Math.max(7, (int) (10 * scale));

        // Numer atomowy
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("Arial", Font.PLAIN, atomicFontSize));
        g2d.drawString(String.valueOf(atomicNumber), (int) (4 * scale), (int) (12 * scale));

        // Symbol
        g2d.setFont(new Font("Arial", Font.BOLD, symbolFontSize));
        FontMetrics fm = g2d.getFontMetrics();
        int symbolWidth = fm.stringWidth(symbol);
        g2d.drawString(symbol, (w - symbolWidth) / 2, (int) (h * 0.58f));

        // Ilość (tylko przy hover)
        if (hovered && resource.getAmount() > 0) {
            g2d.setFont(new Font("Arial", Font.PLAIN, amountFontSize));
            String amountStr = Formatter.formatAmount(resource.getAmount());
            int amountWidth = g2d.getFontMetrics().stringWidth(amountStr);
            g2d.drawString(amountStr, (w - amountWidth) / 2, (int) (h * 0.83f));
        }

        // Nie pokazujemy już rozwiniętych opcji tutaj - obsługuje to PeriodicTablePanel

        g2d.dispose();
    }

    /**
     * Aktualizuje rozmiar kafelka
     */
    public void updateSize(int size) {
        Dimension d = new Dimension(size, size);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
        revalidate();
        repaint();
    }

    private Color calculateColor(Resource resource) {
        double amount = resource.getAmount();

        // Jeśli amount <= 0, sprawdź czy surowiec jest produkowany
        if (amount <= 0) {
            // Sprawdź czy jakaś fabryka produkuje ten surowiec
            boolean isProduced = gameState.isResourceBeingProduced(resource.getSymbol());

            if (isProduced) {
                // Surowiec jest produkowany ale ma 0 (np. autosell) - pokaż zielony
                cachedBgColor = BG_GREEN_DARK;
                lastCachedValue = 0;
                lastCachedMoney = gameState.getMoney();
                return BG_GREEN_DARK;
            } else {
                // Surowiec nieaktywny - szary
                cachedBgColor = BG_INACTIVE;
                lastCachedValue = 0;
                lastCachedMoney = gameState.getMoney();
                return BG_INACTIVE;
            }
        }

        double value = resource.getTotalValue();
        double currentMoney = gameState.getMoney();

        // OPTYMALIZACJA: Sprawdź czy trzeba przeliczać kolor
        // Tolerancja 5% - nie przeliczaj przy małych zmianach
        if (lastCachedValue > 0 && lastCachedMoney > 0) {
            double valueDelta = Math.abs(value - lastCachedValue) / lastCachedValue;
            double moneyDelta = Math.abs(currentMoney - lastCachedMoney) / Math.max(1, lastCachedMoney);
            if (valueDelta < 0.05 && moneyDelta < 0.05) {
                return cachedBgColor; // Użyj cache
            }
        }

        // Oblicz ratio: wartość pierwiastka / aktualne pieniądze gracza
        // 0.01 (1%) = ciemnozielony, 1.0 (100%) = ciemnoczerwony
        double ratio;
        if (currentMoney > 0) {
            ratio = value / currentMoney;
            // Normalizacja: 1% -> 0.0, 100% -> 1.0
            ratio = Math.max(0.0, (ratio - 0.01) / (1.0 - 0.01));
            ratio = Math.min(1.0, ratio); // Ograniczenie do [0, 1]
        } else {
            // Jeśli gracz ma 0$, użyj minimalnej wartości
            ratio = 0.0;
        }

        // Interpolacja między ciemnozielonym a ciemnoczerwonym
        cachedBgColor = GameColors.interpolateColor(BG_GREEN_DARK, BG_RED_DARK, ratio);
        lastCachedValue = value;
        lastCachedMoney = currentMoney;
        return cachedBgColor;
    }

    public void setAutoSell(boolean enabled, double threshold) {
        this.autoSellEnabled = enabled;
        this.autoSellThreshold = threshold;

        // Synchronizuj z modelem Resource
        com.labinc.model.Resource resource = gameState.getResources().get(symbol);
        if (resource != null) {
            resource.setAutoSell(enabled, threshold);
        }

        // Powiadom listener o zmianie
        if (autoSellChangeListener != null) {
            autoSellChangeListener.onAutoSellChanged(symbol, enabled, threshold);
        }

        repaint();
    }

    public void setAutoSellChangeListener(AutoSellChangeListener listener) {
        this.autoSellChangeListener = listener;
    }

    public void setSellListener(SellListener listener) {
        this.sellListener = listener;
    }

    public boolean isAutoSellEnabled() {
        return autoSellEnabled;
    }

    public double getAutoSellThreshold() {
        return autoSellThreshold;
    }

    /**
     * Synchronizuje ustawienia auto-sell z Resource (używane przy wczytywaniu
     * zapisu)
     */
    public void syncAutoSellFromResource() {
        com.labinc.model.Resource resource = gameState.getResources().get(symbol);
        if (resource != null) {
            this.autoSellEnabled = resource.isAutoSellEnabled();
            this.autoSellThreshold = resource.getAutoSellThreshold();
        }
    }

    public String getSymbol() {
        return symbol;
    }
}
