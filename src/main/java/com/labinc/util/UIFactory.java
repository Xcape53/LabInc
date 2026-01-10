package com.labinc.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicComboBoxUI;

/**
 * Fabryka komponentów UI - centralizacja stylów
 */
public class UIFactory {

    // Stałe kolorów (zgodne z GameColors)
    private static final Color INPUT_BG = new Color(45, 48, 52);
    private static final Color INPUT_BORDER = new Color(60, 60, 60);
    private static final Color TEXT_LIGHT = GameColors.TEXT_LIGHT;
    private static final Color ORANGE = GameColors.ORANGE;
    private static final Color ORANGE_HOVER = new Color(185, 90, 25);

    /**
     * Włącza antyaliasing dla Graphics2D
     */
    public static void enableAntialiasing(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    /**
     * Włącza pełne ustawienia jakości dla Graphics2D
     */
    public static void enableHighQuality(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    /**
     * Styl ComboBox
     */
    public enum ComboBoxStyle {
        /** Kompaktowy styl (ModernMiningPanel) - zaokrąglone rogi, strzałka */
        COMPACT,
        /** Duży styl (MarketPanel) - prostokątny, bez strzałki */
        LARGE
    }

    /**
     * Tworzy stylizowany ComboBox w określonym stylu
     */
    public static JComboBox<String> createStyledComboBox(ComboBoxStyle style, String[] items) {
        if (style == ComboBoxStyle.COMPACT) {
            return createCompactComboBox(items);
        } else {
            return createLargeComboBox();
        }
    }

    /**
     * Kompaktowy ComboBox (styl ModernMiningPanel)
     */
    private static JComboBox<String> createCompactComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<String>(items) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                enableAntialiasing(g2);

                // Tło - ciemne
                g2.setColor(new Color(40, 45, 50));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

                // Obramowanie
                g2.setColor(new Color(60, 65, 70));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);

                // Tekst wybranego elementu
                Object selected = getSelectedItem();
                if (selected != null) {
                    g2.setColor(new Color(220, 220, 220));
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(selected.toString(), 10, textY);
                }

                // Strzałka w dół
                int arrowX = getWidth() - 20;
                int arrowY = getHeight() / 2 - 2;
                g2.setColor(new Color(150, 150, 150));
                int[] xPoints = { arrowX, arrowX + 8, arrowX + 4 };
                int[] yPoints = { arrowY, arrowY, arrowY + 5 };
                g2.fillPolygon(xPoints, yPoints, 3);
            }
        };

        combo.setOpaque(false);
        combo.setForeground(TEXT_LIGHT);
        combo.setFont(new Font("Arial", Font.PLAIN, 11));
        combo.setPreferredSize(new Dimension(100, 26));

        combo.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? ORANGE : new Color(45, 48, 52));
                setForeground(isSelected ? Color.WHITE : TEXT_LIGHT);
                setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return this;
            }
        });

        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                return new JButton() {
                    @Override
                    public int getWidth() {
                        return 0;
                    }
                };
            }
        });

        return combo;
    }

    /**
     * Duży ComboBox (styl MarketPanel)
     */
    private static JComboBox<String> createLargeComboBox() {
        JComboBox<String> combo = new JComboBox<String>() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                enableAntialiasing(g2);

                // Tło
                g2.setColor(INPUT_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Obramowanie
                g2.setColor(INPUT_BORDER);
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

                // Tekst wybranego elementu
                Object selected = getSelectedItem();
                if (selected != null) {
                    g2.setColor(TEXT_LIGHT);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(selected.toString(), 10, textY);
                }
            }

            @Override
            protected void paintBorder(Graphics g) {
                // Nie rysuj domyślnego borderu
            }
        };

        combo.setOpaque(true);
        combo.setBackground(INPUT_BG);
        combo.setForeground(TEXT_LIGHT);
        combo.setFont(new Font("Arial", Font.PLAIN, 12));
        combo.setPreferredSize(new Dimension(180, 32));
        combo.setMaximumRowCount(20);
        combo.setBorder(null);

        // Renderer dla listy rozwijanej
        combo.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? ORANGE_HOVER : INPUT_BG);
                setForeground(TEXT_LIGHT);
                setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                setOpaque(true);
                return this;
            }
        });

        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                button.setVisible(false);
                return button;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, java.awt.Rectangle bounds, boolean hasFocus) {
                // Nie rysuj domyślnego tła
            }

            @Override
            public void paintCurrentValue(Graphics g, java.awt.Rectangle bounds, boolean hasFocus) {
                // Nie rysuj - rysujemy własne w paintComponent
            }

            @Override
            protected javax.swing.plaf.basic.ComboPopup createPopup() {
                return new javax.swing.plaf.basic.BasicComboPopup(comboBox) {
                    @Override
                    protected javax.swing.JScrollPane createScroller() {
                        javax.swing.JScrollPane scroller = new javax.swing.JScrollPane(list,
                                javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                                javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                        scroller.getViewport().setBackground(INPUT_BG);
                        scroller.setBackground(INPUT_BG);
                        scroller.setBorder(null);
                        return scroller;
                    }

                    @Override
                    protected void configureList() {
                        super.configureList();
                        list.setBackground(INPUT_BG);
                        list.setForeground(TEXT_LIGHT);
                        list.setSelectionBackground(ORANGE_HOVER);
                        list.setSelectionForeground(TEXT_LIGHT);
                    }

                    @Override
                    protected void configurePopup() {
                        super.configurePopup();
                        setBackground(INPUT_BG);
                        setBorder(BorderFactory.createLineBorder(INPUT_BORDER, 1));
                    }
                };
            }
        });

        return combo;
    }

    /**
     * Tworzy separator (linia pozioma)
     */
    public static JPanel createSeparator(int height, Color lineColor, Color bgColor) {
        JPanel sep = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                enableAntialiasing(g2);

                // Linia gradientowa w środku
                int y = getHeight() / 2;
                g2.setColor(lineColor);
                g2.drawLine(20, y, getWidth() - 20, y);
            }
        };
        sep.setBackground(bgColor);
        sep.setOpaque(bgColor != null);
        sep.setPreferredSize(new Dimension(100, height));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        return sep;
    }

    /**
     * Tworzy domyślny separator
     */
    public static JPanel createSeparator() {
        return createSeparator(20, new Color(50, 55, 60), null);
    }
}
