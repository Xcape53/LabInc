package com.labinc.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.Timer;

import com.labinc.model.GameState;
import com.labinc.model.Recipe;
import com.labinc.model.Resource;
import com.labinc.util.Formatter;
import com.labinc.util.GameColors;

/**
 * Pionowa kolumna przemysłowa do syntezy chemicznej.
 * V18: Final Button Colors + UI Layout Fixes.
 */
public class ChemicalColumn extends JPanel {
    private final Recipe recipe;
    private final GameState gameState;
    private final Timer animationTimer;

    private enum State {
        GAS, LIQUID, SOLID
    }

    private final State state;

    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();
    private float time = 0.0f;
    private boolean isProducing = false;

    private double pileSeed;

    private float currentScale = 1.0f;

    // UI - rysowany ręcznie (nie Swing)
    private final Rectangle upgradeBtnBounds; // Bounds w wirtualnych współrzędnych (przed skalowaniem)
    private boolean isHoveringBtn = false;

    // Wymiary
    private static final int PANEL_W = 550;
    private static final int PANEL_H = 700; // Stała wysokość kolumny
    private static final int PIPE_WIDTH = 140;
    private static final int MAIN_PIPE_Y = 80;
    private static final float PIPE_RADIUS = 25f;
    private static final int BOTTOM_SECTION_HEIGHT = 220; // Stała wysokość dolnej sekcji (zwiększone)

    // Kolory
    private static final Color METAL_DARK = new Color(35, 35, 35);
    private static final Color METAL_LIGHT = new Color(64, 64, 64);
    private static final Color GLASS_BG = new Color(15, 20, 25);
    private static final Color GLASS_HIGHLIGHT = new Color(255, 255, 255, 20);

    // Kolory Przycisków
    private static final Color BTN_ORANGE = GameColors.ORANGE;
    private static final Color BTN_GREEN = GameColors.BTN_GREEN;
    private static final Color BTN_DISABLED_BG = new Color(100, 100, 100);
    private static final Color BTN_DISABLED_FG = new Color(70, 70, 70);

