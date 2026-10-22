package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern vector icon for BFH library: chip with a larger inner core block.
 */
public class CategoryBfhIcon extends SymbolicIcon {

  public CategoryBfhIcon() {
    super(20);
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color) {
    g2.setStroke(createStroke(1.75f));
    float s = size / 20.0f;

    // Outer chip body
    float cx = 4.5f * s;
    float cy = 3.5f * s;
    float cw = 11.0f * s;
    float ch = 13.0f * s;
    g2.draw(new RoundRectangle2D.Float(cx, cy, cw, ch, 3.0f * s, 3.0f * s));

    // Inner block
    g2.draw(new RoundRectangle2D.Float(cx + 2.5f * s, cy + 3.0f * s, cw - 5.0f * s, ch - 6.0f * s, 1.5f * s, 1.5f * s));

    // Pins
    g2.draw(new Line2D.Float(1.5f * s, 6.5f * s, cx, 6.5f * s));
    g2.draw(new Line2D.Float(1.5f * s, 13.5f * s, cx, 13.5f * s));
    g2.draw(new Line2D.Float(cx + cw, 6.5f * s, 18.5f * s, 6.5f * s));
    g2.draw(new Line2D.Float(cx + cw, 13.5f * s, 18.5f * s, 13.5f * s));
  }
}
