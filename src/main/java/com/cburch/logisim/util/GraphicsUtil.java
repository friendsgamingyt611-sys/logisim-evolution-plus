/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.util;

import com.cburch.logisim.circuit.RadixOption;
import com.cburch.logisim.data.Value;

import com.cburch.logisim.comp.Palette;
import com.cburch.logisim.prefs.AppPreferences;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public final class GraphicsUtil {

  public static final int H_LEFT = -1;

  public static final int H_CENTER = 0;

  public static final int H_RIGHT = 1;
  public static final int V_TOP = -1;

  public static final int V_CENTER = 0;
  public static final int V_BASELINE = 1;
  public static final int V_BOTTOM = 2;

  public static final int V_CENTER_OVERALL = 3;

  public static final int CONTROL_WIDTH = 2;
  public static final int NEGATED_WIDTH = 2;
  public static final int DATA_SINGLE_WIDTH = 3;
  public static final int DATA_MULTI_WIDTH = 4;

  private GraphicsUtil() {
    throw new IllegalStateException("Utility class. No instantiation allowed.");
  }

  public static void drawArrow(Graphics g, int x0, int y0, int x1, int y1, int headLength, int headAngle) {
    final var offs = headAngle * Math.PI / 180.0;
    final var angle = Math.atan2(y0 - y1, x0 - x1);
    int[] xs = {
      x1 + (int) (headLength * Math.cos(angle + offs)),
      x1,
      x1 + (int) (headLength * Math.cos(angle - offs))
    };
    int[] ys = {
      y1 + (int) (headLength * Math.sin(angle + offs)),
      y1,
      y1 + (int) (headLength * Math.sin(angle - offs))
    };
    g.drawLine(x0, y0, x1, y1);
    g.drawPolyline(xs, ys, 3);
  }

  public static void drawArrow2(Graphics g, int x0, int y0, int x1, int y1, int x2, int y2) {
    int[] xs = {x0, x1, x2};
    int[] ys = {y0, y1, y2};
    GraphicsUtil.switchToWidth(g, 7);
    g.drawPolyline(xs, ys, 3);
    final var oldColor = g.getColor();
    g.setColor(Color.WHITE);
    GraphicsUtil.switchToWidth(g, 3);
    g.drawPolyline(xs, ys, 3);
    g.setColor(oldColor);
    GraphicsUtil.switchToWidth(g, 1);
  }

  public static void drawCenteredArc(Graphics g, int x, int y, int r, int start, int dist) {
    g.drawArc(x - r, y - r, 2 * r, 2 * r, start, dist);
  }

  public static void drawCenteredText(Graphics g, String text, int x, int y) {
    drawText(g, text, x, y, H_CENTER, V_CENTER);
  }

  public static void drawCenteredText(Graphics g, Font font, String text, int x, int y, Color fg, Color bg) {
    drawText(g, text, x, y, H_CENTER, V_CENTER);
  }

  public static void drawCenteredValue(Graphics2D gfx, Value value, RadixOption radix, int x, int y) {
    final var valueString = radix.toString(value);
    final var radixIdentifier = radix.getIndexChar();
    final var fontMetrics = gfx.getFontMetrics();
    final var valueBounds = fontMetrics.getStringBounds(valueString, gfx);
    gfx.drawString(valueString, x - (int) (valueBounds.getWidth() / 2), y + (int) (valueBounds.getHeight() / 2));
    gfx.scale(0.7, 0.7);
    final var radixBounds = fontMetrics.getStringBounds(radixIdentifier, gfx);
    final var currentColor = gfx.getColor();
    gfx.setColor(Color.BLUE);
    final var radixXpos = x + (valueBounds.getWidth() / 2) + 1;
    final var radixYpos = y + valueBounds.getHeight() - (radixBounds.getHeight() / 3);
    gfx.drawString(radixIdentifier, (int) (radixXpos / 0.7), (int) (radixYpos / 0.7));
    gfx.scale(1 / 0.7, 1 / 0.7);
    gfx.setColor(currentColor);
  }

  public static void drawCenteredColoredText(Graphics g, String text, Color fg, Color bg, int x, int y) {
    drawText(g, text, x, y, H_CENTER, V_CENTER, fg, bg);
  }

  public static Rectangle getTextCursor(Graphics g, String text, int x, int y, int pos, int halign, int valign) {
    final var r = getTextBounds(g, text, x, y, halign, valign);
    if (pos > 0 && g != null) {
      r.x += g.getFontMetrics().stringWidth(text.substring(0, pos));
    }
    r.width = 1;
    return r;
  }

  public static int getTextPosition(Graphics g, String text, int x, int y, int halign, int valign) {
    final var r = getTextBounds(g, text, 0, 0, halign, valign);
    x -= r.x;
    var last = 0;
    final var fm = (g != null) ? g.getFontMetrics() : null;
    for (var i = 0; i < text.length(); i++) {
      final var cur = (fm != null) ? fm.stringWidth(text.substring(0, i + 1)) : 0;
      if (x <= (last + cur) / 2) {
        return i;
      }
      last = cur;
    }
    return text.length();
  }

  public static void drawText(
      Graphics g,
      Font font,
      String text,
      int x,
      int y,
      int halign,
      int valign,
      Color fg,
      Color bg) {
    final var oldfont = g.getFont();
    if (font != null) g.setFont(font);
    drawText(g, text, x, y, halign, valign, fg, bg);
    if (font != null) g.setFont(oldfont);
  }

  public static void drawText(
      Graphics g, Font font, String text, int x, int y, int halign, int valign) {
    final var oldfont = g.getFont();
    if (font != null) g.setFont(font);
    drawText(g, text, x, y, halign, valign);
    if (font != null) g.setFont(oldfont);
  }

  public static void drawText(Graphics g, String text, int x, int y, int halign, int valign) {
    if (text == null || text.length() == 0 || g == null) return;
    if (g instanceof Graphics2D g2d) {
      final double sy = Math.abs(g2d.getTransform().getScaleY());
      if (g2d.getFont().getSize2D() * sy < 8.0) {
        return; // Semantic Zooming: drop unreadable microscopic text (< 8.0px on screen)
      }
    }
    final var fm = g.getFontMetrics();
    final int width = fm.stringWidth(text);
    final int ascent = fm.getAscent();
    final int height = fm.getHeight();
    int bx = x;
    int by = y;
    switch (halign) {
      case H_CENTER -> bx -= (width / 2);
      case H_RIGHT -> bx -= width;
      default -> {}
    }
    switch (valign) {
      case V_TOP -> by += 0;
      case V_CENTER -> by -= (height / 2);
      case V_BASELINE -> by -= ascent;
      case V_BOTTOM -> by -= height;
      default -> {}
    }
    g.drawString(text, bx, by + ascent);
  }

  public static void drawText(Graphics g, String text, int x, int y, int halign, int valign, Color fg, Color bg) {
    if (text == null || text.length() == 0 || g == null) return;
    if (g instanceof Graphics2D g2d) {
      final double sy = Math.abs(g2d.getTransform().getScaleY());
      if (g2d.getFont().getSize2D() * sy < 8.0) {
        return; // Semantic Zooming: drop unreadable microscopic text (< 8.0px on screen)
      }
    }
    final var fm = g.getFontMetrics();
    final int width = fm.stringWidth(text);
    final int ascent = fm.getAscent();
    final int height = fm.getHeight();
    int bx = x;
    int by = y;
    switch (halign) {
      case H_CENTER -> bx -= (width / 2);
      case H_RIGHT -> bx -= width;
      default -> {}
    }
    switch (valign) {
      case V_TOP -> by += 0;
      case V_CENTER -> by -= (height / 2);
      case V_BASELINE -> by -= ascent;
      case V_BOTTOM -> by -= height;
      default -> {}
    }
    if (g instanceof Graphics2D g2d) {
      g2d.setPaint(bg);
      g.fillRect(bx, by, width, height);
      g2d.setPaint(fg);
    }
    g.drawString(text, bx, by + ascent);
  }

  public static void outlineText(Graphics g, String text, int x, int y, Color fg, Color bg) {
    final var g2 = (Graphics2D) g;
    final var glyphVector = g2.getFont().createGlyphVector(g2.getFontRenderContext(), text);
    final var textShape = glyphVector.getOutline();
    final var transform = g2.getTransform();
    g2.translate(x, y);
    g2.setColor(bg);
    g2.draw(textShape);
    g2.setColor(fg);
    g2.fill(textShape);
    g2.setTransform(transform);
  }

  public static Rectangle getTextBounds(Graphics g, Font font, String text, int x, int y, int halign, int valign) {
    if (g == null) return new Rectangle(x, y, 0, 0);
    final var oldfont = g.getFont();
    if (font != null) g.setFont(font);
    final var ret = getTextBounds(g, text, x, y, halign, valign);
    if (font != null) g.setFont(oldfont);
    return ret;
  }

  public static Rectangle getTextBounds(Graphics g, String text, int x, int y, int halign, int valign) {
    if (g == null || text == null) return new Rectangle(x, y, 0, 0);

    final var fm = g.getFontMetrics();
    final var width = fm.stringWidth(text);
    final var ascent = fm.getAscent();
    final var height = fm.getHeight();

    final var ret = new Rectangle(x, y, width, height);
    switch (halign) {
      case H_CENTER -> ret.translate(-(width / 2), 0);
      case H_RIGHT -> ret.translate(-width, 0);
      default -> {
      }
    }
    switch (valign) {
      case V_TOP:
        break;
      case V_CENTER:
        ret.translate(0, -(ascent / 2));
        break;
      case V_CENTER_OVERALL:
        ret.translate(0, -(height / 2));
        break;
      case V_BASELINE:
        ret.translate(0, -ascent);
        break;
      case V_BOTTOM:
        ret.translate(0, -height);
        break;
      default:
    }
    return ret;
  }

  private static final java.util.Map<Integer, BasicStroke> STROKE_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

  public static void switchToWidth(Graphics gfx, int width) {
    switchToWidth(gfx, width, true);
  }

  public static void switchToWidth(Graphics gfx, int width, boolean allowRound) {
    if (gfx instanceof Graphics2D g2d) {
      boolean round = allowRound && AppPreferences.EXPERIMENTAL_ROUND_STROKES.getBoolean() && !Palette.isForceLight();
      int key = (width << 1) | (round ? 1 : 0);
      BasicStroke stroke = STROKE_CACHE.computeIfAbsent(key, k -> {
        boolean isRound = (k & 1) != 0;
        int w = k >> 1;
        return isRound
            ? new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            : new BasicStroke(w);
      });
      g2d.setStroke(stroke);
    }
  }

}
