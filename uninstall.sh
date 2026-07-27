#!/bin/sh
set -e

echo "Uninstalling Packtools..."

# Remove installed binaries and JARs
sudo rm -f /usr/local/bin/packtools
sudo rm -f /usr/local/bin/packtools-server
sudo rm -rf /usr/local/lib/packtools

# Remove autocompletion
sudo rm -f /etc/bash_completion.d/packtools

echo "Packtools binaries removed."

# Prompt to clean user config directory
USER_HOME="${SUDO_USER:-$USER}"
CONFIG_DIR="/home/$USER_HOME/.config/packtools"

if [ -d "$CONFIG_DIR" ]; then
    printf "Do you want to delete configuration files in %s? [y/N] " "$CONFIG_DIR"
    read -r response
    if [ "$response" = "y" ] || [ "$response" = "Y" ]; then
        rm -rf "$CONFIG_DIR"
        echo "Configuration directory deleted."
    fi
fi

echo "✓ Uninstallation complete."