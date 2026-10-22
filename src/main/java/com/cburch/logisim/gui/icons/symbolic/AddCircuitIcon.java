package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Modern vector Plus icon for adding a new circuit.
 */
public class AddCircuitIcon extends SymbolicIcon {

  public AddCircuitIcon() {
    super(16);
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
