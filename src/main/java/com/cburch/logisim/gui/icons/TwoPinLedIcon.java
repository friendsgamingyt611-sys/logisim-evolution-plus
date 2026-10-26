/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.icons;

import com.cburch.logisim.prefs.AppPreferences;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class TwoPinLedIcon extends BaseIcon {

  public TwoPinLedIcon() {
    super();
  }

  @Override
  protected void paintIcon(Graphics2D g2) {
    int xy = AppPreferences.getScaled(3);
    int wh = AppPreferences.getScaled(10);
    var currColor = g2.getColor();
    g2.setColor(Color.RED);
    g2.fillOval(xy, xy, wh, wh);
    g2.setColor(currColor);
    g2.setStroke(new BasicStroke(AppPreferences.getScaled(1.5f)));
    g2.drawOval(xy, xy, wh, wh);
    // Two pin leads
    g2.drawLine(0, AppPreferences.getScaled(8), xy, AppPreferences.getScaled(8));
    g2.drawLine(xy + wh, AppPreferences.getScaled(8), AppPreferences.getScaled(16), AppPreferences.getScaled(8));
  }
}
