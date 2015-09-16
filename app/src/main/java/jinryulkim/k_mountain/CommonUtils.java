package jinryulkim.k_mountain;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jinryulkim.k_mountain.DB.NamedDBConst;

/**
 * Created by jinryulkim on 15. 8. 31..
 */
public class CommonUtils {

    public static Typeface typeface = null;

    public static String [] cityToGeo = {
            "제주시_33.501560_126.527425","남제주군_33.251844_126.562940","서귀포시_33.251844_126.562940",
            "대구_35.857493_128.564640","인천_37.452286_126.696323","남원_35.413310_127.388924","김제_35.801216_126.892509",
            "김천_36.125679_128.117677","부안_35.724350_126.722064","무주_35.938218_127.746506","군산_35.967517_126.714483",
            "용인_37.274935_127.108363","이천_37.281007_127.448086","포천_37.896099_127.200821","성남_37.400523_127.103356",
            "강화_37.716916_126.451754","울주_35.559419_129.129022","남양주_37.655939_127.244575","양주_37.821433_126.983821",
            "울산_35.542777_129.329527","거제_34.877225_128.631166","강릉_37.755489_128.898369","삼척_37.444504_129.169572",
            "원주_37.339969_127.946951","나주_35.020725_126.722582","광양_34.936485_127.695906","순천_34.963774_127.507904",
            "여수_34.758706_127.660264","경주_35.844634_129.211903","구미_36.109160_128.377034","김해_35.236243_128.881765",
            "진해_35.150809_128.699556","통영_34.846119_128.428163","밀양_35.495215_128.749027","해남_34.566358_126.595051",
            "화순_35.058261_126.988869","창녕_35.503879_128.501022","청도_35.658422_128.781924","청송_36.395149_129.177856",
            "시흥_37.377876_126.791404","임실_35.613059_127.282836","진안_35.903257_127.403503","논산_36.196581_127.089655",
            "서산_36.780381_126.451237","부여_36.272978_126.910213","천안_36.808505_127.147142","순창_35.480200_126.896607",
            "담양_35.370533_127.024323","목포_34.800827_126.394794","무안_34.968905_126.472136","문경_36.599837_128.203326",
            "상주_36.417591_128.160593","안성_37.009153_127.270554","충주_36.975035_127.930011","괴산_36.742788_127.900698",
            "계룡_36.331427_127.224328","공주_36.462747_127.119101","인제_38.104060_128.331196","홍천_37.762139_128.209645",
            "횡성_37.490870_127.985052","보성_34.760309_127.167376","마산_35.218083_128.592806","진주_35.182390_128.115513",
            "제천_37.137334_128.208037","평창_37.705044_128.603542","춘천_37.874830_127.738787","태백_37.168613_128.986747",
            "단양_36.983663_128.367408","익산_35.952124_126.971207","완주_35.918216_127.263198","고창_35.438517_126.636767",
            "양평_37.524785_127.570935","영월_37.183566_128.485323","속초_38.190215_128.566713","포항_36.005692_129.367025",
            "칠곡_35.999763_128.461854","안동_36.567388_128.721650","경산_35.818986_128.752260","보령_36.347594_126.600740",
            "창원_35.223920_128.683072","청주_36.633089_127.476223","아산_36.784443_127.005750","예산_36.677407_126.825311",
            "사천_35.058495_128.077495","의령_35.387868_128.269063","영동_36.150033_127.806963","정읍_35.566056_126.857748",
            "장수_35.622184_127.547426","양산_35.367191_129.039911","함양_35.554559_127.732835","합천_35.783584_128.102595",
            "강진_34.730052_126.728704","전주_35.838304_127.132537","완도_34.330909_126.708206","장흥_34.686145_126.944035",
            "진도_34.448646_126.263607","하동_35.201977_127.763301","오산_37.165828_127.059413","의왕_37.361620_126.989100",
            "하남_37.527403_127.206043","의정부_37.733500_127.055519","파주_37.822544_126.789338","군포_37.350922_126.933222",
            "안산_37.320969_126.814066","여주_37.294747_127.635304","동두천_37.909556_127.060927","남해_34.789879_127.947155",
            "거창_35.775226_127.865325","예천_36.729574_128.423663","봉화_36.944034_128.947018","구례_35.246644_127.496172",
            "부천_37.503106_126.799935","평택_37.012174_127.066914","영광_35.258873_126.491147","영덕_36.425932_129.254895",
            "영양_36.743338_129.183557","영주_36.890804_128.554412","보은_36.518104_127.771024","철원_38.219046_127.434362",
            "정선_37.373750_128.762009","진천_36.860695_127.458502","음성_36.961580_127.617571","신안_34.705054_125.979431",
            "곡성_35.205167_127.281159","영암_34.768173_126.667652","장성_35.407978_126.802477","함평_35.104691_126.538578",
            "당진_36.897222_126.680472","화천_38.142173_127.721699","화성_37.181269_126.857534","함안_35.272619_128.411789",
            "옥천_36.321576_127.645615","금산_36.106680_127.479577","고흥_34.560291_127.309204","태안_36.747645_126.277866",
            "경기도 광주_37.415333_127.232994","경상남도 고성_35.010136_128.275232","경남 고성_35.010136_128.275232","고성_38.361255_128.415316",
            "고령_35.727670_128.295053","부산_35.212430_129.057037","동해_37.508211_129.060809","양양_37.990560_128.609730",
            "양구_38.150227_127.975396","대전_36.337985_127.406653","서울_37.532436_126.985430","산청_35.360230_127.881581",
            "청양_36.444464_126.861519","서천_36.091866_126.695597","청원_36.595519_127.579925","김포_37.709980_126.579732",
            "연기_36.590093_127.257292","영천_36.045089_128.882578","의성_36.363755_128.654164","울진_36.913131_129.322072",
            "광주_35.140621_126.962780","증평_36.781640_127.605400","청평_37.740908_127.429988","울릉_37.510451_130.861977",
            "여천_34.868315_127.555709","홍성_36.594343_126.655733","군위_36.160398_128.672090","수원_37.270058_127.003706",
            "가평_37.840584_127.454527","경기_37.345029_127.024396"
    };

