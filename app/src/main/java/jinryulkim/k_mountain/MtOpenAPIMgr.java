package jinryulkim.k_mountain;

/**
 * Created by jinryulkim on 15. 8. 25..
 */

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import jinryulkim.k_mountain.DB.NamedDBConst;
import jinryulkim.k_mountain.DB.NamedDBManager;
import jinryulkim.k_mountain.detail.ImageViewerCard;
import jinryulkim.k_mountain.result.MainUICard;

/**
 *   일반 적인 산에 대한 정보 (국립공원 제외)
 *   URL : http://www.forest.go.kr/newkfsweb/kfi/kfs/openapi/mntInfoOpenAPI.do
 *   KEY : b974b63d781d474db5eec5a8725fcf72
 *   Parameter
 *      key : String
 *      pageUnit : Int      페이지당 결과값 개수 (기본값 : 10)
 *      pageIndex : Int     페이지 번호 (기본값 : 1)
 *      searchWrd : String  산이름, UTF-8, URLEncoding
 *   Result
 *      key : String
 *      totalCnt : Int          전체 개수
 *      pageUnit : Int          페이지당 결과 값
 *      pageIndex : Int         페이지 번호
 *      searchWrd : String      검색 값
 *      mntiListNo : String     산 코드
 *      mntiTop : String        100대 명산 구분
 *      mntiName : String       산 이름
 *      mntiSname : String      산 정보 부제
 *      mntiAdd : String        소재지
 *      mntiHigh : String       높이
 *      mntiAdmin : String      관리주체
 *      mntiAdminNum : String   관리자 전화번호
 *      mntiSummary : String    산정보 개관
 *      mntiDetails : String    산정보 상세
 *      fileNo : String         파일 순번
 *      fileName : String       파일 명
 *      filePath : String       파일 경로
 *
 *   100대 명산 등산로 정보
 *   URL : http://www.forest.go.kr/newkfsweb/kfi/kfs/openapi/gdTrailInfoOpenAPI.do
 *   KEY : 0dc67ebd4d004f14a31032786e4a5289
 *   Parameter
 *       key : String
 *       pageUnit : Int
 *       pageIndex : Int
 *       searchArNm : String 지역명, UTF-8, URLEncoding
 *       searchNtNm : String 산이름, UTF-8, URLEncoding
 *   Result
 *       key : String
 *       totalCnt : Int
 *       pageUnit : Int
 *       pageIndex : Int
 *       searchArNm : String
 *       searchMtNm : String
 *       mntNm : String      산이름
 *       mntnCd : String     산코드
 *       areaNm : String     소재지 (지역)
 *       mntHeight : String  높이
 *       areaReason : String 특징 / 선정이유
 *       overView : String   개관
 *       details : String    상세
 *       tpNum : String      산행 PLUS 번호
 *       tpTitl : String     산행 PLUS 이름
 *       tpContent : String  산행 PLUS 내용
 *       transport : String  교통정보
 *       tourismInf : String 주변 관광 정보
 *       etcCourse : String  기타 코스
 *       flashUrl : String   기타 코드 (Flash File URL)
 *       videoUrl : String   동영상 (Media File URL)
 *
 *  등산로 정보 (SHP 파일)
 *      파일이름 형식 : WAY_POINT_산명_산코드.shp(shp,shx,dbf), WG_MT_WAY_산명_산코드.shp(shp,shx,dbf)
 *      파일 타입 : Point, Polyline
 *      좌표계 : GRS80 타원체의 TM 중부(20, 60)
 *      속성정보 :
 *              WAY_PONINT_산명_산코드.shp
 *          컬럼              설명              타입        길이
 *          SHAPEID          도형 ID           NUMBER      8
 *          MT_STD_IDX      산코드_번호          VARCHAR     10
 *          SYM_NUM         웨이포인트_번호        VARCHAR     14
 *          SYM_TYP         신벌_유형코드         VARCHAR     2
 *          SYM_NAM         심벌_주요명칭         VARCHAR     50
 *          SYM_TYP2        심벌_고유기호         VARCHAR     50
 *          SYM_NAM2        심벌_고유이름         VARCHAR     100
 *          PHOTO           사진파일명           VARCHAR     50
 *          SYM_TXT         비고               VARCHAR      200
 *          CROSS_P         경로검색_표출명칭       VARCHAR     10
 *          MNTN_NM         산이름             VARCHAR       50
 *          LABEL           산명_산코드          VARCHAR      50
 *              WG_MT_WAY_산명_산코드.shp
 *          OBJECTID        도형ID             NUMBER         9
 *          HSTR_MNNMB      이력관리번호         NUMBER         18
 *          MNTN_CD_NO      산코드번호           VARCHAR2        10
 *          MNTN_NM         산이름             VARCHAR2         50
 *          SECTN_SEQ       구간순번            VARCHAR2        10
 *          SECTN_DSTNC     구간거리            NUMBER          13
 *          SECTN_UPLN_TM   구간상행시간          NUMBER          10
 *          SECTN_GNGDN_TM  구간하행시간          NUMBER          10
 *          SECTN_DGDFF_NM  구간난이도명          VARCHAR2        8
 *          LABEL           산명_산코드          VARCHAR2        50
 *
 *   SHP
 *      Esri사의 파일 포멧, 공간정보의 공통파일 형식으로 사용되고 있어, Arcgis등 공간정보용 시스템의 보편적 파일
 *      .shp : 공간정보에 대한 위치 정보
 *      .dbf : 컬럼 및 속성 정보 공간 정보에 대한 위치 정보
 *      .shx : shp와 dbf를 연계
 *
 */

