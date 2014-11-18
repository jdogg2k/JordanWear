package com.jorose.jordanwear;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Map;


public class MyActivity extends Activity {

    public Document scoreDoc;
    private String TAG = "jordanwear";
    GoogleApiClient mGoogleApiClient;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(TAG, "onConnected: " + connectionHint);
                        // Now you can use the data layer API
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(TAG, "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
                    }
                })
                        // Request access only to the Wearable API
                .addApi(Wearable.API)
                .build();


        new loadSiteData().execute();

    }

    private class loadSiteData extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... params) {
            Connection.Response res = null;

            try {
                res = Jsoup
                        .connect("http://www.cbssports.com/login?xurl=http://fabf2000.basketball.cbssports.com/")
                        .data("userid", "jlrose2")
                        .data("password", "bentley")
                        .method(Connection.Method.POST)
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert res != null;
            try {
                Document doc = res.parse();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Map<String, String> cookies = res.cookies();

            Connection connection = Jsoup.connect("http://fabf2000.basketball.cbssports.com/");
            for (Map.Entry<String, String> cookie : cookies.entrySet()) {
                connection.cookie(cookie.getKey(), cookie.getValue());
            }
            try {
                scoreDoc = connection.get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(String result) {
            parseHTML();
        }
    }

    private void parseHTML() {
        Element leagueMatchupFirst = scoreDoc.select("div.leagueHeaderMatchupName").first();
        String firstTeam = leagueMatchupFirst.text();

        Element leagueMatchupLast = scoreDoc.select("div.leagueHeaderMatchupName").last();
        String secondTeam = leagueMatchupLast.text();

        Element leagueScoreFirst = scoreDoc.select("div.leagueHeaderMatchupScore").first();
        String firstScore = leagueScoreFirst.text();

        Element leagueScoreLast = scoreDoc.select("div.leagueHeaderMatchupScore").last();
        String secondScore = leagueScoreLast.text();

        PutDataMapRequest dataMap = PutDataMapRequest.create("/score");
        dataMap.getDataMap().putString("firstTeam", firstTeam);
        dataMap.getDataMap().putString("secondTeam", secondTeam);
        dataMap.getDataMap().putLong("firstScore", Long.valueOf(firstScore));
        dataMap.getDataMap().putLong("secondScore", Long.valueOf(secondScore));
        PutDataRequest request = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, request);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }
}
