#!/bin/bash

# Fix Docker Health Check Script
# This script updates the docker-compose.yml to use a proper health check

echo "🔧 Fixing Docker health check configuration..."

# Update docker-compose.yml with better health check
cat > docker-compose.yml << 'EOF'
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
      test: ["CMD-SHELL", "curl -f http://localhost:8080/ || exit 1"]
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

echo "✅ Updated docker-compose.yml with fixed health check"
echo "🔄 Restarting container with new configuration..."

# Restart the container
sudo docker compose down
sudo docker compose up -d

echo "✅ Container restarted successfully!"
echo ""
echo "Now checking container status..."
sleep 10
sudo docker ps
echo ""
echo "Container logs:"
sudo docker compose logs --tail=20