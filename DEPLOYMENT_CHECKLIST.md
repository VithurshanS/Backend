# 🚀 Tutorverse Backend - Complete Deployment Checklist

## ✅ Pre-Deployment Setup (Local Machine)

- [x] ✅ **Docker image built**: `tutorverse-backend:latest`
- [x] ✅ **Image pushed to Docker Hub**: `vithurshansiva/tutorverse:latest`
- [x] ✅ **Environment file uploaded**: `.env.production` → server
- [ ] 🔄 **Google OAuth2 redirect URIs updated** in Google Cloud Console
- [ ] 📋 **Cloudflare Origin Certificates** ready

## 🌐 Cloudflare Configuration Required

### DNS Records (Cloudflare Dashboard → DNS)
```
Type: A
Name: backend
Content: 34.58.223.92
Proxy: ✅ Enabled (Orange Cloud)

Type: A  
Name: frontend
Content: [Your Frontend Server IP]
Proxy: ✅ Enabled (Orange Cloud)
```

### SSL/TLS Settings (Cloudflare Dashboard → SSL/TLS)
- [ ] **Overview**: Set encryption mode to **"Full (strict)"**
- [ ] **Edge Certificates**: Enable **"Always Use HTTPS"**
- [ ] **Origin Server**: Create Origin Certificate for `*.shancloudservice.com`

## 📋 Google OAuth2 Update Required

**Google Cloud Console → APIs & Services → Credentials**

Update **Authorized Redirect URIs**:
```
https://backend.shancloudservice.com/login/oauth2/code/google
```

Update **Authorized JavaScript Origins**:
```
https://frontend.shancloudservice.com
```

## 🖥️ Server Deployment Commands

### 1. Connect to Server
```bash
ssh vithurshan@34.58.223.92
```

### 2. Upload Deployment Files
```bash
# From local machine
scp deploy.sh ssl-setup.sh vithurshan@34.58.223.92:~/
```

### 3. Run Deployment Script
```bash
# On server
chmod +x ~/deploy.sh ~/ssl-setup.sh
sudo ~/deploy.sh backend.shancloudservice.com
```

### 4. Set Up SSL Certificates
```bash
# On server
sudo ~/ssl-setup.sh
```

### 5. Start Application
```bash
# On server
cd /home/vithurshan/backend
docker-compose up -d
```

### 6. Verify Deployment
```bash
# Check container status
docker-compose ps

# Check application logs
docker-compose logs -f

# Test health endpoint
curl http://localhost:8080/actuator/health

# Test SSL (after Cloudflare setup)
curl -I https://backend.shancloudservice.com/health
```

## 🔧 Manual Commands Reference

### Deploy/Update Application
```bash
# Pull latest image and restart
cd /home/vithurshan/backend
docker pull vithurshansiva/tutorverse:latest
docker-compose down
docker-compose up -d

# Check logs
docker-compose logs -f tutorverse-backend
```

### Nginx Management
```bash
# Test configuration
sudo nginx -t

# Reload configuration
sudo systemctl reload nginx

# Check status
sudo systemctl status nginx

# View logs
sudo tail -f /var/log/nginx/error.log
sudo tail -f /var/log/nginx/access.log
```

### SSL Certificate Management
```bash
# Check certificate details
openssl x509 -in /etc/ssl/certs/cloudflare-origin.pem -text -noout | grep -A 2 "Validity"

# Test SSL connection
openssl s_client -connect backend.shancloudservice.com:443 -servername backend.shancloudservice.com
```

### Container Management
```bash
# View all containers
docker ps -a

# View container stats
docker stats tutorverse-backend

# Execute bash in container
docker exec -it tutorverse-backend /bin/bash

# View container logs
docker logs -f tutorverse-backend
```

## 🔍 Troubleshooting

### Common Issues & Solutions

**1. 502 Bad Gateway**
```bash
# Check if container is running
docker-compose ps

# Check container logs
docker-compose logs tutorverse-backend

# Restart container
docker-compose restart tutorverse-backend
```

**2. SSL Certificate Errors**
```bash
# Verify certificate files exist
ls -la /etc/ssl/certs/cloudflare-origin.pem
ls -la /etc/ssl/private/cloudflare-origin.key

# Check certificate validity
openssl x509 -in /etc/ssl/certs/cloudflare-origin.pem -text -noout

# Test nginx configuration
sudo nginx -t
```

**3. OAuth2 Redirect Errors**
- Verify Google Cloud Console redirect URIs
- Check `BACKEND_URL` in `.env.production`
- Ensure Cloudflare DNS is pointing to correct IP

**4. Database Connection Issues**
```bash
# Check environment variables
docker exec tutorverse-backend env | grep SPRING_DATASOURCE

# Test database connection from container
docker exec -it tutorverse-backend /bin/bash
curl http://localhost:8080/actuator/health
```

## 📊 Monitoring Commands

### Health Checks
```bash
# Application health
curl http://localhost:8080/actuator/health

# SSL certificate expiry
openssl x509 -in /etc/ssl/certs/cloudflare-origin.pem -text -noout | grep "Not After"

# System resources
free -h
df -h
top
```

### Log Monitoring
```bash
# Application logs
docker-compose logs -f --tail=100 tutorverse-backend

# Nginx access logs
sudo tail -f /var/log/nginx/access.log

# System logs
sudo journalctl -f -u nginx
sudo journalctl -f -u docker
```

## 🎯 Expected Results After Deployment

- ✅ **Application accessible**: `https://backend.shancloudservice.com`
- ✅ **Health check working**: `https://backend.shancloudservice.com/health`
- ✅ **SSL certificate valid**: A+ rating on SSL Labs test
- ✅ **Google OAuth2 working**: Login redirects properly
- ✅ **API endpoints responding**: All REST APIs functional
- ✅ **Database connected**: Application can read/write data
- ✅ **File uploads working**: AWS S3 integration functional
- ✅ **Email sending working**: SendGrid integration functional

## 📞 Support

If you encounter issues:
1. Check container logs: `docker-compose logs -f`
2. Check nginx logs: `sudo tail -f /var/log/nginx/error.log`
3. Verify environment variables: `docker exec tutorverse-backend env`
4. Test database connectivity: Check health endpoint
5. Verify Cloudflare settings: DNS and SSL configuration

Your Tutorverse backend will be fully operational at: **https://backend.shancloudservice.com** 🚀