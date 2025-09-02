# Railway Environment Variables Setup

## Required Environment Variables

Set these environment variables in your Railway project settings:

### Database Configuration

```
DATABASE_URL=your_postgresql_connection_string
DATABASE_USERNAME=your_db_username
DATABASE_PASSWORD=your_db_password
```

### Email Configuration (Gmail SMTP)

```
MAIL_USERNAME=your_gmail_address
MAIL_PASSWORD=your_gmail_app_password
```

### Optional Configuration

```
PORT=8080
LOG_LEVEL=INFO
FILE_UPLOAD_DIR=/app/uploads
APP_BASE_URL=https://your-frontend-domain.com
```

## Railway Deployment Commands

1. **Login to Railway:**

   ```bash
   railway login
   ```

2. **Link to your Railway project:**

   ```bash
   railway link
   ```

3. **Deploy:**
   ```bash
   railway up
   ```

## Health Check Endpoints

- `/health` - Custom health check endpoint
- `/actuator/health` - Spring Boot Actuator health endpoint

## Troubleshooting

1. **Build fails:** Check that `.mvn` directory is included in your project
2. **App crashes on startup:** Verify all required environment variables are set
3. **Database connection fails:** Check DATABASE_URL format and credentials
4. **Email not working:** Ensure MAIL_PASSWORD is an App Password, not your regular Gmail password

## Performance Settings

The application is configured with optimized JVM settings for Railway's container environment:

- Memory: 512MB max, 256MB initial
- Garbage Collector: G1GC with 200ms pause target
- Container-aware settings enabled
