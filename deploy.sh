#!/bin/bash

# Coupon Service Docker Deployment Script
# This script automates the Docker deployment process

set -e  # Exit on any error

echo "🚀 Starting Coupon Service Docker Deployment"
echo "=============================================="

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
echo "📋 Checking prerequisites..."

if ! command_exists docker; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

if ! command_exists docker-compose; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

echo "✅ Docker and Docker Compose are installed"

# Check if JAR file exists
if [ ! -f "target/couponservice-0.0.1-SNAPSHOT.jar" ]; then
    echo "📦 JAR file not found. Building application..."
    if command_exists mvn; then
        mvn clean package -DskipTests
    else
        echo "❌ Maven not found and JAR file doesn't exist. Please build the application first."
        exit 1
    fi
fi

echo "✅ JAR file found"

# Stop existing containers
echo "🛑 Stopping existing containers..."
docker-compose down || true

# Remove old images (optional)
read -p "🗑️  Remove old Docker images? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🗑️  Removing old images..."
    docker image prune -f
    docker rmi couponservice_coupon-service:latest 2>/dev/null || true
fi

# Build and start services
echo "🏗️  Building and starting services..."
docker-compose up --build -d

# Wait for services to be ready
echo "⏳ Waiting for services to start..."
sleep 30

# Health check
echo "🔍 Performing health checks..."

# Check MySQL
echo "Checking MySQL connection..."
for i in {1..30}; do
    if docker-compose exec -T mysql mysqladmin ping -h localhost --silent; then
        echo "✅ MySQL is ready"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ MySQL failed to start"
        docker-compose logs mysql
        exit 1
    fi
    sleep 2
done

# Check Application
echo "Checking application health..."
for i in {1..30}; do
    if curl -f http://localhost:9091/actuator/health >/dev/null 2>&1; then
        echo "✅ Application is ready"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ Application failed to start"
        docker-compose logs coupon-service
        exit 1
    fi
    sleep 2
done

# Test API endpoints
echo "🧪 Testing API endpoints..."

# Test health endpoint
if curl -f http://localhost:9091/actuator/health >/dev/null 2>&1; then
    echo "✅ Health endpoint working"
else
    echo "❌ Health endpoint failed"
fi

# Test coupons endpoint
if curl -f http://localhost:9091/couponapi/coupons >/dev/null 2>&1; then
    echo "✅ Coupons API endpoint working"
else
    echo "❌ Coupons API endpoint failed"
fi

# Display status
echo ""
echo "🎉 Deployment completed successfully!"
echo "=============================================="
echo "📊 Service Status:"
docker-compose ps

echo ""
echo "🌐 Access URLs:"
echo "   Application: http://localhost:9091"
echo "   Health Check: http://localhost:9091/actuator/health"
echo "   Coupons API: http://localhost:9091/couponapi/coupons"
echo "   MySQL: localhost:3306"

echo ""
echo "📋 Useful Commands:"
echo "   View logs: docker-compose logs -f"
echo "   Stop services: docker-compose down"
echo "   Restart: docker-compose restart"
echo "   Shell access: docker-compose exec coupon-service bash"

echo ""
echo "✅ Coupon Service is now running in Docker containers!"
