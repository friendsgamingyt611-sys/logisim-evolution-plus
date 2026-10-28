package com.cburch.logisim.gui.theme;

import com.cburch.logisim.gui.main.ComponentGlyphCache;
import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.Projects;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.util.SystemInfo;
import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Cross-platform desktop theme manager and real-time OS theme monitor.
 * Automatically synchronizes Logisim's Look and Feel with system appearance changes.
 */
public class ThemeManager {

  private static volatile boolean cachedIsDark = false;
  private static volatile long lastCheckTime = 0;
  private static volatile int preferredLinuxDetector = -1;
  private static ScheduledExecutorService monitorService = null;

  public static boolean isSystemDark() {
    final var now = System.currentTimeMillis();
    if (lastCheckTime > 0) {
      if (SwingUtilities.isEventDispatchThread() || now - lastCheckTime < 1000) {
        return cachedIsDark;
      }
    }
    cachedIsDark = querySystemDarkNow();
    lastCheckTime = now;
    return cachedIsDark;
  }

  public static boolean querySystemDarkNow() {
    if (SystemInfo.isLinux) {
      final var pref = preferredLinuxDetector;
      if (pref == 0) {
        final var portal = queryLinuxPortal();
        if (portal != null) return portal;
      } else if (pref == 1) {
        final var gsettings = queryLinuxGSettings();
        if (gsettings != null) return gsettings;
      } else if (pref == 2) {
        final var gtk = queryLinuxGtkSettings();
        if (gtk != null) return gtk;
      } else if (pref == 3) {
        final var kde = queryLinuxKde();
        if (kde != null) return kde;
      }

      final var portal = queryLinuxPortal();
      if (portal != null) {
        preferredLinuxDetector = 0;
        return portal;
      }
      final var gsettings = queryLinuxGSettings();
      if (gsettings != null) {
        preferredLinuxDetector = 1;
        return gsettings;
      }
      final var gtk = queryLinuxGtkSettings();
      if (gtk != null) {
        preferredLinuxDetector = 2;
        return gtk;
      }
      final var kde = queryLinuxKde();
      if (kde != null) {
        preferredLinuxDetector = 3;
        return kde;
      }
      return false;
    }

    if (SystemInfo.isMacOS) {
      try {
        final var proc = new ProcessBuilder("defaults", "read", "-g", "AppleInterfaceStyle").start();
        if (proc.waitFor(500, TimeUnit.MILLISECONDS)) {
          try (var in = proc.getInputStream()) {
            final var out = new String(in.readAllBytes());
            return out.contains("Dark");
          }
        } else {
          proc.destroyForcibly();
        }
      } catch (Throwable ignored) {
        // Fallback
      }
      return false;
    }

    if (SystemInfo.isWindows) {
      try {
        final var proc =
            new ProcessBuilder(
                    "reg",
                    "query",
                    "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                    "/v",
                    "AppsUseLightTheme")
                .start();
        if (proc.waitFor(500, TimeUnit.MILLISECONDS)) {
          try (var in = proc.getInputStream()) {
            final var out = new String(in.readAllBytes());
            if (out.contains("0x0")) {
              return true;
            }
            if (out.contains("0x1")) {
              return false;
            }
          }
        } else {
          proc.destroyForcibly();
        }
      } catch (Throwable ignored) {
        // Fallback
      }
      return false;
    }

    return false;
  }

