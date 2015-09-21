package jinryulkim.k_mountain.help;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import jinryulkim.k_mountain.CommonUtils;
import jinryulkim.k_mountain.DialogActivity;
import jinryulkim.k_mountain.R;

/**
 * Created by jinryulkim on 15. 9. 18..
 */
public class HelpActivity extends Activity implements View.OnClickListener {

    private LocationManager mLocationManager = null;
    private boolean mbGPSProvider = false;
    private boolean mbNETProvider = false;
    private final static int REQCODE_GPSOFF = 100;
    private Location mCurrentLocation = null;

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mCurrentLocation = location;
            if(mCurrentLocation != null) {
                findViewById(R.id.btnFire).setEnabled(true);
                findViewById(R.id.btnHelpMe).setEnabled(true);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.TOAST_HELP_NO_PERMISSION), Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        setContentView(R.layout.activity_119);

        CommonUtils.typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        CommonUtils.setGlobalFont(getWindow().getDecorView(), CommonUtils.typeface);

        findViewById(R.id.btnFire).setOnClickListener(this);
        findViewById(R.id.btnHelpMe).setOnClickListener(this);
        findViewById(R.id.btnFire).setEnabled(false);
        findViewById(R.id.btnHelpMe).setEnabled(false);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mbGPSProvider = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        mbNETProvider = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 10, mLocationListener);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 10, mLocationListener);

        if(!mbGPSProvider && !mbNETProvider) {
            DialogActivity.launchDialog(this, DialogActivity.DLGTYPE_GPSOFF, REQCODE_GPSOFF);
        }

        if(mbGPSProvider) {
            mCurrentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else if(mbNETProvider) {
            mCurrentLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if(mCurrentLocation != null) {
            findViewById(R.id.btnFire).setEnabled(true);
            findViewById(R.id.btnHelpMe).setEnabled(true);
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent intent) {
        super.onActivityResult(reqCode, resCode, intent);
        switch (reqCode) {
            case REQCODE_GPSOFF:
                if (resCode == DialogActivity.RESCODE_POSITIVE) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                bHaveNoPermission = true;
            }
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
                            mCurrentLocation.getLatitude() + "", mCurrentLocation.getLongitude() + "");
                    Toast.makeText(this, strMessage, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, strMessage, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnHelpMe:
                if(mCurrentLocation != null) {
                    strMessage = String.format(getString(R.string.HELP_SAFE_MESSAGE),
                            mCurrentLocation.getLatitude() + "", mCurrentLocation.getLongitude() + "");
                    Toast.makeText(this, strMessage, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, strMessage, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.zoom_exit);
    }
}