    public ChemicalColumn(Recipe recipe, GameState gameState, int index) {
        this.recipe = recipe;
        this.gameState = gameState;
        this.state = determineState(recipe.getOutputResource());
        this.pileSeed = random.nextDouble() * 100.0;

        setPreferredSize(new Dimension(PANEL_W, PANEL_H));
        setMaximumSize(new Dimension(PANEL_W, PANEL_H));
        setMinimumSize(new Dimension(PANEL_W, PANEL_H));
        setOpaque(false);

        // Wirtualne współrzędne przycisku (przed skalowaniem)
        int btnX = 100;
        int btnY = PANEL_H - BOTTOM_SECTION_HEIGHT + 100;
        int btnW = PANEL_W - 200;
        int btnH = 50;
        upgradeBtnBounds = new Rectangle(btnX, btnY, btnW, btnH);

        // Obsługa kliknięć myszy
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isClickOnButton(e.getX(), e.getY())) {
                    handleUpgradeClick();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean wasHovering = isHoveringBtn;
                isHoveringBtn = isClickOnButton(e.getX(), e.getY());
                if (wasHovering != isHoveringBtn) {
                    setCursor(
                            isHoveringBtn ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
                    repaint();
                }
            }
        });

        animationTimer = new Timer(33, e -> updateAnimation());
        animationTimer.start();
    }

    private boolean isClickOnButton(int mouseX, int mouseY) {
        // Przelicz współrzędne myszy na wirtualne współrzędne
        int virtualX = (int) (mouseX / currentScale);
        int virtualY = (int) (mouseY / currentScale);
        return upgradeBtnBounds.contains(virtualX, virtualY);
    }

    private void handleUpgradeClick() {
        com.labinc.util.SoundManager.getInstance().playClick1();
        double cost = recipe.getUpgradeCost();
        if (gameState.getMoney() >= cost) {
            gameState.upgradeRecipe(recipe.getId());
            pileSeed = random.nextDouble() * 100.0;
            repaint();
        }
    }

    private State determineState(String resource) {
        if (resource.equals("H2O") || resource.equals("H2SO4"))
            return State.LIQUID;
        if (resource.equals("NaCl"))
            return State.SOLID;
        return State.GAS;
    }

    private void updateAnimation() {
        time += 0.05f;
        boolean hasResources = checkResources();
        isProducing = hasResources;

        if (isProducing) {
            generateParticles();
            moveParticles();
        } else {
            particles.clear();
        }
        repaint();
    }

    private boolean checkResources() {
        if (recipe.getLevel() <= 0)
            return false;
        for (Map.Entry<String, Double> input : recipe.getInputs().entrySet()) {
            Resource r = gameState.getResources().get(input.getKey());
            if (r == null || r.getAmount() < input.getValue() * 0.1)
                return false;
        }
        return true;
    }

    private void generateParticles() {
        int centerX = PANEL_W / 2;
        int minX = centerX - PIPE_WIDTH / 2 + 10;
        int pipeH = PANEL_H - MAIN_PIPE_Y - BOTTOM_SECTION_HEIGHT;
        int bottomY = MAIN_PIPE_Y + pipeH;

        if (state == State.GAS) {
            if (random.nextFloat() < 0.6f) {
                double startX = minX + random.nextInt(PIPE_WIDTH - 20);
                particles.add(
                        new Particle(startX, bottomY - 10, 2 + random.nextFloat() * 3, 4 + random.nextFloat() * 6));
            }
        } else if (state == State.LIQUID) {
            if (random.nextFloat() < 0.3f) {
                double startX = minX + random.nextInt(PIPE_WIDTH - 20);
                particles.add(
                        new Particle(startX, bottomY - 10, 1 + random.nextFloat() * 2, 5 + random.nextFloat() * 8));
            }
        } else if (state == State.SOLID) {
            if (random.nextFloat() < 0.5f) {
                double startX = minX + random.nextInt(PIPE_WIDTH - 20);
                particles.add(new Particle(startX, MAIN_PIPE_Y + 20, -3 - random.nextFloat() * 3,
                        3 + random.nextFloat() * 3));
            }
        }
    }

    private void moveParticles() {
        int pipeH = PANEL_H - MAIN_PIPE_Y - BOTTOM_SECTION_HEIGHT;
        float fillPerc = Math.min(1.0f, 0.2f + (recipe.getLevel() / 25.0f));
        int fillH = (int) (pipeH * fillPerc);
        int surfaceY = MAIN_PIPE_Y + pipeH - fillH;
        int bottomY = MAIN_PIPE_Y + pipeH;

        for (int i = 0; i < particles.size(); i++) {
            Particle p = particles.get(i);

            if (state == State.SOLID) {
                p.y -= p.vy;
                p.x += (random.nextFloat() - 0.5) * 1.0;

                double normX = ((p.x - (PANEL_W / 2 - PIPE_WIDTH / 2)) / (double) PIPE_WIDTH) * Math.PI;
                double bigHill = Math.sin(normX) * 25.0;
                double randomShape = Math.sin((p.x - (PANEL_W / 2 - PIPE_WIDTH / 2)) * 0.08 + pileSeed) * 6.0;

                double pileY = bottomY - (fillH + bigHill + randomShape + 10);

                if (p.y > pileY)
                    particles.remove(i--);

            } else {
                p.y -= p.vy;
                p.x += Math.sin(time * 2 + p.y * 0.02) * 1.5;

                if (state == State.GAS) {
                    p.size += 0.05;
                    if (p.y < surfaceY) {
                        p.size -= 0.5;
                        if (p.size <= 0)
                            particles.remove(i--);
                    }
                } else {
                    if (p.y < surfaceY)
                        particles.remove(i--);
                }
            }
        }
    }

    // updateStatus nie jest już potrzebne, wszystko rysujemy w paintComponent
    public void updateStatus() {
        repaint();
    }

    public void updateScale(float scale) {
        this.currentScale = scale;

        // Skaluj rozmiar panelu
        Dimension dim = new Dimension((int) (PANEL_W * scale), (int) (PANEL_H * scale));
        setPreferredSize(dim);
        setMaximumSize(dim);
        setMinimumSize(dim);

        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Zastosuj skalę
        g2.scale(currentScale, currentScale);

        int w = PANEL_W;
        int h = PANEL_H;

        // Tło
        GradientPaint metalBg = new GradientPaint(0, 0, METAL_LIGHT, w, 0, METAL_DARK);
        g2.setPaint(metalBg);
        g2.fillRoundRect(15, 15, w - 30, h - 30, 30, 30);
        g2.setColor(new Color(25, 25, 25));
        g2.setStroke(new BasicStroke(4f));
        g2.drawRoundRect(15, 15, w - 30, h - 30, 30, 30);
        drawBolts(g2, w, h);

        // Tytuł
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        String title = recipe.getName();
        g2.drawString(title, w / 2 - g2.getFontMetrics().stringWidth(title) / 2, 60);

        int pipeH = h - MAIN_PIPE_Y - BOTTOM_SECTION_HEIGHT;
        int pipeX = w / 2 - PIPE_WIDTH / 2;

        // Definicja kształtu rury
        java.awt.geom.RoundRectangle2D.Float tubeShape = new java.awt.geom.RoundRectangle2D.Float(pipeX, MAIN_PIPE_Y,
                PIPE_WIDTH, pipeH, 20, 20);

        drawLongPipes(g2, w, pipeX, MAIN_PIPE_Y, PIPE_WIDTH, pipeH);

        g2.setColor(GLASS_BG);
        g2.fill(tubeShape);

        drawSubtleOutput(g2, w / 2, MAIN_PIPE_Y + pipeH, recipe.getOutputResource());

        if (isProducing) {
            Color c = getMoleculeColor(recipe.getOutputResource());
            float fillPercent = Math.min(1.0f, 0.2f + (recipe.getLevel() / 25.0f));
            int currentHeight = (int) (pipeH * fillPercent);
            int surfaceY = MAIN_PIPE_Y + pipeH - currentHeight;

            Shape clip = g2.getClip();
            g2.clip(tubeShape);

            if (state == State.GAS) {
                GradientPaint gasGrad = new GradientPaint(
                        pipeX, MAIN_PIPE_Y + pipeH, new Color(c.getRed(), c.getGreen(), c.getBlue(), 40),
                        pipeX, surfaceY, new Color(c.getRed(), c.getGreen(), c.getBlue(), 0));
                g2.setPaint(gasGrad);
                g2.fillRect(pipeX, surfaceY, PIPE_WIDTH, currentHeight);
            } else if (state == State.LIQUID) {
                double amp = 5.0;
                GeneralPath backWave = new GeneralPath();
                backWave.moveTo(pipeX, MAIN_PIPE_Y + pipeH);
                backWave.lineTo(pipeX + PIPE_WIDTH, MAIN_PIPE_Y + pipeH);
                backWave.lineTo(pipeX + PIPE_WIDTH,
                        surfaceY + Math.sin((PIPE_WIDTH) * 0.04 + time * 3.5 + Math.PI) * amp);
                for (int x = PIPE_WIDTH; x >= 0; x--) {
                    double wy = Math.sin(x * 0.04 + time * 3.5 + Math.PI) * amp;
                    backWave.lineTo(pipeX + x, surfaceY + wy);
                }
                backWave.closePath();
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 100));
                g2.fill(backWave);

                GeneralPath frontWave = new GeneralPath();
                frontWave.moveTo(pipeX, MAIN_PIPE_Y + pipeH);
                frontWave.lineTo(pipeX + PIPE_WIDTH, MAIN_PIPE_Y + pipeH);
                frontWave.lineTo(pipeX + PIPE_WIDTH, surfaceY + Math.sin((PIPE_WIDTH) * 0.05 + time * 2.0) * amp);
                for (int x = PIPE_WIDTH; x >= 0; x--) {
                    double wy = Math.sin(x * 0.05 + time * 2.0) * amp;
                    frontWave.lineTo(pipeX + x, surfaceY + wy);
                }
                frontWave.closePath();
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 180));
                g2.fill(frontWave);

            } else if (state == State.SOLID) {
                GeneralPath pileShape = new GeneralPath();
                int bottomY = MAIN_PIPE_Y + pipeH;
                pileShape.moveTo(pipeX, bottomY);

                for (int x = 0; x <= PIPE_WIDTH; x++) {
                    double normX = (x / (double) PIPE_WIDTH) * Math.PI;
                    double bigHill = Math.sin(normX) * 25.0;
                    double randomShape = Math.sin(x * 0.08 + pileSeed) * 6.0
                            + Math.cos(x * 0.03 + pileSeed * 1.3) * 4.0;
                    double slowAnim = Math.sin(x * 0.1 + time * 0.2) * 2.0;

                    double pileY = bottomY - (currentHeight + bigHill + randomShape + slowAnim);
                    if (pileY > bottomY)
                        pileY = bottomY;

                    pileShape.lineTo(pipeX + x, pileY);
                }
                pileShape.lineTo(pipeX + PIPE_WIDTH, bottomY);
                pileShape.closePath();

                g2.setColor(c);
                g2.fill(pileShape);

                Shape oldClip = g2.getClip();
                g2.setClip(pileShape);
                g2.clip(tubeShape);

                Random textureRand = new Random(12345);
                g2.setColor(new Color(0, 0, 0, 20));
                int grainsCount = (int) (currentHeight * PIPE_WIDTH * 0.5);
                if (grainsCount > 2000)
                    grainsCount = 2000;
                for (int i = 0; i < grainsCount; i++) {
                    int gx = pipeX + textureRand.nextInt(PIPE_WIDTH);
                    int gy = MAIN_PIPE_Y + pipeH - textureRand.nextInt(Math.max(1, (int) (currentHeight + 40)));
                    g2.fillRect(gx, gy, 2, 2);
                }

                Random sparkleRand = new Random((long) (time * 0.2));
                g2.setColor(new Color(255, 255, 255, 80));
                for (int i = 0; i < 100; i++) {
                    int gx = pipeX + sparkleRand.nextInt(PIPE_WIDTH);
                    int gy = MAIN_PIPE_Y + pipeH - sparkleRand.nextInt(Math.max(1, (int) (currentHeight + 40)));
                    g2.fillRect(gx, gy, 2, 2);
                }
                g2.setClip(oldClip);
            }

            for (Particle p : particles) {
                if (state == State.SOLID) {
                    g2.setColor(c);
                    g2.fillRect((int) p.x, (int) p.y, 3, 3);
                } else {
                    g2.setColor(c);
                    g2.fillOval((int) p.x, (int) p.y, (int) p.size, (int) p.size);
                }
            }
            g2.setClip(clip);
        } else if (recipe.getLevel() > 0) {
            if ((int) (time * 3) % 2 == 0) {
                g2.setColor(Color.RED);
                g2.setFont(new Font("Arial", Font.BOLD, 14));
                String msg = "BRAK SUROWCÓW";
                g2.drawString(msg, w / 2 - g2.getFontMetrics().stringWidth(msg) / 2, MAIN_PIPE_Y + pipeH / 2);
            }
        }

        g2.setColor(GLASS_HIGHLIGHT);
        g2.fillRoundRect(pipeX + 15, MAIN_PIPE_Y + 15, 20, pipeH - 30, 10, 10);
        g2.setColor(new Color(50, 55, 60));
        g2.setStroke(new BasicStroke(3f));
        g2.draw(tubeShape);

        // Rysuj dolną część UI (poziom i przycisk)
        drawBottomUI(g2, w, h);
    }

    private void drawBottomUI(Graphics2D g2, int w, @SuppressWarnings("unused") int h) {
        // Pozycja tekstu poziomu - 30px nad przyciskiem
        int levelY = upgradeBtnBounds.y - 15;

        // Tekst poziomu
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        String levelText = recipe.getLevel() == 0 ? "Zatrzymany" : "Poziom " + recipe.getLevel();
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(levelText, w / 2 - fm.stringWidth(levelText) / 2, levelY);

        // Przycisk upgrade
        double cost = recipe.getUpgradeCost();
        boolean canAfford = gameState.getMoney() >= cost;
        String btnText = recipe.getLevel() == 0 ? "Buduj ($" + Formatter.formatMoney(cost) + ")"
                : "Ulepsz ($" + Formatter.formatMoney(cost) + ")";

        // Kolor tła przycisku
        Color btnBg;
        Color btnFg;
        if (canAfford) {
            if (recipe.getLevel() == 0) {
                btnBg = BTN_GREEN;
            } else {
                btnBg = BTN_ORANGE;
            }
            btnFg = Color.BLACK;
        } else {
            btnBg = BTN_DISABLED_BG;
            btnFg = BTN_DISABLED_FG;
        }

        // Efekt hover
        if (isHoveringBtn && canAfford) {
            btnBg = btnBg.brighter();
        }

        // Rysuj przycisk
        RoundRectangle2D.Float btnShape = new RoundRectangle2D.Float(
                upgradeBtnBounds.x, upgradeBtnBounds.y,
                upgradeBtnBounds.width, upgradeBtnBounds.height, 10, 10);

        g2.setColor(btnBg);
        g2.fill(btnShape);

        // Obramowanie
        g2.setColor(new Color(0, 0, 0, 100));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(btnShape);

        // Tekst przycisku
        g2.setColor(btnFg);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        fm = g2.getFontMetrics();
        int textX = upgradeBtnBounds.x + (upgradeBtnBounds.width - fm.stringWidth(btnText)) / 2;
        int textY = upgradeBtnBounds.y + ((upgradeBtnBounds.height - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(btnText, textX, textY);
    }

    @SuppressWarnings("unused")
    private void drawLongPipes(Graphics2D g2, int w, int targetX, int targetY, int targetW, int targetH) {
        Random r = new Random(recipe.getName().hashCode());
        int centerPipeX = w / 2;
        int inputsCount = recipe.getInputs().size();
        int i = 0;

        for (Map.Entry<String, Double> entry : recipe.getInputs().entrySet()) {
            boolean isLeft = (i % 2 == 0);
            if (inputsCount == 1)
                isLeft = r.nextBoolean();

            int startX = isLeft ? 35 : w - 35;
            int startY = MAIN_PIPE_Y + 40 + r.nextInt(targetH / 2);
            int endX = isLeft ? centerPipeX - targetW / 2 + 8 : centerPipeX + targetW / 2 - 8;

            int offsetY = 60 + r.nextInt(60);
            int endY = startY + (r.nextBoolean() ? offsetY : -offsetY);
            if (endY < MAIN_PIPE_Y + 30)
                endY = MAIN_PIPE_Y + 30;
            if (endY > MAIN_PIPE_Y + targetH - 30)
                endY = MAIN_PIPE_Y + targetH - 30;
            if (Math.abs(endY - startY) < PIPE_RADIUS * 2.5)
                endY = startY + (int) (PIPE_RADIUS * 3);

            int availableW = Math.abs(endX - startX);
            int minGap = (int) (PIPE_RADIUS * 2.2);
            int midX = (startX + endX) / 2;
            if (availableW > minGap * 2) {
                int safeMin = isLeft ? startX + minGap : endX + minGap;
                int safeMax = isLeft ? endX - minGap : startX - minGap;
                if (safeMin > safeMax) {
                    int temp = safeMin;
                    safeMin = safeMax;
                    safeMax = temp;
                }
                midX = safeMin + r.nextInt(Math.max(1, safeMax - safeMin));
            }

            GeneralPath pipePath = createRoundedPipePath(startX, startY, endX, endY, isLeft, PIPE_RADIUS, midX);
            Color c = getMoleculeColor(entry.getKey());
            draw3DPipe(g2, pipePath, c, 16f);

            int tankWallX = isLeft ? centerPipeX - targetW / 2 : centerPipeX + targetW / 2;
            drawTankConnection(g2, tankWallX, (int) endY, c, isLeft);
            drawInputIcon(g2, startX, startY, entry.getKey(), isLeft);

            i++;
        }
    }

    private GeneralPath createRoundedPipePath(float startX, float startY, float endX, float endY, boolean isLeft,
            float radius, float midX) {
        GeneralPath path = new GeneralPath();
        path.moveTo(startX, startY);
        float x1 = isLeft ? midX - radius : midX + radius;
        path.lineTo(x1, startY);
        float yDir = (endY > startY) ? 1 : -1;
        float y1 = startY + radius * yDir;
        path.quadTo(midX, startY, midX, y1);
        float y2 = endY - radius * yDir;
        path.lineTo(midX, y2);
        float xDir = (endX > midX) ? 1 : -1;
        float x2 = midX + radius * xDir;
        path.quadTo(midX, endY, x2, endY);
        path.lineTo(endX, endY);
        return path;
    }

    private void draw3DPipe(Graphics2D g2, Shape path, Color c, float width) {
        g2.setColor(new Color(15, 15, 15));
        g2.setStroke(new BasicStroke(width + 2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g2.draw(path);
        g2.setColor(c.darker().darker());
        g2.setStroke(new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g2.draw(path);
        g2.setColor(c);
        g2.setStroke(new BasicStroke(width - 4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(path);
        g2.setColor(new Color(255, 255, 255, 80));
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(path);
    }

    private void drawTankConnection(Graphics2D g2, int wallX, int y, Color c, boolean isLeft) {
        int heightAtGlass = 28;
        int heightAtPipe = 18;
        int length = 12;
        int pipeX = isLeft ? wallX - length : wallX + length;
        Polygon p = new Polygon();
        if (isLeft) {
            p.addPoint(wallX, y - heightAtGlass / 2);
            p.addPoint(pipeX, y - heightAtPipe / 2);
            p.addPoint(pipeX, y + heightAtPipe / 2);
            p.addPoint(wallX, y + heightAtGlass / 2);
        } else {
            p.addPoint(pipeX, y - heightAtPipe / 2);
            p.addPoint(wallX, y - heightAtGlass / 2);
            p.addPoint(wallX, y + heightAtGlass / 2);
            p.addPoint(pipeX, y + heightAtPipe / 2);
        }
        GradientPaint gp = new GradientPaint(pipeX, y, new Color(60, 65, 70), wallX, y, new Color(30, 30, 35));
        g2.setPaint(gp);
        g2.fill(p);
        g2.setColor(new Color(20, 20, 20, 100));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(p);
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 100));
        int ringW = 2;
        int ringX = isLeft ? wallX - ringW : wallX;
        g2.fillRect(ringX, y - heightAtGlass / 2, ringW, heightAtGlass);
    }

    private void drawInputIcon(Graphics2D g2, int x, int y, String sym, boolean isLeft) {
        Color c = getMoleculeColor(sym);
        int heightAtWall = 40;
        int heightAtPipe = 16;
        int length = 25;
        int wallX = isLeft ? x - length : x + length;
        Polygon p = new Polygon();
        if (isLeft) {
            p.addPoint(wallX, y - heightAtWall / 2);
            p.addPoint(x, y - heightAtPipe / 2);
            p.addPoint(x, y + heightAtPipe / 2);
            p.addPoint(wallX, y + heightAtWall / 2);
        } else {
            p.addPoint(x, y - heightAtPipe / 2);
            p.addPoint(wallX, y - heightAtWall / 2);
            p.addPoint(wallX, y + heightAtWall / 2);
            p.addPoint(x, y + heightAtPipe / 2);
        }
        GradientPaint gp = new GradientPaint(wallX, y, new Color(40, 40, 45), x, y, c);
        g2.setPaint(gp);
        g2.fill(p);
        g2.setColor(new Color(20, 20, 20));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(p);
        g2.setColor(new Color(60, 60, 65));
        int plateW = 6;
        int plateX = isLeft ? wallX : wallX - plateW;
        g2.fillRect(plateX, y - heightAtWall / 2 - 4, plateW, heightAtWall + 8);
        g2.setColor(new Color(30, 30, 30));
        g2.fillOval(plateX + 1, y - heightAtWall / 2, 4, 4);
        g2.fillOval(plateX + 1, y + heightAtWall / 2 - 4, 4, 4);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        int textX = (x + wallX) / 2;
        g2.setColor(new Color(0, 0, 0, 180));
        g2.drawString(sym, textX - fm.stringWidth(sym) / 2 + 1, y + 4 + 1);
        g2.setColor(Color.WHITE);
        g2.drawString(sym, textX - fm.stringWidth(sym) / 2, y + 4);
    }

    private void drawSubtleOutput(Graphics2D g2, int centerX, int pipeBottomY, String sym) {
        int boxY = pipeBottomY + 10; // Przesunięte bliżej rury
        int funnelTopW = PIPE_WIDTH - 4;
        int funnelBottomW = 80;
        Polygon funnel = new Polygon();
        funnel.addPoint(centerX - funnelTopW / 2, pipeBottomY);
        funnel.addPoint(centerX + funnelTopW / 2, pipeBottomY);
        funnel.addPoint(centerX + funnelBottomW / 2, boxY + 5);
        funnel.addPoint(centerX - funnelBottomW / 2, boxY + 5);
        GradientPaint gp = new GradientPaint(centerX, pipeBottomY, new Color(50, 50, 55), centerX, boxY,
                new Color(30, 30, 35));
        g2.setPaint(gp);
        g2.fill(funnel);
        g2.setColor(new Color(20, 20, 20));
        g2.draw(funnel);
        Color c = getMoleculeColor(sym);
        g2.setColor(new Color(30, 30, 30));
        g2.fillRoundRect(centerX - 60, boxY, 120, 50, 10, 10);
        g2.setColor(c);
        g2.fillRoundRect(centerX - 55, boxY + 40, 110, 5, 2, 2);
        g2.setColor(new Color(100, 100, 100));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(centerX - 60, boxY, 120, 50, 10, 10);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(sym, centerX - fm.stringWidth(sym) / 2, boxY + 32);
    }

    private void drawBolts(Graphics2D g2, int w, int h) {
        int offset = 25;
        drawScrew(g2, offset, offset); // Lewy Góra
        drawScrew(g2, w - offset, offset); // Prawy Góra
        drawScrew(g2, offset, h - offset); // Lewy Dół
        drawScrew(g2, w - offset, h - offset); // Prawy Dół
    }

    private void drawScrew(Graphics2D g2, int x, int y) {
        int r = 7; // Promień (Średnica 14)

        // 1. Cień pod śrubą (Ambient Occlusion)
        g2.setColor(new Color(20, 20, 20, 150));
        g2.fillOval(x - r - 1, y - r - 1, (r * 2) + 2, (r * 2) + 2);

        // 2. Główka śruby (Metaliczny Gradient)
        GradientPaint headGrad = new GradientPaint(
                x - r, y - r, new Color(180, 185, 190), // Jasny metal
                x + r, y + r, new Color(40, 45, 50) // Ciemny metal
        );
        g2.setPaint(headGrad);
        g2.fillOval(x - r, y - r, r * 2, r * 2);

        // 3. Obrys (Kontur)
        g2.setColor(new Color(30, 30, 35, 100));
        g2.setStroke(new BasicStroke(1f));
        g2.drawOval(x - r, y - r, r * 2, r * 2);

        // 4. Wcięcie Phillips (Krzyżak)
        Graphics2D gScratch = (Graphics2D) g2.create();
        gScratch.rotate(Math.PI / 4, x, y); // Obrót o 45 stopni

        gScratch.setColor(new Color(25, 25, 30)); // Ciemne wnętrze wcięcia
        int slotLen = 10;
        int slotW = 3;

        // Rysowanie krzyża
        gScratch.fillRoundRect(x - slotLen / 2, y - slotW / 2, slotLen, slotW, 1, 1);
        gScratch.fillRoundRect(x - slotW / 2, y - slotLen / 2, slotW, slotLen, 1, 1);

        // Subtelny highlight krawędzi wcięcia
        gScratch.setColor(new Color(255, 255, 255, 40));
        gScratch.drawLine(x - slotLen / 2, y + slotW / 2, x + slotLen / 2, y + slotW / 2);
        gScratch.drawLine(x + slotW / 2, y - slotLen / 2, x + slotW / 2, y + slotLen / 2);

        gScratch.dispose();
    }

    private Color getMoleculeColor(String symbol) {
        if (symbol.equals("H2O"))
            return new Color(60, 160, 255);
        if (symbol.equals("H2SO4"))
            return new Color(220, 220, 50);
        if (symbol.equals("NaCl"))
            return new Color(240, 240, 240);
        if (symbol.equals("CO2"))
            return new Color(150, 150, 150);
        if (symbol.equals("NH3"))
            return new Color(100, 255, 200);
        if (symbol.equals("CH4"))
            return new Color(200, 100, 255);
        if (symbol.equals("H"))
            return new Color(200, 200, 200);
        if (symbol.equals("O"))
            return new Color(255, 80, 80);
        if (symbol.equals("C"))
            return new Color(50, 50, 50);
        if (symbol.equals("S"))
            return new Color(255, 255, 0);
        if (symbol.equals("N"))
            return new Color(100, 100, 255);
        if (symbol.equals("Na"))
            return new Color(200, 200, 255);
        if (symbol.equals("Cl"))
            return new Color(100, 255, 100);
        return new Color((symbol.hashCode() & 0xFFFFFF)).brighter();
    }

    private static class Particle {
        double x, y, vy, size;

        Particle(double x, double y, double vy, double size) {
            this.x = x;
            this.y = y;
            this.vy = vy;
            this.size = size;
        }
    }
}
