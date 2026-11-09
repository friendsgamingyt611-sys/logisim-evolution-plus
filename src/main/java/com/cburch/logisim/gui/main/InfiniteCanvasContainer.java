/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.main;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JLayeredPane;

/**
 * Container hosting the Infinite Canvas (Layer 0) and floating HUD overlays
 * like the interactive Minimap (Layer 1).
 * Automatically sizes Canvas to 100% of viewport and dynamically anchors
 * the MinimapOverlay to the bottom-right corner without clipping or overflow.
 */
public class InfiniteCanvasContainer extends JLayeredPane {
  private static final long serialVersionUID = 1L;

  private static final int MINIMAP_MARGIN = 16;

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
      canvas.setActivityCallback(() -> minimap.wakeUp());
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

  @Override
  public void doLayout() {
    super.doLayout();
    layoutComponents();
  }

  public void layoutComponents() {
    final int w = getWidth();
    final int h = getHeight();
    if (w <= 0 || h <= 0) return;

    // 1. Canvas fills 100% of the viewport container
    canvas.setBounds(0, 0, w, h);
    if (canvas.getCamera() != null) {
      canvas.getCamera().setViewportSize(w, h);
    }

    // 2. Snap Minimap to bottom-right corner with margin, strictly bounded within viewport
    if (minimap != null) {
      final Dimension pref = minimap.getPreferredSize();
      final int maxAllowedW = Math.max(60, w - 2 * MINIMAP_MARGIN);
      final int maxAllowedH = Math.max(40, h - 2 * MINIMAP_MARGIN);

      final int mw = Math.min(pref.width, maxAllowedW);
      final int mh = Math.min(pref.height, maxAllowedH);

      final int mx = Math.max(MINIMAP_MARGIN, w - mw - MINIMAP_MARGIN);
      final int my = Math.max(MINIMAP_MARGIN, h - mh - MINIMAP_MARGIN);

      minimap.setBounds(mx, my, mw, mh);
    }

    repaint();
  }
}
