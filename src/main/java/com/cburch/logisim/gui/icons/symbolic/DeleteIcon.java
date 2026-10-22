package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Modern vector Close / Delete cross icon.
 */
public class DeleteIcon extends SymbolicIcon {

  public DeleteIcon() {
    super(16);
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color) {
    g2.setStroke(createStroke(1.8f));
    float s = size;
    float p1 = s * 0.28f;
    float p2 = s * 0.72f;

    g2.drawLine((int) p1, (int) p1, (int) p2, (int) p2);
    g2.drawLine((int) p2, (int) p1, (int) p1, (int) p2);
  }
}
