package com.labinc.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

/**
 * Klasa pomocnicza do ładowania i cachowania ikon z folderu resources/icons
 * Obsługuje formaty: PNG, SVG
 */
public class IconLoader {
    private static final Map<String, ImageIcon> cache = new HashMap<>();
    private static final String ICONS_PATH = "/icons/";

    // Mapowanie nazw logicznych na pliki
    public static final String ICON_MINING = "mining";
    public static final String ICON_FACTORY = "factory";
    public static final String ICON_MARKET = "market";
    public static final String ICON_ACHIEVEMENTS = "achievements";
    public static final String ICON_SETTINGS = "settings";
    public static final String ICON_SAVE = "save";
    public static final String ICON_HELP = "help";
    public static final String ICON_LOGO = "logo";
    public static final String ICON_MONEY = "money";
    public static final String ICON_INCOME = "income";
    public static final String ICON_PRESTIGE = "prestige";
    public static final String ICON_SOUND_ON = "sound_on";
    public static final String ICON_SOUND_OFF = "sound_off";
    public static final String ICON_AUTOSAVE = "autosave";
    public static final String ICON_INFO = "info";

    // Fallback teksty (używane gdy brak ikony)
    private static final Map<String, String> fallbackText = new HashMap<>();
    // Mapowanie nazw ikon na nazwy plików PNG w folderze png/
    private static final Map<String, String> pngFileNames = new HashMap<>();
    static {
        fallbackText.put(ICON_MINING, "M");
        fallbackText.put(ICON_FACTORY, "F");
        fallbackText.put(ICON_MARKET, "R");
        fallbackText.put(ICON_ACHIEVEMENTS, "A");
        fallbackText.put(ICON_SETTINGS, "*");
        fallbackText.put(ICON_SAVE, "S");
        fallbackText.put(ICON_HELP, "?");
        fallbackText.put(ICON_LOGO, "L");
        fallbackText.put(ICON_MONEY, "$");
        fallbackText.put(ICON_INCOME, "^");
        fallbackText.put(ICON_PRESTIGE, "*");
        fallbackText.put(ICON_SOUND_ON, "D");
        fallbackText.put(ICON_SOUND_OFF, "X");
        fallbackText.put(ICON_AUTOSAVE, "T");
        fallbackText.put(ICON_INFO, "i");

        // Mapowanie na pliki PNG w folderze png/
        pngFileNames.put(ICON_MINING, "icons8-mining-100");
        pngFileNames.put(ICON_FACTORY, "icons8-factory-100");
        pngFileNames.put(ICON_MARKET, "icons8-stock-100");
        pngFileNames.put(ICON_ACHIEVEMENTS, "icons8-trophy-100");
        pngFileNames.put(ICON_SETTINGS, "icons8-gear-100");
        pngFileNames.put(ICON_SAVE, "icons8-save-100");
        pngFileNames.put(ICON_HELP, "icons8-help-100");
        pngFileNames.put(ICON_LOGO, "icons8-chemistry-100");
        pngFileNames.put(ICON_MONEY, "icons8-money-100");
        pngFileNames.put(ICON_INCOME, "icons8-income-100");
        pngFileNames.put(ICON_PRESTIGE, "icons8-star-100");
        pngFileNames.put(ICON_SOUND_ON, "icons8-speaker-100");
        pngFileNames.put(ICON_SOUND_OFF, "icons8-speaker-100");
        pngFileNames.put(ICON_AUTOSAVE, "icons8-save-100");
        pngFileNames.put(ICON_INFO, "icons8-info-100");
    }

    /**
     * Ładuje ikonę o podanej nazwie i rozmiarze
     * Próbuje kolejno: SVG z resources, PNG z resources, SVG z pliku, PNG z pliku
     * 
     * @param name nazwa ikony (bez rozszerzenia)
     * @param size rozmiar ikony (szerokość i wysokość)
     * @return ImageIcon lub null jeśli nie znaleziono
     */
    public static ImageIcon loadIcon(String name, int size) {
        String cacheKey = name + "_" + size;

        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }

        ImageIcon icon = null;

        // Próbuj SVG z resources
        icon = loadSvgFromResources(name, size);

        // Próbuj PNG z resources
        if (icon == null) {
            icon = loadPngFromResources(name, size);
        }

        // Próbuj SVG z pliku
        if (icon == null) {
            icon = loadSvgFromFile(name, size);
        }

        // Próbuj PNG z pliku
        if (icon == null) {
            icon = loadPngFromFile(name, size);
        }

        if (icon != null) {
            cache.put(cacheKey, icon);
        }

