# 🔧 Google OAuth "Access Blocked" Fix

## ❌ **Current Issue**

**Error**: "Access blocked: Ghoorni has not completed the Google verification process"
**Cause**: Your Google Cloud project is in testing mode and the user email is not added as a test user.

## ✅ **Quick Solution: Add Test Users**

### Step 1: Access Google Cloud Console

1. Go to: https://console.cloud.google.com/
2. Select your project (should show "ghoorni" or similar)

### Step 2: Navigate to OAuth Consent Screen

1. Click on **"APIs & Services"** in the left menu
2. Click on **"OAuth consent screen"**

### Step 3: Add Test Users

1. Scroll down to find the **"Test users"** section
2. Click **"+ ADD USERS"** button
3. Add these emails:
   - `ghoorni.cuet@gmail.com` (your current email)
   - Add any other emails you want to test with
4. Click **"Save"**

### Step 4: Test Again

1. Go back to: `http://localhost:8080/auth/google/authorize`
2. Sign in with the email you just added as a test user
3. It should now work! ✅

## 📋 **Alternative: Publish Your App**

If you want anyone to be able to use your app:

1. In the **OAuth consent screen**
2. Click **"PUBLISH APP"**
3. **Note**: This may require Google's review process which can take days/weeks

## 🎯 **Expected Result After Fix**

After adding test users, you should see:

1. ✅ Google sign-in page (instead of blocked page)
2. ✅ Permission request for Google Drive access
3. ✅ Redirect back to your app with success message
4. ✅ File uploads will work!

## 🆘 **Troubleshooting**

### If still blocked:

- Make sure you're signing in with the exact email you added as test user
- Check that your project is selected in Google Cloud Console
- Wait a few minutes for changes to propagate

### If permissions not requested:

- Make sure Google Drive API is enabled in your project
- Check that the OAuth scopes include `https://www.googleapis.com/auth/drive.file`

## 📞 **Need Help?**

This is a standard Google OAuth setup issue. The test user approach is the quickest solution for development and testing.

**Once you add the test user, the Google Drive integration will work perfectly!** 🚀
