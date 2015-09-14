package jinryulkim.k_mountain.map;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ToggleButton;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapView;

import org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.PointData;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PointShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolylineShape;

import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

import jinryulkim.k_mountain.CommonUtils;
import jinryulkim.k_mountain.DialogActivity;
import jinryulkim.k_mountain.MtInfoMgr;
import jinryulkim.k_mountain.MtInfo_General;
import jinryulkim.k_mountain.R;

/**
 * Created by jinryulkim on 15. 9. 3..
 */
public class MapActivity extends Activity {

    public final static String EXTRA_INFO_POS = "extra_info_pos";
    private MtInfo_General mInfo = null;
    private ArrayList<GeoInformationWAY> mArrWayPoint = new ArrayList<GeoInformationWAY>();
    private ArrayList<GeoInformationWG> mArrWGWays = new ArrayList<GeoInformationWG>();

    private class GeoInformationWAY {
        public GeoPoint pt = null;
        public String SYM_NUM = null;
        public String SYM_TYP = null;   // 유형 코드
        public String SYM_NAM = null;   // 주요 명칭
        public String SYM_TYP2 = null;  // 고유 코드
        public String SYM_NAM2 = null;  // 고유 이름
        public String PHOTO = null;     // 사진파일명
        public String SYM_TXT = null;   // 비고
        public String CROSS_P = null;   // 표출 명칭
    }

    private class GeoInformationWG {
        public ArrayList<GeoPoint> pts = null;
        public String HSTR_MNNMB = null; // 이력관리번호
        public String SECTN_SEQ = null; // 구간순번
        public String SECTN_DSTNC = null; // 구간 거리
        public String SECTN_UPLN_TM = null; // 구간 상행 시간
        public String SECTN_GNGDN_TM = null; // 구간 하행 시간
        public String SECTN_DGDFF_NM = null; // 구간 난이도
    }

    // WAY_POINT_
    private final static int WAY_MT_STD_IDX = 0;
    private final static int WAY_SYM_NUM = 1;
    private final static int WAY_SYM_TYP = 2;
    private final static int WAY_SYM_NAM = 3;
    private final static int WAY_SYM_TYP2 = 4;
    private final static int WAY_SYM_NAM2 = 5;
    private final static int WAY_PHOTO = 6;
    private final static int WAY_SYM_TXT = 7;
    private final static int WAY_CROSS_P = 8;
    private final static int WAY_MNTN_NM = 9;
    private final static int WAY_LABEL = 10;

    // WG_MT_WAY
    private final static int WG_HSTR_MNNMB = 0;
    private final static int WG_MNTN_CD_NO = 1;
    private final static int WG_MNTN_NM = 2;
    private final static int WG_SECTN_SEQ = 3;
    private final static int WG_SECTN_DSTNC = 4;
    private final static int WG_SECTN_UPLN_TM = 5;
    private final static int WG_SECTN_GNGDN_TM = 6;
    private final static int WG_SECTN_DGDFF_NM = 7;
    private final static int WG_LABEL = 8;

    private final static int REQCODE_GPSOFF = 100;

    private final static int GPS_STATE_OFF = 0;
    private final static int GPS_STATE_TRACKING = 1;
    private final static int GPS_STATE_NO_TRACKING = 2;
    private int mGPSState = GPS_STATE_OFF;

    private final static int MAP_POINT_MT   = 0;
    private final static int MAP_POINT_ME   = 1;
    private final static int MAP_POINT_WAY  = 1000;
    private final static int MAP_POINT_WG   = 10000;

