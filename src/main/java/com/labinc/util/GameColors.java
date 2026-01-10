package com.labinc.util;

import java.awt.Color;

/**
 * Centralne stałe kolorów używane w całej aplikacji.
 * Zapewnia spójność wizualną i łatwość modyfikacji.
 */
public final class GameColors {

    private GameColors() {
    } // Prevent instantiation

    // === Tła główne ===
    public static final Color BG_DARK = new Color(35, 35, 35);
    public static final Color BG_SIDEBAR = new Color(38, 43, 48);

    // === Akcenty ===
    public static final Color ACCENT_GREEN = new Color(0, 200, 83);
    public static final Color ACCENT_RED = new Color(220, 53, 69);
    public static final Color ACCENT_ORANGE = new Color(255, 152, 0);
    public static final Color ACCENT_BLUE = new Color(33, 150, 243);

    // === Tekst ===
    public static final Color TEXT_BRIGHT = new Color(230, 230, 230);
    public static final Color TEXT_SECONDARY = new Color(150, 150, 150);
    public static final Color TEXT_MUTED = new Color(100, 100, 100);

    // === Panele i karty ===
    public static final Color PANEL_DARK = new Color(45, 45, 45);
    public static final Color PANEL_HIGHLIGHT = new Color(55, 55, 55);

    // === Przyciski ===
    public static final Color BUTTON_ENABLED = new Color(50, 130, 50);
    public static final Color BUTTON_DISABLED = new Color(80, 80, 80);
    public static final Color BUTTON_HOVER = new Color(60, 150, 60);

    // === Kolory UI (używane w wielu komponentach) ===
    public static final Color ORANGE = new Color(255, 120, 15);
    public static final Color ORANGE_HOVER = new Color(185, 90, 25);
    public static final Color ORANGE_GLOW = new Color(255, 150, 50);

    public static final Color TEXT_LIGHT = new Color(220, 220, 220);
    public static final Color TEXT_DIM = new Color(150, 150, 150);

    public static final Color BTN_GREEN = new Color(34, 197, 94);
    public static final Color BTN_DISABLED_BG = new Color(100, 100, 100);
    public static final Color BTN_DISABLED_FG = new Color(70, 70, 70);

    public static final Color CARD_BG = new Color(45, 50, 55);
    public static final Color CARD_BG_DARK = new Color(64, 64, 64);
    public static final Color CARD_BORDER = new Color(60, 65, 70);

    /**
     * Interpoluje kolor między dwoma kolorami na podstawie wskaźnika.
     * 
     * @param c1    kolor początkowy (ratio = 0)
     * @param c2    kolor końcowy (ratio = 1)
     * @param ratio wartość od 0.0 do 1.0
     * @return interpolowany kolor
     */
    public static Color interpolateColor(Color c1, Color c2, double ratio) {
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * ratio);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * ratio);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * ratio);
        return new Color(r, g, b);
    }
}
