package com.labinc.gui;

import java.awt.BorderLayout;

import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import com.labinc.model.GameState;
import com.labinc.model.Recipe;
import com.labinc.util.GameColors;

public class FactoryPanel extends JPanel {
    private final GameState gameState;
    private final List<ChemicalColumn> columns = new ArrayList<>();
    private final Timer uiTimer;

    private JPanel contentPanel;
    private JScrollPane scrollPane;

    public FactoryPanel(GameState gameState) {
        this.gameState = gameState;

        setLayout(new BorderLayout());
        setBackground(GameColors.BG_DARK);

        createHorizontalView();

        uiTimer = new Timer(100, e -> updateDisplay());
        uiTimer.start();
    }

    private void createHorizontalView() {
        // Panel główny z kolumnami w poziomie
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
        contentPanel.setBackground(GameColors.BG_DARK);

        // Dodaj kolumny
        int index = 0;
        for (Recipe recipe : gameState.getRecipes().values()) {
            if (recipe.isUnlocked()) {
                ChemicalColumn col = new ChemicalColumn(recipe, gameState, index);
                columns.add(col);
                contentPanel.add(col);
                index++;
            }
        }

        // Wrapper który centruje kolumny w pionie
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(GameColors.BG_DARK);
        centerWrapper.add(contentPanel); // GridBagLayout automatycznie centruje

        // Scroll Pane z gradientami
        scrollPane = new GradientScrollPane(centerWrapper);
        scrollPane.setBackground(GameColors.BG_DARK);
        scrollPane.getViewport().setBackground(GameColors.BG_DARK);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(30);

        // Dodaj listener do skalowania
        scrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateColumnScaling();
            }
        });

        add(scrollPane, BorderLayout.CENTER);
    }

    private void updateColumnScaling() {
        if (scrollPane == null)
            return;

        int h = scrollPane.getViewport().getHeight();
        if (h <= 0)
            return;

        // Bazowa wysokość kolumny to 700px
        float scale = Math.min(1.0f, (float) h / 700.0f);
        // Ogranicz minimalną skalę żeby nie zniknęło całkiem (np. 0.3)
        scale = Math.max(0.3f, scale);

        for (ChemicalColumn col : columns) {
            col.updateScale(scale);
        }
    }

    private void updateDisplay() {
        for (ChemicalColumn col : columns) {
            col.updateStatus();
        }
    }
}