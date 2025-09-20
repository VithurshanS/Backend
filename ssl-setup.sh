#!/bin/bash

# SSL Certificate Setup Script for Cloudflare Origin Certificates
# Usage: ./ssl-setup.sh

echo "🔐 SSL Certificate Setup for Tutorverse Backend"
echo "================================================"

# Create SSL directories
echo "📁 Creating SSL directories..."
sudo mkdir -p /etc/ssl/certs
sudo mkdir -p /etc/ssl/private

echo ""
echo "📋 Please follow these steps to get your Cloudflare Origin Certificates:"
echo ""
echo "1. Go to Cloudflare Dashboard: https://dash.cloudflare.com"
echo "2. Select your domain: shancloudservice.com"
echo "3. Go to SSL/TLS → Origin Server"
echo "4. Click 'Create Certificate'"
echo "5. Configure:"
echo "   - Private key type: RSA (2048)"
echo "   - Hostnames: backend.shancloudservice.com, *.shancloudservice.com"
echo "   - Certificate Validity: 15 years"
echo "6. Click 'Create'"
echo ""

# Function to input certificate
setup_certificate() {
    echo "📄 Setting up Origin Certificate..."
    echo "Copy and paste the ENTIRE certificate content (including -----BEGIN CERTIFICATE----- and -----END CERTIFICATE-----)"
    echo "Press Ctrl+D when finished:"
    
    sudo tee /etc/ssl/certs/cloudflare-origin.pem > /dev/null
    
    if [ $? -eq 0 ]; then
        echo "✅ Certificate saved successfully!"
        sudo chmod 644 /etc/ssl/certs/cloudflare-origin.pem
        sudo chown root:root /etc/ssl/certs/cloudflare-origin.pem
    else
        echo "❌ Failed to save certificate"
        exit 1
    fi
}

# Function to input private key
setup_private_key() {
    echo ""
    echo "🔑 Setting up Private Key..."
    echo "Copy and paste the ENTIRE private key content (including -----BEGIN PRIVATE KEY----- and -----END PRIVATE KEY-----)"
    echo "Press Ctrl+D when finished:"
    
    sudo tee /etc/ssl/private/cloudflare-origin.key > /dev/null
    
    if [ $? -eq 0 ]; then
        echo "✅ Private key saved successfully!"
        sudo chmod 600 /etc/ssl/private/cloudflare-origin.key
        sudo chown root:root /etc/ssl/private/cloudflare-origin.key
    else
        echo "❌ Failed to save private key"
        exit 1
    fi
}

# Check if certificates already exist
if [ -f "/etc/ssl/certs/cloudflare-origin.pem" ] && [ -f "/etc/ssl/private/cloudflare-origin.key" ]; then
    echo "⚠️  SSL certificates already exist!"
    echo "Do you want to replace them? (y/N): "
    read -r response
    if [[ ! "$response" =~ ^[Yy]$ ]]; then
        echo "Skipping certificate setup..."
        exit 0
    fi
fi

echo "Ready to input certificates? (y/N): "
read -r ready

if [[ "$ready" =~ ^[Yy]$ ]]; then
    setup_certificate
    setup_private_key
    
    echo ""
    echo "🧪 Testing certificate setup..."
    
    # Verify certificate files exist and have correct permissions
    if [ -f "/etc/ssl/certs/cloudflare-origin.pem" ] && [ -f "/etc/ssl/private/cloudflare-origin.key" ]; then
        echo "✅ Certificate files exist"
        
        # Check file permissions
        cert_perms=$(stat -c "%a" /etc/ssl/certs/cloudflare-origin.pem)
        key_perms=$(stat -c "%a" /etc/ssl/private/cloudflare-origin.key)
        
        if [ "$cert_perms" = "644" ] && [ "$key_perms" = "600" ]; then
            echo "✅ File permissions are correct"
        else
            echo "⚠️  Fixing file permissions..."
            sudo chmod 644 /etc/ssl/certs/cloudflare-origin.pem
            sudo chmod 600 /etc/ssl/private/cloudflare-origin.key
        fi
        
        # Test certificate validity
        if openssl x509 -in /etc/ssl/certs/cloudflare-origin.pem -text -noout > /dev/null 2>&1; then
            echo "✅ Certificate is valid"
            
            # Show certificate details
            echo ""
            echo "📋 Certificate Details:"
            openssl x509 -in /etc/ssl/certs/cloudflare-origin.pem -text -noout | grep -A 2 "Subject:"
            openssl x509 -in /etc/ssl/certs/cloudflare-origin.pem -text -noout | grep -A 2 "Validity"
            openssl x509 -in /etc/ssl/certs/cloudflare-origin.pem -text -noout | grep -A 5 "Subject Alternative Name"
        else
            echo "❌ Certificate is invalid"
            exit 1
        fi
        
        # Test private key
        if openssl rsa -in /etc/ssl/private/cloudflare-origin.key -check > /dev/null 2>&1; then
            echo "✅ Private key is valid"
        else
            echo "❌ Private key is invalid"
            exit 1
        fi
        
        echo ""
        echo "🎉 SSL setup completed successfully!"
        echo ""
        echo "Next steps:"
        echo "1. Configure Cloudflare SSL/TLS settings:"
        echo "   - Go to SSL/TLS → Overview"
        echo "   - Set encryption mode to 'Full (strict)'"
        echo "   - Enable 'Always Use HTTPS'"
        echo ""
        echo "2. Test Nginx configuration:"
        echo "   sudo nginx -t"
        echo ""
        echo "3. Reload Nginx:"
        echo "   sudo systemctl reload nginx"
        echo ""
        echo "4. Test your SSL:"
        echo "   curl -I https://backend.shancloudservice.com/health"
        
    else
        echo "❌ Certificate setup failed"
        exit 1
    fi
else
    echo "Certificate setup cancelled."
    echo ""
    echo "Alternative: You can manually create the certificate files:"
    echo "sudo nano /etc/ssl/certs/cloudflare-origin.pem"
    echo "sudo nano /etc/ssl/private/cloudflare-origin.key"
    echo ""
    echo "Then set permissions:"
    echo "sudo chmod 644 /etc/ssl/certs/cloudflare-origin.pem"
    echo "sudo chmod 600 /etc/ssl/private/cloudflare-origin.key"
fi