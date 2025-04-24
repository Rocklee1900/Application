package com.example.carbooking;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.carbooking.Model.AppUser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 100; // Request code for Google Sign-In

    TextInputEditText etEmail, etPassword,etFirstName,etLastName,etPhone;
    Button buttonSignUp;
    ImageView buttonGoogleSignIn;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    TextView textView;
    GoogleSignInClient googleSignInClient;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            goToHomeActivity();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        setupGoogleSignIn();

        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.et_lastname);
        etPhone = findViewById(R.id.etPhoneNumber);
        buttonSignUp = findViewById(R.id.btn_signup);
        buttonGoogleSignIn = findViewById(R.id.iv_google);
        textView = findViewById(R.id.tv_login_now);

        textView.setOnClickListener(v -> {
            goToLoginActivity();
        });

        buttonSignUp.setOnClickListener(v -> registerWithEmail());

        buttonGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
    }

    // ✅ Setup Google Sign-In
    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Ensure this matches your Firebase project
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    // ✅ Sign in with Google
    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // ✅ Handle Google Sign-In result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Log.w(TAG, "Google sign-in failed", e);
                Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ✅ Authenticate Google account with Firebase
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null){
                            String uid = user.getUid();
                            String email = user.getEmail();
                            String firstName = user.getDisplayName();
                            String lastName = "";
                            String phoneNumber = user.getPhoneNumber();
                            String role = "normal";
                            AppUser newUser = new AppUser(email,uid,role,firstName,lastName,phoneNumber);
                            db.collection("users").document(uid).set(newUser)
                                    .addOnSuccessListener(aVoid ->{
                                        Log.d("Register","User created with normal role");
                                    })
                                    .addOnFailureListener(e ->{
                                        Log.w("Register","Failed to save user profile",e);
                                    });
                        }
                        Toast.makeText(SignUpActivity.this, "Sign In Successful", Toast.LENGTH_SHORT).show();
                        goToHomeActivity();
                    } else {
                        Toast.makeText(SignUpActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ✅ Register with Email and Password
    private void registerWithEmail() {
        String email = String.valueOf(etEmail.getText());
        String password = String.valueOf(etPassword.getText());
        String firstName = String.valueOf(etFirstName.getText());
        String lastName = String.valueOf(etLastName.getText());
        String phoneNumber = String.valueOf(etPhone.getText());
        String role = "normal";

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(SignUpActivity.this, "Enter Your Email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(SignUpActivity.this, "Enter Your Password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if(firebaseUser != null){
                            String uid = firebaseUser.getUid();

                            AppUser newUser = new AppUser(email,uid,role,firstName,lastName,phoneNumber);

                            db.collection("users").document(uid).set(newUser)
                                    .addOnSuccessListener(aVoid ->{
                                        Log.d("Register","User created with normal role");
                                    })
                                    .addOnFailureListener(e ->{
                                        Log.w("Register","Failed to save user profile",e);
                                    });
                        }
                        Toast.makeText(SignUpActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();

                        goToLoginActivity();
                    } else {
                        Log.w("Register","Auth failed",task.getException());
                        Toast.makeText(SignUpActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivity(intent);
        finish();
    }

    // ✅ Redirect to Home Page
    private void goToHomeActivity() {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
