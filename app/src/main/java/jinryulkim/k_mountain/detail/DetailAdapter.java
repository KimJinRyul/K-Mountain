package jinryulkim.k_mountain.detail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import jinryulkim.k_mountain.MtInfo_General;

/**
 * Created by jinryulkim on 15. 9. 2..
 */
public class DetailAdapter extends BaseAdapter {

    private Context mContext = null;
    private LayoutInflater mInflater = null;
    private MtInfo_General mInfo = null;
    private int mPosition = 0;
    private static ImageViewerCard mImageViewerCard = null;
    private CardListener mListener = null;
    private PinnedHeaderListViewForDetail mPHLV = null;

    public interface CardListener {
        void onClickBack();
        void onClickMap();
    }

    private final static int IMAGEVIEWER = 0;
    private final static int INFORMATION = 1;

    public DetailAdapter(Context context, MtInfo_General info, int position, CardListener listener) {
        mContext = context;
        mListener = listener;
        mInfo = info;
        mPosition = position;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setStatusBarHeight(int height) {
        if(mImageViewerCard != null) {
            try {
                mImageViewerCard.setTitleHeight(height);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static ImageViewerCard getImageViewCard() {
        return mImageViewerCard;
    }
    public static void removeImageViewCard() {mImageViewerCard = null;}
    public void setPinnedHeaderListViewForDetail(PinnedHeaderListViewForDetail phlv) {
        if(mImageViewerCard != null)
            mImageViewerCard.setPinnedHeaderListViewForDetail(phlv);
        else {
            mPHLV = phlv;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(mInfo != null) {
            try {
                if(position == 0) {
                    if(mImageViewerCard == null) {
                        mImageViewerCard = new ImageViewerCard(mContext, mListener);

                        if(mPHLV != null) {
                            mImageViewerCard.setPinnedHeaderListViewForDetail(mPHLV);
                            mPHLV = null;
                        }

                        int width = parent.getWidth();
                        int height = (width * 9) / 16;
                        mImageViewerCard.setLayoutParams(new AbsListView.LayoutParams(width, height));
                        mImageViewerCard.setTag(IMAGEVIEWER);
                        mImageViewerCard.setInformation(mInfo, mPosition);
                    }

                    if(mImageViewerCard != null) {
                        if(convertView == null || (Integer)convertView.getTag() != IMAGEVIEWER)
                            convertView = mImageViewerCard;
                    }
                } else if(position == 1) {
                    if(convertView == null || (Integer)convertView.getTag() != INFORMATION) {
                        convertView = new InfoCard(mContext);
                        convertView.setTag(INFORMATION);
                    }

                    if(convertView != null) {
                        ((InfoCard)convertView).setMtInfo(mInfo, mPosition);
                        ((InfoCard)convertView).startAnimation();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return convertView;
    }
}
