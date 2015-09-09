package jinryulkim.k_mountain;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by jinryulkim on 15. 8. 31..
 */
public class CommonUtils {

    public static Typeface typeface = null;

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

        Log.i("jrkim", "isExistInCache");
        Log.i("jrkim", way_point_dbf.getAbsolutePath());
        Log.i("jrkim", way_point_shp.getAbsolutePath());
        Log.i("jrkim", way_point_shx.getAbsolutePath());
        Log.i("jrkim", wg_mt_way_dbf.getAbsolutePath());
        Log.i("jrkim", wg_mt_way_shp.getAbsolutePath());
        Log.i("jrkim", wg_mt_way_shx.getAbsolutePath());

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
        Log.i("jrkim", "unzipFromAsset");
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
        Log.i("jrkim", "unzip -> " + dstDirPath);
        boolean bRes = false;
        if(dirCheck(dstDirPath, "") == false)
            return bRes;

        byte [] buffer = new byte[BUF_SIZE];
        try {
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry ze = null;
            boolean bWhileFail = false;
            while((ze = zis.getNextEntry()) != null) {
                Log.i("jrkim", "unzipping..." + ze.getName());
                String zeName = ze.getName();

                if(zeName.startsWith(".") || zeName.startsWith("_")) {
                    Log.i("jrkim", zeName + " is by-pass");
                    continue;
                }

                if(ze.isDirectory()) {
                    Log.i("jrkim", "이거...디렉토리임.... by-pass");
                    /*if (dirCheck(dstDirPath, ze.getName()) == false) {
                        bWhileFail = true;
                        break;
                    }*/
                    continue;
                } else {
                    String newFilePath = dstDirPath + "/" + ze.getName();
                    String newDirPath = newFilePath.substring(0, newFilePath.lastIndexOf("/"));

                    Log.i("jrkim", "새파일 나가신다..:" + newFilePath + ", newDir : " + newDirPath);
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
                            Log.i("jrkim", "생성:" + newFilePath);
                        } else {
                            Log.i("jrkim", "이미 이 파일 있는데 ?!");
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

        Log.i("jrkim", "dirCheck:" + newDirPath);
        File f = new File(newDirPath);
        if(f.exists() == false) {
            Log.i("jrkim", newDirPath + " 이 없엉...");
            if (f.mkdirs() == false) {
                Log.e("jrkim", newDirPath + " 못 만듦 ㅠ_ㅠ");
                return false;
            } else {
                Log.i("jrkim", newDirPath + " 새로 생성");
            }
        } else {
            Log.i("jrkim", newDirPath + " 이미 존재.");
            if (!f.isDirectory()) {
                Log.e("jrkim", "근데....ㅅㅂ directory가 아님....");
                return false;
            }
        }
        return true;
    }
}
