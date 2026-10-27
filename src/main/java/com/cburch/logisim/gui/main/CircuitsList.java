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
  private final JLabel headerLabel;
  private final JPanel itemsPanel;
  private Tool hoveredTool = null;

  public CircuitsList(ExplorerModel model) {
    super(new BorderLayout());
    this.model = model;
    setOpaque(false);

    // Section header with subtle rule above
    final var headerBox = new JPanel(new BorderLayout());
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

      final var rowH = AppPreferences.getScaled(28);
      setPreferredSize(new Dimension(-1, rowH));
      setMaximumSize(new Dimension(Integer.MAX_VALUE, rowH));

      final var isActive = model.isCircuitActive(tool);
      final var isArmed = (model.getArmedTool() == tool);
      icon = new CircuitGlyphIcon(isActive);

      label = new JLabel(tool.getDisplayName());
      label.setFont(new Font(Font.SANS_SERIF, isActive ? Font.BOLD : Font.PLAIN, AppPreferences.getScaled(12)));
      label.setIcon(icon);
      label.setIconTextGap(AppPreferences.getScaled(8));
      label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 8));

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
        // Armed tool: solid accent fill -> white text
        icon.setColor(Color.WHITE);
        label.setForeground(Color.WHITE);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
      } else if (isActive) {
        // Current circuit: 14% accent tint -> semibold text with normal/accent color (not solid white)
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

      Color accent = UIManager.getColor("Component.accentColor");
      if (accent == null) accent = new Color(53, 132, 228);

      final var isActive = model.isCircuitActive(tool);
      final var isHovered = (hoveredTool == tool);
      final var isArmed = (model.getArmedTool() == tool);

      updateRowColors(isActive, isArmed);

      if (isArmed) {
        // Armed tool gets the solid accent fill
        g2.setColor(accent);
        g2.fillRoundRect(marginX, 1, w - 2 * marginX, h - 2, arc, arc);
      } else if (isActive) {
        // Current circuit gets accent 14% tint + 3px left bar + semibold
        Color tint = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int) (255 * 0.14f));
        g2.setColor(tint);
        g2.fillRoundRect(marginX, 1, w - 2 * marginX, h - 2, arc, arc);

        // 3px solid accent left bar
        int barW = AppPreferences.getScaled(3);
        int barH = h - AppPreferences.getScaled(8);
        int barY = (h - barH) / 2;
        g2.setColor(accent);
        g2.fillRoundRect(marginX + 1, barY, barW, barH, 2, 2);

        // Active simulation indicator badge on right
        int dotR = AppPreferences.getScaled(3);
        int dotX = w - marginX - AppPreferences.getScaled(14);
        int dotY = (h - dotR * 2) / 2;
        g2.setColor(new Color(46, 209, 115, 60));
        g2.fillOval(dotX - 2, dotY - 2, dotR * 2 + 4, dotR * 2 + 4);
        g2.setColor(new Color(46, 209, 115));
        g2.fillOval(dotX, dotY, dotR * 2, dotR * 2);
      } else if (isHovered) {
        Color hoverBg = AppPreferences.isDarkTheme()
            ? new Color(255, 255, 255, 14)
            : new Color(0, 0, 0, 12);
        g2.setColor(hoverBg);
        g2.fillRoundRect(marginX, 1, w - 2 * marginX, h - 2, arc, arc);
      }

      g2.dispose();
      super.paintComponent(g);
    }
  }
}
