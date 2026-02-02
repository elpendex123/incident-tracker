pipeline {
    agent any

    tools {
        maven 'Maven 3.6.3'
        jdk 'JDK 17'
    }

    environment {
        APP_NAME = 'incident-tracker'
        DB_HOST = 'localhost'
        DB_PORT = '5432'
        DB_NAME = 'incidents'
        DB_USER = 'postgres'
    }

    stages {
        stage('Checkout') {
            steps {
                echo '=== Checking out source code ==='
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo '=== Building application with Maven ==='
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                echo '=== Running unit and integration tests ==='
                sh 'mvn test'
            }
            post {
                always {
                    echo 'Publishing test results...'
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Deploy to Local') {
            steps {
                echo '=== Deploying application locally ==='
                sh '''
                    # Stop existing process if running
                    pkill -f 'incident-tracker' || true
                    sleep 2

                    # Create logs directory if it doesn't exist
                    mkdir -p logs

                    # Start application in background
                    nohup java -jar target/*.jar > logs/app.log 2>&1 &

                    # Save PID for later reference
                    echo $! > app.pid
                    sleep 3
                '''
            }
        }

        stage('Health Check') {
            steps {
                echo '=== Checking application health ==='
                script {
                    timeout(time: 2, unit: 'MINUTES') {
                        waitUntil {
                            script {
                                def response = sh(
                                    script: 'curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/actuator/health',
                                    returnStdout: true
                                ).trim()

                                if (response == '200') {
                                    echo '✓ Application is healthy!'
                                    return true
                                } else {
                                    echo "Health check returned: ${response}"
                                    return false
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('Verify API Endpoints') {
            steps {
                echo '=== Verifying API endpoints ==='
                sh '''
                    echo "Testing REST API..."
                    curl -s http://localhost:8081/api/incidents | jq . || true

                    echo ""
                    echo "Testing Swagger UI..."
                    curl -s -o /dev/null -w "Swagger UI: %{http_code}\n" http://localhost:8081/swagger-ui.html

                    echo "Testing GraphiQL..."
                    curl -s -o /dev/null -w "GraphiQL: %{http_code}\n" http://localhost:8081/graphiql
                '''
            }
        }
    }

    post {
        always {
            echo '=== Cleaning up workspace ==='
            cleanWs()
        }

        success {
            echo '✓ Pipeline completed successfully!'
        }

        failure {
            echo '✗ Pipeline failed!'
            sh '''
                # Stop the application if build failed
                pkill -f 'incident-tracker' || true
            '''
        }
    }
}
