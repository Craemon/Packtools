#!/bin/sh
set -e

echo "Uninstalling Packtools..."

# Resolve real user's home directory
REAL_USER="${SUDO_USER:-$USER}"
REAL_HOME=$(eval echo "~$REAL_USER")

# 1. Stop and remove systemd user service if present
if command -v systemctl >/dev/null 2>&1; then
    if systemctl --user is-active --quiet packtools.service 2>/dev/null; then
        echo "Stopping Packtools background service..."
        systemctl --user stop packtools.service 2>/dev/null || true
        systemctl --user disable packtools.service 2>/dev/null || true
    fi
fi

if [ -f "$REAL_HOME/.config/systemd/user/packtools.service" ]; then
    rm -f "$REAL_HOME/.config/systemd/user/packtools.service"
    systemctl --user daemon-reload 2>/dev/null || true
fi

# 2. Remove installed binaries and JARs
echo "Removing system files..."
sudo rm -f /usr/local/bin/packtools
sudo rm -f /usr/local/bin/packtools-server
sudo rm -rf /usr/local/lib/packtools

# 3. Remove autocompletion
sudo rm -f /etc/bash_completion.d/packtools

echo "Packtools system files removed."

# 4. Handle configuration directory cleanup
CONFIG_DIR="$REAL_HOME/.config/packtools"

if [ -d "$CONFIG_DIR" ]; then
    # Read directly from /dev/tty so curl | sh pipeline doesn't skip prompt input
    if [ -c /dev/tty ]; then
        printf "Do you want to delete user configuration files in %s? [y/N] " "$CONFIG_DIR"
        read -r response < /dev/tty
        case "$response" in
            [yY][eE][sS]|[yY])
                rm -rf "$CONFIG_DIR"
                echo "Configuration directory deleted."
                ;;
            *)
                echo "Skipped configuration deletion."
                ;;
        esac
    else
        echo "Skipped configuration deletion (non-interactive shell)."
    fi
fi

echo "✓ Uninstallation complete."