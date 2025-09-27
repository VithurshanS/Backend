# GitHub Actions Secrets Configuration

This document lists all the GitHub Actions secrets that need to be configured in your repository settings for the CI/CD pipeline to work.

## Required Secrets

### Docker Hub Configuration
- **DOCKER_USERNAME**: Your Docker Hub username (e.g., `vithurshansiva`)
- **DOCKER_PASSWORD**: Your Docker Hub access token (NOT your Google password)

### Google Cloud Platform Configuration
- **GCP_SERVICE_ACCOUNT_KEY**: Base64 encoded service account key JSON file
- **GCP_PROJECT_ID**: Your Google Cloud Project ID
- **GCE_INSTANCE_NAME**: Name of your GCE instance
- **GCE_ZONE**: Zone where your GCE instance is located (e.g., `us-central1-a`)
- **GCE_USERNAME**: Username for SSH access to your GCE instance

## Setup Instructions

### 1. Docker Hub Setup (For Google Sign-in Users)

Since you use Google to sign in to Docker Hub, you need to create an access token:

#### Step 1: Create Docker Hub Access Token
1. Go to [Docker Hub](https://hub.docker.com/)
2. Sign in with your Google account
3. Click on your username in the top right → Account Settings
4. Go to "Security" tab
5. Click "New Access Token"
6. Give it a name like "GitHub Actions CI/CD"
7. Select permissions: "Read, Write, Delete" (or "Public Repo Read/Write" if you prefer)
8. Click "Generate"
9. **IMPORTANT**: Copy the token immediately - you won't be able to see it again!

#### Step 2: Add Secrets to GitHub
1. Go to your GitHub repository → Settings → Secrets and variables → Actions
2. Click "New repository secret"
3. Add these secrets:
   - Name: `DOCKER_USERNAME`, Value: Your Docker Hub username (e.g., `vithurshansiva`)
   - Name: `DOCKER_PASSWORD`, Value: The access token you just created (NOT your Google password)

### 2. Google Cloud Platform Setup

#### Create Service Account:
```bash
# Create service account
gcloud iam service-accounts create github-actions-deploy \
    --description="Service account for GitHub Actions deployment" \
    --display-name="GitHub Actions Deploy"

# Get your project ID
export PROJECT_ID=$(gcloud config get-value project)

# Grant necessary permissions
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:github-actions-deploy@$PROJECT_ID.iam.gserviceaccount.com" \
    --role="roles/compute.instanceAdmin.v1"

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:github-actions-deploy@$PROJECT_ID.iam.gserviceaccount.com" \
    --role="roles/compute.osLogin"

# Create and download key
gcloud iam service-accounts keys create github-actions-key.json \
    --iam-account=github-actions-deploy@$PROJECT_ID.iam.gserviceaccount.com

# Encode the key file to base64
base64 -w 0 github-actions-key.json > github-actions-key-base64.txt
```

#### Add secrets to GitHub:
1. Go to your GitHub repository → Settings → Secrets and variables → Actions
2. Add the following secrets:
   - `GCP_SERVICE_ACCOUNT_KEY`: Content of `github-actions-key-base64.txt`
   - `GCP_PROJECT_ID`: Your Google Cloud project ID
   - `GCE_INSTANCE_NAME`: Your GCE instance name
   - `GCE_ZONE`: Your GCE instance zone
   - `GCE_USERNAME`: Your username on the GCE instance

### 3. GCE Instance Setup

#### Install Docker and Docker Compose on your GCE instance:
```bash
# Update system
sudo apt update

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.21.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Log out and back in for group changes to take effect
```

#### Setup application directory:
```bash
# Create application directory
mkdir -p ~/tutorverse-backend
cd ~/tutorverse-backend

# Clone your repository (optional, for docker-compose files)
git clone https://github.com/VithurshanS/Backend.git .

# Copy the deployment script
chmod +x deploy-gce.sh

# Create environment file
cp .env.example .env.production  # Make sure to configure this with your actual values
```

### 4. Test the Setup

#### Manual test:
```bash
# Test the deployment script manually
./deploy-gce.sh deploy v1.0.6
```

#### Test GitHub Actions:
1. Create a new tag and push it to trigger the workflow:
```bash
git tag v1.0.7
git push fork v1.0.7
```

## Workflow Trigger

The CI/CD pipeline will trigger on:
- **Tags**: When you push a tag starting with 'v' (e.g., v1.0.6, v2.1.0)
- **Manual**: Using GitHub Actions workflow dispatch

## Deployment Process

1. **Build Stage**: Builds JAR file and creates GitHub release
2. **Docker Stage**: Downloads JAR from release, builds Docker image, pushes to Docker Hub
3. **Deploy Stage**: SSH into GCE instance, updates docker-compose file, pulls new image, restarts containers

## Monitoring

Check deployment status:
```bash
# On your GCE instance
./deploy-gce.sh status
./deploy-gce.sh logs
```

## Rollback

If something goes wrong:
```bash
# On your GCE instance
./deploy-gce.sh rollback
```