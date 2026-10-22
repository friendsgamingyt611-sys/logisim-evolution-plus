/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.fpga.gui;

import static com.cburch.logisim.fpga.Strings.S;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class HdlColorRenderer extends JLabel implements TableCellRenderer {
  public static final String NO_SUPPORT_STRING = "HDL_NOT_SUPPORTED";
  public static final String SUPPORT_STRING = "HDL_SUPPORTED";
  public static final String UNKNOWN_STRING = "HDL_UNKNOWN";
  public static final String REQUIRED_FIELD_STRING = ">_HDL_REQUIRED_FIELD_<";

  protected static final int LABEL = 0;
  protected static final int VALUE = 1;

  private static final Color SUCCESS_TEXT = new Color(51, 209, 122);
  private static final Color SUCCESS_BG = new Color(51, 209, 122, 45);
  private static final Color DANGER_TEXT = new Color(246, 97, 81);
  private static final Color DANGER_BG = new Color(246, 97, 81, 45);

  private boolean isPill = false;
  private Color pillBg = null;
  private Color pillFg = null;

  public HdlColorRenderer() {
    setOpaque(true);
  }

  @Override
  public Component getTableCellRendererComponent(
      JTable table, Object infoObj, boolean isSelected, boolean hasFocus, int rowIdx, int columnIdx) {
    final var value = (infoObj != null) ? (String) infoObj : "???";

    isPill = false;
    pillBg = null;
    pillFg = null;

    Color tableBg = isSelected
        ? UIManager.getColor("Table.selectionBackground")
        : (rowIdx % 2 == 1
            ? (UIManager.getColor("Table.alternateRowColor") != null
                ? UIManager.getColor("Table.alternateRowColor")
                : UIManager.getColor("Table.background"))
            : UIManager.getColor("Table.background"));
    setBackground(tableBg);

    setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

    if (rowIdx == 0) {
      return renderFpgaSupportRow(columnIdx, value, isSelected);
    }

    if (columnIdx == LABEL) {
      Color muted = UIManager.getColor("Label.disabledForeground");
      if (muted == null) muted = new Color(154, 153, 150);
      setForeground(isSelected ? UIManager.getColor("Table.selectionForeground") : muted);
      setText(value);
      setHorizontalAlignment(JLabel.LEFT);
      return this;
    }

    if (value.equals(REQUIRED_FIELD_STRING)) {
      isPill = true;
      pillBg = new Color(229, 165, 10, 45);
      pillFg = new Color(229, 165, 10);
      setText(S.get("FPGAHdlRequired"));
      setHorizontalAlignment(JLabel.CENTER);
    } else if ((value.length() == 7 || value.length() == 9) && value.indexOf('#') == 0) {
      try {
        final var red = Integer.valueOf(value.substring(1, 3), 16);
        final var green = Integer.valueOf(value.substring(3, 5), 16);
        final var blue = Integer.valueOf(value.substring(5, 7), 16);
        final var alpha = value.length() == 7 ? 255 : Integer.valueOf(value.substring(7, 9), 16);
        final var col = new Color(red, green, blue, alpha);
        isPill = true;
        pillBg = col;
        pillFg = (red * 299 + green * 587 + blue * 114) / 1000 > 128 ? Color.BLACK : Color.WHITE;
        setText(value.toUpperCase());
        setHorizontalAlignment(JLabel.CENTER);
      } catch (Exception ex) {
        setText(value);
        setHorizontalAlignment(JLabel.LEFT);
      }
    } else {
      setForeground(isSelected ? UIManager.getColor("Table.selectionForeground") : UIManager.getColor("Table.foreground"));
      setText(value);
      setHorizontalAlignment(JLabel.LEFT);
    }

    return this;
  }

  protected Component renderFpgaSupportRow(int columnIdx, String value, boolean isSelected) {
    if (columnIdx == LABEL) {
      Color muted = UIManager.getColor("Label.disabledForeground");
      if (muted == null) muted = new Color(154, 153, 150);
      setForeground(isSelected ? UIManager.getColor("Table.selectionForeground") : muted);
      setText(value);
      setHorizontalAlignment(JLabel.LEFT);
      return this;
    }

    isPill = true;
    if (value.equals(SUPPORT_STRING)) {
      pillBg = SUCCESS_BG;
      pillFg = SUCCESS_TEXT;
      setText(S.get("FPGASupported"));
    } else if (value.equals(NO_SUPPORT_STRING)) {
      pillBg = DANGER_BG;
      pillFg = DANGER_TEXT;
      setText(S.get("FPGANotSupported"));
    } else {
      pillBg = new Color(128, 128, 128, 35);
      pillFg = UIManager.getColor("Label.disabledForeground");
      setText(S.get("FPGAUnknown"));
    }
    setHorizontalAlignment(JLabel.CENTER);
    return this;
  }

  @Override
  protected void paintComponent(Graphics g) {
    if (isPill && pillBg != null) {
      final var g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      g2.setColor(getBackground());
      g2.fillRect(0, 0, getWidth(), getHeight());

      int pillPadX = 8;
      int pillPadY = 4;
      int pillW = getWidth() - 2 * pillPadX;
      int pillH = getHeight() - 2 * pillPadY;
      int arc = pillH;

      g2.setColor(pillBg);
      g2.fillRoundRect(pillPadX, pillPadY, pillW, pillH, arc, arc);

      g2.setColor(pillFg);
      g2.setStroke(new BasicStroke(1f));
      g2.drawRoundRect(pillPadX, pillPadY, pillW, pillH, arc, arc);

      setForeground(pillFg);
      super.paintComponent(g2);
      g2.dispose();
      return;
    }
    super.paintComponent(g);
  }
}
