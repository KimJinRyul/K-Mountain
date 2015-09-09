package jinryulkim.k_mountain.detail;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import jinryulkim.k_mountain.CommonUtils;
import jinryulkim.k_mountain.MtInfo_General;
import jinryulkim.k_mountain.R;
import jinryulkim.k_mountain.map.MapActivity;

/**
 * Created by jinryulkim on 15. 9. 2..
 */
public class InfoCard extends LinearLayout implements View.OnClickListener {

    private Context mContext = null;
    private int mPosition = 0;
    private MtInfo_General mInfo = null;

    private RelativeLayout mRlSummaryExpand = null;
    private RelativeLayout mRlDetailExpand = null;
    private LinearLayout mllAdminExpand = null;

    private ValueAnimator mSummaryExpandAnimator = null;
    private ValueAnimator mDetailExpandAnimator = null;
    private ValueAnimator mAdminExpandAnimator = null;

    private final static int MESSAGE_START_ANIMATION = 1000;
    private final static int MESSAGE_READY_TO_EXPAND = 1001;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_START_ANIMATION:
                    animate().translationY(0).alpha(1).setDuration(600).setListener(null);
                    break;
                case MESSAGE_READY_TO_EXPAND:
                    if(mInfo.summary != null && mInfo.summary.length() > 0)
                        readyToExpand_summary();
                    if(mInfo.detail != null && mInfo.detail.length() > 0)
                        readyToExpand_detail();

