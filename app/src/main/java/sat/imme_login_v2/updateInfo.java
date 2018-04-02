package sat.imme_login_v2;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class updateInfo extends AppCompatActivity {
    String TAG = "Account";
    EditText emailEditText;
    EditText passwordEditText;
    EditText usernameEdittext;
    EditText addressEditText;
    EditText nricEditText;
    FirebaseAuth mAuth;
    ImageView profilepic;
    Bitmap imageBitmap = null;
    ProgressBar progressBar;
    User user;
    FirebaseUser fireuser;
    CoordinatorLayout coordinatorLayout;
    boolean isFABOpen = false;
    FloatingActionButton fab;
    final Context context = this;
    HashMap<String, Boolean> fieldsUpdated;
    String EMAIL = "email"; String ADDRESS = "address"; String USERNAME = "username";
    String RESET = "reset";
    int REQUEST_IMAGE_CAPTURE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.accountFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabOnClick();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        fieldsUpdated = new HashMap<String, Boolean>();
        fieldsUpdated.put(EMAIL, false);
        fieldsUpdated.put(ADDRESS, false);
        fieldsUpdated.put(USERNAME, false);

        emailEditText = (EditText) findViewById(R.id.updateEmail);
        passwordEditText = (EditText) findViewById(R.id.updatePassword);
        usernameEdittext = (EditText) findViewById(R.id.updateUsername);
        addressEditText = (EditText) findViewById(R.id.updateAddress);
        nricEditText = (EditText) findViewById(R.id.updateNRIC);
        profilepic = (ImageView) findViewById(R.id.update_profile_picture);
        progressBar = (ProgressBar) findViewById(R.id.accountProgress);
        fireuser = FirebaseAuth.getInstance().getCurrentUser();
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.accountLayout);
        profilepic.setClickable(false);

        progressBar.setVisibility(View.VISIBLE);

        User.getUser(fireuser, new onFirebaseUserCallback() {
            @Override
            public void onSuccess(User user) {
                emailEditText.setText(user.getEmail());
                usernameEdittext.setText(user.getUsername());
                addressEditText.setText(user.getAddress());
                nricEditText.setText(user.getNRIC());
                Log.d(TAG, "nricEDITTEXT = "+user.getNRIC());
                user.getProfilePic(new onFirebaseGetImageCallback() {
                    @Override
                    public void onSuccess(Bitmap image) {
                        progressBar.setVisibility(View.GONE);
                        profilepic.setImageBitmap(image);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Snackbar.make(coordinatorLayout, "Cannot Retrive Profile Picture",
                                Snackbar.LENGTH_LONG);
                    }
                });

            }

            @Override
            public void onFailure(Task task) {
                progressBar.setVisibility(View.GONE);
                Snackbar.make(coordinatorLayout, "Cannot Retrive User Details",
                        Snackbar.LENGTH_LONG);
            }
        });
    }

    void showUpdateComplete(String field) {
        if (field.equals(RESET)) {
            fieldsUpdated.put(EMAIL, false);
            fieldsUpdated.put(ADDRESS, false);
            fieldsUpdated.put(USERNAME, false);
        }
        if (field.equals(EMAIL)) fieldsUpdated.put(EMAIL, true);
        if (field.equals(ADDRESS)) fieldsUpdated.put(ADDRESS, true);
        if (field.equals(USERNAME)) fieldsUpdated.put(USERNAME, true);
        if (!fieldsUpdated.containsValue(false)) {
            Snackbar.make(findViewById(R.id.accountLayout), "Details Updated",
                    Snackbar.LENGTH_LONG).show();
        }

    }

    void fabOnClick() {
        if (isFABOpen) {
            fab.setElevation(0);
            emailEditText.setEnabled(false);
            usernameEdittext.setEnabled(false);
            addressEditText.setEnabled(false);
            profilepic.setClickable(false);
            String username = usernameEdittext.getText().toString();
            String address = addressEditText.getText().toString();
            String email = emailEditText.getText().toString();

            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(fireuser.getUid());
            fireuser.updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User email address updated.");
                        showUpdateComplete(EMAIL);
                    } else {
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
                }
            });

            mDatabase.child("address").setValue(address);
            showUpdateComplete(ADDRESS);
            mDatabase.child("username").setValue(username);
            showUpdateComplete(USERNAME);
            isFABOpen = false;


        } else {
            showUpdateComplete(RESET);
            emailEditText.setEnabled(true);
            usernameEdittext.setEnabled(true);
            addressEditText.setEnabled(true);
            profilepic.setClickable(true);
            isFABOpen = true;
        }
    }

    public void accountTakeCameraPicture(View view) {
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

            StorageReference imageStorage =  FirebaseStorage.getInstance().getReference("users")
                    .child(fireuser.getUid()).child("images/profilepicture");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data1 = baos.toByteArray();
            imageStorage.putBytes(data1)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Snackbar.make(findViewById(R.id.accountLayout), "Profile Picture Updated",
                                    Snackbar.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Snackbar.make(findViewById(R.id.accountLayout), "Failed To Update Profile Picture",
                            Snackbar.LENGTH_LONG).show();
                }
            });
        }

    }

    public void updatePassword(View view) {
        Log.d(TAG, "Attempting to inflate dialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View newview = inflater.inflate(R.layout.dialog_changepassword, null);
        builder.setView(newview)
                .setPositiveButton("Change Password", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, int i) {
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                progressBar.setVisibility(View.VISIBLE);
                final EditText passwordEditText = newview.findViewById(R.id.updatePassword);
                final EditText verifyEditText = newview.findViewById(R.id.updateVerifyPassword);
                String password = passwordEditText.getText().toString();
                String verify = verifyEditText.getText().toString();
                if (password.equals(verify)) {
                    //change password
                    fireuser.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User password updated");
                                progressBar.setVisibility(View.GONE);
                                dialog.dismiss();
                                Snackbar.make(findViewById(R.id.accountLayout), "Password Updated",
                                        Snackbar.LENGTH_LONG).show();

                            } else {
                                progressBar.setVisibility(View.GONE);
                                try {
                                    throw task.getException();
                                }

                                catch (FirebaseAuthWeakPasswordException e) {
                                    passwordEditText.setError(e.getReason());
                                }
                                catch (Exception e) {
                                    Log.d(TAG, "Exception" + e.getMessage());
                                }
                            }
                        }
                    });

                }
                else {
                    passwordEditText.setError("Passwords Don't Match");
                    verifyEditText.setError("Passwords Don't Match");
                }

            }
        });
    }

}



