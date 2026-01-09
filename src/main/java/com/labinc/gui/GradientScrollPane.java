package com.labinc.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import com.labinc.util.GameColors;

/**
 * Custom JScrollPane that hides scrollbars and draws fade-out gradients
 * to indicate more content. Supports mouse wheel horizontal scrolling.
 * Gradient intensity is directly proportional to scroll distance.
 */
public class GradientScrollPane extends JScrollPane {

    private static final int MAX_SHADOW_WIDTH = 60; // Maksymalna szerokość gradientu

    // Gradient osiąga maksimum po przescrollowaniu 180px
    private static final int SCROLL_FOR_MAX_GRADIENT = 180;

    public GradientScrollPane(Component view) {
        super(view);

        // Ukryj paski przewijania, ale zachowaj funkcjonalność
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        setBorder(null);
        getViewport().setBackground(GameColors.BG_DARK);
        setBackground(GameColors.BG_DARK);

        // Obsługa przewijania kółkiem myszy (Pionowe kółko -> Poziomy scroll)
        addMouseWheelListener(e -> {
            JScrollBar sb = getHorizontalScrollBar();
            if (sb != null) {
                int amount = e.getWheelRotation() * 40; // Prędkość przewijania
                sb.setValue(sb.getValue() + amount);
            }
        });

        // Repaint przy każdej zmianie scrolla
        getHorizontalScrollBar().addAdjustmentListener(e -> repaint());
    }

    @Override
    public void paintChildren(Graphics g) {
        super.paintChildren(g); // Najpierw narysuj zawartość

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        JScrollBar sb = getHorizontalScrollBar();
        if (sb == null)
            return;

        int val = sb.getValue();
        int max = sb.getMaximum();
        int extent = sb.getVisibleAmount();

        // 1. Lewy Gradient - proporcjonalny do dystansu scrollowania w prawo
        if (val > 0) {
            float progress = Math.min(1.0f, (float) val / SCROLL_FOR_MAX_GRADIENT);
            int shadowWidth = (int) (MAX_SHADOW_WIDTH * progress);
            int alpha = (int) (255 * progress);

            if (shadowWidth > 0) {
                GradientPaint leftGrad = new GradientPaint(
                        0, 0, new Color(35, 35, 35, alpha),
                        shadowWidth, 0, new Color(35, 35, 35, 0));
                g2.setPaint(leftGrad);
                g2.fillRect(0, 0, shadowWidth, h);
            }
        }

        // 2. Prawy Gradient - proporcjonalny do pozostałej treści po prawej
        int remaining = max - val - extent;
        if (remaining > 0) {
            float progress = Math.min(1.0f, (float) remaining / SCROLL_FOR_MAX_GRADIENT);
            int shadowWidth = (int) (MAX_SHADOW_WIDTH * progress);
            int alpha = (int) (255 * progress);

            if (shadowWidth > 0) {
                GradientPaint rightGrad = new GradientPaint(
                        w - shadowWidth, 0, new Color(35, 35, 35, 0),
                        w, 0, new Color(35, 35, 35, alpha));
                g2.setPaint(rightGrad);
                g2.fillRect(w - shadowWidth, 0, shadowWidth, h);
            }
        }
    }
}