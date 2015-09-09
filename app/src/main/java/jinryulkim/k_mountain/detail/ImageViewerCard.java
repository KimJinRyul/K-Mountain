package jinryulkim.k_mountain.detail;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

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
    private Rect mRect, mRectBackBtn, mRectShareBtn;
    private Paint mPaint;
    private ArrayList<Bitmap> mImages = new ArrayList<Bitmap>();
    private int mCurImage = 0;
    private static Bitmap mBmpBK = null;
    private static Drawable mDrawableBack = null, mDrawableBackPressed = null, mDrawableShare = null, mDrawableSharePressed = null;
    private DetailAdapter.CardListener mListener = null;

    private MtInfo_General mInfo;
    private int mInfoPosition;

    private VelocityTracker mVT = null;
    private float mStartX = 0;
    private int mSwipeDistanceDivisor = 2;
    private boolean mSwipe = false;
    private int mSlop;
    private int mMinFlingVelocity, mMaxFlingVelocity;
    private int mTranslationX = 0;

    private final static int BTN_STATE_NOTHING = 0;
    private final static int BTN_STATE_BACK = 1;
    private final static int BTN_STATE_SHARE = 2;

    public ImageViewerCard(Context context, DetailAdapter.CardListener listener) {
        super(context);
        mContext = context;
        mListener = listener;
        init();
    }

    public static void loadImages(Context context) {
        if(mDrawableBack == null) {
            mBmpBK = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_image);
            mDrawableBack = context.getResources().getDrawable(R.drawable.ic_arrow_back_white_48dp);
            mDrawableBackPressed = context.getResources().getDrawable(R.drawable.ic_arrow_back_grey600_48dp);
            mDrawableShare = context.getResources().getDrawable(R.drawable.ic_share_white_48dp);
            mDrawableSharePressed = context.getResources().getDrawable(R.drawable.ic_share_grey600_48dp);
        }
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

        Log.i("jrkim", "mImage.size():" + mImages.size());
        if(mImages.size() <= 0) {
            mImages.add(mBmpBK);
        }
        Log.i("jrkim", "-> mImage.size():" + mImages.size());
    }

    /*
    private Bitmap loadBitmapWithSampling(String path, int targetW, int targetH, Bitmap.Config bmConfig) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        int photoW = options.outWidth;
        int photoH = options.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        options.inPreferredConfig = bmConfig;
        options.inJustDecodeBounds = false;
        options.inSampleSize = scaleFactor;
        options.inPurgeable = true;

        return BitmapFactory.decodeFile(path, options);
    }*/

    private void init() {
        mOriginHeight = mOriginHeightTemp = mCurrentHeight = mCurrentWidth = mFlexableHeight = 0;
        mDeltaY = 0;
        mTitleHeight = 0;
        mRect = new Rect(0, 0, 0, 0);
        mRectBackBtn = new Rect(0, 0, 0, 0);
        mRectShareBtn = new Rect(0, 0, 0, 0);
        mPaint = new Paint();
        mPaint.setTypeface(CommonUtils.typeface);

        ViewConfiguration vc = ViewConfiguration.get(mContext);
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
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
            mRectShareBtn = new Rect(mCurrentWidth - margin - width, margin, mCurrentWidth - margin, margin + width);

            mFlexableHeight = mOriginHeight - (margin + width + margin);

            mDrawableBack.setBounds(mRectBackBtn);
            mDrawableBackPressed.setBounds(mRectBackBtn);
            mDrawableShare.setBounds(mRectShareBtn);
            mDrawableSharePressed.setBounds(mRectShareBtn);

            mFixed = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if(mCurrentWidth <= 0 || mCurrentHeight <= 0 || mPaint == null || mFixed == false)
            return;

        // draw image
        mPaint.setColor(0xff212121);
        Rect src, dst;
        if(mImages.size() > 0 && mCurImage <= mImages.size() - 1) { // 그릴 이미지가 있음
            Bitmap bmpBK = mImages.get(mCurImage);
            int curDelta = mOriginHeight - mCurrentHeight;
            int I_Delta = ((curDelta * bmpBK.getHeight()) / mOriginHeight) / 2;
            src = new Rect(0, I_Delta, bmpBK.getWidth(), bmpBK.getHeight() - I_Delta);
            dst = new Rect(mTranslationX, 0, mCurrentWidth + mTranslationX, mCurrentHeight);
            canvas.drawBitmap(bmpBK, src, dst, mPaint);
        } else {                                                  // 그릴 이미지가 없음
            canvas.drawRect(0, 0, mCurrentWidth, mCurrentHeight, mPaint);
        }

        int bkColor = 0x00212121;
        int percentage = (255 * (mOriginHeight - mCurrentHeight)) / mFlexableHeight;
        bkColor += percentage << 24;
        mPaint.setColor(bkColor);
        canvas.drawRect(0, 0, mCurrentWidth, mCurrentHeight, mPaint);

        // draw btns
        if(mBtnState == BTN_STATE_BACK)     mDrawableBackPressed.draw(canvas);
        else                                mDrawableBack.draw(canvas);

        if(mBtnState == BTN_STATE_SHARE)   mDrawableSharePressed.draw(canvas);
        else                                mDrawableShare.draw(canvas);
    }

    private void checkkBtnArea(Point pt) {
        mBtnState = BTN_STATE_NOTHING;
        if(CommonUtils.isPointInRect(pt, mRectBackBtn)) {
            mBtnState = BTN_STATE_BACK;
        } else if(CommonUtils.isPointInRect(pt, mRectShareBtn)){
            mBtnState = BTN_STATE_SHARE;
        }
    }

    public void setTouchEvent(MotionEvent event, Point pt) {
        pt.y -= mTitleHeight;
        if(pt.y > mCurrentHeight)
            return;

        float dX;
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartX = event.getRawX();
                mVT = VelocityTracker.obtain();
                mVT.addMovement(event);
                checkkBtnArea(pt);
                break;

            case MotionEvent.ACTION_MOVE:
                checkkBtnArea(pt);
                if(mVT == null)
                    break;
                mVT.addMovement(event);
                dX = event.getRawX() - mStartX;
                if(Math.abs(dX) > mSlop) {
                    mSwipe = true;
                }

                if(mSwipe == true) {
                    mTranslationX = (int)dX;
                }
                break;
            case MotionEvent.ACTION_UP:
                switch(mBtnState) {
                    case BTN_STATE_BACK:
                        mListener.onClickBack();
                        break;
                    case BTN_STATE_SHARE:
                        mListener.onClickShare();
                        break;
                }
                mBtnState = BTN_STATE_NOTHING;
                if(mVT == null)
                    break;
                /*dX = event.getRawX() - mStartX;
                mVT.addMovement(event);
                mVT.computeCurrentVelocity(1000);
                float vX = mVT.getXVelocity();
                float absVX = Math.abs(vX);
                float absVY = Math.abs(mVT.getYVelocity());
                boolean change = false;
                boolean next = false;

                if(Math.abs(dX) > mCurrentWidth / mSwipeDistanceDivisor) {
                    change = true;
                    next = dX > 0;
                } else if(mMinFlingVelocity <= absVX && absVX <= mMaxFlingVelocity && absVY < absVX) {
                    change = (vX < 0) == (dX < 0);
                    next = vX > 0;
                }

                if(change == true) {
                    // next...
                } else {
                    // 원래자리로 돌아가자..
                }*/
                mVT.recycle();
                mVT = null;
                mTranslationX = 0;
                mStartX = 0;
                mSwipe = false;
                break;
        }
        invalidate();
    }
}
