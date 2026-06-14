/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.generic;

import com.cburch.contracts.BaseComponentListenerContract;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class CanvasPane extends JScrollPane {
  private static final long serialVersionUID = 1L;
  private final CanvasPaneContents contents;
  private final Listener listener;
  private final ZoomListener zoomListener;
  private ZoomModel zoomModel;

  public CanvasPane(CanvasPaneContents contents) {
    super((Component) contents);
    this.contents = contents;
    this.listener = new Listener();
    this.zoomListener = new ZoomListener();
    this.zoomModel = null;
    addComponentListener(listener);
    setWheelScrollingEnabled(false);
    if (contents instanceof Component comp) {
      if (comp.getMouseWheelListeners().length == 0) {
        addMouseWheelListener(zoomListener);
      }
    } else {
      addMouseWheelListener(zoomListener);
    }
    contents.setCanvasPane(this);
  }

  public Dimension getViewportSize() {
    final var size = new Dimension();
    getViewport().getSize(size);
    return size;
  }

  public double getZoomFactor() {
    final var model = zoomModel;
    return model == null ? 1.0 : model.getZoomFactor();
  }

  public void setZoomModel(ZoomModel model) {
    final var oldModel = zoomModel;
    if (oldModel != null) {
      oldModel.removePropertyChangeListener(ZoomModel.ZOOM, listener);
      oldModel.removePropertyChangeListener(ZoomModel.CENTER, listener);
    }
    zoomModel = model;
    if (model != null) {
      model.addPropertyChangeListener(ZoomModel.ZOOM, listener);
      model.addPropertyChangeListener(ZoomModel.CENTER, listener);
    }
  }

  public Dimension supportPreferredSize(int width, int height) {
    final var zoom = getZoomFactor();
    if (zoom != 1.0) {
      width = (int) Math.ceil(width * zoom);
      height = (int) Math.ceil(height * zoom);
    }
    final var minSize = getViewportSize();
    if (minSize.width > width) width = minSize.width;
    if (minSize.height > height) height = minSize.height;
    return new Dimension(width, height);
  }

  public int supportScrollableBlockIncrement(
      Rectangle visibleRect, int orientation, int direction) {
    final var unit = supportScrollableUnitIncrement(visibleRect, orientation, direction);
    return (direction == SwingConstants.VERTICAL)
        ? visibleRect.height / unit * unit
        : visibleRect.width / unit * unit;
  }

  public int supportScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    return (int) Math.round(10 * getZoomFactor());
  }

  private class Listener implements BaseComponentListenerContract, PropertyChangeListener {

    @Override
    public void componentHidden(ComponentEvent e) {
      // do nothing
    }

    @Override
    public void componentMoved(ComponentEvent e) {
      // do nothing
    }

    //
    // ComponentListener methods
    //
    @Override
    public void componentResized(ComponentEvent e) {
      contents.recomputeSize();
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
      final var prop = e.getPropertyName();
      if (prop.equals(ZoomModel.ZOOM)) {
        final var model = zoomModel;
        // If this zoom was triggered by a mouse-zoom (scroll wheel or Ctrl+scroll),
        // skip the scroll-bar override — the caller already set them correctly.
        if (model instanceof BasicZoomModel bzm && bzm.isMouseZoom) {
          bzm.isMouseZoom = false;
          contents.recomputeSize();
          return;
        }
        final var oldZoom = (Double) e.getOldValue();
        var r = getViewport().getViewRect();
        final var cx = (r.x + r.width / 2) / oldZoom;
        final var cy = (r.y + r.height / 2) / oldZoom;

        final var newZoom = (Double) e.getNewValue();
        r = getViewport().getViewRect();
        final var hv = (int) (cx * newZoom) - r.width / 2;
        final var vv = (int) (cy * newZoom) - r.height / 2;
        getHorizontalScrollBar().setValue(hv);
        getVerticalScrollBar().setValue(vv);
        contents.recomputeSize();
      } else if (prop.equals(ZoomModel.CENTER)) {
        contents.center();
      }
    }
  }

  private class ZoomListener implements MouseWheelListener {
    @Override
    public void mouseWheelMoved(MouseWheelEvent mwe) {
      if (mwe.isControlDown()) {
        // Continuous exponential zoom using precise wheel rotation for smoothness
        final var opts = zoomModel.getZoomOptions();
        final var min = opts.get(0) / 100.0;
        final var max = opts.get(opts.size() - 1) / 100.0;
        final var zoom = zoomModel.getZoomFactor();
        // smaller base gives finer control; use exponential scale for natural feel
        final double base = 1.05; // ~5% per rotation unit
        final double rotation = mwe.getPreciseWheelRotation();
        final double factor = Math.pow(base, -rotation);
        final double newZoom = Math.max(min, Math.min(max, zoom * factor));
        zoomModel.setZoomFactor(newZoom, mwe);
      } else if (mwe.isShiftDown()) {
        getHorizontalScrollBar()
            .setValue(scrollValue(getHorizontalScrollBar(), mwe.getWheelRotation()));
      } else {
        getVerticalScrollBar()
            .setValue(scrollValue(getVerticalScrollBar(), mwe.getWheelRotation()));
      }
    }

    private int scrollValue(JScrollBar bar, int val) {
      if (val > 0) {
        return Math.min(bar.getMaximum(), bar.getValue() + val * bar.getUnitIncrement());
      } else {
        return Math.max(bar.getMinimum(), bar.getValue() + val * bar.getUnitIncrement());
      }
    }
  }
}
