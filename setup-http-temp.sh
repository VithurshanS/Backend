#!/bin/bash

# Temporary HTTP Setup Script
# This sets up HTTP access temporarily while we get SSL certificates

echo "🌐 Setting up temporary HTTP-only Nginx configuration..."

# Create temporary HTTP-only Nginx config
sudo tee /etc/nginx/sites-available/tutorverse > /dev/null << 'EOF'
server {
    listen 80;
    server_name backend.shancloudservice.com www.backend.shancloudservice.com;
    
    # File Upload Size
    client_max_body_size 100M;
    
    # Proxy to Spring Boot Application
    location / {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
        
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
    
    # Test endpoint
    location /test {
        return 200 "Nginx is working! Backend: http://localhost:8080";
        add_header Content-Type text/plain;
    }
}
EOF

echo "🔄 Reloading Nginx..."
sudo nginx -t && sudo systemctl reload nginx

echo "✅ Temporary HTTP setup complete!"
echo ""
echo "You can now test:"
echo "1. Nginx test: curl http://backend.shancloudservice.com/test"
echo "2. Backend API: curl http://backend.shancloudservice.com/"
echo "3. Health check: curl http://backend.shancloudservice.com/health"