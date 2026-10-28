package com.cburch.logisim.gui.main;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.SubcircuitFactory;
import com.cburch.logisim.gui.icons.symbolic.CircuitGlyphIcon;
import com.cburch.logisim.gui.menu.Popups;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.vhdl.base.VhdlEntity;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * Modern circuits list section in the redesigned left explorer.
 * Displays circuits with semantic hierarchy: 14% tint + 3px accent bar for active circuit,
 * solid accent for armed tool, and unified section header styling.
 */
public class CircuitsList extends JPanel implements ExplorerModel.Listener {

  private static final long serialVersionUID = 1L;
  private final ExplorerModel model;
  private final JPanel headerBox;
  private final JLabel headerLabel;
  private final JPanel itemsPanel;
  private Tool hoveredTool = null;

  public CircuitsList(ExplorerModel model) {
    super(new BorderLayout());
    this.model = model;
    setOpaque(false);

    // Section header with subtle rule above
    headerBox = new JPanel(new BorderLayout());
    headerBox.setOpaque(false);
    Color borderSubtle = UIManager.getColor("Surface.borderSubtle");
    if (borderSubtle == null) {
      borderSubtle = AppPreferences.isDarkTheme() ? new Color(0x39, 0x3C, 0x42) : new Color(0xD5, 0xDA, 0xE2);
    }
    headerBox.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 0, 0, 0, borderSubtle),
        BorderFactory.createEmptyBorder(6, 12, 4, 12)));

    headerLabel = new JLabel("CIRCUITS");
    headerLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, AppPreferences.getScaled(11)));
    headerLabel.setForeground(getMutedHeaderColor());
    headerBox.add(headerLabel, BorderLayout.WEST);

    add(headerBox, BorderLayout.NORTH);

    itemsPanel = new JPanel();
    itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
    itemsPanel.setOpaque(false);
    itemsPanel.setBorder(BorderFactory.createEmptyBorder(2, 6, 6, 6));

    add(itemsPanel, BorderLayout.CENTER);

    updateBorder();
    model.addListener(this);
    refresh();
  }

  private Color getMutedHeaderColor() {
    Color fgMuted = UIManager.getColor("Surface.mutedText");
    if (fgMuted == null) fgMuted = UIManager.getColor("Chrome.mutedText");
    if (fgMuted == null) {
      fgMuted = AppPreferences.isDarkTheme() ? new Color(0x9E, 0x9E, 0x9E) : new Color(0x5A, 0x5D, 0x66);
    }
    return fgMuted;
  }

  private void updateBorder() {
    Color divColor = UIManager.getColor("Surface.borderSubtle");
    if (divColor == null) {
      divColor = AppPreferences.isDarkTheme() ? new Color(0x39, 0x3C, 0x42) : new Color(0xD5, 0xDA, 0xE2);
    }
    setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, divColor));
    if (headerBox != null) {
      headerBox.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createMatteBorder(1, 0, 0, 0, divColor),
          BorderFactory.createEmptyBorder(6, 12, 4, 12)));
    }
  }

  @Override
  public void updateUI() {
    super.updateUI();
    updateBorder();
    if (headerLabel != null) {
      headerLabel.setForeground(getMutedHeaderColor());
    }
    if (model != null) {
      refresh();
    }
  }

  public void refresh() {
    itemsPanel.removeAll();
    final var circuits = model.getCircuits();
    headerLabel.setText("CIRCUITS (" + circuits.size() + ")");

    for (final var tool : circuits) {
      itemsPanel.add(new CircuitRow(tool));
      itemsPanel.add(Box.createVerticalStrut(AppPreferences.getScaled(2)));
    }

    revalidate();
    repaint();
  }

  @Override
  public void toolArmed(Tool tool) {
    repaint();
  }

  @Override
  public void circuitChanged(Circuit circuit) {
    refresh();
  }

  @Override
  public void structureChanged() {
    refresh();
  }

  @Override
  public void filterChanged(String query) {
    if (query == null || query.isBlank()) {
      for (final var comp : itemsPanel.getComponents()) {
        comp.setVisible(true);
      }
    } else {
      final var q = query.toLowerCase();
      for (final var comp : itemsPanel.getComponents()) {
        if (comp instanceof CircuitRow row) {
          row.setVisible(row.tool.getDisplayName().toLowerCase().contains(q));
        }
      }
    }
    revalidate();
    repaint();
  }

  @Override
  public void viewModeChanged(String newMode) {
    // Handled by card layout
  }

  private class CircuitRow extends JPanel {
    private static final long serialVersionUID = 1L;
    private final AddTool tool;
    private final CircuitGlyphIcon icon;
    private final JLabel label;

    CircuitRow(AddTool tool) {
      super(new BorderLayout());
      this.tool = tool;
      setOpaque(false);

      final var rowH = AppPreferences.getScaled(30);
      setPreferredSize(new Dimension(-1, rowH));
      setMaximumSize(new Dimension(Integer.MAX_VALUE, rowH));

      final var isActive = model.isCircuitActive(tool);
      final var isArmed = (model.getArmedTool() == tool);
      icon = new CircuitGlyphIcon(isActive, 18);

      label = new JLabel(tool.getDisplayName());
      label.setFont(new Font(Font.SANS_SERIF, isActive ? Font.BOLD : Font.PLAIN, AppPreferences.getScaled(12)));
      label.setIcon(icon);
      label.setIconTextGap(AppPreferences.getScaled(8));
      label.setBorder(BorderFactory.createEmptyBorder(0, AppPreferences.getScaled(10), 0, AppPreferences.getScaled(22)));

      updateRowColors(isActive, isArmed);
      add(label, BorderLayout.CENTER);

      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
          hoveredTool = tool;
          repaint();
        }

        @Override
        public void mouseExited(MouseEvent e) {
          if (hoveredTool == tool) {
            hoveredTool = null;
            repaint();
          }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
          if (e.getClickCount() == 2) {
            final var factory = tool.getFactory(false);
            if (factory instanceof SubcircuitFactory sub) {
              model.setCurrentCircuit(sub.getSubcircuit());
            } else if (factory instanceof VhdlEntity vhdl) {
              model.setCurrentHdl(vhdl.getContent());
            }
          } else {
            model.armTool(tool);
          }
        }

        @Override
        public void mousePressed(MouseEvent e) {
          if (e.isPopupTrigger()) showMenu(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
          if (e.isPopupTrigger()) showMenu(e);
        }

        private void showMenu(MouseEvent e) {
          final var fact = tool.getFactory(false);
          if (fact instanceof SubcircuitFactory sub) {
            final var menu = Popups.forCircuit(model.getProject(), tool, sub.getSubcircuit());
            if (menu != null) menu.show(CircuitRow.this, e.getX(), e.getY());
          } else if (fact instanceof VhdlEntity vhdl) {
            final var menu = Popups.forVhdl(model.getProject(), tool, vhdl.getContent());
            if (menu != null) menu.show(CircuitRow.this, e.getX(), e.getY());
          }
        }
      });
    }

    private void updateRowColors(boolean isActive, boolean isArmed) {
      Color fgMain = UIManager.getColor("Label.foreground");
      if (fgMain == null) fgMain = AppPreferences.isDarkTheme() ? new Color(245, 245, 245) : new Color(38, 40, 44);
      Color fgMuted = getMutedHeaderColor();
      Color accent = UIManager.getColor("Component.accentColor");
      if (accent == null) accent = new Color(53, 132, 228);

      if (isArmed) {
        // Armed/selected tool: solid accent fill -> crisp white text
        icon.setColor(Color.WHITE);
        label.setForeground(Color.WHITE);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
      } else if (isActive) {
        // Current/active circuit: subtle accent tint pill -> prominent accent icon & semibold text
        icon.setColor(accent);
        label.setForeground(fgMain);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
      } else {
        icon.setColor(fgMuted);
        label.setForeground(fgMain);
        label.setFont(label.getFont().deriveFont(Font.PLAIN));
      }
    }

    @Override
    protected void paintComponent(Graphics g) {
      final var g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      int w = getWidth();
      int h = getHeight();
      int arc = AppPreferences.getScaled(6);
      int marginX = AppPreferences.getScaled(4);
      int innerW = w - 2 * marginX;
      int innerH = h - 2;

      Color accent = UIManager.getColor("Component.accentColor");
      if (accent == null) accent = new Color(53, 132, 228);

      final var isActive = model.isCircuitActive(tool);
      final var isHovered = (hoveredTool == tool);
      final var isArmed = (model.getArmedTool() == tool);

      updateRowColors(isActive, isArmed);

      if (isArmed) {
        // Selected circuit gets clean modern solid accent fill with rounded corners
        g2.setColor(accent);
        g2.fillRoundRect(marginX, 1, innerW, innerH, arc, arc);
      } else if (isActive) {
        // Active/viewed circuit gets high-end subtle translucent rounded pill (no harsh left notch)
        Color tint = AppPreferences.isDarkTheme()
            ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int) (255 * 0.18f))
            : new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int) (255 * 0.12f));
        g2.setColor(tint);
        g2.fillRoundRect(marginX, 1, innerW, innerH, arc, arc);
      } else if (isHovered) {
        // Smooth theme-aware hover highlight
        Color hoverBg = AppPreferences.isDarkTheme()
            ? new Color(255, 255, 255, 14)
            : new Color(0, 0, 0, 12);
        g2.setColor(hoverBg);
        g2.fillRoundRect(marginX, 1, innerW, innerH, arc, arc);
      }

      // Simulation active indicator dot on the right
      if (isActive) {
        int dotR = AppPreferences.getScaled(3);
        int dotX = w - marginX - AppPreferences.getScaled(14);
        int dotY = (h - dotR * 2) / 2;
        Color halo = isArmed ? new Color(255, 255, 255, 90) : new Color(46, 209, 115, 60);
        Color dot = isArmed ? Color.WHITE : new Color(46, 209, 115);
        g2.setColor(halo);
        g2.fillOval(dotX - 2, dotY - 2, dotR * 2 + 4, dotR * 2 + 4);
        g2.setColor(dot);
        g2.fillOval(dotX, dotY, dotR * 2, dotR * 2);
      }

      g2.dispose();
      super.paintComponent(g);
    }
  }
}
