# ============================================
# TUTORVERSE BACKEND - DOCKER RUN COMMANDS
# ============================================

# Option 1: Run with Environment File (Recommended)
# ==================================================

# For Development:
docker run -d \
  --name tutorverse-backend-dev \
  -p 8080:8080 \
  --env-file .env.development \
  tutorverse-backend:latest

# For Production:
docker run -d \
  --name tutorverse-backend-prod \
  -p 8080:8080 \
  --env-file .env.production \
  tutorverse-backend:latest

# Option 2: Run with Individual Environment Variables
# ==================================================

docker run -d \
  --name tutorverse-backend \
  -p 8080:8080 \
  -e FRONTEND_URL=https://your-frontend-domain.com \
  -e BACKEND_URL=https://your-backend-domain.com \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/tutorverse \
  -e SPRING_DATASOURCE_USERNAME=your-db-username \
  -e SPRING_DATASOURCE_PASSWORD=your-db-password \
  -e GOOGLE_CLIENT_ID=your-google-client-id \
  -e GOOGLE_CLIENT_SECRET=your-google-client-secret \
  -e AWS_ACCESS_KEY=your-aws-access-key \
  -e AWS_SECRET_KEY=your-aws-secret-key \
  -e AWS_REGION=us-east-1 \
  -e AWS_BUCKET_NAME=your-bucket-name \
  -e SENDGRID_API_KEY=your-sendgrid-key \
  tutorverse-backend:latest

# Option 3: Run with Custom Port
# ==============================

docker run -d \
  --name tutorverse-backend \
  -p 9090:8080 \
  --env-file .env.production \
  tutorverse-backend:latest

# Option 4: Run in Development Mode with Volume Mount (for logs)
# ==============================================================

docker run -d \
  --name tutorverse-backend-dev \
  -p 8080:8080 \
  --env-file .env.development \
  -v $(pwd)/logs:/app/logs \
  tutorverse-backend:latest

# Option 5: Run with Network (if connecting to containerized database)
# ===================================================================

# First create a network
docker network create tutorverse-network

# Run backend with custom network
docker run -d \
  --name tutorverse-backend \
  --network tutorverse-network \
  -p 8080:8080 \
  --env-file .env.production \
  tutorverse-backend:latest

# ============================================
# USEFUL DOCKER COMMANDS
# ============================================

# Check if container is running
docker ps

# View container logs
docker logs tutorverse-backend

# Follow container logs in real-time
docker logs -f tutorverse-backend

# Stop the container
docker stop tutorverse-backend

# Remove the container
docker rm tutorverse-backend

# Remove the container forcefully
docker rm -f tutorverse-backend

# Execute bash inside the running container (for debugging)
docker exec -it tutorverse-backend /bin/bash

# Check container resource usage
docker stats tutorverse-backend

# Restart the container
docker restart tutorverse-backend

# ============================================
# HEALTH CHECK COMMANDS
# ============================================

# Check if Spring Boot app is running
curl http://localhost:8080/actuator/health

# Or if actuator is not enabled, check any public endpoint
curl http://localhost:8080/api/health

# Test the container locally before deployment
docker run --rm -p 8080:8080 --env-file .env.development tutorverse-backend:latest