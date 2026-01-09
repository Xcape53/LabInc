package com.labinc.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JPanel;
import javax.swing.Timer;

import com.labinc.model.GameState;
import com.labinc.model.Resource;
import com.labinc.util.Formatter;
import com.labinc.util.GameColors;

/**
 * Transparentny panel overlay do renderowania powiększonych elementów przy
 * hover
 */
public class HoverOverlayPanel extends JPanel {
    private final GameState gameState;
    private String hoveredSymbol = null;
    private int hoveredAtomicNumber = -1;
    private Point hoverPosition = null;

    private int tileWidth = 60; // Dynamiczny rozmiar z ElementTile
    private int tileHeight = 60; // Dynamiczny rozmiar z ElementTile
    private static final int REFERENCE_SIZE = 60;

    // Proporcje wysokości
    private static final double HEIGHT_1_BTN_RATIO = 1.83; // 110/60
    private static final double HEIGHT_2_BTN_RATIO = 2.25; // 135/60
    private static final double HEIGHT_3_BTN_RATIO = 2.66; // 160/60

    private static final int CORNER_RADIUS = 8;
    private static final int ANIMATION_DURATION_MS = 150; // 0.15s - szybsza animacja

    // Animacja
    private Timer animationTimer;
    private long animationStartTime;
    private float currentScale = 1.0f;
    private int currentHeight = 60;
    private int targetHeight = 60;

    // OPTYMALIZACJA: Cache koloru
    private Color cachedBgColor = null;
    private String lastColorSymbol = null;

    // Tracking pozycji przycisków dla kliknięć
    private Rectangle sellButtonBounds = null;
    private Rectangle autoSellButtonBounds = null;

    // Mapa ElementTile dla auto-sell
    private java.util.Map<String, ElementTile> elementTileMap = new java.util.HashMap<>();

    // Listener sprzedaży dla floating text
    private ElementTile.SellListener sellListener;

    private static final Color BG_INACTIVE = new Color(35, 35, 35);
    private static final Color BG_GREEN_DARK = new Color(15, 60, 25);
    private static final Color BG_RED_DARK = new Color(100, 15, 15);
    private static final Color TEXT_COLOR = new Color(220, 220, 220);
    private static final Color ORANGE = GameColors.ORANGE;

    // ====== AUTOSELL INPUT MODE ======
    private boolean isAutoSellInputMode = false;
    private String autoSellInputSymbol = null;
    private String mantissaBuffer = "1"; // Bufor tekstowy dla mantysy
    private String exponentBuffer = "3"; // Bufor tekstowy dla wykładnika
    private int activeInputField = 0; // 0 = mantissa, 1 = exponent
    private Rectangle okButtonBounds = null;
    private Rectangle cancelButtonBounds = null;
    private Rectangle mantissaFieldBounds = null;
    private Rectangle exponentFieldBounds = null;

    // Helper methods for parsing buffers
    private double parseMantissa() {
        if (mantissaBuffer.isEmpty())
            return 1.0;
        try {
            return Double.parseDouble(mantissaBuffer.replace(',', '.'));
        } catch (NumberFormatException e) {
            return 1.0;
        }
    }

    private int parseExponent() {
        if (exponentBuffer.isEmpty())
            return 0;
        try {
            return Math.min(99, Integer.parseInt(exponentBuffer));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public HoverOverlayPanel(GameState gameState) {
        this.gameState = gameState;
        setOpaque(false); // Transparentny
        setLayout(null); // Absolute positioning

        // Obsługa myszy
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                hideHover();
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                handleClick(e.getPoint());
            }
        });

        // Keyboard listener for autosell input mode
        setFocusable(true);
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (!isAutoSellInputMode)
                    return;

