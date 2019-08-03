package com.app.livit.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.media.ExifInterface;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.services.cognitoidentity.model.NotAuthorizedException;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Rémi OLLIVIER on 20/06/2018.
 * This class contains the utils to ease the use of AWS
 */

public class AWSUtils {
    private static final int PICTUREWIDTH = 450;
    private static final int PICTUREHEIGHT = 450;

    private static AmazonS3Client sS3Client;
    private static CognitoCachingCredentialsProvider sCredProvider;
    private static CognitoUserPool userPool;

    /**
     * This static method inits AWSUtils
     * It creates a CognitoUserPool if it is not already existing
     */
    public static void init() {
        if (userPool == null) {
            userPool = new CognitoUserPool(Utils.getContext(), Constants.AWSCOGNITOUSERPOOLID, Constants.AWSCOGNITOAPPCLIENTID, Constants.AWSCOGNITOAPPCLIENTSECRET, Constants.AWSREGION);
        }
    }

    /**
     * Static method to login in background
     * @param email the user's email used to login
     * @param handler the result handler for login
     */
    public static void login(String email, AuthenticationHandler handler) {
        userPool.getUser(email).getSessionInBackground(handler);
    }

    /**
     * This static method is used to relogin after a token expiration
     * @param handler the result handler for relogin
     */
    private static void relogin(AuthenticationHandler handler) {
        userPool.getCurrentUser().getSession(handler);
    }

    /**
     * This static method is used to logout the current user in background, on success it clears all user's data
     * @param handler the result handler for logout
     */
    public static void logout(final GenericHandler handler) {
        userPool.getCurrentUser().globalSignOutInBackground(new GenericHandler() {
            @Override
            public void onSuccess() {
                sCredProvider.clear();
                sCredProvider.clearCredentials();
                sCredProvider = null;
                handler.onSuccess();
            }

            @Override
            public void onFailure(Exception exception) {
                handler.onFailure(exception);
            }
        });
    }

    public static CognitoCachingCredentialsProvider getCredProvider(Context context) {
        if (sCredProvider == null) {
            sCredProvider = new CognitoCachingCredentialsProvider(
                    context.getApplicationContext(),
                    Constants.AWSIDENTITYPOOLID,
                    Constants.AWSREGION);
        }
        return sCredProvider;
    }

    /**
     * This static method is used to get a credentials provider that is up to date
     * @param context the context
     * @param needRefresh a boolean to know if a refresh is needed or not
     */
    public static void getUpToDateCredProvider(Context context, boolean needRefresh, final GetCredProviderHandler handler) {
        if (sCredProvider == null || needRefresh) {
            sCredProvider = new CognitoCachingCredentialsProvider(
                    context.getApplicationContext(),
                    Constants.AWSIDENTITYPOOLID,
                    Constants.AWSREGION);
        }
        if (sCredProvider.getSessionCredentitalsExpiration() == null || sCredProvider.getSessionCredentitalsExpiration().before(Calendar.getInstance(Locale.getDefault()).getTime())) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sCredProvider.refresh();
                        handler.onSuccess(sCredProvider);
                    } catch (NotAuthorizedException e) {
                        e.printStackTrace();
                        relogin(new AuthenticationHandler() {
                            @Override
                            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                                Log.e("onSuccess Relogin", "OK");
                                Map<String, String> logins = new HashMap<>();
                                logins.put(Constants.AWSTOKENVERIFICATIONURL, userSession.getIdToken().getJWTToken());
                                sCredProvider.setLogins(logins);
                                handler.onSuccess(sCredProvider);
                            }

                            @Override
                            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                                try {
                                    AuthenticationDetails authenticationDetails = new AuthenticationDetails(userId, AESCrypt.decrypt(PreferencesHelper.getInstance().getPassword()), null);
                                    //Log.e("Decrypted password", AESCrypt.decrypt(PreferencesHelper.getInstance().getPassword()));
                                    authenticationContinuation.setAuthenticationDetails(authenticationDetails);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    // Allow the sign-in to continue
                                    authenticationContinuation.continueTask();
                                }
                            }

                            @Override
                            public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
                                continuation.continueTask();
                            }

                            @Override
                            public void authenticationChallenge(ChallengeContinuation continuation) {
                                continuation.continueTask();
                            }

