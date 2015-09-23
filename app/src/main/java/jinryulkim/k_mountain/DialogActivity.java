package jinryulkim.k_mountain;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by jinryulkim on 15. 9. 11..
 */
public class DialogActivity extends Activity implements View.OnClickListener {

    private final static String EXTRA_DLGTYPE        = "extra_dlgtype";

    public final static int DLGTYPE_NOTHING     = -1;
    public final static int DLGTYPE_GPSOFF      = 1;

    public final static int RESCODE_NEGATIVE = -1;
    public final static int RESCODE_POSITIVE = 1;

    private int mDlgType = DLGTYPE_NOTHING;
    private boolean mbAnswered = false;
    public static boolean mbLaunched = false;

    public static void launchDialog(Context context, int type, int reqCode) {
        Intent i = new Intent(context, DialogActivity.class);
        i.putExtra(EXTRA_DLGTYPE, type);
        ((Activity)context).startActivityForResult(i, reqCode);
        ((Activity)context).overridePendingTransition(R.anim.zoom_enter, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dlg);

        mbLaunched = true;

        CommonUtils.typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        CommonUtils.setGlobalFont(getWindow().getDecorView(), CommonUtils.typeface);

        findViewById(R.id.btnNegative).setOnClickListener(this);
        findViewById(R.id.btnPositive).setOnClickListener(this);

        mDlgType = getIntent().getIntExtra(EXTRA_DLGTYPE, DLGTYPE_NOTHING);

        switch(mDlgType) {
            case DLGTYPE_GPSOFF:
                ((TextView)findViewById(R.id.tvTitle)).setText(R.string.DLG_GPSOFF_TITLE);
                ((TextView)findViewById(R.id.tvDescription)).setText(R.string.DLG_GPSOFF_DESCRIPTION);
                ((Button)findViewById(R.id.btnNegative)).setText(R.string.DLG_GPSOFF_BTNNEGATIVE);
                ((Button)findViewById(R.id.btnPositive)).setText(R.string.DLG_GPSOFF_BTNPOSITIVE);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(mbAnswered == false) {
            setResult(RESCODE_NEGATIVE);
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnNegative:
                setResult(RESCODE_NEGATIVE);
                break;
            case R.id.btnPositive:
                setResult(RESCODE_POSITIVE);
                break;
        }
        finish();
    }

    @Override
    public void finish() {
        mbLaunched = false;
        super.finish();
        overridePendingTransition(0, R.anim.zoom_exit);
    }
}
