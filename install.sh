#!/bin/sh
set -e

REPO="Craemon/Packtools"

if [ -n "$TAG" ]; then
    LATEST_TAG="$TAG"
    echo "Using specified tag: $LATEST_TAG"
else
    echo "Fetching latest release from GitHub ($REPO)..."
    LATEST_TAG=$(curl -s "https://api.github.com/repos/$REPO/releases/latest" | grep '"tag_name":' | sed -E 's/.*"([^"]+)".*/\1/')
fi

if [ -z "$LATEST_TAG" ]; then
    echo "Error: Could not determine release version."
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

    # Default fallback if installer is run directly from $HOME
    if [ "$CURRENT_DIR" = "$HOME" ]; then
        SUGGESTED_DIR="$HOME/Projects"
    else
        SUGGESTED_DIR="$CURRENT_DIR"
    fi

    TARGET_DIR=""

    # Read directly from /dev/tty to support `curl | sh` pipeline
    if [ -c /dev/tty ]; then
        echo ""
        printf "Enter repository root path [%s]: " "$SUGGESTED_DIR"
        read -r INPUT_DIR < /dev/tty
        TARGET_DIR="${INPUT_DIR:-$SUGGESTED_DIR}"
    else
        TARGET_DIR="$SUGGESTED_DIR"
    fi

    # Expand tilde ~ if user entered it manually
    TARGET_DIR=$(eval echo "$TARGET_DIR")

    cat <<EOF > "$CONFIG_FILE"
# Packtools Configuration
repoRoot: "$TARGET_DIR"
EOF
    echo "✓ Created config at $CONFIG_FILE (repoRoot: $TARGET_DIR)"
fi

# 5. Setup Background Systemd Service
SYSTEMD_ENABLED=0

if command -v systemctl >/dev/null 2>&1 && systemctl --user status >/dev/null 2>&1; then
    SYSTEMD_USER_DIR="$HOME/.config/systemd/user"
    mkdir -p "$SYSTEMD_USER_DIR"

    cat <<EOF > "$SYSTEMD_USER_DIR/packtools.service"
[Unit]
Description=Packtools Core Server
After=network.target

[Service]
Type=simple
ExecStart=/usr/local/bin/packtools-server
Restart=on-failure
RestartSec=5

[Install]
WantedBy=default.target
EOF

    systemctl --user daemon-reload 2>/dev/null || true
    systemctl --user enable --now packtools.service 2>/dev/null || true
    SYSTEMD_ENABLED=1

    # Give server a moment to spin up before CLI completion calls the API
    sleep 1
fi

# 6. Setup Bash Completion
if [ -d /etc/bash_completion.d ]; then
    if /usr/local/bin/packtools completion bash > /tmp/packtools_completion 2>/dev/null; then
        if [ -s /tmp/packtools_completion ]; then
            sudo mv /tmp/packtools_completion /etc/bash_completion.d/packtools
            sudo chmod 644 /etc/bash_completion.d/packtools
            echo "Installed bash autocompletion."
        else
            rm -f /tmp/packtools_completion
        fi
    else
        rm -f /tmp/packtools_completion
    fi
fi

echo ""
echo "✓ Installation complete ($LATEST_TAG)!"
echo "  To configure your Packs folder path, edit: $CONFIG_FILE"

if [ "$SYSTEMD_ENABLED" -eq 1 ]; then
    echo "  Background server automatically started via systemd (packtools.service)."
    echo "  Run 'packtools' to start using CLI commands immediately."
else
    echo "  ℹ systemd user session not detected."
    echo "  To run the server manually, execute: packtools-server &"
fi