package sat.imme_login_v2;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    ProgressBar progressBar;
    String Tag = "LoginActivity";
    static String EMAIL = "email";
    static String PASSWORD = "password";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    EditText emailEditText;
    EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = (EditText) findViewById(R.id.email_input);
        passwordEditText = (EditText) findViewById(R.id.password_input);
        progressBar = (ProgressBar) findViewById(R.id.loginprogress);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(Tag, "onAuthStateChanged:signed_in" + user.getUid());
                } else {
                    Log.d(Tag, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    public void loginSignIn(View view) {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        progressBar.setVisibility(View.VISIBLE);

        //reset errors
        emailEditText.setError(null);
        passwordEditText.setError(null);


        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(Tag, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            try {
                                throw task.getException();
                            }
                            catch (FirebaseAuthInvalidUserException e) {
                                emailEditText.setError("Email Does Not Exist");
                            }
                            catch (FirebaseAuthInvalidCredentialsException e) {
                                passwordEditText.setError("Wrong Password");
                            }
                            catch (Exception e) {
                                Log.w(Tag, "signInWithEmail:Failed. Unknown Exception");
                            }
                            Log.w(Tag, "signInWithEmail:failed", task.getException());
                            Toast.makeText(Login.this, "Authentication Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            progressBar.setVisibility(View.GONE);
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            startActivity(intent);
                        }


                        // ...
                    }
                });
    }



    public void loginsignup(View view) {
        Intent i = new Intent(this, signup.class);
        i.putExtra(EMAIL, emailEditText.getText().toString());
        i.putExtra(PASSWORD, passwordEditText.getText().toString());
        startActivity(i);
    }

    public void loginforgetpassword(View view) {
        Intent i = new Intent(this, PasswordReset.class);
        i.putExtra(EMAIL, emailEditText.getText().toString());
        startActivity(i);
    }
}
