package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

/**
 * Modern monochrome vector icon for Add HDL: Stage 11 DIP chip outline with a plus badge at lower-right.
 */
public class AddVhdlIcon extends SymbolicIcon {

  public AddVhdlIcon() {
    this(20);
  }

  public AddVhdlIcon(int baseSize) {
    super(baseSize);
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color, Component c) {
    float s = size / 20.0f;
    g2.setStroke(createStroke(1.75f));
    g2.setColor(color);

    float cx = 4.5f * s;
    float cy = 2.5f * s;
    float cw = 9.0f * s;
    float ch = 12.0f * s;

    // DIP body with top notch
    Path2D.Float body = new Path2D.Float();
    body.moveTo(cx, cy + 1.5f * s);
    body.quadTo(cx, cy, cx + 1.5f * s, cy);
    body.lineTo(cx + 3.0f * s, cy);
    // Top notch
    body.curveTo(cx + 3.2f * s, cy + 1.8f * s, cx + 5.8f * s, cy + 1.8f * s, cx + 6.0f * s, cy);
    body.lineTo(cx + cw - 1.5f * s, cy);
    body.quadTo(cx + cw, cy, cx + cw, cy + 1.5f * s);
    body.lineTo(cx + cw, cy + ch - 1.5f * s);
    body.quadTo(cx + cw, cy + ch, cx + cw - 1.5f * s, cy + ch);
    body.lineTo(cx + 1.5f * s, cy + ch);
    body.quadTo(cx, cy + ch, cx, cy + ch - 1.5f * s);
    body.closePath();
    g2.draw(body);

    // Side pins
    g2.draw(new Line2D.Float(1.5f * s, 5.0f * s, cx, 5.0f * s));
    g2.draw(new Line2D.Float(1.5f * s, 8.5f * s, cx, 8.5f * s));
    g2.draw(new Line2D.Float(1.5f * s, 12.0f * s, cx, 12.0f * s));

    g2.draw(new Line2D.Float(cx + cw, 5.0f * s, 16.5f * s, 5.0f * s));
    g2.draw(new Line2D.Float(cx + cw, 8.5f * s, 16.5f * s, 8.5f * s));

    // Lower right plus badge with 2px knockout gap
    float badgeCx = 15.0f * s;
    float badgeCy = 15.0f * s;
    float badgeR = 4.4f * s;
    float knockoutGap = 2.0f;
    drawKnockoutBadge(g2, badgeCx, badgeCy, badgeR, knockoutGap, color, c);
  }
}
