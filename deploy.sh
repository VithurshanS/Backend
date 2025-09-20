#!/bin/bash

# Tutorverse Backend - Quick Deployment Script
# Usage: ./deploy.sh [domain-name]

set -e

DOMAIN=${1:-"backend.shancloudservice.com"}
BACKEND_DIR="/home/vithurshan/backend"

echo "🚀 Starting Tutorverse Backend Deployment for domain: $DOMAIN"

# Step 1: Update system and install dependencies
echo "📦 Installing dependencies..."
sudo apt update && sudo apt upgrade -y
sudo apt install nginx curl -y

# Step 2: Create application directory
echo "📁 Setting up application directory..."
mkdir -p $BACKEND_DIR
cd $BACKEND_DIR

# Step 3: Pull the latest Docker image
echo "🐳 Pulling latest Docker image..."
docker pull vithurshansiva/tutorverse:latest

# Step 4: Create docker-compose.yml
echo "📄 Creating docker-compose.yml..."
cat > docker-compose.yml << 'EOF'
version: '3.8'

services:
  tutorverse-backend:
    image: vithurshansiva/tutorverse:latest
    container_name: tutorverse-backend
    restart: always
    ports:
      - "8080:8080"
    env_file:
      - .env.production
    
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 60s
    
    logging:
      driver: "json-file"
      options:
        max-size: "100m"
        max-file: "3"
    
    volumes:
      - ./logs:/app/logs
EOF

# Step 5: Create Nginx configuration
echo "🌐 Setting up Nginx configuration..."
sudo tee /etc/nginx/sites-available/tutorverse > /dev/null << EOF
server {
    listen 80;
    server_name $DOMAIN www.$DOMAIN;
    
    # Redirect all HTTP to HTTPS
    return 301 https://\$server_name\$request_uri;
}

server {
    listen 443 ssl http2;
    server_name $DOMAIN www.$DOMAIN;
    
    # SSL Configuration (Cloudflare Origin Certificate)
    ssl_certificate /etc/ssl/certs/cloudflare-origin.pem;
    ssl_certificate_key /etc/ssl/private/cloudflare-origin.key;
    
    # SSL Security Settings
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    
    # Security Headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header X-Content-Type-Options "nosniff" always;
    
    # Cloudflare Real IP
    set_real_ip_from 173.245.48.0/20;
    set_real_ip_from 103.21.244.0/22;
    set_real_ip_from 103.22.200.0/22;
    set_real_ip_from 103.31.4.0/22;
    set_real_ip_from 141.101.64.0/18;
    set_real_ip_from 108.162.192.0/18;
    set_real_ip_from 190.93.240.0/20;
    set_real_ip_from 188.114.96.0/20;
    set_real_ip_from 197.234.240.0/22;
    set_real_ip_from 198.41.128.0/17;
    set_real_ip_from 162.158.0.0/15;
    set_real_ip_from 104.16.0.0/13;
    set_real_ip_from 104.24.0.0/14;
    set_real_ip_from 172.64.0.0/13;
    set_real_ip_from 131.0.72.0/22;
    real_ip_header CF-Connecting-IP;
    
    # File Upload Size
    client_max_body_size 100M;
    
    # Proxy to Spring Boot Application
    location / {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_cache_bypass \$http_upgrade;
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # Health Check Endpoint
    location /health {
        proxy_pass http://localhost:8080/actuator/health;
        access_log off;
    }
}
EOF

# Step 6: Enable the site
echo "⚙️ Enabling Nginx site..."
sudo ln -sf /etc/nginx/sites-available/tutorverse /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default

# Step 7: Configure firewall


# Step 8: Check if .env.production exists
if [ ! -f ".env.production" ]; then
    echo "⚠️  WARNING: .env.production file not found!"
    echo "Please create .env.production file with your environment variables"
    echo "You can use: scp .env.production vithurshan@34.58.223.92:/home/vithurshan/backend"
    exit 1
fi

echo "✅ Deployment setup complete!"
echo ""
echo "Next steps:"
echo "1. 📋 Upload SSL certificates to /etc/ssl/certs/cloudflare-origin.pem and /etc/ssl/private/cloudflare-origin.key"
echo "2. 🔧 Update .env.production with correct FRONTEND_URL and BACKEND_URL"
echo "3. 🌐 Configure Cloudflare DNS to point to this server (34.58.223.92)"
echo "4. 🚀 Run 'docker-compose up -d' to start the application"
echo "5. 🧪 Test with 'sudo nginx -t && sudo systemctl reload nginx'"
echo ""
echo "Your application will be available at: https://$DOMAIN"