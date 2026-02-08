#!/bin/sh
set -e

VERSION="1.1.0"
URL="https://github.com/Craemon/Packtools/releases/download/$VERSION/packtools-all.jar"

echo "Installing PackTools $VERSION..."

sudo mkdir -p /usr/local/lib/packtools
sudo curl -L "$URL" -o /usr/local/lib/packtools/packtools.jar
sudo chmod 755 /usr/local/lib/packtools/packtools.jar

cat <<'EOF' | sudo tee /usr/local/bin/packtools > /dev/null
#!/bin/sh
exec java -jar /usr/local/lib/packtools/packtools.jar "$@"
EOF

sudo chmod +x /usr/local/bin/packtools

echo "Installed. Run: packtools"
