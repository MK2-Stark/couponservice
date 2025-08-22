# Jenkins Freestyle Job Configuration for Automated Testing

## Overview

This guide shows how to configure your existing Jenkins freestyle job to automatically run tests and create JAR files when you push to your GitHub repository.

## Step-by-Step Configuration

### 1. Job Basic Settings

- **Job Name**: Keep your existing job name
- **Description**: "Automated build and test for Coupon Service"

### 2. Source Code Management

- ✅ **Git**
- **Repository URL**: `https://github.com/YOUR_USERNAME/couponservice.git`
- **Credentials**: Your GitHub credentials
- **Branch Specifier**: `*/main` (or `*/master` depending on your default branch)

### 3. Build Triggers

Configure to trigger automatically on GitHub pushes:

**Option A: GitHub Hook Trigger (Recommended)**

- ✅ Check "GitHub hook trigger for GITScm polling"
- This requires webhook setup in GitHub (see GitHub Webhook Setup section below)

**Option B: Poll SCM (Alternative)**

- ✅ Check "Poll SCM"
- **Schedule**: `H/5 * * * *` (checks every 5 minutes)

### 4. Build Environment

- ✅ Check "Delete workspace before build starts" (ensures clean builds)

### 5. Build Steps

Add these build steps in order:

#### Build Step 1: Clean and Compile

- **Add build step** → **Execute shell** (Linux/Mac) or **Execute Windows batch command** (Windows)

**For Linux/Mac:**

```bash
echo "=== CLEANING WORKSPACE ==="
mvn clean

echo "=== COMPILING APPLICATION ==="
mvn compile
```

**For Windows:**

```batch
echo === CLEANING WORKSPACE ===
mvn clean

echo === COMPILING APPLICATION ===
mvn compile
```

#### Build Step 2: Run Tests

- **Add build step** → **Execute shell** (Linux/Mac) or **Execute Windows batch command** (Windows)

**For Linux/Mac:**

```bash
echo "=== RUNNING UNIT TESTS ==="
mvn test

echo "=== RUNNING INTEGRATION TESTS ==="
mvn verify

echo "=== GENERATING TEST REPORTS ==="
mvn surefire-report:report
```

**For Windows:**

```batch
echo === RUNNING UNIT TESTS ===
mvn test

echo === RUNNING INTEGRATION TESTS ===
mvn verify

echo === GENERATING TEST REPORTS ===
mvn surefire-report:report
```

#### Build Step 3: Package Application

- **Add build step** → **Execute shell** (Linux/Mac) or **Execute Windows batch command** (Windows)

**For Linux/Mac:**

```bash
echo "=== PACKAGING APPLICATION ==="
mvn package

echo "=== BUILD COMPLETED SUCCESSFULLY ==="
ls -la target/*.jar
```

**For Windows:**

```batch
echo === PACKAGING APPLICATION ===
mvn package

echo === BUILD COMPLETED SUCCESSFULLY ===
dir target\*.jar
```

### 6. Post-build Actions

Add these post-build actions to capture results:

#### Test Results

- **Add post-build action** → **Publish JUnit test result report**
- **Test report XMLs**: `target/surefire-reports/*.xml`
- ✅ Check "Retain long standard output/error"

#### Archive Artifacts

- **Add post-build action** → **Archive the artifacts**
- **Files to archive**: `target/*.jar`
- **Advanced** → ✅ Check "Archive artifacts only if build is successful"

#### Email Notifications (Optional)

- **Add post-build action** → **E-mail Notification**
- Configure email settings for build failures

### 7. Advanced Configuration

#### Build Timeout

- **Add build step** → **Build Environment** → ✅ **Abort the build if it's stuck**
- **Time-out strategy**: Absolute
- **Timeout minutes**: 15

#### Console Output

- **Add post-build action** → **Set build description**
- **Description**: `Build #${BUILD_NUMBER} - Tests: ${TEST_COUNTS,var="total"}`

## GitHub Webhook Setup (For Automatic Triggers)

### ⚠️ **Important: Localhost Issue**

You correctly identified the problem! `http://localhost:8080/github-webhook/` won't work because GitHub cannot reach your local machine. Here are the solutions:

### **Solution 1: Use ngrok (Recommended for Development)**

**Step 1: Install ngrok**

- Download from https://ngrok.com/
- Create free account and get auth token
- Install: `npm install -g ngrok` or download binary

**Step 2: Expose Jenkins**

```bash
# Start ngrok tunnel to your Jenkins
ngrok http 8080
```

**Step 3: Get Public URL**

- ngrok will show: `Forwarding https://abc123.ngrok.io -> http://localhost:8080`
- Your webhook URL becomes: `https://abc123.ngrok.io/github-webhook/`

**Step 4: Configure GitHub Webhook**

1. Go to your repository → **Settings** → **Webhooks**
2. Click **Add webhook**
3. **Payload URL**: `https://abc123.ngrok.io/github-webhook/` (use your ngrok URL)
4. **Content type**: `application/json`
5. **Which events**: Select "Just the push event"
6. ✅ **Active**
7. Click **Add webhook**

### **Solution 2: Use Poll SCM (No Webhook Needed)**

If you can't use ngrok, configure Jenkins to check GitHub periodically:

**In Jenkins Job Configuration:**

- **Build Triggers** → ✅ **Poll SCM**
- **Schedule**: `H/2 * * * *` (checks every 2 minutes)
- This works without webhooks but has slight delay

### **Solution 3: Cloud Jenkins (Production)**

For production, use cloud-hosted Jenkins:

- **AWS**: Jenkins on EC2 with public IP
- **Azure**: Jenkins on Azure VM
- **Google Cloud**: Jenkins on Compute Engine
- **DigitalOcean**: Jenkins droplet

