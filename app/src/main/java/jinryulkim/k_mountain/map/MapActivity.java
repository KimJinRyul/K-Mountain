package jinryulkim.k_mountain.map;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.CompoundButton;
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


    MapView mapView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        int position = getIntent().getIntExtra(EXTRA_INFO_POS, -1);
        if(position < 0 || position >= MtInfoMgr.mMtInfos.size()) {
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
            while((as = r.next()) != null) {
                switch(as.getShapeType()) {
                    case POINT:
                        // TM 좌표
                        PointShape aPoint = (PointShape)as;
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

            Log.i("jrkim", "open dbf");
            String dbfPath = WayPointPath + ".dbf";
            DBFReader dbfReader = new DBFReader(dbfPath);
            int i = 0;
            while(dbfReader.hasNextRecord()) {
                Object obj[] = dbfReader.nextRecord(Charset.forName("euc-kr"));
                for(int j = 0; j < obj.length; j++) {
                    Log.i("jrkim", j + ")" + obj[j]);
                    switch(j) {
                        case WAY_MT_STD_IDX:        break;
                        case WAY_SYM_NUM:   mArrWayPoint.get(i).SYM_NUM =  "" + obj[j];      break;
                        case WAY_SYM_TYP:   mArrWayPoint.get(i).SYM_TYP = "" + obj[j];         break;
                        case WAY_SYM_NAM:   mArrWayPoint.get(i).SYM_NAM = "" + obj[j];        break;
                        case WAY_SYM_TYP2:  mArrWayPoint.get(i).SYM_TYP2 = "" + obj[j];         break;
                        case WAY_SYM_NAM2:  mArrWayPoint.get(i).SYM_NAM2 = "" + obj[j];         break;
                        case WAY_PHOTO:     mArrWayPoint.get(i).PHOTO = "" + obj[j];      break;
                        case WAY_SYM_TXT:   mArrWayPoint.get(i).SYM_TXT = "" + obj[j];        break;
                        case WAY_CROSS_P:   mArrWayPoint.get(i).CROSS_P = "" + obj[j];         break;
                        case WAY_MNTN_NM:           break;
                        case WAY_LABEL:           break;

                    }

                }
                i++;
            }

            // WG_MT_WAY's shape type is POLYLINE
            WayPointPath = getCacheDir() + "/geo/" + mInfo.code + "/WG_MT_WAY_" + mInfo.code + "/WG_MT_WAY_" + mInfo.code;
            shpPath = WayPointPath + ".shp";
            Log.i("jrkim", "shpPath:" + shpPath);
            fis = new FileInputStream(shpPath);
            r = new ShapeFileReader(fis);
            Log.i("jrkim", "while...");
            while((as = r.next()) != null) {
                Log.i("jrkim", "while.......");
                switch (as.getShapeType()) {
                    case POLYLINE:
                        PolylineShape aPolyline = (PolylineShape)as;
                        PointData[] points = aPolyline.getPointsOfPart(0);
                        GeoInformationWG info = new GeoInformationWG();
                        info.pts = new ArrayList<GeoPoint>();
                        for(i = 0; i < points.length; i++) {
                            info.pts.add(GeoTrans.convert(GeoTrans.GRS80, GeoTrans.GEO, new GeoPoint(points[i].getX(), points[i].getY())));
                        }
                        mArrWGWays.add(info);
                        break;
                }
            }
            fis.close();

            Log.i("jrkim", "open dbf");
            dbfPath = WayPointPath + ".dbf";
            dbfReader = new DBFReader(dbfPath);
            i = 0;
            while(dbfReader.hasNextRecord()) {
                Object obj[] = dbfReader.nextRecord(Charset.forName("euc-kr"));
                for(int j = 0; j < obj.length; j++) {
                    Log.i("jrkim", j + ")" + obj[j]);
                    switch(j) {
                        case WG_HSTR_MNNMB: mArrWGWays.get(i).HSTR_MNNMB = "" + obj[j]; break;
                        case WG_MNTN_CD_NO: break;
                        case WG_MNTN_NM:break;
                        case WG_SECTN_SEQ: mArrWGWays.get(i).SECTN_SEQ = "" + obj[j];  break;
                        case WG_SECTN_DSTNC: mArrWGWays.get(i).SECTN_DSTNC = "" + obj[j];  break;
                        case WG_SECTN_UPLN_TM: mArrWGWays.get(i).SECTN_UPLN_TM = "" + obj[j];  break;
                        case WG_SECTN_GNGDN_TM: mArrWGWays.get(i).SECTN_GNGDN_TM = "" + obj[j];  break;
                        case WG_SECTN_DGDFF_NM: mArrWGWays.get(i).SECTN_DGDFF_NM = "" + obj[j];  break;
                        case WG_LABEL: break;
                    }
                }
                i++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        if(mArrWayPoint.size() > 0) {

            double maxX = 0, maxY = 0;
            double minX = 200, minY = 200;
            double curX, curY;
            for(int i = 0; i < mArrWayPoint.size(); i++) {
                curX = mArrWayPoint.get(i).pt.getX();
                curY = mArrWayPoint.get(i).pt.getY();

                if(maxX < curX) maxX = curX;
                if(maxY < curY) maxY = curY;
                if(minX > curX) minX = curX;
                if(minY > curY) minY = curY;
            }

            double avgX = (maxX + minX) / 2;
            double avgY = (maxY + minY) / 2;

            GeoPoint avgPt = new GeoPoint(avgX, avgY);

            mapView = new MapView(MapActivity.this);
            mapView.setDaumMapApiKey("58692d6bc7249daaf10d0aca8142f896");
            ViewGroup mapViewContainer = (ViewGroup)findViewById(R.id.map_view);
            mapViewContainer.addView(mapView);
            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(avgPt.getY(), avgPt.getX());
            mapView.setMapCenterPoint(mapPoint, true);
            mapView.setZoomLevel(4, true);

            MapPOIItem marker = new MapPOIItem();
            marker.setItemName(mInfo.name);
            marker.setTag(0);
            marker.setMapPoint(mapPoint);
            marker.setMarkerType(MapPOIItem.MarkerType.YellowPin);
            marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
            mapView.addPOIItem(marker);
        }

        CommonUtils.typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        CommonUtils.setGlobalFont(getWindow().getDecorView(), CommonUtils.typeface);

        ((ToggleButton)findViewById(R.id.tbPoints)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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

                        marker.setItemName(name);
                        marker.setTag(0);
                        marker.setMapPoint(mapPoint);
                        marker.setMarkerType(MapPOIItem.MarkerType.YellowPin);
                        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
                        mapView.addPOIItem(marker);
                    }
                } else {
                    mapView.removeAllPOIItems();
                }
            }
        });

        ((ToggleButton)findViewById(R.id.tbWays)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    for(int i = 0; i < mArrWGWays.size(); i++) {
                        MapPolyline polyline = new MapPolyline();
                        polyline.setTag(i);
                        polyline.setLineColor(Color.argb(128, 255, 51, 0));

                        for(int j = 0; j < mArrWGWays.get(i).pts.size(); j++) {
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
}
