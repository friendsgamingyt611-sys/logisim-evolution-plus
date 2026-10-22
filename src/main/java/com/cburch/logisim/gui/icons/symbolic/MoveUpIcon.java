package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

/**
 * Modern vector Up Arrow icon.
 */
public class MoveUpIcon extends SymbolicIcon {

  public MoveUpIcon() {
    super(16);
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color) {
    g2.setStroke(createStroke(1.8f));
    float s = size;

    // Upward chevron
    Path2D.Float chevron = new Path2D.Float();
    chevron.moveTo(s * 0.22f, s * 0.58f);
    chevron.lineTo(s * 0.50f, s * 0.30f);
    chevron.lineTo(s * 0.78f, s * 0.58f);
    g2.draw(chevron);

    // Stem
    g2.drawLine((int) (s * 0.50f), (int) (s * 0.32f), (int) (s * 0.50f), (int) (s * 0.75f));
  }
}
