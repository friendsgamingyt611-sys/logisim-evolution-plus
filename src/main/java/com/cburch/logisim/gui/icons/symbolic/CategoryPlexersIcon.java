package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

/**
 * Modern vector icon for Plexers library: trapezoid wide on left, narrow on right, with 2 inputs and 1 output.
 */
public class CategoryPlexersIcon extends SymbolicIcon {

  public CategoryPlexersIcon() {
    super(20);
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color) {
    g2.setStroke(createStroke(1.75f));
    float s = size / 20.0f;

    // Two input leads on the wide left side
    g2.draw(new Line2D.Float(2.0f * s, 7.0f * s, 5.5f * s, 7.0f * s));
    g2.draw(new Line2D.Float(2.0f * s, 13.0f * s, 5.5f * s, 13.0f * s));

    // Trapezoid body
    Path2D.Float trap = new Path2D.Float();
    trap.moveTo(5.5f * s, 3.5f * s);
    trap.lineTo(14.5f * s, 6.5f * s);
    trap.lineTo(14.5f * s, 13.5f * s);
    trap.lineTo(5.5f * s, 16.5f * s);
    trap.closePath();
    g2.draw(trap);

    // One output lead on the narrow right side
    g2.draw(new Line2D.Float(14.5f * s, 10.0f * s, 18.0f * s, 10.0f * s));
  }
}
