package jinryulkim.k_mountain.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

/**
 * Created by jinryulkim on 15. 9. 16..
 */
public class MyDBManager {
    private static MyDBManager mInstance = null;
    private static SQLiteDatabase mDB = null;
    private Context mContext = null;
    private static final String DB_NAME = "kmt_mydb";

    public static MyDBManager getInstance(Context context) {
        synchronized (MyDBManager.class) {
            if(mInstance == null) {
                mInstance = new MyDBManager(context);
            }
        }
        return mInstance;
    }

    private MyDBManager(Context context) {
        mContext = context;
    }

    public void open() {
        if(mDB == null) {
            try {
                File db = mContext.getDatabasePath(DB_NAME);
                if (db.exists() == false) {
                    File parent = db.getParentFile();
                    parent.mkdirs();
                    db.createNewFile();
                }
                mDB = SQLiteDatabase.openDatabase(db.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
                mDB.execSQL(NamedDBConst._CREATE_MY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        if(mDB != null) {
            mDB.close();
            mDB = null;
        }
    }

    public Cursor get(String [] item) {
        return mDB.rawQuery("SELECT * FROM " + NamedDBConst._MY_TABLE + " WHERE " + NamedDBConst.code + " = ?", item);
    }

    public Cursor getAll() {
        return mDB.query(NamedDBConst._MY_TABLE, null, null, null, null, null, null);
    }

    public void delete(String code) {
        String [] arg = new String [] {code};
        if(mDB != null && get(arg).getCount() > 0) {
            mDB.delete(NamedDBConst._MY_TABLE, NamedDBConst.code + "=\'" + code + "\'", null);
        } else {
        }
    }

    public void insert(String code) {
        if(mDB != null && get(new String [] {code}).getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(NamedDBConst.code, code);
            values.put(NamedDBConst.reserved1, "");
            values.put(NamedDBConst.reserved2, "");
            values.put(NamedDBConst.reserved3, "");
            values.put(NamedDBConst.reserved4, "");
            mDB.insert(NamedDBConst._MY_TABLE, null, values);
        } else {
        }
    }
}
