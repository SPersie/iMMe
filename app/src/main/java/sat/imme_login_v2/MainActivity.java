package sat.imme_login_v2;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    TextView uid;
    Button device;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseUser user;
    private DrawerLayout mDrawerLayout;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        uid = (TextView) findViewById(R.id.mainuid);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
        }
        else {
            uid.setText(user.getUid());
        }
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(MainActivity.this, Login.class);
                    startActivity(intent);
                }
                // ...
            }
        };

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(true);
                mDrawerLayout.closeDrawers();

                //TODO: implement the navigation drawer
                if (item.getItemId() ==R.id.user_user) {
                    Intent i0 =new Intent(MainActivity.this, usertoUserS.class);
                    startActivity(i0);
                }
                else if (item.getItemId() ==R.id.user_user_r) {
                    Intent i1 =new Intent(MainActivity.this, usertoUserR.class);
                    startActivity(i1);
                }
                else if (item.getItemId() ==R.id.add_user_device) {
                    Intent i1 =new Intent(MainActivity.this, AddDeviceActivity.class);
                    startActivity(i1);
                }
                else if (item.getItemId() ==R.id.user_device) {
                    Intent i2 =new Intent(MainActivity.this, usertoDevice.class);
                    startActivity(i2);
                }
                else if (item.getItemId() ==R.id.web_otp) {
                    Intent i3 =new Intent(MainActivity.this, webotp.class);
                    startActivity(i3);
                }
                else if (item.getItemId() ==R.id.verify_account) {
                    Intent i4 =new Intent(MainActivity.this, verifyAccount.class);
                    startActivity(i4);
                }
                else if (item.getItemId() ==R.id.upload_photo) {
                    Intent i5 =new Intent(MainActivity.this, uploadPhoto.class);
                    startActivity(i5);
                }
                else if (item.getItemId() ==R.id.updateinfo) {
                    Intent i6 =new Intent(MainActivity.this, updateInfo.class);
                    startActivity(i6);

                }
                else if (item.getItemId() ==R.id.logout) {
                    Intent i7 =new Intent(MainActivity.this, Login.class);
                    startActivity(i7);
                }

                return true;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
