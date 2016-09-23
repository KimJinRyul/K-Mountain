package jinryulkim.k_mountain;

import android.content.Intent;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;

import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import java.io.File;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import jinryulkim.k_mountain.DB.MyDBManager;
import jinryulkim.k_mountain.DB.NamedDBConst;
import jinryulkim.k_mountain.My.MyActivity;
import jinryulkim.k_mountain.help.HelpActivity;
import jinryulkim.k_mountain.named100.Named100Activity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private final static String SHAREDPREFERENCE_NAME = "k_mountain";
    private final static String SP_DBVERSION = "db_version";

    private static MainHandler mHandler = null;

    static class MainHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;
        MainHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if(activity != null)
                activity.handleMessage(msg);
        }
    };

    private final static int MESSAGE_SPLASH =           1000;
    private final static int MESSAGE_SPLASH_DONE =      1001;
    public void handleMessage(Message msg) {
        switch(msg.what) {
            case MESSAGE_SPLASH:
                findViewById(R.id.rlSplash).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out));
                mHandler.sendEmptyMessageDelayed(MESSAGE_SPLASH_DONE, 600);
                break;
            case MESSAGE_SPLASH_DONE:
                findViewById(R.id.rlSplash).setVisibility(View.GONE);
                break;
        }
    }

    private void showSplash() {
        findViewById(R.id.rlSplash).setVisibility(View.VISIBLE);
        findViewById(R.id.rlSplash).setAlpha(1.f);
        mHandler.sendEmptyMessageDelayed(MESSAGE_SPLASH, 3200);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CommonUtils.typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        CommonUtils.setGlobalFont(getWindow().getDecorView(), CommonUtils.typeface);

        mHandler = new MainHandler(this);

        findViewById(R.id.rlUISearch).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        findViewById(R.id.rlUISearchShadow).setVisibility(View.VISIBLE);
                        return false;
                    case MotionEvent.ACTION_UP:
                        findViewById(R.id.rlUISearchShadow).setVisibility(View.GONE);
                        return false;
                }
                return false;
            }
        });

        findViewById(R.id.rlUIMy).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        findViewById(R.id.rlUIMyShadow).setVisibility(View.VISIBLE);
                        return false;
                    case MotionEvent.ACTION_UP:
                        findViewById(R.id.rlUIMyShadow).setVisibility(View.GONE);
                        return false;
                }
                return false;
            }
        });

        findViewById(R.id.rlUI100).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        findViewById(R.id.rlUI100Shadow).setVisibility(View.VISIBLE);
                        return false;
                    case MotionEvent.ACTION_UP:
                        findViewById(R.id.rlUI100Shadow).setVisibility(View.GONE);
                        return false;
                }
                return false;
            }
        });

        findViewById(R.id.rlUIFire).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        findViewById(R.id.rlUIFireShadow).setVisibility(View.VISIBLE);
                        return false;
                    case MotionEvent.ACTION_UP:
                        findViewById(R.id.rlUIFireShadow).setVisibility(View.GONE);
                        return false;
                }
                return false;
            }
        });

        findViewById(R.id.rlUISearch).setOnClickListener(this);
        findViewById(R.id.rlUIMy).setOnClickListener(this);
        findViewById(R.id.rlUI100).setOnClickListener(this);
        findViewById(R.id.rlUIFire).setOnClickListener(this);
        findViewById(R.id.btnInfo).setOnClickListener(this);

        showSplash();

        if(copyDBFromAssetIfNotExist() == false) {
            Toast.makeText(this, getString(R.string.MAIN_FAIL_INIT), Toast.LENGTH_SHORT).show();
        }

        // 100대 명산 정보가 멍청한 탓에 임시로 전부 긁어 DB 로 저장하고 자 한다.
        //MtOpenAPIMgr.createNamedMtInfoDB(this);

        // 일반 산정보 1600여개를 DB로 저장하고자 한다.
        //MtOpenAPIMgr.createGeneralMtInfoDB(this, 1);

        // 100대 명산 정보가 멍청한 탓에 임시로 전부 긁어 DB 로 저장한 것을 빼내고자 한다.
        /*File dbFile = getDatabasePath(NamedDBConst.DB_NAME);
        if(dbFile.exists()) {
            try {
                File fDir = Environment.getExternalStorageDirectory();
                String outputPath = fDir.getAbsolutePath() + "/" + NamedDBConst.DB_NAME;
                Log.i("jrkim", "outputPath:" + outputPath);
                File outputDB = new File(outputPath);
                if (!outputDB.exists())
                    outputDB.createNewFile();

                FileInputStream fis = new FileInputStream(dbFile);
                FileOutputStream fos = new FileOutputStream(outputDB);
                byte[] buffer = new byte[1024];
                int read = 0;
                while ((read = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, read);
                }
                fis.close();
                fos.close();
                Log.i("jrkim", "DB Output :" + outputPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/

        // DB test code
       /* try {
            NamedDBManager db = NamedDBManager.getInstance(this);
            db.openReadonly(CommonUtils.getDBPath(this));
            Cursor cursor = db.getAll(NamedDBConst._GEN_TABLE);

            Log.i("jrkim", "cursorSize:" + cursor.getCount());
           String code, name, address;
            while(cursor.moveToNext()) {
                //Log.i("jrkim", "-----------------");
                //code = cursor.getString(cursor.getColumnIndex(NamedDBConst.code)).trim();
                //name = cursor.getString(cursor.getColumnIndex(NamedDBConst.name)).trim();
                //Log.i("jrkim", "sname:" + cursor.getString(cursor.getColumnIndex(NamedDBConst.sname)).trim());
                //Log.i("jrkim", "high:" + cursor.getString(cursor.getColumnIndex(NamedDBConst.high)).trim());
                address = cursor.getString(cursor.getColumnIndex(NamedDBConst.address)).trim();
                //Log.i("jrkim", "admin:" + cursor.getString(cursor.getColumnIndex(NamedDBConst.admin)).trim());
                //Log.i("jrkim", "adminNum:" + cursor.getString(cursor.getColumnIndex(NamedDBConst.adminNum)).trim());
                //Log.i("jrkim", "summary:" + cursor.getString(cursor.getColumnIndex(NamedDBConst.summary)).trim());
                //Log.i("jrkim", "detail:" + cursor.getString(cursor.getColumnIndex(NamedDBConst.detail)).trim());
                //Log.i("jrkim", "imagePaths:" + cursor.getString(cursor.getColumnIndex(NamedDBConst.imagePaths)).trim());

                //Log.i("jrkim", name + "-" + code + "-" + address);
                String cityLine;
                String [] citySplit;
                boolean bMatched = false;
                for(int i = 0; i < CommonUtils.cityToGeo.length; i++) {
                    cityLine = CommonUtils.cityToGeo[i];
                    citySplit = cityLine.split("_");
                    if(citySplit.length == 3) {
                        if(address.indexOf(citySplit[0]) >= 0) {
                            Log.i("jrkim", address + " is matched to " + cityLine);
                            bMatched = true;
                            break;
                        }

                    } else {
                        Log.e("jrkim", cityLine + " doesn't split by 3");
                    }
                }

                if(bMatched == false) {
                    Log.e("jrkim", address + " is not matched....!!!!");
                }
            }
            cursor.close();

        } catch (Exception e) {
            Log.e("jrkim", "db...." + e.getMessage());
        }
        Log.i("jrkim", "DONE");
        */
    }

    private boolean copyDBFromAssetIfNotExist() {
        SharedPreferences sp = getSharedPreferences(SHAREDPREFERENCE_NAME, MODE_PRIVATE);
        int dbVersion = sp.getInt(SP_DBVERSION, -1);

        String dbPath = CommonUtils.getDBPath(getApplicationContext());
        File dbFile = new File(dbPath);
        boolean bRes = false;
        if(dbFile.exists() && dbFile.length() > 1024 && dbVersion == NamedDBConst.DB_VERSION) {
            return true;
        }

        dbFile.delete();

        try {
            InputStream is = getAssets().open("db/" + NamedDBConst.DB_NAME);
            FileOutputStream fos = new FileOutputStream(dbPath);

            byte buf[] = new byte[1024];
            int len = 0;
            while((len = is.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
            is.close();
            fos.close();
            bRes = true;

            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(SP_DBVERSION, NamedDBConst.DB_VERSION);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
            bRes = false;
        }

        return bRes;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.rlUISearch:
                startActivity(new Intent(this, SearchActivity.class));
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
            case R.id.rlUIMy:
                MyDBManager myDB = MyDBManager.getInstance(this);
                myDB.open();
                Cursor cursor = myDB.getAll();
                int myCnt = cursor.getCount();
                cursor.close();
                myDB.close();

                if(myCnt == 0) {
                    Toast.makeText(this, getString(R.string.TOAST_NO_MY), Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(this, MyActivity.class));
                    overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                }
                break;
            case R.id.rlUI100:
                startActivity(new Intent(this, Named100Activity.class));
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
            case R.id.rlUIFire:
                startActivity(new Intent(this, HelpActivity.class));
                overridePendingTransition(R.anim.zoom_enter, 0);
                break;
            case R.id.btnInfo:
                startActivity(new Intent(this, InfoActivity.class));
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
        }
    }
}
