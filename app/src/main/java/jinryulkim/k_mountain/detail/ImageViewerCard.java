package jinryulkim.k_mountain.detail;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

import jinryulkim.k_mountain.CommonUtils;
import jinryulkim.k_mountain.MtInfo_General;
import jinryulkim.k_mountain.R;

/**
 * Created by jinryulkim on 15. 9. 2..
 */
public class ImageViewerCard extends View {
    private Context mContext;
    private int mOriginHeight, mOriginHeightTemp;
    private int mCurrentHeight, mCurrentWidth, mFlexableHeight;
    private int mTitleHeight;
    private int mDeltaY;
    private int mBtnState;
    private boolean mFixed = false;
    private Rect mRect, mRectBackBtn, mRectMapBtn;
    private Paint mPaint;
    private ArrayList<Bitmap> mImages = new ArrayList<Bitmap>();
    private int mCurImage = 0;
    private static Bitmap mBmpBK = null;
    private static Drawable mDrawableBack = null, mDrawableMap = null;
    private DetailAdapter.CardListener mListener = null;
    private PinnedHeaderListViewForDetail mPHLV = null;

    private MtInfo_General mInfo;
    private int mInfoPosition;
    private boolean mMapExist = false;

    private final static int BTN_STATE_NOTHING = 0;
    private final static int BTN_STATE_BACK = 1;
    private final static int BTN_STATE_MAP = 2;

