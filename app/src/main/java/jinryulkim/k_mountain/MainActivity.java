package jinryulkim.k_mountain;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import jinryulkim.k_mountain.DB.NamedDBConst;
import jinryulkim.k_mountain.result.ResultActivity;

public class MainActivity extends Activity implements MtOpenAPIMgr.MtOpenAPIMgrListener,
                                                               View.OnClickListener
{
    private final static int REQCODE_RESULT     = 10000;
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
    private final static int MESSAGE_SEARCH_DONE =      1002;
    private final static int MESSAGE_SEARCH_ERROR =     1003;
    public void handleMessage(Message msg) {
        switch(msg.what) {
            case MESSAGE_SPLASH:
                findViewById(R.id.rlSplash).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out));
                mHandler.sendEmptyMessageDelayed(MESSAGE_SPLASH_DONE, 600);
                break;
            case MESSAGE_SPLASH_DONE:
                findViewById(R.id.rlSplash).setVisibility(View.GONE);
                break;
            case MESSAGE_SEARCH_DONE:
                showSearchResult(true);
                break;
            case MESSAGE_SEARCH_ERROR:
                showSearchResult(false);
                break;
        }
    }

    private void showSearchResult(boolean success) {
        if(success) {
            if (MtInfoMgr.totalCnt <= 0) {
                hideProgress();
                Toast.makeText(this, R.string.TOAST_NO_RESULT, Toast.LENGTH_SHORT).show();
            } else {
                startActivityForResult(new Intent(this, ResultActivity.class), REQCODE_RESULT);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
            }
        } else {
            hideProgress();
            Toast.makeText(this, R.string.TOAST_SEARCH_ERROR, Toast.LENGTH_SHORT).show();
        }
    }

    private void showSplash() {
        findViewById(R.id.rlSplash).setVisibility(View.VISIBLE);
        findViewById(R.id.rlSplash).setAlpha(1.f);
        mHandler.sendEmptyMessageDelayed(MESSAGE_SPLASH, 3200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQCODE_RESULT:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CommonUtils.typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        CommonUtils.setGlobalFont(getWindow().getDecorView(), CommonUtils.typeface);

        mHandler = new MainHandler(this);

        findViewById(R.id.btnSearch).setOnClickListener(this);
        findViewById(R.id.btnInfo).setOnClickListener(this);
        ((EditText)findViewById(R.id.etSearch)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        search();
                        return true;
                    default:
                        return false;
                }
            }
        });
        showSplash();

        // 100대 명산 정보가 멍청한 탓에 임시로 전부 긁어 DB 로 저장하고 자 한다.
        //MtOpenAPIMgr.createNamedMtInfoDB(this);

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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/

        if(copyDBFromAssetIfNotExist() == false) {
            Log.e("jrkim", "DB copy failed");
        }
    }

    private boolean copyDBFromAssetIfNotExist() {
        String dbPath = CommonUtils.getDBPath(getApplicationContext());
        File dbFile = new File(dbPath);
        boolean bRes = false;
        if(dbFile.exists()) {
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
        } catch (Exception e) {
            e.printStackTrace();
            bRes = false;
        }

        return bRes;
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideProgress();
    }

    private void search() {
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(findViewById(R.id.etSearch).getWindowToken(), 0);
        String mtName = ((EditText)findViewById(R.id.etSearch)).getText().toString().trim();

        if(mtName == null || mtName.length() <= 0) {
            Toast.makeText(this, R.string.TOAST_SEARCH_NOINPUT, Toast.LENGTH_SHORT).show();
        } else {
            showProgress();

            MtOpenAPIMgr.setListener(this);
            MtOpenAPIMgr.requestGeneralInfo(getApplicationContext(), mtName);
        }
    }

    private void showProgress() {
        findViewById(R.id.ivProgress).startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate));
        findViewById(R.id.rlProgress).setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        findViewById(R.id.ivProgress).clearAnimation();
        findViewById(R.id.rlProgress).setVisibility(View.GONE);
    }

    @Override
    public void onRequestGeneralMtInfoStarted() {
    }

    @Override
    public void onRequestGeneralMtInfoCompleted() {
        mHandler.sendEmptyMessageDelayed(MESSAGE_SEARCH_DONE, 1000);
    }

    @Override
    public void onRequestGeneralMtInfoError() {
        mHandler.sendEmptyMessageDelayed(MESSAGE_SEARCH_ERROR, 1000);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnSearch:
                search();
                break;
            case R.id.btnInfo:
                startActivity(new Intent(this, InfoActivity.class));
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
        }
    }
}
