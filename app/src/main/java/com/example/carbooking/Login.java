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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.carbooking.Model.AppUser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 100; // Request code for Google Sign-In

    ImageView buttonGoogleSignIn;

    TextInputEditText editTextEmail,editTextPassword;
    Button buttonSignIn;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    TextView textView;
    GoogleSignInClient googleSignInClient;
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        setupGoogleSignIn();
        db = FirebaseFirestore.getInstance();
        editTextEmail = findViewById(R.id.etEmail);
        editTextPassword = findViewById(R.id.etPassword);
        buttonSignIn = findViewById(R.id.btn_signIn);
        buttonGoogleSignIn = findViewById(R.id.iv_google);
        buttonGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
        textView = findViewById(R.id.tv_signup_now);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email,password;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());
                if(TextUtils.isEmpty(email)){
                    Toast.makeText(Login.this,"Enter Your Email",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    Toast.makeText(Login.this,"Enter Your Password",Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if(user != null){
                                        String uid = user.getUid();
                                        // Load role & other data from firestore
                                        loadUserData(uid);
                                    }
                                    // Sign in success, update UI with the signed-in user's information
                                    Toast.makeText(Login.this, "Login Successful.",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(Login.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            }

            private void loadUserData(String uid) {
                db.collection("users").document(uid).get()
                        .addOnSuccessListener(document -> {
                            if (document.exists()) {
                                String role = document.getString("user_role");

                                if ("admin".equals(role)) {
                                    // Go to admin dashboard
                                    Intent intent = new Intent(getApplicationContext(),AdminActivity.class);
                                    startActivity(intent);
                                    finish();

                                    Log.d("Login", "Welcome Admin!");
                                } else if ("normal".equals(role)) {
                                    // Go to normal user dashboard
                                    Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                    Log.d("Login", "Welcome Normal User!");
                                } else {
                                    Log.w("Login", "Unknown role: " + role);
                                }
                            } else {
                                Log.w("Login", "User data not found.");
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Login", "Failed to fetch user data", e);
                        });
            }

        });
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
                        Toast.makeText(Login.this, "Sign In Successful", Toast.LENGTH_SHORT).show();
                        goToHomeActivity();
                    } else {
                        Toast.makeText(Login.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToHomeActivity() {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
        finish();
    }
}