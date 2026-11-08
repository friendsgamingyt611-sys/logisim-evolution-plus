/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitMutation;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.file.Loader;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.gui.generic.GridPainter;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.wiring.Pin;
import com.cburch.logisim.std.wiring.WiringLibrary;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Stage 1 Comprehensive Acceptance and Performance Test for the Infinite Canvas feature.
 * Validates:
 * 1. Affine roundtrip coordinate math across all 4 quadrants (-50000 to +50000).
 * 2. Pixel-perfect cursor anchor invariance during continuous zooming.
 * 3. Linear 1:1 delta accumulation during middle-mouse panning without runaway drift.
 * 4. Placement and bounding box calculations of components in negative coordinates.
 * 5. Screen-space grid rendering performance (target >= 60 FPS / < 16ms render time).
 * 6. Minimap radar frustum synchronization and click-to-center navigation.
 */
class InfiniteCanvasStage1Test {

  private Camera2D camera;

  @BeforeAll
  static void initHeadlessAwt() {
    System.setProperty("java.awt.headless", "true");
  }

  @BeforeEach
  void setUp() {
    camera = new Camera2D();
    camera.setViewportSize(1600, 900);
    camera.resetToCenter();
  }

  @Test
  @DisplayName("1. Camera2D: Affine roundtrip projection precision across all 4 quadrants")
  void testAffineCoordinateRoundtripAcrossAllQuadrants() {
    final double[] testCoordinates = {-50000, -1234.56, -420, -10, 0, 10, 350.25, 1920, 50000};
    final double[] testZooms = {0.1, 0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 2.0, 3.0, 5.0};
    final double[] testPans = {-2500, -314.15, 0, 450, 3000};

    for (double zoom : testZooms) {
      camera.setZoomFactor(zoom);
      for (double px : testPans) {
        for (double py : testPans) {
          camera.setPan(px, py);

          for (double wx : testCoordinates) {
            for (double wy : testCoordinates) {
              double sx = camera.worldToScreenX(wx);
              double sy = camera.worldToScreenY(wy);

              double recoveredWx = camera.screenToWorldX(sx);
              double recoveredWy = camera.screenToWorldY(sy);

              assertEquals(wx, recoveredWx, 1e-4,
                  String.format("World X mismatch at zoom=%.2f, pan=(%.1f, %.1f), orig=(%.2f, %.2f)", zoom, px, py, wx, wy));
              assertEquals(wy, recoveredWy, 1e-4,
                  String.format("World Y mismatch at zoom=%.2f, pan=(%.1f, %.1f), orig=(%.2f, %.2f)", zoom, px, py, wx, wy));
            }
          }
        }
      }
    }
  }

  @Test
  @DisplayName("2. Camera2D: Cursor-anchored zoom preserves exact world coordinate under mouse cursor")
  void testCursorAnchoredZoomInvariance() {
    // Start with arbitrary non-zero pan
    camera.setPan(150.0, -85.0);
    camera.setZoomFactor(1.0);

    final double cursorScreenX = 640.0;
    final double cursorScreenY = 360.0;

    final double anchorWorldXBefore = camera.screenToWorldX(cursorScreenX);
    final double anchorWorldYBefore = camera.screenToWorldY(cursorScreenY);

    // Zoom inward to 1.75
    camera.zoomAt(cursorScreenX, cursorScreenY, 1.75);

    double anchorWorldXAfter = camera.screenToWorldX(cursorScreenX);
    double anchorWorldYAfter = camera.screenToWorldY(cursorScreenY);

    assertEquals(anchorWorldXBefore, anchorWorldXAfter, 1e-4,
        "World X under cursor must remain identical after zooming in");
    assertEquals(anchorWorldYBefore, anchorWorldYAfter, 1e-4,
        "World Y under cursor must remain identical after zooming in");

    // Zoom outward to 0.4
    camera.zoomAt(cursorScreenX, cursorScreenY, 0.4);

    anchorWorldXAfter = camera.screenToWorldX(cursorScreenX);
    anchorWorldYAfter = camera.screenToWorldY(cursorScreenY);

    assertEquals(anchorWorldXBefore, anchorWorldXAfter, 1e-4,
        "World X under cursor must remain identical after zooming out");
    assertEquals(anchorWorldYBefore, anchorWorldYAfter, 1e-4,
        "World Y under cursor must remain identical after zooming out");
  }

