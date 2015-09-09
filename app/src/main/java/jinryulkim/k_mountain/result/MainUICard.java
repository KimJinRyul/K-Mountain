package jinryulkim.k_mountain.result;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;

import jinryulkim.k_mountain.CommonUtils;
import jinryulkim.k_mountain.MtInfoMgr;
import jinryulkim.k_mountain.R;

/**
 * Created by jinryulkim on 15. 8. 28..
 */
public class MainUICard extends View {
    private Context mContext;
    private int mOriginHeight, mOriginHeightTemp;
    private int mCurrentHeight, mCurrentWidth, mFlexableHeight; /*, mBKFlexableHeight;*/
    private int mTitleHeight;
    private int mDeltaY;
    private int mBtnState;
    private float mTextSize = 0, mTextMargin;
    private boolean mFixed = false;
    private ListAdapter.CardListener mListener;

    private Rect mRect, mRectBackBtn, mRectSearchBtn;
    private Paint mPaint;
    private static Bitmap mBmpBK = null;
    private static Drawable mDrawableBack, mDrawableBackPressed, mDrawableSearch, mDrawableSearchPressed;

    private final static int BTN_STATE_NOTHING = 0;
    private final static int BTN_STATE_BACK = 1;
    private final static int BTN_STATE_SEARCH = 2;

    /**
     * MainUICard는 프로그램 적으로만 생성되어야 함
     * @param context
     */
    public MainUICard(Context context, ListAdapter.CardListener listener) {
        super(context);
        mContext = context;
        mListener = listener;
        init();
    }

    public static void loadImages(Context context) {
        if(mBmpBK == null) {
            mBmpBK = BitmapFactory.decodeResource(context.getResources(), R.drawable.result);
            mDrawableBack = context.getResources().getDrawable(R.drawable.ic_arrow_back_white_48dp);
            mDrawableBackPressed = context.getResources().getDrawable(R.drawable.ic_arrow_back_grey600_48dp);
            mDrawableSearch = context.getResources().getDrawable(R.drawable.ic_search_white_48dp);
            mDrawableSearchPressed = context.getResources().getDrawable(R.drawable.ic_search_grey600_48dp);
        }
    }

    private void init() {
        mOriginHeight = mOriginHeightTemp = mCurrentHeight = mCurrentWidth = mFlexableHeight = 0;
        mDeltaY = 0;
        mTitleHeight = 0;
        mRect = new Rect(0, 0, 0, 0);
        mRectBackBtn = new Rect(0, 0, 0, 0);
        mRectSearchBtn = new Rect(0, 0, 0, 0);
        mPaint = new Paint();
        mPaint.setTypeface(CommonUtils.typeface);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setClickable(false);
    }

    /**
     * 현재 StatusBar와 Title의 높이값, 좌표 보정하기 위해 입력받아 둔다.
     * @param titleHeight
     */
    public void setTitleHeight(int titleHeight) {
        mTitleHeight = titleHeight;
    }

    public int getTitleHeight() {
        return mTitleHeight;
    }

    /**
     * MainUICard는 프로그램적으로만 생성하므로 EXACTLY 로만 동작
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if(widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            mOriginHeightTemp = height;
            setMeasuredDimension(width, height);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mCurrentHeight = h;
        mCurrentWidth = w;
        invalidate();
    }

    /**
     * 현재 MainUICard가 그려야 할 높이를 반환
     * 실제 MainUICard의 layout의 실제 높이와 항상 일치하지 않는다는 점에 유의
     * ListView가 Scroll 되더라도 MainUICard 아래에 위치하는 Item들이 MainUICard가 충분히 접히기 전에
     * MainUICard 아래로 들어가는 현상을 방지하기 위해 실제 Layout의 높이값과 그려져야 할 높이값은 달라야 함
     * @return
     */
    public int getCurrentHeight() {
        return mCurrentHeight;
    }

