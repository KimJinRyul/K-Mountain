package jinryulkim.k_mountain.named100;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import jinryulkim.k_mountain.CommonUtils;
import jinryulkim.k_mountain.DB.MyDBManager;
import jinryulkim.k_mountain.DB.NamedDBConst;
import jinryulkim.k_mountain.DB.NamedDBManager;
import jinryulkim.k_mountain.MtInfoMgr;
import jinryulkim.k_mountain.MtInfo_General;
import jinryulkim.k_mountain.MtInfo_Named;
import jinryulkim.k_mountain.R;
import jinryulkim.k_mountain.detail.DetailActivity;
import jinryulkim.k_mountain.detail.ImageViewerCard;
import jinryulkim.k_mountain.result.CardView;
import jinryulkim.k_mountain.result.ListAdapter;
import jinryulkim.k_mountain.result.MainUICard;

/**
 * Created by jinryulkim on 15. 9. 18..
 */
public class Named100Activity extends Activity implements ListAdapter.CardListener, View.OnClickListener {
    private ListAdapter mListAdapter = null;
    private ListView mListView = null;
    private ArrayList<MtInfo_General>mMtInfo = new ArrayList<MtInfo_General>();

    private static Named100Handler mHandler = null;
    private final static int MESSAGE_LOAD_FINISHED  = 1000;
    private final static int MESSAGE_LOADING        = 1001;
    @Override
    public void onCardRemoved(MtInfo_General info, int position) {
    }

    @Override
    public void onClickCard(MtInfo_General info, int position, int btnID, Object obj) {
        Intent intent = null;
        MyDBManager myDB = null;
        switch(btnID) {
            case CardView.BTN_ID_DETAIL:
                intent = new Intent(this, DetailActivity.class);
                intent.putExtra(DetailActivity.EXTRA_POSITION, position);
                startActivity(intent);
                break;
            case CardView.BTN_ID_SHARE:
                CommonUtils.launchShare(this, info);
                break;
            case CardView.BTN_ID_ADDMY:
                myDB = MyDBManager.getInstance(this);
                myDB.open();
                myDB.insert(info.code);
                myDB.close();
                break;
            case CardView.BTN_ID_DELMY:
                myDB = MyDBManager.getInstance(this);
                myDB.open();
                myDB.delete(info.code);
                myDB.close();
                break;
        }
    }

