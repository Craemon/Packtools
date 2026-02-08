#!/bin/sh
set -e

LATEST=$(curl -s https://api.github.com/repos/Craemon/Packtools/releases/latest \
  | grep tag_name | cut -d '"' -f 4)

echo "Updating to $LATEST..."

sudo curl -L "https://github.com/Craemon/Packtools/releases/download/$LATEST/packtools-all.jar" \
  -o /usr/local/lib/packtools/packtools.jar

echo "Update complete."