    public static void setGlobalFont(View view, Typeface typeface) {
        if(typeface == null)
            return;

        if(view != null) {
            if(view instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup)view;
                int vgCnt= vg.getChildCount();
                for(int i = 0; i < vgCnt; i++) {
                    View child = vg.getChildAt(i);
                    if(child instanceof TextView) {
                        //if(child instanceof EditText == false)
                            ((TextView)child).setTypeface(typeface);
                    } else {
                        setGlobalFont(child, typeface);
                    }
                }
            }
        }
    }

    public static int DP2PX(Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static boolean isPointInRect(Point pt, Rect rt) {
        Rect rect = new Rect();
        rect.set(rt);
        if((rect.left <= pt.x && pt.x <= rect.right) && (pt.y <= rect.bottom && rect.top <= pt.y)) {
            return true;
        }
        return false;
    }

    public static boolean isExistInAsset(Context context, String code) {
        boolean bRes = false;
        try {
            InputStream is = context.getAssets().open("geo/" + code + ".zip");
            if(is != null)
                bRes = true;
        } catch( IOException e) {
            e.printStackTrace();
            bRes = false;
        }
        return bRes;
    }

    public static boolean isExistInCache(Context context, String code) {
        boolean bRes = false;
        String cachePath = context.getCacheDir() + "/geo/" + code; // 최상위 dir
        String way_point = cachePath + "/WAY_POINT_" + code + "/WAY_POINT_" + code;
        File way_point_dbf = new File(way_point + ".dbf");
        File way_point_shp = new File(way_point + ".shp");
        File way_point_shx = new File(way_point + ".shx");
        String wg_mt_way = cachePath + "/WG_MT_WAY_" + code + "/WG_MT_WAY_" + code;
        File wg_mt_way_dbf = new File(wg_mt_way + ".dbf");
        File wg_mt_way_shp = new File(wg_mt_way + ".shp");
        File wg_mt_way_shx = new File(wg_mt_way + ".shx");

        // 6개 파일이 모두 존재해야 성공
        if((way_point_dbf.exists() && way_point_dbf.length() > 0) &&
                (way_point_shp.exists() && way_point_shp.length() > 0) &&
                (way_point_shx.exists() && way_point_shx.length() > 0) &&
                (wg_mt_way_dbf.exists() && wg_mt_way_dbf.length() > 0) &&
                (wg_mt_way_shp.exists() && wg_mt_way_shp.length() > 0) &&
                (wg_mt_way_shx.exists() && wg_mt_way_shx.length() > 0)) {
            bRes = true;
        }

        return bRes;
    }

    public static boolean unzipFromAsset(Context context, String zipPath, String dstDirPath) {
        boolean bRes = false;
        if(context == null ||
                zipPath == null || zipPath.length() <= 0 ||
                dstDirPath == null || dstDirPath.length() <= 0) {
            return bRes;
        }

        try {
            InputStream is = context.getAssets().open(zipPath);
            if(unzip(is, dstDirPath))
                bRes = true;
            else
                bRes = false;
        } catch(IOException e) {
            e.printStackTrace();
            bRes = false;
        }
        return bRes;
    }

    public static boolean unzip(String zipFile, String dstDirPath) {
        try {
            FileInputStream fis = new FileInputStream(zipFile);
            unzip(fis, dstDirPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private final static int BUF_SIZE = 2048;
    public static boolean unzip(InputStream is, String dstDirPath) {
        boolean bRes = false;
        if(dirCheck(dstDirPath, "") == false)
            return bRes;

        byte [] buffer = new byte[BUF_SIZE];
        try {
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry ze = null;
            boolean bWhileFail = false;
            while((ze = zis.getNextEntry()) != null) {
                String zeName = ze.getName();

                if(zeName.startsWith(".") || zeName.startsWith("_")) {
                    continue;
                }

                if(ze.isDirectory()) {
                    continue;
                } else {
                    String newFilePath = dstDirPath + "/" + ze.getName();
                    String newDirPath = newFilePath.substring(0, newFilePath.lastIndexOf("/"));

                    if(dirCheck(newDirPath, "")) {
                        File f = new File(newFilePath);
                        if (!f.exists()) {

                            FileOutputStream fos = new FileOutputStream(f);
                            int count;
                            while ((count = zis.read(buffer)) != -1) {
                                fos.write(buffer, 0, count);
                            }
                            zis.closeEntry();
                            fos.close();
                        }
                    } else {
                        bWhileFail = true;
                        break;
                    }
                }
            }
            zis.close();
            if(bWhileFail == false)
                bRes = true;
        } catch (Exception e) {
            e.printStackTrace();
            bRes = false;
        }
        return bRes;
    }

    public static boolean dirCheck(String dirPath, String dirName) {
        String newDirPath = dirPath;
        if(dirName != null && dirName.length() > 0)
            newDirPath += "/" + dirName;

        File f = new File(newDirPath);
        if(f.exists() == false) {
            if (f.mkdirs() == false) {
                return false;
            }
        } else {
            if (!f.isDirectory()) {
                return false;
            }
        }
        return true;
    }

    public static String getDBPath(Context context) {
        return context.getFilesDir().getAbsolutePath() + File.separator + NamedDBConst.DB_NAME;
    }

    public static String stringFromHtmlFormat(String htmlFormat) {
        String result = htmlFormat.replaceAll("<p>", "");
        result = result.replaceAll("<P>", "");
        result = result.replaceAll("</p>", "");
        result = result.replaceAll("</P>", "");
        result = result.replaceAll("<br>", "\n");
        result = result.replaceAll("<BR>", "\n");
        result = result.replaceAll("&nbsp;", " ");
        result = result.replaceAll("&lt;", "<");
        result = result.replaceAll("&gt;", ">");
        result = result.replaceAll("&amp;", "&");
        result = result.replaceAll("&quot;", "\"");
        result = result.replaceAll("&35;", "#");
        result = result.replaceAll("&39;", "\'");
        return result;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if(height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest in SampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width

            while((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqheight) {

        // First decode with inJustDecodeBounds = true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqheight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static void launchShare(Context context, MtInfo_General info) {

        /*String imgPath;
        File imgFile;
        if(info.imagePaths.size() > 0 &&
           (imgPath = info.makeImagePath(context, 0)) != null &&
           (imgFile = new File(imgPath)).exists()) {        // 이미지 파일이 존재 할 때
            // step 1, 대표 이미지 파일을 temp 폴더에 저장
            String fileName = imgPath.substring(imgPath.lastIndexOf("/"));
            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            /*String fileName = new File(downloadDir, info.imagePaths.get(0))
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(imageFilePath));

            context.startActivity(Intent.createChooser(intent, context.getString(R.string.INTENT_SHARE)));*/
        //} else {                               // 이미지 없음
            String text = "";
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);

            sendIntent.putExtra(Intent.EXTRA_TITLE, info.name + "(" + info.high + ")");
            text = info.name + "(" + info.high + "M)";
            if(info.sname != null)
                text += "\n" + info.sname;
            if(info.address != null)
                text += "\n" + info.address;

            text += "\n\n";

            if( info.summary != null && info.summary.length() > 0) {
                text += info.summary;
            } else if(info.detail != null && info.detail.length() > 0) {
                text += info.detail;
            }

            sendIntent.putExtra(Intent.EXTRA_TEXT, text);
            sendIntent.setType("text/plain");
            context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.INTENT_SHARE)));
     //   }
    }

    public static int getWeatherIconResId(int weatherId) {
        if(200 <= weatherId && weatherId < 300) {           // thunderstorm
            return R.drawable.w11d;
        } else if(300 <= weatherId && weatherId < 400) {    // drizzle
            return R.drawable.w09d;
        } else if(500 <= weatherId && weatherId < 600) {    // rain
            return R.drawable.w10d;
        } else if(600 <= weatherId && weatherId < 700) {    // snow
            return R.drawable.w13d;
        } else if(700 <= weatherId && weatherId < 800) {    // Atmosphere
            return R.drawable.w50d;
        } else if(800 == weatherId) {
            return R.drawable.w01d;
        } else if(801 == weatherId) {
            return R.drawable.w02d;
        } else if(802 == weatherId) {
            return R.drawable.w03d;
        } else if(803 == weatherId || 804 == weatherId) {
            return R.drawable.w04d;
        }

        return R.drawable.w02n;
    }

    public static String getWeatherText(int weatherId) {
        String res = "맑음";
        switch(weatherId) {
            case 200:   res = "약한 비/번개";       break;
            case 201:   res = "비/번개";          break;
            case 202:   res = "강한 비/번개";       break;
            case 210:   res = "약한 뇌우";           break;
            case 211:   res = "뇌우";               break;
            case 212:   res = "강한 뇌우";           break;
            case 221:   res = "가끔 뇌우";           break;
            case 230:   res = "약한 이슬비와 번개";     break;
            case 231:   res = "이슬비와 번개";        break;
            case 232:   res = "강한 이슬비와 번개";     break;
            case 300:   res = "약한 이슬비";          break;
            case 301:   res = "이슬비";                break;
            case 302:   res = "강한 이슬비";             break;
            case 310:   res = "약한 가랑비";               break;
            case 311:   res = "가랑비";                break;
            case 312:   res = "강한 가랑비";             break;
            case 313:   res = "가랑비/소나기";           break;
            case 314:   res = "가랑비/폭우";          break;
            case 321:   res = "소나기";                break;
            case 500:   res = "약한 비";           break;
            case 501:   res = "비";              break;
            case 502:   res = "조금 강한 비";          break;
            case 503:   res = "강한 비";        break;
            case 504:   res = "매우 강한 비";        break;
            case 511:   res = "우박";             break;
            case 520:   res = "약한 소나기";         break;
            case 521:   res = "소나기";            break;
            case 522:   res = "강한 소나기";         break;
            case 531:   res = "가끔 소나기";         break;
            case 600:   res = "약한 눈";           break;
            case 601:   res = "눈";              break;
            case 602:   res = "강한 눈";           break;
            case 611:   res = "진눈깨비";           break;
            case 612:   res = "강한 진눈깨비";        break;
            case 615:   res = "약한 눈/비";         break;
            case 616:   res = "눈/비";            break;
            case 620:   res = "약한 함박눈";         break;
            case 621:   res = "함박눈";            break;
            case 622:   res = "강한 함박눈";         break;
            case 701:   res = "약한 안개";             break;
            case 711:   res = "스모그";            break;
            case 721:   res = "안개";          break;
            case 731:   res = "먼지바람";           break;
            case 741:   res = "깉은 안개";          break;
            case 751:   res = "모래";             break;
            case 761:   res = "먼지";             break;
            case 762:   res = "화산재";            break;
            case 771:   res = "돌풍";             break;
            case 781:   res = "태풍";             break;
            case 800:   res = "맑음";             break;
            case 801:   res = "구름 조금";          break;
            case 802:   res = "구름";             break;
            case 803:   res = "구름 많음";       break;
            case 804:   res = "흐림";             break;
            case 900:   res = "태풍";          break;
            case 901:   res = "열대성 폭우";     break;
            case 902:   res = "태풍";          break;
            case 903:   res = "한파";          break;
            case 904:   res = "무더위";        break;
            case 905:   res = "강풍";         break;
            case 906:   res = "헤일";          break;
            case 951:   res = "고요함";         break;
            case 952:   res = "약한 바람";      break;
            case 953:   res = "산들 바람";      break;
            case 954:   res = "바람";         break;
            case 955:   res = "신선한 바람";     break;
            case 956:   res = "강한 바람";      break;
            case 957:   res = "강풍";         break;
            case 958:   res = "돌풍";         break;
            case 959:   res = "강한 돌풍";         break;
            case 960:   res = "폭풍";         break;
            case 961:   res = "강한 폭풍";      break;
            case 962:   res = "태풍";         break;
        }

        return res;
    }

    public static String getDayOfWeek(int dayOfWeek) {
        String res = "일";
        switch(dayOfWeek) {
            case 1: res = "일"; break;
            case 2: res = "월"; break;
            case 3: res = "화"; break;
            case 4: res = "수"; break;
            case 5: res = "목"; break;
            case 6: res = "금"; break;
            case 7: res = "토"; break;
        }
        return res;
    }
}
