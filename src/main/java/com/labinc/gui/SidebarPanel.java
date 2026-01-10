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
import java.awt.RenderingHints;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.labinc.util.IconLoader;
import com.labinc.util.GameColors;

/**
 * Lewy sidebar z menu nawigacyjnym - WARIANT A: Industrial Dark
 * Metalowe przyciski z nitami, neonowa poświata na aktywnym
 */
public class SidebarPanel extends JPanel {
    private static final Color ORANGE = GameColors.ORANGE;
    private static final Color ORANGE_HOVER = GameColors.ORANGE_HOVER;
    private static final Color ORANGE_GLOW = GameColors.ORANGE_GLOW;
    private static final Color BG_DARK = GameColors.BG_SIDEBAR;

    private JButton activeButton = null;
    private java.util.Map<String, JButton> buttonMap = new java.util.HashMap<>();
    private ActionListener settingsListener;
    private ActionListener saveListener;
    private ActionListener helpListener;

    public SidebarPanel(ActionListener wydobycieListener, ActionListener fabrykiListener,
            ActionListener rynekListener, ActionListener osiagnieciaListener) {
        this(wydobycieListener, fabrykiListener, rynekListener, osiagnieciaListener, null, null, null, null);
    }

    public SidebarPanel(ActionListener wydobycieListener, ActionListener fabrykiListener,
            ActionListener rynekListener, ActionListener infoListener, ActionListener osiagnieciaListener,
            ActionListener settingsListener, ActionListener saveListener, ActionListener helpListener) {
        this.settingsListener = settingsListener;
        this.saveListener = saveListener;
        this.helpListener = helpListener;
        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        setPreferredSize(new Dimension(260, 0));

        // Panel główny z przyciskami menu
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int bevel = 12; // Szerokość fazy 3D

                // === GÓRNY TRAPEZ (jasny - światło od góry) - BEZ lewego rogu ===
                int[] topTrapX = { 0, w, w - bevel, 0 };
                int[] topTrapY = { 0, 0, bevel, bevel };
                GradientPaint topGrad = new GradientPaint(0, 0, new Color(120, 125, 130), 0, bevel,
                        new Color(70, 75, 80));
                g2.setPaint(topGrad);
                g2.fillPolygon(topTrapX, topTrapY, 4);

                // === PRAWY TRAPEZ (ciemny - cień) ===
                int[] rightTrapX = { w, w, w - bevel, w - bevel };
                int[] rightTrapY = { 0, h, h - bevel, bevel };
                GradientPaint rightGrad = new GradientPaint(w - bevel, 0, new Color(40, 43, 47), w, 0,
                        new Color(25, 28, 32));
                g2.setPaint(rightGrad);
                g2.fillPolygon(rightTrapX, rightTrapY, 4);

                // === DOLNY TRAPEZ (ciemny - cień) - BEZ lewego rogu ===
                // Szersza podstawa na dole (h), węższa na górze (h-bevel)
                int[] bottomTrapX = { 0, w, w - bevel, 0 };
                int[] bottomTrapY = { h, h, h - bevel, h - bevel };
                GradientPaint bottomGrad = new GradientPaint(0, h - bevel, new Color(45, 48, 52), 0, h,
                        new Color(20, 22, 25));
                g2.setPaint(bottomGrad);
                g2.fillPolygon(bottomTrapX, bottomTrapY, 4);

                // === ŚRODKOWA PŁYTA (główna powierzchnia - jednolity kolor) ===
                // Rozszerzona do lewej krawędzi (brak lewego bevela)
                g2.setColor(new Color(38, 43, 48));
                g2.fillRect(0, bevel, w - bevel, h - 2 * bevel);

                // Linie oddzielające trapezy od środka (efekt krawędzi)
                g2.setColor(new Color(30, 32, 35));
                g2.setStroke(new BasicStroke(1f));
                // Górna krawędź wewnętrzna
                g2.drawLine(0, bevel, w - bevel, bevel);

                // Nity w rogach wewnętrznej płyty (tylko po prawej stronie)
                drawRivet(g2, w - bevel - 10, bevel + 10);
                drawRivet(g2, w - bevel - 10, h - bevel - 10);
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);

        // Logo na górze
        JPanel logoPanel = createLogoPanel();
        mainPanel.add(logoPanel);

        // Separator (rura)
        mainPanel.add(createSeparator());
        mainPanel.add(Box.createVerticalStrut(15));

        // Przyciski menu - nazwa ikony, tekst
        String[][] menuItems = {
                { IconLoader.ICON_MINING, "WYDOBYCIE" },
                { IconLoader.ICON_FACTORY, "FABRYKI" },
                { IconLoader.ICON_MARKET, "RYNEK" },
                { IconLoader.ICON_HELP, "INFORMACJE" },
                { IconLoader.ICON_ACHIEVEMENTS, "OSIĄGNIĘCIA" }
        };
        ActionListener[] listeners = { wydobycieListener, fabrykiListener, rynekListener, infoListener,
                osiagnieciaListener };

