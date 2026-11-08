package com.cburch.logisim.gui.main.render;

import com.cburch.logisim.data.Bounds;

/**
 * Screen-Space Continuous Level of Detail (LOD) Policy.
 * Evaluates visual complexity based on projected physical pixel footprint
 * on screen, completely decoupling LOD decisions from arbitrary camera zoom factors.
 */
public final class DetailPolicy {

  public enum SymbolTier {
    /** Screen area <= 16px^2: Fast bounding block fill, zero vector/arc/text overhead */
    MICRO_BOX,
    /** Screen area <= 256px^2: Simplified outline, suppressed pin labels and state icons */
    SIMPLIFIED,
    /** Full vector geometry and detailed annotations */
    FULL
  }

  private final double zoom;
  private final double deviceScale;
  private final QualityTier tier;

  public DetailPolicy(double zoom, double deviceScale, QualityTier tier) {
    this.zoom = zoom > 0 ? zoom : 1.0;
    this.deviceScale = deviceScale > 0 ? deviceScale : 1.0;
    this.tier = tier != null ? tier : QualityTier.SETTLED;
  }

  public SymbolTier getSymbolTier(Bounds worldBounds) {
    if (worldBounds == null) return SymbolTier.FULL;
    if (tier == QualityTier.EXACT) return SymbolTier.FULL;

    final double screenWidth = worldBounds.getWidth() * zoom * deviceScale;
    final double screenHeight = worldBounds.getHeight() * zoom * deviceScale;
    final double screenArea = screenWidth * screenHeight;

    if (screenArea <= 16.0) {
      return SymbolTier.MICRO_BOX;
    } else if (screenArea <= 256.0) {
      return SymbolTier.SIMPLIFIED;
    }
    return SymbolTier.FULL;
  }

  public boolean isTextVisible(double fontWorldPt) {
    if (tier == QualityTier.EXACT) return true;
    final double screenPx = fontWorldPt * zoom * deviceScale;
    return screenPx >= 8.5;
  }

  public boolean isMinorGridVisible(double screenStep) {
    if (tier == QualityTier.EXACT) return true;
    return screenStep >= 8.0 && (zoom * deviceScale) >= 0.15;
  }

  public boolean isWireValuesVisible() {
    return tier != QualityTier.INTERACTIVE;
  }

  public QualityTier getQualityTier() {
    return tier;
  }

  public double getZoom() {
    return zoom;
  }

  public double getDeviceScale() {
    return deviceScale;
  }
}
