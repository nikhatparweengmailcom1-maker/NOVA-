#!/usr/bin/env bash
# =============================================================================
#  NOVA — Release Keystore Generator
#  Run this ONCE on any machine that has the JDK installed (keytool is part
#  of the JDK, which GitHub Actions also has).
#
#  After running, it prints the three values you must paste into GitHub Secrets.
# =============================================================================

set -e

KEYSTORE_FILE="nova-release.keystore"
KEY_ALIAS="nova-key"
VALIDITY_DAYS=10000          # ~27 years

# ── Prompt for passwords ──────────────────────────────────────────────────────
read -rsp "Enter keystore password (remember this!): " KS_PASS; echo
read -rsp "Confirm keystore password: "                KS_PASS2; echo

if [ "$KS_PASS" != "$KS_PASS2" ]; then
  echo "ERROR: Passwords do not match." >&2; exit 1
fi

read -rsp "Enter key password (can be same as above): " KEY_PASS; echo

# ── Prompt for certificate info ───────────────────────────────────────────────
echo ""
echo "Certificate info (press Enter to accept defaults):"
read -rp "  Your full name  [NOVA Developer]: " CN;  CN="${CN:-NOVA Developer}"
read -rp "  Organisation    [NOVA]:            " OU; OU="${OU:-NOVA}"
read -rp "  City            [Unknown]:         " L;  L="${L:-Unknown}"
read -rp "  Country code    [US]:              " C;  C="${C:-US}"

DNAME="CN=$CN, OU=$OU, O=$OU, L=$L, ST=$L, C=$C"

# ── Generate keystore ─────────────────────────────────────────────────────────
echo ""
echo "Generating keystore..."
keytool -genkey \
  -v \
  -keystore "$KEYSTORE_FILE" \
  -alias    "$KEY_ALIAS" \
  -keyalg   RSA \
  -keysize  2048 \
  -validity "$VALIDITY_DAYS" \
  -storepass "$KS_PASS" \
  -keypass   "$KEY_PASS" \
  -dname     "$DNAME"

echo ""
echo "✅  Keystore written to: $KEYSTORE_FILE"

# ── Base64-encode for GitHub Secret ──────────────────────────────────────────
B64=$(base64 -w 0 "$KEYSTORE_FILE")

echo ""
echo "========================================================"
echo "  COPY THESE VALUES INTO YOUR GITHUB REPOSITORY SECRETS"
echo "========================================================"
echo ""
echo "Secret name : KEYSTORE_BASE64"
echo "Secret value: $B64"
echo ""
echo "Secret name : KEYSTORE_PASSWORD"
echo "Secret value: $KS_PASS"
echo ""
echo "Secret name : KEY_ALIAS"
echo "Secret value: $KEY_ALIAS"
echo ""
echo "Secret name : KEY_PASSWORD"
echo "Secret value: $KEY_PASS"
echo ""
echo "========================================================"
echo "  IMPORTANT: Keep $KEYSTORE_FILE somewhere safe."
echo "  You need it every time you want to update the app."
echo "  If you lose it you cannot push updates to the same APK."
echo "========================================================"
