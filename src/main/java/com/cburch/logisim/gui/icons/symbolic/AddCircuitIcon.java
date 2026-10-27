package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern monochrome vector icon for Add Circuit: IC subcircuit box with a plus badge at lower-right.
 */
public class AddCircuitIcon extends SymbolicIcon {

  public AddCircuitIcon() {
    this(20);
  }

  public AddCircuitIcon(int baseSize) {
    super(baseSize);
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color, Component c) {
    float s = size;
    g2.setStroke(createStroke(1.75f));
    g2.setColor(color);

    // Box bounds
    float x = s * 0.20f;
    float y = s * 0.16f;
    float w = s * 0.54f;
    float h = s * 0.58f;
    float arc = s * 0.12f;

    // Subcircuit body
    g2.draw(new RoundRectangle2D.Float(x, y, w, h, arc, arc));

    // Internal logic gate chevron
    Path2D.Float chevron = new Path2D.Float();
    chevron.moveTo(x + w * 0.32f, y + h * 0.30f);
    chevron.lineTo(x + w * 0.68f, y + h * 0.50f);
    chevron.lineTo(x + w * 0.32f, y + h * 0.70f);
    g2.draw(chevron);

    // Left input pins
    float pinLen = s * 0.12f;
    g2.drawLine((int) (x - pinLen), (int) (y + h * 0.35f), (int) x, (int) (y + h * 0.35f));
    g2.drawLine((int) (x - pinLen), (int) (y + h * 0.65f), (int) x, (int) (y + h * 0.65f));

    // Right output pin (top only, leaving lower right for badge)
    g2.drawLine((int) (x + w), (int) (y + h * 0.35f), (int) (x + w + pinLen), (int) (y + h * 0.35f));

    // Lower right plus badge with 2px knockout gap
    float badgeCx = s * 0.74f;
    float badgeCy = s * 0.74f;
    float badgeR = s * 0.22f;
    float knockoutGap = 2.0f;
    drawKnockoutBadge(g2, badgeCx, badgeCy, badgeR, knockoutGap, color, c);
  }
}
