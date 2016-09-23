package jinryulkim.k_mountain.result;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import jinryulkim.k_mountain.CardTouchListener;
import jinryulkim.k_mountain.CommonUtils;
import jinryulkim.k_mountain.DB.MyDBManager;
import jinryulkim.k_mountain.MtInfo_General;
import jinryulkim.k_mountain.R;

/**
 * Created by jinryulkim on 15. 8. 28..
 */
public class CardView extends RelativeLayout implements View.OnClickListener {
    private Context mContext = null;
    private int mPosition = 0;
    private MtInfo_General mInfo = null;
    private CardTouchListener.CardTouchCallback mCallback = null;
    private int mListType = ListAdapter.LIST_TYPE_RESULT;
    private boolean mbAddMy = true;
    public CardTouchListener mCardTouchListener = null;

    private final static int ANIMATION_TIME = 600;
    private final static int MESSAGE_START_ANIMATION = 1000;
    private final static int MESSAGE_IMAGE_DOWNLOADED = 1001;
    private final static int MESSAGE_WEATHER_DOWNLOADED = 1002;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_START_ANIMATION:
                    animate().translationY(0)
                            .alpha(1)
                            .setDuration(ANIMATION_TIME)
                            .setListener(null);
                    break;
                case MESSAGE_IMAGE_DOWNLOADED:
                    setDownloadedImage();
                    break;
                case MESSAGE_WEATHER_DOWNLOADED:
                    setWeatherInfo(true);
                    break;
            }
        }
    };

    public final static int BTN_ID_SEARCH_MORE  = 1000;
    public final static int BTN_ID_DETAIL       = 1001;
    public final static int BTN_ID_SHARE        = 1002;
    public final static int BTN_ID_ADDMY        = 1003;
    public final static int BTN_ID_DELMY        = 1004;

    public CardView(Context context, CardTouchListener.CardTouchCallback callback) {
        super(context);
        mContext = context;
        mCallback = callback;
    }

    public void startAnimationWithDelay(long delay) {
        setTranslationY(CommonUtils.DP2PX(mContext, 100.f));
        setAlpha(0);
        mHandler.sendEmptyMessageDelayed(MESSAGE_START_ANIMATION, delay);
    }

    public MtInfo_General getMtInfo(){
        return mInfo;
    }

    public void startLoading() {
        if(mInfo.code == null || mInfo.code.length() <= 0) {
            findViewById(R.id.ivProgress).startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.rotate));
        }
    }

    public void stopLoading() {
        if(mInfo.code == null || mInfo.code.length() <= 0) {
            findViewById(R.id.ivProgress).clearAnimation();
        }
    }

    public void downloadCompleted() {
        mHandler.sendEmptyMessage(MESSAGE_IMAGE_DOWNLOADED);
    }

    public void weatherCompleted() {
        mHandler.sendEmptyMessageDelayed(MESSAGE_WEATHER_DOWNLOADED, 500);
    }

    private void setWeatherInfo(boolean animation) {
        Log.i("jrkim", "setWeatherInfo");
        findViewById(R.id.ivWeatherProgress).clearAnimation();
        findViewById(R.id.ivWeatherProgress).setVisibility(View.GONE);

        if(mInfo.weatherinfo == false || mInfo.todaysWeather == null || mInfo.arrWeathers == null || mInfo.arrWeathers.size() != 5) {
            findViewById(R.id.tvWeatherLoading).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.tvWeatherLoading)).setText(R.string.RESULT_WEATHER_ERROR);
            Log.i("jrkim", "못 얻... 끝-");
            return;
        }

        Log.i("jrkim", "얻었당~");
        findViewById(R.id.tvWeatherLoading).setVisibility(View.GONE);

        Calendar cal = Calendar.getInstance();
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);   // 오늘 날짜
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);      // 일요일 = 1
        int lastDay = cal.getActualMaximum(Calendar.DATE);  // 이번달 마지막 날

        findViewById(R.id.rlTodaysWeather).setBackgroundResource(CommonUtils.getWeatherIconResId(mInfo.todaysWeather.id));
        ((TextView)findViewById(R.id.tvCurrentTemp)).setText(mInfo.todaysWeather.tempDay + "°C");

        try {
            Date dateSunrise = new Date(mInfo.todaysWeather.sunrise * 1000L);
            Date dateSunset = new Date(mInfo.todaysWeather.sunset * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String strSunRise = sdf.format(dateSunrise);
            String strSunSet = sdf.format(dateSunset);

            ((TextView)findViewById(R.id.tvSunRise)).setText(mContext.getString(R.string.RESULT_SUNRISE) + strSunRise);
            ((TextView)findViewById(R.id.tvSunSet)).setText(mContext.getString(R.string.RESULT_SUNSET) + strSunSet);
            findViewById(R.id.tvSunRise).setVisibility(View.VISIBLE);
            findViewById(R.id.tvSunSet).setVisibility(View.VISIBLE);

        } catch (Exception e) {
            ((TextView)findViewById(R.id.tvSunRise)).setText("");
            ((TextView)findViewById(R.id.tvSunSet)).setText("");
            findViewById(R.id.tvSunRise).setVisibility(View.GONE);
            findViewById(R.id.tvSunSet).setVisibility(View.GONE);
        }

        if(mInfo.arrWeathers != null && mInfo.arrWeathers.size() == 5) {
            synchronized (mInfo.arrWeathers) {
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
                setDateText(dayOfWeek, dayOfMonth, ((TextView)findViewById(R.id.tvDay5)));
                ((TextView)findViewById(R.id.tvDay5TempMax)).setText(mInfo.arrWeathers.get(4).tempMax);
                ((TextView)findViewById(R.id.tvDay5TempMin)).setText(mInfo.arrWeathers.get(4).tempMin);
            }
        }

        if(animation) {
            findViewById(R.id.rlTodaysWeather).startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.zoom_enter));
            findViewById(R.id.llWeathers).startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
        }

        findViewById(R.id.llWeathers).setVisibility(VISIBLE);
        findViewById(R.id.rlTodaysWeather).setVisibility(View.VISIBLE);
    }

    private void setDateText(int dayOfWeek, int dayOfMonth, TextView tv) {
        tv.setText(dayOfMonth + CommonUtils.getDayOfWeek(dayOfWeek));
        if(dayOfWeek == 7)  tv.setTextColor(0xff0000ff);
        else if(dayOfWeek == 1) tv.setTextColor(0xffff0000);
        else tv.setTextColor(0xff000000);
    }

    private void setDownloadedImage() {
        findViewById(R.id.ivProgress).clearAnimation();
        boolean bImageSet = false;
        for(int i = 0; i < mInfo.imagePaths.size(); i++) {
            String filePath = mInfo.makeImagePath(mContext, i);
            if (filePath != null) {
                Bitmap bmp = BitmapFactory.decodeFile(filePath);
                if (bmp != null) {
                    bImageSet = true;
                    ((ImageView) findViewById(R.id.ivImage)).setImageBitmap(bmp);
                    findViewById(R.id.ivProgress).setVisibility(View.GONE);
                    break;
                }
            }
        }

        if(bImageSet == false) {
            findViewById(R.id.ivProgress).setVisibility(View.VISIBLE);
            findViewById(R.id.ivImage).setVisibility(View.GONE);
            ((ImageView) findViewById(R.id.ivProgress)).setImageResource(R.drawable.ic_highlight_remove_white);
        }
    }

    private void init(MtInfo_General info, boolean swipe, int listType) {
        mHandler.removeMessages(MESSAGE_IMAGE_DOWNLOADED);
        mHandler.removeMessages(MESSAGE_WEATHER_DOWNLOADED);

        Log.i("jrkim", "CardView.init");

        mInfo = info;
        mListType = listType;
        removeAllViewsInLayout();
        setOnTouchListener(null);

        if(mInfo.code != null && mInfo.code.length() > 0) {
            LayoutInflater.from(mContext).inflate(R.layout.item_list, this);

            CommonUtils.setGlobalFont(this, CommonUtils.typeface);

            ((TextView) findViewById(R.id.tvMtName)).setText(info.name);
            if(info.sname != null && info.sname.length() > 0) {
                findViewById(R.id.tvSubName).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.tvSubName)).setText(info.sname);
            } else {
                findViewById(R.id.tvSubName).setVisibility(View.GONE);
            }

            if(info.high != null && info.high.length() > 0) {
                findViewById(R.id.tvHeight).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.tvHeight)).setText(String.format(mContext.getString(R.string.RESULT_HEIGHT), info.high));
            } else {
                findViewById(R.id.tvHeight).setVisibility(View.GONE);
            }

            if(info.address != null && info.address.length() > 0) {
                findViewById(R.id.tvAddress).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.tvAddress)).setText(info.address);
            } else {
                findViewById(R.id.tvAddress).setVisibility(View.GONE);
            }

            if(info.summary != null && info.summary.length() > 0) {
                findViewById(R.id.tvSummary).setVisibility(View.VISIBLE);
                if(info.summary.indexOf(".") > 0)
                    ((TextView) findViewById(R.id.tvSummary)).setText(info.summary.substring(0, info.summary.indexOf(".") + 1));
                else
                    ((TextView) findViewById(R.id.tvSummary)).setText(info.summary);
            } else {
                findViewById(R.id.tvSummary).setVisibility(View.GONE);
            }

            if(mInfo.namedInfo != null) {
                findViewById(R.id.tvNamed).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.tvNamed).setVisibility(View.GONE);
            }

            if(CommonUtils.isExistInAsset(mContext, mInfo.code)) {
                findViewById(R.id.tvMap).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.tvMap).setVisibility(View.GONE);
            }

            if(mInfo.imagePaths != null && mInfo.imagePaths.size() > 0) {
                if(mInfo.downloaded == true) {
                    setDownloadedImage();
                } else {
                    findViewById(R.id.ivProgress).startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.rotate));
                    mInfo.requestDownloadImage(mContext);
                }
            } else {
                ((ImageView)findViewById(R.id.ivProgress)).setImageResource(R.drawable.ic_highlight_remove_white);
            }

            if(mInfo.weatherinfo == true) {
                Log.i("jrkim", "날씨... 이미 있넹.");
                setWeatherInfo(false);
            } else {
                Log.i("jrkim", "날씨... 없당...");
                findViewById(R.id.rlTodaysWeather).setVisibility(GONE);
                findViewById(R.id.ivWeatherProgress).setVisibility(VISIBLE);
                findViewById(R.id.tvWeatherLoading).setVisibility(VISIBLE);
                ((TextView)findViewById(R.id.tvWeatherLoading)).setText(R.string.CARD_WEATHER);
                findViewById(R.id.ivWeatherProgress).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setOnClickListener(null);
                        ((TextView)findViewById(R.id.tvWeatherLoading)).setText(R.string.RESULT_WEATHER_LOADING);
                        findViewById(R.id.ivWeatherProgress).startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.rotate));
                        mInfo.requestWeatherInfo(mContext);
                    }
                });
                //mInfo.requestWeatherInfo(mContext);
            }

            findViewById(R.id.btnDetail).setOnClickListener(this);
            findViewById(R.id.btnShare).setOnClickListener(this);
            findViewById(R.id.btnMy).setOnClickListener(this);

            MyDBManager myDB = MyDBManager.getInstance(mContext);
            myDB.open();
            Cursor cursor = myDB.get(new String [] { mInfo.code });
            if(cursor != null && cursor.getCount() > 0) {
                mbAddMy = false;
                ((Button)findViewById(R.id.btnMy)).setText(R.string.CARD_DELMY);
                ((Button)findViewById(R.id.btnMy)).setTextColor(0xfff44336);
            } else {
                mbAddMy = true;
                ((Button)findViewById(R.id.btnMy)).setText(R.string.CARD_ADDMY);
                ((Button)findViewById(R.id.btnMy)).setTextColor(0xff673ab7);
            }

            cursor.close();
            myDB.close();

        } else {
            LayoutInflater.from(mContext).inflate(R.layout.item_empty, this);
            /*if(MtInfoMgr.totalCnt > MtInfoMgr.mMtInfos.size() + MtInfoMgr.deletedCnt) {
                findViewById(R.id.ivProgress).setVisibility(View.VISIBLE);
                findViewById(R.id.ivProgress).setOnClickListener(this);
                ((TextView)findViewById(R.id.tvMoreInfo)).setText(R.string.RESULT_MORE_INFO);

            } else {*/
                findViewById(R.id.ivProgress).setVisibility(View.GONE);
                ((TextView)findViewById(R.id.tvMoreInfo)).setText(R.string.RESULT_NO_MORE_INFO);
            //}
        }

        // ready to swipe
        if(swipe) {
            mCardTouchListener = new CardTouchListener(this, mCallback, mListType);
            setOnTouchListener(mCardTouchListener);
        }
    }

    public int getPosition() {
        return mPosition;
    }

    public void setMtInfo(MtInfo_General mtInfo, int position, boolean swipe, int listType) {
        mCardTouchListener = null;
        mPosition = position;
        init(mtInfo, swipe, listType);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnDetail:
                mCallback.onClickBtn(mInfo, mPosition, BTN_ID_DETAIL, null);
                break;
            case R.id.ivProgress:
                mCallback.onClickBtn(null, -1, BTN_ID_SEARCH_MORE, null);
                break;
            case R.id.btnShare:
                mCallback.onClickBtn(mInfo, mPosition, BTN_ID_SHARE, null);
                break;
            case R.id.btnMy:
                mCallback.onClickBtn(mInfo, mPosition, mbAddMy ? BTN_ID_ADDMY : BTN_ID_DELMY, mCardTouchListener);
                if(mbAddMy) {
                    mbAddMy = false;
                    ((Button)findViewById(R.id.btnMy)).setText(R.string.CARD_DELMY);
                    ((Button)findViewById(R.id.btnMy)).setTextColor(0xfff44336);
                } else {
                    mbAddMy = true;
                    ((Button)findViewById(R.id.btnMy)).setText(R.string.CARD_ADDMY);
                    ((Button)findViewById(R.id.btnMy)).setTextColor(0xff673ab7);
                }
                break;
        }
    }

