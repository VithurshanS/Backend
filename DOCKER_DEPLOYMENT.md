# 🚀 Tutorverse Backend Docker Deployment Guide

## Prerequisites
- Docker installed on your system
- Docker Compose installed
- Your environment variables configured

## Quick Start

### 1. Build the Application
```bash
# Build the JAR file
mvn clean package -DskipTests

# Build the Docker image
docker build -t tutorverse-backend:latest .
```

### 2. Configure Environment Variables
Copy and modify the appropriate environment file:
```bash
# For development
cp .env.development .env
# Edit .env with your actual values

# For production
cp .env.production .env
# Edit .env with your actual production values
```

### 3. Run with Docker Compose (Recommended)

#### Development:
```bash
docker-compose up -d
```

#### Production:
```bash
docker-compose -f docker-compose.prod.yml up -d
```

### 4. Run with Docker Commands

#### Simple run with environment file:
```bash
docker run -d --name tutorverse-backend -p 8080:8080 --env-file .env tutorverse-backend:latest
```

## Environment Variables Required

### Core Configuration
- `FRONTEND_URL`: Your frontend application URL
- `BACKEND_URL`: Your backend application URL
- `SPRING_DATASOURCE_URL`: PostgreSQL database URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password

### OAuth2 Configuration
- `GOOGLE_CLIENT_ID`: Google OAuth2 client ID
- `GOOGLE_CLIENT_SECRET`: Google OAuth2 client secret

### AWS S3 Configuration
- `AWS_ACCESS_KEY`: AWS access key
- `AWS_SECRET_KEY`: AWS secret key
- `AWS_REGION`: AWS region (e.g., us-east-1)
- `AWS_BUCKET_NAME`: S3 bucket name

### Email Configuration
- `SENDGRID_API_KEY`: SendGrid API key for email services

## Google OAuth2 Setup

Make sure to configure these URLs in Google Cloud Console:

### Development:
- **Authorized Redirect URIs**: `http://localhost:8080/login/oauth2/code/google`
- **Authorized JavaScript Origins**: `http://localhost:3000`

### Production:
- **Authorized Redirect URIs**: `https://your-backend-domain.com/login/oauth2/code/google`
- **Authorized JavaScript Origins**: `https://your-frontend-domain.com`

## Container Management

### View logs:
```bash
docker logs -f tutorverse-backend
```

### Check health:
```bash
curl http://localhost:8080/actuator/health
```

### Stop container:
```bash
docker-compose down
# or
docker stop tutorverse-backend
```

### Update deployment:
```bash
# Rebuild and restart
mvn clean package -DskipTests
docker build -t tutorverse-backend:latest .
docker-compose down
docker-compose up -d
```

## Production Deployment Checklist

- [ ] Configure production environment variables
- [ ] Set up production database
- [ ] Configure Google OAuth2 with production URLs
- [ ] Set up AWS S3 bucket and IAM permissions
- [ ] Configure SendGrid API key
- [ ] Set up SSL/TLS certificates
- [ ] Configure reverse proxy (Nginx/Apache)
- [ ] Set up monitoring and logging
- [ ] Configure backup strategy
- [ ] Test all endpoints

## Troubleshooting

### Common Issues:

1. **Container won't start**: Check logs with `docker logs tutorverse-backend`
2. **Database connection failed**: Verify database URL and credentials
3. **OAuth2 errors**: Check Google Cloud Console configuration
4. **CORS errors**: Verify `FRONTEND_URL` environment variable

### Debug Mode:
```bash
# Run container interactively for debugging
docker run -it --rm --env-file .env tutorverse-backend:latest /bin/bash
```

## File Structure
```
Backend/
├── Dockerfile
├── docker-compose.yml
├── docker-compose.prod.yml
├── .env.development
├── .env.production
├── docker-run-commands.md
└── target/
    └── Tutorverse-0.0.1-SNAPSHOT.jar
```