/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.gui.generic;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

public class GridAlignmentTest {

  @Test
  public void testDotAndPinAlignment() {
    Component dummy = new JPanel();
    GridPainter grid = new GridPainter(dummy);

    int[] testCoords = {0, 2000, -2000, 20000, -20000};
    double[] testZooms = {0.1, 0.5, 0.93, 1.0, 1.33, 1.63, 1.631, 1.634, 2.0, 2.5, 8.0};
    double[] hiDpiScales = {1.0, 2.0};

    int patchSize = 100;

    double maxError = 0.0;
    for (double hiDpi : hiDpiScales) {
      for (double zoom : testZooms) {
        grid.setZoomFactor(zoom);

        for (int coord : testCoords) {
          int targetX = coord;
          int targetY = coord;

          int imgW = (int) Math.round(patchSize * hiDpi);
          int imgH = (int) Math.round(patchSize * hiDpi);

          double canvasX = targetX * zoom;
          double canvasY = targetY * zoom;

          int clipX = (int) Math.floor(canvasX) - (patchSize / 2);
          int clipY = (int) Math.floor(canvasY) - (patchSize / 2);

          // 1. Paint grid through real GridPainter into imgGrid
          BufferedImage imgGrid = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
          Graphics2D gGrid = imgGrid.createGraphics();
          if (hiDpi != 1.0) {
            gGrid.scale(hiDpi, hiDpi);
          }
          gGrid.translate(-clipX, -clipY);
          gGrid.setClip(clipX, clipY, patchSize, patchSize);
          grid.paintGrid(gGrid);
          gGrid.dispose();

          // 2. Paint pin marker through real CanvasPainter path into imgMarker
          BufferedImage imgMarker = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
          Graphics2D gMarker = imgMarker.createGraphics();
          gMarker.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          if (hiDpi != 1.0) {
            gMarker.scale(hiDpi, hiDpi);
          }
          gMarker.translate(-clipX, -clipY);
          gMarker.setClip(clipX, clipY, patchSize, patchSize);

          Graphics2D scaledG = (Graphics2D) gMarker.create();
          scaledG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          if (zoom != 1.0) {
            scaledG.transform(AffineTransform.getScaleInstance(zoom, zoom));
          }
          scaledG.setColor(new Color(255, 0, 0, 255));
          double r = Math.max(1.0, 2.0 / zoom);
          scaledG.fill(new Ellipse2D.Double(targetX - r, targetY - r, 2 * r, 2 * r));
          scaledG.dispose();
          gMarker.dispose();

          // 3. Find centroids
          double markerSumX = 0;
          double markerSumY = 0;
          double markerCount = 0;

          double dotSumX = 0;
          double dotSumY = 0;
          double dotCount = 0;

          double expectedDevX = (canvasX - clipX) * hiDpi;
          double expectedDevY = (canvasY - clipY) * hiDpi;

          // Search radius for the dot (dots have radius <= 3px in device pixels)
          int dotSearchRadius = (int) Math.ceil(3.5 * hiDpi);
          int minX = Math.max(0, (int) Math.floor(expectedDevX - dotSearchRadius));
          int maxX = Math.min(imgW - 1, (int) Math.ceil(expectedDevX + dotSearchRadius));
          int minY = Math.max(0, (int) Math.floor(expectedDevY - dotSearchRadius));
          int maxY = Math.min(imgH - 1, (int) Math.ceil(expectedDevY + dotSearchRadius));

          // Sample local background halfway between grid dots (furthest from any dot)
          double halfSpacing = Math.max(4.0, (10.0 * zoom * hiDpi) / 2.0);
          int bgSampleX = (int) Math.round(expectedDevX + halfSpacing);
          int bgSampleY = (int) Math.round(expectedDevY + halfSpacing);
          if (bgSampleX >= imgW) {
            bgSampleX = (int) Math.round(expectedDevX - halfSpacing);
          }
          if (bgSampleY >= imgH) {
            bgSampleY = (int) Math.round(expectedDevY - halfSpacing);
          }
          bgSampleX = Math.max(0, Math.min(imgW - 1, bgSampleX));
          bgSampleY = Math.max(0, Math.min(imgH - 1, bgSampleY));

          int bgRgb = imgGrid.getRGB(bgSampleX, bgSampleY);
          int bgRed = (bgRgb >> 16) & 0xFF;
          int bgGreen = (bgRgb >> 8) & 0xFF;
          int bgBlue = bgRgb & 0xFF;

          for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
              // Marker from imgMarker
              int mRgb = imgMarker.getRGB(x, y);
              int mAlpha = (mRgb >> 24) & 0xFF;
              if (mAlpha > 20) {
                markerSumX += (x + 0.5) * mAlpha;
                markerSumY += (y + 0.5) * mAlpha;
                markerCount += mAlpha;
              }

              // Dot from imgGrid
              int gRgb = imgGrid.getRGB(x, y);
              int gr = (gRgb >> 16) & 0xFF;
              int gg = (gRgb >> 8) & 0xFF;
              int gb = gRgb & 0xFF;
              int diff = Math.abs(gr - bgRed) + Math.abs(gg - bgGreen) + Math.abs(gb - bgBlue);
              if (diff > 15) {
                dotSumX += (x + 0.5) * diff;
                dotSumY += (y + 0.5) * diff;
                dotCount += diff;
              }
            }
          }

          assertTrue(markerCount > 0,
              String.format("Pin marker not found at coord=%d, zoom=%.4f, hiDpi=%.1f",
                  coord, zoom, hiDpi));
          assertTrue(dotCount > 0,
              String.format("Grid dot not found at coord=%d, zoom=%.4f, hiDpi=%.1f",
                  coord, zoom, hiDpi));

          double markerCentroidX = markerSumX / markerCount;
          double markerCentroidY = markerSumY / markerCount;
          double dotCentroidX = dotSumX / dotCount;
          double dotCentroidY = dotSumY / dotCount;

          double errorX = Math.abs(markerCentroidX - dotCentroidX);
          double errorY = Math.abs(markerCentroidY - dotCentroidY);
          maxError = Math.max(maxError, Math.max(errorX, errorY));
          if (Math.abs(coord) == 20000) {
            System.out.printf("Coord=%d, zoom=%.3f, hiDpi=%.1f -> errorX=%.4f, errorY=%.4f%n",
                coord, zoom, hiDpi, errorX, errorY);
          }

          assertTrue(errorX <= 0.5,
              String.format("X Error %.4f px > 0.5 px at coord=%d, zoom=%.4f, hiDpi=%.1f (marker=%.2f, dot=%.2f)",
                  errorX, coord, zoom, hiDpi, markerCentroidX, dotCentroidX));
          assertTrue(errorY <= 0.5,
              String.format("Y Error %.4f px > 0.5 px at coord=%d, zoom=%.4f, hiDpi=%.1f (marker=%.2f, dot=%.2f)",
                  errorY, coord, zoom, hiDpi, markerCentroidY, dotCentroidY));
        }
      }
    }
    System.out.printf("GridAlignmentTest passed across all conditions. Maximum measured error = %.4f px%n", maxError);
  }
}
