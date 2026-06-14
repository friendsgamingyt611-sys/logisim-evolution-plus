/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.generic;

import com.cburch.logisim.prefs.PrefMonitor;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import javax.swing.SwingUtilities;

public class BasicZoomModel implements ZoomModel {
  private final List<Double> zoomOptions;

  private final PropertyChangeSupport support;
  private final CanvasPane canvas;
  private double zoomFactor;
  private boolean showGrid;
  // When true, the CanvasPane listener should NOT override scroll position
  // (scroll bars are already set correctly by the mouse-zoom caller)
  volatile boolean isMouseZoom = false;

  public BasicZoomModel(
      PrefMonitor<Boolean> gridPref,
      PrefMonitor<Double> zoomPref,
      List<Double> zoomOpts,
      CanvasPane pane) {
    zoomOptions = zoomOpts;
    support = new PropertyChangeSupport(this);
    zoomFactor = 1.0;
    showGrid = true;
    canvas = pane;

    setZoomFactor(zoomPref.get());
    setShowGrid(gridPref.getBoolean());
  }

  @Override
  public void addPropertyChangeListener(String prop, PropertyChangeListener l) {
    support.addPropertyChangeListener(prop, l);
  }

  @Override
  public boolean getShowGrid() {
    return showGrid;
  }

  @Override
  public void setShowGrid(boolean value) {
    if (value != showGrid) {
      showGrid = value;
      support.firePropertyChange(ZoomModel.SHOW_GRID, !value, value);
    }
  }

  @Override
  public double getZoomFactor() {
    return zoomFactor;
  }

  @Override
  public List<Double> getZoomOptions() {
    return zoomOptions;
  }

  @Override
  public void setZoomFactor(double value) {
    final var oldValue = zoomFactor;
    if (value != oldValue) {
      zoomFactor = value;
      support.firePropertyChange(ZoomModel.ZOOM, oldValue, value);
    }
  }

  @Override
  public void setZoomFactor(double value, MouseEvent e) {
    final var oldValue = zoomFactor;
    if (value != oldValue) {
      if (canvas == null) {
        setZoomFactor(value);
        return;
      }
      setZoomFactor(value, e.getX(), e.getY());
    }
  }

  @Override
  public void setZoomFactor(double value, int anchorX, int anchorY) {
    final var oldValue = zoomFactor;
    if (value != oldValue) {
      if (canvas == null) {
        setZoomFactor(value);
        return;
      }
      final var mouseScreenX = anchorX;
      final var mouseScreenY = anchorY;
      final var canvasX = (mouseScreenX + canvas.getHorizontalScrollBar().getValue()) / oldValue;
      final var canvasY = (mouseScreenY + canvas.getVerticalScrollBar().getValue()) / oldValue;
      final var targetViewX = (int) Math.round(canvasX * value);
      final var targetViewY = (int) Math.round(canvasY * value);
      final var newScrollX = targetViewX - mouseScreenX;
      final var newScrollY = targetViewY - mouseScreenY;

      isMouseZoom = true;
      zoomFactor = value;
      support.firePropertyChange(ZoomModel.ZOOM, oldValue, value);
      canvas.getHorizontalScrollBar().setValue(newScrollX);
      canvas.getVerticalScrollBar().setValue(newScrollY);
    }
  }

  @Override
  public void removePropertyChangeListener(String prop, PropertyChangeListener l) {
    support.removePropertyChangeListener(prop, l);
  }

  @Override
  public void setZoomFactorCenter(double value) {
    final var oldValue = zoomFactor;
    if (value != oldValue) {
      zoomFactor = value;
      support.firePropertyChange(ZoomModel.ZOOM, oldValue, value);
      SwingUtilities.invokeLater(
          () -> support.firePropertyChange(ZoomModel.CENTER, oldValue, value));
    }
  }
}
