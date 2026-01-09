package com.labinc.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import javax.swing.JPanel;
import javax.swing.JSlider;

import com.labinc.model.GameSettings;
import com.labinc.util.GameColors;

/**
 * Panel ustawień - pełnoekranowy, zintegrowany z resztą UI
 */
public class SettingsPanel extends JPanel {
    private static final Color BG_DARK = GameColors.BG_DARK;
    private static final Color CARD_BG = GameColors.CARD_BG;
    private static final Color CARD_BORDER = GameColors.CARD_BORDER;
    private static final Color TEXT_COLOR = new Color(220, 220, 220);
    private static final Color TEXT_DIM = GameColors.TEXT_DIM;
    private static final Color ORANGE = GameColors.ORANGE;
    private static final Color ORANGE_DARK = new Color(180, 85, 10);

    private final GameSettings settings;

    // Kontrolki grafiki
    private JCheckBox cloudsEnabledCheck;
    private JSlider cloudDensitySlider;

    // Kontrolki dźwięku
    private JCheckBox soundEnabledCheck;
    private JCheckBox musicEnabledCheck;
    private JSlider soundVolumeSlider;
    private JSlider musicVolumeSlider;

    // Referencja do HoverOverlay dla powiadomień
    private HoverOverlayPanel hoverOverlay;

    public SettingsPanel() {
        this.settings = GameSettings.getInstance();
        setBackground(BG_DARK);
        setLayout(new BorderLayout());

        initComponents();
        loadCurrentSettings();
    }

    public void setHoverOverlay(HoverOverlayPanel overlay) {
        this.hoverOverlay = overlay;
    }

    public void refresh() {
        loadCurrentSettings();
    }

