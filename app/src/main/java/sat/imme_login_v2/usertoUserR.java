package sat.imme_login_v2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import java.nio.charset.Charset;

public class usertoUserR extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback,
        NfcAdapter.OnNdefPushCompleteCallback {

    private static final int CAMERA_REQUEST = 1888;
    EditText receiverOtp;
    ImageView userPhoto;
    TextView otpTextView;
    Button submit;
    String imageString;
    String idToken;
    FirebaseUser mUser;
    usertoReceiverModel usertoReceiverModel;
    ProgressBar progressBar;

    NfcAdapter mNfcAdapter;

    private static final int MESSAGE_SENT = 1;
    private String Key="";

    NfcAdapter mNfcAdapter;

    private static final int MESSAGE_SENT = 1;
    private String Key="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userto_user_r);


        progressBar = findViewById(R.id.user_receive_progress_bar);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        otpTextView =findViewById(R.id.receive_otp);
        if (mNfcAdapter == null) {
            otpTextView = (TextView)findViewById(R.id.nfc_textView);
            otpTextView.setText("NFC is not available on this device.");
        }

        receiverOtp =findViewById(R.id.receiver_otp);

        submit =findViewById(R.id.receive_submit_button);
        userPhoto =findViewById(R.id.receive_user_photo);

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
            Toast.makeText(usertoUserR.this, "connected", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(usertoUserR.this, "not connected", Toast.LENGTH_LONG).show();
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            otpTextView.setText("NFC is not available on this device.");
        }
        // Register callback to set NDEF message
        mNfcAdapter.setNdefPushMessageCallback(this, this);
        // Register callback to listen for message-sent success
        mNfcAdapter.setOnNdefPushCompleteCallback(this, this);

    }

    /**
     * Implementation for the CreateNdefMessageCallback interface
     */
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { createMimeRecord(
                        "application/user_to_user.beam", Key.getBytes())

                });
        return msg;
    }

    public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        NdefRecord mimeRecord = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
        return mimeRecord;
    }
    /**
     * Implementation for the OnNdefPushCompleteCallback interface
     */
    @Override
    public void onNdefPushComplete(NfcEvent arg0) {
        // A handler is needed to send messages to the activity when this
        // callback occurs, because it happens from a binder thread
        mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
    }

    /** This handler receives a message from onNdefPushComplete */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SENT:
                    Toast.makeText(getApplicationContext(), "OTP sent!", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type
        String mKey = new String(msg.getRecords()[0].getPayload());
        receiverOtp.setText(mKey);
        Log.i("jinjing","set text");
    }
    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
            Log.i("jinjing","processIntent");
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
        Log.i("jinjing","setIntent");
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



    public String POST(String url, usertoReceiverModel usertoReceiverModel){
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
            jsonObject.accumulate("otp", usertoReceiverModel.getmyOtp());
            jsonObject.accumulate("idToken", usertoReceiverModel.getIdToken());
            jsonObject.accumulate("image", usertoReceiverModel.getmyImage());

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
            if(inputStream == null) {
                return "Failed: Unknown Error";
            }
            result = convertInputStreamToString(inputStream);

            //get the otp value from the response
            JsonElement root = new JsonParser().parse(result);
            String success =root.getAsJsonObject().get("success").getAsString();
            if (success.equals("true")) {
                return success;
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

    public void receive_submitJson(View view) {
        switch(view.getId()){
            case R.id.receive_submit_button:
                if(!validate())
                    Toast.makeText(getBaseContext(), "Enter some data!", Toast.LENGTH_LONG).show();
                    // call AsynTask to perform network operation on separate thread
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    new usertoUserR.HttpAsyncTask().execute("https://imme-195707.appspot.com/checkKey");
                }
                break;
        }
    }


    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            usertoReceiverModel = new usertoReceiverModel();
            usertoReceiverModel.setIdToken(idToken);
            usertoReceiverModel.setmyImage(imageString);
            usertoReceiverModel.setmyOtp(receiverOtp.getText().toString());
            return POST(urls[0],usertoReceiverModel);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
//            Log.d("usertoUserR", "onPostExecute " + result);
            otpTextView.setVisibility(View.VISIBLE);
            if (result.contains("Failed")) {
                otpTextView.setText(result);
                progressBar.setVisibility(View.GONE);
                otpTextView.setTextColor(Color.RED);
            }
            else {
                otpTextView.setText(result);
                progressBar.setVisibility(View.GONE);
                otpTextView.setTextColor(Color.BLACK);
            }
        }
    }





    private boolean validate(){
        if(idToken.equals(""))
            return false;
        else if(receiverOtp.equals(""))
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