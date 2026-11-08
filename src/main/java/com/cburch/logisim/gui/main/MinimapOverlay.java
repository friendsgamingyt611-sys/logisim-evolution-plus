/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.main;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Bounds;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * Interactive Minimap HUD overlay for the infinite canvas.
 * Floats in the bottom-right corner of the canvas viewport.
 * Displays a bird's-eye radar view of components and the visible viewport frustum,
 * allowing instant click-to-jump and drag navigation across infinite 4-quadrant space.
 */
public class MinimapOverlay extends JPanel {
  private static final long serialVersionUID = 1L;

  private static final int DEFAULT_WIDTH = 180;
  private static final int DEFAULT_HEIGHT = 120;
  private static final int PADDING = 10;

  private final Canvas canvas;
  private final Camera2D camera;

  // Cached world bounding box of circuit for minimap scale calculation
  private Bounds cachedCircuitBounds = Bounds.create(0, 0, 100, 100);
  private double minimapScale = 1.0;
  private double worldOriginX = 0;
  private double worldOriginY = 0;

  private boolean isDraggingFrustum = false;

  public MinimapOverlay(Canvas canvas, Camera2D camera) {
    this.canvas = canvas;
    this.camera = camera;

    setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
    setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    setOpaque(false);
    putClientProperty(FlatClientProperties.STYLE, "arc: 8;");

    final var mouse = new MinimapMouseListener();
    addMouseListener(mouse);
    addMouseMotionListener(mouse);

    if (camera != null) {
      camera.addPropertyChangeListener(evt -> {
        System.out.println("[DEBUG MINIMAP] Received PropertyChange: " + evt.getPropertyName() + " | Forcing repaint.");
        repaint();
      });
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    final var g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    final int w = getWidth();
    final int h = getHeight();

    // 1. Semi-transparent background panel
    Color bg = UIManager.getColor("Panel.background");
    if (bg == null) bg = new Color(30, 30, 30);
    g2.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 200));
    g2.fillRoundRect(0, 0, w, h, 8, 8);

    // Border
    Color border = UIManager.getColor("Component.borderColor");
    if (border == null) border = new Color(80, 80, 80);
    g2.setColor(border);
    g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);

    final var proj = canvas.getProject();
    if (proj == null) {
      g2.dispose();
      return;
    }
    final Circuit circuit = proj.getCurrentCircuit();
    if (circuit == null) {
      g2.dispose();
      return;
    }

    // 2. Compute bounding area that includes both circuit and current viewport
    var circBds = circuit.getBounds();
    if (circBds == null || circBds.getWidth() == 0 || circBds.getHeight() == 0) {
      circBds = Bounds.create(-100, -100, 200, 200);
    }

    final double viewWorldLeft = camera.screenToWorldX(0);
    final double viewWorldTop = camera.screenToWorldY(0);
    final double viewWorldRight = camera.screenToWorldX(camera.getViewportWidth());
    final double viewWorldBottom = camera.screenToWorldY(camera.getViewportHeight());

    double minX = Math.min(circBds.getX(), viewWorldLeft);
    double minY = Math.min(circBds.getY(), viewWorldTop);
    double maxX = Math.max(circBds.getX() + circBds.getWidth(), viewWorldRight);
    double maxY = Math.max(circBds.getY() + circBds.getHeight(), viewWorldBottom);

    // Add 10% breathing room
    double spanX = Math.max(100.0, (maxX - minX) * 1.1);
    double spanY = Math.max(100.0, (maxY - minY) * 1.1);

    double availW = w - 2 * PADDING;
    double availH = h - 2 * PADDING;

    minimapScale = Math.min(availW / spanX, availH / spanY);
    worldOriginX = minX - (spanX - (maxX - minX)) / 2.0;
    worldOriginY = minY - (spanY - (maxY - minY)) / 2.0;

    // 3. Draw circuit component footprint outlines
    Color compColor = UIManager.getColor("Component.accentColor");
    if (compColor == null) compColor = new Color(0, 120, 215);
    g2.setColor(new Color(compColor.getRed(), compColor.getGreen(), compColor.getBlue(), 140));

    for (Component c : circuit.getNonWires()) {
      Bounds b = c.getBounds();
      int mx = PADDING + (int) Math.round((b.getX() - worldOriginX) * minimapScale);
      int my = PADDING + (int) Math.round((b.getY() - worldOriginY) * minimapScale);
      int mw = Math.max(2, (int) Math.round(b.getWidth() * minimapScale));
      int mh = Math.max(2, (int) Math.round(b.getHeight() * minimapScale));
      g2.fillRect(mx, my, mw, mh);
    }

    // 4. Draw Viewport Frustum Box (the current view area)
    int fLeft = PADDING + (int) Math.round((viewWorldLeft - worldOriginX) * minimapScale);
    int fTop = PADDING + (int) Math.round((viewWorldTop - worldOriginY) * minimapScale);
    int fRight = PADDING + (int) Math.round((viewWorldRight - worldOriginX) * minimapScale);
    int fBottom = PADDING + (int) Math.round((viewWorldBottom - worldOriginY) * minimapScale);

    int fWidth = Math.max(4, fRight - fLeft);
    int fHeight = Math.max(4, fBottom - fTop);

    // Semi-transparent frustum fill
    g2.setColor(new Color(compColor.getRed(), compColor.getGreen(), compColor.getBlue(), 40));
    g2.fillRect(fLeft, fTop, fWidth, fHeight);

    // Glowing frustum outline
    g2.setColor(compColor);
    g2.setStroke(new BasicStroke(1.5f));
    g2.drawRect(fLeft, fTop, fWidth, fHeight);

    g2.dispose();
  }

  /**
   * Translates a click inside the minimap to a target world coordinate,
   * centering the camera on that location instantly.
   */
  private void navigateToMinimapPoint(int mouseX, int mouseY) {
    if (minimapScale <= 0) return;
    double targetWorldX = worldOriginX + (mouseX - PADDING) / minimapScale;
    double targetWorldY = worldOriginY + (mouseY - PADDING) / minimapScale;
    camera.centerOn(targetWorldX, targetWorldY);
    canvas.repaint();
  }

  private class MinimapMouseListener extends MouseAdapter {
    @Override
    public void mousePressed(MouseEvent e) {
      isDraggingFrustum = true;
      navigateToMinimapPoint(e.getX(), e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      isDraggingFrustum = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
      if (isDraggingFrustum) {
        navigateToMinimapPoint(e.getX(), e.getY());
      }
    }
  }
}
