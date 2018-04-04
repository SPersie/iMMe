package sat.imme_login_v2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class usertoUserS extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;
    ImageView userPhoto;
    TextView otpTextView;
    EditText nric;
    Button submit;
    String imageString;
    String idToken;
    FirebaseUser mUser;
    usertouserModel usertouserModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userto_user_s);

        otpTextView =findViewById(R.id.contact_otp);
        nric =findViewById(R.id.contact_NRIC);
        userPhoto =findViewById(R.id.contact_user_photo);
        submit =findViewById(R.id.contact_submit_button);

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
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        //TODO: test connection
        if (isConnected()) {
            Toast.makeText(usertoUserS.this, "connected", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(usertoUserS.this, "not connected", Toast.LENGTH_LONG).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == webotp.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            userPhoto.setImageBitmap(photo);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();
            imageString = Base64.encodeToString(byteArray, Base64.NO_WRAP);
//            imageString =java.util.Base64.getEncoder().encodeToString(byteArray);
//            System.out.println(imageString);
        }
    }

    public String POST(String url, usertouserModel usertouserModel){
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
            jsonObject.accumulate("idToken", usertouserModel.getIdToken());
            jsonObject.accumulate("nric", usertouserModel.getNric());
            jsonObject.accumulate("image", usertouserModel.getmyImage());

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
            System.out.println("Print our the inputStream");
            System.out.println(inputStream);
            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);

                //get the otp value from the response
                JsonElement root = new JsonParser().parse(result);
                String success =root.getAsJsonObject().get("success").getAsString();
                if (success.equals("true")) {
                    String otp =root.getAsJsonObject().get("otp").getAsString();
                    otpTextView.setText(otp);
                } else {
//                    Toast.makeText(getBaseContext(), "Authentication failed", Toast.LENGTH_LONG).show();
                    String reason =root.getAsJsonObject().get("reason").getAsString();
                    System.out.println("This is the reason why it fails." +reason);
                    otpTextView.setText(reason);
                }
            }
            else
                result = "Did not work!";

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

    public void contact_submitJson(View view) {
        switch(view.getId()){
            case R.id.contact_submit_button:
                if(!validate())
                    Toast.makeText(getBaseContext(), "Enter some data!", Toast.LENGTH_LONG).show();
                    // call AsynTask to perform network operation on separate thread
                else
                    new usertoUserS.HttpAsyncTask().execute("https://imme-195707.appspot.com/userUserAuth");
                break;
        }
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            usertouserModel = new usertouserModel();
            usertouserModel.setIdToken(idToken);
            usertouserModel.setNric(nric.getText().toString());
            usertouserModel.setmyImage(imageString);
            return POST(urls[0],usertouserModel);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
//            Toast.makeText(getBaseContext(), IdToken, Toast.LENGTH_LONG).show();
//            Toast.makeText(getBaseContext(), webid.getText().toString(), Toast.LENGTH_LONG).show();
//            Toast.makeText(getBaseContext(), imageString, Toast.LENGTH_LONG).show();

            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
        }
    }





    private boolean validate(){
        if(idToken.equals(""))
            return false;
        else if(nric.getText().toString().equals(""))
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
