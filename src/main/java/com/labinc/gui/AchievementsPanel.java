package com.labinc.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import com.labinc.model.Achievement;
import com.labinc.model.AchievementManager;
import com.labinc.util.GameColors;

/**
 * Panel osiągnięć - nowoczesny design
 */
public class AchievementsPanel extends JPanel {
    private final AchievementManager achievementManager;

    // Kolory motywu

    private static final Color CARD_BG = new Color(50, 50, 50);
    private static final Color CARD_BG_UNLOCKED = new Color(45, 65, 50);
    private static final Color ORANGE = GameColors.ORANGE;
    private static final Color TEXT_LIGHT = GameColors.TEXT_LIGHT;
    private static final Color TEXT_DIM = new Color(120, 120, 120);
    private static final Color GREEN = new Color(74, 222, 128);
    private static final Color GOLD = new Color(255, 215, 0);

    private static final int CARD_WIDTH = 320;
    private static final int CARD_HEIGHT = 140;

    public AchievementsPanel(AchievementManager achievementManager) {
        this.achievementManager = achievementManager;
        setLayout(new BorderLayout(0, 20));
        setBackground(GameColors.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        updateDisplay();
    }

    public void updateDisplay() {
        removeAll();

        // Nagłówek z tytułem i statystykami
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel header = new JLabel("OSIĄGNIĘCIA");
        header.setFont(new Font("Arial", Font.BOLD, 32));
        header.setForeground(ORANGE);
        headerPanel.add(header, BorderLayout.WEST);

        List<Achievement> achievements = achievementManager.getAchievements();
        int unlocked = (int) achievements.stream().filter(Achievement::isUnlocked).count();

        JLabel statsLabel = new JLabel(String.format("%d / %d", unlocked, achievements.size()));
        statsLabel.setFont(new Font("Arial", Font.BOLD, 24));
        statsLabel.setForeground(GREEN);
        headerPanel.add(statsLabel, BorderLayout.EAST);

        // Pasek postępu
        JPanel progressContainer = new JPanel(new BorderLayout(10, 0));
        progressContainer.setOpaque(false);
        progressContainer.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        float progress = (float) unlocked / achievements.size();
        JPanel progressBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Tło paska
                g2.setColor(new Color(64, 64, 64));
                g2.fillRoundRect(0, 0, w, h, h, h);

                // Wypełnienie
                int fillW = (int) (w * progress);
                if (fillW > 0) {
                    GradientPaint gp = new GradientPaint(0, 0, GREEN, fillW, 0, new Color(34, 197, 94));
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, fillW, h, h, h);
                }

                g2.dispose();
            }
        };
        progressBar.setPreferredSize(new Dimension(0, 12));
        progressBar.setOpaque(false);

        JLabel percentLabel = new JLabel(String.format("%.0f%%", progress * 100));
        percentLabel.setFont(new Font("Arial", Font.BOLD, 16));
        percentLabel.setForeground(TEXT_LIGHT);

        progressContainer.add(progressBar, BorderLayout.CENTER);
        progressContainer.add(percentLabel, BorderLayout.EAST);

        // Wrapper dla headera i progressu
        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.add(headerPanel, BorderLayout.NORTH);
        topSection.add(progressContainer, BorderLayout.SOUTH);
        add(topSection, BorderLayout.NORTH);

        // Grid z osiągnięciami
        JPanel achievementsGrid = new JPanel(new GridBagLayout());
        achievementsGrid.setBackground(GameColors.BG_DARK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.NONE;

        int col = 0;
        int row = 0;
        int maxCols = 3;

        for (Achievement achievement : achievements) {
            gbc.gridx = col;
            gbc.gridy = row;
            achievementsGrid.add(createAchievementCard(achievement), gbc);

            col++;
            if (col >= maxCols) {
                col = 0;
                row++;
            }
        }

        // Wrapper do centrowania grida
        JPanel gridWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        gridWrapper.setBackground(GameColors.BG_DARK);
        gridWrapper.add(achievementsGrid);

        // ScrollPane z niewidocznym scrollbarem (ale działającym scrollowaniem)
        JScrollPane scrollPane = new JScrollPane(gridWrapper);
        scrollPane.setBackground(GameColors.BG_DARK);
        scrollPane.getViewport().setBackground(GameColors.BG_DARK);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0)); // Ukryj scrollbar wizualnie
        scrollPane.setWheelScrollingEnabled(true);

        add(scrollPane, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel createAchievementCard(Achievement achievement) {
        boolean isUnlocked = achievement.isUnlocked();

        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Tło z gradientem
                Color bgTop = isUnlocked ? new Color(55, 80, 60) : new Color(55, 55, 55);
                Color bgBottom = isUnlocked ? CARD_BG_UNLOCKED : CARD_BG;
                GradientPaint gp = new GradientPaint(0, 0, bgTop, 0, h, bgBottom);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 16, 16));

                // Ramka
                g2.setColor(isUnlocked ? new Color(74, 222, 128, 100) : new Color(80, 80, 80, 80));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 16, 16));

                // Blask dla odblokowanych
                if (isUnlocked) {
                    g2.setColor(new Color(74, 222, 128, 30));
                    g2.fill(new RoundRectangle2D.Float(0, 0, w, h / 3, 16, 16));
                }

                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Górna część: ikona + nazwa
        JPanel topRow = new JPanel(new BorderLayout(12, 0));
        topRow.setOpaque(false);

        // Ikona w zaokrąglonym kwadracie
        JPanel iconWrapper = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = Math.min(getWidth(), getHeight());
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                int radius = 12; // Promień zaokrąglenia rogów

                // Tło ikony
                if (isUnlocked) {
                    GradientPaint gp = new GradientPaint(x, y, GOLD, x + size, y + size, new Color(255, 180, 0));
                    g2.setPaint(gp);
                } else {
                    g2.setColor(new Color(70, 70, 70));
                }
                g2.fillRoundRect(x, y, size, size, radius, radius);

                // Obramowanie
                g2.setColor(isUnlocked ? new Color(255, 215, 0, 150) : new Color(90, 90, 90));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(x + 1, y + 1, size - 2, size - 2, radius, radius);

                g2.dispose();
            }
        };
        iconWrapper.setOpaque(false);
        iconWrapper.setPreferredSize(new Dimension(50, 50));

        JLabel iconLabel = new JLabel(achievement.getIcon(), SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        iconWrapper.add(iconLabel);
        topRow.add(iconWrapper, BorderLayout.WEST);

        // Nazwa i status
        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setOpaque(false);

        JLabel nameLabel = new JLabel(achievement.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 15));
        nameLabel.setForeground(isUnlocked ? Color.WHITE : TEXT_LIGHT);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        namePanel.add(nameLabel);

        namePanel.add(Box.createVerticalStrut(4));

        JLabel statusLabel = new JLabel(isUnlocked ? "Odblokowane" : "Zablokowane");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        statusLabel.setForeground(isUnlocked ? GREEN : TEXT_DIM);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        namePanel.add(statusLabel);

        topRow.add(namePanel, BorderLayout.CENTER);

        card.add(topRow, BorderLayout.NORTH);

        // Opis
        JLabel descLabel = new JLabel("<html><div style='width: " + (CARD_WIDTH - 50) + "px;'>" +
                achievement.getDescription() + "</div></html>");
        descLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        descLabel.setForeground(isUnlocked ? new Color(220, 220, 220) : TEXT_DIM);
        descLabel.setVerticalAlignment(SwingConstants.TOP);
        card.add(descLabel, BorderLayout.CENTER);

        return card;
    }
}
