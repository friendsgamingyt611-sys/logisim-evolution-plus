#!/bin/bash
# =============================================================================
# setup.sh — Logisim-evolution+ one-shot setup script
#
# Works on any freshly cloned repository on Linux/macOS.
# Run once; after that just use: ./logisim-evolution+
# =============================================================================

set -euo pipefail

# ── colours ──────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
RESET='\033[0m'

info()    { echo -e "${CYAN}[INFO]${RESET}  $*"; }
success() { echo -e "${GREEN}[OK]${RESET}    $*"; }
warn()    { echo -e "${YELLOW}[WARN]${RESET}  $*"; }
error()   { echo -e "${RED}[ERROR]${RESET} $*" >&2; }
die()     { error "$*"; exit 1; }
header()  { echo -e "\n${BOLD}${CYAN}===  $*  ===${RESET}\n"; }

# ── resolve project root (follow symlinks) ────────────────────────────────────
SCRIPT_SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SCRIPT_SOURCE" ]; do
  SCRIPT_DIR="$(cd "$(dirname "$SCRIPT_SOURCE")" && pwd)"
  SCRIPT_SOURCE="$(readlink "$SCRIPT_SOURCE")"
  [[ $SCRIPT_SOURCE != /* ]] && SCRIPT_SOURCE="$SCRIPT_DIR/$SCRIPT_SOURCE"
done
PROJECT_DIR="$(cd "$(dirname "$SCRIPT_SOURCE")" && pwd)"

# ── version info ──────────────────────────────────────────────────────────────
GRADLE_VERSION="$(grep '^version' "$PROJECT_DIR/gradle.properties" | cut -d= -f2 | tr -d ' ')"
JAR_VERSION="${GRADLE_VERSION//-/}"
APP_NAME="logisim-evolution+"
JAR_NAME="${APP_NAME}-${JAR_VERSION}-all.jar"
JAR_PATH="$PROJECT_DIR/build/libs/$JAR_NAME"
LAUNCHER="$PROJECT_DIR/$APP_NAME"

echo -e "${BOLD}"
echo "  +==================================================+"
echo "  |       Logisim-evolution+ Setup Script            |"
echo "  |       Version: ${GRADLE_VERSION}                 |"
echo "  +==================================================+"
echo -e "${RESET}"

# =============================================================================
# STEP 1 - Ensure a JDK (not just JRE) is present — install if missing
# =============================================================================
header "Step 1: Checking for Java JDK (compiler)"

# Helper: check if a given java binary actually has a compiler alongside it
has_jdk() {
  local java_bin="$1"
  local java_home
  java_home="$(dirname "$(dirname "$java_bin")")"
  [ -x "$java_home/bin/javac" ]
}

# Helper: get major version from a java binary
java_major() {
  "$1" -version 2>&1 | head -1 | grep -oP '(?<=version ")[0-9]+' || echo "0"
}

find_jdk() {
  # 1. Check well-known JDK paths (prefer Java 21)
  for candidate in \
      /usr/lib/jvm/java-21-openjdk-amd64/bin/java \
      /usr/lib/jvm/java-21-openjdk/bin/java \
      /usr/lib/jvm/temurin-21/bin/java \
      /usr/lib/jvm/java-17-openjdk-amd64/bin/java \
      /usr/lib/jvm/java-17-openjdk/bin/java; do
    if [ -x "$candidate" ] && has_jdk "$candidate"; then
      echo "$candidate"; return 0
    fi
  done
  # 2. Check whatever 'java' resolves to on PATH
  if command -v java &>/dev/null; then
    local p
    p="$(command -v java)"
    # follow symlinks
    p="$(readlink -f "$p" 2>/dev/null || echo "$p")"
    if has_jdk "$p"; then
      echo "$p"; return 0
    fi
  fi
  # 3. Scan all JVM dirs for any JDK >= 21
  for jvm_dir in /usr/lib/jvm/*/; do
    local jbin="$jvm_dir/bin/java"
    if [ -x "$jbin" ] && has_jdk "$jbin"; then
      local ver
      ver="$(java_major "$jbin")"
      if [ "$ver" -ge 21 ] 2>/dev/null; then
        echo "$jbin"; return 0
      fi
    fi
  done
  return 1
}

install_jdk() {
  info "No JDK (compiler) found. Attempting to install one automatically ..."

  if command -v dnf &>/dev/null; then
    # Build a candidate list: prefer the devel package matching already-installed
    # Java versions, then fall back to known good versions.
    local installed_vers
    installed_vers="$(rpm -qa --qf '%{NAME}\n' 2>/dev/null \
      | grep -oP '(?<=java-)\d+(?=-openjdk)' | sort -rn | uniq)"

    local candidates=()
    for ver in $installed_vers; do
      candidates+=("java-${ver}-openjdk-devel")
    done
    # Always include these fallbacks
    candidates+=(java-25-openjdk-devel java-21-openjdk-devel java-latest-openjdk-devel)

    # dnf options:
    #   fastestmirror=True  — pick the fastest mirror, avoids stuck Chinese/slow mirrors
    #   timeout/minrate     — abort slow downloads quickly and retry on a different mirror
    #   retries             — retry each package several times before giving up
    #   exclude fonts       — xorg-x11-fonts-Type1, ttmkfdir, mkfontscale are optional
    #                         GUI font extras; they time out on slow mirrors and are not
    #                         needed to compile or run Logisim (Java uses its own fonts)
    local DNF_OPTS=(
      --setopt=fastestmirror=True
      --setopt=timeout=60
      --setopt=minrate=1000
      --setopt=retries=5
      --exclude='*fonts*'
      --exclude='mkfontscale'
      --exclude='ttmkfdir'
    )

    for pkg in "${candidates[@]}"; do
      if dnf list available "$pkg" &>/dev/null 2>&1; then
        info "Installing $pkg ..."
        sudo dnf install -y "${DNF_OPTS[@]}" "$pkg" && return 0
      fi
    done
    die "Could not find any available JDK package via dnf.\nInstall manually: sudo dnf install java-latest-openjdk-devel"

  elif command -v apt-get &>/dev/null; then
    # Try matching installed version first
    local installed_ver
    installed_ver="$(java -version 2>&1 | grep -oP '(?<=version ")[0-9]+' || echo '')"
    local apt_candidates=()
    [ -n "$installed_ver" ] && apt_candidates+=("openjdk-${installed_ver}-jdk")
    apt_candidates+=(openjdk-21-jdk openjdk-17-jdk default-jdk)
    for pkg in "${apt_candidates[@]}"; do
      if apt-cache show "$pkg" &>/dev/null 2>&1; then
        info "Installing $pkg ..."
        sudo apt-get install -y "$pkg" && return 0
      fi
    done
    die "Could not find any JDK package via apt. Install manually: sudo apt install default-jdk"

  elif command -v pacman &>/dev/null; then
    sudo pacman -S --noconfirm jdk21-openjdk || sudo pacman -S --noconfirm jdk-openjdk

  elif command -v brew &>/dev/null; then
    brew install --quiet openjdk@21

  else
    die "Cannot auto-install JDK: no supported package manager found.\nPlease install a Java JDK (21+) manually:\n  Fedora/RHEL:   sudo dnf install java-latest-openjdk-devel\n  Ubuntu/Debian: sudo apt install default-jdk\n  macOS:         brew install openjdk@21"
  fi
}

