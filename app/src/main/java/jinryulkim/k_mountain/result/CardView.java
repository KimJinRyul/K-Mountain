package jinryulkim.k_mountain.result;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import jinryulkim.k_mountain.CardTouchListener;
import jinryulkim.k_mountain.CommonUtils;
import jinryulkim.k_mountain.MtInfoMgr;
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

    private final static int ANIMATION_TIME = 600;
    private final static int MESSAGE_START_ANIMATION = 1000;
    private final static int MESSAGE_IMAGE_DOWNLOADED = 1001;
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
            }
        }
    };

    public final static int BTN_ID_SEARCH_MORE  = 1000;
    public final static int BTN_ID_DETAIL       = 1001;
    public final static int BTN_ID_SHARE        = 1002;

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
            ((ImageView) findViewById(R.id.ivProgress)).setImageResource(R.drawable.ic_highlight_remove_white_48dp);
        }
    }

    private void init(MtInfo_General info) {
        mInfo = info;
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

            if(mInfo.imagePaths != null && mInfo.imagePaths.size() > 0) {
                if(mInfo.downloaded == true) {
                    setDownloadedImage();
                } else {
                    findViewById(R.id.ivProgress).startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.rotate));
                    mInfo.requestDownloadImage(mContext);
                }
            } else {
                ((ImageView)findViewById(R.id.ivProgress)).setImageResource(R.drawable.ic_highlight_remove_white_48dp);
            }

            findViewById(R.id.btnDetail).setOnClickListener(this);
            findViewById(R.id.btnShare).setOnClickListener(this);
        } else {
            LayoutInflater.from(mContext).inflate(R.layout.item_empty, this);
            if(MtInfoMgr.totalCnt > MtInfoMgr.mMtInfos.size() + MtInfoMgr.deletedCnt) {
                findViewById(R.id.ivProgress).setVisibility(View.VISIBLE);
                findViewById(R.id.ivProgress).setOnClickListener(this);
                ((TextView)findViewById(R.id.tvMoreInfo)).setText(R.string.RESULT_MORE_INFO);

            } else {
                findViewById(R.id.ivProgress).setVisibility(View.GONE);
                ((TextView)findViewById(R.id.tvMoreInfo)).setText(R.string.RESULT_NO_MORE_INFO);
            }
        }

        // ready to swipe
        setOnTouchListener(new CardTouchListener(this, mCallback));
    }

    public int getPosition() {
        return mPosition;
    }

    public void setMtInfo(MtInfo_General mtInfo, int position) {
        mPosition = position;
        init(mtInfo);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnDetail:
                mCallback.onClickBtn(mInfo, mPosition, BTN_ID_DETAIL);
                break;
            case R.id.btnShare:
                mCallback.onClickBtn(mInfo, mPosition, BTN_ID_SHARE);
                break;
            case R.id.ivProgress:
                mCallback.onClickBtn(null, -1, BTN_ID_SEARCH_MORE);
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
