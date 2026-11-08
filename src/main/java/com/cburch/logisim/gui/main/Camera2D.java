/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.main;

import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.gui.generic.ZoomModel;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 * 2D Camera Model representing an infinite 2D canvas viewport.
 * Directly implements Logisim's ZoomModel to serve as the single authoritative
 * source of truth for both canvas transforms and application UI zoom controls.
 */
public class Camera2D implements ZoomModel {
  public static final String PROPERTY_TRANSFORM = "cameraTransform";
  public static final String PROPERTY_PAN = "cameraPan";

  private final PropertyChangeSupport support = new PropertyChangeSupport(this);

  // Pan offsets: world (0, 0) is at screen center when panX = 0 and panY = 0
  private double panX = 0.0;
  private double panY = 0.0;

  // Zoom scale factor
  private double zoomFactor = 1.0;
  private boolean showGrid = true;
  private final List<Double> zoomOptions;

  // Viewport dimensions
  private int viewportWidth = 800;
  private int viewportHeight = 600;

  public Camera2D() {
    this.zoomOptions = createDefaultZoomSteps();
  }

  public Camera2D(List<Double> zoomSteps) {
    this.zoomOptions = (zoomSteps != null && !zoomSteps.isEmpty())
        ? Collections.unmodifiableList(new ArrayList<>(zoomSteps))
        : createDefaultZoomSteps();
  }

  private static List<Double> createDefaultZoomSteps() {
    final var steps = new ArrayList<Double>();
    int[] maxZooms = {50, 200, 1000};
    int[] stepVals = {5, 10, 20};
    double z = 0.0;
    for (int i = 0; i < maxZooms.length; i++) {
      while (z < maxZooms[i]) {
        z += stepVals[i];
        steps.add(z);
      }
    }
    return Collections.unmodifiableList(steps);
  }

  // ---------------------------------------------------------------------------
  // ZoomModel Contract Implementation (Single Source of Truth)
  // ---------------------------------------------------------------------------

  @Override
  public boolean getShowGrid() {
    return showGrid;
  }