    private MapView mapView = null;
    private boolean mbGPSProvider, mbNETProvider;
    private GeoPoint mMtPt = null, mMePt = null;

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (mGPSState == GPS_STATE_TRACKING) {
                makeMyPoint(location);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            if (LocationManager.GPS_PROVIDER.equals(provider))
                mbGPSProvider = true;
            else if (LocationManager.NETWORK_PROVIDER.equals(provider))
                mbNETProvider = true;

            if ((mbGPSProvider || mbNETProvider) && mGPSState == GPS_STATE_OFF) {
                mGPSState = GPS_STATE_NO_TRACKING;
                ((ImageView)findViewById(R.id.ivGPS)).setImageResource(R.drawable.ic_location_searching_white);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            if (LocationManager.GPS_PROVIDER.equals(provider))
                mbGPSProvider = false;
            else if (LocationManager.NETWORK_PROVIDER.equals(provider))
                mbNETProvider = false;

            if (mbGPSProvider == false && mbNETProvider == false) {
                mGPSState = GPS_STATE_OFF;
                ((ImageView)findViewById(R.id.ivGPS)).setImageResource(R.drawable.ic_gps_off_white);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        int position = getIntent().getIntExtra(EXTRA_INFO_POS, -1);
        if (position < 0 || position >= MtInfoMgr.mMtInfos.size()) {
            finish();
        }

        try {
            mInfo = MtInfoMgr.mMtInfos.get(position);
            mArrWayPoint.clear();
            mArrWGWays.clear();

            // WAY_POINT's shape type is POINT
            String WayPointPath = getCacheDir() + "/geo/" + mInfo.code + "/WAY_POINT_" + mInfo.code + "/WAY_POINT_" + mInfo.code;
            String shpPath = WayPointPath + ".shp";

            FileInputStream fis = new FileInputStream(shpPath);
            ShapeFileReader r = new ShapeFileReader(fis);

            AbstractShape as;
            while ((as = r.next()) != null) {
                switch (as.getShapeType()) {
                    case POINT:
                        // TM 좌표
                        PointShape aPoint = (PointShape) as;
                        // 위/경도 좌표
                        GeoInformationWAY info = new GeoInformationWAY();
                        info.pt = GeoTrans.convert(GeoTrans.GRS80, GeoTrans.GEO, new GeoPoint(aPoint.getX(), aPoint.getY()));
                        mArrWayPoint.add(info);
                        break;
                    default:
                        break;
                }
            }
            fis.close();

            String dbfPath = WayPointPath + ".dbf";
            DBFReader dbfReader = new DBFReader(dbfPath);
            int i = 0;
            while (dbfReader.hasNextRecord()) {
                Object obj[] = dbfReader.nextRecord(Charset.forName("euc-kr"));
                for (int j = 0; j < obj.length; j++) {
                    switch (j) {
                        case WAY_MT_STD_IDX:
                            break;
                        case WAY_SYM_NUM:
                            mArrWayPoint.get(i).SYM_NUM = "" + obj[j];
                            break;
                        case WAY_SYM_TYP:
                            mArrWayPoint.get(i).SYM_TYP = "" + obj[j];
                            break;
                        case WAY_SYM_NAM:
                            mArrWayPoint.get(i).SYM_NAM = "" + obj[j];
                            break;
                        case WAY_SYM_TYP2:
                            mArrWayPoint.get(i).SYM_TYP2 = "" + obj[j];
                            break;
                        case WAY_SYM_NAM2:
                            mArrWayPoint.get(i).SYM_NAM2 = "" + obj[j];
                            break;
                        case WAY_PHOTO:
                            mArrWayPoint.get(i).PHOTO = "" + obj[j];
                            break;
                        case WAY_SYM_TXT:
                            mArrWayPoint.get(i).SYM_TXT = "" + obj[j];
                            break;
                        case WAY_CROSS_P:
                            mArrWayPoint.get(i).CROSS_P = "" + obj[j];
                            break;
                        case WAY_MNTN_NM:
                            break;
                        case WAY_LABEL:
                            break;

                    }

                }
                i++;
            }

            // WG_MT_WAY's shape type is POLYLINE
            WayPointPath = getCacheDir() + "/geo/" + mInfo.code + "/WG_MT_WAY_" + mInfo.code + "/WG_MT_WAY_" + mInfo.code;
            shpPath = WayPointPath + ".shp";
            fis = new FileInputStream(shpPath);
            r = new ShapeFileReader(fis);
            while ((as = r.next()) != null) {
                switch (as.getShapeType()) {
                    case POLYLINE:
                        PolylineShape aPolyline = (PolylineShape) as;
                        PointData[] points = aPolyline.getPointsOfPart(0);
                        GeoInformationWG info = new GeoInformationWG();
                        info.pts = new ArrayList<GeoPoint>();
                        for (i = 0; i < points.length; i++) {
                            info.pts.add(GeoTrans.convert(GeoTrans.GRS80, GeoTrans.GEO, new GeoPoint(points[i].getX(), points[i].getY())));
                        }
                        mArrWGWays.add(info);
                        break;
                }
            }
            fis.close();

            dbfPath = WayPointPath + ".dbf";
            dbfReader = new DBFReader(dbfPath);
            i = 0;
            while (dbfReader.hasNextRecord()) {
                Object obj[] = dbfReader.nextRecord(Charset.forName("euc-kr"));
                for (int j = 0; j < obj.length; j++) {
                    switch (j) {
                        case WG_HSTR_MNNMB:
                            mArrWGWays.get(i).HSTR_MNNMB = "" + obj[j];
                            break;
                        case WG_MNTN_CD_NO:
                            break;
                        case WG_MNTN_NM:
                            break;
                        case WG_SECTN_SEQ:
                            mArrWGWays.get(i).SECTN_SEQ = "" + obj[j];
                            break;
                        case WG_SECTN_DSTNC:
                            mArrWGWays.get(i).SECTN_DSTNC = "" + obj[j];
                            break;
                        case WG_SECTN_UPLN_TM:
                            mArrWGWays.get(i).SECTN_UPLN_TM = "" + obj[j];
                            break;
                        case WG_SECTN_GNGDN_TM:
                            mArrWGWays.get(i).SECTN_GNGDN_TM = "" + obj[j];
                            break;
                        case WG_SECTN_DGDFF_NM:
                            mArrWGWays.get(i).SECTN_DGDFF_NM = "" + obj[j];
                            break;
                        case WG_LABEL:
                            break;
                    }
                }
                i++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        if (mArrWayPoint.size() > 0) {

            double maxX = 0, maxY = 0;
            double minX = 200, minY = 200;
            double curX, curY;
            for (int i = 0; i < mArrWayPoint.size(); i++) {
                curX = mArrWayPoint.get(i).pt.getX();
                curY = mArrWayPoint.get(i).pt.getY();

                if (maxX < curX) maxX = curX;
                if (maxY < curY) maxY = curY;
                if (minX > curX) minX = curX;
                if (minY > curY) minY = curY;
            }

            double avgX = (maxX + minX) / 2;
            double avgY = (maxY + minY) / 2;

            mMtPt = new GeoPoint(avgX, avgY);

            mapView = new MapView(MapActivity.this);
            mapView.setDaumMapApiKey("58692d6bc7249daaf10d0aca8142f896");
            ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
            mapViewContainer.addView(mapView);
            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(mMtPt.getY(), mMtPt.getX());
            mapView.setMapCenterPoint(mapPoint, true);
            mapView.setZoomLevel(3, true);

            MapPOIItem marker = new MapPOIItem();
            marker.setItemName(mInfo.name);
            marker.setTag(MAP_POINT_MT);
            marker.setMapPoint(mapPoint);
            marker.setMarkerType(MapPOIItem.MarkerType.YellowPin);
            marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
            mapView.addPOIItem(marker);
        }

        CommonUtils.typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        CommonUtils.setGlobalFont(getWindow().getDecorView(), CommonUtils.typeface);

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        mbGPSProvider = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        mbNETProvider = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (mbGPSProvider == false && mbNETProvider == false) {
            ((ImageView) findViewById(R.id.ivGPS)).setImageResource(R.drawable.ic_gps_off_white);
            mGPSState = GPS_STATE_OFF;
        } else {
            ((ImageView) findViewById(R.id.ivGPS)).setImageResource(R.drawable.ic_location_searching_white);
            mGPSState = GPS_STATE_NO_TRACKING;
        }

        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, mLocationListener);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, mLocationListener);

        findViewById(R.id.ivGPS).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mbGPSProvider == false && mbNETProvider == false) {
                    mGPSState = GPS_STATE_OFF;
                    ((ImageView) findViewById(R.id.ivGPS)).setImageResource(R.drawable.ic_gps_off_white);
                    DialogActivity.launchDialog(MapActivity.this, DialogActivity.DLGTYPE_GPSOFF, REQCODE_GPSOFF);
                } else {
                    switch (mGPSState) {
                        case GPS_STATE_OFF:
                        case GPS_STATE_NO_TRACKING:
                            mGPSState = GPS_STATE_TRACKING;
                            ((ImageView) findViewById(R.id.ivGPS)).setImageResource(R.drawable.ic_gps_fixed_white);
                            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
                            mbGPSProvider = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                            mbNETProvider = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                            boolean located = false;
                            if (mbNETProvider) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        break;
                                    }
                                }
                                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (location != null) {
                                    makeMyPoint(location);
                                    located = true;
                                }
                            }

                            if (mbGPSProvider && located == false) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        break;
                                    }
                                }
                                Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                if (location != null) {
                                    makeMyPoint(location);
                                }
                            }
                            break;

                        case GPS_STATE_TRACKING:
                            mGPSState = GPS_STATE_NO_TRACKING;
                            removeMyPoint();
                            ((ImageView) findViewById(R.id.ivGPS)).setImageResource(R.drawable.ic_location_searching_white);
                            mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(mMtPt.getY(), mMtPt.getX()), true);
                            break;
                    }
                }
            }
        });

        ((ToggleButton) findViewById(R.id.tbPoints)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    for (int i = 0; i < mArrWayPoint.size(); i++) {
                        MapPOIItem marker = new MapPOIItem();

                        String name = mArrWayPoint.get(i).SYM_NAM2;
                        if (name == null || name.length() <= 0) {
                            name = mArrWayPoint.get(i).SYM_TXT;

                            if (name == null || name.length() <= 0) {
                                name = mArrWayPoint.get(i).CROSS_P;
                            }
                        }

                        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(mArrWayPoint.get(i).pt.getY(), mArrWayPoint.get(i).pt.getX());

                        int tag = MAP_POINT_WAY + i;
                        marker.setItemName(name);
                        marker.setTag(tag);
                        Log.i("jrkim", "add tag : "+ tag);
                        marker.setMapPoint(mapPoint);
                        marker.setMarkerType(MapPOIItem.MarkerType.YellowPin);
                        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
                        mapView.addPOIItem(marker);
                    }
                } else {
                    mapView.removeAllPOIItems();

                    MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(mMtPt.getY(), mMtPt.getX());
                    MapPOIItem marker = new MapPOIItem();
                    marker.setItemName(mInfo.name);
                    marker.setTag(MAP_POINT_MT);
                    marker.setMapPoint(mapPoint);
                    marker.setMarkerType(MapPOIItem.MarkerType.YellowPin);
                    marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
                    mapView.addPOIItem(marker);

                    if(mGPSState == GPS_STATE_TRACKING && mMePt != null) {
                        mapPoint = MapPoint.mapPointWithGeoCoord(mMePt.getX(), mMePt.getY());
                        marker = new MapPOIItem();
                        marker.setItemName(getString(R.string.MAP_MY_POINT));
                        marker.setTag(MAP_POINT_ME);
                        marker.setMapPoint(mapPoint);
                        marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
                        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
                        mapView.addPOIItem(marker);
                    }
                }
            }
        });

        ((ToggleButton) findViewById(R.id.tbWays)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    for (int i = 0; i < mArrWGWays.size(); i++) {
                        MapPolyline polyline = new MapPolyline();
                        polyline.setTag(1000 + i);
                        polyline.setLineColor(Color.argb(128, 255, 51, 0));

                        for (int j = 0; j < mArrWGWays.get(i).pts.size(); j++) {
                            polyline.addPoint(MapPoint.mapPointWithGeoCoord(
                                    mArrWGWays.get(i).pts.get(j).getY(),
                                    mArrWGWays.get(i).pts.get(j).getX()));
                        }
                        mapView.addPolyline(polyline);
                    }
                } else {
                    mapView.removeAllPolylines();
                }
            }
        });
    }

    private void removeMyPoint() {
        MapPOIItem [] items = mapView.getPOIItems();
        if(items != null) {
            for(int i = 0; i < items.length; i++) {
                if(items[i].getTag() == MAP_POINT_ME) {
                    mapView.removePOIItem(items[i]);
                    break;
                }
            }
        }
    }

    private void makeMyPoint(Location location) {
        removeMyPoint();

        mMePt = new GeoPoint(location.getLatitude(), location.getLongitude());
        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(mMePt.getX(), mMePt.getY());
        MapPOIItem marker = new MapPOIItem();
        marker.setItemName(getString(R.string.MAP_MY_POINT));
        marker.setTag(MAP_POINT_ME);
        marker.setMapPoint(mapPoint);
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        mapView.addPOIItem(marker);
        mapView.setMapCenterPoint(mapPoint, true);
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
    protected void onDestroy() {
        super.onDestroy();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        lm.removeUpdates(mLocationListener);
    }
}