    @Override
    public void onClickBack() {
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnBack:
                finish();
                break;
        }
    }

    static class Named100Handler extends Handler {
        private final WeakReference<Named100Activity> mActivity;
        Named100Handler(Named100Activity activity) {
            mActivity = new WeakReference<Named100Activity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            Named100Activity activity = mActivity.get();
            if(activity != null)
                activity.handleMessage(msg);
        }
    };

    public void handleMessage(Message msg) {
        switch(msg.what) {
            case MESSAGE_LOADING:
            case MESSAGE_LOAD_FINISHED:
                mMtInfo = (ArrayList<MtInfo_General>) MtInfoMgr.mMtInfos.clone();
                mListAdapter.setData(mMtInfo);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_named100);
        CommonUtils.setGlobalFont(getWindow().getDecorView(), CommonUtils.typeface);
        mHandler = new Named100Handler(this);

        findViewById(R.id.btnBack).setOnClickListener(this);

        MtInfoMgr.mMtInfos.clear();
        mMtInfo.clear();

        mListView = (ListView)findViewById(R.id.lvResults);
        mListAdapter = new ListAdapter(this, this, mMtInfo, false, ListAdapter.LIST_100);
        mListView.setAdapter(mListAdapter);

        loadThread();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    private void loadThread() {

        new Thread() {
            @Override
            public void run() {
                try {
                    MainUICard.loadImages(getApplicationContext());
                    ImageViewerCard.loadImages(getApplicationContext());

                    NamedDBManager db = NamedDBManager.getInstance(Named100Activity.this);
                    db.openReadonly(CommonUtils.getDBPath(Named100Activity.this));

                    Cursor namedC = db.getAll(NamedDBConst._NAMED_TABLE);
                    Cursor curGen;
                    while (namedC.moveToNext()) {
                        MtInfo_Named named = new MtInfo_Named();
                        named.tpTitle = new ArrayList<String>();
                        named.tpContent = new ArrayList<String>();
                        named.name = namedC.getString(namedC.getColumnIndex(NamedDBConst.mntNm)).trim();
                        named.sname = namedC.getString(namedC.getColumnIndex(NamedDBConst.subNm)).trim();
                        named.code = namedC.getString(namedC.getColumnIndex(NamedDBConst.mntnCd)).trim();
                        named.area = namedC.getString(namedC.getColumnIndex(NamedDBConst.areaNm)).trim();
                        named.height = namedC.getString(namedC.getColumnIndex(NamedDBConst.mntHeight)).trim();
                        named.reason = namedC.getString(namedC.getColumnIndex(NamedDBConst.areaReason)).trim();
                        named.overview = namedC.getString(namedC.getColumnIndex(NamedDBConst.overView)).trim();
                        named.details = namedC.getString(namedC.getColumnIndex(NamedDBConst.details)).trim();
                        String tpTitles = namedC.getString(namedC.getColumnIndex(NamedDBConst.tpTitl)).trim();
                        String tpContents = namedC.getString(namedC.getColumnIndex(NamedDBConst.tpContent)).trim();

                        if (tpTitles.length() > 0 && tpTitles.indexOf("|") > 0) {
                            String[] tmp = tpTitles.split("\\|");
                            for (int k = 0; k < tmp.length; k++)
                                named.tpTitle.add(tmp[k]);
                        } else
                            named.tpTitle.add(tpTitles);

                        if (tpContents.length() > 0 && tpContents.indexOf("|") > 0) {
                            String[] tmp = tpContents.split("\\|");
                            for (int k = 0; k < tmp.length; k++)
                                named.tpContent.add(tmp[k]);
                        } else
                            named.tpContent.add(tpContents);

                        named.transport = namedC.getString(namedC.getColumnIndex(NamedDBConst.transport)).trim();
                        named.tourismInfo = namedC.getString(namedC.getColumnIndex(NamedDBConst.tourismInf)).trim();
                        named.etcCource = namedC.getString(namedC.getColumnIndex(NamedDBConst.etcCourse)).trim();
                        named.flahsUrl = namedC.getString(namedC.getColumnIndex(NamedDBConst.flashUrl)).trim();
                        named.videoUrl = namedC.getString(namedC.getColumnIndex(NamedDBConst.videoUrl)).trim();

                        curGen = db.get(NamedDBConst._GEN_TABLE, NamedDBConst.code, new String[]{named.code});
                        if (curGen.getCount() == 1) {
                            curGen.moveToFirst();

                            MtInfo_General mtInfo = new MtInfo_General();
                            mtInfo.imagePaths = new ArrayList<String>();
                            mtInfo.code = curGen.getString(curGen.getColumnIndex(NamedDBConst.code)).trim();
                            mtInfo.name = curGen.getString(curGen.getColumnIndex(NamedDBConst.name)).trim();
                            mtInfo.sname = curGen.getString(curGen.getColumnIndex(NamedDBConst.sname)).trim();
                            mtInfo.high = curGen.getString(curGen.getColumnIndex(NamedDBConst.high)).trim();
                            mtInfo.address = curGen.getString(curGen.getColumnIndex(NamedDBConst.address)).trim();
                            mtInfo.admin = curGen.getString(curGen.getColumnIndex(NamedDBConst.admin)).trim();
                            mtInfo.adminNum = curGen.getString(curGen.getColumnIndex(NamedDBConst.adminNum)).trim();
                            mtInfo.summary = curGen.getString(curGen.getColumnIndex(NamedDBConst.summary)).trim();
                            mtInfo.detail = curGen.getString(curGen.getColumnIndex(NamedDBConst.detail)).trim();
                            String imgPath = curGen.getString(curGen.getColumnIndex(NamedDBConst.imagePaths)).trim();
                            if (imgPath.length() > 0 && imgPath.indexOf("|") > 0) {
                                String[] tmp = imgPath.split("\\|");
                                for (int k = 0; k < tmp.length; k++) {
                                    mtInfo.imagePaths.add(tmp[k]);
                                }
                            } else {
                                if (imgPath.length() > 0) {
                                    mtInfo.imagePaths.add(imgPath);
                                }
                            }

                            mtInfo.namedInfo = named;
                            MtInfoMgr.mMtInfos.add(mtInfo);
                            mHandler.sendEmptyMessage(MESSAGE_LOADING);

                        } else {
                            // ??!?!?!?
                        }
                        curGen.close();
                    }
                    namedC.close();
                    db.close();
                } catch ( Exception e) {
                    e.printStackTrace();
                }

                mHandler.sendEmptyMessage(MESSAGE_LOAD_FINISHED);
            }
        }.start();
    }
}