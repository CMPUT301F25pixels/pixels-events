package com.example.pixel_events.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.util.Log;
import android.util.Patterns;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginViewModel extends ViewModel {
    private static final String TAG = "LoginViewModel";

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private FirebaseAuth auth;

    LoginViewModel() {
        this.auth = FirebaseAuth.getInstance();
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String email, String password) {
        // Sign in with Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "Login successful: " + user.getEmail());

                                // Load user profile using DatabaseHandler.getProfile (uid -> positive int)
                                int id = DatabaseHandler.uidToId(user.getUid());
                                DatabaseHandler.getInstance().getProfile(id,
                                    profile -> {
                                        if (profile != null) {
                                            AuthManager.getInstance().setCurrentUserProfile(profile);
                                            loginResult.setValue(new LoginResult(
                                                    new LoggedInUserView(profile.getUserName(), user.getUid())));
                                        } else {
                                            Log.e(TAG, "Profile not found for id: " + id);
                                            loginResult.setValue(new LoginResult(R.string.login_failed));
                                        }
                                    },
                                    e -> {
                                        Log.e(TAG, "Failed to load profile", e);
                                        loginResult.setValue(new LoginResult(R.string.login_failed));
                                    });
                        }
                    } else {
                        Log.e(TAG, "Login failed", task.getException());
                        loginResult.setValue(new LoginResult(R.string.login_failed));
                    }
                });
    }

    public void loginDataChanged(String email, String password) {
        if (!isEmailValid(email)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    private boolean isEmailValid(String email) {
        if (email == null) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}