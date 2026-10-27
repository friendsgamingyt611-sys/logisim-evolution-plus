package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Classic-clarity vector Circuit Appearance icon with colored pins and pencil overlay.
 */
public class AppearanceIcon extends SymbolicIcon {

  private static final Color PIN_GREEN = new Color(0x2E, 0xD1, 0x73);
  private static final Color PIN_BLUE = new Color(0x35, 0x84, 0xE4);
  private static final Color PENCIL_BODY = new Color(0xF5, 0x9E, 0x0B);
  private static final Color PENCIL_TIP = new Color(0x78, 0x35, 0x0F);

  public AppearanceIcon() {
    super(16);
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color) {
    float s = size;

    // 1. Draw side pins (green on left, blue on right)
    float pad = s * 0.24f;
    g2.setStroke(createStroke(1.8f));

    g2.setColor(PIN_GREEN);
    g2.drawLine((int) (s * 0.04f), (int) (s * 0.35f), (int) pad, (int) (s * 0.35f));
    g2.drawLine((int) (s * 0.04f), (int) (s * 0.65f), (int) pad, (int) (s * 0.65f));

    g2.setColor(PIN_BLUE);
    g2.drawLine((int) (s - pad), (int) (s * 0.35f), (int) (s * 0.96f), (int) (s * 0.35f));
    g2.drawLine((int) (s - pad), (int) (s * 0.65f), (int) (s * 0.96f), (int) (s * 0.65f));

    // 2. Draw chip outline
    g2.setColor(color);
    g2.setStroke(createStroke(1.5f));
    float w = s - pad * 2;
    float h = s - pad * 2;
    g2.draw(new RoundRectangle2D.Float(pad, pad, w, h, s * 0.14f, s * 0.14f));

    // 3. Diagonal amber pencil overlay across the chip
    Path2D.Float pencil = new Path2D.Float();
    pencil.moveTo(s * 0.44f, s * 0.70f);
    pencil.lineTo(s * 0.76f, s * 0.28f);
    pencil.lineTo(s * 0.86f, s * 0.36f);
    pencil.lineTo(s * 0.54f, s * 0.78f);
    pencil.closePath();

    g2.setColor(PENCIL_BODY);
    g2.fill(pencil);

    // Pencil tip
    Path2D.Float tip = new Path2D.Float();
    tip.moveTo(s * 0.44f, s * 0.70f);
    tip.lineTo(s * 0.54f, s * 0.78f);
    tip.lineTo(s * 0.36f, s * 0.86f);
    tip.closePath();

    g2.setColor(PENCIL_TIP);
    g2.fill(tip);
  }
}
