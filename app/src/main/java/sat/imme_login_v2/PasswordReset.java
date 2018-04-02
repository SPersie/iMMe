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
import com.google.firebase.auth.FirebaseAuth;

public class PasswordReset extends AppCompatActivity {
    String TAG = "PasswordReset";
    EditText emailEditText;
    FirebaseAuth auth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);
        emailEditText = (EditText) findViewById(R.id.forgetpasswordemail);
        progressBar = (ProgressBar) findViewById(R.id.resetprogress);

        Intent i = getIntent();
        String email = i.getStringExtra("email");
        if (email != null) emailEditText.setText(email);
        auth = FirebaseAuth.getInstance();
    }

    public void sendResetLink(View view){
        emailEditText.setError(null);
        progressBar.setVisibility(View.VISIBLE);
        String email = emailEditText.getText().toString();
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Email sent.");
                    Toast.makeText(PasswordReset.this, "Email Sent!",
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    Intent i = new Intent(PasswordReset.this, Login.class);
                    startActivity(i);
                }
                else {
                    progressBar.setVisibility(View.GONE);
                    emailEditText.setError("Invalid Email");
                    Toast.makeText(PasswordReset.this, "Failed to send email!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
