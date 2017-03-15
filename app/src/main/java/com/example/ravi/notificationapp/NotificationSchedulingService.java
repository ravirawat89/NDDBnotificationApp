package com.example.ravi.notificationapp;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

/**
 * Created by Ravi on 05-Dec-16.
 */

public class NotificationSchedulingService extends IntentService
{
    public final static String EXTRA_MESSAGE = "com.example.ravi.notificationapp.MESSAGE";
    private  String message="";
    private static final String TAG = "NotificationService";
    String resBody = "",lastEvent,startTime;
    int HttpErrorCode,lastNot=0;
    StringBuilder builder = new StringBuilder();

    public NotificationSchedulingService()
    {
      super("SchedulingService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        // BEGIN_INCLUDE(service_onhandle)
        SharedPreferences NotPrefs = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = NotPrefs.edit();

          sendNotification(message);
         if(isNetworkConnected())
         {

             if(NotPrefs.getString("last Event time",null)== null)
             {
                 startTime = "2016-11-25 11:14:54";
             }

             else {
                 //startTime = NotPrefs.getString("last Event time",null);
                 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                 String dateInString = NotPrefs.getString("last Event time",null);
                 try {
                     Date date = sdf.parse(dateInString);
                     Calendar gc = new GregorianCalendar();
                     gc.setTime(date);
                     gc.add(Calendar.SECOND, 1);
                     Date d2 = gc.getTime();
                     startTime  = sdf.format(d2);
                 } catch (ParseException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
                 }

             String url = "http://sensecan.org/wisekar/api/resource.php/resource/events";
             List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
             nameValuePairs.add(new BasicNameValuePair("key", "ixbmrd686715twqpmz674515nafbyv")); //nameValuePairs.add(new BasicNameValuePair("key", "AuhISTvpzXS7xU96MF2fIpFbT844"));
             //nameValuePairs.add(new BasicNameValuePair("typeId", "9"));
            // nameValuePairs.add(new BasicNameValuePair("datasetId", "29"));
             nameValuePairs.add(new BasicNameValuePair("startTime", startTime));
             String paramsString = URLEncodedUtils.format(nameValuePairs, "UTF-8");

             HttpClient client = new DefaultHttpClient();
             HttpGet request = new HttpGet(url + "?" + paramsString); // replace with your
             // url
             // making request

             HttpResponse response;
             try {
                 response = client.execute(request);
                 Log.d("Response of GET request", response.toString());
                 HttpErrorCode = response.getStatusLine().getStatusCode();
                 if(HttpErrorCode==200)
                     resBody = EntityUtils.toString(response.getEntity());

             } catch (ClientProtocolException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }

             if (resBody!=null&&HttpErrorCode==200)
             {
                 try {
                     JSONArray json = new JSONArray(resBody);
                     JSONObject obj = json.getJSONObject(0);
                     lastEvent = obj.getString("addedOn");
                     editor.putString("last Event time",lastEvent);
                     editor.commit();

                     for (int i = 0; i < json.length(); i++)
                     {
                         obj = json.getJSONObject(i);
                         String EventId = obj.getString("eventId");
                         builder.append(EventId).append(" ");
                     }
                    // builder.append("\n");
                     message = builder.toString();
                 } catch (final JSONException e) {
                     Log.e(TAG, "Json parsing error:" + e.getMessage());}
                 }
               if(HttpErrorCode==200&&message!=null)
                   sendNotification(message);

         }


        // Release the wake lock provided by the BroadcastReceiver.
        NotificationAlarmReceiver.completeWakefulIntent(intent);
        // END_INCLUDE(service_onhandle)

        //*****************************// Broadcast receiver for updating main UI****************************************
        Intent i = new Intent("DATA_UPDATED");
        i.putExtra(EXTRA_MESSAGE,message);
        sendBroadcast(i);
        //***************************************************************************************************************
    }


    private void sendNotification(String message)
    {
        Random random = new Random();
        int notifyID = random.nextInt(9999 - 1000) + 1000;

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.notify);
        mBuilder.setContentTitle("Animal ID:1468 is in heat!");  //Notification Alert, Click Me!
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

    //***********************************GET EVENT********************************************************************///////////////////
  /*  class  makeGetRequestTask extends AsyncTask<String, Void, String> {

        String resBody = " ";
        int HttpErrorCode;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... urls) {
            String url = "http://sensecan.org/wisekar/api/resource.php/resource/events";
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
            nameValuePairs.add(new BasicNameValuePair("key", "ma5Tfkp3ajZKPoP746sDCHdd7144"));
            nameValuePairs.add(new BasicNameValuePair("typeId", "9"));
            nameValuePairs.add(new BasicNameValuePair("datasetId", "29"));
            nameValuePairs.add(new BasicNameValuePair("startTime", "2016-11-25 11:14:54"));
            nameValuePairs.add(new BasicNameValuePair("endTime", "2016-11-25 11:28:11"));
            String paramsString = URLEncodedUtils.format(nameValuePairs, "UTF-8");

            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url + "?" + paramsString); // replace with your
            // url
            // making request

            HttpResponse response;
            try {
                response = client.execute(request);
                Log.d("Response of GET request", response.toString());
                HttpErrorCode = response.getStatusLine().getStatusCode();
                resBody = EntityUtils.toString(response.getEntity());

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return resBody;

        }

        protected void onPostExecute(String result) {

            //sendNotification(result);
            if (result != null) {
                try {
                    JSONArray json = new JSONArray(result);
                    for (int i = 0; i < json.length(); i++) {
                        JSONObject obj1 = json.getJSONObject(i);
                        String EventId = obj1.getString("eventId");
                        String s = (new StringBuilder()).append(EventId + ",").toString();

                    }
                } catch (final JSONException e) {
                    //  Log.e(TAG, "Json parsing error:" + e.getMessage());}
                }
            }
        }

    }*/

    private boolean isNetworkConnected()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }

}
