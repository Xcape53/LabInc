package com.labinc.gui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Panel mgły (Fog) z logiką pasmową i ANIMOWANYM PRZESUWANIEM GRANICY.
 * Zmiany:
 * - Dodano interpolację (Lerp) dla LOCKED_ZONE_START_X.
 * - Mgła "odsuwa się" płynnie po odblokowaniu fabryki.
 */
public class FogPanel extends JPanel {
    private enum CloudType {
        SMALL, MEDIUM, LARGE
    }

    private static final List<Cloud> WORLD_CLOUDS = new ArrayList<>();
    private static final int WORLD_WIDTH = 10000;

    // Zmienne do animacji granicy
    private static float currentLockedX = 0;
    private static float targetLockedX = 0;
    private static boolean positionInitialized = false;

    private static Timer ANIMATION_TIMER;
    private static boolean initialized = false;

    private final int worldOffsetX;
    private Timer repaintTimer;

    public FogPanel(int worldOffsetX) {
        this.worldOffsetX = worldOffsetX;
        setOpaque(false);
        initWorldIfNeeded();
    }

    private static void initWorldIfNeeded() {
        if (initialized)
            return;
        initialized = true;

        Random random = new Random();
        WORLD_CLOUDS.clear();

        // 1. DUŻE (Baza)
        for (int i = 0; i < 30; i++) {
            WORLD_CLOUDS.add(new Cloud(random, CloudType.LARGE));
        }

        // 2. ŚREDNIE (Pas przejściowy)
        for (int i = 0; i < 400; i++) {
            WORLD_CLOUDS.add(new Cloud(random, CloudType.MEDIUM));
        }

        // 3. MAŁE (Pas graniczny)
        for (int i = 0; i < 1250; i++) {
            WORLD_CLOUDS.add(new Cloud(random, CloudType.SMALL));
        }

        // 30 FPS - wystarczy dla płynnej mgły
        ANIMATION_TIMER = new Timer(33, e -> updateWorldClouds());
        ANIMATION_TIMER.start();
    }

    public static void setLockedZoneStartX(int x) {
        float oldX = currentLockedX;
        targetLockedX = x;

        if (!positionInitialized) {
            currentLockedX = x;
            positionInitialized = true;
        } else if (x > oldX) {
            // Granica się przesuwa w prawo - rozpocznij fade dla chmur w nowej strefie
            for (Cloud c : WORLD_CLOUDS) {
                // Chmury między starą a nową granicą zaczynają fadeout (te które zostały
                // odsłonięte)
                if (c.x >= oldX - 200 && c.x < x) {
                    // Wszystkie typy chmur znikają całkowicie po odblokowaniu
                    c.fadeTarget = 0.0f;
                }

                // Upewnij się, że chmury w nowej strefie mgły są widoczne (na wypadek gdyby
                // były ukryte)
                if (c.x >= x) {
                    c.fadeTarget = 1.0f;
                }
            }
        }
    }

