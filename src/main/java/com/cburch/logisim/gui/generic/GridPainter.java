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
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class GridPainter implements PropertyChangeListener {
  public static final String ZOOM_PROPERTY = "zoom";
  public static final String SHOW_GRID_PROPERTY = "showgrid";

  private final Component destination;
  private final PropertyChangeSupport support;
  private final int gridSize = 10;
  private Listener listener;
  private ZoomModel zoomModel;
  private boolean showGrid = true;
  private double zoomFactor = 1.0;
  private BufferedImage gridImage;
  private int gridImageWidth;
  private int gridImageHeight;

  public GridPainter(Component destination) {
    this.destination = destination;
    support = new PropertyChangeSupport(this);
    createGridImage(gridSize, zoomFactor);

    AppPreferences.CANVAS_BG_COLOR.addPropertyChangeListener(this);
    AppPreferences.GRID_BG_COLOR.addPropertyChangeListener(this);
    AppPreferences.GRID_DOT_COLOR.addPropertyChangeListener(this);
    AppPreferences.GRID_ZOOMED_DOT_COLOR.addPropertyChangeListener(this);
    AppPreferences.LookAndFeel.addPropertyChangeListener(this);
  }

  public void addPropertyChangeListener(String prop, PropertyChangeListener listener) {
    support.addPropertyChangeListener(prop, listener);
  }

  public boolean getShowGrid() {
    return showGrid;
  }

  public void setShowGrid(boolean value) {
    if (showGrid != value) {
      showGrid = value;
      support.firePropertyChange(SHOW_GRID_PROPERTY, !value, value);
    }
  }

  public double getZoomFactor() {
    return zoomFactor;
  }

  public void setZoomFactor(double value) {
    final var oldValue = zoomFactor;
    if (oldValue != value) {
      zoomFactor = value;
      createGridImage(gridSize, value);
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
      destination.repaint();
    }
  }

  public void paintGrid(Graphics g) {
    final var clip = g.getClipBounds();
    if (clip == null) return;

    if (!showGrid) {
      g.setColor(AppPreferences.getCanvasBgColor());
      g.fillRect(clip.x, clip.y, clip.width, clip.height);
      return;
    }

    final var tw = gridImageWidth;
    final var th = gridImageHeight;
    if (tw <= 0 || th <= 0 || gridImage == null) return;

    final var x0 = Math.floorDiv(clip.x, tw) * tw;
    final var y0 = Math.floorDiv(clip.y, th) * th;
    final var maxX = clip.x + clip.width;
    final var maxY = clip.y + clip.height;

    for (var x = x0; x < maxX; x += tw) {
      for (var y = y0; y < maxY; y += th) {
        g.drawImage(gridImage, x, y, destination);
      }
    }
  }

  public void removePropertyChangeListener(String prop, PropertyChangeListener listener) {
    support.removePropertyChangeListener(prop, listener);
  }

  private void createGridImage(int size, double f) {
    var effF = (f <= 0.0) ? 1.0 : f;
    final var bg = AppPreferences.getGridBgColor();
    final var minorBase = AppPreferences.getGridMinorDotColor();
    final var majorBase = AppPreferences.getGridMajorDotColor();

    final var baseStep = size;
    final var majorStep = size * 5;

    // 5x hierarchical LOD decimation
    var step = baseStep;
    while (step * effF < 4.0 && step < 1000) {
      step *= 5;
    }
    final var currentMajorStep = Math.max(step * 5, majorStep);

    // Calculate spacing of the finest visible dots in pixels
    final var spacing = step * effF;
    final var majorSpacing = currentMajorStep * effF;

    // Pick k so that the tile has integer dimensions without subpixel drift
    var k = 1;
    while (k * majorSpacing < 100 && k < 10) k++;
    final var tileW = Math.max(1, (int) Math.round(k * majorSpacing));
    final var tileH = tileW;

    final var img = new BufferedImage(tileW, tileH, BufferedImage.TYPE_INT_RGB);
    final var g2 = img.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

    g2.setColor(bg);
    g2.fillRect(0, 0, tileW, tileH);

    // Scale dot diameter gently with zoom, capped at 3.5px for minor, 4.0px for major
    final var scale = Math.min(1.5, Math.pow(Math.max(1.0, effF), 0.35));
    final var minorD = Math.min(3.5, 2.0 * scale);
    final var majorD = Math.min(4.0, 3.2 * scale);

    // 4px to 8px fade for the minor dots
    var minorAlpha = 1.0f;
    var drawMinor = true;
    if (spacing < 4.0) {
      drawMinor = false;
    } else if (spacing < 8.0) {
      minorAlpha = (float) ((spacing - 4.0) / 4.0);
    }

    final var minorColor = blend(bg, minorBase, minorAlpha);
    final var totalUnits = k * currentMajorStep;

    // Draw minor dots
    if (drawMinor && minorAlpha > 0.01f) {
      g2.setColor(minorColor);
      for (var uy = 0; uy < totalUnits; uy += step) {
        final var isMajorY = (uy % currentMajorStep == 0);
        final var sy = (uy * (double) tileH) / totalUnits;
        for (var ux = 0; ux < totalUnits; ux += step) {
          if (isMajorY && (ux % currentMajorStep == 0)) continue;
          final var sx = (ux * (double) tileW) / totalUnits;
          g2.fill(new Ellipse2D.Double(sx - minorD / 2.0, sy - minorD / 2.0, minorD, minorD));
        }
      }
    }

    // Draw major dots (every currentMajorStep, wrapping across borders for seamless tiling)
    g2.setColor(majorBase);
    for (var uy = 0; uy <= totalUnits; uy += currentMajorStep) {
      final var sy = (uy * (double) tileH) / totalUnits;
      for (var ux = 0; ux <= totalUnits; ux += currentMajorStep) {
        final var sx = (ux * (double) tileW) / totalUnits;
        g2.fill(new Ellipse2D.Double(sx - majorD / 2.0, sy - majorD / 2.0, majorD, majorD));
      }
    }
    g2.dispose();

    gridImage = img;
    gridImageWidth = tileW;
    gridImageHeight = tileH;
  }

  private static Color blend(Color bg, Color fg, float alpha) {
    if (alpha <= 0.0f) return bg;
    if (alpha >= 1.0f) return fg;
    final var r = Math.round(bg.getRed() * (1f - alpha) + fg.getRed() * alpha);
    final var g = Math.round(bg.getGreen() * (1f - alpha) + fg.getGreen() * alpha);
    final var b = Math.round(bg.getBlue() * (1f - alpha) + fg.getBlue() * alpha);
    return new Color(r, g, b);
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    if (AppPreferences.GRID_BG_COLOR.isSource(event)
        || AppPreferences.GRID_DOT_COLOR.isSource(event)
        || AppPreferences.GRID_ZOOMED_DOT_COLOR.isSource(event)
        || AppPreferences.CANVAS_BG_COLOR.isSource(event)
        || AppPreferences.LookAndFeel.isSource(event)) {
      createGridImage(gridSize, zoomFactor);
      if (destination != null) {
        destination.repaint();
      }
    }
  }

  private class Listener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent event) {
      final var prop = event.getPropertyName();
      final var val = event.getNewValue();
      if (prop.equals(ZoomModel.ZOOM)) {
        setZoomFactor((Double) val);
        destination.repaint();
      } else if (prop.equals(ZoomModel.SHOW_GRID)) {
        setShowGrid((Boolean) val);
        destination.repaint();
      }
    }
  }
}
