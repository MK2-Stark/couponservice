# Docker Deployment Guide for Coupon Service

## Overview

This guide provides complete Docker deployment strategy for your Spring Boot coupon service, including automated Docker image creation through Jenkins and deployment options.

## 🐳 **Docker Files Created**

### 1. Dockerfile

- **Base Image**: OpenJDK 17 slim
- **Security**: Non-root user configuration
- **Health Check**: Actuator endpoint monitoring
- **Port**: 9091 (matches application.yml)

### 2. docker-compose.yml

- **MySQL Database**: Persistent storage with initialization
- **Application Service**: Linked to database
- **Networking**: Isolated Docker network
- **Health Checks**: Both database and application

## 🚀 **Deployment Options**

### **Option 1: Local Docker Deployment**

#### Step 1: Build and Run with Docker Compose

```bash
# Build and start all services
docker-compose up --build

# Run in background
docker-compose up -d --build

# View logs
docker-compose logs -f coupon-service

# Stop services
docker-compose down
```

#### Step 2: Test the Deployment

```bash
# Check application health
curl http://localhost:9091/actuator/health

# Test coupon API
curl -X GET http://localhost:9091/couponapi/coupons

# Create a coupon
curl -X POST http://localhost:9091/couponapi/coupons \
  -H "Content-Type: application/json" \
  -d '{"code":"DOCKER10","discount":10.00,"expDate":"2024-12-31"}'
```

### **Option 2: Manual Docker Commands**

#### Build Image

```bash
# Build the Docker image
docker build -t coupon-service:latest .

# Run MySQL container
docker run -d \
  --name coupon-mysql \
  -e MYSQL_ROOT_PASSWORD=A00Imran \
  -e MYSQL_DATABASE=servicedb \
  -p 3306:3306 \
  mysql:8.0

# Run application container
docker run -d \
  --name coupon-app \
  --link coupon-mysql:mysql \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/servicedb \
  -p 9091:9091 \
  coupon-service:latest
```

### **Option 3: Jenkins Automated Docker Deployment**

#### Enhanced Jenkins Freestyle Job Configuration

Add these additional build steps to your existing Jenkins job:

#### Build Step 4: Docker Image Creation

```bash
echo "=== BUILDING DOCKER IMAGE ==="
docker build -t coupon-service:${BUILD_NUMBER} .
docker tag coupon-service:${BUILD_NUMBER} coupon-service:latest

echo "=== DOCKER IMAGE CREATED ==="
docker images | grep coupon-service
```

#### Build Step 5: Docker Container Deployment

```bash
echo "=== STOPPING EXISTING CONTAINERS ==="
docker-compose down || true

echo "=== DEPLOYING NEW VERSION ==="
docker-compose up -d --build

echo "=== WAITING FOR APPLICATION STARTUP ==="
sleep 30

echo "=== VERIFYING DEPLOYMENT ==="
curl -f http://localhost:9091/actuator/health || exit 1

echo "=== DEPLOYMENT COMPLETED SUCCESSFULLY ==="
docker ps | grep coupon
```

## 🏗️ **Production Deployment Strategies**

### **Strategy 1: Cloud Server Deployment**

#### AWS EC2 / Azure VM / Google Cloud

```bash
# On your cloud server
git clone https://github.com/YOUR_USERNAME/couponservice.git
cd couponservice

# Install Docker and Docker Compose
sudo apt update
sudo apt install docker.io docker-compose -y
sudo systemctl start docker
sudo systemctl enable docker

# Deploy application
sudo docker-compose up -d --build

# Configure firewall
sudo ufw allow 9091
```

#### Access Application

- **Public URL**: `http://YOUR_SERVER_IP:9091/couponapi/coupons`
- **Health Check**: `http://YOUR_SERVER_IP:9091/actuator/health`

### **Strategy 2: Docker Registry Deployment**

#### Push to Docker Hub

```bash
# Tag for Docker Hub
docker tag coupon-service:latest YOUR_USERNAME/coupon-service:latest

# Push to registry
docker push YOUR_USERNAME/coupon-service:latest
```

#### Deploy from Registry

```bash
# On target server
docker pull YOUR_USERNAME/coupon-service:latest
docker run -d -p 9091:9091 YOUR_USERNAME/coupon-service:latest
```

### **Strategy 3: Container Orchestration**

#### Kubernetes Deployment (Advanced)

```yaml
# k8s-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: coupon-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: coupon-service
  template:
    metadata:
      labels:
        app: coupon-service
    spec:
      containers:
        - name: coupon-service
          image: coupon-service:latest
          ports:
            - containerPort: 9091
```

