package jinryulkim.k_mountain.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.SQLException;

/**
 * Created by jinryulkim on 15. 9. 9..
 */
public class NamedDBManager {
    private static NamedDBManager mInstance = null;
    private static SQLiteDatabase mDB = null;
    private static DBHelper mHelper = null;
    private Context mContext = null;

    private final static int DB_VERSION = 1;

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(NamedDBConst._CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXIST " + NamedDBConst._TABLE);
            onCreate(db);
        }
    }

    public static NamedDBManager getInstance(Context context) {
        synchronized (NamedDBManager.class) {
            if(mInstance == null)
                mInstance = new NamedDBManager(context);
        }
        return mInstance;
    }

    private NamedDBManager(Context context) { mContext = context; }

    public void openWritable() throws SQLException {
        if(mDB == null) {
            mHelper = new DBHelper(mContext, NamedDBConst.DB_NAME, null, DB_VERSION);
            mDB = mHelper.getWritableDatabase();
        }
    }

    public void openReadonly() throws SQLException {
        if(mDB == null) {
            mHelper = new DBHelper(mContext, NamedDBConst.DB_NAME, null, DB_VERSION);
            mDB = mHelper.getReadableDatabase();
        }
    }

    public void close() {
        if(mDB != null) {
            mDB.close();
            mDB = null;
        }
    }

    public Cursor get(String type, String [] item) {
        return mDB.rawQuery("SELECT * FROM " + NamedDBConst._TABLE + " WHERE " + type + " = ?", item);
    }

    public Cursor getAll() {
        return mDB.query(NamedDBConst._TABLE, null, null, null, null, null, null);
    }

    public boolean isExistInDB(String type, String [] item) {
        boolean bRet = false;
        Cursor cursor = get(type, item);
        if(cursor.getCount() > 0)
            bRet = true;
        cursor.close();
        return bRet;
    }

    public boolean insertDB(String mntNm, String subNm, String mntnCd, String areaNm, String mntHeight,
                            String areaReason, String overView, String details, String tpTitl, String tpContent,
                            String transport, String tourismInf, String etcCourse, String flashUrl, String videoUrl) {

        if(isExistInDB(NamedDBConst.mntnCd, new String [] {mntnCd})) {
            Log.e("jrkim", mntNm + "-" + mntnCd + " is aready exist in DB");
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(NamedDBConst.mntNm, mntNm);
        values.put(NamedDBConst.subNm, subNm);
        values.put(NamedDBConst.mntnCd, mntnCd);
        values.put(NamedDBConst.areaNm, areaNm);
        values.put(NamedDBConst.mntHeight, mntHeight);
        values.put(NamedDBConst.areaReason, areaReason);
        values.put(NamedDBConst.overView, overView);
        values.put(NamedDBConst.details, details);
        values.put(NamedDBConst.tpTitl, tpTitl);
        values.put(NamedDBConst.tpContent, tpContent);
        values.put(NamedDBConst.transport, transport);
        values.put(NamedDBConst.tourismInf, tourismInf);
        values.put(NamedDBConst.etcCourse, etcCourse);
        values.put(NamedDBConst.flashUrl, flashUrl);
        values.put(NamedDBConst.videoUrl, videoUrl);

        mDB.insert(NamedDBConst._TABLE, null, values);


        Cursor cursor = getAll();
        Log.i("jrkim", "inserted : " + cursor.getCount());
        cursor.close();

        return true;
    }
}
