#!/bin/sh
set -e

REPO="Craemon/Packtools"

echo "Fetching latest release from GitHub ($REPO)..."
LATEST_TAG=$(curl -s "https://api.github.com/repos/$REPO/releases/latest" | grep '"tag_name":' | sed -E 's/.*"([^"]+)".*/\1/')

if [ -z "$LATEST_TAG" ]; then
    echo "Error: Could not determine latest release version."
    exit 1
fi

echo "Installing Packtools $LATEST_TAG..."

# 1. Download CLI Binary
echo "Downloading CLI..."
sudo curl -sSL "https://github.com/$REPO/releases/download/$LATEST_TAG/packtools-linux-amd64" -o /usr/local/bin/packtools
sudo chmod 755 /usr/local/bin/packtools

# 2. Download Core Server JAR
echo "Downloading Core Server..."
sudo mkdir -p /usr/local/lib/packtools
sudo curl -sSL "https://github.com/$REPO/releases/download/$LATEST_TAG/core-all.jar" -o /usr/local/lib/packtools/core.jar
sudo chmod 644 /usr/local/lib/packtools/core.jar

# 3. Create 'packtools-server' wrapper script
cat <<'EOF' | sudo tee /usr/local/bin/packtools-server > /dev/null
#!/bin/sh
exec java -jar /usr/local/lib/packtools/core.jar "$@"
EOF
sudo chmod 755 /usr/local/bin/packtools-server

# 4. Generate default ~/.config/packtools/config.yaml if missing
CONFIG_DIR="$HOME/.config/packtools"
CONFIG_FILE="$CONFIG_DIR/config.yaml"
CURRENT_DIR="$(pwd)"

if [ ! -f "$CONFIG_FILE" ]; then
    mkdir -p "$CONFIG_DIR"
    cat <<EOF > "$CONFIG_FILE"
# Packtools Configuration
repoRoot: "$CURRENT_DIR"
EOF
    echo "Created default config at $CONFIG_FILE pointing to $CURRENT_DIR"
fi

# 5. Setup Bash Completion
if [ -d /etc/bash_completion.d ]; then
    if /usr/local/bin/packtools completion bash > /tmp/packtools_completion 2>/dev/null; then
        sudo mv /tmp/packtools_completion /etc/bash_completion.d/packtools
        sudo chmod 644 /etc/bash_completion.d/packtools
        echo "Installed bash autocompletion."
    else
        rm -f /tmp/packtools_completion
    fi
fi

echo ""
echo "✓ Installation complete ($LATEST_TAG)!"
echo "  To configure your Packs folder path, edit: $CONFIG_FILE"
echo "  Run 'packtools-server &' to start the server."
echo "  Run 'packtools' to run CLI commands."