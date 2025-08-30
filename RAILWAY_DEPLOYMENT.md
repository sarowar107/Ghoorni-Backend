# Railway Deployment Guide for Ghoorni Backend

## Prerequisites

- Railway account (sign up at https://railway.app)
- MySQL database service on Railway
- This Spring Boot application

## Step 1: Create Railway Project

1. Log in to Railway
2. Click "New Project"
3. Choose "Empty Project"
4. Name your project (e.g., "ghoorni-backend")

## Step 2: Add MySQL Database

1. In your Railway project, click "New Service"
2. Choose "Database" → "MySQL"
3. Wait for the database to be provisioned
4. Note down the connection details from the "Connect" tab

## Step 3: Deploy the Application

1. In your Railway project, click "New Service"
2. Choose "GitHub Repo" and connect your repository
3. Railway will automatically detect the Dockerfile and start building

## Step 4: Configure Environment Variables

In your Railway application service, go to "Variables" tab and add:

### Required Environment Variables:

```
DATABASE_URL=mysql://username:password@host:port/database_name
DATABASE_USERNAME=your_mysql_username
DATABASE_PASSWORD=your_mysql_password
PORT=8080
```

### Optional Environment Variables:

```
SPRING_PROFILES_ACTIVE=prod
```

## Step 5: Get Database Connection Details

1. Go to your MySQL service in Railway
2. Click on "Connect" tab
3. Copy the connection details:
   - **Host**: Usually something like `containers-us-west-xxx.railway.app`
   - **Port**: Usually `6543` or similar
   - **Username**: Generated username
   - **Password**: Generated password
   - **Database**: Usually same as your service name

## Step 6: Set Environment Variables

Use the MySQL connection details to set these variables in your application service:

```
DATABASE_URL=jdbc:mysql://[HOST]:[PORT]/[DATABASE_NAME]
DATABASE_USERNAME=[USERNAME]
DATABASE_PASSWORD=[PASSWORD]
```

Example:

```
DATABASE_URL=jdbc:mysql://containers-us-west-123.railway.app:6543/railway
DATABASE_USERNAME=root
DATABASE_PASSWORD=your_generated_password
```

## Step 7: Deploy and Verify

1. After setting environment variables, Railway will automatically redeploy
2. Check the "Deployments" tab for build logs
3. Once deployed, your app will be available at the generated Railway URL
4. Test your API endpoints to ensure everything works

## Common Issues and Solutions

### Build Fails

- Check if all required files are in the repository
- Ensure Dockerfile is in the root directory
- Check build logs for specific errors

### Database Connection Issues

- Verify environment variables are set correctly
- Ensure MySQL service is running
- Check if the database URL format is correct

### Application Doesn't Start

- Check application logs in Railway dashboard
- Verify PORT environment variable is set
- Ensure all required dependencies are in pom.xml

## Health Check Endpoint

Your application should be accessible at:

- `https://your-app-name.railway.app/`
- Test with a simple endpoint like `/api/health` if available

## File Uploads

The application creates an `resources/uploads` directory for file storage. Files uploaded will be stored in the container's filesystem. For production, consider using Railway's volume storage or external file storage services.

## Security Notes

- Change default CORS settings in production
- Use environment-specific profiles
- Regularly update dependencies
- Monitor application logs

## Support

If you encounter issues:

1. Check Railway's documentation: https://docs.railway.app
2. Review application logs in Railway dashboard
3. Check database connectivity
4. Verify environment variables are correctly set