                switch (e.getKeyCode()) {
                    case java.awt.event.KeyEvent.VK_UP:
                        if (activeInputField == 0) {
                            double val = HoverOverlayPanel.this.parseMantissa();
                            val = Math.min(9.9, val + 0.1);
                            mantissaBuffer = String.format("%.1f", val).replace('.', ',');
                        } else {
                            int val = HoverOverlayPanel.this.parseExponent();
                            exponentBuffer = String.valueOf(Math.min(99, val + 1));
                        }
                        repaint();
                        break;
                    case java.awt.event.KeyEvent.VK_DOWN:
                        if (activeInputField == 0) {
                            double val = HoverOverlayPanel.this.parseMantissa();
                            val = Math.max(0.1, val - 0.1);
                            mantissaBuffer = String.format("%.1f", val).replace('.', ',');
                        } else {
                            int val = HoverOverlayPanel.this.parseExponent();
                            exponentBuffer = String.valueOf(Math.max(0, val - 1));
                        }
                        repaint();
                        break;
                    case java.awt.event.KeyEvent.VK_LEFT:
                    case java.awt.event.KeyEvent.VK_TAB:
                        if ((e.getModifiersEx() & java.awt.event.KeyEvent.SHIFT_DOWN_MASK) != 0) {
                            activeInputField = 0;
                        } else {
                            activeInputField = (activeInputField + 1) % 2;
                        }
                        repaint();
                        break;
                    case java.awt.event.KeyEvent.VK_RIGHT:
                        activeInputField = 1;
                        repaint();
                        break;
                    case java.awt.event.KeyEvent.VK_ENTER:
                        confirmAutoSellInput();
                        break;
                    case java.awt.event.KeyEvent.VK_ESCAPE:
                        cancelAutoSellInput();
                        break;
                    // Obsługa cyfr
                    case java.awt.event.KeyEvent.VK_0:
                    case java.awt.event.KeyEvent.VK_1:
                    case java.awt.event.KeyEvent.VK_2:
                    case java.awt.event.KeyEvent.VK_3:
                    case java.awt.event.KeyEvent.VK_4:
                    case java.awt.event.KeyEvent.VK_5:
                    case java.awt.event.KeyEvent.VK_6:
                    case java.awt.event.KeyEvent.VK_7:
                    case java.awt.event.KeyEvent.VK_8:
                    case java.awt.event.KeyEvent.VK_9:
                        char digit = (char) ('0' + (e.getKeyCode() - java.awt.event.KeyEvent.VK_0));
                        if (activeInputField == 0) {
                            // Mantisa: max 3 znaki (np. "9,9")
                            if (mantissaBuffer.length() < 3) {
                                mantissaBuffer += digit;
                            }
                        } else {
                            // Wykładnik: max 2 znaki
                            if (exponentBuffer.length() < 2) {
                                exponentBuffer += digit;
                            }
                        }
                        repaint();
                        break;
                    case java.awt.event.KeyEvent.VK_PERIOD:
                    case java.awt.event.KeyEvent.VK_COMMA:
                        if (activeInputField == 0 && !mantissaBuffer.contains(",") && mantissaBuffer.length() < 3) {
                            mantissaBuffer += ",";
                            repaint();
                        }
                        break;
                    case java.awt.event.KeyEvent.VK_BACK_SPACE:
                        if (activeInputField == 0 && mantissaBuffer.length() > 0) {
                            mantissaBuffer = mantissaBuffer.substring(0, mantissaBuffer.length() - 1);
                        } else if (activeInputField == 1 && exponentBuffer.length() > 0) {
                            exponentBuffer = exponentBuffer.substring(0, exponentBuffer.length() - 1);
                        }
                        repaint();
                        break;
                    case java.awt.event.KeyEvent.VK_DELETE:
                        if (activeInputField == 0) {
                            mantissaBuffer = "";
                        } else {
                            exponentBuffer = "";
                        }
                        repaint();
                        break;
                }
            }
        });

        // Event-driven update zamiast timera
        gameState.addListener(new GameState.GameEventListener() {
            @Override
            public void onResourceChanged(String resourceSymbol, double newAmount) {
                if (hoveredSymbol != null && hoveredSymbol.equals(resourceSymbol)) {
                    repaint();
                }
            }
        });
    }

    /**
     * Override contains() aby panel blokował mouse events TYLKO w obszarze hover
     * tile
     * Dzięki temu tile pod spodem nie otrzymują mouse events gdy mysz jest nad
     * hover
     */
    @Override
    public boolean contains(int x, int y) {
        if (hoveredSymbol == null || hoverPosition == null) {
            return false; // Nie blokuj - pozwól event przejść do tile pod spodem
        }

        // Oblicz bounds hover tile
        int hoverWidth = (int) (tileWidth * 1.33 * currentScale);
        int animHeight = currentHeight;
        int offsetX = (hoverWidth - tileWidth) / 2;
        int offsetY = (animHeight - tileHeight) / 2;
        int hoverX = hoverPosition.x - offsetX;
        int hoverY = hoverPosition.y - offsetY;

        // Rozszerz bounds o panel auto-sell gdy w trybie wprowadzania
        if (isAutoSellInputMode) {
            float fontScale = (tileWidth / (float) REFERENCE_SIZE);
            int inputPanelHeight = (int) (100 * fontScale);
            animHeight += inputPanelHeight + (int) (24 * fontScale); // Dodatkowa wysokość dla panelu + AUTO-SELL button
        }

        // Zwróć true TYLKO jeśli punkt jest w bounds hover tile (lub rozszerzonych
        // bounds)
        return x >= hoverX && x <= hoverX + hoverWidth &&
                y >= hoverY && y <= hoverY + animHeight;
    }

    /**
     * Wymusza ukrycie panelu autosell - wywoływane przy zmianie zakładki
     */
    public void forceHideAutoSellInput() {
        isAutoSellInputMode = false;
        autoSellInputSymbol = null;
        hoveredSymbol = null;
        hoverPosition = null;
        repaint();
    }

    /**
     * Ustawia listener sprzedaży dla floating text
     */
    public void setSellListener(ElementTile.SellListener listener) {
        this.sellListener = listener;
    }

    /**
     * Rejestruje ElementTile dla auto-sell
     */
    public void registerElementTile(String symbol, ElementTile tile) {
        elementTileMap.put(symbol, tile);
    }

    /**
     * Pokazuje hover dla danego elementu na określonej pozycji
     */
    public void showHover(String symbol, int atomicNumber, Point position, int width, int height) {
        this.hoveredSymbol = symbol;
        this.hoveredAtomicNumber = atomicNumber;
        this.hoverPosition = position;
        this.tileWidth = width;
        this.tileHeight = height;

        // Oblicz docelową wysokość na podstawie liczby przycisków
        int buttonCount = calculateButtonCount();
        targetHeight = getHeightForButtons(buttonCount);

        // Rozpocznij animację
        startAnimation();
    }

    /**
     * Ukrywa hover
     */
    public void hideHover() {
        // Nie ukrywaj gdy w trybie wprowadzania auto-sell
        if (isAutoSellInputMode) {
            return;
        }

        this.hoveredSymbol = null;
        this.hoveredAtomicNumber = -1;
        this.hoverPosition = null;
        currentScale = 1.0f;
        currentHeight = tileHeight;
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        repaint();
    }

    /**
     * Rozpoczyna płynną animację scale
     */
    private void startAnimation() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }

        // Reset cache koloru przy nowym hover
        cachedBgColor = null;
        lastColorSymbol = null;

        animationStartTime = System.currentTimeMillis();

        animationTimer = new Timer(16, e -> { // ~60 FPS
            long elapsed = System.currentTimeMillis() - animationStartTime;
            double progress = Math.min(1.0, (double) elapsed / ANIMATION_DURATION_MS);

            // Prosta liniowa interpolacja (szybsza niż easing)
            currentScale = 1.0f + (0.33f * (float) progress);
            currentHeight = tileHeight + (int) ((targetHeight - tileHeight) * progress);

            repaint();

            // Zakończ animację
            if (progress >= 1.0) {
                ((Timer) e.getSource()).stop();
            }
        });
        animationTimer.start();
    }

    /**
     * Oblicza liczbę przycisków do wyświetlenia
     */
    private int calculateButtonCount() {
        if (hoveredSymbol == null)
            return 1;
        Resource resource = gameState.getResources().get(hoveredSymbol);
        if (resource == null)
            return 1;

        int count = 0;

        // Przycisk SPRZEDAJ (jeśli amount > 0)
        if (resource.getAmount() > 0) {
            count++;
        }

        // Przycisk AUTO-SELL (zawsze)
        count++;

        return Math.max(1, count);
    }

    /**
     * Zwraca wysokość dla danej liczby przycisków
     */
    /**
     * Zwraca wysokość dla danej liczby przycisków - skalowana
     */
    private int getHeightForButtons(int buttonCount) {
        // Skalowanie wysokości przycisków w zależności od rozmiaru kafla
        double ratio;
        switch (buttonCount) {
            case 1:
                ratio = HEIGHT_1_BTN_RATIO;
                break;
            case 2:
                ratio = HEIGHT_2_BTN_RATIO;
                break;
            case 3:
                ratio = HEIGHT_3_BTN_RATIO;
                break;
            default:
                ratio = HEIGHT_3_BTN_RATIO;
                break;
        }
        return (int) (tileHeight * ratio); // tileHeight jest równy tileWidth (kwadrat)
    }

    // ====== NOTIFICATION ======
    private String notifTitle = null;
    private String notifDesc = null;
    private float notifY = -100; // Increased hidden offset
    private Timer notifTimer;
    private long notifStartTime;
    private Color notifBgColor = new Color(34, 197, 94); // Domyślnie zielony
    private Color notifBorderColor = new Color(20, 120, 45); // Domyślnie ciemnozielony
    private static final int NOTIF_DISPLAY_TIME = 2500;
    private static final int NOTIF_SLIDE_TIME = 500;

    public void showNotification(String title, String description) {
        showNotification(title, description, new Color(34, 197, 94), new Color(20, 120, 45)); // Zielony
    }

    public void showNotification(String title, String description, Color bgColor, Color borderColor) {
        this.notifTitle = title;
        this.notifDesc = description;
        this.notifBgColor = bgColor;
        this.notifBorderColor = borderColor;
        this.notifStartTime = System.currentTimeMillis();

        if (notifTimer != null && notifTimer.isRunning())
            notifTimer.stop();

        notifTimer = new Timer(16, e -> {
            long elapsed = System.currentTimeMillis() - notifStartTime;

            if (elapsed < NOTIF_SLIDE_TIME) {
                // Slide In
                float progress = (float) elapsed / NOTIF_SLIDE_TIME;
                // Ease out
                progress = 1.0f - (1.0f - progress) * (1.0f - progress);
                notifY = -100 + (160 * progress); // Target 60 (-100 + 160)
            } else if (elapsed < NOTIF_DISPLAY_TIME) {
                // Hold
                notifY = 60;
            } else if (elapsed < NOTIF_DISPLAY_TIME + NOTIF_SLIDE_TIME) {
                // Slide Out
                float progress = (float) (elapsed - NOTIF_DISPLAY_TIME) / NOTIF_SLIDE_TIME;
                // Ease in
                progress = progress * progress;
                notifY = 60 - (160 * progress); // Target -100
            } else {
                // End
                notifY = -100;
                ((Timer) e.getSource()).stop();
                notifTitle = null;
            }
            repaint();
        });
        notifTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw notification always on top
        if (notifTitle != null) {
            Graphics2D g2n = (Graphics2D) g.create();
            g2n.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int boxW = 400; // Bigger box
            int boxH = 80; // Bigger box
            int x = (getWidth() - boxW) / 2;
            int y = (int) notifY;

            // Shadow
            g2n.setColor(new Color(0, 0, 0, 100));
            g2n.fillRoundRect(x + 4, y + 4, boxW, boxH, 15, 15);

            // Background
            g2n.setColor(notifBgColor);
            g2n.fillRoundRect(x, y, boxW, boxH, 15, 15);

            // Border
            g2n.setColor(notifBorderColor);
            g2n.setStroke(new BasicStroke(2));
            g2n.drawRoundRect(x, y, boxW, boxH, 15, 15);

            // Text - Centered
            g2n.setColor(Color.WHITE);
            g2n.setFont(new Font("Arial", Font.BOLD, 18)); // Larger font
            FontMetrics fm = g2n.getFontMetrics();
            int titleW = fm.stringWidth(notifTitle);
            g2n.drawString(notifTitle, x + (boxW - titleW) / 2, y + 35);

            g2n.setFont(new Font("Arial", Font.PLAIN, 14)); // Larger font
            fm = g2n.getFontMetrics();
            int descW = fm.stringWidth(notifDesc != null ? notifDesc : "");
            g2n.drawString(notifDesc != null ? notifDesc : "", x + (boxW - descW) / 2, y + 60);

            g2n.dispose();
        }

        if (hoveredSymbol == null || hoverPosition == null) {
            return; // Nic do renderowania (poza powiadomieniem)
        }

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Resource resource = gameState.getResources().get(hoveredSymbol);
        if (resource == null) {
            g2d.dispose();
            return;
        }

        // Oblicz rozmiar z animacją
        int animWidth = (int) (tileWidth * 1.33 * currentScale);
        int animHeight = currentHeight;

        // Oblicz skalę dla tekstów (bazując na szerokości kafla, nie animacji)
        // Używamy tileWidth, bo animWidth zmienia się w czasie animacji, co
        // powodowałoby "puchnięcie" tekstu
        // Jednak chcemy, żeby tekst był czytelny, więc może lekko skalować z animWidth?
        // Decyzja: Skalujemy tekst bazując na tileWidth (niezmienne w trakcie hover),
        // ale powiększamy trochę bo hover jest większy (1.33x)
        float fontScale = (tileWidth / (float) REFERENCE_SIZE);

        // Oblicz pozycję z wycentrowaniem
        int offsetX = (animWidth - tileWidth) / 2;
        int offsetY = (animHeight - tileHeight) / 2;
        int x = hoverPosition.x - offsetX;
        int y = hoverPosition.y - offsetY;

        // Oblicz kolor na podstawie wartości
        Color bgColor = calculateColor(resource);

        // Rysuj powiększony element
        RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, animWidth - 1, animHeight - 1,
                CORNER_RADIUS, CORNER_RADIUS);
        g2d.setColor(bgColor);
        g2d.fill(rect);

        // Obramowanie pomarańczowe (hover)
        g2d.setColor(ORANGE);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(rect);

        int yOffset = y + (int) (12 * fontScale);

        // Numer atomowy
        g2d.setColor(new Color(180, 180, 180));
        g2d.setFont(new Font("Arial", Font.PLAIN, Math.max(8, (int) (9 * fontScale))));
        g2d.drawString(String.valueOf(hoveredAtomicNumber), x + (int) (4 * fontScale), yOffset);
        yOffset += Math.max(12, (int) (14 * fontScale));

        // Symbol (większa czcionka)
        g2d.setFont(new Font("Arial", Font.BOLD, Math.max(12, (int) (24 * fontScale))));
        g2d.setColor(TEXT_COLOR);
        FontMetrics fm = g2d.getFontMetrics();
        int symbolWidth = fm.stringWidth(hoveredSymbol);
        g2d.drawString(hoveredSymbol, x + (animWidth - symbolWidth) / 2, yOffset);
        // Zwiększony odstęp po symbolu, żeby nie nachodził na wagę
        yOffset += Math.max(16, (int) (24 * fontScale));

        // Ilość
        g2d.setFont(new Font("Arial", Font.PLAIN, Math.max(8, (int) (10 * fontScale))));
        g2d.setColor(TEXT_COLOR);
        String amountStr = Formatter.formatAmount(resource.getAmount()) + " kg";
        int amountWidth = g2d.getFontMetrics().stringWidth(amountStr);
        g2d.drawString(amountStr, x + (animWidth - amountWidth) / 2, yOffset);
        yOffset += Math.max(12, (int) (14 * fontScale));

        // Wartość
        g2d.setFont(new Font("Arial", Font.BOLD, Math.max(9, (int) (11 * fontScale))));
        g2d.setColor(new Color(120, 255, 120));
        String valueStr = "$" + Formatter.formatAmount(resource.getTotalValue());
        int valueWidth = g2d.getFontMetrics().stringWidth(valueStr);
        g2d.drawString(valueStr, x + (animWidth - valueWidth) / 2, yOffset);
        yOffset += Math.max(14, (int) (18 * fontScale));

        // Przyciski
        int btnPadding = (int) (6 * fontScale);
        int btnWidth = animWidth - (2 * btnPadding);
        int btnHeight = (int) (20 * fontScale);

        // Reset bounds
        sellButtonBounds = null;
        autoSellButtonBounds = null;

        if (resource.getAmount() > 0) {
            sellButtonBounds = new Rectangle(x + btnPadding, yOffset, btnWidth, btnHeight);
            drawButton(g2d, "SPRZEDAJ", x + btnPadding, yOffset, btnWidth, btnHeight, ORANGE, Color.BLACK, fontScale);
            yOffset += (btnHeight + (int) (4 * fontScale));
        }

        // Przycisk Auto-sell
        autoSellButtonBounds = new Rectangle(x + btnPadding, yOffset, btnWidth, btnHeight);
        drawButton(g2d, "AUTO-SELL", x + btnPadding, yOffset, btnWidth, btnHeight,
                new Color(60, 60, 60), TEXT_COLOR, fontScale);
        yOffset += (btnHeight + (int) (4 * fontScale));

        // Panel wprowadzania auto-sell (jeśli aktywny)
        if (isAutoSellInputMode && autoSellInputSymbol != null && autoSellInputSymbol.equals(hoveredSymbol)) {
            drawAutoSellInputPanel(g2d, x, yOffset, animWidth, fontScale);
        }

        g2d.dispose();
    }

    private void drawAutoSellInputPanel(Graphics2D g2d, int x, int yOffset, int animWidth, float fontScale) {
        int panelHeight = (int) (100 * fontScale);
        int padding = (int) (6 * fontScale);

        // Tło panelu
        g2d.setColor(new Color(30, 30, 30, 240));
        g2d.fillRoundRect(x + 2, yOffset, animWidth - 4, panelHeight, 8, 8);
        g2d.setColor(ORANGE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x + 2, yOffset, animWidth - 4, panelHeight, 8, 8);

        int innerY = yOffset + padding;

        // Tytuł
        g2d.setFont(new Font("Arial", Font.BOLD, Math.max(9, (int) (11 * fontScale))));
        g2d.setColor(TEXT_COLOR);
        String title = "Auto-sprzedaż przy:";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(title, x + (animWidth - fm.stringWidth(title)) / 2, innerY + fm.getAscent());
        innerY += fm.getHeight() + padding;

        // Pola wprowadzania: [ mantisa ] e [ exp ]
        int fieldWidth = (int) (35 * fontScale);
        int fieldHeight = (int) (22 * fontScale);
        int totalWidth = fieldWidth * 2 + (int) (20 * fontScale); // dwa pola + "e"
        int startX = x + (animWidth - totalWidth) / 2;

        // Pole mantisy
        mantissaFieldBounds = new Rectangle(startX, innerY, fieldWidth, fieldHeight);
        g2d.setColor(activeInputField == 0 ? new Color(80, 80, 80) : new Color(50, 50, 50));
        g2d.fillRoundRect(startX, innerY, fieldWidth, fieldHeight, 5, 5);
        if (activeInputField == 0) {
            g2d.setColor(ORANGE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(startX, innerY, fieldWidth, fieldHeight, 5, 5);
        }
        g2d.setFont(new Font("Arial", Font.BOLD, Math.max(10, (int) (14 * fontScale))));
        g2d.setColor(TEXT_COLOR);
        String mantissaStr = mantissaBuffer.isEmpty() ? "0" : mantissaBuffer;
        fm = g2d.getFontMetrics();
        g2d.drawString(mantissaStr, startX + (fieldWidth - fm.stringWidth(mantissaStr)) / 2,
                innerY + (fieldHeight + fm.getAscent()) / 2 - 2);

        // "e"
        int eX = startX + fieldWidth + (int) (4 * fontScale);
        g2d.setFont(new Font("Arial", Font.BOLD, Math.max(10, (int) (14 * fontScale))));
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("e", eX, innerY + (fieldHeight + fm.getAscent()) / 2 - 2);

        // Pole wykładnika
        int expX = eX + (int) (12 * fontScale);
        exponentFieldBounds = new Rectangle(expX, innerY, fieldWidth, fieldHeight);
        g2d.setColor(activeInputField == 1 ? new Color(80, 80, 80) : new Color(50, 50, 50));
        g2d.fillRoundRect(expX, innerY, fieldWidth, fieldHeight, 5, 5);
        if (activeInputField == 1) {
            g2d.setColor(ORANGE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(expX, innerY, fieldWidth, fieldHeight, 5, 5);
        }
        g2d.setColor(TEXT_COLOR);
        String expStr = exponentBuffer.isEmpty() ? "0" : exponentBuffer;
        g2d.drawString(expStr, expX + (fieldWidth - fm.stringWidth(expStr)) / 2,
                innerY + (fieldHeight + fm.getAscent()) / 2 - 2);

        innerY += fieldHeight + padding;

        // Podgląd wartości
        double previewValue = parseMantissa() * Math.pow(10, parseExponent());
        String previewStr = "= $" + Formatter.formatAmount(previewValue);
        g2d.setFont(new Font("Arial", Font.PLAIN, Math.max(8, (int) (10 * fontScale))));
        g2d.setColor(new Color(120, 255, 120));
        fm = g2d.getFontMetrics();
        g2d.drawString(previewStr, x + (animWidth - fm.stringWidth(previewStr)) / 2, innerY + fm.getAscent());
        innerY += fm.getHeight() + padding;

        // Przyciski OK i Anuluj
        int btnWidth = (int) (40 * fontScale);
        int btnHeight = (int) (18 * fontScale);
        int btnGap = (int) (8 * fontScale);
        int btnStartX = x + (animWidth - btnWidth * 2 - btnGap) / 2;

        // OK
        okButtonBounds = new Rectangle(btnStartX, innerY, btnWidth, btnHeight);
        g2d.setColor(new Color(34, 197, 94));
        g2d.fillRoundRect(btnStartX, innerY, btnWidth, btnHeight, 5, 5);
        g2d.setFont(new Font("Arial", Font.BOLD, Math.max(8, (int) (10 * fontScale))));
        g2d.setColor(Color.BLACK);
        fm = g2d.getFontMetrics();
        g2d.drawString("OK", btnStartX + (btnWidth - fm.stringWidth("OK")) / 2,
                innerY + (btnHeight + fm.getAscent()) / 2 - 2);

        // Anuluj (czerwony)
        int cancelX = btnStartX + btnWidth + btnGap;
        cancelButtonBounds = new Rectangle(cancelX, innerY, btnWidth, btnHeight);
        g2d.setColor(new Color(220, 53, 69)); // Czerwony
        g2d.fillRoundRect(cancelX, innerY, btnWidth, btnHeight, 5, 5);
        g2d.setColor(Color.WHITE);
        g2d.drawString("X", cancelX + (btnWidth - fm.stringWidth("X")) / 2,
                innerY + (btnHeight + fm.getAscent()) / 2 - 2);
    }

    private Color calculateColor(Resource resource) {
        // OPTYMALIZACJA: Użyj cache jeśli symbol ten sam
        if (cachedBgColor != null && hoveredSymbol != null && hoveredSymbol.equals(lastColorSymbol)) {
            return cachedBgColor;
        }

        double amount = resource.getAmount();
        if (amount <= 0) {
            // Sprawdź czy surowiec jest produkowany
            boolean isProduced = gameState.isResourceBeingProduced(resource.getSymbol());

            if (isProduced) {
                // Surowiec jest produkowany ale ma 0 (np. autosell) - pokaż zielony
                cachedBgColor = BG_GREEN_DARK;
                lastColorSymbol = hoveredSymbol;
                return BG_GREEN_DARK;
            } else {
                cachedBgColor = BG_INACTIVE;
                lastColorSymbol = hoveredSymbol;
                return BG_INACTIVE;
            }
        }

        double value = resource.getTotalValue();
        double currentMoney = gameState.getMoney();

        double ratio;
        if (currentMoney > 0) {
            ratio = value / currentMoney;
            ratio = Math.max(0.0, (ratio - 0.01) / (1.0 - 0.01));
            ratio = Math.min(1.0, ratio);
        } else {
            ratio = 0.0;
        }

        cachedBgColor = GameColors.interpolateColor(BG_GREEN_DARK, BG_RED_DARK, ratio);
        lastColorSymbol = hoveredSymbol;
        return cachedBgColor;
    }

    /**
     * Obsługuje kliknięcie na hover tile
     */
    private void handleClick(Point clickPoint) {
        if (hoveredSymbol == null) {
            return;
        }

        Resource resource = gameState.getResources().get(hoveredSymbol);
        if (resource == null) {
            return;
        }

        // Sprawdź kliknięcie w SPRZEDAJ
        if (sellButtonBounds != null && sellButtonBounds.contains(clickPoint)) {
            double value = resource.getTotalValue();
            gameState.sellResource(hoveredSymbol, resource.getAmount());

            // Powiadom listener o sprzedaży (floating text)
            if (sellListener != null && hoverPosition != null) {
                try {
                    // Oblicz pozycję nad hover tile
                    int hoverWidth = (int) (tileWidth * 1.33 * currentScale);
                    int offsetX = (hoverWidth - tileWidth) / 2;
                    int offsetY = (currentHeight - tileHeight) / 2;
                    int hoverX = hoverPosition.x - offsetX;
                    int hoverY = hoverPosition.y - offsetY;

                    java.awt.Point loc = getLocationOnScreen();
                    loc.x += hoverX + hoverWidth / 2;
                    loc.y += hoverY - 10; // Nad górną krawędzią hover tile
                    sellListener.onSell(hoveredSymbol, value, loc);
                } catch (Exception e) {
                    // Ignoruj jeśli panel nie jest widoczny
                }
            }
            repaint();
            return;
        }

        // Sprawdź kliknięcie w AUTO-SELL
        if (autoSellButtonBounds != null && autoSellButtonBounds.contains(clickPoint)) {
            ElementTile tile = elementTileMap.get(hoveredSymbol);
            if (tile == null) {
                return;
            }

            // Aktywuj tryb wprowadzania auto-sell
            isAutoSellInputMode = true;
            autoSellInputSymbol = hoveredSymbol;

            // Ustaw wartości początkowe na podstawie istniejącego progu
            double currentThreshold = tile.getAutoSellThreshold();
            if (currentThreshold > 0) {
                int exp = (int) Math.floor(Math.log10(currentThreshold));
                double mant = currentThreshold / Math.pow(10, exp);
                mant = Math.round(mant * 10) / 10.0; // Zaokrąglij do 1 miejsca po przecinku
                mantissaBuffer = String.format("%.1f", mant).replace('.', ',');
                exponentBuffer = String.valueOf(exp);
            } else {
                mantissaBuffer = "1";
                exponentBuffer = "3";
            }
            activeInputField = 0;

            requestFocusInWindow();
            repaint();
            return;
        }

        // Sprawdź kliknięcie w trybie input mode
        if (isAutoSellInputMode) {
            if (okButtonBounds != null && okButtonBounds.contains(clickPoint)) {
                confirmAutoSellInput();
                return;
            }
            if (cancelButtonBounds != null && cancelButtonBounds.contains(clickPoint)) {
                cancelAutoSellInput();
                return;
            }
            // Kliknięcie w pola - zmiana aktywnego pola
            if (mantissaFieldBounds != null && mantissaFieldBounds.contains(clickPoint)) {
                activeInputField = 0;
                repaint();
                return;
            }
            if (exponentFieldBounds != null && exponentFieldBounds.contains(clickPoint)) {
                activeInputField = 1;
                repaint();
            }
        }
    }

    private void confirmAutoSellInput() {
        if (!isAutoSellInputMode || autoSellInputSymbol == null)
            return;

        ElementTile tile = elementTileMap.get(autoSellInputSymbol);
        if (tile != null) {
            double threshold = parseMantissa() * Math.pow(10, parseExponent());
            tile.setAutoSell(true, threshold);
        }

        isAutoSellInputMode = false;
        autoSellInputSymbol = null;
        repaint();
    }

    private void cancelAutoSellInput() {
        isAutoSellInputMode = false;
        autoSellInputSymbol = null;
        repaint();
    }

    /**
     * Sprawdza czy mysz jest nad hover tile
     */
    public boolean isMouseOverHover(Point mousePos) {
        if (hoveredSymbol == null || hoverPosition == null) {
            return false;
        }

        int animWidth = (int) (tileWidth * 1.33 * currentScale);
        int animHeight = currentHeight;
        int offsetX = (animWidth - tileWidth) / 2;
        int offsetY = (animHeight - tileHeight) / 2;
        int x = hoverPosition.x - offsetX;
        int y = hoverPosition.y - offsetY;

        return mousePos.x >= x && mousePos.x <= x + animWidth &&
                mousePos.y >= y && mousePos.y <= y + animHeight;
    }

    /**
     * Rysuje przycisk
     */
    /**
     * Rysuje przycisk
     */
    private void drawButton(Graphics2D g2d, String text, int x, int y, int width, int height,
            Color bgColor, Color textColor, float fontScale) {
        RoundRectangle2D btn = new RoundRectangle2D.Double(x, y, width, height, 6, 6);
        g2d.setColor(bgColor);
        g2d.fill(btn);
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(btn);

        g2d.setColor(textColor);
        g2d.setFont(new Font("Arial", Font.BOLD, Math.max(8, (int) (11 * fontScale))));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textX = x + (width - textWidth) / 2;
        int textY = y + ((height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, textX, textY);
    }
}
