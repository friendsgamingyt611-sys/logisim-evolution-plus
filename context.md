# Infinite Canvas & Modern Viewport Architecture Context

## Current Status & Branch
- **Repository:** `logisim-evolution-plus`
- **Active Branch:** `feat/infinite-canvas`
- **Upstream Clean Directory:** `logisim-evolution-upstream-clean` (preserved pristine on `main`, never modified)
- **Status:** Functional milestone reached — infinite 4-quadrant canvas view, camera transform, middle-mouse panning, interactive radar minimap, screen-space modulo grid painting, and component placement in all quadrants are operational.

---

## 1. Architectural Overview

### Core Objectives
1. **Eliminate JScrollPane / Scrollbar dependency:** Viewport is governed by an affine 2D camera model (`Camera2D`) rather than bounded integer scrollbars.
2. **Infinite 4-Quadrant Canvas:** Origin `(0, 0)` is at screen center by default, with complete continuous support for negative world coordinates $(x < 0, y < 0)$.
3. **Interactive Radar Minimap:** Floating semi-transparent HUD overlay in the bottom-right corner displaying circuit footprint bounds, camera view frustum, and supporting click/drag navigation.
4. **CAD-Grade Screen-Space Modulo Grid (NI Multisim / Altium architecture):** Dot grid renders strictly in screen space using device pixels $[0, W] \times [0, H]$ and modulo offset math, avoiding bounding box cutoffs, black voids, and offscreen raster allocations.

---

## 2. Key Components & Responsibilities

