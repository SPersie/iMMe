package sat.imme_login_v2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by tianlerk on 6/3/18.
 */

public class User {
    static String TAG = "User";
    private FirebaseDatabaseUserInfo firebaseDatabaseUserInfo;
    private String uid;
    private String email;
    private StorageReference mStorageRef;
    private Bitmap profilepic = null;

    private User(FirebaseUser fireuser, FirebaseDatabaseUserInfo firebaseDatabaseUserInfo) {
        email = fireuser.getEmail();
        uid = fireuser.getUid();
        this.firebaseDatabaseUserInfo = firebaseDatabaseUserInfo;
        mStorageRef = FirebaseStorage.getInstance().getReference().child("users").child(uid);
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return firebaseDatabaseUserInfo.getUsername();
    }

    public String getAddress() {
        return firebaseDatabaseUserInfo.getAddress();
    }

    public String getNRIC() {
        return firebaseDatabaseUserInfo.getNRIC();
    }



    public Uri getImageuri() {
        return firebaseDatabaseUserInfo.getImageurl();
    }

    public static void updateUser(FirebaseUser fireuser, String username, String address,
                                  String NRIC, onFirebaseUserCallback callback) {
        createFirebaseDatabaseUserInfo(fireuser, username, address,
                NRIC, callback);
    }

    public static void getUser(final FirebaseUser fireuser, final onFirebaseUserCallback callback) {
        String uid = fireuser.getUid();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        mDatabase.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseDatabaseUserInfo firebaseDatabaseUserInfo = dataSnapshot
                        .getValue(FirebaseDatabaseUserInfo.class);
                Log.d(TAG, "getUser Successful. Callingback");
                User user = new User(fireuser, firebaseDatabaseUserInfo);
                callback.onSuccess(user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
                callback.onFailure(null);
            }
        });
    }

    public static void createUser(final String email, final String password, final String username,
                                  final String address, final String NRIC, final onFirebaseUserCallback callback) {
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        //create user            //upload image to cloud storage
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "creatUserWithEmail:Success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            createFirebaseDatabaseUserInfo(user, username, address, NRIC, callback);
                        } else {
                            Log.d(TAG, "creatUserWithEmail:Failed");
                            callback.onFailure(task);
                        }
                    }
                });

        //upload database extra info


    }

    private static void createFirebaseDatabaseUserInfo(FirebaseUser fireuser, String username, String address,
                                                       String NRIC, onFirebaseUserCallback callback) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseDatabaseUserInfo firebaseDatabaseUserInfo =
                new FirebaseDatabaseUserInfo(address, NRIC, username);
        mDatabase.child("users").child(fireuser.getUid()).setValue(firebaseDatabaseUserInfo);
        User user = new User(fireuser, firebaseDatabaseUserInfo);
        callback.onSuccess(user);
    }



    public void uploadProfilePic(Bitmap ProfilePic, final onFirebaseStorageCallback callback) {
        StorageReference imageStorage = mStorageRef.child("images/profilepicture");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ProfilePic.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        imageStorage.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests") Uri imageurl = taskSnapshot.getDownloadUrl();
                        firebaseDatabaseUserInfo.setImageurl(imageurl);
                        callback.onSuccess(imageurl);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void getProfilePic(final onFirebaseGetImageCallback callback) {
        final long TEN_MEGABYTES = 1024*1024*10;

        if (profilepic == null) {
            StorageReference imageStorage = mStorageRef.child("images/profilepicture");
            imageStorage.getBytes(TEN_MEGABYTES).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    profilepic = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    callback.onSuccess(profilepic);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onFailure(e);
                }
            });

        }
        else {
            callback.onSuccess(profilepic);
        }
    }
}

class FirebaseDatabaseUserInfo {
    private String address;
    private String nric;
    private Uri imageurl = null;
    private String username;

    public FirebaseDatabaseUserInfo() {
    }

    public FirebaseDatabaseUserInfo(String address, String NRIC, String username) {
        this.address = address;
        this.nric = NRIC;
        this.username = username;
    }

    public void setImageurl(Uri imageurl) {this.imageurl = imageurl;}

    public String getAddress() {
        return address;
    }

    public String getNRIC() {
        return nric;
    }

    public Uri getImageurl() {
        return imageurl;
    }

    public String getUsername() {
        return username;
    }


}