    private static void updateWorldClouds() {
        // --- Płynna animacja granicy (gradient tła) ---
        float diff = targetLockedX - currentLockedX;
        if (Math.abs(diff) > 0.5f) {
            currentLockedX += diff * 0.12f; // Szybszy lerp dla gradientu
        } else {
            currentLockedX = targetLockedX;
        }

        // --- Ruch chmur i animacja fade ---
        for (Cloud c : WORLD_CLOUDS) {
            c.x += c.speed * 0.5f;

            if (c.x > WORLD_WIDTH) {
                c.x = -c.width;
                c.fadeTarget = 1.0f; // Reset widoczności po zawinięciu
                c.fadeProgress = 1.0f;
            }
            if (c.x < -c.width - 1000) {
                c.x = WORLD_WIDTH;
                c.fadeTarget = 1.0f; // Reset widoczności po zawinięciu
                c.fadeProgress = 1.0f;
            }

            // Płynna animacja fade (każda chmura osobno)
            if (Math.abs(c.fadeProgress - c.fadeTarget) > 0.01f) {
                c.fadeProgress += (c.fadeTarget - c.fadeProgress) * 0.05f; // Wolny, płynny fade
            } else {
                c.fadeProgress = c.fadeTarget;
            }
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        setupRepaintTimer();
    }

    private void setupRepaintTimer() {
        if (repaintTimer == null) {
            // Użyj FPS z ustawień
            int interval = com.labinc.model.GameSettings.getInstance().getFogTimerInterval();
            repaintTimer = new Timer(interval, e -> repaint());
            repaintTimer.start();
        }
    }

    @Override
    public void removeNotify() {
        if (repaintTimer != null) {
            repaintTimer.stop();
            repaintTimer = null;
        }
        super.removeNotify();
    }

    @Override
    public boolean contains(int x, int y) {
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Sprawdź czy mgła jest włączona w ustawieniach
        com.labinc.model.GameSettings settings = com.labinc.model.GameSettings.getInstance();
        if (!settings.isCloudsEnabled()) {
            return; // Nie rysuj mgły jeśli wyłączona
        }

        Graphics2D g2 = (Graphics2D) g;
        // Wyłącz antyaliasing dla lepszej wydajności - mgła nie potrzebuje gładkich
        // krawędzi
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        int myWidth = getWidth();
        int myHeight = getHeight();

        // Używamy aktualnej (animowanej) pozycji granicy - float dla płynności!
        float visibleLockedX = currentLockedX;

        // --- 1. Rysowanie Tła Mgły (Base Fog Gradient) ---
        int gradientOffset = 105;
        float gradientGlobalStart = visibleLockedX + gradientOffset;
        float gradientLocalX = gradientGlobalStart - worldOffsetX;

        if (gradientLocalX < myWidth) {
            Color transparentFog = new Color(230, 230, 230, 0);
            Color solidFog = new Color(230, 230, 230, 255);

            int drawStart = Math.max(0, (int) gradientLocalX);

            GradientPaint gp = new GradientPaint(
                    gradientLocalX, 0, transparentFog,
                    gradientLocalX + 400, 0, solidFog);

            g2.setPaint(gp);
            g2.fillRect(drawStart, 0, myWidth - drawStart, myHeight);
        }

        // --- 2. Rysowanie Chmur ---
        // Zakres widoczności na ekranie
        int viewStartX = worldOffsetX - 200; // Mały margines
        int viewEndX = worldOffsetX + myWidth + 200;

        // Użyj gęstości chmur z ustawień
        int cloudDensity = settings.getCloudDensity();
        int cloudIndex = 0;

        for (Cloud c : WORLD_CLOUDS) {
            // Pomijanie chmur na podstawie gęstości (10-100%)
            cloudIndex++;
            if (cloudDensity < 100 && (cloudIndex % 100) >= cloudDensity)
                continue;

            // Szybkie odrzucenie chmur poza ekranem
            if (c.x + c.width < viewStartX || c.x > viewEndX)
                continue;

            double localX = c.x - worldOffsetX;
            float dist = (float) (c.x - visibleLockedX); // Float dla płynnej interpolacji

            // OPTYMALIZACJA: Nie rysuj detali głęboko w mgle
            if (c.type == CloudType.SMALL && dist > 550)
                continue;
            if (c.type == CloudType.MEDIUM && dist > 1100)
                continue;

            float alphaFactor = 0.0f;

            switch (c.type) {
                case SMALL:
                    if (dist < -105)
                        alphaFactor = 0.0f;
                    else if (dist < 100)
                        alphaFactor = (float) ((dist + 105) / 205.0);
                    else if (dist < 300)
                        alphaFactor = 1.0f;
                    else if (dist < 500)
                        alphaFactor = 1.0f - (float) ((dist - 300) / 200.0);
                    else
                        alphaFactor = 0.0f;
                    break;

                case MEDIUM:
                    if (dist < 95)
                        alphaFactor = 0.0f;
                    else if (dist < 295)
                        alphaFactor = (float) ((dist - 95) / 200.0);
                    else if (dist < 695)
                        alphaFactor = 1.0f;
                    else
                        alphaFactor = 1.0f;
                    break;

                case LARGE:
                    if (dist < 495)
                        alphaFactor = 0.0f;
                    else if (dist < 895)
                        alphaFactor = (float) ((dist - 495) / 400.0);
                    else
                        alphaFactor = 1.0f;
                    break;
            }

            if (alphaFactor <= 0.01f)
                continue;

            // Połącz alphaFactor z indywidualnym fadeProgress chmury
            float finalAlpha = alphaFactor * c.fadeProgress;
            if (finalAlpha <= 0.01f)
                continue;

            int newAlpha = (int) (c.baseAlpha * finalAlpha);
            if (newAlpha <= 0)
                continue;

            g2.setColor(new Color(c.grey, c.grey, c.grey, newAlpha));
            g2.fillOval((int) localX, (int) c.y, (int) c.width, (int) c.height);
        }
    }

    private static class Cloud {
        double x, y;
        double width, height;
        double speed;
        int grey; // Wartość szarości (200-255)
        int baseAlpha; // Bazowa przezroczystość (80-200)
        CloudType type;
        float fadeProgress = 1.0f; // 1.0 = pełna widoczność, 0.0 = niewidoczna
        float fadeTarget = 1.0f; // Cel animacji fade

        Cloud(Random random, CloudType type) {
            this.type = type;

            speed = (random.nextDouble() - 0.5) * 0.8;
            if (Math.abs(speed) < 0.15)
                speed = 0.15 * Math.signum(speed);

            grey = 200 + random.nextInt(55);
            baseAlpha = 80 + random.nextInt(120);

            y = random.nextInt(1000) - 150;

            switch (type) {
                case LARGE:
                    height = 300 + random.nextInt(300);
                    width = height * (2.5 + random.nextDouble() * 2.0);
                    y = random.nextInt(1200) - 400;
                    break;

                case MEDIUM:
                    height = 100 + random.nextInt(150);
                    width = height * (2.0 + random.nextDouble() * 1.5);
                    break;

                case SMALL:
                default:
                    height = 60 + random.nextInt(80);
                    width = height * (2.0 + random.nextDouble() * 2.0);
                    break;
            }

            x = random.nextInt(WORLD_WIDTH);
        }
    }
}