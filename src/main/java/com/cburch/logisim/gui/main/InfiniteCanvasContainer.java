/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.main;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JLayeredPane;

/**
 * Container hosting the Infinite Canvas (Layer 0) and floating HUD overlays
 * like the interactive Minimap (Layer 1).
 * Uses a ComponentListener to automatically size the Canvas to 100% of the viewport
 * and anchor the MinimapOverlay dynamically to the bottom-right corner.
 */
public class InfiniteCanvasContainer extends JLayeredPane {
  private static final long serialVersionUID = 1L;

  private static final int MINIMAP_MARGIN = 16;
  private static final int MINIMAP_WIDTH = 190;
  private static final int MINIMAP_HEIGHT = 130;

  private final Canvas canvas;
  private final MinimapOverlay minimap;

  public InfiniteCanvasContainer(Canvas canvas, MinimapOverlay minimap) {
    this.canvas = canvas;
    this.minimap = minimap;

    setOpaque(true);
    setBackground(canvas.getBackground());

    // Layer 0: Main Canvas
    add(canvas, JLayeredPane.DEFAULT_LAYER);

    // Layer 1: Floating Minimap HUD
    if (minimap != null) {
      add(minimap, JLayeredPane.PALETTE_LAYER);
    }

    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        layoutComponents();
      }
    });
  }

  public Canvas getCanvas() {
    return canvas;
  }

  public MinimapOverlay getMinimap() {
    return minimap;
  }

  private void layoutComponents() {
    final int w = getWidth();
    final int h = getHeight();
    if (w <= 0 || h <= 0) return;

    // 1. Canvas fills 100% of the viewport container
    canvas.setBounds(0, 0, w, h);

    // 2. Snap Minimap to bottom-right corner with margin
    if (minimap != null) {
      final int mw = Math.min(MINIMAP_WIDTH, Math.max(80, w / 4));
      final int mh = Math.min(MINIMAP_HEIGHT, Math.max(60, h / 4));
      final int mx = w - mw - MINIMAP_MARGIN;
      final int my = h - mh - MINIMAP_MARGIN;
      minimap.setBounds(mx, my, mw, mh);
    }

    revalidate();
    repaint();
  }
}
