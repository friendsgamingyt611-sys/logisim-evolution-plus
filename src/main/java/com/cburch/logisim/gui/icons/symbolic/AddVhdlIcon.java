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

    // Clean vector 'H' (Hardware Entity) in chip center
    float cx = s * 0.5f;
    float hw = s * 0.14f;
    float hh = s * 0.18f;
    g2.setStroke(createStroke(1.3f));
    // left leg
    g2.drawLine((int) (cx - hw), (int) (s * 0.5f - hh), (int) (cx - hw), (int) (s * 0.5f + hh));
    // right leg
    g2.drawLine((int) (cx + hw), (int) (s * 0.5f - hh), (int) (cx + hw), (int) (s * 0.5f + hh));
    // crossbar
    g2.drawLine((int) (cx - hw), (int) (s * 0.5f), (int) (cx + hw), (int) (s * 0.5f));
  }
}
