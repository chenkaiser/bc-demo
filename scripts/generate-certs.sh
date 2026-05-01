#!/bin/bash
# Run once before 'podman compose up' to generate the self-signed TLS keystore.
# For production, replace the generated keystore.p12 with a real certificate.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
KEYSTORE="$REPO_ROOT/certs/keystore.p12"
PASSWORD="${SSL_KEY_STORE_PASSWORD:-changeit}"

if [[ -f "$KEYSTORE" ]]; then
    echo "Keystore already exists at $KEYSTORE — skipping."
    echo "Delete it and re-run to regenerate."
    exit 0
fi

mkdir -p "$REPO_ROOT/certs"

keytool -genkeypair \
    -alias gateway \
    -keyalg RSA \
    -keysize 2048 \
    -validity 365 \
    -keystore "$KEYSTORE" \
    -storetype PKCS12 \
    -storepass "$PASSWORD" \
    -dname "CN=localhost,OU=Demo,O=Example,L=NYC,ST=NY,C=US" \
    -noprompt

echo "Done: $KEYSTORE"
echo ""
echo "IMPORTANT: add the following line to /etc/hosts so Keycloak is reachable"
echo "from both the host machine and inside containers under the same hostname:"
echo ""
echo "  127.0.0.1  keycloak"
