package com.gj.crazyhat.city;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gj.crazyhat.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuzi on 16-5-30.
 */
public class CityManager {
    public static final String LOCATE_CITY_CODE = "0";
    public static final String LOCATE_CITY_NAME = "定位城市";
    private static final int DB_VERSION = 1;
    private static final String DB_PATH = "city.db";
    private static final String TAG = "Crazy.City";
    private static final String PREFERENCE_CITY_DB_VERSION = "pref_city_db_version";
    private static final String TABLE_NAME = "cityinfo";
    private Context context;

    public CityManager(Context context) {
        this.context = context;
    }

    public SQLiteDatabase openDatabase() {
        File dbFile = context.getFileStreamPath(DB_PATH);
        FileOutputStream fout = null;
        InputStream is = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int version = prefs.getInt(PREFERENCE_CITY_DB_VERSION, -1);
        try {
            //判断数据库文件是否存在，若不存在则执行导入，否则直接打开数据库
            if (!dbFile.exists() || version < DB_VERSION) {
                is = context.getResources().openRawResource(
                        R.raw.city_code);
                fout = context.openFileOutput(DB_PATH, Context.MODE_PRIVATE);
                byte[] buffer = new byte[1024];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fout.write(buffer, 0, count);
                }
                fout.close();
                is.close();
                prefs.edit().putInt(PREFERENCE_CITY_DB_VERSION, DB_VERSION).apply();
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        return db;
    }

    public static class CityInfo {
        public CityInfo(String code, String name) {
            this.code = code;
            this.name = name;
            this.idInDb = -1L;
        }

        public CityInfo(String code, String name, long idInDb) {
            this.code = code;
            this.name = name;
            this.idInDb = idInDb;
        }

        public String name;
        public String code;
        public long idInDb;
    }

    public List<CityInfo> queryAllHotCity() {
        ArrayList<CityInfo> result = new ArrayList<CityInfo>();
        SQLiteDatabase db = openDatabase();
        assert (db != null);
        String[] columns = new String[] {
                "cityid", "cityname"
        };
        String selection = "hot != ''";
        Cursor cursor = db.query(TABLE_NAME, columns, selection, null, null, null, "hot desc");
        result.add(new CityInfo(LOCATE_CITY_CODE, LOCATE_CITY_NAME));
        while (cursor.moveToNext()) {
            result.add(new CityInfo(cursor.getString(0), cursor.getString(1)));
        }
        cursor.close();
        db.close();
        return result;
    }

    public List<CityInfo> seachCityInfo(String input) {
        ArrayList<CityInfo> result = new ArrayList<CityInfo>();
        SQLiteDatabase db = openDatabase();
        assert (db != null);
        String[] columns = new String[] {
                "cityid", "cityname"
        };
        String selection = "cityname like ? or citypy like ? or cityshort like ?";
        input = input + "%";
        String[] selectionArgs = new String[] {input, input, input};
        Cursor cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null,
                "CAST (hot AS DECIMAL) DESC, cityid");
        while (cursor.moveToNext()) {
            result.add(new CityInfo(cursor.getString(0), cursor.getString(1)));
        }
        cursor.close();
        db.close();
        return result;
    }

    public CityInfo seachExactCity(String cityName) {
        CityInfo result = null;
        SQLiteDatabase db = openDatabase();
        assert (db != null);
        String[] columns = new String[] {"cityid", "cityname"};
        String selection = "cityname = ?";
        String selectionArgs[] = new String[] {cityName};
        Cursor cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null,
                null);
        if (cursor == null) {
            db.close();
            Log.w(TAG, "cursor count null, no such city: " + cityName);
            return result;
        }
        if (cursor.moveToFirst()) {
            result = new CityInfo(cursor.getString(0), cursor.getString(1));
        }
        cursor.close();
        db.close();
        return result;
    }

}