JAVA_CMD=""
if ! JAVA_CMD="$(find_jdk)"; then
  install_jdk
  # Try again after install
  JAVA_CMD="$(find_jdk)" || die "JDK install seemed to succeed but still cannot find a usable JDK.\nPlease re-open your terminal and re-run this script."
fi

JAVA_MAJOR_VER="$(java_major "$JAVA_CMD")"
if [ "$JAVA_MAJOR_VER" -lt 21 ] 2>/dev/null; then
  warn "Found JDK $JAVA_MAJOR_VER at $JAVA_CMD — project requires Java 21+."
  info "Attempting to install Java 21 JDK ..."
  install_jdk
  JAVA_CMD="$(find_jdk)" || die "Still cannot find Java 21 JDK after install attempt."
  JAVA_MAJOR_VER="$(java_major "$JAVA_CMD")"
fi

JAVA_HOME_DIR="$(dirname "$(dirname "$JAVA_CMD")")"
success "JDK $JAVA_MAJOR_VER found at: $JAVA_CMD"
info    "JAVA_HOME: $JAVA_HOME_DIR"

# =============================================================================
# STEP 2 - Ensure gradlew is executable
# =============================================================================
header "Step 2: Preparing Gradle wrapper"

GRADLEW="$PROJECT_DIR/gradlew"
if [ ! -f "$GRADLEW" ]; then
  die "Gradle wrapper (gradlew) not found in $PROJECT_DIR -- is this a complete clone?"
