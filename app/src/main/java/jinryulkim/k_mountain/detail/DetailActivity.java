package jinryulkim.k_mountain.detail;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import jinryulkim.k_mountain.CommonUtils;
import jinryulkim.k_mountain.MtInfoMgr;
import jinryulkim.k_mountain.MtInfo_General;
import jinryulkim.k_mountain.R;

/**
 * Created by jinryulkim on 15. 9. 2..
 */
public class DetailActivity extends Activity implements DetailAdapter.CardListener {

    public final static String EXTRA_POSITION   = "extra_position";

    private DetailAdapter mDetailAdapter = null;
    private PinnedHeaderListViewForDetail mPinnedHeaderListView = null;

    private static DetailHandler mHandler = null;

    static class DetailHandler extends Handler {
        private final WeakReference<DetailActivity> mActivity;
        DetailHandler(DetailActivity activity) {
            mActivity = new WeakReference<DetailActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            DetailActivity activity = mActivity.get();
            if(activity != null)
                activity.handleMessage(msg);
        }
    };

    private final static int MESSAGE_FINISH = 1000;
    public void handleMessage(Message msg) {
        switch(msg.what) {
            case MESSAGE_FINISH:
                finish();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int position = getIntent().getIntExtra(EXTRA_POSITION, -1);
        if(MtInfoMgr.mMtInfos == null || position < 0 || position >= MtInfoMgr.mMtInfos.size())
            finish();

        setContentView(R.layout.activity_detail);
        mHandler = new DetailHandler(this);

        MtInfo_General info = MtInfoMgr.mMtInfos.get(position);
        mDetailAdapter = new DetailAdapter(this, info, position, this);

        mPinnedHeaderListView = (PinnedHeaderListViewForDetail)findViewById(R.id.lvDetails);
        mPinnedHeaderListView.setSmoothScrollbarEnabled(true);
        mPinnedHeaderListView.setAdapter(mDetailAdapter);
        mPinnedHeaderListView.setVisibility(View.VISIBLE);
        mPinnedHeaderListView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mPinnedHeaderListView.getViewTreeObserver().removeOnPreDrawListener(this);
                mPinnedHeaderListView.doFix();
                mDetailAdapter.setStatusBarHeight(CommonUtils.getStatusBarHeight(getApplicationContext()));
                return false;
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
        DetailAdapter.removeImageViewCard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DetailAdapter.removeImageViewCard();
    }

    @Override
    public void onClickBack() {
        mHandler.sendEmptyMessage(MESSAGE_FINISH);
    }

    @Override
    public void onClickShare() {
        Toast.makeText(this, "SHARE", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean res = super.onTouchEvent(motionEvent);
        switch(motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(isViewerUIAreaTouch(motionEvent) == false) {
                    // TODO
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if(isViewerUIAreaTouch(motionEvent) == false) {
                    // TODO
                }
                return true;
            case MotionEvent.ACTION_UP:
                if(isViewerUIAreaTouch(motionEvent) == false) {
                    // TODO
                }
                return false;
        }
        return res;
    }

    private boolean isViewerUIAreaTouch(MotionEvent motionEvent) {
        ImageViewerCard ivc = DetailAdapter.getImageViewCard();
        if(ivc != null && motionEvent.getRawY() <= ivc.getCurrentHeight() + ivc.getTitleHeight()) {
            ivc.setTouchEvent(motionEvent, new Point((int) motionEvent.getRawX(), (int)motionEvent.getRawY()));
            return true;
        }
        return false;
    }
}