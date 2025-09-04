# Google Drive Integration Setup Guide

This guide will help you set up Google Drive integration for your Ghoorni application.

## Prerequisites

1. Google Cloud Console account
2. Google Drive API enabled
3. OAuth 2.0 credentials configured

## Setup Steps

### 1. Google Cloud Console Configuration

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the Google Drive API:
   - Navigate to "APIs & Services" > "Library"
   - Search for "Google Drive API"
   - Click "Enable"

### 2. OAuth 2.0 Credentials

1. Go to "APIs & Services" > "Credentials"
2. Click "Create Credentials" > "OAuth 2.0 Client ID"
3. Configure the OAuth consent screen if not already done
4. Select "Web application" as the application type
5. Add the following redirect URIs:
   - `http://localhost:8080/auth/google/callback` (for development)
   - `https://your-backend-domain.com/auth/google/callback` (for production)
6. Add the following JavaScript origins:
   - `http://localhost:8080` (for development)
   - `https://your-backend-domain.com` (for production)

### 3. Environment Configuration

Update your `application.properties` file with the following configurations:

```properties
# Google Drive Configuration
google.drive.client-id=YOUR_CLIENT_ID_HERE
google.drive.client-secret=YOUR_CLIENT_SECRET_HERE
google.drive.redirect-uri=http://localhost:8080/auth/google/callback
google.drive.folder-id=YOUR_GOOGLE_DRIVE_FOLDER_ID_HERE
```

Or set environment variables:

```bash
export GOOGLE_DRIVE_CLIENT_ID=your_client_id_here
export GOOGLE_DRIVE_CLIENT_SECRET=your_client_secret_here
export GOOGLE_DRIVE_REDIRECT_URI=http://localhost:8080/auth/google/callback
export GOOGLE_DRIVE_FOLDER_ID=your_folder_id_here
```

### 4. Google Drive Folder Setup (Optional)

1. Create a folder in Google Drive where you want to store uploaded files
2. Get the folder ID from the URL when viewing the folder in Google Drive
3. The folder ID is the part after `/folders/` in the URL
4. Add this ID to your configuration

### 5. Initial Authorization

1. Start your backend application
2. Navigate to: `http://localhost:8080/auth/google/authorize`
3. Complete the OAuth flow by signing in with your Google account
4. Grant the necessary permissions for Google Drive access

### 6. Testing the Integration

1. Try uploading a file through your application
2. Check your Google Drive to see if the file appears
3. Test downloading and viewing files

## Important Notes

- **Security**: Keep your client secret secure and never expose it in client-side code
- **Scopes**: The application requests `https://www.googleapis.com/auth/drive.file` scope, which allows access only to files created by the application
- **Refresh Tokens**: The application stores credentials in memory. For production, implement proper credential storage and refresh token handling
- **File Permissions**: Uploaded files are made publicly readable by default. Adjust the `makeFilePublic` method in `GoogleDriveService` if you need different permissions

## Troubleshooting

### Common Issues

1. **"redirect_uri_mismatch" error**: Ensure the redirect URI in your OAuth credentials matches exactly with the one in your configuration
2. **"invalid_client" error**: Check that your client ID and secret are correct
3. **Files not appearing**: Verify that the folder ID is correct and the service account has access to it
4. **Download links not working**: Ensure files have proper sharing permissions

### Logs

Check the application logs for detailed error messages. Enable debug logging for Google API calls:

```properties
logging.level.com.google.api=DEBUG
```

## Production Considerations

1. **Credential Storage**: Implement proper credential storage using a database or secure key management service
2. **Error Handling**: Add comprehensive error handling for API failures and rate limits
3. **Monitoring**: Set up monitoring for Google Drive API usage and quotas
4. **Backup**: Consider implementing a backup strategy for important files
5. **Permissions**: Review and adjust file sharing permissions based on your security requirements

## Support

If you encounter issues, check:

1. Google Cloud Console for API quotas and errors
2. Application logs for detailed error messages
3. Google Drive API documentation for the latest updates
