#!/usr/bin/env bash
# =============================================================================
# Window Android — Project Scaffold & Git Branch Setup
# Run from the repo root: bash scripts/setup_project.sh
# =============================================================================
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
APP_SRC="$ROOT/app/src/main"
KOTLIN_ROOT="$APP_SRC/kotlin/com/window/app"
RES="$APP_SRC/res"

echo "🪟  Scaffolding Window Android project at: $ROOT"

# ---------------------------------------------------------------------------
# 1. Directory tree
# ---------------------------------------------------------------------------
dirs=(
  "$KOTLIN_ROOT/data/db"
  "$KOTLIN_ROOT/data/ai"
  "$KOTLIN_ROOT/data/repository"
  "$KOTLIN_ROOT/di"
  "$KOTLIN_ROOT/service"
  "$KOTLIN_ROOT/ui/dashboard"
  "$KOTLIN_ROOT/ui/settings"
  "$KOTLIN_ROOT/ui/components"
  "$KOTLIN_ROOT/ui/navigation"
  "$KOTLIN_ROOT/ui/theme"
  "$KOTLIN_ROOT/util"
  "$RES/xml"
  "$RES/values"
  "$RES/drawable"
  "$RES/mipmap-hdpi"
  "$RES/mipmap-mdpi"
  "$RES/mipmap-xhdpi"
  "$RES/mipmap-xxhdpi"
  "$RES/mipmap-xxxhdpi"
  "$ROOT/app/src/test/kotlin/com/window/app"
  "$ROOT/app/src/androidTest/kotlin/com/window/app"
  "$ROOT/gradle/wrapper"
)

for d in "${dirs[@]}"; do
  mkdir -p "$d"
  echo "  ✅  $d"
done

# ---------------------------------------------------------------------------
# 2. Git setup
# ---------------------------------------------------------------------------
cd "$ROOT"

if [ ! -d ".git" ]; then
  git init
  echo "  🔧  git init"
fi

# .gitignore
cat > .gitignore << 'EOF'
*.iml
.gradle/
local.properties
.idea/
.DS_Store
build/
captures/
*.jks
*.keystore
*.apk
*.aab
google-services.json
EOF

git add -A
git commit -m "chore: initial project scaffold" --allow-empty

# Ensure main exists
CURRENT=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "")
if [ "$CURRENT" != "main" ]; then
  git checkout -b main 2>/dev/null || git checkout main
fi

# develop
git checkout -b develop 2>/dev/null || git checkout develop
git merge main --no-edit 2>/dev/null || true

# feature branches
for branch in feature/accessibility-engine feature/room-persistence feature/gemini-nano-bridge; do
  git checkout -b "$branch" develop 2>/dev/null || echo "  ⚠️  branch $branch already exists"
  git checkout develop
done

git checkout develop
echo ""
echo "✅  Done. Branches: main | develop | feature/accessibility-engine | feature/room-persistence | feature/gemini-nano-bridge"
echo "📂  Source root: $KOTLIN_ROOT"

