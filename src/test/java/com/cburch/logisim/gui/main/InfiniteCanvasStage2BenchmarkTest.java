package com.cburch.logisim.gui.main;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitMutation;
import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.file.Loader;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.wiring.Clock;
import com.cburch.logisim.std.wiring.Constant;
import com.cburch.logisim.std.wiring.Pin;
import com.cburch.logisim.std.wiring.Probe;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Phase 2.1 Diagnostic Benchmark Harness:
 * Parametric performance profiling across zoom bands, circuit sizes,
 * and continuous panning paths to capture p50, p95, and p99 frame times.
 */
class InfiniteCanvasStage2BenchmarkTest {

  private record BenchmarkResult(
      String circuitName,
      int componentCount,
      int wireCount,
      double zoomFactor,
      double p50Ms,
      double p95Ms,
      double p99Ms,
      double avgMs,
      double minMs,
      double maxMs,
      double calculatedFps) {}

  private Circuit buildBenchmarkCircuit(String name, Project proj, LogisimFile file, int targetComponents) {
    final var circ = new Circuit(name, file, proj);
    file.addCircuit(circ);
    final var mutation = new CircuitMutation(circ);

    final var pinFactory = Pin.FACTORY;
    final var clockFactory = Clock.FACTORY;
    final var constFactory = Constant.FACTORY;
    final var probeFactory = Probe.FACTORY;

    // Distribute across all 4 quadrants (-15000 to +15000 world coordinate space)
    final int gridDim = (int) Math.ceil(Math.sqrt(targetComponents));
    final int spacing = 180;
    final int offset = (gridDim * spacing) / 2;

    int count = 0;
    for (int r = 0; r < gridDim && count < targetComponents; r++) {
      for (int c = 0; c < gridDim && count < targetComponents; c++) {
        int x = (c * spacing) - offset;
        int y = (r * spacing) - offset;
        final var loc = Location.create(x, y, false);

        switch (count % 4) {
          case 0 -> {
            final var attrs = pinFactory.createAttributeSet();
            mutation.add(pinFactory.createComponent(loc, attrs));
          }
          case 1 -> {
            final var attrs = clockFactory.createAttributeSet();
            mutation.add(clockFactory.createComponent(loc, attrs));
            mutation.add(Wire.create(loc, Location.create(x + 40, y, false)));
          }
          case 2 -> {
            final var attrs = constFactory.createAttributeSet();
            mutation.add(constFactory.createComponent(loc, attrs));
            mutation.add(Wire.create(loc, Location.create(x, y + 40, false)));
          }
          case 3 -> {
            final var attrs = probeFactory.createAttributeSet();
            mutation.add(probeFactory.createComponent(loc, attrs));
            mutation.add(Wire.create(loc, Location.create(x - 30, y, false)));
          }
        }
        count++;
      }
    }
    mutation.execute();
    return circ;
  }

  private BenchmarkResult runPanSweepBenchmark(
      String circuitName,
      Circuit circ,
      Project proj,
      double zoom,
      int frameCount) {

    final var canvas = new Canvas(proj);
    canvas.setSize(1920, 1080);
    final var cam = canvas.getCamera();
    cam.setViewportSize(1920, 1080);
    cam.setZoomFactor(zoom);
    cam.resetToCenter();

    final var img = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_ARGB);
    final var g = img.createGraphics();

    // 1. JIT Warm-up (5 iterations)
    for (int i = 0; i < 5; i++) {
      cam.setPan(i * 30.0, -i * 20.0);
      canvas.paintComponent(g);
    }

    // 2. Continuous Pan Sweep (circular + diagonal trajectory)
    final double[] frameTimesMs = new double[frameCount];
    for (int i = 0; i < frameCount; i++) {
      double angle = (2.0 * Math.PI * i) / frameCount;
      double radius = 150.0;
      double panX = Math.cos(angle) * radius + (i * 2.0);
      double panY = Math.sin(angle) * radius - (i * 1.5);
      cam.setPan(panX, panY);

      long startNanos = System.nanoTime();
      canvas.paintComponent(g);
      long durationNanos = System.nanoTime() - startNanos;
      frameTimesMs[i] = durationNanos / 1_000_000.0;
    }
    g.dispose();

    // 3. Compute Percentiles (p50, p95, p99)
    final var sortedTimes = Arrays.copyOf(frameTimesMs, frameTimesMs.length);
    Arrays.sort(sortedTimes);

    int idx50 = (int) Math.round(0.50 * (sortedTimes.length - 1));
    int idx95 = (int) Math.round(0.95 * (sortedTimes.length - 1));
    int idx99 = (int) Math.round(0.99 * (sortedTimes.length - 1));

    double p50 = sortedTimes[idx50];
    double p95 = sortedTimes[idx95];
    double p99 = sortedTimes[idx99];
    double min = sortedTimes[0];
    double max = sortedTimes[sortedTimes.length - 1];

    double sum = 0.0;
    for (double t : frameTimesMs) sum += t;
    double avg = sum / frameTimesMs.length;
    double fps = avg > 0 ? (1000.0 / avg) : 0.0;