public class MtOpenAPIMgr {

    public interface MtOpenAPIMgrListener {
        void onRequestGeneralMtInfoStarted();
        void onRequestGeneralMtInfoCompleted();
        void onRequestGeneralMtInfoError();
    }

    private static MtOpenAPIMgrListener mListener = null;

    // 공통
    private final static String KEY = "key=";
    private final static String PAGEUNIT = "pageUnit=";
    private final static String PAGEINDEX = "pageIndex=";

    // 산정보
    private final static String GENERAL_URL = "http://www.forest.go.kr/newkfsweb/kfi/kfs/openapi/mntInfoOpenAPI.do";
    private final static String GENERAL_KEY = "b974b63d781d474db5eec5a8725fcf72";
    private final static String SEARCHWRD = "searchWrd=";

    // 100대 명산 정보
    private final static String NAMED_URL = "http://www.forest.go.kr/newkfsweb/kfi/kfs/openapi/gdTrailInfoOpenAPI.do";
    private final static String NAMED_KEY = "0dc67ebd4d004f14a31032786e4a5289";
    private final static String AREANAME = "searchArNm=";
    private final static String MTNAME = "searchNtNm=";

    public static void setListener(MtOpenAPIMgrListener listener) {
        mListener = listener;
    }


    /**
     *
     * @param pageUnit          페이지당 정보 수       (기본 10)
     * @param pageIndex         현재 페이지          (기본 1)
     * @param mountainName      산 이름
     */
    public static boolean requestGeneralInfo(final Context context, final int pageUnit, final int pageIndex, final String mountainName, final boolean clear) {

        MtInfoMgr.totalCnt = 0;
        MtInfoMgr.pageUnit = pageUnit;
        MtInfoMgr.pageIndex = pageIndex;
        MtInfoMgr.searchWrd = mountainName;

        if(clear) {
            MtInfoMgr.mMtInfos.clear();
            MtInfoMgr.deletedCnt = 0;
            MtInfoMgr.mMtInfos.add(new MtInfo_General());       // MainUICard
        }

        if(mountainName == null || mountainName.length() <= 0) {
            if(mListener != null)
                mListener.onRequestGeneralMtInfoError();
            return false;
        }

        new Thread() {
            @Override
            public void run() {

                if(mListener != null)
                    mListener.onRequestGeneralMtInfoStarted();

                try {
                    NamedDBManager db = NamedDBManager.getInstance(context);
                    db.openReadonly();

                    MainUICard.loadImages(context);
                    ImageViewerCard.loadImages(context);

                    String addr = GENERAL_URL + "?" + KEY + GENERAL_KEY +
                            "&" + PAGEUNIT + pageUnit +
                            "&" + PAGEINDEX + pageIndex +
                            (mountainName.length() > 0 ? "&" + SEARCHWRD + URLEncoder.encode(mountainName, "UTF-8") : "");

                    URL url = new URL(addr);
                    BufferedInputStream bis = new BufferedInputStream(url.openStream());
                    XmlPullParserFactory xmlFactory = XmlPullParserFactory.newInstance();
                    xmlFactory.setNamespaceAware(true);
                    XmlPullParser xmlParser = xmlFactory.newPullParser();
                    xmlParser.setInput(bis, "utf-8");

                    String tag = null;
                    String txt = null;
                    int event_type = xmlParser.getEventType();
                    MtInfo_General mtInfo = null;
                    while(event_type != XmlPullParser.END_DOCUMENT) {
                        if(event_type == XmlPullParser.START_TAG) {
                            tag = xmlParser.getName();
                            if("mntInfo".equals(tag)) {
                                mtInfo = new MtInfo_General();
                                mtInfo.imagePaths = new ArrayList<String>();
                            }
                        } else if(event_type == XmlPullParser.TEXT) {
                            txt = xmlParser.getText().trim();
                            if(tag != null) {
                                if ("totalCnt".equals(tag)) {
                                    MtInfoMgr.totalCnt = Integer.parseInt(txt);
                                } else if ("pageUnit".equals(tag)) {
                                    MtInfoMgr.pageUnit = Integer.parseInt(txt);
                                } else if ("pageIndex".equals(tag)) {
                                    MtInfoMgr.pageIndex = Integer.parseInt(txt);
                                }else if(mtInfo != null) {
                                    if ("mntiListNo".equals(tag)) {
                                        mtInfo.code = txt;
                                    } else if ("mntiName".equals(tag)) {
                                        mtInfo.name = txt;
                                    } else if ("mntiSname".equals(tag)) {
                                        mtInfo.sname = txt;
                                    } else if ("mntiAdd".equals(tag)) {
                                        mtInfo.address = txt;
                                    } else if ("mntiHigh".equals(tag)) {
                                        mtInfo.high = txt;
                                    } else if ("mntiAdmin".equals(tag)) {
                                        mtInfo.admin = txt;
                                    } else if ("mntiAdminNum".equals(tag)) {
                                        mtInfo.adminNum = txt;
                                    } else if ("mntiSummary".equals(tag)) {
                                        mtInfo.summary = txt;
                                    } else if ("mntiDetails".equals(tag)) {
                                        mtInfo.detail = txt;
                                    } else if ("filePath".equals(tag)) {
                                        mtInfo.imagePaths.add(txt);
                                    }
                                }
                            }
                        } else if(event_type == XmlPullParser.END_TAG) {
                            tag = xmlParser.getName();
                            if("mntInfo".equals(tag) && mtInfo != null) {
                                // 명산인지 확인해보자.
                                // mtInfo.namedInfo = requestNamedInfo(mtInfo.code, mtInfo.name);
                                Cursor cursor = db.get(NamedDBConst.mntnCd, new String[] {mtInfo.code});
                                if(cursor.getCount() == 0) {
                                    mtInfo.namedInfo = null;
                                } else {
                                    cursor.moveToFirst();
                                    MtInfo_Named named = new MtInfo_Named();
                                    named.tpTitle = new ArrayList<String>();
                                    named.tpContent = new ArrayList<String>();

                                    named.name = CommonUtils.stringFromHtmlFormat(cursor.getString(cursor.getColumnIndex(NamedDBConst.mntNm)).trim());
                                    named.sname = CommonUtils.stringFromHtmlFormat(cursor.getString(cursor.getColumnIndex(NamedDBConst.subNm)).trim());
                                    named.code = CommonUtils.stringFromHtmlFormat(cursor.getString(cursor.getColumnIndex(NamedDBConst.mntnCd)).trim());
                                    named.area = CommonUtils.stringFromHtmlFormat(cursor.getString(cursor.getColumnIndex(NamedDBConst.areaNm)).trim());
                                    named.height = CommonUtils.stringFromHtmlFormat(cursor.getString(cursor.getColumnIndex(NamedDBConst.mntHeight)).trim());
                                    named.reason = CommonUtils.stringFromHtmlFormat(cursor.getString(cursor.getColumnIndex(NamedDBConst.areaReason)).trim());
                                    named.overview = CommonUtils.stringFromHtmlFormat(cursor.getString(cursor.getColumnIndex(NamedDBConst.overView)).trim());
                                    named.details = CommonUtils.stringFromHtmlFormat(cursor.getString(cursor.getColumnIndex(NamedDBConst.details)).trim());
                                    String tpTitles = CommonUtils.stringFromHtmlFormat(cursor.getString(cursor.getColumnIndex(NamedDBConst.tpTitl)).trim());
                                    String tpContents = CommonUtils.stringFromHtmlFormat(cursor.getString(cursor.getColumnIndex(NamedDBConst.tpContent)).trim());

                                    if(tpTitles.length() > 0 && tpTitles.indexOf("|") > 0) {
                                        String [] tmp = tpTitles.split("|");
                                        for(int k = 0; k < tmp.length; k++)
                                            named.tpTitle.add(tmp[k]);
                                    } else
                                        named.tpTitle.add(tpTitles);

                                    if(tpContents.length() > 0 && tpContents.indexOf("|") > 0) {
                                        String [] tmp = tpContents.split("|");
                                        for(int k = 0; k < tmp.length; k++)
                                            named.tpContent.add(tmp[k]);
                                    } else
                                        named.tpContent.add(tpContents);

                                    named.transport = CommonUtils.stringFromHtmlFormat(cursor.getString(cursor.getColumnIndex(NamedDBConst.transport)).trim());
                                    named.tourismInfo = CommonUtils.stringFromHtmlFormat(cursor.getString(cursor.getColumnIndex(NamedDBConst.tourismInf)).trim());
                                    named.etcCource = CommonUtils.stringFromHtmlFormat(cursor.getString(cursor.getColumnIndex(NamedDBConst.etcCourse)).trim());
                                    named.flahsUrl = CommonUtils.stringFromHtmlFormat(cursor.getString(cursor.getColumnIndex(NamedDBConst.flashUrl)).trim());
                                    named.videoUrl = CommonUtils.stringFromHtmlFormat(cursor.getString(cursor.getColumnIndex(NamedDBConst.videoUrl)).trim());

                                    mtInfo.namedInfo = named;
                                }
                                cursor.close();

                                mtInfo.checkDownloaded(context);
                                MtInfoMgr.mMtInfos.add(mtInfo);
                                mtInfo = null;
                            }
                            tag = null;
                        }
                        event_type = xmlParser.next();
                    }

                    bis.close();

                    // 기존의 Empty가 있으면 삭제
                    for(int i = 1; i < MtInfoMgr.mMtInfos.size(); i++) {
                        if(MtInfoMgr.mMtInfos.get(i).name == null) {
                            MtInfoMgr.mMtInfos.remove(i);
                            break;
                        }
                    }
                    // 새로운 Empty 추가
                    MtInfoMgr.mMtInfos.add(new MtInfo_General());
                    db.close();

                    if(mListener != null)
                        mListener.onRequestGeneralMtInfoCompleted();

                } catch (Exception e) {
                    e.printStackTrace();
                    if(mListener != null)
                        mListener.onRequestGeneralMtInfoError();
                }
            }
        }.start();
        return true;
    }
    public static boolean requestGeneralInfo(Context context, String mountainName) {
        return requestGeneralInfo(context, 10, 1, mountainName, true);
    }

