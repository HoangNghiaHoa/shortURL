# 📋 Deployment Checklist

## ✅ Files Verification

### Core Application (Java)
- [x] `Application.java` - Main entry point
- [x] `URLShortenerService.java` - URL shortening logic
- [x] `URLController.java` - REST API endpoints
- [x] `AnalyticsService.java` - Analytics processing
- [x] `GeoIPService.java` - GeoIP tracking
- [x] `RateLimiter.java` - Rate limiting

### Entities & DTOs
- [x] `URLEntity.java` - URL entity
- [x] `ClickAnalytics.java` - Analytics entity ✅
- [x] `AnalyticsData.java` - Analytics DTO
- [x] `CreateURLRequest.java` - API request DTO

### Repositories
- [x] `URLRepository.java` - URL repository
- [x] `ClickAnalyticsRepository.java` - Analytics repository

### Configuration
- [x] `application.yml` - Spring configuration
- [x] `pom.xml` - Maven dependencies
- [x] `schema.sql` - Database schema

### Docker & Infrastructure
- [x] `Dockerfile` - Application container
- [x] `docker-compose.yml` - Multi-container setup
- [x] `nginx.conf` - Reverse proxy config
- [x] `.dockerignore` - Docker ignore rules
- [x] `.gitignore` - Git ignore rules

### Documentation & Tools
- [x] `README.md` - Setup guide
- [x] `ARCHITECTURE.md` - Architecture docs
- [x] `Makefile` - Build commands
- [x] `DEPLOYMENT_CHECKLIST.md` - This file

## 🚀 Pre-Deployment Steps

### 1. Environment Setup
- [ ] Java 17+ installed
- [ ] Docker & Docker Compose installed
- [ ] Maven 3.6+ installed
- [ ] Minimum 4GB RAM available
- [ ] 20GB disk space available

### 2. Database Setup
- [ ] PostgreSQL credentials configured
- [ ] Redis password set
- [ ] Database schema imported
- [ ] Indexes created

### 3. GeoIP Database
- [ ] Downloaded GeoLite2-City.mmdb
- [ ] Placed in `data/` directory
- [ ] File permissions correct (readable)

### 4. Configuration Review
```bash
# Check application.yml
cat src/main/resources/application.yml

# Verify environment variables
cat .env
```
- [ ] Database URL correct
- [ ] Redis host/port correct
- [ ] Kafka bootstrap servers correct
- [ ] Domain name configured
- [ ] Rate limits appropriate

### 5. Security Checklist
- [ ] Strong database passwords set
- [ ] Redis password enabled
- [ ] SSL certificates obtained (Let's Encrypt)
- [ ] HTTPS redirect enabled
- [ ] Rate limiting configured
- [ ] CloudFlare DNS configured (optional)

### 6. Build & Test
```bash
# Build application
mvn clean package

# Run tests
mvn test

# Build Docker images
docker-compose build
```
- [ ] Build successful
- [ ] All tests passing
- [ ] Docker images built

## 🌐 Production Deployment

### Step 1: Start Infrastructure
```bash
# Start database & cache first
docker-compose up -d postgres redis

# Wait 30 seconds for initialization
sleep 30

# Check health
docker-compose ps
```

### Step 2: Initialize Database
```bash
# Connect to database
make db-shell

# Verify tables
\dt

# Check indexes
\di
```

### Step 3: Start Application
```bash
# Start Kafka
docker-compose up -d zookeeper kafka

# Wait for Kafka
sleep 20

# Start application
docker-compose up -d url-shortener

# View logs
make logs
```

### Step 4: Start Monitoring
```bash
# Start Prometheus & Grafana
docker-compose up -d prometheus grafana

# Access Grafana
# http://localhost:3000 (admin/admin)
```

### Step 5: Configure Nginx
```bash
# Update nginx.conf with your domain
vim nginx.conf

# Start Nginx
docker-compose up -d nginx
```

### Step 6: Health Check
```bash
# Run health check
make health-check

# Expected output:
# {
#   "status": "UP",
#   "components": {
#     "db": {"status": "UP"},
#     "redis": {"status": "UP"}
#   }
# }
```

### Step 7: Test Endpoints
```bash
# Test create URL
curl -X POST http://localhost/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://example.com/test","userId":1}'

# Expected: {"shortCode":"Xs2dF3",...}

# Test redirect
curl -I http://localhost/Xs2dF3

# Expected: 302 redirect

# Test stats
curl http://localhost/api/stats/Xs2dF3

# Expected: {"totalClicks":1,...}
```

## 📊 Post-Deployment Verification

### Performance Testing
```bash
# Load test with Apache Bench
ab -n 1000 -c 10 http://localhost/Xs2dF3

# Monitor metrics
curl http://localhost:8080/actuator/prometheus | grep http_
```

### Monitoring Setup
- [ ] Grafana dashboard imported
- [ ] Alerts configured
- [ ] Log aggregation setup
- [ ] Backup scripts scheduled

### Backup Configuration
```bash
# Schedule daily backup (crontab)
0 2 * * * /path/to/project/make backup-db
```

## 🔧 Troubleshooting

### Common Issues

**1. Database connection failed**
```bash
# Check PostgreSQL status
docker-compose logs postgres

# Test connection
docker exec -it url-shortener-db psql -U postgres -d urlshortener
```

**2. Redis connection timeout**
```bash
# Check Redis
docker-compose logs redis

# Test Redis
docker exec -it url-shortener-redis redis-cli ping
```

**3. Application won't start**
```bash
# Check logs
docker-compose logs url-shortener

# Common fixes:
# - Verify all dependencies are up
# - Check port conflicts (8080)
# - Ensure GeoIP database exists
```

**4. High latency**
```bash
# Check cache hit rate
docker exec -it url-shortener-redis redis-cli info stats

# Monitor database
docker exec -it url-shortener-db psql -U postgres -c "
  SELECT pid, query, state, wait_event_type 
  FROM pg_stat_activity 
  WHERE state != 'idle';
"
```

## 🎯 Success Criteria

Deployment is successful when:
- [x] All containers running (`docker-compose ps`)
- [x] Health check returns UP
- [x] Can create short URLs
- [x] Redirects work correctly
- [x] Analytics being recorded
- [x] Response time < 100ms
- [x] No errors in logs
- [x] Grafana dashboard showing data

## 📞 Support

If you encounter issues:
1. Check logs: `make logs`
2. Review this checklist
3. Check GitHub issues
4. Contact support team

## 🔄 Rollback Plan

If deployment fails:
```bash
# Stop all services
docker-compose down

# Restore database backup
make restore-db FILE=backup_latest.sql

# Revert to previous version
git checkout <previous-commit>
docker-compose up -d
```

---

**Last Updated**: 2026-01-09
**Version**: 1.0.0