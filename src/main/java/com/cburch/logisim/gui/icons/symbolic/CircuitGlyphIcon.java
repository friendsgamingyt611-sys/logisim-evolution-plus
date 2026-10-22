package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Modern vector icon for Logisim Circuits: signature IC subcircuit block with pins.
 */
public class CircuitGlyphIcon extends SymbolicIcon {

  private boolean isCurrentCircuit;

  public CircuitGlyphIcon(boolean isCurrentCircuit) {
    super(16);
    this.isCurrentCircuit = isCurrentCircuit;
  }

  public void setCurrentCircuit(boolean current) {
    this.isCurrentCircuit = current;
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color) {
    g2.setStroke(createStroke(1.6f));
    float s = size;

    int x = (int) (s * 0.24f);
    int y = (int) (s * 0.20f);
    int w = (int) (s * 0.52f);
    int h = (int) (s * 0.60f);

    if (isCurrentCircuit) {
      // Subtle filled body for current circuit
      g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
      g2.fillRoundRect(x, y, w, h, (int) (s * 0.12f), (int) (s * 0.12f));
      g2.setColor(color);
    }

    // Outer chip box
    g2.drawRoundRect(x, y, w, h, (int) (s * 0.12f), (int) (s * 0.12f));

    // Internal logic gate / subcircuit symbol hint
    g2.drawLine((int) (s * 0.40f), (int) (s * 0.38f), (int) (s * 0.60f), (int) (s * 0.50f));
    g2.drawLine((int) (s * 0.40f), (int) (s * 0.62f), (int) (s * 0.60f), (int) (s * 0.50f));

    // Input/output connector pins on edges
    int[] pinsY = {(int) (s * 0.36f), (int) (s * 0.64f)};
    int pinLen = (int) (s * 0.10f);
    for (int py : pinsY) {
      g2.drawLine(x - pinLen, py, x, py);
      g2.drawLine(x + w, py, x + w + pinLen, py);
    }
  }
}
