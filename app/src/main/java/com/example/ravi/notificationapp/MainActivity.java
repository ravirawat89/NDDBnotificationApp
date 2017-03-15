package com.example.ravi.notificationapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity
{

    private Button send;
    private TextView msg;
    private final static String EXTRA_MESSAGE = "com.example.ravi.notificationapp.MESSAGE";
    private  String message="Alarm set";
    NotificationAlarmReceiver alarm = new NotificationAlarmReceiver();
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        msg=(TextView)findViewById(R.id.textMsg);
        registerReceiver(uiUpdated, new IntentFilter("DATA_UPDATED"));  // Register Broadcast receiver for updating main UI

        final NotificationAlarmReceiver alarm = new NotificationAlarmReceiver();
        alarm.setAlarm(this);

       send=(Button)findViewById(R.id.sendNotify);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                 alarm.cancelAlarm(getApplicationContext());
                //if(isNetworkConnected())
                    //new makePostRequestTask().execute();
            }
        });
       if(getIntent()!=null)
        {
           String str = getIntent().getStringExtra(NotificationSchedulingService.EXTRA_MESSAGE);
           msg.setText(str);
        }
    }

    private BroadcastReceiver uiUpdated= new BroadcastReceiver()  //Broadcast Receiver to refress main UI of App

    {
    @Override
    public void onReceive(Context context, Intent intent) {

        msg.append((intent.getExtras().getString(NotificationSchedulingService.EXTRA_MESSAGE))+"\n");

    }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(uiUpdated, new IntentFilter("DATA_UPDATED"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(uiUpdated);
        NotificationManager notificationManager = (NotificationManager)getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    /*
    private void sendNotification(String message)
    {
        Random random = new Random();
        int notifyID = random.nextInt(9999 - 1000) + 1000;

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setSmallIcon(R.drawable.notify);
            mBuilder.setContentTitle("Notification Alert, Click Me!");
            mBuilder.setContentText(message);
            mBuilder.setAutoCancel(true);
            mBuilder.setSound(defaultSoundUri);


        Intent notificationIntent = new Intent(this, MainActivity.class);
               notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
               notificationIntent.putExtra(EXTRA_MESSAGE,message);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // notificationID allows you to update the notification later on.
        mNotificationManager.notify(notifyID, mBuilder.build());
    }
*/
    ///**************************POST EVENT--send Data to WiseKar****************************************************************************//////////////////////////////////////////////

    class  makePostRequestTask extends AsyncTask<String, Void, String> {

        String responseBody = " ";
        int ErrorCode;

        @Override
        protected String doInBackground(String... urls) {

            HttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost("http://sensecan.org/wisekar/api/resource.php/resource/event"); // replace with
            // your url

            try {

                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(5);

                nameValuePair.add(new BasicNameValuePair("key", "AuhISTvpzXS7xU96MF2fIpFbT844"));

                nameValuePair.add(new BasicNameValuePair("nodeId", "8078"));
                nameValuePair.add(new BasicNameValuePair("status", "6777868"));
                nameValuePair.add(new BasicNameValuePair("typeId", "9"));
                nameValuePair.add(new BasicNameValuePair("xmlFragment", "<additionalInfo><sensor><patientFirstName>$FirstName</patientFirstName><patientLastName>$LastName</patientLastName><bloodPressureMeasurement></bloodPressureMeasurement><bodyTemperature>N/A</bodyTemperature><pulseOximeter>Oxygen_Saturation=$PulseSPO2,Heart_Rate=$PulseBPM</pulseOximeter><galvanicSkinResponse>N/A</galvanicSkinResponse><airFlow>N/A</airFlow><ecg>" +(message.substring(0, message.length()))+ "</ecg><urineAnalysis>N/A</urineAnalysis></sensor></additionalInfo>"));

                // Encoding data

                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
                } catch (UnsupportedEncodingException e) {
                    // log exception
                    e.printStackTrace();
                }

                // making request

                HttpResponse response = httpClient.execute(httpPost);
                // write response to log

                Log.d("Http Post Response:", response.toString());
                responseBody = EntityUtils.toString(response.getEntity());
                ErrorCode = response.getStatusLine().getStatusCode();
            } catch (ClientProtocolException e) {
                // Log exception
                e.printStackTrace();
            } catch (IOException e) {
                // Log exception
                e.printStackTrace();
            }
            return responseBody;
        }

        protected void onPostExecute(String result)
        {

           // sendNotification(result);
        }

    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }


}