package sat.imme_login_v2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class signup extends AppCompatActivity {
    String TAG = "SignUp";
    EditText emailEditText;
    EditText passwordEditText;
    EditText usernameEdittext;
    EditText addressEditText;
    EditText nricEditText;
    FirebaseAuth mAuth;
    TextView profilepicerror;
    ImageView profilepic;
    Bitmap imageBitmap = null;
    ProgressBar progressBar;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance();

        emailEditText = (EditText) findViewById(R.id.signupEmail);
        passwordEditText = (EditText) findViewById(R.id.signupPassword);
        usernameEdittext = (EditText) findViewById(R.id.signupName);
        addressEditText = (EditText) findViewById(R.id.signupAddress);
        nricEditText = (EditText) findViewById(R.id.signupNRIC);
        profilepic = (ImageView) findViewById(R.id.signup_profile_picture);
        profilepicerror = (TextView) findViewById(R.id.signup_profile_picture_error);
        progressBar = (ProgressBar) findViewById(R.id.signup_progress);

        Intent i = getIntent();
        Bundle b = i.getExtras();
        if (b!=null) {
            emailEditText.setText((String) b.get("email"));
            passwordEditText.setText((String) b.get("password"));
        }
    }

    public void signup(View v) {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        final String username = usernameEdittext.getText().toString();
        final String address = addressEditText.getText().toString();
        final String nric = nricEditText.getText().toString();

        //set all error to null;
        emailEditText.setError(null);
        passwordEditText.setError(null);
        usernameEdittext.setError(null);
        addressEditText.setError(null);
        nricEditText.setError(null);
        profilepicerror.setVisibility(View.GONE);


        if (email.isEmpty()) emailEditText.setError("Cannot be empty");
        else if (!emailValidator(email)) emailEditText.setError("Invalid Email");
        else if (password.isEmpty()) passwordEditText.setError("Cannot be empty");
        else if (username.isEmpty()) usernameEdittext.setError("Cannot be empty");
        else if (address.isEmpty()) addressEditText.setError("Cannot be empty");
        else if (nric.isEmpty()) nricEditText.setError("Cannot be empty");
        else if (!nricValidator(nric)) nricEditText.setError("Invalid NRIC");
        else if (imageBitmap == null) profilepicerror.setVisibility(View.VISIBLE);
        else {
            progressBar.setVisibility(View.VISIBLE);
            User.createUser(email, password, username, address, nric, new onFirebaseUserCallback() {
                @Override
                public void onSuccess(User user) {
                    user.uploadProfilePic(imageBitmap, new onFirebaseStorageCallback() {
                        @Override
                        public void onSuccess(Uri uri) {
                            progressBar.setVisibility(View.GONE);
                            Log.d(TAG, "Image Upload Success");
                            Intent i = new Intent(signup.this, MainActivity.class);
                            startActivity(i);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(signup.this, "Image Upload Failed",
                                    Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Image Upload Failed: " + e.getMessage());
                        }
                    });
                }

                @Override
                public void onFailure(Task task) {
                    progressBar.setVisibility(View.GONE);
                    try {
                        throw task.getException();
                    }
                    catch (FirebaseAuthUserCollisionException e) {
                        emailEditText.setError("Email Already Used");
                    }
                    catch (FirebaseAuthWeakPasswordException e) {
                        passwordEditText.setError(e.getReason());
                    }
                    catch (Exception e) {
                        Log.d(TAG, "Exception" + e.getMessage());
                    }
                }

            });
        }
    }

    boolean emailValidator(String email) {
        Pattern pattern;
        Matcher matcher;
        String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    boolean nricValidator(String nric) {
        Pattern pattern;
        Matcher matcher;
        String NRIC_PATTERN =
                "[ST]\\d{7}[A-Z]";
        pattern = Pattern.compile(NRIC_PATTERN);
        matcher = pattern.matcher(nric);
        return matcher.matches();
    }

    public void takeCameraPicture(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            profilepic.setImageBitmap(imageBitmap);
            Log.d(TAG, "Image Capture");
        }

    }

}
