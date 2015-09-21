package jinryulkim.k_mountain;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import javax.xml.transform.Result;

import jinryulkim.k_mountain.My.MyActivity;
import jinryulkim.k_mountain.result.CardView;
import jinryulkim.k_mountain.result.ListAdapter;
import jinryulkim.k_mountain.result.ResultActivity;

/**
 * Created by jinryulkim on 15. 8. 28..
 */
public class CardTouchListener implements View.OnTouchListener {
    private CardView mCardView = null;
    private VelocityTracker mVT = null;
    private int mSlop;
    private int mMinFlingVelocity, mMaxFlingVelocity;
    private long mAnimationTime;
    private float mStartX = 0;
    private int mSwipeDistanceDivisor = 2;
    private float mTranslationX;
    private int mViewWidth = 1;
    private boolean mSwipe = false;
    private int mListType = ListAdapter.LIST_TYPE_RESULT;

    public interface CardTouchCallback {
        void onDismiss(MtInfo_General info, int position);
        void onClickBtn(MtInfo_General info, int position, int btnId, Object obj);
    }

    CardTouchCallback mCallback = null;

    public CardTouchListener(CardView cardView, CardTouchCallback callback, int listType) {
        mCardView = cardView;
        mCallback = callback;
        mListType = listType;

        ViewConfiguration vc = ViewConfiguration.get(mCardView.getContext());
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 16;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = mCardView.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        motionEvent.offsetLocation(mTranslationX, 0);
        if(mViewWidth < 2) {
            mViewWidth = mCardView.getWidth();
        }

        float dX;
        switch(motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mStartX = motionEvent.getRawX();
                mVT = VelocityTracker.obtain();
                mVT.addMovement(motionEvent);
                view.onTouchEvent(motionEvent);
                return true;
            case MotionEvent.ACTION_MOVE:
                if(mVT == null)
                    break;
                mVT.addMovement(motionEvent);
                dX = motionEvent.getRawX() - mStartX;
                if(Math.abs(dX) > mSlop) {
                    mSwipe = true;
                    mCardView.getParent().requestDisallowInterceptTouchEvent(true);
                    MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (motionEvent.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    mCardView.onTouchEvent(cancelEvent);
                    cancelEvent.recycle();
                }

                if(mSwipe == true) {
                    mTranslationX = dX;
                    mCardView.setTranslationX(dX);
                    mCardView.setAlpha(Math.max(0.f, Math.min(1.f, 1.f - 2.f * Math.abs(dX) / mViewWidth)));
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
                if(mVT == null)
                    break;
                dX = motionEvent.getRawX() - mStartX;
                mVT.addMovement(motionEvent);
                mVT.computeCurrentVelocity(1000);
                float vX = mVT.getXVelocity();
                float absVX = Math.abs(vX);
                float absVY = Math.abs(mVT.getYVelocity());
                boolean dismiss = false;
                boolean dismissRight = false;

                if(Math.abs(dX) > mViewWidth / mSwipeDistanceDivisor) {
                    dismiss = true;
                    dismissRight = dX > 0;
                } else if(mMinFlingVelocity <= absVX && absVX <= mMaxFlingVelocity && absVY < absVX) {
                    dismiss = (vX < 0) == (dX < 0);
                    dismissRight = vX > 0;
                }

                if(dismiss)
                    doDismiss(dismissRight);


                mVT.recycle();
                mVT = null;
                mStartX = 0;
                mSwipe = false;
                break;
        }
        return false;
    }

    public void doDismiss(boolean dismissRight) {
        boolean cardDismiss = false;
        switch(mListType) {
            case ListAdapter.LIST_TYPE_RESULT:
                cardDismiss = ResultActivity.mCardDismissingNow;
                break;
            case ListAdapter.LIST_MY:
                cardDismiss = MyActivity.mCardDismissingNow;
        }
        if(cardDismiss == false) {
            switch(mListType) {
                case ListAdapter.LIST_TYPE_RESULT:
                    ResultActivity.mCardDismissingNow = true;
                    break;
                case ListAdapter.LIST_MY:
                    MyActivity.mCardDismissingNow = true;
                    break;
            }

            if(mViewWidth < 2) {
                mViewWidth = mCardView.getWidth();
            }

            mCardView.animate()
                    .translationX(dismissRight ? mViewWidth : -mViewWidth)
                    .alpha(0)
                    .setDuration(mAnimationTime)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animator) {
                            performDismiss();
                        }
                    });
        } else {
            mCardView.animate()
                    .translationX(0)
                    .alpha(1)
                    .setDuration(mAnimationTime)
                    .setListener(null);
        }
    }

    private void performDismiss() {
        // View의 높이를 0으로 애니메이션 한 후 callback을 호출 한다.
        final ViewGroup.LayoutParams lp =mCardView.getLayoutParams();
        final int originalHeight = mCardView.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(mAnimationTime);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // 실제 데이터를 삭제할 수 있도록 callback
                if(mCallback != null)
                    mCallback.onDismiss(mCardView.getMtInfo(), mCardView.getPosition());
                // 원래 상태로 원복, 그러지 않으면 해당 카드가 재활용 되었을 때 계속해서 보이지 않게 됨
                mCardView.setAlpha(1);
                mCardView.setTranslationX(0);
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                mCardView.setLayoutParams(lp);
            }
        });
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                lp.height = (Integer)valueAnimator.getAnimatedValue();
                mCardView.setLayoutParams(lp);
            }
        });
        animator.start();
    }

}
