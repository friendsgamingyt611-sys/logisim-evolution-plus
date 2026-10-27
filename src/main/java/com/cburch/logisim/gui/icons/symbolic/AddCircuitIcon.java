package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Modern vector Plus icon for adding a new circuit.
 */
public class AddCircuitIcon extends SymbolicIcon {

  private static final Color SEMANTIC_GREEN = new Color(0x2E, 0xD1, 0x73);

  public AddCircuitIcon() {
    super(16);
  }

  @Override
  public Color getSemanticColor() {
    return SEMANTIC_GREEN;
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color) {
    float s = size;
    float pad = s * 0.16f;
    float thickness = s * 0.28f;
    float mid = s * 0.50f;
    float halfT = thickness / 2.0f;
    float arc = s * 0.08f;

    // Draw bold, solid-filled green cross
    g2.fill(new java.awt.geom.RoundRectangle2D.Float(
        pad, mid - halfT, s - 2 * pad, thickness, arc, arc));
    g2.fill(new java.awt.geom.RoundRectangle2D.Float(
        mid - halfT, pad, thickness, s - 2 * pad, arc, arc));
  }
}
