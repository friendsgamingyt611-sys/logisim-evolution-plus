package com.cburch.logisim.gui.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.file.Loader;
import com.cburch.logisim.file.LogisimFileActions;
import com.cburch.logisim.gui.menu.LogisimMenuBar;
import com.cburch.logisim.gui.menu.ProjectCircuitActions;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectActions;
import com.cburch.logisim.vhdl.base.VhdlContent;
import java.io.File;
import java.util.List;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class Stage23MoveLogicTest {

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

  private void assertMoveState(Project proj, Frame frame, int expectedIndex, int totalCircuits) throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      Circuit cur = proj.getCurrentCircuit();
      assertNotNull(cur, "Current circuit must not be null");
      List<Circuit> displayed = ProjectCircuitActions.getDisplayedCircuits(proj);
      assertEquals(totalCircuits, displayed.size(), "Displayed circuit count mismatch");
      assertEquals(expectedIndex, displayed.indexOf(cur), "Current circuit index mismatch");

      boolean expectUp = expectedIndex > 0;
      boolean expectDown = expectedIndex < totalCircuits - 1;

      // Check ProjectCircuitActions function
      assertEquals(expectUp, ProjectCircuitActions.canMoveUp(proj, cur),
          "ProjectCircuitActions.canMoveUp mismatch for index " + expectedIndex);
      assertEquals(expectDown, ProjectCircuitActions.canMoveDown(proj, cur),
          "ProjectCircuitActions.canMoveDown mismatch for index " + expectedIndex);

      // Check MenuBar
      LogisimMenuBar menu = (LogisimMenuBar) frame.getJMenuBar();
      assertEquals(expectUp, menu.isEnabled(LogisimMenuBar.MOVE_CIRCUIT_UP),
          "MenuBar MOVE_CIRCUIT_UP mismatch for index " + expectedIndex);
      assertEquals(expectDown, menu.isEnabled(LogisimMenuBar.MOVE_CIRCUIT_DOWN),
          "MenuBar MOVE_CIRCUIT_DOWN mismatch for index " + expectedIndex);

      // Check Toolbox buttons
      Toolbox tb = frame.getToolbox();
      JButton btnUp = tb.getBtnMoveUp();
      JButton btnDown = tb.getBtnMoveDown();
      assertNotNull(btnUp, "btnUp should exist");
      assertNotNull(btnDown, "btnDown should exist");
      assertEquals(expectUp, btnUp.isEnabled(), "Toolbox btnUp mismatch for index " + expectedIndex);
      assertEquals(expectDown, btnDown.isEnabled(), "Toolbox btnDown mismatch for index " + expectedIndex);

      // Check Tooltips
      if (expectUp) {
        assertEquals("Move '" + cur.getName() + "' up · Shift+Ctrl+U", btnUp.getToolTipText());
      } else {
        assertEquals("Already first", btnUp.getToolTipText());
      }

      if (expectDown) {
        assertEquals("Move '" + cur.getName() + "' down · Shift+Ctrl+D", btnDown.getToolTipText());
      } else {
        assertEquals("Already last", btnDown.getToolTipText());
      }
    });
  }

  @Test
  public void testMoveLogic1Circuit() throws Exception {
    Frame frame = createFrame();
    Project proj = frame.getProject();
    assertMoveState(proj, frame, 0, 1);
  }

  @Test
  public void testMoveLogic2Circuits() throws Exception {
    Frame frame = createFrame();
    Project proj = frame.getProject();
    Circuit c2 = new Circuit("sub1", proj.getLogisimFile(), proj);
    SwingUtilities.invokeAndWait(() -> proj.doAction(LogisimFileActions.addCircuit(c2)));

    // Index 0 ("main")
    SwingUtilities.invokeAndWait(() -> proj.setCurrentCircuit(proj.getLogisimFile().getCircuits().get(0)));
    assertMoveState(proj, frame, 0, 2);

    // Index 1 ("sub1")
    SwingUtilities.invokeAndWait(() -> proj.setCurrentCircuit(proj.getLogisimFile().getCircuits().get(1)));
    assertMoveState(proj, frame, 1, 2);
  }

  @Test
  public void testMoveLogic3Circuits() throws Exception {
    Frame frame = createFrame();
    Project proj = frame.getProject();
    Circuit c2 = new Circuit("sub1", proj.getLogisimFile(), proj);
    Circuit c3 = new Circuit("sub2", proj.getLogisimFile(), proj);
    SwingUtilities.invokeAndWait(() -> {
      proj.doAction(LogisimFileActions.addCircuit(c2));
      proj.doAction(LogisimFileActions.addCircuit(c3));
    });

    for (int i = 0; i < 3; i++) {
      final int idx = i;
      SwingUtilities.invokeAndWait(() -> proj.setCurrentCircuit(proj.getLogisimFile().getCircuits().get(idx)));
      assertMoveState(proj, frame, idx, 3);
    }
  }

  @Test
  public void testMoveLogic5Circuits() throws Exception {
    Frame frame = createFrame();
    Project proj = frame.getProject();
    for (int i = 1; i <= 4; i++) {
      Circuit c = new Circuit("sub" + i, proj.getLogisimFile(), proj);
      SwingUtilities.invokeAndWait(() -> proj.doAction(LogisimFileActions.addCircuit(c)));
    }

    for (int i = 0; i < 5; i++) {
      final int idx = i;
      SwingUtilities.invokeAndWait(() -> proj.setCurrentCircuit(proj.getLogisimFile().getCircuits().get(idx)));
      assertMoveState(proj, frame, idx, 5);
    }
  }

  @Test
  public void testMoveLogicWithVhdlEntity() throws Exception {
    Frame frame = createFrame();
    Project proj = frame.getProject();
    Circuit c2 = new Circuit("sub1", proj.getLogisimFile(), proj);
    Circuit c3 = new Circuit("sub2", proj.getLogisimFile(), proj);
    VhdlContent vhdl = VhdlContent.create("MyVhdl", proj.getLogisimFile());
    SwingUtilities.invokeAndWait(() -> {
      proj.doAction(LogisimFileActions.addCircuit(c2));
      proj.doAction(LogisimFileActions.addVhdl(vhdl));
      proj.doAction(LogisimFileActions.addCircuit(c3));
    });

    // We have 3 circuits and 1 VHDL entity
    for (int i = 0; i < 3; i++) {
      final int idx = i;
      SwingUtilities.invokeAndWait(() -> proj.setCurrentCircuit(proj.getLogisimFile().getCircuits().get(idx)));
      assertMoveState(proj, frame, idx, 3);
    }
  }

  @Test
  public void testMoveFollowsSelectionAndMatchesSavedFileOrder() throws Exception {
    Frame frame = createFrame();
    Project proj = frame.getProject();
    Circuit c1 = proj.getCurrentCircuit(); // "main"
    Circuit c2 = new Circuit("c2", proj.getLogisimFile(), proj);
    Circuit c3 = new Circuit("c3", proj.getLogisimFile(), proj);
    SwingUtilities.invokeAndWait(() -> {
      proj.doAction(LogisimFileActions.addCircuit(c2));
      proj.doAction(LogisimFileActions.addCircuit(c3));
      proj.setCurrentCircuit(c2); // Select middle circuit "c2"
    });

    assertMoveState(proj, frame, 1, 3);

    // Move c2 UP
    SwingUtilities.invokeAndWait(() -> ProjectCircuitActions.doMoveCircuit(proj, proj.getCurrentCircuit(), -1));
    // Selection must follow c2 to index 0
    assertEquals(c2, proj.getCurrentCircuit());
    assertMoveState(proj, frame, 0, 3);

    // Verify displayed order is now [c2, c1, c3]
    List<Circuit> circuits = ProjectCircuitActions.getDisplayedCircuits(proj);
    assertEquals("c2", circuits.get(0).getName());
    assertEquals(c1.getName(), circuits.get(1).getName());
    assertEquals("c3", circuits.get(2).getName());

    // Save and assert saved file order matches
    File tmp = File.createTempFile("stage23_test", ".circ");
    tmp.deleteOnExit();
    SwingUtilities.invokeAndWait(() -> assertTrue(ProjectActions.doSave(proj, tmp)));

    Loader loader = new Loader(frame);
    var loadedFile = loader.openLogisimFile(tmp);
    List<Circuit> loadedCircuits = loadedFile.getCircuits();
    assertEquals("c2", loadedCircuits.get(0).getName());
    assertEquals(c1.getName(), loadedCircuits.get(1).getName());
    assertEquals("c3", loadedCircuits.get(2).getName());

    // Move c2 DOWN
    SwingUtilities.invokeAndWait(() -> ProjectCircuitActions.doMoveCircuit(proj, proj.getCurrentCircuit(), 1));
    assertEquals(c2, proj.getCurrentCircuit());
    assertMoveState(proj, frame, 1, 3);

    // Move c2 DOWN again to bottom
    SwingUtilities.invokeAndWait(() -> ProjectCircuitActions.doMoveCircuit(proj, proj.getCurrentCircuit(), 1));
    assertEquals(c2, proj.getCurrentCircuit());
    assertMoveState(proj, frame, 2, 3);

    // Rapid repeated clicks: clicking DOWN when already last should not change state or throw
    SwingUtilities.invokeAndWait(() -> {
      for (int i = 0; i < 5; i++) {
        ProjectCircuitActions.doMoveCircuit(proj, proj.getCurrentCircuit(), 1);
      }
    });
    assertEquals(c2, proj.getCurrentCircuit());
    assertMoveState(proj, frame, 2, 3);

    // Rename circuit: rename c2 to "c2_renamed"
    com.cburch.logisim.circuit.CircuitMutation x = new com.cburch.logisim.circuit.CircuitMutation(c2);
    x.setForCircuit(com.cburch.logisim.circuit.CircuitAttributes.NAME_ATTR, "c2_renamed");
    SwingUtilities.invokeAndWait(() -> proj.doAction(x.toAction(null)));
    assertEquals("c2_renamed", proj.getCurrentCircuit().getName());
    assertMoveState(proj, frame, 2, 3);

    // Remove circuit: remove c3
    SwingUtilities.invokeAndWait(() -> proj.doAction(LogisimFileActions.removeCircuit(c3)));
    assertMoveState(proj, frame, 1, 2);
  }
}
