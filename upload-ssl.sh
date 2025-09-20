#!/bin/bash

# SSL Certificate Upload Script
# Usage: ./upload-ssl.sh

echo "🔒 SSL Certificate Upload Script"
echo "Make sure you have created the certificate files:"
echo "- cloudflare-origin.pem (Origin Certificate)"
echo "- cloudflare-origin.key (Private Key)"
echo ""

SERVER="34.58.223.92"
USER="vithurshan"

# Check if certificate files exist
if [ ! -f "cloudflare-origin.pem" ]; then
    echo "❌ cloudflare-origin.pem not found!"
    echo "Please download the Origin Certificate from Cloudflare and save it as cloudflare-origin.pem"
    exit 1
fi

if [ ! -f "cloudflare-origin.key" ]; then
    echo "❌ cloudflare-origin.key not found!"
    echo "Please download the Private Key from Cloudflare and save it as cloudflare-origin.key"
    exit 1
fi

echo "📤 Uploading SSL certificates to server..."

# Upload certificates to server
scp cloudflare-origin.pem $USER@$SERVER:/tmp/
scp cloudflare-origin.key $USER@$SERVER:/tmp/

# Move certificates to proper locations on server
ssh $USER@$SERVER << 'EOF'
    echo "📁 Creating SSL directories..."
    sudo mkdir -p /etc/ssl/certs
    sudo mkdir -p /etc/ssl/private
    
    echo "🔒 Installing SSL certificates..."
    sudo mv /tmp/cloudflare-origin.pem /etc/ssl/certs/
    sudo mv /tmp/cloudflare-origin.key /etc/ssl/private/
    
    echo "🔧 Setting proper permissions..."
    sudo chmod 644 /etc/ssl/certs/cloudflare-origin.pem
    sudo chmod 600 /etc/ssl/private/cloudflare-origin.key
    sudo chown root:root /etc/ssl/certs/cloudflare-origin.pem
    sudo chown root:root /etc/ssl/private/cloudflare-origin.key
    
    echo "✅ SSL certificates installed successfully!"
    
    echo "🧪 Testing Nginx configuration..."
    sudo nginx -t
    
    if [ $? -eq 0 ]; then
        echo "🌐 Starting Nginx..."
        sudo systemctl enable nginx
        sudo systemctl start nginx
        sudo systemctl reload nginx
        echo "✅ Nginx started successfully!"
    else
        echo "❌ Nginx configuration test failed!"
    fi
EOF

echo "🎉 SSL setup complete!"
echo ""
echo "Next steps:"
echo "1. Test HTTP access: curl -I http://backend.shancloudservice.com"
echo "2. Test HTTPS access: curl -I https://backend.shancloudservice.com"
echo "3. Test API health: curl https://backend.shancloudservice.com/health"