  @Override
  public void setShowGrid(boolean value) {
    if (this.showGrid != value) {
      this.showGrid = value;
      support.firePropertyChange(ZoomModel.SHOW_GRID, !value, value);
      support.firePropertyChange(PROPERTY_TRANSFORM, null, this);
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
    // Zoom toward center of viewport when no anchor is specified
    zoomAt(viewportWidth / 2.0, viewportHeight / 2.0, value);
  }

  @Override
  public void setZoomFactor(double value, MouseEvent e) {
    if (e != null) {
      zoomAt(e.getX(), e.getY(), value);
    } else {
      setZoomFactor(value);
    }
  }

  @Override
  public void setZoomFactor(double value, int anchorX, int anchorY) {
    zoomAt(anchorX, anchorY, value);
  }

  @Override
  public void setZoomFactorCenter(double value) {
    zoomAt(viewportWidth / 2.0, viewportHeight / 2.0, value);
    SwingUtilities.invokeLater(() -> support.firePropertyChange(ZoomModel.CENTER, null, value));
  }

  @Override
  public void addPropertyChangeListener(String prop, PropertyChangeListener l) {
    support.addPropertyChangeListener(prop, l);
  }

  @Override
  public void removePropertyChangeListener(String prop, PropertyChangeListener l) {
    support.removePropertyChangeListener(prop, l);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    support.removePropertyChangeListener(listener);
  }

  // ---------------------------------------------------------------------------
  // 2D Viewport & Panning Controls
  // ---------------------------------------------------------------------------

  public double getPanX() {
    return panX;
  }

  public double getPanY() {
    return panY;
  }

  public int getViewportWidth() {
    return viewportWidth;
  }

  public int getViewportHeight() {
    return viewportHeight;
  }

  public void setViewportSize(int width, int height) {
    if (this.viewportWidth != width || this.viewportHeight != height) {
      this.viewportWidth = Math.max(1, width);
      this.viewportHeight = Math.max(1, height);
      support.firePropertyChange(PROPERTY_TRANSFORM, null, this);
    }
  }

  public void setPan(double newPanX, double newPanY) {
    if (this.panX != newPanX || this.panY != newPanY) {
      double oldX = this.panX;
      double oldY = this.panY;
      this.panX = newPanX;
      this.panY = newPanY;
      support.firePropertyChange(PROPERTY_PAN, new Point2D.Double(oldX, oldY), new Point2D.Double(newPanX, newPanY));
      support.firePropertyChange(PROPERTY_TRANSFORM, null, this);
    }
  }

  public void panBy(double dx, double dy) {
    setPan(this.panX + dx, this.panY + dy);
  }

  /**
   * Zooms toward or away from a specific screen coordinate anchor (e.g. mouse cursor)
   * while guaranteeing pixel-perfect position stability under the cursor.
   */
  public void zoomAt(double screenX, double screenY, double newZoom) {
    newZoom = Math.max(0.05, Math.min(10.0, newZoom));
    if (Math.abs(this.zoomFactor - newZoom) < 0.0001) {
      return;
    }
    double oldZoom = this.zoomFactor;

    // Calculate world coordinate under cursor before zoom
    double worldX = screenToWorldX(screenX);
    double worldY = screenToWorldY(screenY);

    this.zoomFactor = newZoom;

    // Adjust pan so the world point stays exactly under (screenX, screenY)
    double newPanX = screenX - (worldX * newZoom) - (viewportWidth / 2.0);
    double newPanY = screenY - (worldY * newZoom) - (viewportHeight / 2.0);

    this.panX = newPanX;
    this.panY = newPanY;

    support.firePropertyChange(ZoomModel.ZOOM, oldZoom, newZoom);
    support.firePropertyChange(PROPERTY_PAN, null, new Point2D.Double(newPanX, newPanY));
    support.firePropertyChange(PROPERTY_TRANSFORM, null, this);
  }

  /**
   * Centers the camera on the given world coordinates.
   */
  public void centerOn(double worldX, double worldY) {
    setPan(-worldX * zoomFactor, -worldY * zoomFactor);
  }

  /**
   * Resets pan and zoom to origin (0, 0) centered in viewport.
   */
  public void resetToCenter() {
    this.zoomFactor = 1.0;
    setPan(0.0, 0.0);
    support.firePropertyChange(ZoomModel.ZOOM, null, 1.0);
  }

  // ---------------------------------------------------------------------------
  // Coordinate Conversions & Viewport Culling Bounds
  // ---------------------------------------------------------------------------

  /**
   * Converts a screen X pixel coordinate to world circuit coordinate.
   */
  public double screenToWorldX(double screenX) {
    return (screenX - panX - (viewportWidth / 2.0)) / zoomFactor;
  }

  /**
   * Converts a screen Y pixel coordinate to world circuit coordinate.
   */
  public double screenToWorldY(double screenY) {
    return (screenY - panY - (viewportHeight / 2.0)) / zoomFactor;
  }

  /**
   * Converts a world circuit X coordinate to screen pixel coordinate.
   */
  public double worldToScreenX(double worldX) {
    return (worldX * zoomFactor) + panX + (viewportWidth / 2.0);
  }

  /**
   * Converts a world circuit Y coordinate to screen pixel coordinate.
   */
  public double worldToScreenY(double worldY) {
    return (worldY * zoomFactor) + panY + (viewportHeight / 2.0);
  }

  /**
   * Computes the visible bounding box in schematic World Coordinates.
   * Crucial for O(1) viewport culling in Canvas rendering.
   */
  public Bounds getViewportWorldBounds() {
    int left = (int) Math.floor(screenToWorldX(0));
    int top = (int) Math.floor(screenToWorldY(0));
    int right = (int) Math.ceil(screenToWorldX(viewportWidth));
    int bottom = (int) Math.ceil(screenToWorldY(viewportHeight));
    return Bounds.create(left, top, Math.max(1, right - left), Math.max(1, bottom - top));
  }

  /**
   * Checks whether a given world bounding box intersects the current viewport.
   */
  public boolean intersectsViewport(Bounds worldBounds) {
    if (worldBounds == null) return false;
    final var vp = getViewportWorldBounds();
    return !(worldBounds.getX() + worldBounds.getWidth() < vp.getX()
        || worldBounds.getX() > vp.getX() + vp.getWidth()
        || worldBounds.getY() + worldBounds.getHeight() < vp.getY()
        || worldBounds.getY() > vp.getY() + vp.getHeight());
  }
}
