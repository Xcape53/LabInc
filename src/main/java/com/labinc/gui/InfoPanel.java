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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.labinc.model.Factory;
import com.labinc.model.GameState;
import com.labinc.model.Recipe;
import com.labinc.model.Resource;
import com.labinc.util.ElectronConfigCalculator;
import com.labinc.util.GameColors;

/**
 * Panel informacji o fabrykach i związkach chemicznych
 */
public class InfoPanel extends JPanel {
    private static final Color BG_DARK = GameColors.BG_DARK;
    private static final Color CARD_BG = GameColors.CARD_BG;
    private static final Color CARD_BORDER = new Color(60, 65, 70);
    private static final Color TEXT_COLOR = new Color(220, 220, 220);
    private static final Color TEXT_DIM = GameColors.TEXT_DIM;
    private static final Color ORANGE = GameColors.ORANGE;
    private static final Color GREEN = GameColors.BTN_GREEN;

    private final GameState gameState;
    private JPanel contentPanel;
    private JScrollPane scrollPane;
    private java.util.Map<String, JPanel> factoryCards = new java.util.HashMap<>();

    // Cache obrazków fabryk
    private Map<String, BufferedImage> factoryImages = new HashMap<>();

    // Dane o surowcach - rozszerzone informacje
    private static final Map<String, ResourceInfo> RESOURCE_INFO = new HashMap<>();

    // Opisy fabryk - jak działaja
    private static final Map<String, String> FACTORY_DESCRIPTIONS = new HashMap<>();
    static {
        FACTORY_DESCRIPTIONS.put("KW", "Kopalnia głębinowa wydobywająca węgiel kamienny z pokładów pod ziemią.");
        FACTORY_DESCRIPTIONS.put("SN", "Separacja powietrza metodą kriogeniczną - skraplanie i destylacja frakcyjna.");
        FACTORY_DESCRIPTIONS.put("KDA", "Elektroliza solanki produkująca wodorotlenek sodu, chlor i wodór.");
        FACTORY_DESCRIPTIONS.put("SAL", "Wydobycie soli i minerałów z solanek i złóż kopalnianych.");
        FACTORY_DESCRIPTIONS.put("KRZ", "Kopalnia odkrywkowa rudy żelaza z procesem wzbogacania magnetycznego.");
        FACTORY_DESCRIPTIONS.put("KB", "Kopalnia boksytów z procesem Bayera do produkcji aluminium.");
        FACTORY_DESCRIPTIONS.put("KRM", "Kopalnia metali szlachetnych z rafinacją elektrolityczną.");
        FACTORY_DESCRIPTIONS.put("KRCO", "Wydobycie rud kobaltu i niklu metodą hydrometalurgiczną HPAL.");
        FACTORY_DESCRIPTIONS.put("KRN", "Kopalnia rud nieżelaznych z flotacją i elektrorafinacją.");
        FACTORY_DESCRIPTIONS.put("KMW", "Kopalnia metali wysokotopliwych z redukcją w piecach łukowych.");
        FACTORY_DESCRIPTIONS.put("KF", "Kopalnia fluorytu z procesem produkcji fluorowodoru.");
        FACTORY_DESCRIPTIONS.put("KRCW", "Kopalnia rud ciężkich z separacją grawitacyjną.");
        FACTORY_DESCRIPTIONS.put("KP", "Kopalnia platynowców z rafinacją chemiczną i elektrolizą.");
        FACTORY_DESCRIPTIONS.put("KZR", "Kopalnia ziem rzadkich z ekstrakcją rozpuszczalnikową.");
        FACTORY_DESCRIPTIONS.put("EJ", "Elektrownia jądrowa z reaktorem wodnym ciśnieniowym.");
        FACTORY_DESCRIPTIONS.put("RJ", "Reaktor jądrowy produkujący izotopy poprzez napromieniowanie neutronami.");
    }

