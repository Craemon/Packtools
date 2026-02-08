#!/bin/sh
set -e

JAR="build/libs/packtools-all.jar"

echo "Installing PackTools locally..."

sudo mkdir -p /usr/local/lib/packtools
sudo cp "$JAR" /usr/local/lib/packtools/packtools.jar

cat <<'EOF' | sudo tee /usr/local/bin/packtools > /dev/null
#!/bin/sh
exec java -jar /usr/local/lib/packtools/packtools.jar "$@"
EOF

sudo chmod +x /usr/local/bin/packtools

echo "Installed: /usr/local/bin/packtools"
