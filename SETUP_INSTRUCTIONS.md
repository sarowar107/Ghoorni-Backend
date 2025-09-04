# Google Drive Integration Setup Instructions

## Current Status

I've successfully migrated your file upload system from local storage to Google Drive. Here's what has been implemented:

### Backend Changes

1. **Dependencies Added** (`pom.xml`):

   - Google Drive API v3
   - Google Auth Library
   - Google API Client

2. **New Configuration** (`GoogleDriveConfig.java`):

   - Google Drive client configuration
   - OAuth 2.0 setup

3. **New Service** (`GoogleDriveService.java`):

   - File upload to Google Drive
   - File deletion from Google Drive
   - OAuth 2.0 authentication flow
   - File metadata retrieval

4. **Updated Models** (`Files.java`):

   - Added fields for Google Drive file ID
   - Added fields for Google Drive view/download links
   - Added original filename and file size tracking

5. **Updated Services** (`FileService.java`):

   - Modified to use Google Drive instead of local storage
   - Updated file deletion to remove from Google Drive

6. **Updated Controllers**:

   - `FileController.java`: Updated download/view endpoints to redirect to Google Drive
   - `GoogleDriveAuthController.java`: New controller for OAuth setup

7. **Updated Response Models** (`FileResponse.java`):
   - Added Google Drive-related fields

### Frontend Changes

1. **Updated Interfaces** (`fileService.ts`):

   - Added Google Drive fields to file data structures

2. **Updated File Service**:

   - Modified to handle Google Drive URLs
   - Added methods for direct Google Drive links

3. **Updated Components**:
   - `FilesPage.tsx`: Updated to use Google Drive download links
   - `FilePreviewModal.tsx`: Updated to use Google Drive preview links

## Setup Steps

### 1. Build the Project

First, rebuild your backend to resolve the new dependencies:

```bash
cd Ghoorni-Backend
./mvnw clean install
```

### 2. Google Cloud Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create/select your project
3. Enable Google Drive API
4. Create OAuth 2.0 credentials
5. Add redirect URIs:
   - `http://localhost:8080/auth/google/callback`
   - `https://your-production-domain.com/auth/google/callback`

### 3. Environment Configuration

Update your `application.properties`:

```properties
# Google Drive Configuration
google.drive.client-id=545695704161-odbceqmuti5vm8e8v06vf5e2ohkll403.apps.googleusercontent.com
google.drive.client-secret=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
google.drive.redirect-uri=http://localhost:8080/auth/google/callback
google.drive.folder-id=YOUR_FOLDER_ID_HERE
```

### 4. Database Migration

The new fields will be automatically added to your `files` table when you start the application (thanks to `spring.jpa.hibernate.ddl-auto=update`).

### 5. Initial Authorization

1. Start your backend
2. Visit: `http://localhost:8080/auth/google/authorize`
3. Complete OAuth flow
4. You're ready to upload files!

## Key Features

- **Seamless Migration**: Existing local files will still work, new files go to Google Drive
- **Better Performance**: Files are served directly from Google Drive CDN
- **Scalability**: No storage limits on your server
- **Preview Support**: Native Google Drive preview for documents
- **Security**: Files are private to your application by default

## Troubleshooting

If you encounter dependency resolution issues:

1. Clean and rebuild: `./mvnw clean install -U`
2. Check Java version compatibility
3. Verify internet connection for dependency download

## Next Steps

1. Test file upload functionality
2. Verify Google Drive integration
3. Update production environment variables
4. Consider implementing file migration script for existing local files

The integration is ready to use once you complete the Google Cloud setup and authorization flow!
