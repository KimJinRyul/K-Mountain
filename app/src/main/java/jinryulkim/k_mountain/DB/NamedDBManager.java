package jinryulkim.k_mountain.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
            db.execSQL(NamedDBConst._CREATE_GEN);
            db.execSQL(NamedDBConst._CREATE_NAMED);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXIST " + NamedDBConst._GEN_TABLE);
            db.execSQL("DROP TABLE IF EXIST " + NamedDBConst._NAMED_TABLE);
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

    public void openReadonly(String dbPath) throws SQLException {
        if(mDB == null) {
            //mHelper = new DBHelper(mContext, NamedDBConst.DB_NAME, null, DB_VERSION);
            //mDB = mHelper.getReadableDatabase();
            mDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
        }
    }

    public void close() {
        if(mDB != null) {
            mDB.close();
            mDB = null;
        }
    }

    public Cursor getLike(String table, String type, String [] item) {
        return mDB.rawQuery("SELECT * FROM " + table + " WHERE " + type + " LIKE ?", item);
    }

    public Cursor get(String table, String type, String [] item) {
        return mDB.rawQuery("SELECT * FROM " + table+ " WHERE " + type + " = ?", item);
    }

    public Cursor getAll(String table) {
        return mDB.query(table, null, null, null, null, null, null);
    }

    public boolean isExistInDB(String table, String type, String [] item) {
        boolean bRet = false;
        Cursor cursor = get(table, type, item);
        if(cursor.getCount() > 0)
            bRet = true;
        cursor.close();
        return bRet;
    }

    public boolean insertGenDB(String code, String name, String sname,
                               String address, String high, String admin, String adminNum,
                               String imagePaths, String summary, String detail) {

        if(isExistInDB(NamedDBConst._GEN_TABLE, NamedDBConst.code, new String [] {code})) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(NamedDBConst.code, code != null ? code : "");
        values.put(NamedDBConst.name, name != null ? name : "");
        values.put(NamedDBConst.sname, sname != null ? sname : "");
        values.put(NamedDBConst.address, address != null ? address : "");
        values.put(NamedDBConst.high, high != null ? high : "");
        values.put(NamedDBConst.admin, admin != null ? admin : "");
        values.put(NamedDBConst.adminNum, adminNum != null ? adminNum : "");
        values.put(NamedDBConst.imagePaths, imagePaths != null ? imagePaths : "");
        values.put(NamedDBConst.summary, summary != null ? summary : "");
        values.put(NamedDBConst.detail, detail != null ? detail : "");

        mDB.insert(NamedDBConst._GEN_TABLE, null, values);


        Cursor cursor = getAll(NamedDBConst._GEN_TABLE);
        cursor.close();

        return true;
    }

    public boolean insertNamedDB(String mntNm, String subNm, String mntnCd, String areaNm, String mntHeight,
                            String areaReason, String overView, String details, String tpTitl, String tpContent,
                            String transport, String tourismInf, String etcCourse, String flashUrl, String videoUrl) {

        if(isExistInDB(NamedDBConst._NAMED_TABLE, NamedDBConst.mntnCd, new String [] {mntnCd})) {
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

        mDB.insert(NamedDBConst._NAMED_TABLE, null, values);


        Cursor cursor = getAll(NamedDBConst._NAMED_TABLE);
        cursor.close();

        return true;
    }
}