fi
chmod +x "$GRADLEW"
success "Gradle wrapper is ready"

# =============================================================================
# STEP 3 - Build the shadow JAR
# =============================================================================
header "Step 3: Building $APP_NAME"


# -----------------------------------------------------------------------------
# Fix: Fedora's OpenJDK 'release' file is missing the IMAGE_TYPE=JDK key.
# Gradle's JvmInstallationProbe reads this key to grant JAVA_COMPILER capability.
# Without it, Gradle rejects the JDK even though javac is physically present.
# We patch the file temporarily (restored by trap on exit).
# -----------------------------------------------------------------------------
patch_release_file() {
  local release_file="$JAVA_HOME_DIR/release"
  if [ ! -f "$release_file" ]; then
    warn "No release file at $release_file — skipping patch"
    return
  fi
  if grep -q '^IMAGE_TYPE=' "$release_file"; then
    info "release file already has IMAGE_TYPE — no patch needed"
    return
  fi
  info "Patching $release_file to add IMAGE_TYPE=JDK (required by Gradle toolchain probe)"
  # Back up original
  RELEASE_FILE_BACKUP="$(mktemp)"
  cp "$release_file" "$RELEASE_FILE_BACKUP"
  # Register cleanup so we always restore even if the build fails or user Ctrl-Cs
  trap 'restore_release_file' EXIT INT TERM
  # Append the missing key
  echo 'IMAGE_TYPE="JDK"' | sudo tee -a "$release_file" > /dev/null
  success "Patched release file (will be restored after build)"
}

restore_release_file() {
  local release_file="$JAVA_HOME_DIR/release"
  if [ -n "${RELEASE_FILE_BACKUP:-}" ] && [ -f "$RELEASE_FILE_BACKUP" ]; then
    sudo cp "$RELEASE_FILE_BACKUP" "$release_file"
    rm -f "$RELEASE_FILE_BACKUP"
    unset RELEASE_FILE_BACKUP
    info "Restored $release_file to original state"
  fi
}

patch_release_file

do_build() {
  cd "$PROJECT_DIR"
  JAVA_HOME="$JAVA_HOME_DIR" \
  "$GRADLEW" shadowJar \
    -Dorg.gradle.java.installations.paths="$JAVA_HOME_DIR" \
    -Dorg.gradle.java.installations.auto-detect=false \
    -Dorg.gradle.java.installations.auto-download=false \
    --no-configuration-cache \
    --warning-mode none
}


if [ -f "$JAR_PATH" ]; then
  info "JAR already exists at: $JAR_PATH"
  read -r -p "  Re-build? [y/N] " REBUILD
  if [[ "$REBUILD" =~ ^[Yy]$ ]]; then
    info "Re-building ..."
    do_build
    restore_release_file
    success "Build complete"
  else
    restore_release_file
    info "Skipping build"
  fi
else
  info "Building JAR (this may take a few minutes on the first run) ..."
  do_build
  restore_release_file
  success "Build complete"
fi

if [ ! -f "$JAR_PATH" ]; then
  die "Build finished but JAR not found at:\n  $JAR_PATH\nCheck build.gradle.kts for the actual output filename."
fi
success "JAR verified: $JAR_PATH"

# =============================================================================
# STEP 4 - Write / update the launcher script
# =============================================================================
header "Step 4: Writing launcher script"

