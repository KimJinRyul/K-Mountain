package jinryulkim.k_mountain.detail;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;

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

    private RelativeLayout mRlWeatherExpand = null;
    private RelativeLayout mRlReasonExpand = null;
    private RelativeLayout mRlTransportExpand = null;
    private RelativeLayout mRlTourismExpand = null;
    private RelativeLayout mRlEtccourceExpand = null;

    private ValueAnimator mWeatherExpandAnimator = null;
    private ValueAnimator mSummaryExpandAnimator = null;
    private ValueAnimator mDetailExpandAnimator = null;
    private ValueAnimator mAdminExpandAnimator = null;

    private ValueAnimator mReasonExpandAnimator = null;
    private ValueAnimator mTransportExpandAnimator = null;
    private ValueAnimator mTourismExpandAnimator = null;
    private ValueAnimator mEtccourceExpandAnimator = null;

    private int mWeatherSelection = 0;

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
                    readyToExpand_weather();
                    if(mInfo.namedInfo == null) {
                        if (mInfo.summary != null && mInfo.summary.length() > 0)                                readyToExpand_summary();
                        if (mInfo.detail != null && mInfo.detail.length() > 0)                                  readyToExpand_detail();
                        if ((mInfo.admin != null && mInfo.admin.length() > 0) ||
                            (mInfo.adminNum != null && mInfo.adminNum.length() > 0))                            readyToExpand_admin();
                    } else {
                        if (mInfo.namedInfo.reason != null && mInfo.namedInfo.reason.length() > 0)              readyToExpand_reason();
                        if (mInfo.namedInfo.overview != null && mInfo.namedInfo.overview.length() > 0)          readyToExpand_summary();
                        if (mInfo.namedInfo.details != null && mInfo.namedInfo.details.length() > 0)            readyToExpand_detail();
                        if ((mInfo.admin != null && mInfo.admin.length() > 0) ||
                                (mInfo.adminNum != null && mInfo.adminNum.length() > 0))                        readyToExpand_admin();
                        if (mInfo.namedInfo.transport != null && mInfo.namedInfo.transport.length() > 0)        readyToExpand_transport();
                        if (mInfo.namedInfo.tourismInfo != null && mInfo.namedInfo.tourismInfo.length() > 0)    readyToExpand_tourism();
                        if (mInfo.namedInfo.etcCource != null && mInfo.namedInfo.etcCource.length() > 0)        readyToExpand_etcCource();
                    }
                    break;
            }
        }
    };

    private void readyToExpand_weather() {
        mRlWeatherExpand = (RelativeLayout)findViewById(R.id.rlWeatherExpand);
        try {
            mRlWeatherExpand.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mRlWeatherExpand.getViewTreeObserver().removeOnPreDrawListener(this);
                    View parent = (View)mRlWeatherExpand.getParent();
                    final int widthSpec = MeasureSpec.makeMeasureSpec(
                            parent.getMeasuredWidth() - parent.getPaddingLeft() - parent.getPaddingRight(),
                            MeasureSpec.AT_MOST);
                    final int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    mRlWeatherExpand.measure(widthSpec, heightSpec);
                    mWeatherExpandAnimator = ValueAnimator.ofInt(0, mRlWeatherExpand.getMeasuredHeight());
                    mWeatherExpandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            int value = (Integer) animator.getAnimatedValue();
                            ViewGroup.LayoutParams lp = mRlWeatherExpand.getLayoutParams();
                            lp.height = value;
                            mRlWeatherExpand.setLayoutParams(lp);
                        }
                    });
                    ViewGroup.LayoutParams lp = mRlWeatherExpand.getLayoutParams();
                    lp.height = 0;
                    mRlWeatherExpand.setLayoutParams(lp);
                    mRlWeatherExpand.setVisibility(VISIBLE);
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    private void readyToExpand_reason() {
        mRlReasonExpand = (RelativeLayout)findViewById(R.id.rlReasonExpand);
        try {
            mRlReasonExpand.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mRlReasonExpand.getViewTreeObserver().removeOnPreDrawListener(this);
                    View parent = (View)mRlReasonExpand.getParent();
                    final int widthSpec = MeasureSpec.makeMeasureSpec(
                            parent.getMeasuredWidth() - parent.getPaddingLeft() - parent.getPaddingRight(),
                            MeasureSpec.AT_MOST);
                    final int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    mRlReasonExpand.measure(widthSpec, heightSpec);
                    mReasonExpandAnimator = ValueAnimator.ofInt(0, mRlReasonExpand.getMeasuredHeight());
                    mReasonExpandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            int value = (Integer) animator.getAnimatedValue();
                            ViewGroup.LayoutParams lp = mRlReasonExpand.getLayoutParams();
                            lp.height = value;
                            mRlReasonExpand.setLayoutParams(lp);
                        }
                    });
                    ViewGroup.LayoutParams lp = mRlReasonExpand.getLayoutParams();
                    lp.height = 0;
                    mRlReasonExpand.setLayoutParams(lp);
                    mRlReasonExpand.setVisibility(VISIBLE);
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readyToExpand_tourism() {
        mRlTourismExpand = (RelativeLayout)findViewById(R.id.rlTourismExpand);
        try {
            mRlTourismExpand.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mRlTourismExpand.getViewTreeObserver().removeOnPreDrawListener(this);
                    View parent = (View)mRlTourismExpand.getParent();
                    final int widthSpec = MeasureSpec.makeMeasureSpec(
                            parent.getMeasuredWidth() - parent.getPaddingLeft() - parent.getPaddingRight(),
                            MeasureSpec.AT_MOST);
                    final int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    mRlTourismExpand.measure(widthSpec, heightSpec);
                    mTourismExpandAnimator = ValueAnimator.ofInt(0, mRlTourismExpand.getMeasuredHeight());
                    mTourismExpandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            int value = (Integer) animator.getAnimatedValue();
                            ViewGroup.LayoutParams lp = mRlTourismExpand.getLayoutParams();
                            lp.height = value;
                            mRlTourismExpand.setLayoutParams(lp);
                        }
                    });
                    ViewGroup.LayoutParams lp = mRlTourismExpand.getLayoutParams();
                    lp.height = 0;
                    mRlTourismExpand.setLayoutParams(lp);
                    mRlTourismExpand.setVisibility(VISIBLE);
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readyToExpand_transport() {
        mRlTransportExpand = (RelativeLayout)findViewById(R.id.rlTransportExpand);
        try {
            mRlTransportExpand.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mRlTransportExpand.getViewTreeObserver().removeOnPreDrawListener(this);
                    View parent = (View)mRlTransportExpand.getParent();
                    final int widthSpec = MeasureSpec.makeMeasureSpec(
                            parent.getMeasuredWidth() - parent.getPaddingLeft() - parent.getPaddingRight(),
                            MeasureSpec.AT_MOST);
                    final int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    mRlTransportExpand.measure(widthSpec, heightSpec);
                    mTransportExpandAnimator = ValueAnimator.ofInt(0, mRlTransportExpand.getMeasuredHeight());
                    mTransportExpandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            int value = (Integer) animator.getAnimatedValue();
                            ViewGroup.LayoutParams lp = mRlTransportExpand.getLayoutParams();
                            lp.height = value;
                            mRlTransportExpand.setLayoutParams(lp);
                        }
                    });
                    ViewGroup.LayoutParams lp = mRlTransportExpand.getLayoutParams();
                    lp.height = 0;
                    mRlTransportExpand.setLayoutParams(lp);
                    mRlTransportExpand.setVisibility(VISIBLE);
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readyToExpand_etcCource() {
        mRlEtccourceExpand = (RelativeLayout)findViewById(R.id.rlEtcCourceExpand);
        try {
            mRlEtccourceExpand.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mRlEtccourceExpand.getViewTreeObserver().removeOnPreDrawListener(this);
                    View parent = (View)mRlEtccourceExpand.getParent();
                    final int widthSpec = MeasureSpec.makeMeasureSpec(
                            parent.getMeasuredWidth() - parent.getPaddingLeft() - parent.getPaddingRight(),
                            MeasureSpec.AT_MOST);
                    final int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    mRlEtccourceExpand.measure(widthSpec, heightSpec);
                    mEtccourceExpandAnimator = ValueAnimator.ofInt(0, mRlEtccourceExpand.getMeasuredHeight());
                    mEtccourceExpandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            int value = (Integer) animator.getAnimatedValue();
                            ViewGroup.LayoutParams lp = mRlEtccourceExpand.getLayoutParams();
                            lp.height = value;
                            mRlEtccourceExpand.setLayoutParams(lp);
                        }
                    });
                    ViewGroup.LayoutParams lp = mRlEtccourceExpand.getLayoutParams();
                    lp.height = 0;
                    mRlEtccourceExpand.setLayoutParams(lp);
                    mRlEtccourceExpand.setVisibility(VISIBLE);
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

    private void setDateText(int dayOfWeek, int dayOfMonth, TextView tv) {
        tv.setText(dayOfMonth + CommonUtils.getDayOfWeek(dayOfWeek));
        if(dayOfWeek == 7)  tv.setTextColor(0xff0000ff);
        else if(dayOfWeek == 1) tv.setTextColor(0xffff0000);
        else tv.setTextColor(0xff000000);
    }

    private void setWeatherInfo() {
        findViewById(R.id.ivWeatherProgress).clearAnimation();
        findViewById(R.id.ivWeatherProgress).setVisibility(View.GONE);
        findViewById(R.id.tvWeatherLoading).setVisibility(View.GONE);

        Calendar cal = Calendar.getInstance();
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);   // 오늘 날짜
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);      // 일요일 = 1
        int lastDay = cal.getActualMaximum(Calendar.DATE);  // 이번달 마지막 날

        ((ImageView) findViewById(R.id.ivDay1)).setImageResource(CommonUtils.getWeatherIconResId(mInfo.arrWeathers.get(0).id));
        ((ImageView) findViewById(R.id.ivDay2)).setImageResource(CommonUtils.getWeatherIconResId(mInfo.arrWeathers.get(1).id));
        ((ImageView) findViewById(R.id.ivDay3)).setImageResource(CommonUtils.getWeatherIconResId(mInfo.arrWeathers.get(2).id));
        ((ImageView) findViewById(R.id.ivDay4)).setImageResource(CommonUtils.getWeatherIconResId(mInfo.arrWeathers.get(3).id));
        ((ImageView) findViewById(R.id.ivDay5)).setImageResource(CommonUtils.getWeatherIconResId(mInfo.arrWeathers.get(4).id));

        // 1
        setDateText(dayOfWeek, dayOfMonth, ((TextView)findViewById(R.id.tvDay1)));
        ((TextView)findViewById(R.id.tvDay1TempMax)).setText(mInfo.arrWeathers.get(0).tempMax);
        ((TextView)findViewById(R.id.tvDay1TempMin)).setText(mInfo.arrWeathers.get(0).tempMin);

        // 2
        dayOfWeek = ((dayOfWeek + 1) % 8);
        if(dayOfWeek == 0) dayOfWeek = 1;
        dayOfMonth += 1;
        if(dayOfMonth > lastDay) dayOfMonth = 1;
        setDateText(dayOfWeek, dayOfMonth, ((TextView)findViewById(R.id.tvDay2)));
        ((TextView)findViewById(R.id.tvDay2TempMax)).setText(mInfo.arrWeathers.get(1).tempMax);
        ((TextView)findViewById(R.id.tvDay2TempMin)).setText(mInfo.arrWeathers.get(1).tempMin);

        // 3
        dayOfWeek = ((dayOfWeek + 1) % 8);
        if(dayOfWeek == 0) dayOfWeek = 1;
        dayOfMonth += 1;
        if(dayOfMonth > lastDay) dayOfMonth = 1;
        setDateText(dayOfWeek, dayOfMonth, ((TextView)findViewById(R.id.tvDay3)));
        ((TextView)findViewById(R.id.tvDay3TempMax)).setText(mInfo.arrWeathers.get(2).tempMax);
        ((TextView)findViewById(R.id.tvDay3TempMin)).setText(mInfo.arrWeathers.get(2).tempMin);

        // 4
        dayOfWeek = ((dayOfWeek + 1) % 8);
        if(dayOfWeek == 0) dayOfWeek = 1;
        dayOfMonth += 1;
        if(dayOfMonth > lastDay) dayOfMonth = 1;
        setDateText(dayOfWeek, dayOfMonth, ((TextView)findViewById(R.id.tvDay4)));
        ((TextView)findViewById(R.id.tvDay4TempMax)).setText(mInfo.arrWeathers.get(3).tempMax);
        ((TextView)findViewById(R.id.tvDay4TempMin)).setText(mInfo.arrWeathers.get(3).tempMin);

        // 5
        dayOfWeek = ((dayOfWeek + 1) % 8);
        if(dayOfWeek == 0) dayOfWeek = 1;
        dayOfMonth += 1;
        if(dayOfMonth > lastDay) dayOfMonth = 1;
        setDateText(dayOfWeek, dayOfMonth, ((TextView) findViewById(R.id.tvDay5)));
        ((TextView)findViewById(R.id.tvDay5TempMax)).setText(mInfo.arrWeathers.get(4).tempMax);
        ((TextView)findViewById(R.id.tvDay5TempMin)).setText(mInfo.arrWeathers.get(4).tempMin);

        selectWeatherDay(0);
        findViewById(R.id.rlDay1).setOnClickListener(this);
        findViewById(R.id.rlDay2).setOnClickListener(this);
        findViewById(R.id.rlDay3).setOnClickListener(this);
        findViewById(R.id.rlDay4).setOnClickListener(this);
        findViewById(R.id.rlDay5).setOnClickListener(this);

        findViewById(R.id.llWeathers).startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
        findViewById(R.id.llWeathers).setVisibility(VISIBLE);

        /*findViewById(R.id.ivSelectedWeatherIcon).setVisibility(VISIBLE);
        findViewById(R.id.tvSelectedWeatherDescription).setVisibility(VISIBLE);
        findViewById(R.id.llSelectedWeathers).setVisibility(VISIBLE);*/
    }

    private void selectWeatherDay(int day) {
        mWeatherSelection = day;
        findViewById(R.id.rlDay1).setBackgroundColor(0xffbdbdbd);
        findViewById(R.id.rlDay2).setBackgroundColor(0xffbdbdbd);
        findViewById(R.id.rlDay3).setBackgroundColor(0xffbdbdbd);
        findViewById(R.id.rlDay4).setBackgroundColor(0xffbdbdbd);
        findViewById(R.id.rlDay5).setBackgroundColor(0xffbdbdbd);

        switch(day) {
            case 0: findViewById(R.id.rlDay1).setBackgroundColor(0xffffffff);   break;
            case 1: findViewById(R.id.rlDay2).setBackgroundColor(0xffffffff);   break;
            case 2: findViewById(R.id.rlDay3).setBackgroundColor(0xffffffff);   break;
            case 3: findViewById(R.id.rlDay4).setBackgroundColor(0xffffffff);   break;
            case 4: findViewById(R.id.rlDay5).setBackgroundColor(0xffffffff);   break;
        }

        ((ImageView)findViewById(R.id.ivWeatherBK)).setImageResource(CommonUtils.getWeatherBK(mInfo.arrWeathers.get(day).id));
        ((TextView)findViewById(R.id.tvSelectedWeatherDescription)).setText(CommonUtils.getWeatherText(mInfo.arrWeathers.get(day).id));

        ((TextView)findViewById(R.id.tvSelectedWeatherPressure)).setText(
                String.format(mContext.getString(R.string.DETAIL_WEATHER_PRESSURE), mInfo.arrWeathers.get(day).pressure));
        if(mInfo.arrWeathers.get(day).rain != null || mInfo.arrWeathers.get(day).snow != null) {
            ((TextView) findViewById(R.id.tvSelectedWeatherHumidity)).setText(
                    String.format(mContext.getString(R.string.DETAIL_WEATHER_HUMIDITY), "100"));
        } else {
            ((TextView) findViewById(R.id.tvSelectedWeatherHumidity)).setText(
                    String.format(mContext.getString(R.string.DETAIL_WEATHER_HUMIDITY), mInfo.arrWeathers.get(day).humidity));
        }
        ((TextView)findViewById(R.id.tvSelectedWeatherTemp)).setText(
                String.format(mContext.getString(R.string.DETAIL_WEATHER_TEMP), mInfo.arrWeathers.get(day).tempMorn, mInfo.arrWeathers.get(day).tempDay, mInfo.arrWeathers.get(day).tempEve, mInfo.arrWeathers.get(day).tempNight));
        ((TextView)findViewById(R.id.tvSelectedWeatherTempMinMax)).setText(
                String.format(mContext.getString(R.string.DETAIL_WEATHER_TEMP_MINMAX), mInfo.arrWeathers.get(day).tempMin, mInfo.arrWeathers.get(day).tempMax));
        ((TextView)findViewById(R.id.tvSelectedWeatherCloud)).setText(
                String.format(mContext.getString(R.string.DETAIL_WEATHER_CLOUD), mInfo.arrWeathers.get(day).clouds));
        ((TextView)findViewById(R.id.tvSelectedWeatherWind)).setText(
                String.format(mContext.getString(R.string.DETAIL_WEATHER_WIND), mInfo.arrWeathers.get(day).windSpeed, mInfo.arrWeathers.get(day).windDegree, CommonUtils.getWindDeg(mInfo.arrWeathers.get(day).windDegree)));

        if(mInfo.arrWeathers.get(day).rain != null && mInfo.arrWeathers.get(day).rain.trim().length() > 0) {
            ((TextView)findViewById(R.id.tvSelectedWeatherRain)).setText(
                    String.format(mContext.getString(R.string.DETAIL_WEATHER_RAIN), mInfo.arrWeathers.get(day).rain));
            findViewById(R.id.tvSelectedWeatherRain).setVisibility(VISIBLE);
        } else {
            findViewById(R.id.tvSelectedWeatherRain).setVisibility(GONE);
        }

        if(mInfo.arrWeathers.get(day).snow != null && mInfo.arrWeathers.get(day).snow.trim().length() > 0) {
            ((TextView)findViewById(R.id.tvSelectedWeatherSnow)).setText(
                    String.format(mContext.getString(R.string.DETAIL_WEATHER_SNOW), mInfo.arrWeathers.get(day).snow));
            findViewById(R.id.tvSelectedWeatherSnow).setVisibility(VISIBLE);
        } else {
            findViewById(R.id.tvSelectedWeatherSnow).setVisibility(GONE);
        }
        /*
        ((ImageView)findViewById(R.id.ivSelectedWeatherIcon)).setImageResource(CommonUtils.getWeatherIconResId(mInfo.arrWeathers.get(day).id));
        */
    }

    private void init(MtInfo_General info) {
        mInfo = info;
        mInfo.initExpands();

        removeAllViewsInLayout();
        setOnTouchListener(null);

        LayoutInflater.from(mContext).inflate(R.layout.item_detail, this);
        CommonUtils.setGlobalFont(this, CommonUtils.typeface);

        if ((mInfo.admin != null && mInfo.admin.length() > 0) ||
                (mInfo.adminNum != null && mInfo.adminNum.length() > 0)) {

            findViewById(R.id.rlAdmin).setVisibility(VISIBLE);
            if (mInfo.admin != null && mInfo.admin.length() > 0) {
                ((TextView) findViewById(R.id.tvAdmin)).setText(mInfo.admin);
            }

            if (mInfo.adminNum != null && mInfo.adminNum.length() > 0) {
                ((TextView) findViewById(R.id.tvAdminNum)).setText(mInfo.adminNum);
            }
            findViewById(R.id.rlAdmin).setOnClickListener(this);
        } else {
            findViewById(R.id.rlAdmin).setVisibility(GONE);
        }

        if(mInfo.weatherinfo == true) {
            setWeatherInfo();
        } else {
            findViewById(R.id.rlTodaysWeather).setVisibility(GONE);
            findViewById(R.id.ivWeatherProgress).startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.rotate));
            findViewById(R.id.ivWeatherProgress).setVisibility(VISIBLE);
            mInfo.requestWeatherInfo(mContext);
        }
        findViewById(R.id.rlWeather).setOnClickListener(this);

        if(mInfo.namedInfo == null) {

            findViewById(R.id.rlReason).setVisibility(GONE);
            findViewById(R.id.rlTransport).setVisibility(GONE);
            findViewById(R.id.rlTourism).setVisibility(GONE);
            findViewById(R.id.rlEtcCource).setVisibility(GONE);

            ((TextView) findViewById(R.id.tvName)).setText(mInfo.name);
            if (mInfo.sname != null && mInfo.sname.length() > 0)
                ((TextView) findViewById(R.id.tvSName)).setText(mInfo.sname);

            if (info.high != null && info.high.length() > 0) {
                findViewById(R.id.tvHeight).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.tvHeight)).setText(String.format(mContext.getString(R.string.RESULT_HEIGHT), info.high));
            }

            if (info.address != null && info.address.length() > 0) {
                findViewById(R.id.tvAddress).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.tvAddress)).setText(info.address);
            }

            if (mInfo.summary != null && mInfo.summary.length() > 0) {
                findViewById(R.id.rlSummary).setVisibility(VISIBLE);
                ((TextView) findViewById(R.id.tvSummary)).setText(mInfo.summary);
                findViewById(R.id.rlSummary).setOnClickListener(this);
            } else {
                findViewById(R.id.rlSummary).setVisibility(GONE);
            }

            if (mInfo.detail != null && mInfo.detail.length() > 0) {
                findViewById(R.id.rlDetail).setVisibility(VISIBLE);
                ((TextView) findViewById(R.id.tvDetail)).setText(mInfo.detail);
                findViewById(R.id.rlDetail).setOnClickListener(this);
            } else {
                findViewById(R.id.rlDetail).setVisibility(GONE);
            }
        } else {
            ((TextView) findViewById(R.id.tvName)).setText(mInfo.namedInfo.name);
            if(mInfo.namedInfo.height != null && mInfo.namedInfo.height.length() > 0) {
                findViewById(R.id.tvHeight).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.tvHeight)).setText(String.format(mContext.getString(R.string.RESULT_HEIGHT), info.namedInfo.height));
            }

            if (info.namedInfo.area != null && info.namedInfo.area.length() > 0) {
                findViewById(R.id.tvAddress).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.tvAddress)).setText(info.namedInfo.area);
            }

            if (mInfo.namedInfo.sname != null && mInfo.namedInfo.sname.length() > 0)
                ((TextView) findViewById(R.id.tvSName)).setText(mInfo.namedInfo.sname);

            if (mInfo.namedInfo.reason != null && mInfo.namedInfo.reason.length() > 0) {
                findViewById(R.id.rlReason).setVisibility(VISIBLE);
                ((TextView) findViewById(R.id.tvReason)).setText(mInfo.namedInfo.reason);
                findViewById(R.id.rlReason).setOnClickListener(this);
            } else {
                findViewById(R.id.rlReason).setVisibility(GONE);
            }

            if (mInfo.namedInfo.overview != null && mInfo.namedInfo.overview.length() > 0) {
                findViewById(R.id.rlSummary).setVisibility(VISIBLE);
                ((TextView) findViewById(R.id.tvSummary)).setText(mInfo.namedInfo.overview);
                findViewById(R.id.rlSummary).setOnClickListener(this);
            } else {
                findViewById(R.id.rlSummary).setVisibility(GONE);
            }

            if (mInfo.namedInfo.details != null && mInfo.namedInfo.details.length() > 0) {
                findViewById(R.id.rlDetail).setVisibility(VISIBLE);
                ((TextView) findViewById(R.id.tvDetail)).setText(mInfo.namedInfo.details);
                findViewById(R.id.rlDetail).setOnClickListener(this);
            } else {
                findViewById(R.id.rlDetail).setVisibility(GONE);
            }

            if (mInfo.namedInfo.transport != null && mInfo.namedInfo.transport.length() > 0) {
                findViewById(R.id.rlTransport).setVisibility(VISIBLE);
                ((TextView) findViewById(R.id.tvTransport)).setText(mInfo.namedInfo.transport);
                findViewById(R.id.rlTransport).setOnClickListener(this);
            } else {
                findViewById(R.id.rlTransport).setVisibility(GONE);
            }

            if (mInfo.namedInfo.tourismInfo != null && mInfo.namedInfo.tourismInfo.length() > 0) {
                findViewById(R.id.rlTourism).setVisibility(VISIBLE);
                ((TextView) findViewById(R.id.tvTourism)).setText(mInfo.namedInfo.tourismInfo);
                findViewById(R.id.rlTourism).setOnClickListener(this);
            } else {
                findViewById(R.id.rlTourism).setVisibility(GONE);
            }

            if (mInfo.namedInfo.etcCource != null && mInfo.namedInfo.etcCource.length() > 0) {
                findViewById(R.id.rlEtcCource).setVisibility(VISIBLE);
                ((TextView) findViewById(R.id.tvEtcCource)).setText(mInfo.namedInfo.etcCource);
                findViewById(R.id.rlEtcCource).setOnClickListener(this);
            } else {
                findViewById(R.id.rlEtcCource).setVisibility(GONE);
            }
        }

        //Display dp = mContext.getWindowManager().getDefaultDisplay();
        Rect rtDp = new Rect();
        getWindowVisibleDisplayFrame(rtDp);

        RelativeLayout rlLast = (RelativeLayout)findViewById(R.id.rlLast);
        ViewGroup.LayoutParams lp = rlLast.getLayoutParams();
        lp.height = rtDp.height() - CommonUtils.DP2PX(mContext, 160);
        rlLast.setLayoutParams(lp);

        findViewById(R.id.rlShare).setOnClickListener(this);

        initShpFile();
        mHandler.sendEmptyMessage(MESSAGE_READY_TO_EXPAND);
    }

    private void initShpFile() {
        if (mInfo.code != null && mInfo.code.length() > 0) {
            if (CommonUtils.isExistInAsset(mContext, mInfo.code)) {  // 지도 정보는 있음
                if (!CommonUtils.isExistInCache(mContext, mInfo.code)) {    // cache에 없음. 압축 해제 필요
                    String assetZipPath = "geo/" + mInfo.code + ".zip";
                    String dstDirPath = mContext.getCacheDir() + "/geo/" + mInfo.code;

                    if (CommonUtils.unzipFromAsset(mContext, assetZipPath, dstDirPath)) {
                        // 압축해제 성공
                        findViewById(R.id.rlMap).setVisibility(View.VISIBLE);
                        findViewById(R.id.rlMap).setOnClickListener(this);
                    } else {
                        // 압축해제 실패...??!?!?!?!?!
                        findViewById(R.id.rlMap).setVisibility(View.GONE);
                    }
                } else {    // 이미 압축 해제 되어 cache에 존재함.
                    findViewById(R.id.rlMap).setVisibility(View.VISIBLE);
                    findViewById(R.id.rlMap).setOnClickListener(this);
                }
            } else {        // 지도정보는 없음
                findViewById(R.id.rlMap).setVisibility(GONE);
            }
        } else {
            findViewById(R.id.rlMap).setVisibility(GONE);
        }
    }

    public void setMtInfo(MtInfo_General mtInfo, int position) {
        mPosition = position;
        init(mtInfo);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlDay1:   selectWeatherDay(0);    break;
            case R.id.rlDay2:   selectWeatherDay(1);    break;
            case R.id.rlDay3:   selectWeatherDay(2);    break;
            case R.id.rlDay4:   selectWeatherDay(3);    break;
            case R.id.rlDay5:   selectWeatherDay(4);    break;
            case R.id.rlWeather:
                if(mInfo.weather_expanded == true) {
                    mWeatherExpandAnimator.end();
                    ValueAnimator va = ValueAnimator.ofInt(mRlWeatherExpand.getHeight(), 0);
                    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            int value = (Integer)animator.getAnimatedValue();
                            ViewGroup.LayoutParams lp = mRlWeatherExpand.getLayoutParams();
                            lp.height = value;
                            mRlWeatherExpand.setLayoutParams(lp);
                        }
                    });
                    va.start();
                    ((ImageView)findViewById(R.id.ivIconWeatherEx)).setImageResource(R.drawable.ic_expand_more_white);
                    mInfo.weather_expanded = false;
                } else {
                    if (mWeatherExpandAnimator != null) {
                        mWeatherExpandAnimator.start();
                        mInfo.weather_expanded = true;
                        ((ImageView)findViewById(R.id.ivIconWeatherEx)).setImageResource(R.drawable.ic_expand_less_white);
                    }
                }
                break;
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
                    ((ImageView)findViewById(R.id.ivIconSummaryEx)).setImageResource(R.drawable.ic_expand_more_white);
                    mInfo.summary_expanded = false;
                } else {
                    if (mSummaryExpandAnimator != null) {
                        mSummaryExpandAnimator.start();
                        mInfo.summary_expanded = true;
                        ((ImageView)findViewById(R.id.ivIconSummaryEx)).setImageResource(R.drawable.ic_expand_less_white);
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
                    ((ImageView)findViewById(R.id.ivIconDetailEx)).setImageResource(R.drawable.ic_expand_more_white);
                    mInfo.detail_expanded = false;
                } else {
                    if (mDetailExpandAnimator != null) {
                        mDetailExpandAnimator.start();
                        mInfo.detail_expanded = true;
                        ((ImageView)findViewById(R.id.ivIconDetailEx)).setImageResource(R.drawable.ic_expand_less_white);
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
                    ((ImageView)findViewById(R.id.ivIconAdminEx)).setImageResource(R.drawable.ic_expand_more_white);
                    mInfo.admin_expanded = false;
                } else {
                    if(mAdminExpandAnimator != null) {
                        mAdminExpandAnimator.start();
                        mInfo.admin_expanded = true;
                        ((ImageView)findViewById(R.id.ivIconAdminEx)).setImageResource(R.drawable.ic_expand_less_white);
                    }
                }
                break;
            case R.id.rlMap:
                Intent intent = new Intent(mContext, MapActivity.class);
                intent.putExtra(MapActivity.EXTRA_INFO_POS, mPosition);
                mContext.startActivity(intent);
                break;

            case R.id.rlReason:
                if(mInfo.reason_expanded == true) {
                    mReasonExpandAnimator.end();

                    ValueAnimator va = ValueAnimator.ofInt(mRlReasonExpand.getHeight(), 0);
                    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            int value = (Integer)animator.getAnimatedValue();
                            ViewGroup.LayoutParams lp = mRlReasonExpand.getLayoutParams();
                            lp.height = value;
                            mRlReasonExpand.setLayoutParams(lp);
                        }
                    });
                    va.start();
                    ((ImageView)findViewById(R.id.ivIconReasonEx)).setImageResource(R.drawable.ic_expand_more_white);
                    mInfo.reason_expanded = false;
                } else {
                    if(mReasonExpandAnimator != null) {
                        mReasonExpandAnimator.start();
                        mInfo.reason_expanded = true;
                        ((ImageView)findViewById(R.id.ivIconReasonEx)).setImageResource(R.drawable.ic_expand_less_white);
                    }
                }
                break;

            case R.id.rlTransport:
                if(mInfo.transport_expanded == true) {
                    mTransportExpandAnimator.end();

                    ValueAnimator va = ValueAnimator.ofInt(mRlTransportExpand.getHeight(), 0);
                    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            int value = (Integer)animator.getAnimatedValue();
                            ViewGroup.LayoutParams lp = mRlTransportExpand.getLayoutParams();
                            lp.height = value;
                            mRlTransportExpand.setLayoutParams(lp);
                        }
                    });
                    va.start();
                    ((ImageView)findViewById(R.id.ivIconTransportEx)).setImageResource(R.drawable.ic_expand_more_white);
                    mInfo.transport_expanded = false;
                } else {
                    if(mTransportExpandAnimator != null) {
                        mTransportExpandAnimator.start();
                        mInfo.transport_expanded = true;
                        ((ImageView)findViewById(R.id.ivIconTransportEx)).setImageResource(R.drawable.ic_expand_less_white);
                    }
                }
                break;

            case R.id.rlTourism:
                if(mInfo.tourism_expanded == true) {
                    mTourismExpandAnimator.end();

                    ValueAnimator va = ValueAnimator.ofInt(mRlTourismExpand.getHeight(), 0);
                    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            int value = (Integer)animator.getAnimatedValue();
                            ViewGroup.LayoutParams lp = mRlTourismExpand.getLayoutParams();
                            lp.height = value;
                            mRlTourismExpand.setLayoutParams(lp);
                        }
                    });
                    va.start();
                    ((ImageView)findViewById(R.id.ivIconTourismEx)).setImageResource(R.drawable.ic_expand_more_white);
                    mInfo.tourism_expanded = false;
                } else {
                    if(mTourismExpandAnimator != null) {
                        mTourismExpandAnimator.start();
                        mInfo.tourism_expanded = true;
                        ((ImageView)findViewById(R.id.ivIconTourismEx)).setImageResource(R.drawable.ic_expand_less_white);
                    }
                }
                break;

            case R.id.rlEtcCource:
                if(mInfo.etccource_expanded == true) {
                    mEtccourceExpandAnimator.end();

                    ValueAnimator va = ValueAnimator.ofInt(mRlEtccourceExpand.getHeight(), 0);
                    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            int value = (Integer)animator.getAnimatedValue();
                            ViewGroup.LayoutParams lp = mRlEtccourceExpand.getLayoutParams();
                            lp.height = value;
                            mRlEtccourceExpand.setLayoutParams(lp);
                        }
                    });
                    va.start();
                    ((ImageView)findViewById(R.id.ivIconEtcCourceEx)).setImageResource(R.drawable.ic_expand_more_white);
                    mInfo.etccource_expanded = false;
                } else {
                    if(mEtccourceExpandAnimator != null) {
                        mEtccourceExpandAnimator.start();
                        mInfo.etccource_expanded = true;
                        ((ImageView)findViewById(R.id.ivIconEtcCourceEx)).setImageResource(R.drawable.ic_expand_less_white);
                    }
                }
                break;
            case R.id.rlShare:
                CommonUtils.launchShare(mContext, mInfo);
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