                    if((mInfo.admin != null && mInfo.admin.length() > 0) ||
                       (mInfo.adminNum != null && mInfo.adminNum.length() > 0))
                        readyToExpand_admin();
                    break;
            }
        }
    };

    private void readyToExpand_summary() {
        mRlSummaryExpand = (RelativeLayout)findViewById(R.id.rlSummaryExpand);
        try {
            mRlSummaryExpand.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mRlSummaryExpand.getViewTreeObserver().removeOnPreDrawListener(this);
                    View parent = (View)mRlSummaryExpand.getParent();
                    final int widthSpec = MeasureSpec.makeMeasureSpec(
                            parent.getMeasuredWidth() - parent.getPaddingLeft() - parent.getPaddingRight(),
                            MeasureSpec.AT_MOST);
                    final int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    mRlSummaryExpand.measure(widthSpec, heightSpec);
                    mSummaryExpandAnimator = ValueAnimator.ofInt(0, mRlSummaryExpand.getMeasuredHeight());
                    mSummaryExpandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            int value = (Integer) animator.getAnimatedValue();
                            ViewGroup.LayoutParams lp = mRlSummaryExpand.getLayoutParams();
                            lp.height = value;
                            mRlSummaryExpand.setLayoutParams(lp);
                        }
                    });
                    ViewGroup.LayoutParams lp = mRlSummaryExpand.getLayoutParams();
                    lp.height = 0;
                    mRlSummaryExpand.setLayoutParams(lp);
                    mRlSummaryExpand.setVisibility(VISIBLE);
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readyToExpand_detail() {
        mRlDetailExpand = (RelativeLayout)findViewById(R.id.rlDetailExpand);
        try {
            mRlDetailExpand.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mRlDetailExpand.getViewTreeObserver().removeOnPreDrawListener(this);
                    View parent = (View)mRlDetailExpand.getParent();
                    final int widthSpec = MeasureSpec.makeMeasureSpec(
                            parent.getMeasuredWidth() - parent.getPaddingLeft() - parent.getPaddingRight(),
                            MeasureSpec.AT_MOST);
                    final int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    mRlDetailExpand.measure(widthSpec, heightSpec);
                    mDetailExpandAnimator = ValueAnimator.ofInt(0, mRlDetailExpand.getMeasuredHeight());
                    mDetailExpandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            int value = (Integer) animator.getAnimatedValue();
                            ViewGroup.LayoutParams lp = mRlDetailExpand.getLayoutParams();
                            lp.height = value;
                            mRlDetailExpand.setLayoutParams(lp);
                        }
                    });
                    ViewGroup.LayoutParams lp = mRlDetailExpand.getLayoutParams();
                    lp.height = 0;
                    mRlDetailExpand.setLayoutParams(lp);
                    mRlDetailExpand.setVisibility(VISIBLE);
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readyToExpand_admin() {
        mllAdminExpand = (LinearLayout)findViewById(R.id.llAdminExpand);
        try {
            mllAdminExpand.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mllAdminExpand.getViewTreeObserver().removeOnPreDrawListener(this);
                    View parent = (View)mllAdminExpand.getParent();
                    final int widthSpec = MeasureSpec.makeMeasureSpec(
                            parent.getMeasuredWidth() - parent.getPaddingLeft() - parent.getPaddingRight(),
                            MeasureSpec.AT_MOST);
                    final int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    mllAdminExpand.measure(widthSpec, heightSpec);
                    mAdminExpandAnimator = ValueAnimator.ofInt(0, mllAdminExpand.getMeasuredHeight());
                    mAdminExpandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            int value = (Integer) animator.getAnimatedValue();
                            ViewGroup.LayoutParams lp = mllAdminExpand.getLayoutParams();
                            lp.height = value;
                            mllAdminExpand.setLayoutParams(lp);
                        }
                    });
                    ViewGroup.LayoutParams lp = mllAdminExpand.getLayoutParams();
                    lp.height = 0;
                    mllAdminExpand.setLayoutParams(lp);
                    mllAdminExpand.setVisibility(VISIBLE);
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public InfoCard(Context context) {
        super(context);
        mContext = context;
    }

    public void startAnimation() {
        setTranslationY(CommonUtils.DP2PX(mContext, 100.f));
        setAlpha(0);
        mHandler.sendEmptyMessage(MESSAGE_START_ANIMATION);
    }

    private void init(MtInfo_General info) {
        mInfo = info;
        removeAllViewsInLayout();
        setOnTouchListener(null);

        LayoutInflater.from(mContext).inflate(R.layout.item_detail, this);

        CommonUtils.setGlobalFont(this, CommonUtils.typeface);
        ((TextView)findViewById(R.id.tvName)).setText(mInfo.name);
        if(mInfo.sname != null && mInfo.sname.length() > 0)     ((TextView)findViewById(R.id.tvSName)).setText(mInfo.sname);
        if(info.high != null && info.high.length() > 0) {
            findViewById(R.id.tvHeight).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.tvHeight)).setText(String.format(mContext.getString(R.string.RESULT_HEIGHT), info.high));
        }

        if(info.address != null && info.address.length() > 0) {
            findViewById(R.id.tvAddress).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.tvAddress)).setText(info.address);
        }

        if(mInfo.summary != null && mInfo.summary.length() > 0) {
            findViewById(R.id.rlSummary).setVisibility(VISIBLE);
            ((TextView)findViewById(R.id.tvSummary)).setText(mInfo.summary);
            findViewById(R.id.rlSummary).setOnClickListener(this);
        }

        if(mInfo.detail != null && mInfo.detail.length() > 0) {
            findViewById(R.id.rlDetail).setVisibility(VISIBLE);
            ((TextView)findViewById(R.id.tvDetail)).setText(mInfo.detail);
            findViewById(R.id.rlDetail).setOnClickListener(this);
        }

        if((mInfo.admin != null && mInfo.admin.length() > 0) ||
           (mInfo.adminNum != null && mInfo.adminNum.length() > 0)) {

            findViewById(R.id.rlAdmin).setVisibility(VISIBLE);
            if(mInfo.admin != null && mInfo.admin.length() > 0) {
                ((TextView) findViewById(R.id.tvAdmin)).setText(mInfo.admin);
            }

            if(mInfo.adminNum != null && mInfo.adminNum.length() > 0) {
                ((TextView) findViewById(R.id.tvAdminNum)).setText(mInfo.adminNum);
            }
            findViewById(R.id.rlAdmin).setOnClickListener(this);
        }

        if(info.code != null && info.code.length() > 0) {
            Log.i("jrkim", "code 정보 확인 : " + info.code);
            if(CommonUtils.isExistInAsset(mContext, info.code)) {  // 지도 정보는 있음
                if(!CommonUtils.isExistInCache(mContext, info.code)) {    // cache에 없음. 압축 해제 필요
                    String assetZipPath = "geo/" + info.code + ".zip";
                    String dstDirPath = mContext.getCacheDir() + "/geo/" + info.code;

                    Log.i("jrkim", "asset:" + assetZipPath + ", cache:" + dstDirPath);
                    if(CommonUtils.unzipFromAsset(mContext, assetZipPath, dstDirPath)) {
                        // 압축해제 성공
                        Log.i("jrkim", "압축해제 성공");
                        findViewById(R.id.rlMap).setVisibility(View.VISIBLE);
                        findViewById(R.id.rlMap).setOnClickListener(this);
                    } else {
                        // 압축해제 실패...??!?!?!?!?!
                        Log.i("jrkim", "압축해제 실패?!?!?");
                        findViewById(R.id.rlMap).setVisibility(View.GONE);
                    }
                } else {    // 이미 압축 해제 되어 cache에 존재함.
                    // Do Nothing...
                    Log.i("jrkim", "이미 풀어 놨넹....");
                    findViewById(R.id.rlMap).setVisibility(View.VISIBLE);
                    findViewById(R.id.rlMap).setOnClickListener(this);
                }
            } else {        // 지도정보는 없음
                Log.i("jrkim", "없넹....");
                findViewById(R.id.rlMap).setVisibility(View.GONE);
            }
        } else {
            Log.e("jrkim", info.name + " 에 코드 정보 없음.");
        }


        mHandler.sendEmptyMessage(MESSAGE_READY_TO_EXPAND);
    }

    public void setMtInfo(MtInfo_General mtInfo, int position) {
        mPosition = position;
        init(mtInfo);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlSummary:
                if(mInfo.summary_expanded == true) {
                    mSummaryExpandAnimator.end();

                    ValueAnimator va = ValueAnimator.ofInt(mRlSummaryExpand.getHeight(), 0);
                    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            int value = (Integer)animator.getAnimatedValue();
                            ViewGroup.LayoutParams lp = mRlSummaryExpand.getLayoutParams();
                            lp.height = value;
                            mRlSummaryExpand.setLayoutParams(lp);
                        }
                    });
                    va.start();
                    mInfo.summary_expanded = false;
                } else {
                    if (mSummaryExpandAnimator != null) {
                        mSummaryExpandAnimator.start();
                        mInfo.summary_expanded = true;
                    }
                }
                break;
            case R.id.rlDetail:
                if(mInfo.detail_expanded == true) {
                    mDetailExpandAnimator.end();

                    ValueAnimator va = ValueAnimator.ofInt(mRlDetailExpand.getHeight(), 0);
                    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            int value = (Integer)animator.getAnimatedValue();
                            ViewGroup.LayoutParams lp = mRlDetailExpand.getLayoutParams();
                            lp.height = value;
                            mRlDetailExpand.setLayoutParams(lp);
                        }
                    });
                    va.start();
                    mInfo.detail_expanded = false;
                } else {
                    if (mDetailExpandAnimator != null) {
                        mDetailExpandAnimator.start();
                        mInfo.detail_expanded = true;
                    }
                }
                break;
            case R.id.rlAdmin:
                if(mInfo.admin_expanded == true) {
                    mAdminExpandAnimator.end();

                    ValueAnimator va = ValueAnimator.ofInt(mllAdminExpand.getHeight(), 0);
                    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            int value = (Integer)animator.getAnimatedValue();
                            ViewGroup.LayoutParams lp = mllAdminExpand.getLayoutParams();
                            lp.height = value;
                            mllAdminExpand.setLayoutParams(lp);
                        }
                    });
                    va.start();
                    mInfo.admin_expanded = false;
                } else {
                    if(mAdminExpandAnimator != null) {
                        mAdminExpandAnimator.start();
                        mInfo.admin_expanded = true;
                    }
                }
                break;
            case R.id.rlMap:
                Intent intent = new Intent(mContext, MapActivity.class);
                intent.putExtra(MapActivity.EXTRA_INFO_POS, mPosition);
                mContext.startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean res = super.onTouchEvent(motionEvent);
        switch(motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(isViewerAreaTouch(motionEvent) == false) {
                    // TODO
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if(isViewerAreaTouch(motionEvent) == false) {
                    // TODO
                }
                return true;
            case MotionEvent.ACTION_UP:
                if(isViewerAreaTouch(motionEvent) == false) {
                    // TODO
                }
                return false;
        }
        return res;
    }

    private boolean isViewerAreaTouch(MotionEvent motionEvent) {
        ImageViewerCard ivc = DetailAdapter.getImageViewCard();
        if(ivc != null && motionEvent.getRawY() <= ivc.getCurrentHeight() + ivc.getTitleHeight()) {
            ivc.setTouchEvent(motionEvent, new Point((int) motionEvent.getRawX(), (int)motionEvent.getRawY()));
            return true;
        }
        return false;
    }
}
