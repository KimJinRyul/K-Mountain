package jinryulkim.k_mountain;

import android.content.Context;
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
}