  private static Boolean queryLinuxPortal() {
    try {
      final var proc =
          new ProcessBuilder(
                  "busctl",
                  "--user",
                  "call",
                  "org.freedesktop.portal.Desktop",
                  "/org/freedesktop/portal/desktop",
                  "org.freedesktop.portal.Settings",
                  "Read",
                  "ss",
                  "org.freedesktop.appearance",
                  "color-scheme")
              .start();
      if (proc.waitFor(500, TimeUnit.MILLISECONDS)) {
        try (var in = proc.getInputStream()) {
          final var out = new String(in.readAllBytes());
          if (out.contains("uint32 1") || out.contains("u 1")) {
            return Boolean.TRUE;
          }
          if (out.contains("uint32 2") || out.contains("u 2") || out.contains("uint32 0") || out.contains("u 0")) {
            return Boolean.FALSE;
          }
        }
      } else {
        proc.destroyForcibly();
      }
    } catch (Throwable ignored) {
      // Fallback
    }

    try {
      final var proc =
          new ProcessBuilder(
                  "dbus-send",
                  "--session",
                  "--dest=org.freedesktop.portal.Desktop",
                  "--type=method_call",
                  "--print-reply",
                  "/org/freedesktop/portal/desktop",
                  "org.freedesktop.portal.Settings.Read",
                  "string:org.freedesktop.appearance",
                  "string:color-scheme")
              .start();
      if (proc.waitFor(500, TimeUnit.MILLISECONDS)) {
        try (var in = proc.getInputStream()) {
          final var out = new String(in.readAllBytes());
          if (out.contains("uint32 1")) {
            return Boolean.TRUE;
          }
          if (out.contains("uint32 2") || out.contains("uint32 0")) {
            return Boolean.FALSE;
          }
        }
      } else {
        proc.destroyForcibly();
      }
    } catch (Throwable ignored) {
      // Fallback
    }
    return null;
  }

  private static Boolean queryLinuxGSettings() {
    try {
      final var proc =
          new ProcessBuilder("gsettings", "get", "org.gnome.desktop.interface", "color-scheme").start();
      if (proc.waitFor(500, TimeUnit.MILLISECONDS)) {
        try (var in = proc.getInputStream()) {
          final var out = new String(in.readAllBytes());
          if (out.contains("prefer-dark")) {
            return Boolean.TRUE;
          }
          if (out.contains("prefer-light")) {
            return Boolean.FALSE;
          }
        }
      } else {
        proc.destroyForcibly();
      }

      // Check gtk-theme fallback
      final var themeProc =
          new ProcessBuilder("gsettings", "get", "org.gnome.desktop.interface", "gtk-theme").start();
      if (themeProc.waitFor(500, TimeUnit.MILLISECONDS)) {
        try (var in = themeProc.getInputStream()) {
          final var themeOut = new String(in.readAllBytes()).toLowerCase();
          if (themeOut.contains("-dark") || themeOut.contains("dark")) {
            return Boolean.TRUE;
          }
          return Boolean.FALSE;
        }
      } else {
        themeProc.destroyForcibly();
      }
    } catch (Throwable ignored) {
      // Fallback
    }
    return null;
  }

  private static Boolean queryLinuxGtkSettings() {
    try {
      final var home = System.getProperty("user.home");
      if (home != null) {
        for (final var rel : new String[] {".config/gtk-4.0/settings.ini", ".config/gtk-3.0/settings.ini"}) {
          final var file = new File(home, rel);
          if (file.exists() && file.canRead()) {
            final var content = Files.readString(file.toPath());
            if (content.contains("gtk-application-prefer-dark-theme=1")
                || content.contains("gtk-application-prefer-dark-theme = 1")
                || content.contains("gtk-application-prefer-dark-theme=true")) {
              return Boolean.TRUE;
            }
            if (content.contains("gtk-application-prefer-dark-theme=0")
                || content.contains("gtk-application-prefer-dark-theme = 0")
                || content.contains("gtk-application-prefer-dark-theme=false")) {
              return Boolean.FALSE;
            }
          }
        }
      }
    } catch (Throwable ignored) {
      // Fallback
    }
    return null;
  }

  private static Boolean queryLinuxKde() {
    try {
      final var home = System.getProperty("user.home");
      if (home != null) {
        final var file = new File(home, ".config/kdeglobals");
        if (file.exists() && file.canRead()) {
          final var content = Files.readString(file.toPath());
          for (final var line : content.split("\\R")) {
            if (line.startsWith("ColorScheme=") || line.startsWith("widgetStyle=")) {
              if (line.toLowerCase().contains("dark")) {
                return Boolean.TRUE;
              }
              return Boolean.FALSE;
            }
          }
        }
      }
    } catch (Throwable ignored) {
      // Fallback
    }
    return null;
  }

