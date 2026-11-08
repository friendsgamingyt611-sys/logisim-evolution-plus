package com.cburch.logisim.gui.main.render;

/**
 * Quality levels for canvas rendering.
 * INTERACTIVE: Moving (panning/zooming), optimized for ultra-low latency.
 * SETTLED: Resting (debounced idle), crisp progressive vector rendering.
 * EXACT: Export/Print/Verification, full uncompromised fidelity.
 */
public enum QualityTier {
  INTERACTIVE,
  SETTLED,
  EXACT
}
