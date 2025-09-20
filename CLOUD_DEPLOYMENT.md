# 🚀 Tutorverse Backend - Cloud Deployment Guide

## Prerequisites
- Cloud instance (Google Cloud, AWS, DigitalOcean, etc.) running Ubuntu/Debian
- Docker installed on the cloud instance
- Domain name configured with Cloudflare
- SSH access to your cloud instance

## Architecture Overview
```
Internet → Cloudflare (SSL/DNS) → Nginx (Reverse Proxy) → Docker Container (Backend)
         HTTPS (443)              HTTP (80) → HTTP (8080)
```

## Part 1: Cloud Instance Setup

### 1.1 Connect to Your Cloud Instance
```bash
# Replace with your actual IP and username
ssh vithurshan@34.58.223.92
```

### 1.2 Install Docker (if not installed)
```bash
# Update package manager
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Add user to docker group
sudo usermod -aG docker $USER

# Log out and back in, or run:
newgrp docker

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

### 1.3 Install Nginx
```bash
sudo apt install nginx -y
sudo systemctl enable nginx
sudo systemctl start nginx
```

## Part 2: Deploy Your Application

### 2.1 Create Application Directory
```bash
mkdir -p /home/vithurshan/backend
cd /home/vithurshan/backend
```

### 2.2 Upload Environment File
You've already done this step:
```bash
# This was already executed
scp .env.production vithurshan@34.58.223.92:/home/vithurshan/backend
```

### 2.3 Create Docker Compose File for Production
```bash
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
    
    deploy:
      resources:
        limits:
          memory: 2G
          cpus: '1.0'
        reservations:
          memory: 1G
          cpus: '0.5'
    
    logging:
      driver: "json-file"
      options:
        max-size: "100m"
        max-file: "3"
    
    volumes:
      - ./logs:/app/logs
      - /etc/timezone:/etc/timezone:ro
      - /etc/localtime:/etc/localtime:ro

networks:
  default:
    name: tutorverse-network
EOF
```

### 2.4 Pull and Run the Container
```bash
# Pull the latest image
docker pull vithurshansiva/tutorverse:latest

# Start the application
docker-compose up -d

# Check if it's running
docker-compose ps
docker-compose logs -f
```

## Part 3: Configure Nginx Reverse Proxy

### 3.1 Create Nginx Configuration
```bash
sudo cat > /etc/nginx/sites-available/tutorverse << 'EOF'
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;  # Replace with your actual domain
    
    # Redirect all HTTP to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com www.your-domain.com;  # Replace with your actual domain
    
    # SSL Configuration (Let's Encrypt or Cloudflare Origin Certificate)
    ssl_certificate /etc/ssl/certs/cloudflare-origin.pem;
    ssl_certificate_key /etc/ssl/private/cloudflare-origin.key;
    
    # SSL Security Settings
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;
    
    # Security Headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;
    add_header Content-Security-Policy "default-src 'self' http: https: data: blob: 'unsafe-inline'" always;
    
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
    
    # Gzip Compression
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_proxied expired no-cache no-store private must-revalidate auth;
    gzip_types text/plain text/css text/xml text/javascript application/x-javascript application/xml+rss application/json;
    
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
    
    # Health Check Endpoint (optional)
    location /health {
        proxy_pass http://localhost:8080/actuator/health;
        access_log off;
    }
}
EOF
```

### 3.2 Enable the Site
```bash
# Enable the site
sudo ln -sf /etc/nginx/sites-available/tutorverse /etc/nginx/sites-enabled/

# Remove default site
sudo rm -f /etc/nginx/sites-enabled/default

# Test configuration
sudo nginx -t

# Reload Nginx
sudo systemctl reload nginx
```

## Part 4: Cloudflare Configuration

### 4.1 DNS Setup in Cloudflare Dashboard
1. Go to Cloudflare Dashboard → Your Domain → DNS
2. Add/Update these records:

```
Type: A
Name: @ (or your-subdomain)
Content: 34.58.223.92 (your server IP)
Proxy: Enabled (orange cloud)

Type: A
Name: www
Content: 34.58.223.92 (your server IP)
Proxy: Enabled (orange cloud)
```

### 4.2 SSL/TLS Settings in Cloudflare
1. Go to SSL/TLS → Overview
2. Set encryption mode to **"Full (strict)"** or **"Full"**
3. Go to SSL/TLS → Origin Server
4. Create Origin Certificate (download .pem and .key files)

### 4.3 Install Cloudflare Origin Certificate
```bash
# Create SSL directory
sudo mkdir -p /etc/ssl/certs /etc/ssl/private

# Upload your Cloudflare origin certificate
sudo nano /etc/ssl/certs/cloudflare-origin.pem
# Paste the .pem content here

sudo nano /etc/ssl/private/cloudflare-origin.key
# Paste the .key content here

# Set proper permissions
sudo chmod 644 /etc/ssl/certs/cloudflare-origin.pem
sudo chmod 600 /etc/ssl/private/cloudflare-origin.key
```

## Part 5: Security & Firewall

### 5.1 Configure UFW Firewall
```bash
# Enable firewall
sudo ufw enable

# Allow SSH
sudo ufw allow ssh

# Allow HTTP and HTTPS
sudo ufw allow 80
sudo ufw allow 443

# Check status
sudo ufw status
```

### 5.2 Fail2Ban (Optional but Recommended)
```bash
sudo apt install fail2ban -y
sudo systemctl enable fail2ban
sudo systemctl start fail2ban
```

## Part 6: Update Environment Variables

Update your `.env.production` file on the server:
```bash
cd /home/vithurshan/backend
nano .env.production
```

Make sure these are correct:
```env
FRONTEND_URL=https://your-frontend-domain.com
BACKEND_URL=https://your-backend-domain.com
```

Then restart the container:
```bash
docker-compose down
docker-compose up -d
```

## Part 7: Google OAuth2 Configuration

Update your Google Cloud Console OAuth2 settings:

**Authorized Redirect URIs:**
```
https://your-domain.com/login/oauth2/code/google
```

**Authorized JavaScript Origins:**
```
https://your-frontend-domain.com
```

## Deployment Commands Summary

```bash
# On your local machine - push image to Docker Hub
docker push vithurshansiva/tutorverse:latest

# On cloud instance - deploy
cd /home/vithurshan/backend
docker pull vithurshansiva/tutorverse:latest
docker-compose down
docker-compose up -d

# Check logs
docker-compose logs -f

# Check status
curl https://your-domain.com/actuator/health
```

## Monitoring & Maintenance

### Check Application Status
```bash
# Container status
docker-compose ps

# Application logs
docker-compose logs -f tutorverse-backend

# System resources
docker stats

# Nginx logs
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log
```

### Update Deployment
```bash
# Pull latest image
docker pull vithurshansiva/tutorverse:latest

# Restart with zero downtime
docker-compose up -d --no-deps tutorverse-backend
```

## Troubleshooting

### Common Issues:
1. **502 Bad Gateway**: Check if Docker container is running
2. **SSL Certificate Issues**: Verify Cloudflare Origin Certificate
3. **CORS Errors**: Check FRONTEND_URL environment variable
4. **OAuth2 Failures**: Verify Google Cloud Console redirect URIs

### Debug Commands:
```bash
# Check container health
docker-compose exec tutorverse-backend curl localhost:8080/actuator/health

# Check Nginx configuration
sudo nginx -t

# Check firewall
sudo ufw status

# Check DNS resolution
nslookup your-domain.com
```

Your backend will be accessible at: `https://your-domain.com` 🚀