        JButton firstBtn = null;
        for (int i = 0; i < menuItems.length; i++) {
            JButton btn = createMenuButton(menuItems[i][0], menuItems[i][1], listeners[i]);
            mainPanel.add(btn);
            mainPanel.add(Box.createVerticalStrut(8));
            // Zapisz do mapy dla późniejszego dostępu
            buttonMap.put(menuItems[i][1].toLowerCase(), btn);
            if (i == 0)
                firstBtn = btn;
        }

        // Domyślnie aktywny pierwszy
        if (firstBtn != null)
            setActiveButton(firstBtn);

        mainPanel.add(Box.createVerticalGlue());

        add(mainPanel, BorderLayout.CENTER);

        // Dolny panel z ikonami
        JPanel bottomPanel = createBottomIconsPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void drawRivet(Graphics2D g2, int x, int y) {
        // Cień
        g2.setColor(new Color(20, 20, 20));
        g2.fillOval(x - 6, y - 6, 12, 12);
        // Nit
        GradientPaint rivetGrad = new GradientPaint(x - 4, y - 4, new Color(100, 105, 110), x + 4, y + 4,
                new Color(50, 55, 60));
        g2.setPaint(rivetGrad);
        g2.fillOval(x - 5, y - 5, 10, 10);
        // Blask
        g2.setColor(new Color(255, 255, 255, 40));
        g2.fillOval(x - 3, y - 4, 4, 3);
    }

