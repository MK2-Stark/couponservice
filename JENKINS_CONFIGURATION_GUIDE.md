# Jenkins Configuration Guide for Coupon Service

## Option 1: Using Jenkinsfile (Pipeline as Code) - RECOMMENDED ✅

Since we created a `Jenkinsfile` in your project root, Jenkins can automatically use this pipeline configuration.

### Jenkins Configuration Steps:

1. **In Jenkins Dashboard**: Go to your couponservice project
2. **Configure Project**: Click "Configure"
3. **Pipeline Section**:
   - Definition: Select "Pipeline script from SCM"
   - SCM: Select "Git"
   - Repository URL: Your GitHub repository URL
   - Script Path: `Jenkinsfile` (default)
4. **Save Configuration**

### What Happens:

- ✅ Jenkins will automatically read the `Jenkinsfile` from your repository
- ✅ All build steps, test execution, and reporting are defined in the Jenkinsfile
- ✅ **NO MANUAL BUILD STEPS NEEDED** in Jenkins UI
- ✅ Tests run automatically: `mvn clean compile test verify`

---

## Option 2: Manual Build Steps Configuration

If you prefer to configure build steps manually in Jenkins UI:

### Build Steps to Add:

```bash
# Step 1: Clean and compile
mvn clean compile

# Step 2: Run unit tests
mvn test

# Step 3: Run integration tests and generate reports
mvn verify

# Step 4: Generate coverage report
mvn jacoco:report
```

### Post-Build Actions to Configure:

- **Publish JUnit test results**: `target/surefire-reports/*.xml`
- **Publish JaCoCo coverage reports**: `target/site/jacoco/jacoco.xml`
- **Archive artifacts**: `target/*.jar, target/site/jacoco/**`

---

## RECOMMENDATION: Use Option 1 (Jenkinsfile)

### Why Jenkinsfile is Better:

✅ **Version Controlled**: Pipeline configuration is stored with your code
✅ **Consistent**: Same pipeline runs everywhere (dev, staging, prod)
✅ **Maintainable**: Changes to pipeline are tracked in Git
✅ **Automated**: No manual Jenkins UI configuration needed
✅ **Complete**: Includes all stages, quality gates, and reporting

### Current Jenkinsfile Includes:

- **Checkout**: Automatically pulls latest code
- **Build**: `mvn clean compile`
- **Test**: `mvn test` (unit tests)
- **Integration Test**: `mvn verify` (integration + E2E tests)
- **Code Coverage**: `mvn jacoco:report`
- **Quality Gate**: Fails build if coverage < 80%
- **Reporting**: Publishes test results and coverage reports
- **Notifications**: Email notifications on build status

---

## Current Status Check

### What You Should Do:

1. **Check your Jenkins project configuration**
2. **If using Jenkinsfile approach**:
   - Set Pipeline definition to "Pipeline script from SCM"
   - Point to your GitHub repository
   - **DO NOT add manual build steps**
3. **If using manual approach**:
   - Add the Maven commands listed above in build steps
   - Configure post-build actions for reporting

### Test the Configuration:

1. **Commit and push** the current changes to GitHub
2. **Jenkins should automatically trigger** a build
3. **Check build console output** to see test execution
4. **Verify test reports** are published in Jenkins

---

## Expected Build Output

When Jenkins runs (either approach), you should see:

```
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] --- jacoco-maven-plugin:0.8.10:report (report) @ couponservice ---
[INFO] Loading execution data file target/jacoco.exec
[INFO] Analyzed bundle 'couponservice' with 3 classes
[INFO]
BUILD SUCCESS
```

### Coverage Report Location:

- **Jenkins**: Published JaCoCo reports in build artifacts
- **Local**: `target/site/jacoco/index.html`

---

## Troubleshooting

### If Tests Don't Run:

1. **Check Jenkins logs** for Maven command execution
2. **Verify Java 17** is configured in Jenkins
3. **Check repository permissions** for Jenkins to access GitHub
4. **Validate Jenkinsfile syntax** (should be valid as created)

### If Coverage Reports Missing:

1. **Ensure JaCoCo plugin** is executing: `mvn jacoco:report`
2. **Check post-build actions** are configured for JaCoCo
3. **Verify target/site/jacoco/** directory is created

---

## Summary

**RECOMMENDED APPROACH**: Use the Jenkinsfile we created

- ✅ Configure Jenkins to use "Pipeline script from SCM"
- ✅ Point to your GitHub repository
- ✅ **DO NOT add manual build steps**
- ✅ Commit and push to trigger automated build with tests

The Jenkinsfile handles everything automatically including test execution, coverage reporting, and quality gates!