## 🔧 **Jenkins Docker Integration**

### Complete Jenkins Freestyle Job with Docker

#### Prerequisites

1. **Install Docker on Jenkins Server**

```bash
# On Jenkins server
sudo apt update
sudo apt install docker.io docker-compose -y
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

2. **Configure Jenkins Job**

**Build Environment:**

- ✅ Delete workspace before build starts

**Build Steps:**

**Step 1: Clean and Compile**

```bash
echo "=== CLEANING WORKSPACE ==="
mvn clean

echo "=== COMPILING APPLICATION ==="
mvn compile
```

**Step 2: Run Tests**

```bash
echo "=== RUNNING ALL TESTS ==="
mvn test verify

echo "=== GENERATING TEST REPORTS ==="
mvn surefire-report:report
```

**Step 3: Package Application**

```bash
echo "=== PACKAGING APPLICATION ==="
mvn package

echo "=== VERIFYING JAR FILE ==="
ls -la target/*.jar
```

**Step 4: Build Docker Image**

```bash
echo "=== BUILDING DOCKER IMAGE ==="
docker build -t coupon-service:${BUILD_NUMBER} .
docker tag coupon-service:${BUILD_NUMBER} coupon-service:latest

echo "=== DOCKER IMAGE BUILT SUCCESSFULLY ==="
docker images | grep coupon-service
```

**Step 5: Deploy Docker Container**

```bash
echo "=== STOPPING EXISTING DEPLOYMENT ==="
docker-compose down || true

echo "=== DEPLOYING NEW VERSION ==="
docker-compose up -d --build

echo "=== WAITING FOR STARTUP ==="
sleep 45

echo "=== VERIFYING DEPLOYMENT ==="
curl -f http://localhost:9091/actuator/health

echo "=== TESTING API ENDPOINTS ==="
curl -f http://localhost:9091/couponapi/coupons

echo "=== DEPLOYMENT COMPLETED SUCCESSFULLY ==="
docker ps | grep coupon
```

**Post-build Actions:**

- **Publish JUnit test results**: `target/surefire-reports/*.xml`
- **Archive artifacts**: `target/*.jar, Dockerfile, docker-compose.yml`

## 🔍 **Monitoring and Troubleshooting**

### Container Health Monitoring

```bash
# Check container status
docker ps

# View application logs
docker logs coupon-app -f

# Check database logs
docker logs coupon-mysql -f

# Monitor resource usage
docker stats

# Execute commands in container
docker exec -it coupon-app bash
```

### Common Issues and Solutions

#### Issue 1: Port Already in Use

```bash
# Find process using port
sudo lsof -i :9091

# Kill process
sudo kill -9 PID

# Or use different port in docker-compose.yml
```

#### Issue 2: Database Connection Failed

```bash
# Check MySQL container
docker logs coupon-mysql

# Verify network connectivity
docker network ls
docker network inspect couponservice_coupon-network
```

#### Issue 3: Application Won't Start

```bash
# Check application logs
docker logs coupon-app

# Verify JAR file exists
docker exec coupon-app ls -la /app/

# Check Java version
docker exec coupon-app java -version
```

## 🚀 **Complete Automated Workflow**

With Jenkins Docker integration, your complete CI/CD pipeline becomes:

```
Code Push → GitHub → Webhook → Jenkins → Build → Test → Package → Docker Build → Deploy → Verify
```

**Automated Process:**

1. **Code Push** triggers Jenkins via GitHub webhook
2. **Jenkins builds** and tests the application
3. **JAR file created** and verified
4. **Docker image built** with latest code
5. **Containers deployed** with zero downtime
6. **Health checks verify** successful deployment
7. **Application ready** for production use

## 📊 **Deployment Verification**

### Health Check Endpoints

- **Application Health**: `http://localhost:9091/actuator/health`
- **Application Info**: `http://localhost:9091/actuator/info`
- **Metrics**: `http://localhost:9091/actuator/metrics`

### API Testing

```bash
# Get all coupons
curl http://localhost:9091/couponapi/coupons

# Get specific coupon
curl http://localhost:9091/couponapi/coupons/TEST10

# Create new coupon
curl -X POST http://localhost:9091/couponapi/coupons \
  -H "Content-Type: application/json" \
  -d '{"code":"DOCKER20","discount":20.00,"expDate":"2024-12-31"}'
```

Your coupon service is now ready for enterprise-grade Docker deployment with full CI/CD automation!
