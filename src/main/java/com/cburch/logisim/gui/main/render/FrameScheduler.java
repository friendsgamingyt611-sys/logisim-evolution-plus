package com.cburch.logisim.gui.main.render;

import com.cburch.logisim.gui.main.Camera2D;
import com.cburch.logisim.gui.main.Canvas;
import java.awt.GraphicsEnvironment;
import javax.swing.Timer;

/**
 * Phase 2.3 Centralized Frame Scheduler.
 * Coordinates input event coalescing, layer invalidation, and debounced settle rendering.
 */
public final class FrameScheduler {

  public static final int DIRTY_NONE = 0;
  public static final int DIRTY_L0 = 1 << 0; // Surface + Grid
  public static final int DIRTY_L1 = 1 << 1; // Static Schematic Cache
  public static final int DIRTY_L2 = 1 << 2; // Dynamic Sim Values
  public static final int DIRTY_L3 = 1 << 3; // Interaction Overlays
  public static final int DIRTY_ALL = DIRTY_L0 | DIRTY_L1 | DIRTY_L2 | DIRTY_L3;

  private final Canvas canvas;
  private final Camera2D camera;
  private int dirtyMask = DIRTY_ALL;

  private double pendingPanX = 0;
  private double pendingPanY = 0;

  private final Timer frameTimer;
  private final Timer settleTimer;
  private volatile boolean isMoving = false;

  public FrameScheduler(Canvas canvas, Camera2D camera) {
    this.canvas = canvas;
    this.camera = camera;

    this.frameTimer = new Timer(16, e -> onFrameTick());
    this.settleTimer = new Timer(100, e -> onSettleTick());
    this.settleTimer.setRepeats(false);

    if (!GraphicsEnvironment.isHeadless()) {
      frameTimer.start();
    }
  }

  public void invalidateLayer(int mask) {
    this.dirtyMask |= mask;
    if (GraphicsEnvironment.isHeadless() || !frameTimer.isRunning()) {
      flush();
    }
  }

  public void postPanDelta(double dx, double dy) {
    // Macro-Zoom Damping: ignore micro-jitters (< 2px) at extreme zoom-out (< 15%)
    if (camera != null && camera.getZoomFactor() < 0.15 && Math.abs(dx) < 2 && Math.abs(dy) < 2) {
      return;
    }

    pendingPanX += dx;
    pendingPanY += dy;
    isMoving = true;
    dirtyMask |= DIRTY_L0;

    settleTimer.restart();

    if (!canvas.isShowing() || GraphicsEnvironment.isHeadless() || !frameTimer.isRunning()) {
      flush();
    }
  }

  public void onFrameTick() {
    flush();
  }

  private void onSettleTick() {
    isMoving = false;
    // When movement settles, request a high-detail refresh on L1
    dirtyMask |= DIRTY_L1;
    canvas.repaint();
  }

  public void flush() {
    boolean panned = false;
    if (pendingPanX != 0 || pendingPanY != 0) {
      if (camera != null) {
        camera.panBy(pendingPanX, pendingPanY);
      }
      pendingPanX = 0;
      pendingPanY = 0;
      panned = true;
    }

    if (panned || dirtyMask != DIRTY_NONE) {
      dirtyMask = DIRTY_NONE;
      canvas.repaint();
    }
  }

  public boolean isMoving() {
    return isMoving || pendingPanX != 0 || pendingPanY != 0;
  }

  public QualityTier currentTier() {
    return isMoving() ? QualityTier.INTERACTIVE : QualityTier.SETTLED;
  }

  public void stop() {
    frameTimer.stop();
    settleTimer.stop();
  }
}
