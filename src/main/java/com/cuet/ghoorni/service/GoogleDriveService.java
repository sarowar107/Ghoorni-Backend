package com.cuet.ghoorni.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.cuet.ghoorni.config.GoogleDriveConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;

@Service
public class GoogleDriveService {

    @Autowired
    private GoogleDriveConfig googleDriveConfig;

    @Autowired
    private HttpTransport httpTransport;

    @Autowired
    private JsonFactory jsonFactory;

    @Autowired
    private GoogleClientSecrets googleClientSecrets;

    private GoogleAuthorizationCodeFlow flow;
    private Drive driveService;

    private void initializeDriveService() throws IOException {
        if (driveService == null) {
            flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport,
                    jsonFactory,
                    googleClientSecrets,
                    Collections.singletonList(DriveScopes.DRIVE_FILE))
                    .setDataStoreFactory(new MemoryDataStoreFactory())
                    .setAccessType("offline")
                    .build();

            // For service account authentication, we'll use a stored credential
            // In production, you should implement proper OAuth2 flow
            Credential credential = getStoredCredential();

            driveService = new Drive.Builder(httpTransport, jsonFactory, credential)
                    .setApplicationName(googleDriveConfig.getApplicationName())
                    .build();
        }
    }

    private Credential getStoredCredential() throws IOException {
        // This is a simplified implementation
        // In production, you should implement proper credential storage and refresh
        Credential credential = flow.loadCredential("user");
        if (credential == null) {
            throw new IOException(
                    "Google Drive authentication required. Please complete OAuth authorization first by visiting /auth/google/authorize");
        }
        return credential;
    }

    public String uploadFile(MultipartFile multipartFile, String fileName) throws IOException {
        initializeDriveService();

        File fileMetadata = new File();
        fileMetadata.setName(fileName);

        // Set parent folder if configured
        if (googleDriveConfig.getFolderId() != null && !googleDriveConfig.getFolderId().isEmpty()) {
            fileMetadata.setParents(Collections.singletonList(googleDriveConfig.getFolderId()));
        }

        InputStreamContent mediaContent = new InputStreamContent(
                multipartFile.getContentType(),
                multipartFile.getInputStream());
        mediaContent.setLength(multipartFile.getSize());

        File file = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id,name,webViewLink,webContentLink")
                .execute();

        // Make file publicly readable
        makeFilePublic(file.getId());

        return file.getId();
    }

    private void makeFilePublic(String fileId) throws IOException {
        Permission permission = new Permission();
        permission.setType("anyone");
        permission.setRole("reader");

        driveService.permissions().create(fileId, permission).execute();
    }

    public void deleteFile(String fileId) throws IOException {
        initializeDriveService();
        driveService.files().delete(fileId).execute();
    }

    public String getFileViewLink(String fileId) throws IOException {
        initializeDriveService();
        File file = driveService.files().get(fileId)
                .setFields("webViewLink")
                .execute();
        return file.getWebViewLink();
    }

    public String getFileDownloadLink(String fileId) throws IOException {
        initializeDriveService();
        File file = driveService.files().get(fileId)
                .setFields("webContentLink")
                .execute();
        return file.getWebContentLink();
    }

    public File getFileMetadata(String fileId) throws IOException {
        initializeDriveService();
        return driveService.files().get(fileId)
                .setFields("id,name,size,mimeType,createdTime,modifiedTime,webViewLink,webContentLink")
                .execute();
    }

    public String getAuthorizationUrl() throws IOException {
        if (flow == null) {
            flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport,
                    jsonFactory,
                    googleClientSecrets,
                    Collections.singletonList(DriveScopes.DRIVE_FILE))
                    .setDataStoreFactory(new MemoryDataStoreFactory())
                    .setAccessType("offline")
                    .build();
        }

        return flow.newAuthorizationUrl()
                .setRedirectUri(googleDriveConfig.getRedirectUri())
                .build();
    }

    public void handleAuthorizationCallback(String code) throws IOException {
        if (flow == null) {
            initializeDriveService();
        }

        GoogleTokenResponse response = flow.newTokenRequest(code)
                .setRedirectUri(googleDriveConfig.getRedirectUri())
                .execute();

        Credential credential = flow.createAndStoreCredential(response, "user");

        // Update the drive service with the new credential
        driveService = new Drive.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(googleDriveConfig.getApplicationName())
                .build();
    }

    public boolean isAuthenticated() {
        try {
            if (flow == null) {
                flow = new GoogleAuthorizationCodeFlow.Builder(
                        httpTransport,
                        jsonFactory,
                        googleClientSecrets,
                        Collections.singletonList(DriveScopes.DRIVE_FILE))
                        .setDataStoreFactory(new MemoryDataStoreFactory())
                        .setAccessType("offline")
                        .build();
            }

            Credential credential = flow.loadCredential("user");
            return credential != null && !credential.getAccessToken().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
