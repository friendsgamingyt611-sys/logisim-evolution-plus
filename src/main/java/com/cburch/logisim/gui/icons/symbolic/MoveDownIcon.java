package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

/**
 * Modern vector Down Arrow icon.
 */
public class MoveDownIcon extends SymbolicIcon {

  public MoveDownIcon() {
    super(16);
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color) {
    g2.setStroke(createStroke(1.8f));
    float s = size;

    // Downward chevron
    Path2D.Float chevron = new Path2D.Float();
    chevron.moveTo(s * 0.22f, s * 0.42f);
    chevron.lineTo(s * 0.50f, s * 0.70f);
    chevron.lineTo(s * 0.78f, s * 0.42f);
    g2.draw(chevron);

    // Stem
    g2.drawLine((int) (s * 0.50f), (int) (s * 0.68f), (int) (s * 0.50f), (int) (s * 0.25f));
  }
}
