#!/usr/bin/env bash
set -euo pipefail

echo "========================================"
echo "Threat Intelligence Engine - Build"
echo "========================================"

# Navigate to project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

# Check JAVA_HOME fallback
if [ -z "${JAVA_HOME:-}" ]; then
    if [ -d "/usr/lib/jvm/java-17-openjdk" ]; then
        export JAVA_HOME="/usr/lib/jvm/java-17-openjdk"
    fi
fi

# Verify javac is available
if ! command -v javac &> /dev/null; then
    if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/javac" ]; then
        export PATH="$JAVA_HOME/bin:$PATH"
    else
        echo "[ERROR] javac compiler not found on PATH. Please install JDK 17."
        exit 1
    fi
fi

# If Maven is available, use it
if command -v mvn &> /dev/null; then
    echo "[INFO] Maven detected. Building with Maven..."
    mvn clean package
    echo "[SUCCESS] Maven build successful."
    exit 0
fi

echo "[INFO] Maven is not detected on PATH."
echo "[INFO] Compiling directly with javac (Java 17)..."

mkdir -p target/classes

# Collect sources and compile
find src/main/java -name "*.java" > target/sources.txt
javac -d target/classes @target/sources.txt

# Package into JAR if jar tool is present
JAR_CMD="jar"
if ! command -v jar &> /dev/null; then
    if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/jar" ]; then
        JAR_CMD="$JAVA_HOME/bin/jar"
    fi
fi

if command -v "$JAR_CMD" &> /dev/null || [ -x "$JAR_CMD" ]; then
    "$JAR_CMD" --create --file target/kill-chain-correlation-engine-1.0.0.jar \
        --main-class engine.Main -C target/classes .
    echo "[SUCCESS] Packaged target/kill-chain-correlation-engine-1.0.0.jar"
else
    echo "[INFO] jar tool not found; compiled classes reside in target/classes."
fi

echo "[SUCCESS] Build completed successfully."
exit 0
