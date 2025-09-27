# Quick Setup Guide for Docker Hub Authentication

## ⚠️ Important: Google Sign-in Users

Since you sign in to Docker Hub with Google, you **CANNOT** use your Google password for GitHub Actions. You must create an access token.

## Step-by-Step Setup

### 1. Create Docker Hub Access Token

1. **Go to Docker Hub**: https://hub.docker.com/
2. **Sign in** with your Google account
3. **Click your username** (top right) → **Account Settings**
4. **Go to "Security" tab**
5. **Click "New Access Token"**
6. **Configure the token**:
   - Name: `GitHub Actions CI/CD`
   - Permissions: Select `Read, Write, Delete`
7. **Click "Generate"**
8. **⚠️ COPY THE TOKEN IMMEDIATELY** - you won't see it again!

### 2. Add GitHub Secrets

Go to your GitHub repository: `https://github.com/VithurshanS/Backend`

1. **Settings** → **Secrets and variables** → **Actions**
2. **Click "New repository secret"**
3. **Add these two secrets**:

   **Secret 1:**
   - Name: `DOCKER_USERNAME`
   - Value: `vithurshansiva` (your Docker Hub username)

   **Secret 2:**
   - Name: `DOCKER_PASSWORD`
   - Value: `[paste the access token you copied]`

### 3. Test the Setup

1. **Create and push a new tag**:
   ```bash
   git tag v1.0.7
   git push fork v1.0.7
   ```

2. **Check GitHub Actions**:
   - Go to your repository → Actions tab
   - Watch the workflow run
   - The "build-and-push-docker" job should succeed

### 4. Verify Docker Hub

- Go to Docker Hub → Repositories
- You should see `vithurshansiva/tutorverse` with the new tag

## Troubleshooting

**If you get "authentication failed" error:**
- Make sure you used the access token, not your Google password
- Make sure the token has "Read, Write, Delete" permissions
- Regenerate the token if needed

**If the workflow doesn't trigger:**
- Make sure you pushed the tag to your fork (`git push fork v1.0.7`)
- Check that the tag starts with 'v' (like v1.0.7, v2.1.0, etc.)

## Next Steps

After Docker Hub authentication is working, you can set up GCE deployment by following the `CICD_SETUP.md` guide.