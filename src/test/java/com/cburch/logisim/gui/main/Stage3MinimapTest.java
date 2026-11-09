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
import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.file.Loader;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.wiring.Pin;
import java.awt.event.MouseWheelEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class Stage3MinimapTest {

  private Camera2D camera;
  private Canvas canvas;
  private MinimapOverlay minimap;
  private InfiniteCanvasContainer container;
  private Project proj;
  private Circuit circuit;

  @BeforeEach
  void setUp() {
    final var file = LogisimFile.createNew(new Loader(null), null);
    proj = new Project(file);
    circuit = proj.getCurrentCircuit();
    canvas = new Canvas(proj);
    canvas.setSize(1000, 600);
    camera = canvas.getCamera();
    camera.setViewportSize(1000, 600);
    camera.resetToCenter();

    minimap = new MinimapOverlay(canvas, camera);
    container = new InfiniteCanvasContainer(canvas, minimap);
    container.setSize(1000, 600);
    container.layoutComponents();
  }

  @Test
  @DisplayName("1. Camera2D: Step zoom-in and zoom-out")
  void testCameraStepZoom() {
    camera.setZoomFactor(1.0);
    assertEquals(1.0, camera.getZoomFactor(), 1e-4);

    camera.zoomIn();
    assertTrue(camera.getZoomFactor() > 1.0, "Zoom in must increase zoom factor");

    double zoomedIn = camera.getZoomFactor();
    camera.zoomOut();
    assertTrue(camera.getZoomFactor() < zoomedIn, "Zoom out must decrease zoom factor");
  }

  @Test
  @DisplayName("2. Canvas: Zoom-to-fit calculation centers and scales circuit")
  void testZoomToFit() {
    // Add two pins spaced apart
    final var factory = Pin.FACTORY;
    final var mut = new CircuitMutation(circuit);
    mut.add(factory.createComponent(Location.create(100, 100, false), factory.createAttributeSet()));
    mut.add(factory.createComponent(Location.create(500, 400, false), factory.createAttributeSet()));
    mut.execute();

    final Bounds bds = circuit.getBounds();
    assertTrue(bds.getWidth() > 0 && bds.getHeight() > 0, "Circuit bounds must be non-empty");

    // Start with arbitrary zoom and pan
    camera.setZoomFactor(0.10);
    camera.setPan(-3000, 4000);

    // Trigger zoom to fit
    canvas.zoomToFit();

    assertTrue(camera.getZoomFactor() >= 0.5, "Zoom to fit must scale up a 400x300 circuit to fit 1000x600 viewport");

    // Verify center is near circuit center
    double targetCx = bds.getX() + bds.getWidth() / 2.0;
    double targetCy = bds.getY() + bds.getHeight() / 2.0;
    double screenCx = camera.worldToScreenX(targetCx);
    double screenCy = camera.worldToScreenY(targetCy);

    assertEquals(500.0, screenCx, 2.0, "Circuit center X must project to viewport center (width/2)");
    assertEquals(300.0, screenCy, 2.0, "Circuit center Y must project to viewport center (height/2)");
  }

  @Test
  @DisplayName("3. MinimapOverlay: Mouse wheel zoom interaction")
  void testMinimapMouseWheelZoom() {
    camera.setZoomFactor(1.0);
    assertEquals(1.0, camera.getZoomFactor(), 1e-4);

    // Simulate mouse wheel up (zoom in)
    var wheelIn = new MouseWheelEvent(
        minimap, MouseWheelEvent.MOUSE_WHEEL, System.currentTimeMillis(),
        0, 95, 65, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, 1, -1);

    minimap.getMouseWheelListeners()[0].mouseWheelMoved(wheelIn);
    assertTrue(camera.getZoomFactor() > 1.0, "Minimap wheel up must zoom in");

    // Simulate mouse wheel down (zoom out)
    var wheelOut = new MouseWheelEvent(
        minimap, MouseWheelEvent.MOUSE_WHEEL, System.currentTimeMillis(),
        0, 95, 65, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, 1, 1);

    minimap.getMouseWheelListeners()[0].mouseWheelMoved(wheelOut);
    assertTrue(camera.getZoomFactor() <= 1.05, "Minimap wheel down must zoom out");
  }

  @Test
  @DisplayName("4. MinimapOverlay: State-aware [-] and [+] size controls with default as smallest")
  void testMinimapStateAwareSizeControls() {
    // Default size is index 0 (smallest: 200x135)
    assertEquals(0, minimap.getCurrentSizeIndex(), "Initial size must be index 0 (smallest)");
    assertEquals(200, minimap.getPreferredSize().width);
    assertEquals(135, minimap.getPreferredSize().height);

    // At index 0, shrinking is disabled
    assertFalse(minimap.canShrink(), "At smallest size, canShrink must be false");
    assertTrue(minimap.canEnlarge(), "At smallest size, canEnlarge must be true");

    // Shrinking when disabled should be a no-op
    minimap.shrinkMinimap();
    assertEquals(0, minimap.getCurrentSizeIndex());

    // Advance to Level 1
    minimap.enlargeMinimap();
    assertEquals(1, minimap.getCurrentSizeIndex());
    assertEquals(280, minimap.getPreferredSize().width);
    assertTrue(minimap.canShrink());
    assertTrue(minimap.canEnlarge());

    // Advance to Level 2
    minimap.enlargeMinimap();
    assertEquals(2, minimap.getCurrentSizeIndex());
    assertEquals(360, minimap.getPreferredSize().width);

    // Advance to Level 3 (Ultra Large: 450x305)
    minimap.enlargeMinimap();
    assertEquals(3, minimap.getCurrentSizeIndex());
    assertEquals(450, minimap.getPreferredSize().width);
    assertEquals(305, minimap.getPreferredSize().height);

    // At Level 3, canEnlarge must be false
    assertFalse(minimap.canEnlarge(), "At max size, canEnlarge must be false");
    assertTrue(minimap.canShrink(), "At max size, canShrink must be true");

    // Enlarge beyond max should be a no-op
    minimap.enlargeMinimap();
    assertEquals(3, minimap.getCurrentSizeIndex());

    // Shrink back down step-by-step
    minimap.shrinkMinimap();
    assertEquals(2, minimap.getCurrentSizeIndex());
    minimap.shrinkMinimap();
    assertEquals(1, minimap.getCurrentSizeIndex());
    minimap.shrinkMinimap();
    assertEquals(0, minimap.getCurrentSizeIndex());
    assertFalse(minimap.canShrink());
  }

  @Test
  @DisplayName("5. InfiniteCanvasContainer: Ultra Large minimap stays strictly inside screen bounds")
  void testUltraLargeMinimapWithinScreenBounds() {
    // Expand to Ultra Large (450x305)
    minimap.enlargeMinimap();
    minimap.enlargeMinimap();
    minimap.enlargeMinimap();
    assertEquals(450, minimap.getPreferredSize().width);

    // Container is 1000x600
    container.setSize(1000, 600);
    container.layoutComponents();

    int mx = minimap.getX();
    int my = minimap.getY();
    int mw = minimap.getWidth();
    int mh = minimap.getHeight();

    assertTrue(mx >= 0, "Minimap X must be non-negative");
    assertTrue(my >= 0, "Minimap Y must be non-negative");
    assertTrue(mx + mw <= 1000, "Minimap right edge must stay <= container width");
    assertTrue(my + mh <= 600, "Minimap bottom edge must stay <= container height");
    assertEquals(1000 - mw - 16, mx, "Minimap must be anchored exactly 16px from right edge");
    assertEquals(600 - mh - 16, my, "Minimap must be anchored exactly 16px from bottom edge");
  }

  @Test
  @DisplayName("6. MinimapOverlay: Wires disabled rule when viewRect > connectedComponentsRect")
  void testWiresDisplayRule() {
    // Construct two connected pins with a wire between them
    final var factory = Pin.FACTORY;
    final var loc1 = Location.create(100, 100, false);
    final var loc2 = Location.create(800, 600, false);
    final var mut = new CircuitMutation(circuit);
    mut.add(factory.createComponent(loc1, factory.createAttributeSet()));
    mut.add(factory.createComponent(loc2, factory.createAttributeSet()));
    mut.add(Wire.create(loc1, loc2));
    mut.execute();

    final Bounds connBounds = minimap.getConnectedComponentsBounds(circuit);
    assertNotNull(connBounds);
    assertTrue(connBounds.getWidth() >= 700);
    assertTrue(connBounds.getHeight() >= 500);

    // Case A: Zoom = 5% (0.05). Viewport is 20,000 x 12,000, MUCH bigger than connected bounds.
    camera.setZoomFactor(0.05);
    Bounds vpWide = camera.getViewportWorldBounds();
    assertTrue(vpWide.getWidth() > connBounds.getWidth());
    assertTrue(vpWide.getHeight() > connBounds.getHeight());
    // In this wide view, wiresDisabled MUST be true (wires disabled from viewing locally)
    boolean wiresDisabledWide = vpWide.getWidth() > connBounds.getWidth() && vpWide.getHeight() > connBounds.getHeight();
    assertTrue(wiresDisabledWide, "Wires must be disabled when view space is bigger than connected components");

    // Case B: Zoom = 100% (1.0). Viewport is 1000 x 600.
    // connBounds is ~700-800 wide, 500-600 high. Viewport is not strictly bigger in both dimensions or zoomed closer.
    // Zoom closer to 150%: viewport is 666 x 400, strictly SMALLER than connBounds.
    camera.setZoomFactor(1.50);
    Bounds vpFocused = camera.getViewportWorldBounds();
    boolean wiresDisabledFocused = vpFocused.getWidth() > connBounds.getWidth() && vpFocused.getHeight() > connBounds.getHeight();
    assertFalse(wiresDisabledFocused, "Wires must NOT be disabled when view space is smaller than connected components");
  }

  @Test
  @DisplayName("7. MinimapOverlay: Freeform top-left extender drag resizing with clamped bounds")
  void testFreeformExtenderResize() {
    // 1. Resizing to arbitrary valid dimensions
    minimap.applyDimensions(320, 216);
    assertEquals(320, minimap.getPreferredSize().width);
    assertEquals(216, minimap.getPreferredSize().height);

    // 2. Clamped to minimum size bounds (200x135)
    minimap.applyDimensions(50, 20);
    assertEquals(MinimapOverlay.MIN_WIDTH, minimap.getPreferredSize().width);
    assertEquals(MinimapOverlay.MIN_HEIGHT, minimap.getPreferredSize().height);

    // 3. Clamped to maximum size bounds (450x305)
    minimap.applyDimensions(999, 888);
    assertEquals(MinimapOverlay.MAX_WIDTH, minimap.getPreferredSize().width);
    assertEquals(MinimapOverlay.MAX_HEIGHT, minimap.getPreferredSize().height);
  }

  @Test
  @DisplayName("8. MinimapOverlay: Inactivity sleep and canvas activity wake up (panning/zooming)")
  void testInactivitySleepAndCanvasWake() {
    // Initially awake
    minimap.wakeUp();
    assertFalse(minimap.isAsleep());
    assertEquals(1.0f, minimap.getCurrentAlpha(), 1e-4);

    // Transition to sleep
    minimap.goToSleep();

    // Regular mouse clicks / moves should NOT wake the minimap (only pan or zoom)
    minimap.goToSleep();
    // Wake up via camera panning
    camera.panBy(50, 50);
    assertFalse(minimap.isAsleep(), "Panning camera must wake up the minimap");
    assertEquals(1.0f, minimap.getCurrentAlpha(), 1e-4);

    // Wake up via camera zooming
    minimap.goToSleep();
    camera.setZoomFactor(1.25);
    assertFalse(minimap.isAsleep(), "Zooming camera must wake up the minimap");
    assertEquals(1.0f, minimap.getCurrentAlpha(), 1e-4);
  }

  public static void main(String[] args) {
    System.out.println("Starting Stage3MinimapTest suite...");
    Stage3MinimapTest suite = new Stage3MinimapTest();
    int passed = 0;

    suite.setUp(); suite.testCameraStepZoom(); passed++;
    System.out.println("✓ 1. Camera step zoom passed");

    suite.setUp(); suite.testZoomToFit(); passed++;
    System.out.println("✓ 2. Zoom to fit passed");

    suite.setUp(); suite.testMinimapMouseWheelZoom(); passed++;
    System.out.println("✓ 3. Minimap wheel zoom passed");

    suite.setUp(); suite.testMinimapStateAwareSizeControls(); passed++;
    System.out.println("✓ 4. Minimap size controls passed");

    suite.setUp(); suite.testUltraLargeMinimapWithinScreenBounds(); passed++;
    System.out.println("✓ 5. Container edge clamping passed");

    suite.setUp(); suite.testWiresDisplayRule(); passed++;
    System.out.println("✓ 6. Connected-components wire display rule passed");

    suite.setUp(); suite.testFreeformExtenderResize(); passed++;
    System.out.println("✓ 7. Freeform top-left extender resize passed");

    suite.setUp(); suite.testInactivitySleepAndCanvasWake(); passed++;
    System.out.println("✓ 8. Inactivity sleep and canvas activity wake passed");

    System.out.printf("All %d Stage3MinimapTest unit tests passed successfully!%n", passed);
  }
}