    /**
     * ListView가 현재 Y값을 입력해 준다.
     * deltaY 는 MainUICard의 top위치를 의미하는데... ListView의 특성상 항상 정확한 값을 갖지는 않는다.
     * 화면 상단에서 MainUICard가 사라지면 어느 시점부터 더이상 이동하지 않는 케이스가 존재하므로 고려하여야 한다.
     * @param deltaY
     */
    public void setDeltaY(int deltaY) {
        if(mFixed == false)
            return;

        mDeltaY = -deltaY;
        if(mDeltaY <= 0)
            mCurrentHeight = mOriginHeight;
        else {
            mCurrentHeight = mOriginHeight - mDeltaY;
            if(mCurrentHeight < mOriginHeight - mFlexableHeight)
                mCurrentHeight = mOriginHeight - mFlexableHeight;
        }
        invalidate();
    }

    /**
     * 최초 스크롤 시작시 사이즈를 확정한다.
     * 그전에 프로그램적으로 최종 사이즈를 확정하기 어렵다.
     */
    public void fixSizes() {
        mOriginHeight = mOriginHeightTemp;

        mTextSize = mOriginHeight * 0.12f;
        mTextMargin = mOriginHeight * 0.92f;

        int margin = mCurrentWidth / 38;
        int width = mCurrentWidth / 12;

        mRectBackBtn = new Rect(margin, margin, margin + width, margin + width);
        mRectSearchBtn = new Rect(mCurrentWidth - margin - width, margin, mCurrentWidth - margin, margin + width);

        mFlexableHeight = mOriginHeight - (margin + width + margin);

        mDrawableBack.setBounds(mRectBackBtn);
        mDrawableBackPressed.setBounds(mRectBackBtn);
        mDrawableSearch.setBounds(mRectSearchBtn);
        mDrawableSearchPressed.setBounds(mRectSearchBtn);

        mFixed = true;
    }

    /**
     * ListView나 CardView로 부터 터치된 좌표를 입력받아 처리한다.
     * 직접 처리하면 안되는 이유는, MainUICard는 실제로 화면상에는 존재하지 않으나
     * 화면 상단에 존재하는 것 처럼 그려지기만 하는 케이스가 더 많기 때문임
     * @param event
     * @param pt
     */
    public void setTouchEvent(int event, Point pt) {
        pt.y -= mTitleHeight;

        switch(event) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                mBtnState = BTN_STATE_NOTHING;
                if(CommonUtils.isPointInRect(pt, mRectBackBtn)) {
                    mBtnState = BTN_STATE_BACK;
                } else if(CommonUtils.isPointInRect(pt, mRectSearchBtn)){
                    mBtnState = BTN_STATE_SEARCH;
                }
                break;
            case MotionEvent.ACTION_UP:
                switch(mBtnState) {
                    case BTN_STATE_BACK:
                        mListener.onClickBack();
                        break;
                    case BTN_STATE_SEARCH:
                        mListener.onClickSearch();
                        break;
                }
                mBtnState = BTN_STATE_NOTHING;
                break;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mCurrentWidth == 0 || mCurrentHeight == 0 || mPaint == null || mFixed == false)
            return;

        // draw background image
        int curDelta = mOriginHeight - mCurrentHeight;
        int I_Delta = ((curDelta * mBmpBK.getHeight()) / mOriginHeight) / 2;
        Rect src = new Rect(0, I_Delta, mBmpBK.getWidth(), mBmpBK.getHeight() - I_Delta);
        Rect dst = new Rect(0, 0, mCurrentWidth, mCurrentHeight);
        canvas.drawBitmap(mBmpBK, src, dst, mPaint);

        // draw background color
        int bkColor = 0x00009688;
        int percentage = (255 * (mOriginHeight - mCurrentHeight)) / mFlexableHeight;
        bkColor += percentage << 24;
        mPaint.setColor(bkColor);
        canvas.drawRect(0, 0, mCurrentWidth, mCurrentHeight, mPaint);

        // draw btns
        if(mBtnState == BTN_STATE_BACK)     mDrawableBackPressed.draw(canvas);
        else                                mDrawableBack.draw(canvas);

        if(mBtnState == BTN_STATE_SEARCH)   mDrawableSearchPressed.draw(canvas);
        else                                mDrawableSearch.draw(canvas);

        // draw Text
        mPaint.setColor(0xffffffff);
        mPaint.setTextSize(mTextSize);
        canvas.drawText(MtInfoMgr.searchWrd, mCurrentWidth * 0.17f, mTextMargin - curDelta, mPaint);

        /*try {
            PinnedHeaderListView phlv = (PinnedHeaderListView)getParent();
            phlv.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
}