    static class ResourceInfo {
        String[] facts; // 3-5 faktów
        String[] compounds; // 3 związki chemiczne
        String miningReason; // Dlaczego wydobywany w tej fabryce

        ResourceInfo(String miningReason, String[] facts, String[] compounds) {
            this.miningReason = miningReason;
            this.facts = facts;
            this.compounds = compounds;
        }
    }

    static {
        loadElementInfoFromJson();
    }

    private static void loadElementInfoFromJson() {
        try {
            java.io.InputStream is = InfoPanel.class.getResourceAsStream("/element_info.json");
            if (is == null) {
                System.err.println("[InfoPanel] Nie znaleziono element_info.json");
                return;
            }

            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            String json = sb.toString();
            // Parsuj JSON ręcznie (bez zewnętrznej biblioteki)
            // Wyszukaj obiekty elementów - szukamy "symbol": i cofamy się do {
            int searchPos = 0;
            while ((searchPos = json.indexOf("\"symbol\":", searchPos)) != -1) {
                // Cofnij się do poprzedzającego {
                int elemStart = json.lastIndexOf("{", searchPos);
                if (elemStart == -1) {
                    searchPos++;
                    continue;
                }

                // Znajdź koniec obiektu (uwzględnij zagnieżdżone tablice i obiekty)
                int bracketCount = 1;
                int i = elemStart + 1;
                while (i < json.length() && bracketCount > 0) {
                    char c = json.charAt(i);
                    if (c == '{')
                        bracketCount++;
                    else if (c == '}')
                        bracketCount--;
                    i++;
                }
                int elemEnd = i;

                String elemJson = json.substring(elemStart, elemEnd);

                // Wyodrębnij symbol
                String symbol = extractJsonString(elemJson, "symbol");
                if (symbol == null) {
                    searchPos = elemEnd;
                    continue;
                }

                // Wyodrębnij miningReason
                String miningReason = extractJsonString(elemJson, "miningReason");
                if (miningReason == null)
                    miningReason = "";

                // Wyodrębnij facts (tablica)
                String[] facts = extractJsonArray(elemJson, "facts");
                if (facts == null)
                    facts = new String[0];

                // Domyślna tablica związków (pusta)
                String[] compounds = new String[] { "Tlenki", "Sole", "Związki" };

                RESOURCE_INFO.put(symbol, new ResourceInfo(miningReason, facts, compounds));

                searchPos = elemEnd;
            }

            System.out.println("[InfoPanel] Załadowano info dla " + RESOURCE_INFO.size() + " pierwiastków z JSON");

            // Add DEFAULT fallback
            if (!RESOURCE_INFO.containsKey("DEFAULT")) {
                RESOURCE_INFO.put("DEFAULT", new ResourceInfo(
                        "Brak szczegółowych informacji o wydobyciu tego surowca.",
                        new String[] { "Ten surowiec jest ważnym elementem gospodarki.", "Więcej informacji wkrótce." },
                        new String[] { "Związki" }));
            }

        } catch (Exception e) {
            System.err.println("[InfoPanel] Błąd ładowania element_info.json: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\":";
        int idx = json.indexOf(pattern);
        if (idx == -1)
            return null;

        int valueStart = json.indexOf("\"", idx + pattern.length());
        if (valueStart == -1)
            return null;
        valueStart++;

        // Szukaj końca stringa (uwzględnij escape'y)
        int valueEnd = valueStart;
        while (valueEnd < json.length()) {
            char c = json.charAt(valueEnd);
            if (c == '\\' && valueEnd + 1 < json.length()) {
                valueEnd += 2; // Pomiń escaped char
                continue;
            }
            if (c == '"')
                break;
            valueEnd++;
        }

        String value = json.substring(valueStart, valueEnd);
        // Odkoduj podstawowe escape'y
        value = value.replace("\\\"", "\"").replace("\\n", "\n").replace("\\\\", "\\");
        return value;
    }

    private static String[] extractJsonArray(String json, String key) {
        String pattern = "\"" + key + "\":";
        int idx = json.indexOf(pattern);
        if (idx == -1)
            return null;

        int arrStart = json.indexOf("[", idx);
        if (arrStart == -1)
            return null;

        int arrEnd = json.indexOf("]", arrStart);
        if (arrEnd == -1)
            return null;

        String arrContent = json.substring(arrStart + 1, arrEnd);

        java.util.List<String> items = new java.util.ArrayList<>();
        int i = 0;
        while (i < arrContent.length()) {
            // Znajdź początek stringa
            int strStart = arrContent.indexOf("\"", i);
            if (strStart == -1)
                break;
            strStart++;

            // Znajdź koniec stringa
            int strEnd = strStart;
            while (strEnd < arrContent.length()) {
                char c = arrContent.charAt(strEnd);
                if (c == '\\' && strEnd + 1 < arrContent.length()) {
                    strEnd += 2;
                    continue;
                }
                if (c == '"')
                    break;
                strEnd++;
            }

            String item = arrContent.substring(strStart, strEnd);
            item = item.replace("\\\"", "\"").replace("\\n", "\n").replace("\\\\", "\\");
            items.add(item);

            i = strEnd + 1;
        }

        return items.toArray(new String[0]);
    }

    public InfoPanel(GameState gameState) {
        this.gameState = gameState;
        setBackground(BG_DARK);
        setLayout(new BorderLayout());

        loadFactoryImages();
        initComponents();
    }

    private void loadFactoryImages() {
        // Ścieżka do obrazków fabryk (assets/main/factory/)
        String basePath = System.getProperty("user.dir");
        // Spróbuj różne ścieżki
        String[] possiblePaths = {
                basePath + "/../assets/main/factory/",
                basePath + "/../../assets/main/factory/",
                "C:/Users/user/Documents/GitHub/LabIncTest/assets/main/factory/"
        };

        for (String path : possiblePaths) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                for (int i = 1; i <= 16; i++) {
                    try {
                        File imgFile = new File(path + i + ".png");
                        if (imgFile.exists()) {
                            BufferedImage img = ImageIO.read(imgFile);
                            factoryImages.put(String.valueOf(i), img);
                        }
                    } catch (Exception e) {
                        System.err.println("[InfoPanel] Nie można wczytać obrazka fabryki " + i);
                    }
                }
                if (!factoryImages.isEmpty())
                    break;
            }
        }
        System.out.println("[InfoPanel] Wczytano " + factoryImages.size() + " obrazków fabryk");
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("INFORMACJE");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(ORANGE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_DARK);

        scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBackground(BG_DARK);
        scrollPane.getViewport().setBackground(BG_DARK);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        // Ukryj scrollbar ale zachowaj scrollowanie
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        refreshContent();
    }

