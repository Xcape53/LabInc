package com.labinc.gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

import com.labinc.LabIncGame;
import com.labinc.model.SaveGame;
import com.labinc.util.IconLoader;
import com.labinc.util.WindowsThemeUtil;
import com.labinc.util.GameColors;

public class MainMenuFrame extends JFrame {
    private static final Color ORANGE = GameColors.ORANGE;
    private static final Color ORANGE_HOVER = GameColors.ORANGE_HOVER;
    private static final Color ORANGE_GLOW = GameColors.ORANGE_GLOW;
    private static final Color BG_DARK = GameColors.BG_SIDEBAR;

    public MainMenuFrame() {
        super("LabInc - Launcher");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(550, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        WindowsThemeUtil.setDarkTitleBar(this);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_DARK);

        // Logo Section
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(BG_DARK);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 10, 0));

        // Use custom separator logic for decoration if needed, or simple icon
        JLabel logoLabel = new JLabel(IconLoader.loadIcon(IconLoader.ICON_LOGO, 128));
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoPanel.add(logoLabel, BorderLayout.CENTER);

        JLabel titleLabel = new JLabel("LABINC", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(ORANGE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        logoPanel.add(titleLabel, BorderLayout.SOUTH);

        mainPanel.add(logoPanel, BorderLayout.NORTH);

        // Buttons Section
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setBackground(BG_DARK);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(5, 40, 50, 40)); // Adjusted padding

        // Add separator pipe like in sidebar
        buttonsPanel.add(createSeparator());
        buttonsPanel.add(Box.createVerticalStrut(25));

        buttonsPanel.add(createMenuButton("NOWA GRA", IconLoader.ICON_FACTORY, e -> startNewGame()));
        buttonsPanel.add(Box.createVerticalStrut(12));
        buttonsPanel.add(createMenuButton("WCZYTAJ GRĘ", IconLoader.ICON_SAVE, e -> loadGame()));
        buttonsPanel.add(Box.createVerticalStrut(12));
        buttonsPanel.add(createMenuButton("WYJŚCIE", IconLoader.ICON_HELP, e -> System.exit(0))); // Using help icon as
                                                                                                  // placeholder or exit
                                                                                                  // if available

        mainPanel.add(buttonsPanel, BorderLayout.CENTER);

        add(mainPanel);
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
        sep.setPreferredSize(new Dimension(300, 20));
        sep.setMaximumSize(new Dimension(300, 20));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);
        return sep;
    }

    private JButton createMenuButton(String text, String iconName, java.awt.event.ActionListener action) {
        final ImageIcon loadedIcon = IconLoader.loadIcon(iconName, 24);

        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                int w = getWidth();
                int h = getHeight();
                boolean isHover = getModel().isRollover();
                boolean isPressed = getModel().isPressed();

                // Active logic: Hover simulates active state in main menu
                boolean isActive = isHover || isPressed;

                // Tło metalowe
                Color bgTop = isActive ? ORANGE_HOVER : new Color(55, 60, 65);
                Color bgBottom = isActive ? ORANGE_HOVER.darker() : new Color(40, 45, 50);

                // If pressed, darken
                if (isPressed) {
                    bgTop = bgTop.darker();
                    bgBottom = bgBottom.darker();
                }

                GradientPaint bgGrad = new GradientPaint(0, 0, bgTop, 0, h, bgBottom);
                g2.setPaint(bgGrad);
                g2.fillRoundRect(10, 2, w - 20, h - 4, 8, 8);

                // Obramowanie
                g2.setColor(new Color(25, 25, 25));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(10, 2, w - 20, h - 4, 8, 8);

                // Neonowa poświata (dla hover/pressed)
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

                // Rysowanie ikony
                int iconX = 35;
                int iconY = (h - 24) / 2;

                if (loadedIcon != null) {
                    Image img = loadedIcon.getImage();
                    g2.drawImage(img, iconX, iconY, 24, 24, null);
                }

                // Tekst nazwy
                g2.setFont(new Font("Arial", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                int textX = iconX + 32;
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

        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(300, 54));
        btn.setPreferredSize(new Dimension(300, 54));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(action);
        return btn;
    }

    private void startNewGame() {
        launchGame(null, null);
    }

    private void loadGame() {
        JFileChooser fileChooser = new JFileChooser(new File("."));
        fileChooser.setDialogTitle("Wybierz zapis gry");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Pliki zapisu LabInc (*.dat)", "dat"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            SaveGame save = SaveGame.load(selectedFile.getAbsolutePath());
            if (save != null) {
                launchGame(save, selectedFile.getAbsolutePath());
            } else {
                JOptionPane.showMessageDialog(this, "Nie udało się wczytać zapisu.", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void launchGame(SaveGame save, String fileName) {
        dispose(); // Close launcher
        SwingUtilities.invokeLater(() -> {
            new LabIncGame(save, fileName).setVisible(true);
        });
    }
}
