package jinryulkim.k_mountain.help;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import jinryulkim.k_mountain.CommonUtils;
import jinryulkim.k_mountain.DialogActivity;
import jinryulkim.k_mountain.R;

/**
 * Created by jinryulkim on 15. 9. 18..
 */
public class HelpActivity extends AppCompatActivity implements View.OnClickListener {

    private LocationManager mLocationManager = null;
    private boolean mbGPSProvider = false;
    private boolean mbNETProvider = false;
    private final static int REQCODE_GPSOFF = 100;
    private Location mCurrentLocation = null;
    private String mOldAddress = "";
    private String mNewAddress = "";

    private final static int PERMISSION_REQCODE_LOCATION = 1;

    private final static int MESSAGE_GETADDRESS = 1000;

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_GETADDRESS:
                findViewById(R.id.btnFire).setEnabled(true);
                findViewById(R.id.btnHelpMe).setEnabled(true);
                hideProgress();
                break;
        }
    }

    private static HelpHandler mHandler = null;

    static class HelpHandler extends Handler {
        private final WeakReference<HelpActivity> mActivity;

        HelpHandler(HelpActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            HelpActivity activity = mActivity.get();
            if (activity != null)
                activity.handleMessage(msg);
        }
    }

    ;

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mCurrentLocation = location;
            if (mCurrentLocation != null) {
                requestAddress();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            if (LocationManager.GPS_PROVIDER.equals(provider)) mbGPSProvider = true;
            else if (LocationManager.NETWORK_PROVIDER.equals(provider)) mbNETProvider = true;
        }

        @Override
        public void onProviderDisabled(String provider) {
            if (LocationManager.GPS_PROVIDER.equals(provider)) mbGPSProvider = false;
            else if (LocationManager.NETWORK_PROVIDER.equals(provider)) mbNETProvider = false;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResult) {
        switch (requestCode) {
            case PERMISSION_REQCODE_LOCATION:
                if (grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED && grantResult[1] == PackageManager.PERMISSION_GRANTED) {
                    init();
                } else {
                    Toast.makeText(this, getString(R.string.TOAST_HELP_NO_PERMISSION), Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private void init() {
        setContentView(R.layout.activity_119);

        mHandler = new HelpHandler(this);

        CommonUtils.typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        CommonUtils.setGlobalFont(getWindow().getDecorView(), CommonUtils.typeface);

        findViewById(R.id.btnFire).setOnClickListener(this);
        findViewById(R.id.btnHelpMe).setOnClickListener(this);
        findViewById(R.id.btnFire).setEnabled(false);
        findViewById(R.id.btnHelpMe).setEnabled(false);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mbGPSProvider = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        mbNETProvider = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!mbGPSProvider && !mbNETProvider) {
            DialogActivity.launchDialog(this, DialogActivity.DLGTYPE_GPSOFF, REQCODE_GPSOFF);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.TOAST_HELP_NO_PERMISSION), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, mLocationListener);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, mLocationListener);

        showProgress();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
           ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            init();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String [] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS},
                    PERMISSION_REQCODE_LOCATION
            );
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent intent) {
        super.onActivityResult(reqCode, resCode, intent);
        switch (reqCode) {
            case REQCODE_GPSOFF:
                if (resCode == DialogActivity.RESCODE_POSITIVE) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                } else {
                    finish();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        String strMessage = getString(R.string.TOAST_NO_GPS);

        boolean bHaveNoPermission = false;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            bHaveNoPermission = true;
        }

        if(mCurrentLocation == null && (mbGPSProvider || mbNETProvider)) {

            if(bHaveNoPermission == false) {
                if (mbGPSProvider) {
                    mCurrentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                } else {
                    mCurrentLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }
        }



        switch(v.getId()) {
            case R.id.btnFire:
                if(mCurrentLocation != null) {
                    strMessage = String.format(getString(R.string.HELP_FIRE_MESSAGE),
                            mCurrentLocation.getLatitude() + "", mCurrentLocation.getLongitude() + "", mNewAddress.length() > 0 ? mNewAddress : mOldAddress);
                    Log.i("jrkim", strMessage);
                    sendSMS("119", strMessage);
                    finish();
                } else {
                    Toast.makeText(this, strMessage, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnHelpMe:
                if(mCurrentLocation != null) {
                    strMessage = String.format(getString(R.string.HELP_SAFE_MESSAGE),
                            mCurrentLocation.getLatitude() + "", mCurrentLocation.getLongitude() + "", mNewAddress.length() > 0 ? mNewAddress : mOldAddress);
                    Log.i("jrkim", strMessage);
                    sendSMS("119", strMessage);
                    finish();
                } else {
                    Toast.makeText(this, strMessage, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void sendSMS(String number, String text) {


        Log.i("jrkim", "sendSMS : " + number);
        /*PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT_ACTION"), 0);
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED_ACTION"), 0);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("jrkim", "onReceive - sent:" + getResultCode());
                switch (getResultCode()) {

                }
            }
        }, new IntentFilter("SMS_SENT_ACTION"));

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("jrkim", "onReceive - delivered:" + getResultCode());
                switch(getResultCode()) {

                }
            }
        }, new IntentFilter("SMS_+DELIVERED_ACTION"));

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, text, sentIntent, deliveredIntent);
        */

        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + number));
        intent.putExtra("sms_body", text);
        startActivity(intent);

    }


    private void requestAddress() {
        new Thread() {
            @Override
            public void run() {
                try {
                    String addr = "https://apis.daum.net/local/geo/coord2detailaddr?apikey=ffa4604fc5f73524b415b4ffde795fa7&x="+
                            mCurrentLocation.getLongitude() + "&y="+ mCurrentLocation.getLatitude() + "&inputCoordSystem=WGS84&output=xml";

                    URL url = new URL(addr);

                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("User-Agent", "Mozilla/5.0");
                    int responseCode = con.getResponseCode();

                    if(responseCode == 200) {
                        BufferedReader br = new BufferedReader( new InputStreamReader(con.getInputStream()));
                        String line = "";
                        String strXML = "";

                        while((line = br.readLine()) != null) {
                            strXML += line;
                        }

                        strXML = strXML.substring(strXML.indexOf("<old><name value="));
                        mOldAddress = strXML.substring(18, strXML.indexOf("/>") - 1).replaceAll("\'", "").trim();
                        strXML = strXML.substring(strXML.indexOf("<new><name value="));
                        mNewAddress = strXML.substring(18, strXML.indexOf("/>") - 1).replaceAll("\'", "").trim();

                        br.close();
                        mHandler.sendEmptyMessage(MESSAGE_GETADDRESS);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.zoom_exit);
    }

    private void showProgress() {
        findViewById(R.id.ivProgress).startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate));
        findViewById(R.id.rlProgress).setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        findViewById(R.id.ivProgress).clearAnimation();
        findViewById(R.id.rlProgress).setVisibility(View.GONE);
    }
}