Example public URL: `http://your-server-ip:8080/github-webhook/`

### **Solution 4: Jenkins Tunnel Plugin**

Alternative using Jenkins plugin:

1. **Manage Jenkins** → **Manage Plugins**
2. Install **"GitHub Integration Plugin"**
3. Configure automatic polling without webhooks

### **Recommended Setup for Your Case:**

Since you're running Jenkins locally, use **Solution 1 (ngrok)** for development:

```bash
# Terminal 1: Start Jenkins (if not already running)
# Jenkins should be at http://localhost:8080

# Terminal 2: Start ngrok tunnel
ngrok http 8080

# Copy the https URL from ngrok output
# Example: https://abc123.ngrok.io
```

**Then in GitHub webhook:**

- **Payload URL**: `https://abc123.ngrok.io/github-webhook/`

### **Testing the Webhook**

After setup, test it:

1. Make a small change to your code
2. Commit and push to GitHub
3. Check Jenkins dashboard - build should trigger automatically
4. Check GitHub webhook deliveries for success/failure

### **In Jenkins (Global Configuration):**

1. **Manage Jenkins** → **Configure System**
2. Find **GitHub** section
3. **Add GitHub Server**
4. **API URL**: `https://api.github.com`
5. **Credentials**: Add your GitHub personal access token
6. **Test connection** to verify

### **GitHub Personal Access Token Setup:**

1. GitHub → **Settings** → **Developer settings** → **Personal access tokens**
2. **Generate new token (classic)**
3. **Scopes**: Select `repo` and `admin:repo_hook`
4. Copy token and add to Jenkins credentials

## Build Script Alternative (Single Build Step)

If you prefer a single build step, create this comprehensive script:

**For Linux/Mac (build.sh):**

```bash
#!/bin/bash
set -e

echo "=== COUPON SERVICE BUILD STARTED ==="
echo "Build Number: ${BUILD_NUMBER}"
echo "Git Commit: ${GIT_COMMIT}"
echo "=================================="

# Clean workspace
echo "=== CLEANING WORKSPACE ==="
mvn clean

# Compile
echo "=== COMPILING APPLICATION ==="
mvn compile

# Run tests
echo "=== RUNNING ALL TESTS ==="
mvn test verify

# Package
echo "=== PACKAGING APPLICATION ==="
mvn package

# Verify JAR creation
echo "=== VERIFYING BUILD ARTIFACTS ==="
if [ -f target/couponservice-0.0.1-SNAPSHOT.jar ]; then
    echo "✅ JAR file created successfully"
    ls -la target/*.jar
else
    echo "❌ JAR file creation failed"
    exit 1
fi

echo "=== BUILD COMPLETED SUCCESSFULLY ==="
```

**For Windows (build.bat):**

```batch
@echo off
echo === COUPON SERVICE BUILD STARTED ===
echo Build Number: %BUILD_NUMBER%
echo Git Commit: %GIT_COMMIT%
echo ==================================

echo === CLEANING WORKSPACE ===
mvn clean
if %errorlevel% neq 0 exit /b %errorlevel%

echo === COMPILING APPLICATION ===
mvn compile
if %errorlevel% neq 0 exit /b %errorlevel%

echo === RUNNING ALL TESTS ===
mvn test verify
if %errorlevel% neq 0 exit /b %errorlevel%

echo === PACKAGING APPLICATION ===
mvn package
if %errorlevel% neq 0 exit /b %errorlevel%

echo === VERIFYING BUILD ARTIFACTS ===
if exist target\couponservice-0.0.1-SNAPSHOT.jar (
    echo ✅ JAR file created successfully
    dir target\*.jar
) else (
    echo ❌ JAR file creation failed
    exit /b 1
)

echo === BUILD COMPLETED SUCCESSFULLY ===
```

## Troubleshooting Common Issues

### Issue 1: Maven Not Found

**Solution**: Configure Maven in Jenkins

- **Manage Jenkins** → **Global Tool Configuration**
- **Maven** → **Add Maven**
- **Name**: `Maven-3.8`
- **Install automatically**: ✅
- **Version**: Latest

### Issue 2: Java Version Issues

**Solution**: Configure JDK

- **Manage Jenkins** → **Global Tool Configuration**
- **JDK** → **Add JDK**
- **Name**: `JDK-17`
- **JAVA_HOME**: Path to your Java 17 installation

### Issue 3: Tests Failing in Jenkins but Passing Locally

**Solution**: Add environment variables

- **Build Environment** → ✅ **Set environment variables**
- Add: `SPRING_PROFILES_ACTIVE=test`

### Issue 4: Permission Issues (Linux/Mac)

**Solution**: Make scripts executable

```bash
chmod +x build.sh
```

## Expected Build Output

When successful, you should see:

```
=== COUPON SERVICE BUILD STARTED ===
=== CLEANING WORKSPACE ===
=== COMPILING APPLICATION ===
=== RUNNING ALL TESTS ===
Tests run: 33, Failures: 0, Errors: 0, Skipped: 0
=== PACKAGING APPLICATION ===
✅ JAR file created successfully
couponservice-0.0.1-SNAPSHOT.jar
=== BUILD COMPLETED SUCCESSFULLY ===
```

## Next Steps After Configuration

1. **Test the setup**: Make a small change and push to GitHub
2. **Monitor the build**: Check Jenkins dashboard for automatic trigger
3. **Verify artifacts**: Ensure JAR file is created and archived
4. **Check test reports**: Review test results in Jenkins UI

Your freestyle job will now automatically build, test, and package your application whenever you push changes to GitHub!
