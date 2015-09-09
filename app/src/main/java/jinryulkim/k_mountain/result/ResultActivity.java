package jinryulkim.k_mountain.result;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import jinryulkim.k_mountain.CommonUtils;
import jinryulkim.k_mountain.detail.DetailActivity;
import jinryulkim.k_mountain.MtInfoMgr;
import jinryulkim.k_mountain.MtInfo_General;
import jinryulkim.k_mountain.MtOpenAPIMgr;
import jinryulkim.k_mountain.R;
import jinryulkim.k_mountain.map.MapActivity;

/**
 * Created by jinryulkim on 15. 8. 27..
 */
public class ResultActivity extends Activity implements ListAdapter.CardListener,
                                                        PinnedHeaderListView.PinnedHeaderListViewListener,
        MtOpenAPIMgr.MtOpenAPIMgrListener
{
    private ListAdapter mListAdapter = null;
    private PinnedHeaderListView mPinnedHeaderListView = null;
    private ArrayList<MtInfo_General> mMtInfos = null;
    public static boolean mCardDismissingNow = false;
    public static boolean mBNowSearching = false;

    private static ResultHandler mHandler = null;

    static class ResultHandler extends Handler {
        private final WeakReference<ResultActivity> mActivity;
        ResultHandler(ResultActivity activity) {
            mActivity = new WeakReference<ResultActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ResultActivity activity = mActivity.get();
            if(activity != null)
                activity.handleMessage(msg);
        }
    };

    private final static int MESSAGE_LOAD_COMPLETE  = 1000;
    private final static int MESSAGE_FINISH         = 1001;
    public void handleMessage(Message msg) {
        switch(msg.what) {
            case MESSAGE_LOAD_COMPLETE:
                mListAdapter.stopLoading();
                mMtInfos = (ArrayList<MtInfo_General>) MtInfoMgr.mMtInfos.clone();
                mListAdapter.setData(mMtInfos);
                mListAdapter.notifyDataSetChanged();
                mBNowSearching = false;
                break;
            case MESSAGE_FINISH:
                finish();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(MtInfoMgr.mMtInfos == null || MtInfoMgr.mMtInfos.size() <= 0)
            finish();

        setContentView(R.layout.activity_result);

        CommonUtils.setGlobalFont(getWindow().getDecorView(), CommonUtils.typeface);

        mHandler = new ResultHandler(this);

        mMtInfos = (ArrayList<MtInfo_General>)MtInfoMgr.mMtInfos.clone();
        mListAdapter = new ListAdapter(this, this, mMtInfos);

        mPinnedHeaderListView = (PinnedHeaderListView) findViewById(R.id.lvResults);
        mPinnedHeaderListView.setSmoothScrollbarEnabled(true);
        mPinnedHeaderListView.setAdapter(mListAdapter);
        mPinnedHeaderListView.setVisibility(View.VISIBLE);
        mPinnedHeaderListView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mPinnedHeaderListView.getViewTreeObserver().removeOnPreDrawListener(this);
                mListAdapter.setStatusBarHeight(CommonUtils.getStatusBarHeight(getApplicationContext()));
                return false;
            }
        });
        mPinnedHeaderListView.setListener(this);
    }

    private void searchMore() {
        if(mBNowSearching == false && MtInfoMgr.totalCnt > MtInfoMgr.mMtInfos.size() + MtInfoMgr.deletedCnt) {
            mBNowSearching = true;
            mListAdapter.startLoading();
            MtOpenAPIMgr.setListener(this);
            MtOpenAPIMgr.requestGeneralInfo(this, MtInfoMgr.pageUnit, MtInfoMgr.pageIndex + 1, MtInfoMgr.searchWrd, false);
        }
    }

    @Override
    public void onCardRemoved(MtInfo_General info, int position) {
        MtInfoMgr.mMtInfos.remove(position);
        MtInfoMgr.deletedCnt++;
        mMtInfos.remove(position);
        mListAdapter.notifyDataSetChanged();
        mCardDismissingNow = false;
    }

    @Override
    public void onClickCard(MtInfo_General info, int position, int btnID) {
        switch(btnID) {
            case CardView.BTN_ID_SEARCH_MORE:
                searchMore();
                break;
            case CardView.BTN_ID_DETAIL:
                Intent intent = new Intent(this, DetailActivity.class);
                intent.putExtra(DetailActivity.EXTRA_POSITION, position);
                startActivity(intent);
                break;
            case CardView.BTN_ID_SHARE:
                startActivity(new Intent(this, MapActivity.class));
                break;
        }
    }

    @Override
    public void onClickBack() {
        mHandler.sendEmptyMessage(MESSAGE_FINISH);
    }

    @Override
    public void onClickSearch() {
        Toast.makeText(this, "SEARCH", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onScrollLimit() {
        searchMore();
    }

    @Override
    public void onRequestGeneralMtInfoStarted() {
    }

    @Override
    public void onRequestGeneralMtInfoCompleted() {
        mHandler.sendEmptyMessage(MESSAGE_LOAD_COMPLETE);
    }

    @Override
    public void onRequestGeneralMtInfoError() {
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
        ListAdapter.removeMainUICard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ListAdapter.removeMainUICard();
    }
}