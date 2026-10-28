/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.generic;

import com.cburch.logisim.prefs.AppPreferences;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Objects;

public class GridPainter implements PropertyChangeListener {
  public static final String ZOOM_PROPERTY = "zoom";
  public static final String SHOW_GRID_PROPERTY = "showgrid";

  public static final int BASE_GRID_SIZE = 10;
  private static final int MARGIN = 256; // Robust margin around viewport for pan reuse
  private static final int MAX_DOT_BUDGET = 100_000;

  private final Component destination;
  private final PropertyChangeSupport support;
  private Listener listener;
  private ZoomModel zoomModel;
  private boolean showGrid = true;
  private double zoomFactor = 1.0;

  // Single reusable raster buffer
  private BufferedImage cachedRaster;
  private int[] rasterPixels;
  private int rasterWidth;
  private int rasterHeight;

  // Cache validation keys
  private double cachedZoom = -1.0;
  private boolean cachedDark = false;
  private Color cachedBgColor = null;
  private Color cachedDotColor = null;
  private Color cachedMajorDotColor = null;
  private double cachedHiDpiScale = -1.0;
  private long cachedOriginLogicalX = Long.MIN_VALUE;
  private long cachedOriginLogicalY = Long.MIN_VALUE;
  private int cachedViewportX = Integer.MIN_VALUE;
  private int cachedViewportY = Integer.MIN_VALUE;

  // Hysteresis for quantized dot diameter
  private double lastMinorDiameter = 2.0;
  private double lastMajorDiameter = 3.0;

  // Cheap draw mode flag (set during zoom tween or under performance guard)
  private boolean cheapDraw = false;

  public GridPainter(Component destination) {
    this.destination = destination;
    this.support = new PropertyChangeSupport(this);

    AppPreferences.CANVAS_BG_COLOR.addPropertyChangeListener(this);
    AppPreferences.GRID_BG_COLOR.addPropertyChangeListener(this);
    AppPreferences.GRID_DOT_COLOR.addPropertyChangeListener(this);
    AppPreferences.GRID_ZOOMED_DOT_COLOR.addPropertyChangeListener(this);
    AppPreferences.LookAndFeel.addPropertyChangeListener(this);
  }

  public void addPropertyChangeListener(String prop, PropertyChangeListener listener) {
    support.addPropertyChangeListener(prop, listener);
  }

  public void removePropertyChangeListener(String prop, PropertyChangeListener listener) {
    support.removePropertyChangeListener(prop, listener);
  }

  public boolean getShowGrid() {
    return showGrid;
  }

  public void setShowGrid(boolean value) {
    if (showGrid != value) {
      showGrid = value;
      support.firePropertyChange(SHOW_GRID_PROPERTY, !value, value);
      if (destination != null) {
        destination.repaint();
      }
    }
  }

  public double getZoomFactor() {
    return zoomFactor;
  }

  public void setZoomFactor(double value) {
    final var oldValue = zoomFactor;
    if (Math.abs(oldValue - value) > 1e-6) {
      zoomFactor = value;
      support.firePropertyChange(ZOOM_PROPERTY, oldValue, value);
    }
  }

  public ZoomModel getZoomModel() {
    return zoomModel;
  }

  public void setZoomModel(ZoomModel model) {
    final var old = zoomModel;
    if (model != old) {
      if (listener == null) {
        listener = new Listener();
      }
      if (old != null) {
        old.removePropertyChangeListener(ZoomModel.ZOOM, listener);
        old.removePropertyChangeListener(ZoomModel.SHOW_GRID, listener);
      }
      zoomModel = model;
      if (model != null) {
        model.addPropertyChangeListener(ZoomModel.ZOOM, listener);
        model.addPropertyChangeListener(ZoomModel.SHOW_GRID, listener);
      }
      setShowGrid(model.getShowGrid());
      setZoomFactor(model.getZoomFactor());
      if (destination != null) {
        destination.repaint();
      }
    }
  }

