package com.labinc.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.labinc.model.GameState;
import com.labinc.util.GameColors;

/**
 * Panel z interaktywną tablicą Mendelejewa
 */
public class PeriodicTablePanel extends JPanel {
    private final GameState gameState;
    private final Map<String, ElementTile> elementTiles = new HashMap<>();

    private final JPanel gridPanel;
    private HoverOverlayPanel hoverOverlay;
    private javax.swing.JFrame parentFrame;

    // Pozycje pierwiastków [rzadowy, kolumna] - kolumny 0-indexed (0-17)
    private static final Object[][] ELEMENT_POSITIONS = {
            // Okres 1
            { "H", 1, 0, 0 }, { "He", 2, 0, 17 },
            // Okres 2
            { "Li", 3, 1, 0 }, { "Be", 4, 1, 1 }, { "B", 5, 1, 12 }, { "C", 6, 1, 13 },
            { "N", 7, 1, 14 }, { "O", 8, 1, 15 }, { "F", 9, 1, 16 }, { "Ne", 10, 1, 17 },
            // Okres 3
            { "Na", 11, 2, 0 }, { "Mg", 12, 2, 1 }, { "Al", 13, 2, 12 }, { "Si", 14, 2, 13 },
            { "P", 15, 2, 14 }, { "S", 16, 2, 15 }, { "Cl", 17, 2, 16 }, { "Ar", 18, 2, 17 },
            // Okres 4
            { "K", 19, 3, 0 }, { "Ca", 20, 3, 1 }, { "Sc", 21, 3, 2 }, { "Ti", 22, 3, 3 },
            { "V", 23, 3, 4 }, { "Cr", 24, 3, 5 }, { "Mn", 25, 3, 6 }, { "Fe", 26, 3, 7 },
            { "Co", 27, 3, 8 }, { "Ni", 28, 3, 9 }, { "Cu", 29, 3, 10 }, { "Zn", 30, 3, 11 },
            { "Ga", 31, 3, 12 }, { "Ge", 32, 3, 13 }, { "As", 33, 3, 14 }, { "Se", 34, 3, 15 },
            { "Br", 35, 3, 16 }, { "Kr", 36, 3, 17 },
            // Okres 5
            { "Rb", 37, 4, 0 }, { "Sr", 38, 4, 1 }, { "Y", 39, 4, 2 }, { "Zr", 40, 4, 3 },
            { "Nb", 41, 4, 4 }, { "Mo", 42, 4, 5 }, { "Tc", 43, 4, 6 }, { "Ru", 44, 4, 7 },
            { "Rh", 45, 4, 8 }, { "Pd", 46, 4, 9 }, { "Ag", 47, 4, 10 }, { "Cd", 48, 4, 11 },
            { "In", 49, 4, 12 }, { "Sn", 50, 4, 13 }, { "Sb", 51, 4, 14 }, { "Te", 52, 4, 15 },
            { "I", 53, 4, 16 }, { "Xe", 54, 4, 17 },
            // Okres 6
            { "Cs", 55, 5, 0 }, { "Ba", 56, 5, 1 },
            { "Hf", 72, 5, 3 }, { "Ta", 73, 5, 4 }, { "W", 74, 5, 5 }, { "Re", 75, 5, 6 },
            { "Os", 76, 5, 7 }, { "Ir", 77, 5, 8 }, { "Pt", 78, 5, 9 }, { "Au", 79, 5, 10 },
            { "Hg", 80, 5, 11 }, { "Tl", 81, 5, 12 }, { "Pb", 82, 5, 13 }, { "Bi", 83, 5, 14 },
            { "Po", 84, 5, 15 }, { "At", 85, 5, 16 }, { "Rn", 86, 5, 17 },
            // Okres 7
            { "Fr", 87, 6, 0 }, { "Ra", 88, 6, 1 },
            { "Rf", 104, 6, 3 }, { "Db", 105, 6, 4 }, { "Sg", 106, 6, 5 }, { "Bh", 107, 6, 6 },
            { "Hs", 108, 6, 7 }, { "Mt", 109, 6, 8 }, { "Ds", 110, 6, 9 }, { "Rg", 111, 6, 10 },
            { "Cn", 112, 6, 11 }, { "Nh", 113, 6, 12 }, { "Fl", 114, 6, 13 }, { "Mc", 115, 6, 14 },
            { "Lv", 116, 6, 15 }, { "Ts", 117, 6, 16 }, { "Og", 118, 6, 17 },
            // Lantanowce (rząd 8)
            { "La", 57, 8, 2 }, { "Ce", 58, 8, 3 }, { "Pr", 59, 8, 4 }, { "Nd", 60, 8, 5 },
            { "Pm", 61, 8, 6 }, { "Sm", 62, 8, 7 }, { "Eu", 63, 8, 8 }, { "Gd", 64, 8, 9 },
            { "Tb", 65, 8, 10 }, { "Dy", 66, 8, 11 }, { "Ho", 67, 8, 12 }, { "Er", 68, 8, 13 },
            { "Tm", 69, 8, 14 }, { "Yb", 70, 8, 15 }, { "Lu", 71, 8, 16 },
            // Aktynowce (rząd 9)
            { "Ac", 89, 9, 2 }, { "Th", 90, 9, 3 }, { "Pa", 91, 9, 4 }, { "U", 92, 9, 5 },
            { "Np", 93, 9, 6 }, { "Pu", 94, 9, 7 }, { "Am", 95, 9, 8 }, { "Cm", 96, 9, 9 },
            { "Bk", 97, 9, 10 }, { "Cf", 98, 9, 11 }, { "Es", 99, 9, 12 }, { "Fm", 100, 9, 13 },
            { "Md", 101, 9, 14 }, { "No", 102, 9, 15 }, { "Lr", 103, 9, 16 }
    };

