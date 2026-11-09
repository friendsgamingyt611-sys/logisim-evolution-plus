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
import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.EndData;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * Interactive Minimap HUD overlay for the infinite canvas (Stage 3.1).
 * Floats in the bottom-right corner of the canvas viewport.
 * Features:
 *  - State-aware [-] and [+] Minimap Size controls (dimmed/disabled at boundaries).
 *  - Default size is the smallest size (200x135), with large options up to 450x305.
 *  - Natural mouse wheel scroll anywhere over minimap to smoothly zoom the canvas.
 *  - Click / drag anywhere inside the minimap to pan the camera with micro precision.
 *  - Connected-components wire display rule: wires are disabled locally ONLY IF the
 *    viewport rectangle is bigger than the bounding box of distinct components connected by wires.
 *  - Adaptive local focus at high zoom levels for micro corrections, strictly clipped to map area.
 *  - Fully bounded layout anchoring preventing any off-screen overflow.
 */
public class MinimapOverlay extends JPanel {
  private static final long serialVersionUID = 1L;

  // Dimensions bounds for Minimap HUD: Smallest (Default) to Ultra Large
  public static final int MIN_WIDTH = 200;
  public static final int MIN_HEIGHT = 135;
  public static final int MAX_WIDTH = 450;
  public static final int MAX_HEIGHT = 305;
  private static final int[] PRESET_WIDTHS = {200, 280, 360, 450};
  private static final int[] PRESET_HEIGHTS = {135, 190, 245, 305};
  private static final int DEFAULT_SIZE_INDEX = 0; // 200 x 135 is default smallest size

  private static final int TOP_BAR_HEIGHT = 22;
  private static final int PADDING = 10;
  private static final int THUMB_RES = 360;
  private static final int RESIZE_CORNER_SIZE = 22;
  private static final int SLEEP_DELAY_MS = 2500;
  private static final BasicStroke WIRE_STROKE = new BasicStroke(1.0f);

  private final Canvas canvas;
  private final Camera2D camera;

  // Inactivity Sleep & Wake State
  private final javax.swing.Timer sleepTimer;
  private final javax.swing.Timer fadeTimer;
  private float currentAlpha = 1.0f;
  private boolean isAsleep = false;

  // Top-left corner extender resize state
  private boolean isHoveringResizeCorner = false;
  private boolean isResizing = false;
  private int resizeStartScreenX = 0;
  private int resizeStartScreenY = 0;
  private int resizeStartWidth = MIN_WIDTH;
  private int resizeStartHeight = MIN_HEIGHT;

  // Discrete size index tracked for preset steps
  private int currentSizeIndex = DEFAULT_SIZE_INDEX;

  // Cached thumbnail of the circuit components for macro views
  private java.awt.image.BufferedImage cachedCircuitImage = null;
  private long cachedCircuitVersion = -1;
  private Bounds cachedCircuitBounds = null;

  // Cached bounding box of distinct components connected to each other with >= 1 wire
  private Bounds cachedConnectedBounds = null;
  private long cachedConnectedVersion = -1;

  // Active world-to-minimap projection
  private double currentScale = 1.0;
  private double currentMinX = -500;
  private double currentMinY = -500;
  private double currentOffsetX = PADDING;
  private double currentOffsetY = PADDING + TOP_BAR_HEIGHT;
  private boolean isLocalFocus = false;

  // Frustum screen geometry in minimap space (from last paint)
  private int lastVx = 0;
  private int lastVy = 0;
  private int lastVw = 0;
  private int lastVh = 0;

  // Pan drag state
  private boolean isDraggingFrustum = false;
  private double dragScale = 1.0;
  private double dragMinX = 0;
  private double dragMinY = 0;
  private double dragOffsetX = 0;
  private double dragOffsetY = 0;

