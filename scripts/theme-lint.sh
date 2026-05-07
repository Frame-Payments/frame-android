#!/usr/bin/env bash
#
# theme-lint.sh — block hardcoded styling in SDK UI code.
#
# Mirrors the iOS "dark-mode lint" step (see frame-ios/.github/workflows/swift.yml).
# Compose components in FrameSDK-UI and FrameSDK-Onboarding must read colors,
# typography, and corner radii from FrameTheme — never from hardcoded literals or
# raw MaterialTheme.* slots — so consumers can override the SDK's appearance via
# OnboardingConfig(theme = ...) or FrameTheme { ... }.
#
# Allow-list (intentional non-themed sites) is encoded in EXCLUDE_PATTERNS below.
# Each entry is justified with a comment.
#
# Run locally: ./scripts/theme-lint.sh
# Fails with a non-zero exit code and a list of offending lines if any are found.

set -euo pipefail

cd "$(dirname "$0")/.."

# Directories to lint. The non-UI core SDK module (FrameSDK) and host demo app
# are excluded — they're allowed to use raw Material defaults / hardcoded colors
# because they don't ship to integrators as themable surface area.
SCAN_DIRS=(
  "FrameSDK-UI/src/main/java"
  "FrameSDK-Onboarding/src/main/java"
)

# Allow-list — files / call sites where a hardcoded literal is intentional.
# Keep this list short and justified; prefer fixing the call site over adding
# an exception.
EXCLUDE_PATTERNS=(
  # Theme definitions themselves declare the canonical defaults.
  "FrameSDK-UI/src/main/java/com/framepayments/framesdk_ui/theme/"
  # Brand-mark button: Apple/Google Pay HIG mandates black/white pill backgrounds
  # regardless of host theme.
  "FrameSDK-UI/src/main/java/com/framepayments/framesdk_ui/buttons/FramePaymentButton.kt"
  # Capsule pill geometry is derived from the 5.dp height (radius = height/2),
  # not customizable — see comment in ProgressIndicator.kt.
  "FrameSDK-Onboarding/src/main/java/com/framepayments/frameonboarding/views/ProgressIndicator.kt"
  # Camera UX convention: viewfinder uses a black translucent mask and white
  # guide strokes regardless of host theme (matches iOS CameraColors.swift /
  # ViewfinderOverlay.swift allow-list).
  "FrameSDK-Onboarding/src/main/java/com/framepayments/frameonboarding/views/CameraCaptureScreen.kt"
)

# Patterns that signal a violation. Each pattern targets a specific anti-pattern.
# Note: \b doesn't work consistently across grep flavors with `(`; we anchor with
# the surrounding character classes instead.
PATTERNS=(
  # Raw Compose color literals (any 0xAARRGGBB hex)
  'Color\(0x[0-9A-Fa-f]{8}\)'
  # Named Color constants. Anchor with a non-identifier follower OR end-of-line
  # so we don't match Color.Whitesmoke but DO catch trailing-EOL uses.
  'Color\.White([^A-Za-z]|$)'
  'Color\.Black([^A-Za-z]|$)'
  'Color\.Gray([^A-Za-z]|$)'
  'Color\.Red([^A-Za-z]|$)'
  'Color\.Transparent([^A-Za-z]|$)'
  # MaterialTheme leak — must go through LocalFrameTheme.
  'MaterialTheme\.colorScheme\.'
  'MaterialTheme\.typography\.'
  'MaterialTheme\.shapes\.'
  # Hardcoded corner-radius literals (any non-zero literal). Token-driven sites
  # use theme.radii.{small,medium,large}, which carry no `.dp` suffix at the
  # call site (the Dp value is on the field itself).
  'RoundedCornerShape\([0-9]+\.[0-9]+\.dp\)'
  'RoundedCornerShape\([0-9]+\.dp\)'
  # Hardcoded font sizes. theme.fonts.* carry sizes via TextStyle.
  'fontSize\s*=\s*[0-9]+\.sp'
)

# Build an extended regex that matches any of the patterns.
JOINED_PATTERN="$(IFS='|'; echo "${PATTERNS[*]}")"

# Build the exclusion grep filter.
EXCLUDE_RE="$(IFS='|'; echo "${EXCLUDE_PATTERNS[*]}")"

# Find offending lines. `grep -n` prints file:line:match. We filter out
# allow-listed files/sites with a second grep -v.
matches="$(
  grep -RnE "$JOINED_PATTERN" "${SCAN_DIRS[@]}" --include="*.kt" 2>/dev/null \
    | { [ -n "$EXCLUDE_RE" ] && grep -vE "$EXCLUDE_RE" || cat; } \
    || true
)"

if [ -n "$matches" ]; then
  echo "❌ Theme lint failed. Use FrameTheme tokens instead of hardcoded literals or raw MaterialTheme.* slots."
  echo
  echo "   Fix one of these patterns:"
  echo "     • Color(0x...) / Color.White / Color.Black / Color.Gray / Color.Red / Color.Transparent"
  echo "         → use LocalFrameTheme.current.colors.<token>"
  echo "     • MaterialTheme.colorScheme.* / .typography.* / .shapes.*"
  echo "         → use LocalFrameTheme.current.{colors,fonts,radii}.<token>"
  echo "     • RoundedCornerShape(N.dp)"
  echo "         → use RoundedCornerShape(LocalFrameTheme.current.radii.<small|medium|large>)"
  echo "     • fontSize = N.sp"
  echo "         → use style = LocalFrameTheme.current.fonts.<token>"
  echo
  echo "   If the call site is genuinely meant to bypass the theme, add it to"
  echo "   EXCLUDE_PATTERNS in scripts/theme-lint.sh with a one-line justification."
  echo
  echo "Violations:"
  echo "$matches"
  exit 1
fi

echo "✅ Theme lint passed."
