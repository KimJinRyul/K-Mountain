package jinryulkim.k_mountain.result;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.*;

import jinryulkim.k_mountain.MtInfo_General;

/**
 * Created by jinryulkim on 15. 8. 28..
 */
public class PinnedHeaderListView extends ListView implements AbsListView.OnScrollListener {

    private Context mContext = null;

    private ListAdapter mAdapter = null;

    private boolean mFirstScroll = true;
    private boolean mLastItemVisible = false;
    private boolean mTouchToMainUI = false;

    private PinnedHeaderListViewListener mListener = null;

    private int mSlop;

    private float mStartY = 0.f;

    public interface PinnedHeaderListViewListener {
        void onScrollLimit();
    }

    public PinnedHeaderListView(Context context) {
        super(context);
        init(context);
    }

    public PinnedHeaderListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PinnedHeaderListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @Override
    public void setAdapter(android.widget.ListAdapter adapter) {
        if(adapter != null && adapter.getCount() > 0) {
            mAdapter = (ListAdapter)adapter;
            super.setAdapter(adapter);
        }
    }

    private void init(Context context) {
        mContext = context;
        ViewConfiguration vc = ViewConfiguration.get(mContext);
        mSlop = vc .getScaledTouchSlop();
        super.setOnScrollListener(this);
    }

    public void setListener(PinnedHeaderListViewListener listener) {
        mListener = listener;
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if(mAdapter != null) {
            MainUICard mainCard = mAdapter.getMainUICard();
            if(mainCard != null) {
                try {
                    int save = canvas.save();
                    canvas.clipRect(0, 0, getWidth(), mainCard.getCurrentHeight());
                    mainCard.draw(canvas);
                    canvas.restoreToCount(save);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setTouchEvent(MotionEvent event, Point pt) {
        MainUICard main = mAdapter.getMainUICard();
        if(main != null) {
            try {
                main.setTouchEvent(event.getAction(), pt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean res = super.onTouchEvent(event);
        float dY;
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchToMainUI = true;
                mStartY = event.getRawY();
                setTouchEvent(event, new Point((int)event.getRawX(), (int)event.getRawY()));
                return true;

            case MotionEvent.ACTION_MOVE:
                dY = event.getRawY() - mStartY;
                if(Math.abs(dY) > mSlop) {
                    mTouchToMainUI = false;
                    setTouchEvent(event, new Point(0, 0));
                } else if(mTouchToMainUI == true) {
                    setTouchEvent(event, new Point((int)event.getRawX(), (int)event.getRawY()));
                }
                return true;

            case MotionEvent.ACTION_UP:
                if(mTouchToMainUI == true) {
                    setTouchEvent(event, new Point((int)event.getRawX(), (int)event.getRawY()));
                }
                return false;
        }
        return res;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(mAdapter == null || mAdapter.getCount() < 2) {
            return;
        }

        int top = 0;
        try {
            top = mAdapter.getMainUICard().getTop();
        } catch (Exception e) {
            top = 0;
        }

        if(mFirstScroll == true) {
            mFirstScroll = false;
            try {
                mAdapter.getMainUICard().fixSizes();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            mAdapter.getMainUICard().setDeltaY(top);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mLastItemVisible = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);

        if(mAdapter.getCount() >= 2) {
            if(mLastItemVisible) {
                if(isLastCardExceedLimit()) {
                    setSelectionFromTop(getLastCardPositionWithoutEmpty(), getMainUICurrentHeight() + 10);
                    if (mListener != null)
                        mListener.onScrollLimit();
                }
            }
        }
    }

    public int getLastCardPositionWithoutEmpty() {
        if(mAdapter.getCount() > 2) {
            return mAdapter.getCount() - 2;
        } else if(mAdapter.getCount() == 2) {
            return mAdapter.getCount() - 1;
        }
        return -1;
    }

    public int getMainUICurrentHeight() {
        int res = 0;
        try {
            res = mAdapter.getMainUICard().getCurrentHeight();
        } catch (Exception e) {
            res = 0;
            e.printStackTrace();
        }
        return res;
    }

    public int getLastCardTop() {
        if(mAdapter.getCount() > 2) {
            MtInfo_General info = (MtInfo_General)mAdapter.getItem(getLastCardPositionWithoutEmpty());
            return info.cardview.getTop();
        }
        return 0;
    }

    public boolean isLastCardExceedLimit() {
        try {
            if(mLastItemVisible == true) {
                if(mAdapter.getCount() >= 2) {
                    MtInfo_General info = (MtInfo_General)mAdapter.getItem(getLastCardPositionWithoutEmpty());
                    int height = getMainUICurrentHeight();
                    if(info.cardview.getTop() < height + 10) {
                        return true;
                    }
                }
                return false;
            } else {
                return false;
            }
        }  catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(scrollState == OnScrollListener.SCROLL_STATE_IDLE && mLastItemVisible) {
            if(isLastCardExceedLimit()) {
                int height = getMainUICurrentHeight();
                setSelectionFromTop(getLastCardPositionWithoutEmpty(), height + 10);
            }
        }
    }
}