        return icon;
    }

    /**
     * Ładuje SVG z zasobów (classpath) i konwertuje do BufferedImage
     */
    private static ImageIcon loadSvgFromResources(String name, int size) {
        try {
            String path = ICONS_PATH + name + ".svg";
            InputStream is = IconLoader.class.getResourceAsStream(path);
            if (is != null) {
                BufferedImage img = renderSvg(is, size, size);
                is.close();
                if (img != null) {
                    return new ImageIcon(img);
                }
            }
        } catch (Exception e) {
            // Ignoruj
        }
        return null;
    }

    /**
     * Ładuje PNG z zasobów (classpath) - szuka w folderze png/ z mapowaniem nazw
     */
    private static ImageIcon loadPngFromResources(String name, int size) {
        // Pobierz nazwę pliku PNG z mapowania
        String pngFileName = pngFileNames.getOrDefault(name, name);

        // Próbuj z folderu png/
        try {
            String path = ICONS_PATH + "png/" + pngFileName + ".png";
            InputStream is = IconLoader.class.getResourceAsStream(path);
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                is.close();
                return new ImageIcon(resizeImage(img, size, size));
            }
        } catch (Exception e) {
            // Ignoruj
        }

        // Fallback - próbuj bezpośrednio w icons/
        try {
            String path = ICONS_PATH + name + ".png";
            InputStream is = IconLoader.class.getResourceAsStream(path);
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                is.close();
                return new ImageIcon(resizeImage(img, size, size));
            }
        } catch (Exception e) {
            // Ignoruj
        }
        return null;
    }

    /**
     * Ładuje SVG z pliku systemowego
     */
    private static ImageIcon loadSvgFromFile(String name, int size) {
        String[] paths = {
                "src/main/resources/icons/" + name + ".svg",
                "resources/icons/" + name + ".svg",
                "icons/" + name + ".svg"
        };

        for (String path : paths) {
            try {
                File file = new File(path);
                if (file.exists()) {
                    InputStream is = new FileInputStream(file);
                    BufferedImage img = renderSvg(is, size, size);
                    is.close();
                    if (img != null) {
                        return new ImageIcon(img);
                    }
                }
            } catch (Exception e) {
                // Próbuj następną ścieżkę
            }
        }
        return null;
    }

    /**
     * Ładuje PNG z pliku systemowego
     */
    private static ImageIcon loadPngFromFile(String name, int size) {
        String[] paths = {
                "src/main/resources/icons/" + name + ".png",
                "resources/icons/" + name + ".png",
                "icons/" + name + ".png"
        };

        for (String path : paths) {
            try {
                File file = new File(path);
                if (file.exists()) {
                    BufferedImage img = ImageIO.read(file);
                    return new ImageIcon(resizeImage(img, size, size));
                }
            } catch (Exception e) {
                // Próbuj następną ścieżkę
            }
        }
        return null;
    }

    /**
     * Renderuje SVG do BufferedImage używając Apache Batik
     */
    private static BufferedImage renderSvg(InputStream svgInputStream, int width, int height) {
        try {
            // Tworzymy transkoder PNG
            PNGTranscoder transcoder = new PNGTranscoder();

            // Ustawiamy rozmiar wyjściowy
            transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) width);
            transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) height);

            // Wejście SVG
            TranscoderInput input = new TranscoderInput(svgInputStream);

            // Wyjście do pamięci
            java.io.ByteArrayOutputStream ostream = new java.io.ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(ostream);

            // Transkoduj
            transcoder.transcode(input, output);
            ostream.flush();
            ostream.close();

            // Konwertuj bytes do BufferedImage
            byte[] imgData = ostream.toByteArray();
            if (imgData.length < 100) {
                // SVG pusty lub zbyt mały
                return null;
            }
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(imgData);
            BufferedImage result = ImageIO.read(bais);

            // Sprawdź czy obraz nie jest całkowicie przezroczysty
            if (result != null && isImageEmpty(result)) {
                return null;
            }
            return result;

        } catch (Exception e) {
            System.err.println("[IconLoader] Błąd renderowania SVG: " + e.getMessage());
            return null;
        }
    }

    /**
     * Sprawdza czy obraz jest całkowicie przezroczysty/pusty
     */
    private static boolean isImageEmpty(BufferedImage img) {
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int pixel = img.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xff;
                if (alpha > 10) {
                    return false; // Znaleziono nieprzezroczysty piksel
                }
            }
        }
        return true; // Wszystkie piksele przezroczyste
    }

    /**
     * Skaluje obraz do podanego rozmiaru z wysoką jakością
     */
    private static Image resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2.dispose();
        return resizedImage;
    }

    /**
     * Sprawdza czy ikona istnieje
     */
    public static boolean iconExists(String name) {
        return loadIcon(name, 24) != null;
    }

    /**
     * Zwraca tekst zastępczy dla ikony (używany gdy brak pliku)
     */
    public static String getFallbackText(String name) {
        return fallbackText.getOrDefault(name, "?");
    }

    /**
     * Czyści cache ikon
     */
    public static void clearCache() {
        cache.clear();
    }
}
