package jinryulkim.k_mountain;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import jinryulkim.k_mountain.result.ResultActivity;

/**
 * Created by jinryulkim on 15. 9. 17..
 */
public class SearchActivity extends Activity implements MtOpenAPIMgr.MtOpenAPIMgrListener, View.OnClickListener {
    private static SearchHandler mHandler = null;
    static class SearchHandler extends Handler {
        private final WeakReference<SearchActivity> mActivity;
        SearchHandler(SearchActivity activity) {
            mActivity = new WeakReference<SearchActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            SearchActivity activity = mActivity.get();
            if(activity != null)
                activity.handleMessage(msg);
        }
    };

    private final static int REQCODE_RESULT     = 10000;

    private final static int MESSAGE_SEARCH_DONE =      1002;
    private final static int MESSAGE_SEARCH_ERROR =     1003;
    public void handleMessage(Message msg) {
        switch(msg.what) {
            case MESSAGE_SEARCH_DONE:
                showSearchResult(true);
                break;
            case MESSAGE_SEARCH_ERROR:
                showSearchResult(false);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQCODE_RESULT:
                break;
        }
    }

    private void showSearchResult(boolean success) {
        if(success) {
            if (MtInfoMgr.mMtInfos.size() <= 2) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        CommonUtils.typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        CommonUtils.setGlobalFont(getWindow().getDecorView(), CommonUtils.typeface);

        mHandler = new SearchHandler(this);

        findViewById(R.id.btnSearch).setOnClickListener(this);
        findViewById(R.id.btnBack).setOnClickListener(this);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideProgress();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    private void search() {
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(findViewById(R.id.etSearch).getWindowToken(), 0);
        String mtName = ((EditText)findViewById(R.id.etSearch)).getText().toString().trim();

        if(mtName == null || mtName.length() <= 0) {
            Toast.makeText(this, R.string.TOAST_SEARCH_NOINPUT, Toast.LENGTH_SHORT).show();
        } else {
            showProgress();

            MtOpenAPIMgr.setListener(this);
            MtOpenAPIMgr.requestGeneralInfo(getApplicationContext(), mtName, null);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSearch:
                search();
                break;
            case R.id.btnBack:
                finish();
                break;
        }
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
}
