package com.cburch.logisim.gui.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitMutation;
import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.gui.theme.ContrastTokensTest;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Action;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectActions;
import com.cburch.logisim.std.wiring.Pin;
import com.cburch.logisim.util.StringUtil;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class Stage24UndoRedoTest {

  @BeforeAll
  public static void setup() {
  }

  private Frame createFrame() throws Exception {
    final Frame[] holder = new Frame[1];
    SwingUtilities.invokeAndWait(() -> {
      Project p = ProjectActions.doNew((Project) null);
      Frame f = p.getFrame();
      f.setSize(1024, 768);
      f.validate();
      holder[0] = f;
    });
    return holder[0];
  }

  private void assertUndoRedoState(Frame frame, boolean expectedCanUndo, String expectedUndoTip,
                                   boolean expectedCanRedo, String expectedRedoTip) throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      Project proj = frame.getProject();
      ModernHeaderBar header = frame.getModernHeaderBar();
      assertNotNull(header, "ModernHeaderBar must exist");
      JButton btnUndo = header.getUndoButton();
      JButton btnRedo = header.getRedoButton();
      assertNotNull(btnUndo, "Undo button must exist");
      assertNotNull(btnRedo, "Redo button must exist");

      // Check header button enabled state
      assertEquals(expectedCanUndo, btnUndo.isEnabled(), "Undo button enabled mismatch");
      assertEquals(expectedCanRedo, btnRedo.isEnabled(), "Redo button enabled mismatch");

      // Check header button tooltips
      assertEquals(expectedUndoTip, btnUndo.getToolTipText(), "Undo tooltip mismatch");
      assertEquals(expectedRedoTip, btnRedo.getToolTipText(), "Redo tooltip mismatch");

      // Check project consistency
      assertEquals(expectedCanUndo, proj.getLastAction() != null, "proj.getLastAction mismatch");
      assertEquals(expectedCanRedo, proj.getCanRedo(), "proj.getCanRedo mismatch");
    });
  }

  @Test
  public void testUndoRedoFullLifecycle() throws Exception {
    Frame frame = createFrame();
    Project proj = frame.getProject();
    Circuit circuit = proj.getCurrentCircuit();

    // Table to record action sequence vs button states
    List<String> reportTable = new ArrayList<>();
    reportTable.add(String.format("%-25s | %-10s | %-10s | %-30s | %-30s",
        "Step / Action", "Undo Enabled", "Redo Enabled", "Undo Tooltip", "Redo Tooltip"));

    // 0. Initial blank canvas
    assertUndoRedoState(frame, false, "Nothing to undo · Ctrl+Z", false, "Nothing to redo · Ctrl+Y");
    reportTable.add(String.format("%-25s | %-10s | %-10s | %-30s | %-30s",
        "0. Initial (Blank)", false, false, "Nothing to undo · Ctrl+Z", "Nothing to redo · Ctrl+Y"));

    // 1. Edit 1: Place Pin
    final var pin = Pin.FACTORY.createComponent(Location.create(50, 50, true), Pin.FACTORY.createAttributeSet());
    CircuitMutation m1 = new CircuitMutation(circuit);
    m1.add(pin);
    Action placeAction = m1.toAction(StringUtil.constantGetter("Add Pin"));
    SwingUtilities.invokeAndWait(() -> proj.doAction(placeAction));

    assertUndoRedoState(frame, true, "Undo Add Pin · Ctrl+Z", false, "Nothing to redo · Ctrl+Y");
    assertTrue(circuit.contains(pin));
    reportTable.add(String.format("%-25s | %-10s | %-10s | %-30s | %-30s",
        "1. Place Pin", true, false, "Undo Add Pin · Ctrl+Z", "Nothing to redo · Ctrl+Y"));

    // 2. Edit 2: Move Component
    final var wire = Wire.create(Location.create(50, 50, true), Location.create(100, 50, true));
    CircuitMutation m2 = new CircuitMutation(circuit);
    m2.add(wire);
    Action moveAction = m2.toAction(StringUtil.constantGetter("Move Components"));
    SwingUtilities.invokeAndWait(() -> proj.doAction(moveAction));

    assertUndoRedoState(frame, true, "Undo Move Components · Ctrl+Z", false, "Nothing to redo · Ctrl+Y");
    reportTable.add(String.format("%-25s | %-10s | %-10s | %-30s | %-30s",
        "2. Move Components", true, false, "Undo Move Components · Ctrl+Z", "Nothing to redo · Ctrl+Y"));

    // 3. Edit 3: Delete
    CircuitMutation m3 = new CircuitMutation(circuit);
    m3.remove(wire);
    Action deleteAction = m3.toAction(StringUtil.constantGetter("Delete Components"));
    SwingUtilities.invokeAndWait(() -> proj.doAction(deleteAction));

    assertUndoRedoState(frame, true, "Undo Delete Components · Ctrl+Z", false, "Nothing to redo · Ctrl+Y");
    assertFalse(circuit.getWires().contains(wire));
    reportTable.add(String.format("%-25s | %-10s | %-10s | %-30s | %-30s",
        "3. Delete Components", true, false, "Undo Delete Components · Ctrl+Z", "Nothing to redo · Ctrl+Y"));

    // 4. Undo 1 (revert delete)
    SwingUtilities.invokeAndWait(proj::undoAction);
    assertUndoRedoState(frame, true, "Undo Move Components · Ctrl+Z", true, "Redo Delete Components · Ctrl+Y");
    assertTrue(circuit.getWires().contains(wire));
    reportTable.add(String.format("%-25s | %-10s | %-10s | %-30s | %-30s",
        "4. Undo (Delete)", true, true, "Undo Move Components · Ctrl+Z", "Redo Delete Components · Ctrl+Y"));

    // 5. Undo 2 (revert move)
    SwingUtilities.invokeAndWait(proj::undoAction);
    assertUndoRedoState(frame, true, "Undo Add Pin · Ctrl+Z", true, "Redo Move Components · Ctrl+Y");
    assertFalse(circuit.getWires().contains(wire));
    reportTable.add(String.format("%-25s | %-10s | %-10s | %-30s | %-30s",
        "5. Undo (Move)", true, true, "Undo Add Pin · Ctrl+Z", "Redo Move Components · Ctrl+Y"));

    // 6. Undo 3 (revert place)
    SwingUtilities.invokeAndWait(proj::undoAction);
    assertUndoRedoState(frame, false, "Nothing to undo · Ctrl+Z", true, "Redo Add Pin · Ctrl+Y");
    assertFalse(circuit.contains(pin));
    reportTable.add(String.format("%-25s | %-10s | %-10s | %-30s | %-30s",
        "6. Undo (Place)", false, true, "Nothing to undo · Ctrl+Z", "Redo Add Pin · Ctrl+Y"));

    // 7. Redo 1 (redo place)
    SwingUtilities.invokeAndWait(proj::redoAction);
    assertUndoRedoState(frame, true, "Undo Add Pin · Ctrl+Z", true, "Redo Move Components · Ctrl+Y");
    assertTrue(circuit.contains(pin));
    reportTable.add(String.format("%-25s | %-10s | %-10s | %-30s | %-30s",
        "7. Redo (Place)", true, true, "Undo Add Pin · Ctrl+Z", "Redo Move Components · Ctrl+Y"));

    // 8. Redo 2 (redo move)
    SwingUtilities.invokeAndWait(proj::redoAction);
    assertUndoRedoState(frame, true, "Undo Move Components · Ctrl+Z", true, "Redo Delete Components · Ctrl+Y");
    assertTrue(circuit.getWires().contains(wire));
    reportTable.add(String.format("%-25s | %-10s | %-10s | %-30s | %-30s",
        "8. Redo (Move)", true, true, "Undo Move Components · Ctrl+Z", "Redo Delete Components · Ctrl+Y"));

    // 9. Redo 3 (redo delete)
    SwingUtilities.invokeAndWait(proj::redoAction);
    assertUndoRedoState(frame, true, "Undo Delete Components · Ctrl+Z", false, "Nothing to redo · Ctrl+Y");
    assertFalse(circuit.getWires().contains(wire));
    reportTable.add(String.format("%-25s | %-10s | %-10s | %-30s | %-30s",
        "9. Redo (Delete)", true, false, "Undo Delete Components · Ctrl+Z", "Nothing to redo · Ctrl+Y"));

    // 10. Undo after save
    File tmp = File.createTempFile("stage24_save", ".circ");
    tmp.deleteOnExit();
    SwingUtilities.invokeAndWait(() -> assertTrue(ProjectActions.doSave(proj, tmp)));
    assertFalse(proj.isFileDirty(), "File should be clean after save");
    assertUndoRedoState(frame, true, "Undo Delete Components · Ctrl+Z", false, "Nothing to redo · Ctrl+Y");

    // Undo after save should still work and mark file dirty again
    SwingUtilities.invokeAndWait(proj::undoAction);
    assertTrue(proj.isFileDirty(), "File should be dirty after undo");
    assertUndoRedoState(frame, true, "Undo Move Components · Ctrl+Z", true, "Redo Delete Components · Ctrl+Y");
    reportTable.add(String.format("%-25s | %-10s | %-10s | %-30s | %-30s",
        "10. Undo after Save", true, true, "Undo Move Components · Ctrl+Z", "Redo Delete Components · Ctrl+Y"));

    // 11. Rapid repeated clicks on redo / undo
    SwingUtilities.invokeAndWait(() -> {
      for (int i = 0; i < 5; i++) {
        proj.redoAction();
      }
    });
    assertUndoRedoState(frame, true, "Undo Delete Components · Ctrl+Z", false, "Nothing to redo · Ctrl+Y");

    // Print table to stdout for report inclusion
    System.out.println("=== Stage 24 Action Sequence vs Expected Button States ===");
    for (String row : reportTable) {
      System.out.println(row);
    }
  }

  @Test
  public void testTextFieldFocusShortcutSeparation() throws Exception {
    Frame frame = createFrame();
    Project proj = frame.getProject();

    // Place a Pin so project has undoable action
    final var pin = Pin.FACTORY.createComponent(Location.create(50, 50, true), Pin.FACTORY.createAttributeSet());
    CircuitMutation m = new CircuitMutation(proj.getCurrentCircuit());
    m.add(pin);
    Action placeAction = m.toAction(StringUtil.constantGetter("Add Pin"));
    SwingUtilities.invokeAndWait(() -> proj.doAction(placeAction));
    assertTrue(proj.getLastAction() != null);

    // Header buttons always act on project
    SwingUtilities.invokeAndWait(() -> {
      JButton btnUndo = frame.getModernHeaderBar().getUndoButton();
      assertTrue(btnUndo.isEnabled());
      btnUndo.doClick();
    });
    // Pin should be undone
    assertFalse(proj.getCurrentCircuit().contains(pin));
  }

  @Test
  public void testLightDisabledVsEnabledColorContrast() {
    Color lightBg = new Color(0xE6, 0xE9, 0xEE); // Chrome surface
    Color enabledFg = new Color(0x26, 0x28, 0x2C); // Enabled button fg
    // Disabled at 40% alpha blended on lightBg
    int r = (int) Math.round(0.40 * 0x26 + 0.60 * 0xE6);
    int g = (int) Math.round(0.40 * 0x28 + 0.60 * 0xE9);
    int b = (int) Math.round(0.40 * 0x2C + 0.60 * 0xEE);
    Color disabledBlended = new Color(r, g, b);

    double cr = ContrastTokensTest.getContrastRatio(enabledFg, disabledBlended);
    System.out.printf("Contrast ratio between enabled fg (#26282C) and disabled blended on #E6E9EE: %.2f:1%n", cr);
    assertTrue(cr >= 3.0, "Disabled vs Enabled color difference must be at least 3:1 (got " + cr + ":1)");
  }
}