    private void refreshContent() {
        contentPanel.removeAll();
        showFactoriesInfo();
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showFactoriesInfo() {
        int index = 1;
        for (java.util.Map.Entry<String, Factory> entry : gameState.getFactories().entrySet()) {
            Factory factory = entry.getValue();

            // Pokaż tylko aktywne fabryki (tier > 0)
            if (factory.getCurrentTier() <= 0) {
                index++;
                continue;
            }

            JPanel card = createFactoryInfoCard(factory, index);
            contentPanel.add(card);
            contentPanel.add(Box.createVerticalStrut(20));
            // Zapisz kartę do mapy dla późniejszego scrollowania
            factoryCards.put(factory.getShortName(), card);
            index++;
        }

        if (contentPanel.getComponentCount() == 0) {
            JLabel emptyLabel = new JLabel("Brak aktywnych fabryk");
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 18));
            emptyLabel.setForeground(TEXT_DIM);
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(emptyLabel);
        }
    }

    private JPanel createFactoryInfoCard(Factory factory, int factoryIndex) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(CARD_BORDER);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(15, 10));
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Lewa strona: obrazek fabryki (wycentrowany, bez stretchowania)
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(270, 270));

        BufferedImage factoryImg = factoryImages.get(String.valueOf(factoryIndex));
        if (factoryImg != null) {
            // Zachowaj proporcje obrazka
            int maxSize = 260;
            int origW = factoryImg.getWidth();
            int origH = factoryImg.getHeight();
            double scale = Math.min((double) maxSize / origW, (double) maxSize / origH);
            int scaledW = (int) (origW * scale);
            int scaledH = (int) (origH * scale);
            Image scaledImg = factoryImg.getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH);
            JLabel imgLabel = new JLabel(new ImageIcon(scaledImg));
            imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            leftPanel.add(Box.createVerticalGlue());
            leftPanel.add(imgLabel);
            leftPanel.add(Box.createVerticalGlue());
        } else {
            JLabel placeholder = new JLabel("[IMG]");
            placeholder.setFont(new Font("Arial", Font.BOLD, 24));
            placeholder.setForeground(TEXT_DIM);
            placeholder.setAlignmentX(Component.CENTER_ALIGNMENT);
            leftPanel.add(Box.createVerticalGlue());
            leftPanel.add(placeholder);
            leftPanel.add(Box.createVerticalGlue());
        }

        card.add(leftPanel, BorderLayout.WEST);

        // Prawa strona: informacje
        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        // Nazwa fabryki
        JLabel nameLabel = new JLabel(factory.getFullName().toUpperCase());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 22));
        nameLabel.setForeground(ORANGE);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(nameLabel);
        rightPanel.add(Box.createVerticalStrut(5));

        // Status
        JLabel statusLabel = new JLabel("Odblokowana");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statusLabel.setForeground(GREEN);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(statusLabel);
        rightPanel.add(Box.createVerticalStrut(3));

        // Tier
        JLabel tierLabel = new JLabel("Tier: " + factory.getCurrentTier() + " / " + factory.getMaxTier());
        tierLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        tierLabel.setForeground(TEXT_COLOR);
        tierLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(tierLabel);
        rightPanel.add(Box.createVerticalStrut(10));

        // Symbole pierwiastków - klikalne
        JLabel resourcesHeader = new JLabel("SUROWCE (kliknij aby zobaczyć szczegóły):");
        resourcesHeader.setFont(new Font("Arial", Font.BOLD, 12));
        resourcesHeader.setForeground(TEXT_DIM);
        resourcesHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(resourcesHeader);
        rightPanel.add(Box.createVerticalStrut(5));

        JPanel symbolsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        symbolsPanel.setOpaque(false);
        symbolsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        Map<String, Double> production = factory.getCurrentProduction();
        for (String symbol : production.keySet()) {
            JLabel symbolBtn = createClickableSymbol(symbol, factory);
            symbolsPanel.add(symbolBtn);
        }

        if (production.isEmpty()) {
            JLabel noResLabel = new JLabel("Aktywuj fabrykę aby zobaczyć surowce");
            noResLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            noResLabel.setForeground(TEXT_DIM);
            symbolsPanel.add(noResLabel);
        }

        rightPanel.add(symbolsPanel);

        // Sekcja: Wykorzystywane w fabrykach chemicznych
        rightPanel.add(Box.createVerticalStrut(10));
        JLabel chemHeader = new JLabel("WYKORZYSTYWANE W FABRYKACH CHEMICZNYCH:");
        chemHeader.setFont(new Font("Arial", Font.BOLD, 12));
        chemHeader.setForeground(TEXT_DIM);
        chemHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(chemHeader);
        rightPanel.add(Box.createVerticalStrut(3));

        // Opis fabryki - jedno zdanie
        String factoryDesc = FACTORY_DESCRIPTIONS.get(factory.getShortName());
        if (factoryDesc != null) {
            JLabel descLabel = new JLabel(factoryDesc);
            descLabel.setFont(new Font("Arial", Font.ITALIC, 11));
            descLabel.setForeground(TEXT_COLOR);
            descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            rightPanel.add(descLabel);
        }
        rightPanel.add(Box.createVerticalStrut(5));

        // Znajdz receptury wykorzystujace surowce z tej fabryki
        java.util.Set<String> usedRecipes = new java.util.LinkedHashSet<>();
        for (String symbol : production.keySet()) {
            for (Recipe recipe : gameState.getRecipes().values()) {
                if (recipe.getInputs().containsKey(symbol)) {
                    usedRecipes.add(recipe.getName());
                }
            }
        }

        JPanel recipesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
        recipesPanel.setOpaque(false);
        recipesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (usedRecipes.isEmpty()) {
            JLabel noRecLabel = new JLabel("Brak dostepnych receptur");
            noRecLabel.setFont(new Font("Arial", Font.ITALIC, 11));
            noRecLabel.setForeground(TEXT_DIM);
            recipesPanel.add(noRecLabel);
        } else {
            for (String recipeName : usedRecipes) {
                JLabel recLabel = new JLabel(recipeName);
                recLabel.setFont(new Font("Arial", Font.PLAIN, 11));
                recLabel.setForeground(new Color(100, 200, 255));
                recLabel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(80, 150, 200), 1),
                        BorderFactory.createEmptyBorder(2, 6, 2, 6)));
                recipesPanel.add(recLabel);
            }
        }
        rightPanel.add(recipesPanel);

        card.add(rightPanel, BorderLayout.CENTER);

        return card;
    }

    private JLabel createClickableSymbol(String symbol, Factory factory) {
        Resource res = gameState.getResources().get(symbol);
        String name = res != null ? res.getName() : symbol;

        JLabel label = new JLabel(symbol) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Tło
                g2.setColor(new Color(60, 65, 70));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Obramowanie
                g2.setColor(ORANGE);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

                // Tekst
                g2.setColor(ORANGE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setOpaque(false);
        label.setPreferredSize(new Dimension(50, 40));
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.setToolTipText(name + " - kliknij po szczegóły");

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showResourceInfoDialog(symbol, factory);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                label.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                label.repaint();
            }
        });

        return label;
    }

    private void showResourceInfoDialog(String symbol, Factory factory) {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

        // Dialog modalny 80% ekranu
        JDialog dialog = new JDialog(parentFrame, "Informacje o " + symbol, true);
        dialog.setUndecorated(true);

        int screenWidth = parentFrame.getWidth();
        int screenHeight = parentFrame.getHeight();
        int contentWidth = (int) (screenWidth * 0.8);
        int contentHeight = (int) (screenHeight * 0.8);

        // Pełny rozmiar dialogu (cały ekran)
        dialog.setSize(screenWidth, screenHeight);
        dialog.setLocationRelativeTo(parentFrame);

        // Główny panel z przyciemnionym tłem
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(40, 45, 50));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(ORANGE);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);
                g2.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Pobierz dane
        Resource res = gameState.getResources().get(symbol);
        String name = res != null ? res.getName() : symbol;
        ResourceInfo info = RESOURCE_INFO.getOrDefault(symbol, RESOURCE_INFO.get("DEFAULT"));

        // Nagłówek
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(symbol + " - " + name);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(ORANGE);

        JButton closeBtn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(200, 60, 60) : new Color(80, 85, 90));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                // Rysuj X
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int pad = 12;
                g2.drawLine(pad, pad, getWidth() - pad, getHeight() - pad);
                g2.drawLine(getWidth() - pad, pad, pad, getHeight() - pad);
                g2.dispose();
            }
        };
        closeBtn.setFont(new Font("Arial", Font.BOLD, 18));
        closeBtn.setPreferredSize(new Dimension(40, 40));
        closeBtn.setOpaque(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dialog.dispose());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(closeBtn, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Content
        JPanel contentPnl = new JPanel();
        contentPnl.setOpaque(false);
        contentPnl.setLayout(new BoxLayout(contentPnl, BoxLayout.Y_AXIS));
        contentPnl.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // 0. Model atomu (interaktywny)
        int atomicNumber = ElectronConfigCalculator.getAtomicNumber(symbol);
        if (atomicNumber > 0) {
            AtomModelPanel atomPanel = new AtomModelPanel();
            atomPanel.setElement(atomicNumber, symbol);
            atomPanel.setPreferredSize(new Dimension(280, 280));
            atomPanel.setMaximumSize(new Dimension(280, 280));
            atomPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Wrapper dla wyśrodkowania
            JPanel atomWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
            atomWrapper.setOpaque(false);
            atomWrapper.add(atomPanel);
            atomWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPnl.add(atomWrapper);
            contentPnl.add(Box.createVerticalStrut(25));

            // Zatrzymaj animację przy zamykaniu
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    atomPanel.stopAnimation();
                }
            });
        }

        // 1. Dlaczego wydobywany w tej fabryce
        contentPnl.add(createInfoSection("WYDOBYCIE W FABRYCE:", info.miningReason));
        contentPnl.add(Box.createVerticalStrut(25));

        // 2. Informacje ogólne (3-5 faktów)
        StringBuilder factsHtml = new StringBuilder("<html><ul style='margin-left:10px'>");
        for (String fact : info.facts) {
            factsHtml.append("<li style='margin-bottom:8px'>").append(fact).append("</li>");
        }
        factsHtml.append("</ul></html>");
        contentPnl.add(createInfoSection("INFORMACJE OGOLNE:", factsHtml.toString()));
        contentPnl.add(Box.createVerticalStrut(25));

        // 3. Związki chemiczne - dynamicznie z receptur gry
        StringBuilder compoundsHtml = new StringBuilder("<html><ul style='margin-left:10px'>");
        java.util.List<String> usedInRecipes = new java.util.ArrayList<>();
        for (Recipe recipe : gameState.getRecipes().values()) {
            if (recipe.getInputs().containsKey(symbol)) {
                usedInRecipes.add(recipe.getName() + " (" + recipe.getOutputResource() + ")");
            }
        }
        if (usedInRecipes.isEmpty()) {
            compoundsHtml.append("<li>Brak dostępnych receptur</li>");
        } else {
            for (String recipeName : usedInRecipes) {
                compoundsHtml.append("<li style='margin-bottom:8px'>").append(recipeName).append("</li>");
            }
        }
        compoundsHtml.append("</ul></html>");
        contentPnl.add(createInfoSection("ZWIAZKI CHEMICZNE:", compoundsHtml.toString()));

        JScrollPane scroll = new JScrollPane(contentPnl);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        // Ukryj scrollbar ale zachowaj scrollowanie
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        mainPanel.add(scroll, BorderLayout.CENTER);

        // Panel z przyciemnionym tłem 40% opacity
        JPanel overlayPanel = new JPanel(new java.awt.GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 102)); // 102/255 = ~40%
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        overlayPanel.setOpaque(false);
        overlayPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Animacja zamykania
                final float[] closeProgress = { 0f };
                final int closeDuration = 150;
                final int closeFps = 60;
                final int closeDelay = 1000 / closeFps;

                Timer closeAnim = new Timer(closeDelay, null);
                closeAnim.addActionListener(evt -> {
                    closeProgress[0] += (float) closeDelay / closeDuration;
                    if (closeProgress[0] >= 1f) {
                        closeAnim.stop();
                        dialog.dispose();
                        return;
                    }
                    // Ease in quad
                    float t = closeProgress[0];
                    float ease = t * t;

                    int currentW = (int) (contentWidth * (1f - 0.2f * ease));
                    int currentH = (int) (contentHeight * (1f - 0.2f * ease));
                    mainPanel.setPreferredSize(new Dimension(currentW, currentH));
                    mainPanel.revalidate();
                    overlayPanel.repaint();
                });
                closeAnim.start();
            }
        });

        mainPanel.setPreferredSize(new Dimension(contentWidth, contentHeight));
        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                e.consume();
            }
        });

        overlayPanel.add(mainPanel);
        dialog.setContentPane(overlayPanel);
        dialog.setBackground(new Color(0, 0, 0, 0));

        // Animacja otwierania - rozpocznij od małego rozmiaru
        final int targetW = contentWidth;
        final int targetH = contentHeight;
        mainPanel.setPreferredSize(new Dimension((int) (targetW * 0.8f), (int) (targetH * 0.8f)));

        final float[] animProgress = { 0f };
        final int animDuration = 200; // ms
        final int fps = 60;
        final int delay = 1000 / fps;

        Timer openAnim = new Timer(delay, null);
        openAnim.addActionListener(e -> {
            animProgress[0] += (float) delay / animDuration;
            if (animProgress[0] >= 1f) {
                animProgress[0] = 1f;
                openAnim.stop();
            }
            // Ease out quad
            float t = animProgress[0];
            float ease = t * (2 - t);

            int currentW = (int) (targetW * (0.8f + 0.2f * ease));
            int currentH = (int) (targetH * (0.8f + 0.2f * ease));
            mainPanel.setPreferredSize(new Dimension(currentW, currentH));
            mainPanel.revalidate();
            overlayPanel.repaint();
        });

        // Uruchom timer PRZED pokazaniem dialogu
        openAnim.start();
        dialog.setVisible(true);
    }

    private JPanel createInfoSection(String title, String content) {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(ORANGE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(titleLabel);
        section.add(Box.createVerticalStrut(8));

        // Zawijanie tekstu - użyj HTML z max-width
        String wrappedContent = content;
        if (!wrappedContent.startsWith("<html>")) {
            wrappedContent = "<html><body style='width: 1093px'>" + content + "</body></html>";
        } else {
            // Zamień <html> na <html><body style='width: 1093px'>
            wrappedContent = wrappedContent.replace("<html>", "<html><body style='width: 1093px'>");
            wrappedContent = wrappedContent.replace("</html>", "</body></html>");
        }
        JLabel contentLabel = new JLabel(wrappedContent);
        contentLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        contentLabel.setForeground(TEXT_COLOR);
        contentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(contentLabel);

        return section;
    }

    public void updateDisplay() {
        refreshContent();
    }

    /**
     * Scrolluje widok do karty fabryki o podanej nazwie z płynną animacją (500ms)
     */
    public void scrollToFactory(String factoryName) {
        factoryCards.clear(); // Wyczyść przed odświeżeniem
        refreshContent();

        SwingUtilities.invokeLater(() -> {
            JPanel card = factoryCards.get(factoryName);
            if (card != null && scrollPane != null) {
                // Daj czas na pełne renderowanie
                Timer delayTimer = new Timer(100, ev -> {
                    ((Timer) ev.getSource()).stop();

                    java.awt.Rectangle targetRect = card.getBounds();
                    int startY = scrollPane.getViewport().getViewPosition().y;
                    int targetY = Math.max(0, targetRect.y - 20);

                    // Animacja scrollowania (500ms)
                    final int duration = 500;
                    final int fps = 60;
                    final int delay = 1000 / fps;
                    final float[] progress = { 0f };

                    Timer scrollAnim = new Timer(delay, null);
                    scrollAnim.addActionListener(e2 -> {
                        progress[0] += (float) delay / duration;
                        if (progress[0] >= 1f) {
                            progress[0] = 1f;
                            scrollAnim.stop();
                        }
                        // Bell curve / smoothstep easing (wolny start, szybki środek, wolny koniec)
                        float t = progress[0];
                        float ease = t * t * (3f - 2f * t); // Smoothstep formula

                        int currentY = (int) (startY + (targetY - startY) * ease);
                        scrollPane.getViewport().setViewPosition(new java.awt.Point(0, currentY));
                    });
                    scrollAnim.start();
                });
                delayTimer.setRepeats(false);
                delayTimer.start();
            }
        });
    }
}
