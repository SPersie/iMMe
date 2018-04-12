package sat.imme_login_v2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static sat.imme_login_v2.signup.REQUEST_IMAGE_CAPTURE;

public class usertoDevice extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;
    ImageView userPhoto;
    TextView otpTextView;
    EditText deviceId;
    Button submit, beam;
    String imageString;
    String idToken;
    FirebaseUser mUser;
    usertodeviceModel usertodeviceModel;
    String nfcotp;
    ProgressBar progressBar;
    RelativeLayout userToDeviceLayout;
    private String otpResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userto_device);

        userPhoto =findViewById(R.id.device_user_photo);
        otpTextView =findViewById(R.id.device_otp);
        deviceId =findViewById(R.id.device_deviceId);
        submit =findViewById(R.id.getDeviceOtp);
        progressBar = findViewById(R.id.userDeviceProgress);
        beam = findViewById(R.id.userDeviceBeam);
        userToDeviceLayout = findViewById(R.id.userDeviceLayout);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    idToken =task.getResult().getToken();
                    System.out.println(idToken +"This is my idtoken");
                } else {
                    System.out.println("Cannot get idToken");
                }
            }
        });

        userPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        //TODO: test connection
        if (isConnected()) {
            Toast.makeText(usertoDevice.this, "connected", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(usertoDevice.this, "not connected", Toast.LENGTH_LONG).show();
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == webotp.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            Log.d("userToDevice", "Added Photo");
            userPhoto.setImageBitmap(photo);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();
            imageString = Base64.encodeToString(byteArray, Base64.NO_WRAP);
//            imageString =java.util.Base64.getEncoder().encodeToString(byteArray);
//            System.out.println(imageString);
        }
    }

    public String POST(String url, usertodeviceModel usertodeviceModel){
        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json = "";

            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("idToken", usertodeviceModel.getIdToken());
            jsonObject.accumulate("deviceId", usertodeviceModel.getDeviceId());
            jsonObject.accumulate("image", usertodeviceModel.gemytImage());

            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();

            // ** Alternative way to convert Person object to JSON string usin Jackson Lib
            // ObjectMapper mapper = new ObjectMapper();
            // json = mapper.writeValueAsString(person);

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string

            if(inputStream == null) {
                return "Failed: Unknown Error";
            }

            result = convertInputStreamToString(inputStream);

            //get the otp value from the response
            JsonElement root = new JsonParser().parse(result);
            String success =root.getAsJsonObject().get("success").getAsString();
            if (success.equals("true")) {
                String otp = root.getAsJsonObject().get("otp").getAsString();
                Log.d("userToDevice", "Got OTP: " + otp);
                return otp;

            } else {
                return "Failed: " + root.getAsJsonObject().get("reason").getAsString();
            }


        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }

    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    public void getDeviceOtp(View view) {
        switch(view.getId()){
            case R.id.getDeviceOtp:
                if(!validate())
                    Toast.makeText(getBaseContext(), "Enter some data!", Toast.LENGTH_LONG).show();
                    // call AsynTask to perform network operation on separate thread
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    new usertoDevice.HttpAsyncTask().execute("https://imme-195707.appspot.com/userDeviceAuth");
                }
                break;
        }
    }

    public void userDeviceNFC(View view) {
        Intent nfc =new Intent(usertoDevice.this, nfcBeam.class);
        nfc.putExtra("otp", nfcotp);
        startActivity(nfc);
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            usertodeviceModel = new usertodeviceModel();
            usertodeviceModel.setIdToken(idToken);
            usertodeviceModel.setDeviceId(deviceId.getText().toString());
            usertodeviceModel.setmyImage(imageString);
            return POST(urls[0],usertodeviceModel);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("userDevice", "onPostExecute " + result);
            if (result.contains("Failed")) {
                Snackbar.make(userToDeviceLayout, result, Snackbar.LENGTH_LONG);
                otpTextView.setText(result);
                beam.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                otpTextView.setTextColor(Color.RED);
            }
            else {
                beam.setVisibility(View.VISIBLE);
                nfcotp = result;
                otpTextView.setText(result);
                progressBar.setVisibility(View.GONE);
                otpTextView.setTextColor(Color.BLACK);
            }

        }
    }





    private boolean validate(){
        if(idToken.equals(""))
            return false;
        else if(deviceId.getText().toString().equals(""))
            return false;
//        else if(imageString)
//            return false;
        else
            return true;
    }
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}
