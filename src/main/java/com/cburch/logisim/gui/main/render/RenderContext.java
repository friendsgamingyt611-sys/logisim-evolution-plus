package com.cburch.logisim.gui.main.render;

import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.gui.main.Camera2D;

/**
 * Immutable per-frame render context snapshot.
 * Passed through painter hierarchies to ensure thread isolation and consistent state.
 */
public record RenderContext(
    double panX,
    double panY,
    double zoomFactor,
    int viewportWidth,
    int viewportHeight,
    double deviceScale,
    QualityTier qualityTier,
    Bounds worldFrustum,
    DetailPolicy policy
) {
  public static RenderContext of(Camera2D camera, double deviceScale, QualityTier tier) {
    final double z = (camera != null) ? camera.getZoomFactor() : 1.0;
    final double px = (camera != null) ? camera.getPanX() : 0.0;
    final double py = (camera != null) ? camera.getPanY() : 0.0;
    final int vw = (camera != null) ? camera.getViewportWidth() : 1920;
    final int vh = (camera != null) ? camera.getViewportHeight() : 1080;
    final Bounds vp = (camera != null) ? camera.getViewportWorldBounds() : Bounds.EMPTY_BOUNDS;
    final DetailPolicy pol = new DetailPolicy(z, deviceScale, tier);
    return new RenderContext(px, py, z, vw, vh, deviceScale, tier, vp, pol);
  }

  public boolean intersectsViewport(Bounds bds) {
    if (bds == null || worldFrustum == null) return false;
    return !(bds.getX() + bds.getWidth() < worldFrustum.getX()
        || bds.getX() > worldFrustum.getX() + worldFrustum.getWidth()
        || bds.getY() + bds.getHeight() < worldFrustum.getY()
        || bds.getY() > worldFrustum.getY() + worldFrustum.getHeight());
  }
}
