package com.example.robmillaci.realestatemanager.activities.sign_in_activities;

import android.support.annotation.NonNull;
import android.widget.Toast;

import com.example.robmillaci.realestatemanager.databases.firebase.FirebaseHelper;
import com.example.robmillaci.realestatemanager.utils.ToastModifications;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.firestore.auth.User;

import java.util.concurrent.Executor;

public class FaceBookLoginManager {
    private loginManagerCallback mLoginManagerCallback;
    private CallbackManager mCallbackManager;

    FaceBookLoginManager(loginManagerCallback callback) {
        this.mLoginManagerCallback = callback;
    }

    public void createLoginManager() {
        mCallbackManager = CallbackManager.Factory.create(); //creates an instance of callbackManager
        //register the callback and define the actions for onSuccess, onCancel and onError

        com.facebook.login.LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        mLoginManagerCallback.facebookSignInCancelled();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        mLoginManagerCallback.facebookSignInError(exception);
                    }
                });
    }

    public CallbackManager getCallbackManager() {
        return mCallbackManager;
    }




    //Handle the facebook access token and try to sign in with the Auth credentials
    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        FirebaseHelper.getmAuth().signInWithCredential(credential)
               .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                   @Override
                   public void onComplete(@NonNull Task<AuthResult> task) {
                       if (task.isSuccessful()) {
                           // Sign in success
                           FirebaseUser thisUser = FirebaseHelper.getmAuth().getCurrentUser();
                           if (thisUser != null) {
                               FirebaseUserMetadata metadata = thisUser.getMetadata();
                               assert metadata != null;
                               if (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp()) {
                                   // The user is new
                                   mLoginManagerCallback.facebookSignInSuccessNewUser(thisUser);

                               } else {
                                   //The user is not a new user
                                   mLoginManagerCallback.facebookSignInSuccessReturningUser(thisUser);
                               }

                           } else {
                               // If sign in fails, display a message to the user.
                               mLoginManagerCallback.facebookAuthFailure();

                           }
                       }else {
                           mLoginManagerCallback.facebookSignInError(null);
                       }
                   }
               });
    }





    interface loginManagerCallback {
        void facebookSignInSuccessNewUser(FirebaseUser user);

        void facebookSignInSuccessReturningUser(FirebaseUser user);

        void facebookAuthFailure();

        void facebookSignInCancelled();

        void facebookSignInError(Exception e);
    }
}