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
  private boolean cheapDraw = false;
  private double lastMinorDiameter = 2.0;
  private double lastMajorDiameter = 3.0;

  public GridPainter(Component destination) {
    this.destination = destination;
    this.support = new PropertyChangeSupport(this);

    AppPreferences.CANVAS_BG_COLOR.addPropertyChangeListener(this);
    AppPreferences.GRID_BG_COLOR.addPropertyChangeListener(this);
    AppPreferences.GRID_DOT_COLOR.addPropertyChangeListener(this);
    AppPreferences.GRID_ZOOMED_DOT_COLOR.addPropertyChangeListener(this);
    AppPreferences.LookAndFeel.addPropertyChangeListener(this);
    javax.swing.UIManager.addPropertyChangeListener(this);
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
    this.cheapDraw = cheap;
  }

  public void invalidateGrid() {
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
        || AppPreferences.LookAndFeel.isSource(event)
        || "lookAndFeel".equals(event.getPropertyName())) {
      invalidateGrid();
    }
  }

  /**
   * Main entry point to paint the grid onto the canvas graphics.
   */
  public void paintGrid(Graphics g) {
    final com.cburch.logisim.gui.main.Camera2D cam = (destination instanceof com.cburch.logisim.gui.main.Canvas cv)
        ? cv.getCamera()
        : null;
    paintInfiniteGrid(g, cam);
  }

  /**
   * Multisim/Altium architecture: Paints the infinite dot grid directly in Screen Space.
   * Modulo arithmetic maps screen coordinates to the world grid without floating-point drift,
   * completely eliminating voids, clipping boundaries, and memory allocations.
   */
  public void paintInfiniteGrid(Graphics g, com.cburch.logisim.gui.main.Camera2D camera) {
    int width = destination != null ? destination.getWidth() : 0;
    int height = destination != null ? destination.getHeight() : 0;
    final var clip = g.getClipBounds();
    if (width <= 0 || height <= 0) {
      if (clip != null && clip.width > 0 && clip.height > 0) {
        width = clip.x + clip.width;
        height = clip.y + clip.height;
      } else {
        return;
      }
    }

    final var currentBg = AppPreferences.getGridBgColor();
    g.setColor(currentBg);
    if (clip != null) {
      g.fillRect(clip.x, clip.y, clip.width, clip.height);
    } else {
      g.fillRect(0, 0, width, height);
    }

    if (!showGrid) {
      return;
    }

    final double effZoom = (camera != null) ? camera.getZoomFactor() : Math.max(0.05, zoomFactor);

    // LOD Decimation: Ensure dots don't clump into a solid texture when zoomed far out
    int step = BASE_GRID_SIZE;
    while (step * effZoom < 6.0 && step < 10_000) {
      step *= 5;
    }

    final double dotSpacing = step * effZoom;
    if (dotSpacing <= 0) return;

    final var dotColor = AppPreferences.getGridMinorDotColor();
    final var majorDotColor = AppPreferences.getGridMajorDotColor();

    // Minor dot alpha smoothstep: 0.0 at spacing <= 4.0px, 1.0 at spacing >= 8.0px
    float minorAlpha;
    if (dotSpacing <= 4.0) {
      minorAlpha = 0.0f;
    } else if (dotSpacing >= 8.0) {
      minorAlpha = 1.0f;
    } else {
      final var t = (float) ((dotSpacing - 4.0) / 4.0);
      minorAlpha = t * t * (3.0f - 2.0f * t);
    }

    final var blendedMinor = blend(currentBg, dotColor, minorAlpha);
    boolean drawMinor = !cheapDraw && (minorAlpha > 0.01f);
    final int majorStep = step * 5;

    // Hard Grid Cutoff at macro zoom levels (<15% zoom):
    // Minor dots become solid visual noise; drop them entirely.
    if (camera != null && effZoom < 0.15) {
      drawMinor = false;
      // If even major dots become too densely crowded (< 8px screen spacing), abort grid entirely
      if (majorStep * effZoom < 8.0) {
        return;
      }
    }

    // Dot diameter based on zoom
    final double scale = Math.min(1.5, Math.pow(Math.max(1.0, effZoom), 0.35));
    final int minorD = Math.max(1, (int) Math.round(quantizeDiameterWithHysteresis(Math.min(3.5, 2.0 * scale), lastMinorDiameter)));
    final int majorD = Math.max(2, (int) Math.round(quantizeDiameterWithHysteresis(Math.min(4.0, 3.2 * scale), lastMajorDiameter)));

    // Logical schematic grid iteration to guarantee clean major grid dot alignment
    final double leftWorldX = (camera != null) ? camera.screenToWorldX(0) : (clip != null ? clip.x / effZoom : 0);
    final double rightWorldX = (camera != null) ? camera.screenToWorldX(width) : (clip != null ? (clip.x + clip.width) / effZoom : width / effZoom);
    final double topWorldY = (camera != null) ? camera.screenToWorldY(0) : (clip != null ? clip.y / effZoom : 0);
    final double bottomWorldY = (camera != null) ? camera.screenToWorldY(height) : (clip != null ? (clip.y + clip.height) / effZoom : height / effZoom);

    final long startLogX = (long) Math.floor(Math.min(leftWorldX, rightWorldX) / step) * step;
    final long endLogX = (long) Math.ceil(Math.max(leftWorldX, rightWorldX) / step) * step;
    final long startLogY = (long) Math.floor(Math.min(topWorldY, bottomWorldY) / step) * step;
    final long endLogY = (long) Math.ceil(Math.max(topWorldY, bottomWorldY) / step) * step;

    final int minScreenY = (clip != null) ? clip.y - 2 : -2;
    final int maxScreenY = (clip != null) ? clip.y + clip.height + 2 : height + 2;
    final int minScreenX = (clip != null) ? clip.x - 2 : -2;
    final int maxScreenX = (clip != null) ? clip.x + clip.width + 2 : width + 2;

    // 1. Draw minor dots in a single batch (only 1 setColor call, zero state flushes)
    if (drawMinor) {
      g.setColor(blendedMinor);
      for (long ly = startLogY; ly <= endLogY; ly += step) {
        final int py = (camera != null)
            ? (int) Math.round(camera.worldToScreenY(ly))
            : (int) Math.round(ly * effZoom);
        if (py < minScreenY || py >= maxScreenY) continue;
        final boolean isMajorY = (ly % majorStep == 0);

        for (long lx = startLogX; lx <= endLogX; lx += step) {
          if (isMajorY && (lx % majorStep == 0)) continue; // major dot drawn in pass 2
          final int px = (camera != null)
              ? (int) Math.round(camera.worldToScreenX(lx))
              : (int) Math.round(lx * effZoom);
          if (px < minScreenX || px >= maxScreenX) continue;
          g.fillRect(px - minorD / 2, py - minorD / 2, minorD, minorD);
        }
      }
    }

    // 2. Draw major dots in a single batch (step = majorStep directly, minimal loop count)
    g.setColor(majorDotColor);
    final long startMajorX = (long) Math.floor(Math.min(leftWorldX, rightWorldX) / majorStep) * majorStep;
    final long endMajorX = (long) Math.ceil(Math.max(leftWorldX, rightWorldX) / majorStep) * majorStep;
    final long startMajorY = (long) Math.floor(Math.min(topWorldY, bottomWorldY) / majorStep) * majorStep;
    final long endMajorY = (long) Math.ceil(Math.max(topWorldY, bottomWorldY) / majorStep) * majorStep;

    for (long ly = startMajorY; ly <= endMajorY; ly += majorStep) {
      final int py = (camera != null)
          ? (int) Math.round(camera.worldToScreenY(ly))
          : (int) Math.round(ly * effZoom);
      if (py < minScreenY || py >= maxScreenY) continue;

      for (long lx = startMajorX; lx <= endMajorX; lx += majorStep) {
        final int px = (camera != null)
            ? (int) Math.round(camera.worldToScreenX(lx))
            : (int) Math.round(lx * effZoom);
        if (px < minScreenX || px >= maxScreenX) continue;
        g.fillRect(px - majorD / 2, py - majorD / 2, majorD, majorD);
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