  public static String getPairedLaf(String currentLaf, boolean targetDark) {
    if (currentLaf == null || currentLaf.isBlank()) {
      return targetDark ? ModernDarkLaf.class.getName() : ModernLightLaf.class.getName();
    }
    if (currentLaf.equals(ModernDarkLaf.class.getName()) || currentLaf.equals(ModernLightLaf.class.getName())) {
      return targetDark ? ModernDarkLaf.class.getName() : ModernLightLaf.class.getName();
    }
    if (currentLaf.equals(FlatMacDarkLaf.class.getName()) || currentLaf.equals(FlatMacLightLaf.class.getName())) {
      return targetDark ? FlatMacDarkLaf.class.getName() : FlatMacLightLaf.class.getName();
    }
    if (currentLaf.equals(FlatDarkLaf.class.getName()) || currentLaf.equals(FlatLightLaf.class.getName())) {
      return targetDark ? FlatDarkLaf.class.getName() : FlatLightLaf.class.getName();
    }
    if (currentLaf.equals(FlatDarculaLaf.class.getName()) || currentLaf.equals(FlatIntelliJLaf.class.getName())) {
      return targetDark ? FlatDarculaLaf.class.getName() : FlatIntelliJLaf.class.getName();
    }
    return null;
  }

  public static void applyTheme(String lafClassName) {
    if (lafClassName == null || lafClassName.isBlank()) {
      return;
    }
    SwingUtilities.invokeLater(() -> {
      try {
        UIManager.setLookAndFeel(lafClassName);
        AppPreferences.LookAndFeel.set(lafClassName);
        FlatLaf.updateUI();
        ComponentGlyphCache.clearCache();
        for (final var proj : Projects.getOpenProjects()) {
          final var frame = proj.getFrame();
          if (frame != null) {
            frame.updateThemeUI();
          }
        }
      } catch (Throwable t) {
        t.printStackTrace();
      }
    });
  }

  public static synchronized void startSystemThemeMonitor() {
    if (monitorService != null && !monitorService.isShutdown()) {
      return;
    }
    cachedIsDark = querySystemDarkNow();
    lastCheckTime = System.currentTimeMillis();

    // Cold-start alignment if auto-sync is enabled
    if (AppPreferences.AUTO_SYNC_THEME.get()) {
      final var currentLaf = AppPreferences.LookAndFeel.get();
      final var paired = getPairedLaf(currentLaf, cachedIsDark);
      if (paired != null && !paired.equals(currentLaf)) {
        applyTheme(paired);
      }
    }

    monitorService =
        Executors.newSingleThreadScheduledExecutor(
            r -> {
              final var thread = new Thread(r, "SystemThemeMonitor");
              thread.setDaemon(true);
              return thread;
            });

    monitorService.scheduleWithFixedDelay(
        () -> {
          try {
            final var currentDark = querySystemDarkNow();
            final var changed = (currentDark != cachedIsDark);
            cachedIsDark = currentDark;
            lastCheckTime = System.currentTimeMillis();

            if (changed && AppPreferences.AUTO_SYNC_THEME.get()) {
              final var currentLaf = AppPreferences.LookAndFeel.get();
              final var paired = getPairedLaf(currentLaf, currentDark);
              if (paired != null && !paired.equals(currentLaf)) {
                applyTheme(paired);
              }
            }
          } catch (Throwable ignored) {
            // Keep monitor alive
          }
        },
        750,
        750,
        TimeUnit.MILLISECONDS);
  }

  public static synchronized void stopSystemThemeMonitor() {
    if (monitorService != null) {
      monitorService.shutdownNow();
      monitorService = null;
    }
  }
}