    private JPanel createLogoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 15, 10, 15));

        // Ikona - próbuj załadować z pliku, fallback na tekst
        ImageIcon logoIcon = IconLoader.loadIcon(IconLoader.ICON_LOGO, 48);
        JLabel iconLabel;
        if (logoIcon != null) {
            iconLabel = new JLabel(logoIcon);
        } else {
            iconLabel = new JLabel("⚗");
            iconLabel.setFont(new Font("Arial", Font.BOLD, 42));
            iconLabel.setForeground(ORANGE);
        }
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Nazwa
        JLabel nameLabel = new JLabel("LABINC");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 28));
        nameLabel.setForeground(ORANGE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Podtytuł
        JLabel subtitleLabel = new JLabel("Chemical Tycoon");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        subtitleLabel.setForeground(new Color(150, 150, 150));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(iconLabel);
        panel.add(nameLabel);
        panel.add(subtitleLabel);

        return panel;
    }

    private JPanel createSeparator() {
        JPanel sep = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int y = getHeight() / 2;
                int margin = 20;

                // Rura (gradient metalowy)
                GradientPaint pipeGrad = new GradientPaint(margin, y - 4, new Color(70, 75, 80), margin, y + 4,
                        new Color(40, 45, 50));
                g2.setPaint(pipeGrad);
                g2.fillRoundRect(margin, y - 4, getWidth() - 2 * margin, 8, 4, 4);

                // Blask na górze rury
                g2.setColor(new Color(255, 255, 255, 30));
                g2.drawLine(margin + 5, y - 3, getWidth() - margin - 5, y - 3);

                // Łączniki
                g2.setColor(new Color(60, 65, 70));
                g2.fillRect(margin + 30, y - 6, 15, 12);
                g2.fillRect(getWidth() - margin - 45, y - 6, 15, 12);
            }
        };
        sep.setOpaque(false);
        sep.setPreferredSize(new Dimension(260, 20));
        sep.setMaximumSize(new Dimension(260, 20));
        return sep;
    }

    private JButton createMenuButton(String iconName, String text, ActionListener listener) {
        final JButton[] buttonRef = new JButton[1];
        final ImageIcon loadedIcon = IconLoader.loadIcon(iconName, 24);
        final String fallbackText = IconLoader.getFallbackText(iconName);

        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                int w = getWidth();
                int h = getHeight();
                boolean isActive = (buttonRef[0] == activeButton);
                boolean isHover = getModel().isRollover();

                // Tło metalowe
                Color bgTop = isActive ? ORANGE_HOVER : (isHover ? new Color(70, 75, 80) : new Color(55, 60, 65));
                Color bgBottom = isActive ? ORANGE_HOVER.darker()
                        : (isHover ? new Color(50, 55, 60) : new Color(40, 45, 50));
                GradientPaint bgGrad = new GradientPaint(0, 0, bgTop, 0, h, bgBottom);
                g2.setPaint(bgGrad);
                g2.fillRoundRect(10, 2, w - 20, h - 4, 8, 8);

                // Obramowanie
                g2.setColor(new Color(25, 25, 25));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(10, 2, w - 20, h - 4, 8, 8);

                // Neonowa poświata (tylko dla aktywnego)
                if (isActive) {
                    g2.setColor(new Color(ORANGE_GLOW.getRed(), ORANGE_GLOW.getGreen(), ORANGE_GLOW.getBlue(), 100));
                    g2.setStroke(new BasicStroke(4f));
                    g2.drawRoundRect(8, 0, w - 16, h, 10, 10);

                    // Wskaźnik po prawej
                    g2.setColor(ORANGE_GLOW);
                    int[] xPoints = { w - 18, w - 8, w - 18 };
                    int[] yPoints = { h / 2 - 8, h / 2, h / 2 + 8 };
                    g2.fillPolygon(xPoints, yPoints, 3);
                }

                // Nity na rogach przycisku
                drawSmallRivet(g2, 18, h / 2);

                // Rysowanie ikony lub tekstu zastępczego
                int iconX = 35;
                int iconY = (h - 24) / 2;

                if (loadedIcon != null) {
                    // Rysuj ikonę z pliku PNG
                    Image img = loadedIcon.getImage();
                    g2.drawImage(img, iconX, iconY, 24, 24, null);
                } else {
                    // Fallback: rysuj tekst
                    g2.setFont(new Font("Arial", Font.BOLD, 20));
                    g2.setColor(isActive ? Color.WHITE : new Color(220, 220, 220));
                    g2.drawString(fallbackText, iconX, h / 2 + 7);
                }

                // Tekst nazwy
                g2.setFont(new Font("Arial", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                int textX = iconX + 32; // Po ikonie
                int textY = (h + fm.getAscent() - fm.getDescent()) / 2;

                // Cień tekstu
                g2.setColor(new Color(0, 0, 0, 150));
                g2.drawString(text, textX + 1, textY + 1);

                // Tekst główny
                g2.setColor(isActive ? Color.WHITE : new Color(220, 220, 220));
                g2.drawString(text, textX, textY);
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
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(250, 55));
        button.setPreferredSize(new Dimension(250, 55));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buttonRef[0] = button;

        button.addActionListener(e -> {
            com.labinc.util.SoundManager.getInstance().playClick2();
            setActiveButton(buttonRef[0]);
            listener.actionPerformed(e);
        });

        return button;
    }

    private JPanel createBottomIconsPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Metalowa listwa - identyczna z dolnym paskiem głównym
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
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panel.setPreferredSize(new Dimension(260, 44));
        panel.setMinimumSize(new Dimension(260, 44));
        panel.setMaximumSize(new Dimension(260, 44));

        // Wrapper żeby wycentrować w pionie
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        JPanel iconsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        iconsRow.setOpaque(false);

        // Ikony - nazwa ikony i tooltip
        String[] iconNames = { IconLoader.ICON_SETTINGS, IconLoader.ICON_SAVE, IconLoader.ICON_HELP };
        String[] tooltips = { "Ustawienia", "Zapisz grę", "Pomoc" };
        ActionListener[] iconListeners = { settingsListener, saveListener, helpListener };

        for (int i = 0; i < iconNames.length; i++) {
            JButton iconBtn = createIconButton(iconNames[i], tooltips[i]);
            if (iconListeners[i] != null) {
                final ActionListener l = iconListeners[i];
                iconBtn.addActionListener(e -> {
                    com.labinc.util.SoundManager.getInstance().playClick2();
                    l.actionPerformed(e);
                });
            }
            iconsRow.add(iconBtn);
        }

        wrapper.add(iconsRow);
        panel.setLayout(new BorderLayout());
        panel.add(wrapper, BorderLayout.CENTER);

        return panel;
    }

    private JButton createIconButton(String iconName, String tooltip) {
        final ImageIcon loadedIcon = IconLoader.loadIcon(iconName, 20);
        final String fallbackText = IconLoader.getFallbackText(iconName);

        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                boolean hover = getModel().isRollover();

                // Tło okrągłe
                if (hover) {
                    g2.setColor(new Color(80, 85, 90));
                } else {
                    g2.setColor(new Color(60, 65, 70));
                }
                g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);

                // Obramowanie
                g2.setColor(new Color(40, 45, 50));
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(2, 2, getWidth() - 4, getHeight() - 4);

                // Rysuj ikonę lub fallback
                if (loadedIcon != null) {
                    int iconSize = 20;
                    int x = (getWidth() - iconSize) / 2;
                    int y = (getHeight() - iconSize) / 2;
                    g2.drawImage(loadedIcon.getImage(), x, y, iconSize, iconSize, null);
                } else {
                    g2.setFont(new Font("Arial", Font.BOLD, 16));
                    g2.setColor(new Color(200, 200, 200));
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(fallbackText)) / 2;
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(fallbackText, x, y);
                }
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(36, 36));
        btn.setToolTipText(tooltip);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void setActiveButton(JButton button) {
        if (activeButton != null) {
            activeButton.repaint();
        }
        activeButton = button;
        activeButton.repaint();
    }

    /**
     * Wybiera przycisk nawigacyjny po nazwie (np. "informacje", "wydobycie")
     */
    public void selectButton(String name) {
        JButton btn = buttonMap.get(name.toLowerCase());
        if (btn != null) {
            setActiveButton(btn);
        }
    }
}
