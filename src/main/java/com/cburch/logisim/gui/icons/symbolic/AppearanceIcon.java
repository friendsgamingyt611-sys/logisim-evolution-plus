package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern vector Circuit Appearance / Layout icon.
 */
public class AppearanceIcon extends SymbolicIcon {

  public AppearanceIcon() {
    super(16);
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color) {
    g2.setStroke(createStroke(1.4f));
    float s = size;
    float pad = s * 0.22f;
    float w = s - pad * 2;
    float h = s - pad * 2;

    // Outer chip rectangle
    g2.draw(new RoundRectangle2D.Float(pad, pad, w, h, s * 0.12f, s * 0.12f));

    // Pins
    g2.drawLine((int) 0, (int) (s * 0.35f), (int) pad, (int) (s * 0.35f));
    g2.drawLine((int) 0, (int) (s * 0.65f), (int) pad, (int) (s * 0.65f));
    g2.drawLine((int) (s - pad), (int) (s * 0.35f), (int) s, (int) (s * 0.35f));
    g2.drawLine((int) (s - pad), (int) (s * 0.65f), (int) s, (int) (s * 0.65f));

    // Notch at top
    g2.drawArc((int) (s * 0.40f), (int) (pad - scale(1.5f)), (int) (s * 0.20f), (int) scale(3f), 0, -180);
  }
}
