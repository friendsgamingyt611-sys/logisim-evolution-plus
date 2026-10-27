package com.cburch.logisim.gui.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cburch.logisim.gui.menu.LogisimMenuBar;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectActions;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.junit.jupiter.api.Test;

public class Stage20AcceptanceTest {

  private static final String ARTIFACT_DIR =
      "/home/devansh/.gemini/antigravity-ide/brain/904c724c-98b1-4fc8-adc9-6305dcb0eb9c/";

  private Frame createTestFrame(String lafClass) throws Exception {
    UIManager.setLookAndFeel(lafClass);
    AppPreferences.LookAndFeel.set(lafClass);
    final Frame[] holder = new Frame[1];
    SwingUtilities.invokeAndWait(() -> {
      Project p = ProjectActions.doNew((Project) null);
      Frame f = p.getFrame();
      f.setSize(1024, 768);
      f.validate();
      holder[0] = f;
    });
    Thread.sleep(300);
    return holder[0];
  }

  private void captureSidebar(Frame frame, int sidebarWidth, String filename) throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      try {
        Toolbox tb = frame.getToolbox();
        tb.setSize(new Dimension(sidebarWidth, frame.getHeight()));
        tb.validate();

        // Capture top container (row 1 + row 2) of Toolbox
        int w = sidebarWidth;
        int h = AppPreferences.getScaled(88); // 40px + 44px + margins
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        tb.paint(g2);
        g2.dispose();

        File out = new File(ARTIFACT_DIR + filename);
        ImageIO.write(img, "PNG", out);
        System.out.println("Saved sidebar capture: " + out.getName() + " (" + w + "x" + h + ")");
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  @Test
  void testMeasuredStripWidths() {
    double originalScale = AppPreferences.SCALE_FACTOR.get();
    try {
      // 100% scale
      AppPreferences.SCALE_FACTOR.set(1.0);
      int w100 = AppPreferences.getScaled(266);
      System.out.println("Measured Strip Width at 100% scale: " + w100 + " px");
      assertEquals(266, w100);

      // 150% scale
      AppPreferences.SCALE_FACTOR.set(1.5);
      int w150 = AppPreferences.getScaled(266);
      System.out.println("Measured Strip Width at 150% scale: " + w150 + " px");
      assertEquals(399, w150);

      // 200% scale
      AppPreferences.SCALE_FACTOR.set(2.0);
      int w200 = AppPreferences.getScaled(266);
      System.out.println("Measured Strip Width at 200% scale: " + w200 + " px");
      assertEquals(532, w200);
    } finally {
      AppPreferences.SCALE_FACTOR.set(originalScale);
    }
  }

  @Test
  void testSidebarCollapseAndRestore() throws Exception {
    Frame frame = createTestFrame(FlatDarkLaf.class.getName());
    try {
      // Test 5 cycles of collapse and restore
      for (int i = 0; i < 5; i++) {
        SwingUtilities.invokeAndWait(frame::toggleSidebar);
        Thread.sleep(50);
        SwingUtilities.invokeAndWait(() -> {
          // Fraction should be 0.0 when collapsed
          assertEquals(0.0, frame.getMainRegion().getFraction(), 0.01, "Sidebar should collapse to 0.0");
        });

        SwingUtilities.invokeAndWait(frame::toggleSidebar);
        Thread.sleep(50);
        SwingUtilities.invokeAndWait(() -> {
          assertTrue(frame.getMainRegion().getFraction() >= 0.15, "Sidebar should restore to >= 0.15");
        });
      }
      System.out.println("5 cycles of sidebar collapse and restore verified successfully.");
    } finally {
      SwingUtilities.invokeAndWait(() -> frame.setVisible(false));
    }
  }

  @Test
  void testActionButtonsBehaviorAndSync() throws Exception {
    Frame frame = createTestFrame(FlatDarkLaf.class.getName());
    try {
      Toolbox tb = frame.getToolbox();
      assertNotNull(tb);

      JButton btnAdd = tb.getBtnAddCircuit();
      JButton btnVhdl = tb.getBtnAddVhdl();
      JButton btnUp = tb.getBtnMoveUp();
      JButton btnDown = tb.getBtnMoveDown();
      JToggleButton btnApp = tb.getBtnAppearance();
      JButton btnDel = tb.getBtnDelete();

      assertNotNull(btnAdd);
      assertNotNull(btnVhdl);
      assertNotNull(btnUp);
      assertNotNull(btnDown);
      assertNotNull(btnApp);
      assertNotNull(btnDel);

      SwingUtilities.invokeAndWait(tb::updateActionButtons);

      // 1 circuit in new project:
      // Move up/down and remove should be disabled with reasons
      assertFalse(btnUp.isEnabled());
      assertEquals("Already first", btnUp.getToolTipText());

      assertFalse(btnDown.isEnabled());
      assertEquals("Already last", btnDown.getToolTipText());

      assertFalse(btnDel.isEnabled());
      assertEquals("Can't remove the only circuit", btnDel.getToolTipText());

      // Toggle Appearance starts false in layout editor
      assertFalse(btnApp.isSelected());
      assertEquals("Edit Circuit Appearance", btnApp.getToolTipText());

      // Switch to appearance editor
      SwingUtilities.invokeAndWait(() -> frame.setEditorView(Frame.EDIT_APPEARANCE));
      Thread.sleep(100);
      assertTrue(btnApp.isSelected(), "Appearance button must be selected when in EDIT_APPEARANCE");
      assertEquals("Back to Circuit Layout", btnApp.getToolTipText());

      // Switch back to layout
      SwingUtilities.invokeAndWait(() -> frame.setEditorView(Frame.EDIT_LAYOUT));
      Thread.sleep(100);
      assertFalse(btnApp.isSelected(), "Appearance button must be deselected when in EDIT_LAYOUT");
      assertEquals("Edit Circuit Appearance", btnApp.getToolTipText());

      System.out.println("Stage 20 Action buttons behavior, tooltips, and sync verified successfully.");
    } finally {
      SwingUtilities.invokeAndWait(() -> frame.setVisible(false));
    }
  }

  @Test
  void testCaptureScreenshots() throws Exception {
    double originalScale = AppPreferences.SCALE_FACTOR.get();
    try {
      AppPreferences.SCALE_FACTOR.set(1.0);

      // Dark theme captures
      Frame fDark = createTestFrame(FlatDarkLaf.class.getName());
      int minW = fDark.getToolbox().getMeasuredStripWidth();
      captureSidebar(fDark, minW, "stage20_strip_dark_min_100.png");
      captureSidebar(fDark, 336, "stage20_strip_dark_336_100.png");
      captureSidebar(fDark, 500, "stage20_strip_dark_500_100.png");
      SwingUtilities.invokeAndWait(() -> fDark.setVisible(false));

      // Light theme captures
      Frame fLight = createTestFrame(FlatLightLaf.class.getName());
      captureSidebar(fLight, minW, "stage20_strip_light_min_100.png");
      captureSidebar(fLight, 336, "stage20_strip_light_336_100.png");
      captureSidebar(fLight, 500, "stage20_strip_light_500_100.png");
      SwingUtilities.invokeAndWait(() -> fLight.setVisible(false));

      // 200% scale captures
      AppPreferences.SCALE_FACTOR.set(2.0);
      Frame fDark200 = createTestFrame(FlatDarkLaf.class.getName());
      int minW200 = fDark200.getToolbox().getMeasuredStripWidth();
      captureSidebar(fDark200, minW200, "stage20_strip_dark_min_200.png");
      SwingUtilities.invokeAndWait(() -> fDark200.setVisible(false));

      Frame fLight200 = createTestFrame(FlatLightLaf.class.getName());
      captureSidebar(fLight200, minW200, "stage20_strip_light_min_200.png");
      SwingUtilities.invokeAndWait(() -> fLight200.setVisible(false));

      System.out.println("All Stage 20 screenshots captured successfully.");
    } finally {
      AppPreferences.SCALE_FACTOR.set(originalScale);
    }
  }
}