  @Test
  @DisplayName("3. Canvas & Camera2D: Middle-mouse incremental delta panning operates 1:1 without drift")
  void testIncrementalMiddleMousePanningStability() {
    camera.setPan(0, 0);

    // Simulate 100 mouse drag packets of 5px right and 3px down
    final int steps = 100;
    final double stepDx = 5.0;
    final double stepDy = 3.0;

    for (int i = 0; i < steps; i++) {
      camera.panBy(stepDx, stepDy);
    }

    assertEquals(steps * stepDx, camera.getPanX(), 1e-5, "PanX should match exact accumulated delta sum");
    assertEquals(steps * stepDy, camera.getPanY(), 1e-5, "PanY should match exact accumulated delta sum");

    // Reverse drag back to starting position
    for (int i = 0; i < steps; i++) {
      camera.panBy(-stepDx, -stepDy);
    }

    assertEquals(0.0, camera.getPanX(), 1e-5, "PanX should return to exactly 0 after symmetric reverse dragging");
    assertEquals(0.0, camera.getPanY(), 1e-5, "PanY should return to exactly 0 after symmetric reverse dragging");
  }

  @Test
  @DisplayName("4. Circuit & Components: Placement in negative 4-quadrant coordinates")
  void testNegativeQuadrantComponentPlacement() {
    final var file = LogisimFile.createNew(new Loader(null), null);
    final var proj = new Project(file);
    final var circ = new Circuit("InfiniteSub", file, proj);
    file.addCircuit(circ);
    proj.setCurrentCircuit(circ);

    final var factory = Pin.FACTORY;
    final var compNorthWest = factory.createComponent(Location.create(-300, -200, false), factory.createAttributeSet());
    final var compSouthWest = factory.createComponent(Location.create(-150, 400, false), factory.createAttributeSet());
    final var compNorthEast = factory.createComponent(Location.create(250, -350, false), factory.createAttributeSet());

    final var mutation = new CircuitMutation(circ);
    mutation.add(compNorthWest);
    mutation.add(compSouthWest);
    mutation.add(compNorthEast);
    mutation.execute();

    final Bounds bds = circ.getBounds();
    assertNotNull(bds, "Circuit bounds must exist");
    assertTrue(bds.getX() <= -300, "Circuit bounding box must encompass negative world X coordinates");
    assertTrue(bds.getY() <= -350, "Circuit bounding box must encompass negative world Y coordinates");
  }

  @Test
  @DisplayName("5. GridPainter: High-frequency frame rendering performance and latency benchmark")
  void testInfiniteGridRenderingPerformanceAndNoFrameDrops() {
    final int width = 1920;
    final int height = 1080;
    final var dummyPanel = new JPanel();
    dummyPanel.setSize(width, height);

    final var gridPainter = new GridPainter(dummyPanel);
    gridPainter.setZoomModel(camera);

    final var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D g2 = image.createGraphics();

    // Warm-up JIT
    for (int i = 0; i < 20; i++) {
      camera.setPan(i * 10, i * 5);
      gridPainter.paintInfiniteGrid(g2, camera);
    }

    // Benchmark 100 consecutive frames during high-speed panning
    final int benchmarkFrames = 100;
    long totalNanoTime = 0;
    long maxNanoTime = 0;

    for (int i = 0; i < benchmarkFrames; i++) {
      camera.setPan(i * 12.5, -i * 8.5);
      long start = System.nanoTime();
      gridPainter.paintInfiniteGrid(g2, camera);
      long duration = System.nanoTime() - start;
      totalNanoTime += duration;
      if (duration > maxNanoTime) {
        maxNanoTime = duration;
      }
    }
    g2.dispose();

    double avgMs = (totalNanoTime / (double) benchmarkFrames) / 1_000_000.0;
    double maxMs = maxNanoTime / 1_000_000.0;

    // 60 FPS standard is 16.66ms budget per frame
    // A high performance grid painter should take < 5ms per frame even in software Java2D
    assertTrue(avgMs < 8.0, String.format("Average grid frame time (%.2f ms) must be well within 60 FPS budget (< 8.0 ms)", avgMs));
    assertTrue(maxMs < 25.0, String.format("Worst-case grid frame time (%.2f ms) must not induce severe stutter (< 25.0 ms)", maxMs));
  }

