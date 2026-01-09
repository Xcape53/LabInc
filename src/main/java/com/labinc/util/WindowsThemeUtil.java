package com.labinc.util;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

import javax.swing.*;
import java.awt.*;

/**
 * Narzędzie do ustawienia ciemnego paska tytułowego Windows
 */
public class WindowsThemeUtil {

    private static final int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;

    public interface DwmApi extends com.sun.jna.Library {
        DwmApi INSTANCE = Native.load("dwmapi", DwmApi.class);

        int DwmSetWindowAttribute(HWND hwnd, int dwAttribute, Pointer pvAttribute, int cbAttribute);
    }

    /**
     * Ustawia ciemny pasek tytułowy dla okna JFrame
     */
    public static void setDarkTitleBar(JFrame frame) {
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            return; // Działa tylko na Windows
        }

        try {
            // Poczekaj aż okno zostanie wyświetlone
            if (!frame.isDisplayable()) {
                frame.addNotify();
            }

            // Pobierz HWND okna
            HWND hwnd = getHWND(frame);
            if (hwnd == null) {
                return;
            }

            // Ustaw ciemny motyw (1 = dark mode)
            int[] darkMode = new int[] { 1 };
            Pointer pDarkMode = new com.sun.jna.Memory(4);
            pDarkMode.setInt(0, darkMode[0]);

            DwmApi.INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, pDarkMode, 4);

        } catch (Exception e) {
            System.err.println("Nie udało się ustawić ciemnego paska tytułowego: " + e.getMessage());
        }
    }

    /**
     * Pobiera HWND (Windows Handle) dla JFrame
     */
    private static HWND getHWND(JFrame frame) {
        try {
            // Użyj User32 aby znaleźć okno po tytule
            String title = frame.getTitle();
            if (title != null && !title.isEmpty()) {
                HWND hwnd = User32.INSTANCE.FindWindow(null, title);
                if (hwnd != null) {
                    return hwnd;
                }
            }

            // Jeśli to nie zadziałało, spróbuj przez refleksję
            try {
                java.lang.reflect.Method getPeerMethod = Component.class.getDeclaredMethod("getPeer");
                getPeerMethod.setAccessible(true);
                Object peer = getPeerMethod.invoke(frame);

                if (peer != null) {
                    // Szukaj pola 'hwnd' w klasie peera
                    java.lang.reflect.Field hwndField = null;
                    Class<?> clazz = peer.getClass();

                    while (clazz != null && hwndField == null) {
                        try {
                            hwndField = clazz.getDeclaredField("hwnd");
                        } catch (NoSuchFieldException e) {
                            clazz = clazz.getSuperclass();
                        }
                    }

                    if (hwndField != null) {
                        hwndField.setAccessible(true);
                        long hwndValue = hwndField.getLong(peer);
                        return new HWND(new Pointer(hwndValue));
                    }
                }
            } catch (Exception e) {
                // Ignoruj - użyjemy metody FindWindow
            }

        } catch (Exception e) {
            System.err.println("Błąd podczas pobierania HWND: " + e.getMessage());
        }

        return null;
    }
}
