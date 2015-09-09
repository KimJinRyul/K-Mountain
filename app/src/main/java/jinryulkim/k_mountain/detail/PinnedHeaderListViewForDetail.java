package jinryulkim.k_mountain.detail;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by jinryulkim on 15. 9. 2..
 */
public class PinnedHeaderListViewForDetail extends ListView implements AbsListView.OnScrollListener {

    private Context mContext = null;
    private DetailAdapter mAdapter = null;
    private boolean mTouchToMainUI = false;
    private int mSlop;
    private float mStartY = 0.f;

    public PinnedHeaderListViewForDetail(Context context) {
        super(context);
        init(context);
    }

    public PinnedHeaderListViewForDetail(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PinnedHeaderListViewForDetail(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @Override
    public void setAdapter(android.widget.ListAdapter adapter) {
        if(adapter != null) {
            mAdapter = (DetailAdapter)adapter;
            super.setAdapter(adapter);
        }
    }

    private void init(Context context) {
        mContext = context;
        ViewConfiguration vc = ViewConfiguration.get(mContext);
        mSlop = vc .getScaledTouchSlop();
        super.setOnScrollListener(this);
    }

    public void doFix() {
        ImageViewerCard ivc = mAdapter.getImageViewCard();
        if(ivc != null) {
            try {
                ivc.fixSizes();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if(mAdapter != null) {
            ImageViewerCard ivc = mAdapter.getImageViewCard();
            if(ivc != null) {
                try {
                    int save = canvas.save();
                    canvas.clipRect(0, 0, getWidth(), ivc.getCurrentHeight());
                    ivc.draw(canvas);
                    canvas.restoreToCount(save);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(mAdapter == null)
            return;

        ImageViewerCard ivc = mAdapter.getImageViewCard();
        if (ivc == null)
            return;

        try {
            ivc.fixSizes();
            int top = ivc.getTop();
            ivc.setDeltaY(top);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void setTouchEvent(MotionEvent event, Point pt) {
        ImageViewerCard ivc = mAdapter.getImageViewCard();
        if(ivc != null) {
            try {
                ivc.setTouchEvent(event, pt);
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
}
