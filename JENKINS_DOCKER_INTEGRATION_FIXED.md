# Jenkins Docker Integration for College Assignment - FIXED VERSION

## 📋 **Assignment Requirements Fulfilled**

This implementation demonstrates:

1. ✅ **Docker Image Creation**: Automated through Jenkins
2. ✅ **Jenkins Automation**: Complete CI/CD pipeline with Docker
3. ✅ **Application Server Deployment**: Containerized deployment

## 🚀 **Jenkins Automated Docker Deployment (Option 2)**

### **Complete Automated Workflow**

```
GitHub Push → Webhook → Jenkins → Build → Test → Package → Docker Build → Deploy → Verify
```

## 🔧 **Step-by-Step Jenkins Configuration**

### **Prerequisites**

1. **Docker installed on Jenkins server**
2. **Jenkins user added to docker group**
3. **Your existing Jenkins freestyle job working**

### **Enhanced Jenkins Job Configuration**

#### **Add These Build Steps to Your Existing Job:**

**Build Step 4: Docker Image Creation**

**For Windows (Use "Execute Windows batch command"):**

```batch
echo === BUILDING DOCKER IMAGE ===
docker build -t coupon-service:%BUILD_NUMBER% .
docker tag coupon-service:%BUILD_NUMBER% coupon-service:latest

echo === DOCKER IMAGE CREATED ===
docker images | findstr coupon-service
```

**Build Step 5: Docker Container Deployment (FIXED VERSION)**

**For Windows (Use "Execute Windows batch command"):**

```batch
echo === STOPPING EXISTING CONTAINERS ===
docker-compose down 2>nul || echo No containers to stop

echo === CLEANING UP PORT 3306 ===
netstat -ano | findstr :3306 > temp_ports.txt 2>nul
if exist temp_ports.txt (
    for /f "tokens=5" %%a in (temp_ports.txt) do (
        echo Killing process %%a on port 3306
        taskkill /f /pid %%a 2>nul || echo Process %%a already stopped
    )
    del temp_ports.txt
) else (
    echo Port 3306 is free
)

echo === DEPLOYING NEW VERSION ===
docker-compose up -d --build

echo === WAITING FOR APPLICATION STARTUP ===
ping 127.0.0.1 -n 46 > nul

echo === VERIFYING DEPLOYMENT ===
curl -f http://localhost:9091/actuator/health
if %errorlevel% neq 0 exit /b 1

echo === TESTING API ENDPOINTS ===
curl -f http://localhost:9091/couponapi/coupons || echo No coupons yet (expected)

echo === DEPLOYMENT COMPLETED SUCCESSFULLY ===
docker ps | findstr coupon
```

#### **Updated Post-build Actions:**

- **Archive artifacts**: `target/*.jar, Dockerfile, docker-compose.yml`
- **Publish JUnit results**: `target/surefire-reports/*.xml`

## 🔧 **Key Fixes Applied**

### **Issue 1: Port 3306 Conflict**

- **Problem**: MySQL port already in use
- **Solution**: Added port cleanup script to kill existing MySQL processes

### **Issue 2: Windows Timeout Command**

- **Problem**: `timeout /t 45 /nobreak` caused input redirection error
- **Solution**: Replaced with `ping 127.0.0.1 -n 46 > nul` (45-second delay)

## 📊 **Complete Jenkins Build Process**

### **Your Enhanced Jenkins Job Will:**

1. **Clean & Compile** (existing)

   - `mvn clean compile`

2. **Run Tests** (existing)

   - `mvn test verify`
   - All 44 tests execute (as shown in your console)

3. **Package Application** (existing)

   - `mvn package`
   - Creates JAR file

4. **Build Docker Image** (NEW)

   - `docker build -t coupon-service:%BUILD_NUMBER% .`
   - Creates containerized application

5. **Deploy Containers** (NEW - FIXED)

   - Cleans up port conflicts
   - `docker-compose up -d --build`
   - Deploys app + MySQL database

6. **Verify Deployment** (NEW)
   - Health checks and API testing
   - Confirms successful deployment

## 🎯 **What the Fixed Version Does**

### **Port Cleanup Process:**

1. **Checks for processes** using port 3306
2. **Kills existing MySQL processes** if found
3. **Ensures clean deployment** without port conflicts

### **Windows-Compatible Wait:**

1. **Uses ping command** instead of timeout
2. **Avoids input redirection issues**
3. **Provides proper 45-second delay**

## 🚀 **Next Steps for Your Assignment**

1. **Replace your current Jenkins build step 5** with the fixed version above
2. **Test the pipeline** by pushing code to GitHub
3. **Verify the fixes work** - no more port conflicts or timeout errors

## 📋 **Expected Console Output (Fixed)**

```
=== STOPPING EXISTING CONTAINERS ===
No containers to stop

=== CLEANING UP PORT 3306 ===
Port 3306 is free

=== DEPLOYING NEW VERSION ===
Creating network "couponservice_coupon-network" with the default driver
Creating coupon-mysql ... done
Creating coupon-app   ... done

=== WAITING FOR APPLICATION STARTUP ===

=== VERIFYING DEPLOYMENT ===
{"status":"UP"}

=== TESTING API ENDPOINTS ===
[]

=== DEPLOYMENT COMPLETED SUCCESSFULLY ===
coupon-app    Up 30 seconds   0.0.0.0:9091->9091/tcp
coupon-mysql  Up 45 seconds   0.0.0.0:3306->3306/tcp

Finished: SUCCESS
```

## 🎉 **Assignment Success**

With these fixes, your Jenkins pipeline will:

- ✅ **Build successfully** without port conflicts
- ✅ **Deploy containers** properly
- ✅ **Verify health** automatically
- ✅ **Complete the full CI/CD cycle**

Your college assignment will demonstrate a professional-grade, fully automated Docker deployment pipeline!
