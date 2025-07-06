package com.example.taskmanager.domain.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.taskmanager.R;
import com.example.taskmanager.data.remote.FirebaseService;
import com.example.taskmanager.domain.ui.main.MainActivity;
import com.example.taskmanager.domain.ui.register.SignUpActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class SignInActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textSignup;
    private com.google.android.gms.common.SignInButton buttonGoogleSignIn;

    private FirebaseService firebaseService;
    private GoogleSignInClient googleSignInClient;

    // Launcher for Google Sign-In result
    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        String idToken = account.getIdToken();
                        firebaseService.firebaseAuthWithGoogle(idToken, firebaseTask -> {
                            if (firebaseTask.isSuccessful()) {
                                Toast.makeText(this, "Google sign-in successful", Toast.LENGTH_SHORT).show();
                                navigateToMain();
                            } else {
                                Toast.makeText(this, "Firebase auth failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (ApiException e) {
                        Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialize FirebaseService
        firebaseService = new FirebaseService();

        // Setup Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // from google-services.json
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        initViews();
        setListeners();
    }

    private void initViews() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textSignup = findViewById(R.id.textSignup);
        buttonGoogleSignIn = findViewById(R.id.buttonGoogleSignIn);
    }

    private void setListeners() {
        // Email/password login
        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editTextEmail.setError("Enter a valid email");
                return;
            }

            if (password.length() < 6) {
                editTextPassword.setError("Password must be at least 6 characters");
                return;
            }

            firebaseService.signIn(email, password, task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                } else {
                    Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Google Sign-In
        buttonGoogleSignIn.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        // Redirect to Signup
        textSignup.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class); // create this class if not already
            startActivity(intent);
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}