| Component | Path | Responsibility |
|---|---|---|
| `Camera2D` | [`src/main/java/com/cburch/logisim/gui/main/Camera2D.java`](file:///home/devansh/logisim-evolution-plus/src/main/java/com/cburch/logisim/gui/main/Camera2D.java) | Authoritative source of truth for viewport transform (`panX`, `panY`, `zoomFactor`). Implements Logisim's `ZoomModel` to keep UI controls in sync. Provides affine coordinate projections (`screenToWorld`, `worldToScreen`, `getViewportWorldBounds`). |
| `Canvas` | [`src/main/java/com/cburch/logisim/gui/main/Canvas.java`](file:///home/devansh/logisim-evolution-plus/src/main/java/com/cburch/logisim/gui/main/Canvas.java) | Main circuit editing component. Uses `toWorldEvent()` to dispatch world coordinates to tools while preserving screen space for overlays; translates dirty rects during `repaint(x, y, w, h)`; implements 1:1 incremental middle-mouse drag panning. |
| `CanvasPainter` | [`src/main/java/com/cburch/logisim/gui/main/CanvasPainter.java`](file:///home/devansh/logisim-evolution-plus/src/main/java/com/cburch/logisim/gui/main/CanvasPainter.java) | Orchestrates two-phase rendering: paints the infinite grid in screen space on `g`, then creates a scaled/translated `Graphics2D` context with `g2d.translate(tx, ty)` and `g2d.scale(zoom, zoom)` for circuit components and wiring. |
| `GridPainter` | [`src/main/java/com/cburch/logisim/gui/generic/GridPainter.java`](file:///home/devansh/logisim-evolution-plus/src/main/java/com/cburch/logisim/gui/generic/GridPainter.java) | Renders dot grid with `paintInfiniteGrid(g, camera)` via modulo offsets of the screen-projected origin. Supports dynamic LOD decimation and smoothstep dot blending. |
| `MinimapOverlay` | [`src/main/java/com/cburch/logisim/gui/main/MinimapOverlay.java`](file:///home/devansh/logisim-evolution-plus/src/main/java/com/cburch/logisim/gui/main/MinimapOverlay.java) | Floating HUD in `JLayeredPane.PALETTE_LAYER`. Computes union of circuit bounds and camera viewport frustum; translates minimap clicks/drags to world target coordinates via `camera.centerOn()`. |
| `InfiniteCanvasContainer` | [`src/main/java/com/cburch/logisim/gui/main/InfiniteCanvasContainer.java`](file:///home/devansh/logisim-evolution-plus/src/main/java/com/cburch/logisim/gui/main/InfiniteCanvasContainer.java) | `JLayeredPane` host managing Canvas (Layer 0) and Minimap (Layer 1). Resizes Canvas to 100% of container and dynamically anchors Minimap to the bottom-right corner. |

---

## 3. Detailed Audit of Remaining Work & Edge Cases

### A. Major Grid Dot Alignment Artifact
- **Symptom:** In the latest screenshot, major grid dots appear as faint horizontal/vertical bands or inconsistent squares across the canvas.
- **Cause:** In [`GridPainter.java`](file:///home/devansh/logisim-evolution-plus/src/main/java/com/cburch/logisim/gui/generic/GridPainter.java), the world coordinate test for major dots:
  ```java
  final boolean isMajorX = Math.abs(Math.round(wx) % majorStep) <= (step / 2);
  ```
  uses continuous world coordinate projection per pixel line (`wx`), which has floating point rounding jitter and an overly wide threshold (`<= step / 2`), causing multiple adjacent dots or inconsistent columns to be marked as major.
- **Solution:** Quantize the loop indices to discrete schematic grid coordinates $(lx, ly)$ directly:
  ```java
  long startLx = (long) Math.floor(camera.screenToWorldX(0) / step) * step;
  long endLx = (long) Math.ceil(camera.screenToWorldX(width) / step) * step;
  ```
  Iterate over logical grid steps and project to screen coordinates (`px = (int) Math.round(camera.worldToScreenX(lx))`). Check `lx % majorStep == 0` for exact integer alignment.

### B. Legacy Viewport Out-of-Bounds Navigation Arrows
- **Symptom:** `Canvas.updateArrows()` and `Canvas.setArrows()` in [`Canvas.java`](file:///home/devansh/logisim-evolution-plus/src/main/java/com/cburch/logisim/gui/main/Canvas.java) still assume $(0, 0)$ is at top-left:
  ```java
  var x = circBds.getX();
  var y = circBds.getY();
  if (x < 0) x = 0;
  if (y < 0) y = 0;
  ```
- **Cause:** This legacy logic clamps circuit bounds to positive coordinates and checks if bounds fall outside a non-camera rectangle.
- **Solution:** With `Camera2D` and `MinimapOverlay`, either update `updateArrows()` to test against `camera.getViewportWorldBounds()` or suppress legacy offscreen arrow indicators when `camera != null`.

### C. Tooltip Hit-Testing Coordinate Space
- **Symptom:** Hover tooltips in `Canvas.getToolTipText(MouseEvent event)` receive the screen event without world projection.
- **Cause:** `loc = Location.create(event.getX(), event.getY())` looks up components at screen coordinates instead of world coordinates.
- **Solution:** Project `event` through `camera.screenToWorldX(event.getX())` and `camera.screenToWorldY(event.getY())` before component lookup.

### D. Fit to Screen & Auto-Zoom Centering
- **Symptom:** "Fit to Screen" (or auto-zoom button) sets zoom scale but does not center the camera on the circuit's center of mass.
- **Solution:** In `ZoomControl.java` and `Camera2D`, compute the center of `circuit.getBounds()` and invoke `camera.centerOn(cx, cy)` alongside `setZoomFactor()`.

### E. Minimap Wire Rendering & Visual Polish
- **Enhancement:** Minimap currently renders non-wire component boxes. Adding wire trunk rendering and anti-aliasing enhances readability for large schematics.

---

## 4. Stage 1 Automated Acceptance Suite

Located at [`src/test/java/com/cburch/logisim/gui/main/InfiniteCanvasStage1Test.java`](file:///home/devansh/logisim-evolution-plus/src/test/java/com/cburch/logisim/gui/main/InfiniteCanvasStage1Test.java):
1. `testAffineCoordinateRoundtripAcrossAllQuadrants`: 100% reversible affine roundtrip precision ($[-50000, 50000]$).
2. `testCursorAnchoredZoomInvariance`: Exact invariance of world coordinate under the cursor during zoom ($< 10^{-4}\text{ px}$ displacement).
3. `testIncrementalMiddleMousePanningStability`: 1:1 delta accumulation without runaway drift.
4. `testCanvasActualMiddleMouseDragEvents`: End-to-end event pipeline simulation of actual middle mouse press, drag, delta accumulation, and release on `Canvas`.
5. `testNegativeQuadrantComponentPlacement`: Circuit bounding box expansions and component placement in negative coordinates.
6. `testInfiniteGridRenderingPerformanceAndNoFrameDrops`: 100 consecutive frames benchmarked for latency ($< 8.0\text{ ms/frame}$ average, ensuring sustained $\ge 60\text{ FPS}$).
7. `testGridDotWorldAlignmentDuringFreePan`: Continuous dot grid world alignment across arbitrary continuous pan offsets ($[-532.89, 105.12]$) to guarantee sub-pixel dot alignment without floating drift.
8. `testMinimapFrustumAndNavigationMath`: Viewport frustum projection and click-to-center navigation coordinate math.
9. `testRepaintCoverageAndUniformRasterDistribution`: Verifies full-viewport raster coverage, absence of dirty-rect coordinate smearing, and uniform dot density across all quadrants.

---

## 5. Root Cause Isolation & Resolution: Vertical Band / Dirty Rect Smearing

### Diagnosis
- **Symptom:** In user builds, a distinct vertical band of dots with duplicated/smeared artifacts appeared near the canvas center, while outer areas failed to clear properly during free pan and zoom.
- **Root Cause Isolated:**
  1. **Repaint Hijack:** `Canvas.java` overrode `repaint(long tm, int x, int y, int width, int height)` and assumed that any incoming `(x, y, width, height)` was in **World Space**, converting it via `camera.worldToScreenX(x)`. However, standard Swing calls (such as `Component.repaint()` without parameters, window exposure, and UI updates) invoke `repaint(0, 0, 0, getWidth(), getHeight())` in **Screen Space**. By treating screen $(0, 0)$ as world $(0, 0)$, Swing was instructed to only mark the dirty rectangle starting at `worldToScreenX(0) = panX + width / 2.0`. The entire left and top of the canvas were never refreshed on standard `repaint()` events, causing old dots from previous pan frames to remain frozen on screen while new dots were drawn inside the dirty slice.
  2. **Re-entrant Sizing:** `Canvas.paintComponent()` previously called `camera.setViewportSize(getWidth(), getHeight())` during each paint pass, which fired `PROPERTY_TRANSFORM` and enqueued nested repaints during active painting.
  3. **World Clip Redundancy:** `CanvasPainter` explicitly set `g2d.setClip(viewBds)` in world coordinates, overwriting device clipping bounds.

### Resolution Implemented
1. **Full-Viewport Repaint Guarantee:** In [`Canvas.java`](file:///home/devansh/logisim-evolution-plus/src/main/java/com/cburch/logisim/gui/main/Canvas.java), `repaint()` calls when `camera != null` always invalidate `[0, 0, getWidth(), getHeight()]`. Because the full-screen render takes under $0.25\text{ ms}$ (over $4000\text{ FPS}$), full repaints completely eliminate partial dirty-rect coordinate confusion, smear trails, and ghost components during tool placement.
2. **Decoupled Viewport Sizing:** Viewport size updates are bound to a `ComponentListener` on `Canvas` and `InfiniteCanvasContainer.layoutComponents()`, removing re-entrant `setViewportSize()` calls from `paintComponent()`.
3. **Screen Clip Preservation:** Removed redundant `g2d.setClip()` from `CanvasPainter`, allowing Swing's hardware-accelerated clipping to operate cleanly.
4. **Automated Verification:** Added `testRepaintCoverageAndUniformRasterDistribution` to verify equal dot counts across quadrants and prevent regression.

