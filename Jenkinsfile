pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9.0' // Adjust version based on your Jenkins Maven configuration
        jdk 'JDK-17' // Adjust based on your Jenkins JDK configuration
    }
    
    environment {
        MAVEN_OPTS = '-Xmx1024m'
        SONAR_TOKEN = credentials('sonar-token') // Configure this in Jenkins credentials
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                echo 'Building the application...'
                sh 'mvn clean compile'
            }
        }
        
        stage('Unit Tests') {
            steps {
                echo 'Running unit tests...'
                sh 'mvn test'
            }
            post {
                always {
                    // Publish test results
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                    
                    // Archive test reports
                    archiveArtifacts artifacts: 'target/surefire-reports/**/*', allowEmptyArchive: true
                }
            }
        }
        
        stage('Integration Tests') {
            steps {
                echo 'Running integration tests...'
                sh 'mvn verify -DskipUnitTests=true'
            }
            post {
                always {
                    // Publish integration test results
                    publishTestResults testResultsPattern: 'target/failsafe-reports/*.xml'
                    
                    // Archive integration test reports
                    archiveArtifacts artifacts: 'target/failsafe-reports/**/*', allowEmptyArchive: true
                }
            }
        }
        
        stage('Code Coverage') {
            steps {
                echo 'Generating code coverage report...'
                sh 'mvn jacoco:report'
            }
            post {
                always {
                    // Publish coverage reports
                    publishCoverage adapters: [
                        jacocoAdapter('target/site/jacoco/jacoco.xml')
                    ], sourceFileResolver: sourceFiles('STORE_LAST_BUILD')
                    
                    // Archive coverage reports
                    archiveArtifacts artifacts: 'target/site/jacoco/**/*', allowEmptyArchive: true
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                echo 'Checking quality gate...'
                script {
                    // Check if coverage meets minimum threshold
                    def coverageResult = sh(
                        script: 'mvn jacoco:check',
                        returnStatus: true
                    )
                    
                    if (coverageResult != 0) {
                        error('Code coverage below threshold!')
                    }
                }
            }
        }
        
        stage('SonarQube Analysis') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    changeRequest()
                }
            }
            steps {
                echo 'Running SonarQube analysis...'
                withSonarQubeEnv('SonarQube') { // Configure SonarQube server in Jenkins
                    sh '''
                        mvn sonar:sonar \
                        -Dsonar.projectKey=couponservice \
                        -Dsonar.host.url=$SONAR_HOST_URL \
                        -Dsonar.login=$SONAR_TOKEN
                    '''
                }
            }
        }
        
        stage('SonarQube Quality Gate') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    changeRequest()
                }
            }
            steps {
                echo 'Waiting for SonarQube Quality Gate...'
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        
        stage('Package') {
            steps {
                echo 'Packaging the application...'
                sh 'mvn package -DskipTests'
            }
            post {
                always {
                    // Archive the built artifacts
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }
        
        stage('Security Scan') {
            steps {
                echo 'Running security scan...'
                sh 'mvn org.owasp:dependency-check-maven:check'
            }
            post {
                always {
                    // Archive security reports
                    archiveArtifacts artifacts: 'target/dependency-check-report.html', allowEmptyArchive: true
                }
            }
        }
        
        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            steps {
                echo 'Deploying to staging environment...'
                // Add your staging deployment commands here
                sh '''
                    echo "Deploying to staging..."
                    # Example: docker build and deploy commands
                    # docker build -t couponservice:staging .
                    # docker run -d --name couponservice-staging couponservice:staging
                '''
            }
        }
        
        stage('Smoke Tests') {
            when {
                branch 'develop'
            }
            steps {
                echo 'Running smoke tests on staging...'
                // Add smoke test commands here
                sh '''
                    echo "Running smoke tests..."
                    # Example: curl commands to test basic functionality
                    # curl -f http://staging-server:9091/couponapi/coupons || exit 1
                '''
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                echo 'Deploying to production environment...'
                input message: 'Deploy to production?', ok: 'Deploy'
                // Add your production deployment commands here
                sh '''
                    echo "Deploying to production..."
                    # Example: production deployment commands
                    # docker build -t couponservice:latest .
                    # docker run -d --name couponservice-prod couponservice:latest
                '''
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline completed!'
            
            // Clean workspace
            cleanWs()
        }
        
        success {
            echo 'Pipeline succeeded!'
            
            // Send success notification
            emailext (
                subject: "✅ Pipeline Success: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: """
                    <h2>Pipeline Successful</h2>
                    <p><strong>Job:</strong> ${env.JOB_NAME}</p>
                    <p><strong>Build Number:</strong> ${env.BUILD_NUMBER}</p>
                    <p><strong>Branch:</strong> ${env.BRANCH_NAME}</p>
                    <p><strong>Build URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                    
                    <h3>Test Results Summary:</h3>
                    <p>All tests passed successfully!</p>
                """,
                mimeType: 'text/html',
                to: '${DEFAULT_RECIPIENTS}'
            )
        }
        
        failure {
            echo 'Pipeline failed!'
            
            // Send failure notification
            emailext (
                subject: "❌ Pipeline Failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: """
                    <h2>Pipeline Failed</h2>
                    <p><strong>Job:</strong> ${env.JOB_NAME}</p>
                    <p><strong>Build Number:</strong> ${env.BUILD_NUMBER}</p>
                    <p><strong>Branch:</strong> ${env.BRANCH_NAME}</p>
                    <p><strong>Build URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                    
                    <h3>Failure Details:</h3>
                    <p>Please check the build logs for more information.</p>
                """,
                mimeType: 'text/html',
                to: '${DEFAULT_RECIPIENTS}'
            )
        }
        
        unstable {
            echo 'Pipeline is unstable!'
            
            // Send unstable notification
            emailext (
                subject: "⚠️ Pipeline Unstable: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: """
                    <h2>Pipeline Unstable</h2>
                    <p><strong>Job:</strong> ${env.JOB_NAME}</p>
                    <p><strong>Build Number:</strong> ${env.BUILD_NUMBER}</p>
                    <p><strong>Branch:</strong> ${env.BRANCH_NAME}</p>
                    <p><strong>Build URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                    
                    <h3>Issues:</h3>
                    <p>Some tests may have failed or quality gates not met.</p>
                """,
                mimeType: 'text/html',
                to: '${DEFAULT_RECIPIENTS}'
            )
        }
    }
}