    private final static int MESSAGE_CHANGE_IMAGE = 1000;
    private final static int IMAGE_CHANGE_TIME     = 3000;
    private int mAnimCnt = 0;
    private Handler mHandler = new Handler() {
      @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_CHANGE_IMAGE:
                    ValueAnimator va = ValueAnimator.ofInt(0, 255);
                    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            mAnimCnt = (Integer) animator.getAnimatedValue();
                            if(mPHLV != null)
                                mPHLV.invalidate();
                        }
                    });

                    va.addListener(new ValueAnimator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mCurImage = getNextImg();
                            mAnimCnt = 0;
                            if(mPHLV != null)
                                mPHLV.invalidate();
                            mHandler.sendEmptyMessageDelayed(MESSAGE_CHANGE_IMAGE, IMAGE_CHANGE_TIME);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }
                    });
                    va.start();
                    break;
            }
      }
    };

    public ImageViewerCard(Context context, DetailAdapter.CardListener listener) {
        super(context);
        mContext = context;
        mListener = listener;
        init();
    }

    public static void loadImages(Context context) {
        if(mDrawableBack == null) {
            mBmpBK = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_image);
            mDrawableBack = context.getResources().getDrawable(R.drawable.ic_arrow_back_white);
            mDrawableMap = context.getResources().getDrawable(R.drawable.ic_map_white);
        }
    }

    public void setPinnedHeaderListViewForDetail(PinnedHeaderListViewForDetail phlv) {
        mPHLV = phlv;
    }

    public void setInformation(MtInfo_General info, int position) {
        mInfo = info;
        mInfoPosition = position;

        mImages.clear();
        mCurImage = 0;

        Bitmap bmp;
        String filePath;
        for(int i = 0; i < mInfo.imagePaths.size(); i++) {
            filePath = mInfo.makeImagePath(mContext, i);
            if(filePath != null) {
                bmp = BitmapFactory.decodeFile(filePath);
                if(bmp != null) {
                    mImages.add(bmp);
                }
            }
        }

        if(mImages.size() <= 0) {
            mImages.add(mBmpBK);
        }

        mCurImage = 0;
        if(mImages.size() > 1)
            mHandler.sendEmptyMessageDelayed(MESSAGE_CHANGE_IMAGE, IMAGE_CHANGE_TIME);

        if(CommonUtils.isExistInAsset(mContext, mInfo.code))
            mMapExist = true;
    }

    private int getNextImg() {
        int next = mCurImage + 1;
        if(mImages.size() <= next) {
            next = 0;
        }
        return next;
    }

    private void init() {
        mOriginHeight = mOriginHeightTemp = mCurrentHeight = mCurrentWidth = mFlexableHeight = 0;
        mDeltaY = 0;
        mTitleHeight = 0;
        mRect = new Rect(0, 0, 0, 0);
        mRectBackBtn = new Rect(0, 0, 0, 0);
        mRectMapBtn = new Rect(0, 0, 0, 0);
        mPaint = new Paint();
        mPaint.setTypeface(CommonUtils.typeface);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setClickable(false);
    }

    public void setTitleHeight(int titleHeight) {
        mTitleHeight = titleHeight;
    }
    public int getTitleHeight() {
        return mTitleHeight;
    }

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

    public int getCurrentHeight() {
        return mCurrentHeight;
    }

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

    public void fixSizes() {
        if(mFixed == false) {
            mOriginHeight = mOriginHeightTemp;
            int margin = mCurrentWidth / 38;
            int width = mCurrentWidth / 12;

            mRectBackBtn = new Rect(margin, margin, margin + width, margin + width);
            mRectMapBtn = new Rect(mCurrentWidth - margin - width, margin, mCurrentWidth - margin, margin +width);

            mFlexableHeight = mOriginHeight - (margin + width + margin);

            mDrawableBack.setBounds(mRectBackBtn);
            mDrawableMap.setBounds(mRectMapBtn);

            mFixed = true;
        }
    }

    public int getMinimuxHeight() {
        return mOriginHeight - mFlexableHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if(mCurrentWidth <= 0 || mCurrentHeight <= 0 || mPaint == null || mFixed == false)
            return;

        // draw image
        int bkColor = 0x00111111;
        Rect src, dst;
        if(mImages.size() > 0 && mCurImage <= mImages.size() - 1) { // 그릴 이미지가 있음
            // 현재 이미지 그리기
            Bitmap bmpBK = mImages.get(mCurImage);
            bkColor += (255 - mAnimCnt) << 24;
            mPaint.setColor(bkColor);
            int curDelta = mOriginHeight - mCurrentHeight;
            int I_Delta = ((curDelta * bmpBK.getHeight()) / mOriginHeight) / 2;
            src = new Rect(0, I_Delta, bmpBK.getWidth(), bmpBK.getHeight() - I_Delta);
            dst = new Rect(0, 0, mCurrentWidth, mCurrentHeight);
            canvas.drawBitmap(bmpBK, src, dst, mPaint);

            if(mImages.size() > 1 && (mAnimCnt > 0 && mAnimCnt <= 255)) {   // animation 도중
                bmpBK = mImages.get(getNextImg());
                bkColor = 0x00111111 + (mAnimCnt << 24);
                mPaint.setColor(bkColor);
                I_Delta = ((curDelta * bmpBK.getHeight()) / mOriginHeight) / 2;
                src = new Rect(0, I_Delta, bmpBK.getWidth(), bmpBK.getHeight() - I_Delta);
                canvas.drawBitmap(bmpBK, src, dst, mPaint);
            }
        } else {                                                  // 그릴 이미지가 없음
            mPaint.setColor(0xff111111);
            canvas.drawRect(0, 0, mCurrentWidth, mCurrentHeight, mPaint);
        }

        bkColor = 0x00212121;
        int percentage = (255 * (mOriginHeight - mCurrentHeight)) / mFlexableHeight;
        bkColor += percentage << 24;
        mPaint.setColor(bkColor);
        canvas.drawRect(0, 0, mCurrentWidth, mCurrentHeight, mPaint);

        // draw btns
        mDrawableBack.draw(canvas);

        if(mMapExist)
            mDrawableMap.draw(canvas);
    }

    private void checkkBtnArea(Point pt) {
        mBtnState = BTN_STATE_NOTHING;
        if(CommonUtils.isPointInRect(pt, mRectBackBtn)) {
            mBtnState = BTN_STATE_BACK;
        } else if(CommonUtils.isPointInRect(pt, mRectMapBtn)) {
            mBtnState = BTN_STATE_MAP;
        }
    }

    public void setTouchEvent(MotionEvent event, Point pt) {
        pt.y -= mTitleHeight;
        if(pt.y > mCurrentHeight)
            return;

        float dX;
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                checkkBtnArea(pt);
                break;

            case MotionEvent.ACTION_MOVE:
                checkkBtnArea(pt);
                break;
            case MotionEvent.ACTION_UP:
                switch(mBtnState) {
                    case BTN_STATE_BACK:
                        mListener.onClickBack();
                        break;
                    case BTN_STATE_MAP:
                        if(mMapExist)
                            mListener.onClickMap();
                        break;
                }
                mBtnState = BTN_STATE_NOTHING;
                break;
        }
        invalidate();
    }
}
