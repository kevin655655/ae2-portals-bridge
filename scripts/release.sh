#!/usr/bin/env bash
#
# scripts/release.sh — cut a GitHub Release from the develop branch.
#
# What it does:
#   1. Runs `./gradlew clean build` to produce build/libs/<mod>.jar
#   2. Auto-increments the dev tag (e.g. v0.2.0-dev.3 → v0.2.0-dev.4),
#      or accepts an explicit tag as the first arg
#   3. Creates a prerelease GitHub Release tagged at the current develop HEAD,
#      with the jar as the sole binary asset
#   4. Generates release notes from `git log <prev-tag>..HEAD`
#
# Usage:
#   ./scripts/release.sh                  # auto-increment the next dev tag
#   ./scripts/release.sh v0.2.0-dev.5   # explicit tag
#   ./scripts/release.sh v0.2.0          # stable release (omit --prerelease)
#
# Requires: `gh` CLI authenticated against kevin655655.

set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

VERSION_TAG="${1:-}"
BRANCH="$(git rev-parse --abbrev-ref HEAD)"

# Auto-increment the dev tag if none was given
if [ -z "$VERSION_TAG" ]; then
    LAST_TAG=$(git tag --list 'v*-dev.*' --sort=-v:refname | head -1 || true)
    BASE_VERSION=$(grep '^mod_version=' gradle.properties | cut -d= -f2)
    if [ -n "$LAST_TAG" ]; then
        LAST_NUM=$(echo "$LAST_TAG" | sed -E 's/.*dev\.([0-9]+).*/\1/')
        VERSION_TAG="v${BASE_VERSION}-dev.$((LAST_NUM + 1))"
    else
        VERSION_TAG="v${BASE_VERSION}-dev.1"
    fi
fi

echo "==> target tag:    $VERSION_TAG"
echo "==> source branch: $BRANCH"
echo

# Build
echo "==> running ./gradlew clean build ..."
./gradlew clean build --warning-mode=none

# Find the primary jar (not the -sources.jar)
JAR=$(ls build/libs/*.jar 2>/dev/null | grep -v '\-sources' | head -1 || true)
if [ -z "$JAR" ]; then
    echo "ERROR: no jar found in build/libs/" >&2
    exit 1
fi
echo "==> built jar:    $JAR"
echo

# Decide whether this is a prerelease (dev tag) or stable
PRERELEASE_FLAG=""
if [[ "$VERSION_TAG" == *"-dev."* ]]; then
    PRERELEASE_FLAG="--prerelease"
fi

# Generate release notes from the commits since the previous tag
PREV_TAG=$(git tag --sort=-v:refname | sed -n '2p' || true)
if [ -n "$PREV_TAG" ]; then
    NOTES=$(git log "${PREV_TAG}..HEAD" --pretty=format:"- %s")
else
    NOTES="Initial release."
fi

# Prepend a header so the release page is self-explanatory
NOTES="Test build from \`${BRANCH}\`. Grab the jar, drop it in \`mods/\`, report back.

## Changes since ${PREV_TAG:-start}
${NOTES}"

echo "==> creating GitHub release $VERSION_TAG ..."
gh release create "$VERSION_TAG" \
    --target "$BRANCH" \
    --title "$VERSION_TAG" \
    --notes "$NOTES" \
    $PRERELEASE_FLAG \
    "$JAR"

echo
echo "done. release URL:"
gh release view "$VERSION_TAG" --json url --jq '.url'
