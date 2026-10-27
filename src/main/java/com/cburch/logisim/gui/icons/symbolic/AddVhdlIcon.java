package com.cburch.logisim.gui.icons.symbolic;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Modern vector VHDL Entity icon with crisp two-line stacked VH/DL typography.
 */
public class AddVhdlIcon extends SymbolicIcon {

  private static final Color SEMANTIC_GREEN = new Color(0x2E, 0xD1, 0x73);

  public AddVhdlIcon() {
    super(16);
  }

  @Override
  public Color getSemanticColor() {
    return SEMANTIC_GREEN;
  }

  @Override
  protected void paintSymbolic(Graphics2D g2, int size, Color color) {
    float s = size;
    int fontSize = (int) Math.round(s * 0.44f);
    Font font = new Font(Font.SANS_SERIF, Font.BOLD, fontSize);
    g2.setFont(font);
    FontMetrics fm = g2.getFontMetrics();

    String top = "VH";
    String bot = "DL";

    int wTop = fm.stringWidth(top);
    int wBot = fm.stringWidth(bot);

    int xTop = Math.max(0, (int) Math.round((s - wTop) / 2.0));
    int xBot = Math.max(0, (int) Math.round((s - wBot) / 2.0));

    int yTop = (int) Math.round(s * 0.46f);
    int yBot = (int) Math.round(s * 0.92f);

    g2.drawString(top, xTop, yTop);
    g2.drawString(bot, xBot, yBot);
  }
}