  public MinimapOverlay(Canvas canvas, Camera2D camera) {
    this.canvas = canvas;
    this.camera = camera;

    final int w = PRESET_WIDTHS[DEFAULT_SIZE_INDEX];
    final int h = PRESET_HEIGHTS[DEFAULT_SIZE_INDEX];
    setPreferredSize(new Dimension(w, h));
    setSize(w, h);
    setOpaque(false);
    putClientProperty(FlatClientProperties.STYLE, "arc: 8;");

    this.sleepTimer = new javax.swing.Timer(SLEEP_DELAY_MS, e -> goToSleep());
    this.sleepTimer.setRepeats(false);

    this.fadeTimer = new javax.swing.Timer(25, e -> {
      currentAlpha = Math.max(0.0f, currentAlpha - 0.08f);
      if (currentAlpha <= 0.0f) {
        ((javax.swing.Timer) e.getSource()).stop();
        isAsleep = true;
      }
      repaint();
    });
    this.fadeTimer.setRepeats(true);

    final var mouse = new MinimapMouseListener();
    addMouseListener(mouse);
    addMouseMotionListener(mouse);
    addMouseWheelListener(mouse);

    if (camera != null) {
      camera.addPropertyChangeListener(evt -> {
        wakeUp();
        repaint();
      });
    }

    if (!java.awt.GraphicsEnvironment.isHeadless()) {
      sleepTimer.start();
    }
  }

  // ---------------------------------------------------------------------------
  // Inactivity Sleep & Wake Lifecycle
  // ---------------------------------------------------------------------------

  public boolean isAsleep() {
    return isAsleep;
  }

  public float getCurrentAlpha() {
    return currentAlpha;
  }

  public void goToSleep() {
    if (isDraggingFrustum || isResizing || (canvas != null && canvas.isPanning())) {
      sleepTimer.restart();
      return;
    }
    fadeTimer.start();
  }

  public void wakeUp() {
    if (fadeTimer.isRunning()) {
      fadeTimer.stop();
    }
    isAsleep = false;
    currentAlpha = 1.0f;
    repaint();
    if (!java.awt.GraphicsEnvironment.isHeadless()) {
      sleepTimer.restart();
    }
  }

  // ---------------------------------------------------------------------------
  // Minimap Freeform Drag Resizing & Discrete Step Fallbacks
  // ---------------------------------------------------------------------------

  public boolean canShrink() {
    return getPreferredSize().width > MIN_WIDTH;
  }

  public boolean canEnlarge() {
    return getPreferredSize().width < MAX_WIDTH;
  }

  public void shrinkMinimap() {
    if (canShrink()) {
      if (currentSizeIndex > 0) currentSizeIndex--;
      applyDimensions(PRESET_WIDTHS[currentSizeIndex], PRESET_HEIGHTS[currentSizeIndex]);
    }
  }

  public void enlargeMinimap() {
    if (canEnlarge()) {
      if (currentSizeIndex < PRESET_WIDTHS.length - 1) currentSizeIndex++;
      applyDimensions(PRESET_WIDTHS[currentSizeIndex], PRESET_HEIGHTS[currentSizeIndex]);
    }
  }

  public int getCurrentSizeIndex() {
    return currentSizeIndex;
  }