    public PeriodicTablePanel(GameState gameState) {
        this.gameState = gameState;
        setBackground(GameColors.BG_DARK);
        setLayout(new BorderLayout());

        // Panel główny z GridBagLayout dla pierwiastków - 10 rzędów x 18 kolumn
        gridPanel = new JPanel(new GridBagLayout());
        gridPanel.setBackground(GameColors.BG_DARK);

        initElements();

        // Wrapper panel z marginesami lewo-prawo
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setBackground(GameColors.BG_DARK);
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40)); // margines: góra, lewo, dół, prawo

        // Dodaj gridPanel wycentrowany
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE; // Nie rozciągaj
        gbc.anchor = GridBagConstraints.CENTER;
        wrapperPanel.add(gridPanel, gbc);

        // Dodaj ComponentListener do śledzenia zmian rozmiaru
        wrapperPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateTileSizes(wrapperPanel.getWidth(), wrapperPanel.getHeight());
            }
        });

        add(wrapperPanel, BorderLayout.CENTER);

        // Rejestracja listenera dla event-driven updates
        // OPTYMALIZACJA: Nie sprawdzamy autosell tutaj - robi to GameState w batch mode
        gameState.addListener(new GameState.GameEventListener() {
            @Override
            public void onResourceChanged(String resourceSymbol, double newAmount) {
                ElementTile tile = elementTiles.get(resourceSymbol);
                if (tile != null) {
                    tile.repaint();
                    // Autosell jest teraz w GameState.processAutoSell()
                }
            }
        });
    }

    /**
     * Ustaw HoverOverlay i JFrame (wywoływane z MarketPanel)
     */
    public void setHoverOverlay(HoverOverlayPanel overlay, javax.swing.JFrame frame) {
        this.hoverOverlay = overlay;
        this.parentFrame = frame;

        // Rejestruj wszystkie ElementTile w hover overlay dla auto-sell
        for (Map.Entry<String, ElementTile> entry : elementTiles.entrySet()) {
            hoverOverlay.registerElementTile(entry.getKey(), entry.getValue());
        }
    }

    private void initElements() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE; // Nie rozciągaj - stały rozmiar
        gbc.weightx = 0; // Bez dodatkowej przestrzeni
        gbc.weighty = 0;
        gbc.insets = new Insets(1, 1, 1, 1); // 1px odstępy

        // Dodaj każdy pierwiastek na konkretnej pozycji
        for (Object[] data : ELEMENT_POSITIONS) {
            String symbol = (String) data[0];
            int atomicNumber = (int) data[1];
            int row = (int) data[2];
            int col = (int) data[3];

            ElementTile tile = new ElementTile(gameState, symbol, atomicNumber);
            elementTiles.put(symbol, tile);

            // Rejestruj w hover overlay dla auto-sell
            // (będzie dostępne po initElements)

            // Dodaj hover effect i click sound
            tile.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    com.labinc.util.SoundManager.getInstance().playClick2();
                }

                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (hoverOverlay == null || parentFrame == null)
                        return;

                    // Oblicz pozycję przy każdym hover - rozmiar może się zmieniać
                    Point pos = SwingUtilities.convertPoint(tile, new Point(0, 0), parentFrame.getGlassPane());
                    hoverOverlay.showHover(symbol, atomicNumber, pos, tile.getWidth(), tile.getHeight());
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    if (hoverOverlay == null || parentFrame == null)
                        return;

                    // Szybkie sprawdzenie bez convertPoint - użyj bounds
                    Point mouseOnScreen = e.getLocationOnScreen();
                    Point overlayOnScreen = parentFrame.getGlassPane().getLocationOnScreen();
                    Point relMouse = new Point(mouseOnScreen.x - overlayOnScreen.x,
                            mouseOnScreen.y - overlayOnScreen.y);

                    if (hoverOverlay.isMouseOverHover(relMouse)) {
                        return;
                    }

                    hoverOverlay.hideHover();
                }
            });

            // Ustaw pozycję w gridzie
            gbc.gridx = col;
            gbc.gridy = row;

            gridPanel.add(tile, gbc);
        }
    }

    public void setAutoSell(String symbol, boolean enabled, double threshold) {
        ElementTile tile = elementTiles.get(symbol);
        if (tile != null) {
            tile.setAutoSell(enabled, threshold);
        } else {
            // Dla surowców niebędących pierwiastkami (np. związki chemiczne)
            com.labinc.model.Resource resource = gameState.getResources().get(symbol);
            if (resource != null) {
                resource.setAutoSell(enabled, threshold);
            }
        }
    }

    public ElementTile getElementTile(String symbol) {
        return elementTiles.get(symbol);
    }

    /**
     * Ustawia listener dla wszystkich ElementTile (do obsługi zmiany auto-sell)
     */
    public void setAutoSellChangeListener(ElementTile.AutoSellChangeListener listener) {
        for (ElementTile tile : elementTiles.values()) {
            tile.setAutoSellChangeListener(listener);
        }
    }

    /**
     * Ustawia listener sprzedaży dla wszystkich ElementTile
     */
    public void setSellListener(ElementTile.SellListener listener) {
        for (ElementTile tile : elementTiles.values()) {
            tile.setSellListener(listener);
        }
    }

    /**
     * Zwraca mapę wszystkich ElementTile
     */
    public Map<String, ElementTile> getElementTiles() {
        return elementTiles;
    }

    /**
     * Synchronizuje wszystkie ElementTile z Resource (po wczytaniu zapisu)
     */
    public void syncAutoSellFromResources() {
        for (ElementTile tile : elementTiles.values()) {
            tile.syncAutoSellFromResource();
        }
    }

    /**
     * Aktualizuje rozmiary kafelków na podstawie dostępnego miejsca
     */
    private void updateTileSizes(int width, int height) {
        if (width <= 0 || height <= 0)
            return;

        // Marginesy z wrapperPanel (40 lewo/prawo, 10 góra/dół)
        int availableW = width - 80;
        int availableH = height - 20;

        // Grid ma 18 kolumn i 10 rzędów (7 okresów + 2 lant/akt + odstępy)
        // Zakładamy odstępy 2px między kaflami (Insets(1,1,1,1) * 2 strony)
        int gap = 2;
        int cols = 18;
        int rows = 10;

        int tileSizeW = (availableW - (cols * gap)) / cols;
        int tileSizeH = (availableH - (rows * gap)) / rows;

        // Wybierz mniejszy wymiar, aby zachować kwadrat
        int newSize = Math.min(tileSizeW, tileSizeH);

        // Ograniczenia rozmiaru
        newSize = Math.max(20, Math.min(60, newSize));

        // Aktualizuj wszystkie kafelki
        for (ElementTile tile : elementTiles.values()) {
            tile.updateSize(newSize);
        }

        // Wymuś odświeżenie layoutu
        gridPanel.revalidate();
        gridPanel.repaint();
    }
}
