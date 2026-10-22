package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

/**
 * Modern vector icon for TTL / Chip library: DIP chip outline with a notch and 3 pins on each side.
 */
public class CategoryChipIcon extends SymbolicIcon {

  public CategoryChipIcon() {
    super(20);
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color) {
    g2.setStroke(createStroke(1.75f));
    float s = size / 20.0f;

    float cx = 5.5f * s;
    float cy = 3.5f * s;
    float cw = 9.0f * s;
    float ch = 13.0f * s;

    // DIP body with top notch
    Path2D.Float body = new Path2D.Float();
    body.moveTo(cx, cy + 1.5f * s);
    body.quadTo(cx, cy, cx + 1.5f * s, cy);
    body.lineTo(cx + 3.0f * s, cy);
    // Top notch
    body.curveTo(cx + 3.2f * s, cy + 2.0f * s, cx + 5.8f * s, cy + 2.0f * s, cx + 6.0f * s, cy);
    body.lineTo(cx + cw - 1.5f * s, cy);
    body.quadTo(cx + cw, cy, cx + cw, cy + 1.5f * s);
    body.lineTo(cx + cw, cy + ch - 1.5f * s);
    body.quadTo(cx + cw, cy + ch, cx + cw - 1.5f * s, cy + ch);
    body.lineTo(cx + 1.5f * s, cy + ch);
    body.quadTo(cx, cy + ch, cx, cy + ch - 1.5f * s);
    body.closePath();
    g2.draw(body);

    // 3 pins on each side
    float[] pinYs = {6.0f, 10.0f, 14.0f};
    for (float py : pinYs) {
      g2.draw(new Line2D.Float(2.0f * s, py * s, cx, py * s));
      g2.draw(new Line2D.Float(cx + cw, py * s, 18.0f * s, py * s));
    }
  }
}
