package com.cburch.logisim.gui.theme;

import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectActions;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Component;
import java.awt.Container;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.junit.jupiter.api.Test;

public class ComponentHierarchyDiagnosticTest {

  private void inspectComponents(Container container, String prefix) {
    for (Component c : container.getComponents()) {
      String name = c.getClass().getSimpleName();
      if (name.isEmpty()) name = c.getClass().getName();
      System.out.println(prefix + name + " bounds=" + c.getBounds() + " bg=" + c.getBackground() + " fg=" + c.getForeground());
      if (c instanceof Container childCont && childCont.getComponentCount() > 0) {
        inspectComponents(childCont, prefix + "  ");
      }
    }
  }

  @Test
  void testInspectColdAndSwitch() throws Exception {
    System.out.println("--- INSPECTION: COLD START LIGHT ---");
    UIManager.setLookAndFeel(FlatLightLaf.class.getName());
    AppPreferences.LookAndFeel.set(FlatLightLaf.class.getName());
    final Frame[] holder = new Frame[1];
    SwingUtilities.invokeAndWait(() -> {
      Project p = ProjectActions.doNew((Project) null);
      Frame f = p.getFrame();
      holder[0] = f;
      f.setSize(1024, 768);
      f.validate();
    });
    inspectFrame(holder[0], "COLD LIGHT");

    System.out.println("\n--- INSPECTION: COLD START DARK ---");
    SwingUtilities.invokeAndWait(() -> {
      holder[0].setVisible(false);
    });
    UIManager.setLookAndFeel(FlatDarkLaf.class.getName());
    AppPreferences.LookAndFeel.set(FlatDarkLaf.class.getName());
    SwingUtilities.invokeAndWait(() -> {
      Project p = ProjectActions.doNew((Project) null);
      Frame f = p.getFrame();
      holder[0] = f;
      f.setSize(1024, 768);
      f.validate();
    });
    inspectFrame(holder[0], "COLD DARK");

    System.out.println("\n--- INSPECTION: DARK -> LIVE SWITCH TO LIGHT ---");
    SwingUtilities.invokeAndWait(() -> {
      ThemeManager.applyTheme(FlatLightLaf.class.getName());
    });
    Thread.sleep(600);
    inspectFrame(holder[0], "DARK -> LIVE SWITCH TO LIGHT");

    SwingUtilities.invokeAndWait(() -> {
      holder[0].setVisible(false);
    });
  }

  private void inspectFrame(Frame f, String label) {
    System.out.println("=== " + label + " ===");
    for (Component c : f.getContentPane().getComponents()) {
      printInterested(c, "");
    }
  }

  private void printInterested(Component c, String prefix) {
    String name = c.getClass().getSimpleName();
    if (c instanceof JTabbedPane || c.getClass().getName().contains("Toolbar") || c instanceof JTable || name.contains("ModernHeaderBar")) {
      System.out.println(prefix + c.getClass().getName() + " bg=" + toHex(c.getBackground()) + " fg=" + toHex(c.getForeground()) + " bounds=" + c.getBounds());
    }
    if (c instanceof Container cont) {
      for (Component child : cont.getComponents()) {
        printInterested(child, prefix + "  ");
      }
    }
  }

  private String toHex(java.awt.Color c) {
    if (c == null) return "null";
    return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
  }
}