                            @Override
                            public void onFailure(Exception exception) {
                                handler.onFailure(exception);
                            }
                        });
                    }
                }
            }).start();
        } else
            handler.onSuccess(sCredProvider);
    }

    /**
     * This static method inits a new session
     * @param context the context
     * @param handler the result's handler
     */
    public static void initSession(Context context, final GenericHandler handler) {
        if (sCredProvider == null) {
            sCredProvider = new CognitoCachingCredentialsProvider(
                    context.getApplicationContext(),
                    Constants.AWSIDENTITYPOOLID,
                    Constants.AWSREGION);
        }
        if (sCredProvider.getSessionCredentitalsExpiration() == null || sCredProvider.getSessionCredentitalsExpiration().before(Calendar.getInstance(Locale.getDefault()).getTime())) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sCredProvider.refresh();
                    } catch (final NotAuthorizedException e) {
                        relogin(new AuthenticationHandler() {
                            @Override
                            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                                Log.e("onSuccess initSession", "OK");
                                Map<String, String> logins = new HashMap<>();
                                logins.put(Constants.AWSTOKENVERIFICATIONURL, userSession.getIdToken().getJWTToken());
                                sCredProvider.setLogins(logins);
                                handler.onSuccess();
                            }

                            @Override
                            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                                try {
                                    AuthenticationDetails authenticationDetails = new AuthenticationDetails(userId, AESCrypt.decrypt(PreferencesHelper.getInstance().getPassword()), null);
                                    authenticationContinuation.setAuthenticationDetails(authenticationDetails);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    // Allow the sign-in to continue
                                    authenticationContinuation.continueTask();
                                }
                            }

                            @Override
                            public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
                                continuation.continueTask();
                            }

                            @Override
                            public void authenticationChallenge(ChallengeContinuation continuation) {
                                continuation.continueTask();
                            }

                            @Override
                            public void onFailure(Exception exception) {
                                //todo handle relogin needed
                                handler.onFailure(exception);
                            }
                        });
                    }
                }
            }).start();
        }
    }

    /**
     * This static method returns the CognitoCachingCredentialsProvider whithout refresh
     * @param context the context
     */
    public static void getUpToDateCredProvider(Context context, GetCredProviderHandler handler) {
        getUpToDateCredProvider(context, false, handler);
    }

    /**
     * This static method inits if needed and returns the S3 client ready to upload pictures
     * @param context the context
     * @return the AmazonS3Client ready to use
     */
    private static AmazonS3Client getS3Client(Context context) {
        if (sS3Client == null) {
            getUpToDateCredProvider(context, new GetCredProviderHandler() {
                @Override
                public void onSuccess(CognitoCachingCredentialsProvider provider) {
                    sS3Client = new AmazonS3Client(provider);
                    sS3Client.setRegion(Region.getRegion(Constants.AWSREGION));
                }

                @Override
                public void onFailure(Exception exception) {
                    exception.printStackTrace();
                }
            });
        }
        return sS3Client;
    }

    /**
     * This static method returns the TransferUrility nedeed to upload a picture on a S3 bucket
     * @param context the context
     * @return the TransferUtility
     */
    private static TransferUtility getTransferUtility(Context context) {
        return TransferUtility.builder().s3Client(getS3Client(context)).context(context).build();
    }

    /**
     * This static method uploads a file on a S3 bucket
     * @param bucket the name of the S3 bucket
     * @param path the path of the image to upload
     * @param fileName the filename to use to store the image in the S3 bucket
     * @param context the context
     * @param transferListener the listener that allow the app to display the upload progress to the user
     */
    public static void uploadFile(String bucket, String path, String fileName, Context context, TransferListener transferListener) {
        TransferUtility transferUtility = getTransferUtility(context);
        List<TransferObserver> observers = transferUtility.getTransfersWithType(TransferType.UPLOAD);
        File file = new File(path);
        Log.d("FILE LENGTH", String.valueOf(file.length()));
        //TODO compress file
        File compressedFile = null;
        try {
            compressedFile = getCompressed(context, path);
            Log.d("COMPRESSED FILE LENGTH", String.valueOf(compressedFile.length()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (compressedFile != null)
            file = compressedFile;

        TransferObserver observer = transferUtility.upload(bucket, fileName, file);
        observers.add(observer);
        observer.setTransferListener(transferListener);
    }

    /**
        Compress the file/photo from @param <b>path</b> to a private location on the current device and return the compressed file.
        @param path = The original image path
        @param context = Current android Context
     */
    private static File getCompressed(Context context, String path) throws IOException {

        if (context == null)
            throw new NullPointerException("Context must not be null.");
        //getting device external cache directory, might not be available on some devices,
        // so our code fall back to internal storage cache directory, which is always available but in smaller quantity
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null)
            //fall back
            cacheDir = context.getCacheDir();

        String rootDir = cacheDir.getAbsolutePath() + "/Liv'vit";
        File root = new File(rootDir);

        //Create ImageCompressor folder if it doesn't already exist
        if (!root.exists())
            root.mkdirs();

        //decode and resize the original bitmap from @param path
        Bitmap bitmap = decodeImageFromFiles(path, PICTUREWIDTH, PICTUREHEIGHT);

        //SDF to generate a unique name for our compress file.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddhhmmss", Locale.getDefault());

        //create placeholder for the compressed image file
        File compressed = new File(root, sdf.format(new Date()) + ".jpg" /*Your desired format*/);

        //convert the decoded bitmap to stream
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        //      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(path);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
            }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*compress bitmap into byteArrayOutputStream
            Bitmap.compress(Format, Quality, OutputStream)
            Where Quality ranges from 1 - 100.
         */
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);

        /*
        Right now, we have our bitmap inside byteArrayOutputStream Object, all we need next is to write it to the compressed file we created earlier,
        java.io.FileOutputStream can help us do just That!
         */
        try (FileOutputStream fileOutputStream = new FileOutputStream(compressed)) {
            fileOutputStream.write(byteArrayOutputStream.toByteArray());
            fileOutputStream.flush();
        }

        //File written, return to the caller. Done!
        return compressed;
    }

    private static Bitmap decodeImageFromFiles(String path, int width, int height) {
        BitmapFactory.Options scaleOptions = new BitmapFactory.Options();
        scaleOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, scaleOptions);
        int scale = 1;
        while (scaleOptions.outWidth / scale / 2 >= width
                && scaleOptions.outHeight / scale / 2 >= height) {
            scale *= 2;
        }
        // decode with the sample size
        BitmapFactory.Options outOptions = new BitmapFactory.Options();
        outOptions.inSampleSize = scale;
        return BitmapFactory.decodeFile(path, outOptions);
    }

    private AWSUtils() {

    }

    public interface GetCredProviderHandler {
        void onSuccess(CognitoCachingCredentialsProvider provider);

        void onFailure(Exception exception);
    }
}