  public void applyDimensions(int targetW, int targetH) {
    final int clampedW = Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, targetW));
    final int clampedH = Math.max(MIN_HEIGHT, Math.min(MAX_HEIGHT, targetH));
    setPreferredSize(new Dimension(clampedW, clampedH));
    setSize(clampedW, clampedH);

    // Update currentSizeIndex to nearest preset
    int bestIdx = 0;
    int minDiff = Integer.MAX_VALUE;
    for (int i = 0; i < PRESET_WIDTHS.length; i++) {
      int diff = Math.abs(PRESET_WIDTHS[i] - clampedW);
      if (diff < minDiff) {
        minDiff = diff;
        bestIdx = i;
      }
    }
    currentSizeIndex = bestIdx;

    final var parent = getParent();
    if (parent instanceof InfiniteCanvasContainer icc) {
      icc.layoutComponents();
    }
    repaint();
  }

  private void applyMinimapSize() {
    applyDimensions(PRESET_WIDTHS[currentSizeIndex], PRESET_HEIGHTS[currentSizeIndex]);
  }

  // ---------------------------------------------------------------------------
  // Connected Components Bounding Box Calculation
  // ---------------------------------------------------------------------------

  /**
   * Computes the bounding box covering all distinct components connected to each other
   * with at least 1 wire (and the wires between them).
   */
  public Bounds getConnectedComponentsBounds(Circuit circuit) {
    if (circuit.getStructureVersion() == cachedConnectedVersion && cachedConnectedBounds != null) {
      return cachedConnectedBounds;
    }

    final var wires = circuit.getWires();
    if (wires.isEmpty()) {
      cachedConnectedBounds = circuit.getBounds();
      cachedConnectedVersion = circuit.getStructureVersion();
      return cachedConnectedBounds;
    }

    final var wirePoints = new HashSet<Location>();
    int minWireX = Integer.MAX_VALUE, maxWireX = Integer.MIN_VALUE;
    int minWireY = Integer.MAX_VALUE, maxWireY = Integer.MIN_VALUE;

    for (Wire w : wires) {
      final var p0 = w.getEnd0();
      final var p1 = w.getEnd1();
      wirePoints.add(p0);
      wirePoints.add(p1);
      if (p0.getX() < minWireX) minWireX = p0.getX();
      if (p1.getX() < minWireX) minWireX = p1.getX();
      if (p0.getX() > maxWireX) maxWireX = p0.getX();
      if (p1.getX() > maxWireX) maxWireX = p1.getX();
      if (p0.getY() < minWireY) minWireY = p0.getY();
      if (p1.getY() < minWireY) minWireY = p1.getY();
      if (p0.getY() > maxWireY) maxWireY = p0.getY();
      if (p1.getY() > maxWireY) maxWireY = p1.getY();
    }

    int minX = minWireX, maxX = maxWireX;
    int minY = minWireY, maxY = maxWireY;

    for (Component c : circuit.getNonWires()) {
      boolean connected = false;
      for (EndData end : c.getEnds()) {
        if (end != null && wirePoints.contains(end.getLocation())) {
          connected = true;
          break;
        }
      }
      if (connected) {
        Bounds b = c.getBounds();
        if (b.getX() < minX) minX = b.getX();
        if (b.getX() + b.getWidth() > maxX) maxX = b.getX() + b.getWidth();
        if (b.getY() < minY) minY = b.getY();
        if (b.getY() + b.getHeight() > maxY) maxY = b.getY() + b.getHeight();
      }
    }

    cachedConnectedBounds = Bounds.create(minX, minY, Math.max(1, maxX - minX), Math.max(1, maxY - minY));
    cachedConnectedVersion = circuit.getStructureVersion();
    return cachedConnectedBounds;
  }

  // ---------------------------------------------------------------------------
  // Circuit Thumbnail & Vector Rendering
  // ---------------------------------------------------------------------------

  private void updateCircuitThumbnail(Circuit circuit) {
    var circBds = circuit.getBounds();
    if (circBds == null || circBds.getWidth() <= 0 || circBds.getHeight() <= 0) {
      circBds = Bounds.create(-200, -200, 400, 400);
    }
    cachedCircuitBounds = circBds;

    final int tw = THUMB_RES;
    final int th = Math.max(10, (int) Math.round(THUMB_RES * ((double) circBds.getHeight() / Math.max(1, circBds.getWidth()))));

    final int imgW = Math.max(10, Math.min(600, tw));
    final int imgH = Math.max(10, Math.min(600, th));

    if (cachedCircuitImage == null || cachedCircuitImage.getWidth() != imgW || cachedCircuitImage.getHeight() != imgH) {
      cachedCircuitImage = new java.awt.image.BufferedImage(imgW, imgH, java.awt.image.BufferedImage.TYPE_INT_ARGB);
    }

    final var g = cachedCircuitImage.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setComposite(AlphaComposite.Clear);
    g.fillRect(0, 0, imgW, imgH);
    g.setComposite(AlphaComposite.SrcOver);

    Color compColor = UIManager.getColor("Component.accentColor");
    if (compColor == null) compColor = new Color(0, 120, 215);
    g.setColor(new Color(compColor.getRed(), compColor.getGreen(), compColor.getBlue(), 200));

    final double scaleX = (double) imgW / Math.max(1.0, circBds.getWidth());
    final double scaleY = (double) imgH / Math.max(1.0, circBds.getHeight());

    for (Component c : circuit.getNonWires()) {
      Bounds b = c.getBounds();
      int mx = (int) Math.round((b.getX() - circBds.getX()) * scaleX);
      int my = (int) Math.round((b.getY() - circBds.getY()) * scaleY);
      int mw = Math.max(2, (int) Math.round(b.getWidth() * scaleX));
      int mh = Math.max(2, (int) Math.round(b.getHeight() * scaleY));
      g.fillRect(mx, my, mw, mh);
    }
    g.dispose();
    cachedCircuitVersion = circuit.getStructureVersion();
  }

  @Override
  protected void paintComponent(Graphics g) {
    if (isAsleep || currentAlpha <= 0.0f) {
      return;
    }

    super.paintComponent(g);

    final var g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    if (currentAlpha < 1.0f) {
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, currentAlpha));
    }

    final int w = getWidth();
    final int h = getHeight();

    // 1. Semi-transparent background panel
    Color bg = UIManager.getColor("Panel.background");
    if (bg == null) bg = new Color(30, 30, 30);
    g2.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 220));
    g2.fillRoundRect(0, 0, w, h, 8, 8);

    // Border
    Color border = UIManager.getColor("Component.borderColor");
    if (border == null) border = new Color(80, 80, 80);
    g2.setColor(border);
    g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);

    // 2. Top HUD Bar (Title, Zoom %)
    Color headerBg = new Color(bg.getRed() > 100 ? bg.getRed() - 20 : bg.getRed() + 20,
                               bg.getGreen() > 100 ? bg.getGreen() - 20 : bg.getGreen() + 20,
                               bg.getBlue() > 100 ? bg.getBlue() - 20 : bg.getBlue() + 20, 190);
    g2.setColor(headerBg);
    g2.fillRoundRect(1, 1, w - 2, TOP_BAR_HEIGHT, 8, 8);
    g2.fillRect(1, TOP_BAR_HEIGHT - 4, w - 2, 4);

    // Top-left extender selection handle (ONLY displayed when hovered or actively dragging)
    if (isHoveringResizeCorner || isResizing) {
      Color accent = UIManager.getColor("Component.accentColor");
      if (accent == null) accent = new Color(0, 120, 215);

      g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), isResizing ? 130 : 60));
      int[] xs = {1, RESIZE_CORNER_SIZE + 2, 1};
      int[] ys = {1, 1, RESIZE_CORNER_SIZE + 2};
      g2.fillPolygon(xs, ys, 3);

      g2.setColor(Color.WHITE);
      g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g2.drawLine(5, 13, 13, 5);
      g2.drawLine(5, 8, 8, 5);
      g2.drawLine(5, 18, 18, 5);

      g2.setColor(accent);
      g2.drawLine(1, 1, RESIZE_CORNER_SIZE + 2, 1);
      g2.drawLine(1, 1, 1, RESIZE_CORNER_SIZE + 2);
    }

    // Live Zoom % label on left (cleanly positioned after the corner extender)
    Color textColor = UIManager.getColor("Label.foreground");
    if (textColor == null) textColor = Color.WHITE;
    g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
    final int zoomPct = (camera != null) ? (int) Math.round(camera.getZoomFactor() * 100) : 100;
    g2.setColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 210));
    g2.drawString(zoomPct + "%", 26, 15);

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

    // 3. Refresh circuit thumbnail if structure version changed
    if (cachedCircuitImage == null
        || circuit.getStructureVersion() != cachedCircuitVersion
        || cachedCircuitBounds == null) {
      updateCircuitThumbnail(circuit);
    }

    final Bounds circBds = cachedCircuitBounds;
    final Bounds vpBds = (camera != null) ? camera.getViewportWorldBounds() : circBds;

    final int mapAreaY = TOP_BAR_HEIGHT;
    final int mapAreaH = h - TOP_BAR_HEIGHT;

    // 4. Compute adaptive minimap world projection
    if (!isDraggingFrustum) {
      final double availW = Math.max(10.0, w - 2 * PADDING);
      final double availH = Math.max(10.0, mapAreaH - 2 * PADDING);

      final Bounds worldRegion = (circBds != null) ? circBds.add(vpBds) : vpBds;
      final double marginX = Math.max(80.0, worldRegion.getWidth() * 0.12);
      final double marginY = Math.max(80.0, worldRegion.getHeight() * 0.12);
      final double macroSpanX = Math.max(1.0, worldRegion.getWidth() + 2 * marginX);
      final double macroSpanY = Math.max(1.0, worldRegion.getHeight() + 2 * marginY);

      // Enforce a minimum frustum size on screen for micro corrections
      final double minFrustumW = Math.max(50.0, availW * 0.32);
      final double minFrustumH = Math.max(35.0, availH * 0.32);

      final double maxLocalSpanX = vpBds.getWidth() * (availW / minFrustumW);
      final double maxLocalSpanY = vpBds.getHeight() * (availH / minFrustumH);

      isLocalFocus = maxLocalSpanX < macroSpanX || maxLocalSpanY < macroSpanY;

      final double spanX = Math.min(macroSpanX, maxLocalSpanX);
      final double spanY = Math.min(macroSpanY, maxLocalSpanY);

      if (isLocalFocus) {
        // Center the minimap window around the camera viewport for micro corrections
        final double vpCenterX = vpBds.getX() + vpBds.getWidth() / 2.0;
        final double vpCenterY = vpBds.getY() + vpBds.getHeight() / 2.0;
        currentMinX = vpCenterX - spanX / 2.0;
        currentMinY = vpCenterY - spanY / 2.0;
      } else {
        // Normal wide space or full circuit framing
        currentMinX = worldRegion.getX() - marginX;
        currentMinY = worldRegion.getY() - marginY;
      }

      currentScale = Math.min(availW / spanX, availH / spanY);
      final double contentW = spanX * currentScale;
      final double contentH = spanY * currentScale;
      currentOffsetX = PADDING + (availW - contentW) / 2.0;
      currentOffsetY = mapAreaY + PADDING + (availH - contentH) / 2.0;
    }

    final double activeScale = isDraggingFrustum ? dragScale : currentScale;
    final double activeMinX = isDraggingFrustum ? dragMinX : currentMinX;
    final double activeMinY = isDraggingFrustum ? dragMinY : currentMinY;
    final double activeOffX = isDraggingFrustum ? dragOffsetX : currentOffsetX;
    final double activeOffY = isDraggingFrustum ? dragOffsetY : currentOffsetY;

    Color compColor = UIManager.getColor("Component.accentColor");
    if (compColor == null) compColor = new Color(0, 120, 215);

    // Strictly clip rendering to map area (prevents bleeding into top HUD bar)
    final var origClip = g2.getClip();
    g2.clipRect(1, TOP_BAR_HEIGHT, w - 2, mapAreaH - 1);

    // 5. Evaluate Connected-Components Wire Display Rule:
    // Wires are disabled locally ONLY IF the view space rectangle is bigger than the
    // imaginary rectangle covering all distinct components connected with at least 1 wire.
    final Bounds connectedBounds = getConnectedComponentsBounds(circuit);
    final boolean wiresDisabled = (vpBds != null && connectedBounds != null
        && vpBds.getWidth() > connectedBounds.getWidth()
        && vpBds.getHeight() > connectedBounds.getHeight());
    final boolean showWires = !wiresDisabled;

    final double baseMapX = activeOffX - activeMinX * activeScale;
    final double baseMapY = activeOffY - activeMinY * activeScale;

    // 6. Draw Components
    final Color solidCompColor = new Color(compColor.getRed(), compColor.getGreen(), compColor.getBlue(), 180);
    g2.setColor(solidCompColor);
    for (Component c : circuit.getNonWires()) {
      Bounds b = c.getBounds();
      int mx = (int) Math.round(baseMapX + b.getX() * activeScale);
      int my = (int) Math.round(baseMapY + b.getY() * activeScale);
      int mw = Math.max(2, (int) Math.round(b.getWidth() * activeScale));
      int mh = Math.max(2, (int) Math.round(b.getHeight() * activeScale));
      if (mx + mw >= 0 && mx <= w && my + mh >= TOP_BAR_HEIGHT && my <= h) {
        g2.fillRect(mx, my, mw, mh);
      }
    }

    // 7. Draw Wires (enabled whenever view space <= connected components bounds)
    if (showWires) {
      g2.setColor(new Color(compColor.getRed(), compColor.getGreen(), compColor.getBlue(), 130));
      g2.setStroke(WIRE_STROKE);
      for (Wire wire : circuit.getWires()) {
        int x0 = (int) Math.round(baseMapX + wire.getEnd0().getX() * activeScale);
        int y0 = (int) Math.round(baseMapY + wire.getEnd0().getY() * activeScale);
        int x1 = (int) Math.round(baseMapX + wire.getEnd1().getX() * activeScale);
        int y1 = (int) Math.round(baseMapY + wire.getEnd1().getY() * activeScale);
        if ((x0 >= 0 || x1 >= 0) && (x0 <= w || x1 <= w)
            && (y0 >= TOP_BAR_HEIGHT || y1 >= TOP_BAR_HEIGHT) && (y0 <= h || y1 <= h)) {
          g2.drawLine(x0, y0, x1, y1);
        }
      }
    }

    // Circuit beacon outline if miniature in extreme wide space
    if (circBds != null) {
      int cx = (int) Math.round(activeOffX + (circBds.getX() - activeMinX) * activeScale);
      int cy = (int) Math.round(activeOffY + (circBds.getY() - activeMinY) * activeScale);
      int cw = (int) Math.round(circBds.getWidth() * activeScale);
      int ch = (int) Math.round(circBds.getHeight() * activeScale);
      if (cw < 8 || ch < 8) {
        int drawW = Math.max(6, cw);
        int drawH = Math.max(6, ch);
        int drawX = (cw < 6) ? cx - (6 - cw) / 2 : cx;
        int drawY = (ch < 6) ? cy - (6 - ch) / 2 : cy;
        g2.setColor(new Color(compColor.getRed(), compColor.getGreen(), compColor.getBlue(), 220));
        g2.drawRect(drawX, drawY, drawW, drawH);
      }
    }

    // 8. Draw Viewport Frustum Box (Clean rectangle)
    if (camera != null && vpBds != null) {
      lastVx = (int) Math.round(activeOffX + (vpBds.getX() - activeMinX) * activeScale);
      lastVy = (int) Math.round(activeOffY + (vpBds.getY() - activeMinY) * activeScale);
      lastVw = Math.max(8, (int) Math.round(vpBds.getWidth() * activeScale));
      lastVh = Math.max(8, (int) Math.round(vpBds.getHeight() * activeScale));

      // Semi-transparent frustum fill
      g2.setColor(new Color(compColor.getRed(), compColor.getGreen(), compColor.getBlue(), isDraggingFrustum ? 55 : 35));
      g2.fillRect(lastVx, lastVy, lastVw, lastVh);

      // Distinct frustum border outline
      g2.setColor(compColor);
      g2.setStroke(new BasicStroke(1.5f));
      g2.drawRect(lastVx, lastVy, lastVw, lastVh);
    }

    // Restore original clip
    g2.setClip(origClip);
    g2.dispose();
  }

  /**
   * Translates a click/drag inside the minimap to a target world coordinate,
   * centering the camera on that location instantly.
   */
  private void navigateToMinimapPoint(int mouseX, int mouseY, boolean isDrag) {
    final double scale = isDrag ? dragScale : currentScale;
    final double minX = isDrag ? dragMinX : currentMinX;
    final double minY = isDrag ? dragMinY : currentMinY;
    final double offX = isDrag ? dragOffsetX : currentOffsetX;
    final double offY = isDrag ? dragOffsetY : currentOffsetY;

    if (scale <= 0 || camera == null) return;
    double targetWorldX = minX + (mouseX - offX) / scale;
    double targetWorldY = minY + (mouseY - offY) / scale;
    camera.centerOn(targetWorldX, targetWorldY);
    canvas.repaint();
  }

  private class MinimapMouseListener extends MouseAdapter {
    @Override
    public void mouseExited(MouseEvent e) {
      if (isHoveringResizeCorner) {
        isHoveringResizeCorner = false;
        repaint();
      }
      setCursor(Cursor.getDefaultCursor());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
      wakeUp();
      final int x = e.getX();
      final int y = e.getY();
      final boolean overCorner = (x >= 0 && x <= RESIZE_CORNER_SIZE && y >= 0 && y <= RESIZE_CORNER_SIZE);

      if (overCorner != isHoveringResizeCorner) {
        isHoveringResizeCorner = overCorner;
        repaint();
      }

      if (overCorner) {
        setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
      } else if (y > TOP_BAR_HEIGHT) {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      } else {
        setCursor(Cursor.getDefaultCursor());
      }
    }

    @Override
    public void mousePressed(MouseEvent e) {
      wakeUp();
      final int x = e.getX();
      final int y = e.getY();

      // 1. Top-Left Extender Selection to freely drag resize minimap
      if (x >= 0 && x <= RESIZE_CORNER_SIZE && y >= 0 && y <= RESIZE_CORNER_SIZE) {
        isResizing = true;
        resizeStartScreenX = e.getXOnScreen();
        resizeStartScreenY = e.getYOnScreen();
        resizeStartWidth = getWidth();
        resizeStartHeight = getHeight();
        repaint();
        return;
      }

      // 2. Double click to zoom to fit applies strictly to the map area
      if (e.getClickCount() == 2) {
        canvas.zoomToFit();
        return;
      }

      // 3. Pan Drag (Click or drag anywhere on the minimap centers on that point)
      isDraggingFrustum = true;
      dragScale = currentScale;
      dragMinX = currentMinX;
      dragMinY = currentMinY;
      dragOffsetX = currentOffsetX;
      dragOffsetY = currentOffsetY;
      if (canvas != null) canvas.recordPanInputTime();
      navigateToMinimapPoint(x, y, true);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      wakeUp();
      if (isResizing) {
        isResizing = false;
        repaint();
        return;
      }
      isDraggingFrustum = false;
      repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
      wakeUp();
      // Freeform corner drag resizing (anchored at bottom-right corner)
      if (isResizing) {
        final int deltaX = e.getXOnScreen() - resizeStartScreenX;
        int targetW = resizeStartWidth - deltaX;
        targetW = Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, targetW));
        int targetH = (int) Math.round(targetW * ((double) MIN_HEIGHT / MIN_WIDTH));
        targetH = Math.max(MIN_HEIGHT, Math.min(MAX_HEIGHT, targetH));
        applyDimensions(targetW, targetH);
        return;
      }

      if (isDraggingFrustum) {
        if (canvas != null) canvas.recordPanInputTime();
        navigateToMinimapPoint(e.getX(), e.getY(), true);
      }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
      wakeUp();
      if (camera == null) return;
      final double rotation = e.getPreciseWheelRotation();
      if (rotation == 0) return;

      final double oldZoom = camera.getZoomFactor();
      final double factor = Math.pow(1.12, -rotation);
      final double newZoom = Math.max(0.05, Math.min(10.0, oldZoom * factor));

      if (Math.abs(newZoom - oldZoom) > 1e-4) {
        if (currentScale > 0) {
          final double worldX = currentMinX + (e.getX() - currentOffsetX) / currentScale;
          final double worldY = currentMinY + (e.getY() - currentOffsetY) / currentScale;

          final Bounds vp = camera.getViewportWorldBounds();
          if (vp.contains((int) Math.round(worldX), (int) Math.round(worldY))) {
            double screenX = camera.worldToScreenX(worldX);
            double screenY = camera.worldToScreenY(worldY);
            camera.zoomAt(screenX, screenY, newZoom);
          } else {
            camera.setZoomFactor(newZoom);
            double cx = (worldX + vp.getX() + vp.getWidth() / 2.0) / 2.0;
            double cy = (worldY + vp.getY() + vp.getHeight() / 2.0) / 2.0;
            camera.centerOn(cx, cy);
          }
        } else {
          camera.setZoomFactor(newZoom);
        }
        canvas.repaint();
      }
    }
  }
}
