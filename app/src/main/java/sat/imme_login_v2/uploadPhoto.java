package sat.imme_login_v2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
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

public class uploadPhoto extends AppCompatActivity {
    String idToken;
    ImageView photo1;
    ImageView photo2;
    ImageView photo3;
    ImageView photo4;
    ImageView photo5;
    ImageView[] images;
    Button submitPhoto;
    String photo1_String;
    String photo2_String;
    String photo3_String;
    String photo4_String;
    String photo5_String;
    FirebaseUser mUser;
    ProgressBar progressBar;

    uploadphotoModel uploadphotoModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_photo);

        progressBar = findViewById(R.id.upload_photo_progress_bar);

        images =new ImageView[]{photo1, photo2, photo3};
//        idToken =getIntent().getStringExtra("userID");
        photo1 =findViewById(R.id.photo_1);
        photo2 =findViewById(R.id.photo_2);
        photo3 =findViewById(R.id.photo_3);
        photo4 =findViewById(R.id.photo_4);
        photo5 =findViewById(R.id.photo_5);
        submitPhoto =findViewById(R.id.photo_submit_button);
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

        photo1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                startActivityForResult(cameraIntent, 0);
            }
        });

        photo2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                startActivityForResult(cameraIntent, 1);
            }
        });

        photo3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                startActivityForResult(cameraIntent, 2);
            }
        });

        photo4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                startActivityForResult(cameraIntent, 3);
            }
        });

        photo5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                startActivityForResult(cameraIntent, 4);
            }
        });


        //TODO: test connection
//        if (isConnected()) {
//            Toast.makeText(uploadPhoto.this, "connected", Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(uploadPhoto.this, "not connected", Toast.LENGTH_LONG).show();
//        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode ==0) {
            if (resultCode ==uploadPhoto.RESULT_OK) {
                Bitmap photo =(Bitmap) data.getExtras().get("data");
                photo1.setImageBitmap(photo);

                ByteArrayOutputStream byteArrayOutputStream =new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray =byteArrayOutputStream.toByteArray();
                photo1_String =Base64.encodeToString(byteArray, Base64.NO_WRAP);
            }
        } else if (requestCode ==1) {
            if (resultCode ==uploadPhoto.RESULT_OK) {
                Bitmap photo =(Bitmap) data.getExtras().get("data");
                photo2.setImageBitmap(photo);

                ByteArrayOutputStream byteArrayOutputStream =new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray =byteArrayOutputStream.toByteArray();
                photo2_String =Base64.encodeToString(byteArray, Base64.NO_WRAP);
            }
        } else if (requestCode ==2) {
            if (resultCode ==uploadPhoto.RESULT_OK) {
                Bitmap photo =(Bitmap) data.getExtras().get("data");
                photo3.setImageBitmap(photo);

                ByteArrayOutputStream byteArrayOutputStream =new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray =byteArrayOutputStream.toByteArray();
                photo3_String =Base64.encodeToString(byteArray, Base64.NO_WRAP);
            }
        } else if (requestCode ==3) {
            if (resultCode ==uploadPhoto.RESULT_OK) {
                Bitmap photo =(Bitmap) data.getExtras().get("data");
                photo4.setImageBitmap(photo);

                ByteArrayOutputStream byteArrayOutputStream =new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray =byteArrayOutputStream.toByteArray();
                photo4_String =Base64.encodeToString(byteArray, Base64.NO_WRAP);
            }
        } else if (requestCode ==4) {
            if (resultCode ==uploadPhoto.RESULT_OK) {
                Bitmap photo =(Bitmap) data.getExtras().get("data");
                photo5.setImageBitmap(photo);

                ByteArrayOutputStream byteArrayOutputStream =new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray =byteArrayOutputStream.toByteArray();
                photo5_String =Base64.encodeToString(byteArray, Base64.NO_WRAP);
            }
        }
    }


    public String POST(String url, uploadphotoModel uploadphotoModel){
        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json = "";

            // 3. build jsonObject
            JSONObject imageObject =new JSONObject();
            imageObject.accumulate("image1", uploadphotoModel.getPhoto1());
            imageObject.accumulate("image2", uploadphotoModel.getPhoto2());
            imageObject.accumulate("image3", uploadphotoModel.getPhoto3());
            imageObject.accumulate("image4", uploadphotoModel.getPhoto4());
            imageObject.accumulate("image5", uploadphotoModel.getPhoto5());


            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("idToken", uploadphotoModel.getIdToken());
            jsonObject.accumulate("image", imageObject);

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
                Log.d("uploadPhoto", "Photo uploaded ");
                return "success";
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

    public void submit_Image(View view) {
        switch(view.getId()){
            case R.id.photo_submit_button:
                progressBar.setVisibility(View.VISIBLE);
                if(!validate())
                    Toast.makeText(getBaseContext(), "Enter some data!", Toast.LENGTH_LONG).show();
                    // call AsynTask to perform network operation on separate thread
                else
                    new uploadPhoto.HttpAsyncTask().execute("https://imme-195707.appspot.com/uploadImage");
                break;
        }
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            uploadphotoModel = new uploadphotoModel();
            uploadphotoModel.setIdToken(idToken);
            uploadphotoModel.setPhoto1(photo1_String);
            uploadphotoModel.setPhoto2(photo2_String);
            uploadphotoModel.setPhoto3(photo3_String);
            uploadphotoModel.setPhoto4(photo4_String);
            uploadphotoModel.setPhoto5(photo5_String);

            return POST(urls[0],uploadphotoModel);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if (result.contains("Failed")) {
                Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
            else {
                Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    private boolean validate(){
        if(idToken.equals(""))
            return false;
        else if(photo1_String.equals(""))
            return false;
        else if(photo2_String.equals(""))
            return false;
        else if(photo3_String.equals(""))
            return false;
        else if(photo4_String.equals(""))
            return false;
        else if(photo5_String.equals(""))
            return false;
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