    public static void createNamedMtInfoDB(final Context context) {
        File fp = context.getDatabasePath(NamedDBConst.DB_NAME);
        if(fp.exists())
            fp.delete();

        new Thread() {
            @Override public void run() {
                try {
                    NamedDBManager db = NamedDBManager.getInstance(context);
                    db.openWritable();
                    MtInfo_Named mtInfo = null;

                    for(int r = 1; r <= 10; r++) {
                        String addr = NAMED_URL + "?" + KEY + NAMED_KEY +
                                "&" + PAGEUNIT + 10 +
                                "&" + PAGEINDEX + r +
                        //"&" + AREANAME + URLEncoder.encode(mtName, "UTF-8");
                                "&" + MTNAME + URLEncoder.encode("", "UTF-8");

                        Log.i("jrkim", "NAMED:" + addr);

                        URL url = new URL(addr);
                        BufferedInputStream bis = new BufferedInputStream(url.openStream());
                        XmlPullParserFactory xmlFactory = XmlPullParserFactory.newInstance();
                        xmlFactory.setNamespaceAware(true);
                        XmlPullParser xmlParser = xmlFactory.newPullParser();
                        xmlParser.setInput(bis, "utf-8");

                        String tag = null;
                        String txt = null;
                        int event_type = xmlParser.getEventType();
                        while (event_type != XmlPullParser.END_DOCUMENT) {
                            if (event_type == XmlPullParser.START_TAG) {
                                tag = xmlParser.getName();
                                //Log.i("jrkim", "start_tag:" + tag);
                                if ("gdTrailInfo".equals(tag)) {
                                    mtInfo = new MtInfo_Named();
                                    mtInfo.tpTitle = new ArrayList<String>();
                                    mtInfo.tpContent = new ArrayList<String>();
                                }
                            } else if (event_type == XmlPullParser.TEXT && tag != null /*&& mtInfo != null*/) {
                                txt = xmlParser.getText();
                                //Log.i("jrkim", "tag:" + tag + ", text:" + txt);
                                if ("totalCnt".equals(tag)) {
                                    int totalCtn = Integer.parseInt(txt);
                                    Log.i("jrkim", "total Cnt : " + totalCtn);
                                } else if ("mntnCd".equals(tag)) {
                                    mtInfo.code = txt;
                                    Log.i("jrkim", "mntnCd:" + txt);
                                } else if ("mntNm".equals(tag)) {
                                    mtInfo.name = txt;
                                    Log.i("jrkim", "mntNm:" + txt);
                                } else if ("subNm".equals(tag)) {
                                    mtInfo.sname = txt;
                                    Log.i("jrkim", "subNm:" + txt);
                                } else if ("areaNm".equals(tag)) {
                                    mtInfo.area = txt;
                                    Log.i("jrkim", "areaNm:" + txt);
                                } else if ("aeatReason".equals(tag)) {
                                    mtInfo.reason = txt;
                                    Log.i("jrkim", "areaReason:" + txt);
                                } else if ("overView".equals(tag)) {
                                    mtInfo.overview = txt;
                                    Log.i("jrkim", "overview:" + txt);
                                } else if ("details".equals(tag)) {
                                    mtInfo.details = txt;
                                    Log.i("jrkim", "details:" + txt);
                                } else if ("tpTitl".equals(tag)) {
                                    mtInfo.tpTitle.add(txt);
                                } else if ("tpContent".equals(tag)) {
                                    mtInfo.tpContent.add(txt);
                                } else if ("transport".equals(tag)) {
                                    mtInfo.transport = txt;
                                    Log.i("jrkim", "transport:" + txt);
                                } else if ("tourismInf".equals(tag)) {
                                    mtInfo.tourismInfo = txt;
                                    Log.i("jrkim", "tourismInfo:" + txt);
                                } else if ("etcCourse".equals(tag)) {
                                    mtInfo.etcCource = txt;
                                    Log.i("jrkim", "etcCource:" + txt);
                                } else if ("videoUrl".equals(tag)) {
                                    mtInfo.videoUrl = txt;
                                    Log.i("jrkim", "videourl:" + txt);
                                } else if("flashUrl".equals(tag)) {
                                    mtInfo.flahsUrl = txt;
                                    Log.i("jrkim", "flashUrl:" + txt);
                                } else if("mntHeight".equals(tag)) {
                                    mtInfo.height = txt;
                                    Log.i("jrkim", "mntHeight:" + txt);
                                }
                            } else if (event_type == XmlPullParser.END_TAG) {
                                tag = xmlParser.getName();
                                if ("gdTrailInfo".equals(tag) && mtInfo != null) {
                                    // DB에 입력
                                    String tpTitles = "", tpContent = "";
                                    for(int i = 0; i < mtInfo.tpTitle.size(); i++) {
                                        tpTitles += mtInfo.tpTitle.get(i) + "|";
                                    }
                                    if(tpTitles.length() > 0)
                                        tpTitles = tpTitles.substring(0, tpTitles.length() - 1);

                                    for(int i = 0; i < mtInfo.tpContent.size(); i++) {
                                        tpContent += mtInfo.tpContent.get(i) + "|";
                                    }
                                    if(tpContent.length() > 0)
                                        tpContent = tpContent.substring(0, tpContent.length() - 1);


                                    Log.i("jrkim", "tpTitle:" + tpTitles);
                                    Log.i("jrkim", "tpContent:" + tpContent);

                                    db.insertDB(mtInfo.name, mtInfo.sname, mtInfo.code, mtInfo.area, mtInfo.height,
                                            mtInfo.reason, mtInfo.overview, mtInfo.details, tpTitles, tpContent,
                                            mtInfo.transport, mtInfo.tourismInfo, mtInfo.etcCource, mtInfo.flahsUrl, mtInfo.videoUrl);
                                    mtInfo = null;
                                }
                                tag = null;
                            }
                            event_type = xmlParser.next();
                        }
                        bis.close();
                    }

                    db.close();

                } catch (Exception e) {
                  e.printStackTrace();
                }

                Log.i("jrkim", "DONE!!!!!!!!");
            }
        }.start();
    }
/*
            mntNm : String      산이름
    *       areaNm : String     소재지 (지역)
    *       mntHeight : String  높이
    *       areaReason : String 특징 / 선정이유
    *       overView : String   개관
    *       details : String    상세
    *       tpNum : String      산행 PLUS 번호
    *       tpTitl : String     산행 PLUS 이름
    *       tpContent : String  산행 PLUS 내용
    *       transport : String  교통정보
    *       tourismInf : String 주변 관광 정보
    *       etcCourse : String  기타 코스
    *       flashUrl : String   기타 코드 (Flash File URL)
    *       videoUrl : String   동영상 (Media File URL)
        */
    private static MtInfo_Named requestNamedInfo(String code, String mtName) {

        Log.i("jrkim", "requestNamedInfo-" + code + "-" + mtName);

        MtInfo_Named mtInfo = null;

        try {
            /**
             !!! MTNAME이 아니고 AREANAME으로 검색해야 올바른 값이 나옴..... ㅂㅅ
             추후에 산림청 Open API 에서 만약 문제를 발견하고 수정한다면 동작하지 않을 가능성 있음
             */
            String addr = NAMED_URL + "?" + KEY + NAMED_KEY +
                    "&" + PAGEUNIT + 10 +
                    "&" + PAGEINDEX + 1 +
                    //"&" + AREANAME + URLEncoder.encode(mtName, "UTF-8");
                    "&" + MTNAME + URLEncoder.encode(mtName, "UTF-8");

            Log.i("jrkim", "NAMED:" + addr);

            URL url = new URL(addr);
            BufferedInputStream bis = new BufferedInputStream(url.openStream());
            XmlPullParserFactory xmlFactory = XmlPullParserFactory.newInstance();
            xmlFactory.setNamespaceAware(true);
            XmlPullParser xmlParser = xmlFactory.newPullParser();
            xmlParser.setInput(bis, "utf-8");

            String tag = null;
            String txt = null;
            int event_type = xmlParser.getEventType();
            while (event_type != XmlPullParser.END_DOCUMENT) {
                if (event_type == XmlPullParser.START_TAG) {
                    tag = xmlParser.getName();
                    Log.i("jrkim", "start_tag:" + tag);
                    if ("gdTrailInfo".equals(tag)) {
                        mtInfo = new MtInfo_Named();
                        mtInfo.tpTitle = new ArrayList<String>();
                        mtInfo.tpContent = new ArrayList<String>();
                    }
                } else if (event_type == XmlPullParser.TEXT && tag != null /*&& mtInfo != null*/) {
                    txt = xmlParser.getText();
                    Log.i("jrkim", "tag:" + tag + ", text:" + txt);
                    /*if ("totalCnt".equals(tag)) {
                        MtInfoMgr.totalCnt = Integer.parseInt(txt);
                        if(Integer.parseInt(txt) > 0) {
                            mtInfo = new MtInfo_Named();
                            break;
                        }
                    }*/
                    if ("mntnCd".equals(tag)) {
                        mtInfo.code = txt;
                    } else if ("mntNm".equals(tag)) {
                        mtInfo.name = txt;
                    } else if ("subNm".equals(tag)) {
                        mtInfo.sname = txt;
                    } else if ("areaNm".equals(tag)) {
                        mtInfo.area = txt;
                    } else if ("aeatReason".equals(tag)) {
                        mtInfo.reason = txt;
                    } else if ("overView".equals(tag)) {
                        mtInfo.overview = txt;
                    } else if ("details".equals(tag)) {
                        mtInfo.details = txt;
                    } else if ("tpTitl".equals(tag)) {
                        mtInfo.tpTitle.add(txt);
                    } else if ("tpContent".equals(tag)) {
                        mtInfo.tpContent.add(txt);
                    } else if ("transport".equals(tag)) {
                        mtInfo.transport = txt;
                    } else if ("tourismInf".equals(tag)) {
                        mtInfo.tourismInfo = txt;
                    } else if ("etcCourse".equals(tag)) {
                        mtInfo.etcCource = txt;
                    } else if ("videoUrl".equals(tag)) {
                        mtInfo.videoUrl = txt;
                    }
                } else if (event_type == XmlPullParser.END_TAG) {
                    tag = xmlParser.getName();
                    if ("gdTrailInfo".equals(tag) && mtInfo != null) {
                        Log.i("jrkim", "요놈이 맞는가? " + code + "vs" + mtInfo.code);
                        if(code.equals(mtInfo.code)) {
                            Log.i("jrkim", "찾았당!!");
                            return mtInfo;
                        } else {
                            mtInfo = null;
                        }
                    }
                    tag = null;
                }
                event_type = xmlParser.next();
            }
            bis.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return mtInfo;
    }
}
