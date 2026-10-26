/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.std.io.extra;

import static com.cburch.logisim.std.Strings.S;

import com.cburch.logisim.circuit.appear.DynamicElement;
import com.cburch.logisim.circuit.appear.DynamicElementProvider;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.gui.icons.TwoPinLedIcon;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceDataSingleton;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstanceLogger;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.std.io.IoLibrary;
import com.cburch.logisim.tools.key.DirectionConfigurator;
import com.cburch.logisim.util.GraphicsUtil;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

public class TwoPinLed extends InstanceFactory implements DynamicElementProvider {

  public static final String _ID = "2-Pin LED";

  public static class Logger extends InstanceLogger {
    @Override
    public String getLogName(InstanceState state, Object option) {
      return state.getAttributeValue(StdAttr.LABEL);
    }

    @Override
    public BitWidth getBitWidth(InstanceState state, Object option) {
      return BitWidth.ONE;
    }

    @Override
    public Value getLogValue(InstanceState state, Object option) {
      final var data = (InstanceDataSingleton) state.getData();
      if (data == null) return Value.FALSE;
      return data.getValue() == Value.TRUE ? Value.TRUE : Value.FALSE;
    }
  }

  public TwoPinLed() {
    super(_ID, S.getter("twopinLEDComponent"));
    setAttributes(
        new Attribute[] {
          StdAttr.FACING,
          IoLibrary.ATTR_ON_COLOR,
          IoLibrary.ATTR_OFF_COLOR,
          IoLibrary.ATTR_ACTIVE,
          StdAttr.LABEL,
          StdAttr.LABEL_LOC,
          StdAttr.LABEL_FONT,
          StdAttr.LABEL_COLOR,
          StdAttr.LABEL_VISIBILITY
        },
        new Object[] {
          Direction.EAST,
          new Color(240, 0, 0),
          Color.DARK_GRAY,
          Boolean.TRUE,
          "",
          Direction.NORTH,
          StdAttr.DEFAULT_LABEL_FONT,
          StdAttr.DEFAULT_LABEL_COLOR,
          true
        });
    setFacingAttribute(StdAttr.FACING);
    setIcon(new TwoPinLedIcon());
    setKeyConfigurator(new DirectionConfigurator(StdAttr.LABEL_LOC, KeyEvent.ALT_DOWN_MASK));
    setPorts(
        new Port[] {
          new Port(-20, 0, Port.INPUT, 1),
          new Port(0, 0, Port.INPUT, 1)
        });
    setInstanceLogger(Logger.class);
  }

  @Override
  protected void configureNewInstance(Instance instance) {
    instance.addAttributeListener();
    instance.computeLabelTextField(Instance.AVOID_LEFT);
  }

  @Override
  public Bounds getOffsetBounds(AttributeSet attrs) {
    Direction facing = attrs.getValue(StdAttr.FACING);
    return Bounds.create(-20, -10, 20, 20).rotate(Direction.EAST, facing, 0, 0);
  }

  @Override
  protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
    if (attr == StdAttr.FACING) {
      instance.recomputeBounds();
      instance.computeLabelTextField(Instance.AVOID_LEFT);
    } else if (attr == StdAttr.LABEL_LOC) {
      instance.computeLabelTextField(Instance.AVOID_LEFT);
    }
  }

  @Override
  public void paintGhost(InstancePainter painter) {
    final var g = (Graphics2D) painter.getGraphics();
    final var bds = painter.getBounds();
    GraphicsUtil.switchToWidth(g, 2);
    g.drawOval(bds.getX() + 2, bds.getY() + 2, bds.getWidth() - 4, bds.getHeight() - 4);
  }

  @Override
  public void paintInstance(InstancePainter painter) {
    final var data = (InstanceDataSingleton) painter.getData();
    final var isLit = data != null && data.getValue() == Value.TRUE;
    final var bds = painter.getBounds();
    final var g = (Graphics2D) painter.getGraphics();

    int cx = bds.getX() + bds.getWidth() / 2;
    int cy = bds.getY() + bds.getHeight() / 2;
    int r = Math.min(bds.getWidth(), bds.getHeight()) / 2 - 2;

    if (painter.getShowState()) {
      final var onColor = painter.getAttributeValue(IoLibrary.ATTR_ON_COLOR);
      final var offColor = painter.getAttributeValue(IoLibrary.ATTR_OFF_COLOR);
      g.setColor(isLit ? onColor : offColor);
      g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
    }

    g.setColor(AppPreferences.getComponentColor());
    GraphicsUtil.switchToWidth(g, 2);
    g.drawOval(cx - r, cy - r, 2 * r, 2 * r);
    GraphicsUtil.switchToWidth(g, 1);

    painter.drawLabel();
    painter.drawPorts();
  }

  @Override
  public void propagate(InstanceState state) {
    final var val0 = state.getPortValue(0);
    final var val1 = state.getPortValue(1);
    final var active = state.getAttributeValue(IoLibrary.ATTR_ACTIVE);
    final var desiredForward = active ? (val0 == Value.TRUE && val1 == Value.FALSE) : (val0 == Value.FALSE && val1 == Value.TRUE);
    final var lit = desiredForward ? Value.TRUE : Value.FALSE;

    final var data = (InstanceDataSingleton) state.getData();
    if (data == null) {
      state.setData(new InstanceDataSingleton(lit));
    } else {
      data.setValue(lit);
    }
  }

  @Override
  public DynamicElement createDynamicElement(int x, int y, DynamicElement.Path path) {
    return new TwoPinLedShape(x, y, path);
  }
}
