#!/bin/bash

# Generate RSA key pair for JWT signing (RS256)
# This script generates a 2048-bit RSA key pair

KEY_DIR="src/main/resources/jwt"
PRIVATE_KEY_PATH="$KEY_DIR/private-key.pem"
PUBLIC_KEY_PATH="$KEY_DIR/public-key.pem"

# Create directory if it doesn't exist
mkdir -p "$KEY_DIR"

# Generate private key
openssl genpkey -algorithm RSA -out "$PRIVATE_KEY_PATH" -pkeyopt rsa_keygen_bits:2048

# Generate public key from private key
openssl rsa -pubout -in "$PRIVATE_KEY_PATH" -out "$PUBLIC_KEY_PATH"

echo "RSA keys generated successfully!"
echo "Private key: $PRIVATE_KEY_PATH"
echo "Public key: $PUBLIC_KEY_PATH"
echo ""
echo "Note: Keep the private key secure and never commit it to version control in production!"

