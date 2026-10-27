package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;

/**
 * Trademark Add HDL/VHDL Entity icon: classic stacked VH/DL lettering with modern gradient styling.
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
    boolean isEnabled = (c == null || c.isEnabled());
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    // Trademark green gradient (Light: #28B463 to #196F3D, Dark: #76F0A4 to #42DC80)
    boolean isDark = com.cburch.logisim.prefs.AppPreferences.isDarkTheme();
    Color topColor = isDark ? new Color(0x76, 0xF0, 0xA4) : new Color(0x28, 0xB4, 0x63);
    Color botColor = isDark ? new Color(0x42, 0xDC, 0x80) : new Color(0x19, 0x6F, 0x3D);

    if (!isEnabled) {
      topColor = toDisabled(topColor);
      botColor = toDisabled(botColor);
    }

    float fontSize = (float) size / 1.75f;
    Font font = new Font(Font.SANS_SERIF, Font.BOLD, Math.round(fontSize));
    var frc = g2.getFontRenderContext();

    var l1 = new TextLayout("VH", font, frc);
    var l2 = new TextLayout("DL", font, frc);

    double left1 = (size / 2.0) - l1.getBounds().getCenterX();
    double top1 = (size * 0.28) - l1.getBounds().getCenterY();

    double left2 = (size / 2.0) - l2.getBounds().getCenterX();
    double top2 = (size * 0.72) - l2.getBounds().getCenterY();

    var shape1 = AffineTransform.getTranslateInstance(left1, top1).createTransformedShape(l1.getOutline(null));
    var shape2 = AffineTransform.getTranslateInstance(left2, top2).createTransformedShape(l2.getOutline(null));

    g2.setPaint(new GradientPaint(0, (float) (size * 0.1), topColor, 0, (float) (size * 0.9), botColor));
    g2.fill(shape1);
    g2.fill(shape2);
  }
}