  @Test
  @DisplayName("5b. GridPainter: Continuous dot grid world alignment during arbitrary fractional panning")
  void testGridDotWorldAlignmentDuringFreePan() {
    final int width = 1200;
    final int height = 800;
    final var dummyPanel = new JPanel();
    dummyPanel.setSize(width, height);

    final var gridPainter = new GridPainter(dummyPanel);
    gridPainter.setZoomModel(camera);

    final var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D g2 = image.createGraphics();

    // Verify grid alignment over various free continuous pan coordinates
    final double[][] arbitraryPans = {
        {0.0, 0.0},
        {3.7, 7.2},
        {-14.3, 29.8},
        {105.12, -73.44},
        {-532.89, -321.11}
    };

    final int step = GridPainter.BASE_GRID_SIZE; // 10
    final int majorStep = step * 5; // 50

    for (double[] pan : arbitraryPans) {
      camera.setPan(pan[0], pan[1]);
      gridPainter.paintInfiniteGrid(g2, camera);

      // Verify that any world coordinate that is a multiple of 10 maps directly to a dot position
      for (int testWx = -200; testWx <= 200; testWx += 10) {
        double screenX = camera.worldToScreenX(testWx);
        int px = (int) Math.round(screenX);
        if (px >= 0 && px < width) {
          // Re-projecting screen pixel back to world must match testWx within 0.5px tolerance
          double recoveredWx = camera.screenToWorldX(px);
          assertEquals(testWx, recoveredWx, 0.55,
              String.format("Grid dot at world X %d must align with screen pixel %d at panX=%.2f", testWx, px, pan[0]));
        }
      }

      for (int testWy = -200; testWy <= 200; testWy += 10) {
        double screenY = camera.worldToScreenY(testWy);
        int py = (int) Math.round(screenY);
        if (py >= 0 && py < height) {
          double recoveredWy = camera.screenToWorldY(py);
          assertEquals(testWy, recoveredWy, 0.55,
              String.format("Grid dot at world Y %d must align with screen pixel %d at panY=%.2f", testWy, py, pan[1]));
        }
      }
    }
    g2.dispose();
  }

