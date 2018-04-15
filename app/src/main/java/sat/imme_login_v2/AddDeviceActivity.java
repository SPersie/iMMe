package sat.imme_login_v2;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class AddDeviceActivity extends AppCompatActivity  {

    String idToken;
    EditText deviceName;
    TextView otpTextView;
    AddDeviceModel addDeviceModel;
    EditText key;
    Button addLocker;
    ListView list;
    ArrayAdapter<String> adapter;
    ArrayList<String> arrayList;
    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        //IdToken =getIntent().getStringExtra("userID");
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

        key = findViewById(R.id.deviceKey);
        otpTextView =findViewById(R.id.otp);
        deviceName =findViewById(R.id.deviceName);
        addLocker =findViewById(R.id.buttonAddLocker);
        list = findViewById(R.id.list);

        edittextFilter(key);
        edittextFilter(deviceName);

        arrayList = new ArrayList<String>();

        // Adapter: You need three parameters 'the context, id of the layout (it will be where the data is shown),
        // and the array that contains the data
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, arrayList);

        // Here, you set the data in your ListView
        list.setAdapter(adapter);


        //TODO: test connection
//        if (isConnected()) {
//            Toast.makeText(webotp.this, "connected", Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(webotp.this, "not connected", Toast.LENGTH_LONG).show();
//        }




    }


    public String POST(String url, AddDeviceModel AddDeviceModel){
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
            jsonObject.accumulate("idToken", AddDeviceModel.getIdToken());
            jsonObject.accumulate("key", AddDeviceModel.getKey());
            jsonObject.accumulate("deviceName", AddDeviceModel.getdeviceName());


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
            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);

                //get the otp value from the response
                JsonElement root = new JsonParser().parse(result);
                String success =root.getAsJsonObject().get("success").getAsString();
                if (success.equals("true")) {
                    String deviceId =root.getAsJsonObject().get("deviceId").getAsString();
                    // this line adds the data of your EditText and puts in your array
                    arrayList.add(deviceName.getText().toString()+" : "+ deviceId);
                    // next thing you have to do is check if your adapter has changed
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getBaseContext(), "Authentication failed", Toast.LENGTH_LONG).show();
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

    public void addLocker(View view) {
        switch(view.getId()){
            case R.id.buttonAddLocker:
                if(!validate())
                    Toast.makeText(getBaseContext(), "Enter some data!", Toast.LENGTH_LONG).show();
                    // call AsynTask to perform network operation on separate thread
                else
                    new AddDeviceActivity.HttpAsyncTask().execute("https://imme-195707.appspot.com/addDevice");

                break;
        }
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            addDeviceModel = new AddDeviceModel();
            addDeviceModel.setIdToken(idToken);
            addDeviceModel.setKey(key.getText().toString());
            addDeviceModel.setdeviceName(deviceName.getText().toString());


            return POST(urls[0],addDeviceModel);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
//            Toast.makeText(getBaseContext(), IdToken, Toast.LENGTH_LONG).show();
//            Toast.makeText(getBaseContext(), deviceName.getText().toString(), Toast.LENGTH_LONG).show();
//            Toast.makeText(getBaseContext(), imageString, Toast.LENGTH_LONG).show();

            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
        }
    }

    public void edittextFilter(final EditText edt) {
        InputFilter[] filters = new InputFilter[1];
        final int len = edt.getText().toString().length();
        filters[0] = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {

                try {
                    char[] invaildInputs = new char[] { '.', ',', '#', '{',
                            '}', '[', ']', '$', '@', '`' };
                    for (int index = 0; index < end; index++) {
                        if (new String(invaildInputs).contains(String
                                .valueOf(source.charAt(index)))) {
                            Toast.makeText(getBaseContext(), "Invalid input is removed.", Toast.LENGTH_LONG).show();
                            return "";
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        edt.setFilters(filters);
    }



    private boolean validate(){
        String deviceNameString = deviceName.getText().toString();
        if(idToken.equals(""))
            return false;
        else if(deviceNameString.equals(""))
            //||deviceNameString.contains(".")||deviceNameString.contains("#")||deviceNameString.contains(",")||deviceNameString.contains("#")
            return false;
        else if(key.getText().toString().equals(""))
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
