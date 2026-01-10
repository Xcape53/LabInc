package com.labinc.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;
import javax.swing.Timer;

import com.labinc.util.ElectronConfigCalculator;

/**
 * Panel renderujący interaktywny model atomu w stylu 2.5D.
 * Umożliwia rotację modelu myszką.
 */
public class AtomModelPanel extends JPanel {
    
    private int atomicNumber = 6; // domyślnie węgiel
    @SuppressWarnings("unused")
    private String elementSymbol = "C";
    private int[] electronShells;
    
    // Kąty rotacji (w radianach)
    private double rotationX = 0.3;
    private double rotationY = 0.0;
    
    // Obsługa przeciągania
    private Point lastDragPoint;
    private boolean isDragging = false;
    
    // Kolory
    private static final Color NUCLEUS_COLOR = new Color(255, 100, 50);
    private static final Color ORBIT_COLOR = new Color(100, 150, 255, 80);
    private static final Color ELECTRON_COLOR = new Color(50, 150, 255);
    private static final Color ELECTRON_GLOW = new Color(100, 200, 255, 100);
    private static final Color BG_COLOR = new Color(25, 30, 35);
    
    // Timer dla animacji elektronów
    private final Timer animationTimer;
    private double animationPhase = 0;
    
    public AtomModelPanel() {
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(250, 250));
        setOpaque(false);
        
        // Obsługa myszy
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastDragPoint = e.getPoint();
                isDragging = true;
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                isDragging = false;
                setCursor(Cursor.getDefaultCursor());
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging && lastDragPoint != null) {
                    int dx = e.getX() - lastDragPoint.x;
                    int dy = e.getY() - lastDragPoint.y;
                    
                    rotationY += dx * 0.01;
                    rotationX += dy * 0.01;
                    
                    // Ogranicz rotację X
                    rotationX = Math.max(-Math.PI / 2, Math.min(Math.PI / 2, rotationX));
                    
                    lastDragPoint = e.getPoint();
                    repaint();
                }
            }
        };
        
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        
        // Animacja elektronów
        animationTimer = new Timer(50, e -> {
            animationPhase += 0.05;
            if (animationPhase > 2 * Math.PI) {
                animationPhase -= 2 * Math.PI;
            }
            repaint();
        });
        animationTimer.start();
        
        updateElement();
    }
    
    /**
     * Ustawia pierwiastek do wyświetlenia.
     */
    public void setElement(int atomicNumber, String symbol) {
        this.atomicNumber = atomicNumber;
        this.elementSymbol = symbol;
        updateElement();
        repaint();
    }
    
    private void updateElement() {
        electronShells = ElectronConfigCalculator.getElectronShells(atomicNumber);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int maxRadius = Math.min(getWidth(), getHeight()) / 2 - 20;
        
        // Rysuj ciemne tło z gradientem
        RadialGradientPaint bgGradient = new RadialGradientPaint(
            centerX, centerY, maxRadius * 1.5f,
            new float[]{0f, 1f},
            new Color[]{new Color(40, 45, 55), BG_COLOR}
        );
        g2.setPaint(bgGradient);
        g2.fillOval(centerX - maxRadius, centerY - maxRadius, maxRadius * 2, maxRadius * 2);
        
        // Rysuj jądro NAJPIERW (za elektronami)
        drawNucleus(g2, centerX, centerY);
        
        // Rysuj orbity i elektrony (od zewnątrz do wewnątrz dla poprawnego z-order)
        if (electronShells != null) {
            for (int i = electronShells.length - 1; i >= 0; i--) {
                drawOrbitAndElectrons(g2, centerX, centerY, maxRadius, i, electronShells[i]);
            }
        }

        g2.dispose();
    }
    
    private void drawNucleus(Graphics2D g2, int cx, int cy) {
        // Rozmiar jądra proporcjonalny do numeru atomowego
        int nucleusSize = 15 + (int)(Math.log(atomicNumber + 1) * 5);
        
        // Gradient jądra
        RadialGradientPaint nucleusGradient = new RadialGradientPaint(
            cx - nucleusSize/4, cy - nucleusSize/4, nucleusSize,
            new float[]{0f, 0.5f, 1f},
            new Color[]{new Color(255, 200, 100), NUCLEUS_COLOR, new Color(150, 50, 20)}
        );
        
        g2.setPaint(nucleusGradient);
        g2.fillOval(cx - nucleusSize/2, cy - nucleusSize/2, nucleusSize, nucleusSize);
        
        // Blask jądra
        g2.setColor(new Color(255, 200, 100, 50));
        g2.fillOval(cx - nucleusSize, cy - nucleusSize, nucleusSize * 2, nucleusSize * 2);
    }
    
    private void drawOrbitAndElectrons(Graphics2D g2, int cx, int cy, int maxRadius, int shellIndex, int electronCount) {
        // Promień orbity
        int numShells = electronShells.length;
        int minOrbitRadius = 30;
        int orbitSpacing = (maxRadius - minOrbitRadius) / Math.max(1, numShells);
        int orbitRadius = minOrbitRadius + orbitSpacing * (shellIndex + 1);
        
        // Perspektywa - spłaszczenie elipsy
        double tiltFactor = Math.cos(rotationX);
        int orbitHeight = (int)(orbitRadius * Math.abs(tiltFactor));
        if (orbitHeight < 5) orbitHeight = 5;
        
        // Rysuj orbitę
        g2.setColor(ORBIT_COLOR);
        g2.setStroke(new BasicStroke(1.5f));
        
        // Obrót całej elipsy
        AffineTransform oldTransform = g2.getTransform();
        g2.rotate(rotationY, cx, cy);
        
        g2.drawOval(cx - orbitRadius, cy - orbitHeight/2, orbitRadius * 2, orbitHeight);
        
        // Rysuj elektrony
        for (int e = 0; e < electronCount; e++) {
            double angle = (2 * Math.PI * e / electronCount) + animationPhase * (shellIndex % 2 == 0 ? 1 : -1);
            
            int ex = (int)(cx + orbitRadius * Math.cos(angle));
            int ey = (int)(cy + (orbitHeight/2) * Math.sin(angle));
            
            // Rozmiar i przezroczystość elektronu zależna od pozycji (efekt głębi)
            double depth = Math.sin(angle);
            int electronSize = (int)(6 + depth * 2);
            int alpha = (int)(200 + depth * 55);
            
            // Blask elektronu
            g2.setColor(new Color(ELECTRON_GLOW.getRed(), ELECTRON_GLOW.getGreen(), ELECTRON_GLOW.getBlue(), alpha/2));
            g2.fillOval(ex - electronSize - 2, ey - electronSize - 2, (electronSize + 2) * 2, (electronSize + 2) * 2);
            
            // Elektron
            g2.setColor(new Color(ELECTRON_COLOR.getRed(), ELECTRON_COLOR.getGreen(), ELECTRON_COLOR.getBlue(), alpha));
            g2.fillOval(ex - electronSize/2, ey - electronSize/2, electronSize, electronSize);
        }
        
        g2.setTransform(oldTransform);
    }
    
    /**
     * Zatrzymuje animację (do wywołania przy zamykaniu).
     */
    public void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }
}