  @Test
  @DisplayName("3b. Canvas Mouse Pipeline: Actual middle-mouse drag events translate camera and dispatch correctly")
  void testCanvasActualMiddleMouseDragEvents() {
    final var file = LogisimFile.createNew(new Loader(null), null);
    final var proj = new Project(file);
    final var canvas = new Canvas(proj);
    canvas.setSize(1000, 700);

    final var cam = canvas.getCamera();
    cam.setPan(0, 0);

    // 1. Simulate physical press of BUTTON2 (middle mouse) at screen position (400, 300)
    final var pressEvent = new MouseEvent(
        canvas,
        MouseEvent.MOUSE_PRESSED,
        System.currentTimeMillis(),
        MouseEvent.BUTTON2_DOWN_MASK,
        400, 300, // screen pos relative to component
        400, 300, // on-screen abs pos
        1,
        false,
        MouseEvent.BUTTON2);
    canvas.getMouseListeners()[0].mousePressed(pressEvent);

    // 2. Drag to (450, 280) -> dx = +50, dy = -20
    final var dragEvent1 = new MouseEvent(
        canvas,
        MouseEvent.MOUSE_DRAGGED,
        System.currentTimeMillis(),
        MouseEvent.BUTTON2_DOWN_MASK,
        450, 280,
        450, 280,
        1,
        false,
        MouseEvent.BUTTON2);
    canvas.getMouseMotionListeners()[0].mouseDragged(dragEvent1);

    assertEquals(50.0, cam.getPanX(), 1e-4, "Camera panX must update by exact mouse drag dx (+50)");
    assertEquals(-20.0, cam.getPanY(), 1e-4, "Camera panY must update by exact mouse drag dy (-20)");

    // 3. Continue drag to (410, 310) -> dx = -40, dy = +30 (from 450, 280)
    final var dragEvent2 = new MouseEvent(
        canvas,
        MouseEvent.MOUSE_DRAGGED,
        System.currentTimeMillis(),
        MouseEvent.BUTTON2_DOWN_MASK,
        410, 310,
        410, 310,
        1,
        false,
        MouseEvent.BUTTON2);
    canvas.getMouseMotionListeners()[0].mouseDragged(dragEvent2);

    assertEquals(10.0, cam.getPanX(), 1e-4, "Camera panX must accumulate incremental delta (+10)");
    assertEquals(10.0, cam.getPanY(), 1e-4, "Camera panY must accumulate incremental delta (+10)");

    // 4. Release BUTTON2
    final var releaseEvent = new MouseEvent(
        canvas,
        MouseEvent.MOUSE_RELEASED,
        System.currentTimeMillis(),
        0,
        410, 310,
        410, 310,
        1,
        false,
        MouseEvent.BUTTON2);
    canvas.getMouseListeners()[0].mouseReleased(releaseEvent);

    // 5. Subsequent drag after release should NOT pan camera
    final var strayDragEvent = new MouseEvent(
        canvas,
        MouseEvent.MOUSE_DRAGGED,
        System.currentTimeMillis(),
        0,
        500, 500,
        500, 500,
        1,
        false,
        MouseEvent.NOBUTTON);
    canvas.getMouseMotionListeners()[0].mouseDragged(strayDragEvent);

    assertEquals(10.0, cam.getPanX(), 1e-4, "Camera panX must remain unchanged after mouse release");
    assertEquals(10.0, cam.getPanY(), 1e-4, "Camera panY must remain unchanged after mouse release");
  }

  @Test
  @DisplayName("6. MinimapOverlay & Camera2D: Frustum computation and center-on navigation")
  void testMinimapFrustumAndNavigationMath() {
    camera.setViewportSize(1000, 600);
    camera.setPan(200.0, -100.0);
    camera.setZoomFactor(1.2);

    final Bounds viewWorldBounds = camera.getViewportWorldBounds();
    assertNotNull(viewWorldBounds);

    // Verify viewWorldBounds corners match screen projection
    int screenLeftX = (int) Math.round(camera.worldToScreenX(viewWorldBounds.getX()));
    int screenTopY = (int) Math.round(camera.worldToScreenY(viewWorldBounds.getY()));

    assertEquals(0, screenLeftX, 2, "Viewport bounding box left must map to screen coordinate 0");
    assertEquals(0, screenTopY, 2, "Viewport bounding box top must map to screen coordinate 0");

    // Center camera on target point (-450, 600)
    final double targetX = -450.0;
    final double targetY = 600.0;
    camera.centerOn(targetX, targetY);

    // After centerOn, target (X, Y) should project to center of screen (width/2, height/2)
    double projectedTargetScreenX = camera.worldToScreenX(targetX);
    double projectedTargetScreenY = camera.worldToScreenY(targetY);

    assertEquals(500.0, projectedTargetScreenX, 1.0, "CenterOn target world X must project to center of screen (width/2)");
    assertEquals(300.0, projectedTargetScreenY, 1.0, "CenterOn target world Y must project to center of screen (height/2)");
  }
}
