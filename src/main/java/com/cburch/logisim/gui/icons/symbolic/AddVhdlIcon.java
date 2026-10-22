package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern vector VHDL Entity icon with chip package and clean label.
 */
public class AddVhdlIcon extends SymbolicIcon {

  public AddVhdlIcon() {
    super(16);
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color) {
    float s = size;
    g2.setStroke(createStroke(1.2f));

    // Chip boundary
    float pad = s * 0.15f;
    float w = s - pad * 2;
    float h = s - pad * 2;
    g2.draw(new RoundRectangle2D.Float(pad, pad, w, h, s * 0.15f, s * 0.15f));

    // Pins on left and right
    float pLen = s * 0.12f;
    g2.drawLine((int) 0, (int) (s * 0.35f), (int) pad, (int) (s * 0.35f));
    g2.drawLine((int) 0, (int) (s * 0.65f), (int) pad, (int) (s * 0.65f));
    g2.drawLine((int) (s - pad), (int) (s * 0.35f), (int) s, (int) (s * 0.35f));
    g2.drawLine((int) (s - pad), (int) (s * 0.65f), (int) s, (int) (s * 0.65f));

    // Text "V" in center
    Font font = g2.getFont().deriveFont(Font.BOLD, scale(7.5f));
    g2.setFont(font);
    g2.drawString("HDL", pad + scale(1.5f), pad + h - scale(2.0f));
  }
}