  public void setCheapDraw(boolean cheap) {
    if (this.cheapDraw != cheap) {
      this.cheapDraw = cheap;
      // Invalidate so cheap mode takes effect
      cachedZoom = -1.0;
    }
  }

  public void invalidateGrid() {
    cachedZoom = -1.0;
    cachedBgColor = null;
    if (destination != null) {
      destination.repaint();
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    if (AppPreferences.GRID_BG_COLOR.isSource(event)
        || AppPreferences.GRID_DOT_COLOR.isSource(event)
        || AppPreferences.GRID_ZOOMED_DOT_COLOR.isSource(event)
        || AppPreferences.CANVAS_BG_COLOR.isSource(event)
        || AppPreferences.LookAndFeel.isSource(event)) {
      invalidateGrid();
    }
  }

  /**
   * Main entry point to paint the grid onto the canvas graphics.
   */
  public void paintGrid(Graphics g) {
    final var clip = g.getClipBounds();
    if (clip == null || clip.width <= 0 || clip.height <= 0) return;

    final var currentBg = AppPreferences.getGridBgColor();
    if (!showGrid) {
      g.setColor(currentBg);
      g.fillRect(clip.x, clip.y, clip.width, clip.height);
      return;
    }

    final var g2d = (g instanceof Graphics2D) ? (Graphics2D) g : null;
    final var transform = (g2d != null) ? g2d.getTransform() : null;
    final var hiDpiScale = (transform != null) ? Math.max(1.0, transform.getScaleX()) : 1.0;

    final var vr = (destination instanceof javax.swing.JComponent jc) ? jc.getVisibleRect() : null;
    final var viewRect = (vr != null && vr.width > 0) ? vr : clip;

    ensureRaster(viewRect, currentBg, hiDpiScale);

    if (cachedRaster != null) {
      // Blit cached raster directly
      if (hiDpiScale == 1.0) {
        g.drawImage(cachedRaster, cachedViewportX, cachedViewportY, destination);
      } else {
        final var userW = (int) Math.round(rasterWidth / hiDpiScale);
        final var userH = (int) Math.round(rasterHeight / hiDpiScale);
        g.drawImage(cachedRaster, cachedViewportX, cachedViewportY, userW, userH, destination);
      }
    }
  }

  /**
   * Ensures the cached raster matches current viewport, zoom, colors, and fills it.
   */
  private void ensureRaster(Rectangle viewRect, Color currentBg, double hiDpiScale) {
    final var dark = AppPreferences.isDarkTheme();
    final var dotColor = AppPreferences.getGridMinorDotColor();
    final var majorDotColor = AppPreferences.getGridMajorDotColor();

    final var neededW = (int) Math.ceil((viewRect.width + 2 * MARGIN) * hiDpiScale);
    final var neededH = (int) Math.ceil((viewRect.height + 2 * MARGIN) * hiDpiScale);

    // Check if buffer reallocation is needed due to window resize
    if (cachedRaster == null || rasterWidth < neededW || rasterHeight < neededH) {
      rasterWidth = Math.max(rasterWidth, neededW);
      rasterHeight = Math.max(rasterHeight, neededH);
      cachedRaster = new BufferedImage(rasterWidth, rasterHeight, BufferedImage.TYPE_INT_RGB);
      rasterPixels = ((DataBufferInt) cachedRaster.getRaster().getDataBuffer()).getData();
      cachedZoom = -1.0; // Force regeneration
    }

    // Check if we can reuse the raster during panning
    final var userW = rasterWidth / hiDpiScale;
    final var userH = rasterHeight / hiDpiScale;
    final var canReusePan = (cachedZoom == zoomFactor)
        && (cachedDark == dark)
        && Objects.equals(cachedBgColor, currentBg)
        && Objects.equals(cachedDotColor, dotColor)
        && Objects.equals(cachedMajorDotColor, majorDotColor)
        && (cachedHiDpiScale == hiDpiScale)
        && (viewRect.x >= cachedViewportX)
        && (viewRect.y >= cachedViewportY)
        && (viewRect.x + viewRect.width <= cachedViewportX + userW)
        && (viewRect.y + viewRect.height <= cachedViewportY + userH);

    if (canReusePan) {
      return; // Reusing cached raster directly via integer offset!
    }

    // Regenerate raster centered around the current viewport
    cachedViewportX = viewRect.x - MARGIN;
    cachedViewportY = viewRect.y - MARGIN;
    cachedZoom = zoomFactor;
    cachedDark = dark;
    cachedBgColor = currentBg;
    cachedDotColor = dotColor;
    cachedMajorDotColor = majorDotColor;
    cachedHiDpiScale = hiDpiScale;

    renderRaster(currentBg, dotColor, majorDotColor, hiDpiScale);
  }

  /**
   * Renders dots into rasterPixels directly using camera math.
   * Dot positions: round(Lx * zoomFactor - cachedViewportX)
   */
  private void renderRaster(Color bg, Color dotColor, Color majorDotColor, double hiDpiScale) {
    final var bgRgb = bg.getRGB();
    Arrays.fill(rasterPixels, bgRgb);

    final var effZoom = (zoomFactor <= 0.0) ? 1.0 : zoomFactor;

    // LOD decimation calculation
    var step = BASE_GRID_SIZE;
    while (step * effZoom < 4.0 && step < 10_000) {
      step *= 5;
    }

    // Dot-count budget check
    final var visibleW = rasterWidth / hiDpiScale;
    final var visibleH = rasterHeight / hiDpiScale;
    final var estDotsX = (long) Math.ceil(visibleW / (step * effZoom));
    final var estDotsY = (long) Math.ceil(visibleH / (step * effZoom));
    if (estDotsX * estDotsY > MAX_DOT_BUDGET && step < 10_000) {
      step *= 5;
    }

    final var majorStep = step * 5;
    final var spacing = step * effZoom;

    // Continuous minor dot alpha: 0.0 at spacing <= 4.0px, 1.0 at spacing >= 8.0px
    float minorAlpha;
    if (spacing <= 4.0) {
      minorAlpha = 0.0f;
    } else if (spacing >= 8.0) {
      minorAlpha = 1.0f;
    } else {
      final var t = (float) ((spacing - 4.0) / 4.0);
      minorAlpha = t * t * (3.0f - 2.0f * t); // Smoothstep curve
    }

    // Quantize dot diameter to 0.5px steps with hysteresis
    final var scale = Math.min(1.5, Math.pow(Math.max(1.0, effZoom), 0.35));
    final var rawMinorD = Math.min(3.5, 2.0 * scale);
    final var rawMajorD = Math.min(4.0, 3.2 * scale);

    final var minorD = quantizeDiameterWithHysteresis(rawMinorD, lastMinorDiameter);
    final var majorD = quantizeDiameterWithHysteresis(rawMajorD, lastMajorDiameter);
    lastMinorDiameter = minorD;
    lastMajorDiameter = majorD;

    final var blendedMinor = blend(bg, dotColor, minorAlpha);
    final var minorRgb = blendedMinor.getRGB();
    final var majorRgb = majorDotColor.getRGB();

    // Bounds in logical schematic coordinates
    // Using Math.floorDiv for robust negative coordinate handling
    final var minLogX = (long) Math.floor((double) cachedViewportX / effZoom);
    final var maxLogX = (long) Math.ceil((double) (cachedViewportX + visibleW) / effZoom);
    final var minLogY = (long) Math.floor((double) cachedViewportY / effZoom);
    final var maxLogY = (long) Math.ceil((double) (cachedViewportY + visibleH) / effZoom);

    final var startLogX = Math.floorDiv(minLogX, step) * step;
    final var endLogX = Math.floorDiv(maxLogX + step - 1, step) * step;
    final var startLogY = Math.floorDiv(minLogY, step) * step;
    final var endLogY = Math.floorDiv(maxLogY + step - 1, step) * step;

    final var drawMinor = !cheapDraw && (minorAlpha > 0.01f);
    final var minorRadius = (int) Math.max(1, Math.round((minorD * hiDpiScale) / 2.0));
    final var majorRadius = (int) Math.max(1, Math.round((majorD * hiDpiScale) / 2.0));

    for (var ly = startLogY; ly <= endLogY; ly += step) {
      final var isMajorY = (Math.floorMod(ly, majorStep) == 0);
      // Absolute camera device coordinate
      final var py = (int) Math.round((ly * effZoom - cachedViewportY) * hiDpiScale);
      if (py < -4 || py >= rasterHeight + 4) continue;

      for (var lx = startLogX; lx <= endLogX; lx += step) {
        final var isMajorX = (Math.floorMod(lx, majorStep) == 0);
        final var isMajor = isMajorX && isMajorY;

        if (!isMajor && !drawMinor) continue;

        final var px = (int) Math.round((lx * effZoom - cachedViewportX) * hiDpiScale);
        if (px < -4 || px >= rasterWidth + 4) continue;

        if (isMajor) {
          stampDot(px, py, majorRadius, majorRgb);
        } else {
          stampDot(px, py, minorRadius, minorRgb);
        }
      }
    }
  }

  /**
   * Fast raster dot stamp with clipping.
   */
  private void stampDot(int cx, int cy, int radius, int colorRgb) {
    if (radius <= 1) {
      if (cx >= 0 && cx < rasterWidth && cy >= 0 && cy < rasterHeight) {
        rasterPixels[cy * rasterWidth + cx] = colorRgb;
      }
      return;
    }

    final var r2 = radius * radius;
    final var y0 = Math.max(0, cy - radius);
    final var y1 = Math.min(rasterHeight - 1, cy + radius);
    final var x0 = Math.max(0, cx - radius);
    final var x1 = Math.min(rasterWidth - 1, cx + radius);

    for (var y = y0; y <= y1; y++) {
      final var dy = y - cy;
      final var dy2 = dy * dy;
      final var rowOff = y * rasterWidth;
      for (var x = x0; x <= x1; x++) {
        final var dx = x - cx;
        if (dx * dx + dy2 <= r2) {
          rasterPixels[rowOff + x] = colorRgb;
        }
      }
    }
  }

  /**
   * Quantizes diameter to 0.5px steps with a +-0.08px deadband hysteresis.
   */
  private static double quantizeDiameterWithHysteresis(double raw, double last) {
    final var nearestHalf = Math.round(raw * 2.0) / 2.0;
    if (Math.abs(raw - last) < 0.29) {
      return last; // Keep current quantized size to prevent oscillation
    }
    return nearestHalf;
  }

  private static Color blend(Color bg, Color fg, float alpha) {
    if (alpha <= 0.0f) return bg;
    if (alpha >= 1.0f) return fg;
    final var r = Math.round(bg.getRed() * (1f - alpha) + fg.getRed() * alpha);
    final var g = Math.round(bg.getGreen() * (1f - alpha) + fg.getGreen() * alpha);
    final var b = Math.round(bg.getBlue() * (1f - alpha) + fg.getBlue() * alpha);
    return new Color(r, g, b);
  }

  private class Listener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent event) {
      final var prop = event.getPropertyName();
      final var val = event.getNewValue();
      if (prop.equals(ZoomModel.ZOOM)) {
        setZoomFactor((Double) val);
        if (destination != null) destination.repaint();
      } else if (prop.equals(ZoomModel.SHOW_GRID)) {
        setShowGrid((Boolean) val);
        if (destination != null) destination.repaint();
      }
    }
  }
}
