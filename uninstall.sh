#!/bin/sh
set -e

echo "Uninstalling Packtools..."

# Remove installed binaries and JARs
sudo rm -f /usr/local/bin/packtools
sudo rm -f /usr/local/bin/packtools-server
sudo rm -rf /usr/local/lib/packtools

# Remove autocompletion
sudo rm -f /etc/bash_completion.d/packtools

echo "Packtools system files removed."

# Resolve real user's home directory
REAL_USER="${SUDO_USER:-$USER}"
REAL_HOME=$(eval echo "~$REAL_USER")
CONFIG_DIR="$REAL_HOME/.config/packtools"

if [ -d "$CONFIG_DIR" ]; then
    printf "Do you want to delete user configuration files in %s? [y/N] " "$CONFIG_DIR"
    read -r response
    case "$response" in
        [yY][eE][sS]|[yY])
            rm -rf "$CONFIG_DIR"
            echo "Configuration directory deleted."
            ;;
        *)
            echo "Skipped configuration deletion."
            ;;
    esac
fi

echo "✓ Uninstallation complete."