    private void initComponents() {
        // Główny panel z marginesami
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BG_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // Tytuł
        JLabel titleLabel = new JLabel("USTAWIENIA");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(ORANGE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(10));

        // Podtytuł
        JLabel subtitleLabel = new JLabel("Dostosuj grę do swoich preferencji");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_DIM);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(30));

        // Panel z kartami ustawień (2 kolumny - Grafika i Dźwięk)
        JPanel cardsPanel = new JPanel(new GridBagLayout());
        cardsPanel.setBackground(BG_DARK);
        cardsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 20);

        // Karta 1: Grafika
        gbc.gridx = 0;
        cardsPanel.add(createGraphicsCard(), gbc);

        // Karta 2: Dźwięk
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        cardsPanel.add(createSoundCard(), gbc);

        mainPanel.add(cardsPanel);
        mainPanel.add(Box.createVerticalStrut(30));

        // Przyciski na dole
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBackground(BG_DARK);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton resetBtn = createButton("Przywróć domyślne", false);
        resetBtn.addActionListener(e -> resetToDefaults());

        JButton saveBtn = createButton("Zapisz ustawienia", true);
        saveBtn.addActionListener(e -> saveSettings());

        buttonPanel.add(resetBtn);
        buttonPanel.add(Box.createHorizontalStrut(15));
        buttonPanel.add(saveBtn);
        buttonPanel.add(Box.createHorizontalGlue());

        mainPanel.add(buttonPanel);

        add(mainPanel, BorderLayout.NORTH);
    }

    private JPanel createGraphicsCard() {
        return createCard("GRAFIKA", new String[] {
                "Efekty wizualne i jakość renderowania"
        }, () -> {
            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setOpaque(false);

            // Chmury włączone
            cloudsEnabledCheck = createCheckbox("Włącz efekt mgły");
            content.add(cloudsEnabledCheck);
            content.add(Box.createVerticalStrut(20));

            // Gęstość chmur
            cloudDensitySlider = createPercentSlider();
            content.add(createSliderRow("Gęstość chmur", "Ilość chmur na ekranie",
                    cloudDensitySlider));
            content.add(Box.createVerticalStrut(20));

            // Info
            JLabel infoLabel = new JLabel("<html>Wyłączenie mgły znacząco<br>poprawia wydajność</html>");
            infoLabel.setForeground(TEXT_DIM);
            infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
            infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(infoLabel);

            return content;
        });
    }

    private JPanel createSoundCard() {
        return createCard("DŹWIĘK", new String[] {
                "Głośność i efekty dźwiękowe"
        }, () -> {
            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setOpaque(false);

            // Dźwięk włączony
            soundEnabledCheck = createCheckbox("Włącz dźwięki (SFX)");
            content.add(soundEnabledCheck);
            content.add(Box.createVerticalStrut(10));

            // Muzyka włączona
            musicEnabledCheck = createCheckbox("Włącz muzykę");
            content.add(musicEnabledCheck);
            content.add(Box.createVerticalStrut(10));

            // Głośność SFX
            soundVolumeSlider = createPercentSlider();
            content.add(createSliderRow("Głośność Efektów", "SFX",
                    soundVolumeSlider));
            content.add(Box.createVerticalStrut(10));

            // Głośność Muzyki
            musicVolumeSlider = createPercentSlider();
            musicVolumeSlider.addChangeListener(e -> {
                com.labinc.util.SoundManager.getInstance().setMusicVolume(musicVolumeSlider.getValue());
            });
            content.add(createSliderRow("Głośność Muzyki", "Music",
                    musicVolumeSlider));
            content.add(Box.createVerticalStrut(20));

            return content;
        });
    }

    private JPanel createCard(String title, String[] description, CardContentSupplier contentSupplier) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Tło karty z gradientem
                GradientPaint gradient = new GradientPaint(
                        0, 0, CARD_BG,
                        0, getHeight(), new Color(40, 45, 50));
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                // Obramowanie
                g2.setColor(CARD_BORDER);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

                // Górna linia akcentowa
                g2.setColor(ORANGE);
                g2.fillRoundRect(0, 0, getWidth(), 4, 12, 12);
                g2.fillRect(0, 2, getWidth(), 2);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setPreferredSize(new Dimension(280, 320));
        card.setMinimumSize(new Dimension(250, 300));

        // Tytuł karty
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));

        // Opis
        for (String desc : description) {
            JLabel descLabel = new JLabel(desc);
            descLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            descLabel.setForeground(TEXT_DIM);
            descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(descLabel);
        }
        card.add(Box.createVerticalStrut(20));

        // Separator
        card.add(createSeparator());
        card.add(Box.createVerticalStrut(15));

        // Zawartość
        JPanel content = contentSupplier.get();
        content.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(content);

        return card;
    }

    private JPanel createSeparator() {
        JPanel sep = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(CARD_BORDER);
                g2.drawLine(0, 0, getWidth(), 0);
            }
        };
        sep.setOpaque(false);
        sep.setPreferredSize(new Dimension(100, 1));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private JPanel createSliderRow(String label, String sublabel, JSlider slider) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Etykieta z wartością
        JPanel labelRow = new JPanel(new BorderLayout());
        labelRow.setOpaque(false);
        labelRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel nameLabel = new JLabel(label);
        nameLabel.setForeground(TEXT_COLOR);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 12));

        JLabel valueLabel = new JLabel(getFpsDisplayValue(slider.getValue()));
        valueLabel.setForeground(ORANGE);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 12));

        slider.addChangeListener(e -> valueLabel.setText(getFpsDisplayValue(slider.getValue())));

        labelRow.add(nameLabel, BorderLayout.WEST);
        labelRow.add(valueLabel, BorderLayout.EAST);

        row.add(labelRow);

        // Podpis
        JLabel subLabel = new JLabel(sublabel);
        subLabel.setForeground(TEXT_DIM);
        subLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(subLabel);
        row.add(Box.createVerticalStrut(5));

        // Slider
        slider.setAlignmentX(Component.LEFT_ALIGNMENT);
        slider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.add(slider);

        return row;
    }

    private String getFpsDisplayValue(int value) {
        // 301 = unlimited
        if (value >= 301)
            return "Bez limitu";
        return String.valueOf(value);
    }

    private JSlider createPercentSlider() {
        JSlider slider = new JSlider(0, 100, 100) {
            @Override
            protected void paintComponent(Graphics g) {
                // Czyść tło żeby nie było ghostów przy przeciąganiu
                g.setColor(CARD_BG);
                g.fillRect(0, 0, getWidth(), getHeight());

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int trackY = getHeight() / 2;
                int trackHeight = 6;
                int thumbWidth = 14;
                int thumbHeight = 20;

                // Tło tracka
                g2.setColor(new Color(50, 55, 60));
                g2.fillRoundRect(7, trackY - trackHeight / 2, getWidth() - 14, trackHeight, trackHeight, trackHeight);

                // Wypełniona część tracka
                int fillWidth = (int) ((getValue() - getMinimum()) / (double) (getMaximum() - getMinimum())
                        * (getWidth() - 14));
                GradientPaint gradient = new GradientPaint(0, 0, ORANGE_DARK, fillWidth, 0, ORANGE);
                g2.setPaint(gradient);
                g2.fillRoundRect(7, trackY - trackHeight / 2, fillWidth, trackHeight, trackHeight, trackHeight);

                // Thumb (uchwyt)
                int thumbX = 7 + fillWidth - thumbWidth / 2;
                g2.setColor(new Color(70, 75, 80));
                g2.fillRoundRect(thumbX, trackY - thumbHeight / 2, thumbWidth, thumbHeight, 4, 4);
                g2.setColor(ORANGE);
                g2.fillRoundRect(thumbX + 2, trackY - thumbHeight / 2 + 2, thumbWidth - 4, thumbHeight - 4, 3, 3);

                // Linie na uchwycie
                g2.setColor(ORANGE_DARK);
                g2.drawLine(thumbX + thumbWidth / 2, trackY - 4, thumbX + thumbWidth / 2, trackY + 4);

                g2.dispose();
            }
        };
        slider.setOpaque(true);
        slider.setBackground(CARD_BG);
        slider.setFocusable(false);
        // Repaint kontenera podczas przeciągania żeby usunąć ghosty
        slider.addChangeListener(e -> {
            java.awt.Container parent = slider.getParent();
            while (parent != null) {
                parent.repaint();
                if (parent.getClass().getName().contains("SettingsPanel"))
                    break;
                parent = parent.getParent();
            }
        });
        return slider;
    }

    private JCheckBox createCheckbox(String text) {
        JCheckBox check = new JCheckBox(text);
        check.setOpaque(false);
        check.setForeground(TEXT_COLOR);
        check.setFont(new Font("Arial", Font.PLAIN, 13));
        check.setFocusPainted(false);
        check.setAlignmentX(Component.LEFT_ALIGNMENT);
        return check;
    }

    private JButton createButton(String text, boolean primary) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg = primary ? (getModel().isRollover() ? ORANGE : ORANGE_DARK)
                        : (getModel().isRollover() ? new Color(70, 75, 80) : new Color(55, 60, 65));

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                g2.setColor(primary ? Color.WHITE : TEXT_COLOR);
                g2.setFont(new Font("Arial", Font.BOLD, 13));

                String txt = getText();
                int textWidth = g2.getFontMetrics().stringWidth(txt);
                int textHeight = g2.getFontMetrics().getAscent();
                g2.drawString(txt, (getWidth() - textWidth) / 2,
                        (getHeight() + textHeight) / 2 - 2);
            }
        };
        btn.setPreferredSize(new Dimension(180, 40));
        btn.setMaximumSize(new Dimension(180, 40));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadCurrentSettings() {
        cloudsEnabledCheck.setSelected(settings.isCloudsEnabled());
        cloudDensitySlider.setValue(settings.getCloudDensity());
        soundEnabledCheck.setSelected(settings.isSoundEnabled());
        musicEnabledCheck.setSelected(settings.isMusicEnabled());
        soundVolumeSlider.setValue(settings.getSoundVolume());
        musicVolumeSlider.setValue(settings.getMusicVolume());
    }

    private void saveSettings() {
        settings.setCloudsEnabled(cloudsEnabledCheck.isSelected());
        settings.setCloudDensity(cloudDensitySlider.getValue());
        settings.setSoundEnabled(soundEnabledCheck.isSelected());
        settings.setMusicEnabled(musicEnabledCheck.isSelected());
        settings.setSoundVolume(soundVolumeSlider.getValue());
        settings.setMusicVolume(musicVolumeSlider.getValue());
        settings.save();

        // Update music state immediately
        com.labinc.util.SoundManager.getInstance().setMusicVolume(musicVolumeSlider.getValue());
        com.labinc.util.SoundManager.getInstance().updateMusicState();

        // Użyj hoverOverlay jeśli dostępny (szare powiadomienie wjeżdżające od góry)
        if (hoverOverlay != null) {
            hoverOverlay.showNotification("Ustawienia zapisane!", "Niektóre zmiany wymagają restartu gry",
                    new Color(60, 65, 70), new Color(80, 85, 90));
        }
    }

    private void resetToDefaults() {
        cloudsEnabledCheck.setSelected(true);
        cloudDensitySlider.setValue(100);
        soundEnabledCheck.setSelected(true);
        musicEnabledCheck.setSelected(true);
        soundVolumeSlider.setValue(100);
        musicVolumeSlider.setValue(50);
    }

    @FunctionalInterface
    private interface CardContentSupplier {
        JPanel get();
    }
}
