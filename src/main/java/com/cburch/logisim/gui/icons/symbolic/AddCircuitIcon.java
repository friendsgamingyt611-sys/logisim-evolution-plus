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
    g2.setStroke(createStroke(2.0f));
    float s = size;
    float pad = s * 0.22f;
    float mid = s * 0.50f;

    // Horizontal bar
    g2.drawLine((int) pad, (int) mid, (int) (s - pad), (int) mid);
    // Vertical bar
    g2.drawLine((int) mid, (int) pad, (int) mid, (int) (s - pad));
  }
}