cat > "$LAUNCHER" <<'LAUNCHER_SCRIPT'
#!/bin/bash
# Logisim-evolution+ launcher  (auto-generated by setup.sh — do not hand-edit)
# Uses Java 21+ to run the pre-built jar

# Resolve symlink to get the real project directory
SCRIPT_SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SCRIPT_SOURCE" ]; do
  SCRIPT_DIR="$(cd "$(dirname "$SCRIPT_SOURCE")" && pwd)"
  SCRIPT_SOURCE="$(readlink "$SCRIPT_SOURCE")"
  [[ $SCRIPT_SOURCE != /* ]] && SCRIPT_SOURCE="$SCRIPT_DIR/$SCRIPT_SOURCE"
done
SCRIPT_DIR="$(cd "$(dirname "$SCRIPT_SOURCE")" && pwd)"
LAUNCHER_SCRIPT

# Inject the real JAR name (expanded at setup time, not at run time)
echo "JAR=\"\$SCRIPT_DIR/build/libs/${JAR_NAME}\"" >> "$LAUNCHER"

cat >> "$LAUNCHER" <<'LAUNCHER_TAIL'

# Find a suitable Java to run with (JRE is fine here — we only need 'java', not 'javac')
find_java() {
  for candidate in \
      /usr/lib/jvm/java-21-openjdk-amd64/bin/java \
      /usr/lib/jvm/java-21-openjdk/bin/java \
      /usr/lib/jvm/temurin-21/bin/java \
      /usr/lib/jvm/java-17-openjdk-amd64/bin/java \
      /usr/lib/jvm/java-17-openjdk/bin/java; do
    [ -x "$candidate" ] && echo "$candidate" && return 0
  done
  command -v java &>/dev/null && echo "java" && return 0
  return 1
}

if ! JAVA="$(find_java)"; then
  echo "ERROR: Java not found. Install Java 21+ to run Logisim-evolution+."
  exit 1
fi

if [ ! -f "$JAR" ]; then
  echo "ERROR: JAR not found at $JAR"
  echo "Run ./setup.sh to build the project."
  exit 1
fi

exec "$JAVA" -jar "$JAR" "$@"
LAUNCHER_TAIL

chmod +x "$LAUNCHER"
success "Launcher written to: $LAUNCHER"

# =============================================================================
# STEP 5 - (Optional) install system-wide symlink
# =============================================================================
header "Step 5: System-wide installation (optional)"

INSTALL_DIR="/usr/local/bin"
SYMLINK="$INSTALL_DIR/$APP_NAME"
DO_INSTALL=false

if [ -L "$SYMLINK" ] || [ -f "$SYMLINK" ]; then
  info "A command called '$APP_NAME' already exists in $INSTALL_DIR"
  read -r -p "  Overwrite/update it? [y/N] " UPDATE_SYM
  [[ "$UPDATE_SYM" =~ ^[Yy]$ ]] && DO_INSTALL=true
else
  read -r -p "  Install '$APP_NAME' system-wide to $INSTALL_DIR so it works from anywhere? [Y/n] " INSTALL_SYS
  [[ ! "$INSTALL_SYS" =~ ^[Nn]$ ]] && DO_INSTALL=true
fi

if $DO_INSTALL; then
  if sudo ln -sf "$LAUNCHER" "$SYMLINK" 2>/dev/null; then
    success "Symlink created: $SYMLINK -> $LAUNCHER"
    info "You can now run '$APP_NAME' from any directory."
  else
    warn "Could not create symlink (no sudo?). Skipping system-wide install."
    warn "To install manually:  sudo ln -sf \"$LAUNCHER\" \"$SYMLINK\""
    DO_INSTALL=false
  fi
else
  info "Skipping system-wide install."
fi

# =============================================================================
# Done
# =============================================================================
echo ""
echo -e "${GREEN}${BOLD}Setup complete!${RESET}"
echo ""
echo -e "  Run the app with:"
echo -e "    ${BOLD}./$APP_NAME${RESET}    (from this directory)"
if $DO_INSTALL; then
  echo -e "    ${BOLD}$APP_NAME${RESET}      (from anywhere)"
fi
echo ""