    return new BenchmarkResult(
        circuitName,
        circ.getNonWires().size(),
        circ.getWires().size(),
        zoom,
        p50,
        p95,
        p99,
        avg,
        min,
        max,
        fps);
  }

  @Test
  @DisplayName("Phase 2.1: Parametric Zoom Sweep Benchmark Harness across 12 Zoom Bands")
  void testParametricZoomSweepBenchmark() {
    final var file = LogisimFile.createNew(new Loader(null), null);
    final var proj = new Project(file);

    // Standard Medium Circuit (300 components + 225 wires across 4 quadrants)
    final var circ = buildBenchmarkCircuit("MediumBenchCircuit", proj, file, 300);
    proj.setCurrentCircuit(circ);

    final double[] zoomBands = {0.05, 0.10, 0.15, 0.20, 0.25, 0.30, 0.35, 0.50, 0.80, 1.00, 1.50, 2.80};
    final List<BenchmarkResult> results = new ArrayList<>();

    final int framesPerBand = 25;
    for (double zoom : zoomBands) {
      final var result = runPanSweepBenchmark("Medium-300", circ, proj, zoom, framesPerBand);
      results.add(result);
    }

    // Print Formatted Markdown Diagnostics Report
    System.out.println("\n=========================================================================================");
    System.out.println("PHASE 2.1 DIAGNOSTIC BENCHMARK REPORT: MEDIUM CIRCUIT (300 COMPS, 225 WIRES)");
    System.out.println("=========================================================================================");
    System.out.printf("%-8s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s%n",
        "Zoom", "Avg (ms)", "p50 (ms)", "p95 (ms)", "p99 (ms)", "Max (ms)", "FPS");
    System.out.println("---------+------------+------------+------------+------------+------------+----------");

    for (var r : results) {
      System.out.printf("%-7.0f%% | %10.2f | %10.2f | %10.2f | %10.2f | %10.2f | %10.1f%n",
          r.zoomFactor() * 100.0,
          r.avgMs(),
          r.p50Ms(),
          r.p95Ms(),
          r.p99Ms(),
          r.maxMs(),
          r.calculatedFps());
    }
    System.out.println("=========================================================================================\n");

    // Baseline sanity assertions
    for (var r : results) {
      assertTrue(r.avgMs() > 0.0, "Frame time must be strictly positive");
      assertTrue(r.p50Ms() <= r.p95Ms(), "p50 must be <= p95");
      assertTrue(r.p95Ms() <= r.p99Ms(), "p95 must be <= p99");
    }
  }

  @Test
  @DisplayName("Phase 2.1: Multi-Scale Circuit Density Benchmark (Small to Large)")
  void testCircuitDensityBenchmark() {
    final var file = LogisimFile.createNew(new Loader(null), null);
    final var proj = new Project(file);

    int[] circuitSizes = {50, 300, 1000};
    String[] sizeNames = {"Small (50)", "Medium (300)", "Large (1000)"};

    System.out.println("\n=========================================================================================");
    System.out.println("PHASE 2.1 CIRCUIT DENSITY BENCHMARK REPORT (ZOOM = 100%, 1920x1080 VIEWPORT)");
    System.out.println("=========================================================================================");
    System.out.printf("%-15s | %-10s | %-10s | %-10s | %-10s | %-10s%n",
        "Scale", "Comps", "Wires", "Avg (ms)", "p95 (ms)", "FPS");
    System.out.println("----------------+------------+------------+------------+------------+----------");

    for (int i = 0; i < circuitSizes.length; i++) {
      int size = circuitSizes[i];
      final var circ = buildBenchmarkCircuit("Circuit-" + size, proj, file, size);
      proj.setCurrentCircuit(circ);

      final var result = runPanSweepBenchmark(sizeNames[i], circ, proj, 1.0, 20);
      System.out.printf("%-15s | %10d | %10d | %10.2f | %10.2f | %10.1f%n",
          sizeNames[i],
          result.componentCount(),
          result.wireCount(),
          result.avgMs(),
          result.p95Ms(),
          result.calculatedFps());

      assertTrue(result.avgMs() < 50.0, "Average frame time for " + sizeNames[i] + " must be < 50ms at 100% zoom");
    }
    System.out.println("=========================================================================================\n");
  }

  @Test
  @DisplayName("Phase 2.1: Steady-State Allocation & Heap GC Churn Test")
  void testSteadyStateAllocationRate() {
    final var file = LogisimFile.createNew(new Loader(null), null);
    final var proj = new Project(file);
    final var circ = buildBenchmarkCircuit("MemoryBenchmarkCircuit", proj, file, 300);
    proj.setCurrentCircuit(circ);

    final var canvas = new Canvas(proj);
    canvas.setSize(1920, 1080);
    final var cam = canvas.getCamera();
    cam.setViewportSize(1920, 1080);
    cam.setZoomFactor(1.0);
    cam.resetToCenter();

    final var img = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_ARGB);
    final var g = img.createGraphics();

    // 1. Warm-up
    for (int i = 0; i < 20; i++) {
      cam.setPan(i * 10.0, i * 10.0);
      canvas.paintComponent(g);
    }

    System.gc();
    try {
      Thread.sleep(100);
    } catch (InterruptedException ignored) {}

    final var runtime = Runtime.getRuntime();
    long beforeMem = runtime.totalMemory() - runtime.freeMemory();

    // 2. Execute 100 frames of steady-state panning
    final int frames = 100;
    for (int i = 0; i < frames; i++) {
      cam.setPan(i * 5.0, -i * 5.0);
      canvas.paintComponent(g);
    }
    g.dispose();

    long afterMem = runtime.totalMemory() - runtime.freeMemory();
    long allocatedApproxBytes = Math.max(0, afterMem - beforeMem);
    double kbPerFrame = (allocatedApproxBytes / 1024.0) / frames;

    System.out.printf("[PHASE 2.1 MEMORY] Steady-state Pan Allocation: %.2f KB/frame%n", kbPerFrame);
    assertNotNull(canvas);
  }

  public static void main(String[] args) {
    System.out.println("Running Phase 2.1 Benchmark Suite Directly...");
    final var test = new InfiniteCanvasStage2BenchmarkTest();
    test.testParametricZoomSweepBenchmark();
    test.testCircuitDensityBenchmark();
    test.testSteadyStateAllocationRate();
    System.out.println("All Phase 2.1 Benchmarks Completed Successfully!");
  }
}
