package jinryulkim.k_mountain.result;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import java.util.ArrayList;

import jinryulkim.k_mountain.CardTouchListener;
import jinryulkim.k_mountain.MtInfoMgr;
import jinryulkim.k_mountain.MtInfo_General;

/**
 * Created by jinryulkim on 15. 8. 27..
 */
public class ListAdapter extends BaseAdapter {

    private Context mContext = null;
    private LayoutInflater mInflater = null;
    private static MainUICard mMainUICard = null;
    private CardListener mListener = null;
    private ArrayList<MtInfo_General> mtInfos = null;


    public interface CardListener {
        void onCardRemoved(MtInfo_General info, int position);
        void onClickCard(MtInfo_General info, int position, int btnID);
        void onClickBack();
        void onClickSearch();
    }

    private CardTouchListener.CardTouchCallback mCallback = new CardTouchListener.CardTouchCallback() {
        @Override
        public void onDismiss(MtInfo_General info, int position) {
            mListener.onCardRemoved(info, position);
        }

        @Override
        public void onClickBtn(MtInfo_General info, int pos, int btnId) {
            mListener.onClickCard(info, pos, btnId);
        }
    };

    private final static int MAINUI = 0;
    private final static int CARD = 1;

    public ListAdapter(Context context, CardListener listener, ArrayList<MtInfo_General> infos) {
        mContext = context;
        mtInfos = infos;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mListener = listener;
    }

    public void startLoading() {
        ((MtInfo_General)getItem(getCount() - 1)).cardview.startLoading();
    }

    public void stopLoading() {
        ((MtInfo_General)getItem(getCount() - 1)).cardview.stopLoading();
    }

    public void setData(ArrayList<MtInfo_General> infos) {
        mtInfos = infos;
    }

    public static MainUICard getMainUICard() {
        return mMainUICard;
    }
    public static void removeMainUICard() { mMainUICard = null;}

    public void setStatusBarHeight(int height) {
        if (mMainUICard != null) {
            try {
                mMainUICard.setTitleHeight(height);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getCount() {
        return mtInfos.size();
    }

    @Override
    public Object getItem(int position) {
        if(mtInfos != null && position < mtInfos.size())
            return mtInfos.get(position);
        else
            return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MtInfo_General info = (MtInfo_General)getItem(position);
        if(info != null) {
            try {
                if(position == 0) { // MainUICard
                    if(mMainUICard == null) {
                        mMainUICard = new MainUICard(mContext, mListener);

                        int width = parent.getWidth();
                        int height = (width * 9) / 16;
                        mMainUICard.setLayoutParams(new AbsListView.LayoutParams(width, height));
                        mMainUICard.setTag(MAINUI);
                    }

                    if(mMainUICard != null) {
                        if(convertView == null || (Integer)convertView.getTag() != MAINUI)
                            convertView = mMainUICard;
                    }
                } else { // Normal
                    if(convertView == null || (Integer)convertView.getTag() != CARD) {
                        convertView = new CardView(mContext, mCallback);
                        convertView.setTag(CARD);
                    }

                    if(convertView != null) {
                        // 기존에 참조하고 있던 정보를 초기화 하야 준다.
                        for(int i = 0; i < MtInfoMgr.mMtInfos.size(); i++) {
                            if(MtInfoMgr.mMtInfos.get(i).cardview == convertView) {
                                MtInfoMgr.mMtInfos.get(i).cardview = null;
                                break;
                            }
                        }

                        ((CardView)convertView).setMtInfo(info, position);
                        info.cardview = (CardView)convertView;

                        if(info.animated == false) {
                            ((CardView)convertView).startAnimationWithDelay(0);
                            info.animated = true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return convertView;
    }
}