/**
 * 각 버튼 등에 OnClickListener등을 사용하지 않고 TouchEvent를 직접 처리해야 하는 이유는
 * 접힐수 있는 MainUICard 를 사용하기 위해서임.
 * 만약 OnClickListener를 사용하면 MainUICard 뒤에 숨겨져있는데 클릭이 가능해져 버리는 현상 발생
 * @param motionEvent
 * @return
 */

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean res = super.onTouchEvent(motionEvent);
        switch(motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(isMainUIAreaTouch(motionEvent) == false) {
                    // TODO
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if(isMainUIAreaTouch(motionEvent) == false) {
                    // TODO
                }
                return true;
            case MotionEvent.ACTION_UP:
                if(isMainUIAreaTouch(motionEvent) == false) {
                    // TODO
                }
                return false;
        }
        return res;
    }

    private boolean isMainUIAreaTouch(MotionEvent motionEvent) {
        MainUICard mainUICard = ListAdapter.getMainUICard();
        if(mainUICard != null && motionEvent.getRawY() <= mainUICard.getCurrentHeight() + mainUICard.getTitleHeight()) {
            mainUICard.setTouchEvent(motionEvent.getAction(), new Point((int) motionEvent.getRawX(), (int)motionEvent.getRawY()));
            return true;
        }
        return false;
    }
}
