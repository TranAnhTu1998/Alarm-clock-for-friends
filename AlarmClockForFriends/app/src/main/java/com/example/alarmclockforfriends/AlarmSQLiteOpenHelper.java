package com.example.alarmclockforfriends;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class AlarmSQLiteOpenHelper extends SQLiteOpenHelper {

    public final static String DATABASE_NAME = "mathalarmclock.db"; // Имя базы данных
    public final static int DATABASE_VERSION = 1; // Версия базы данных
    private static SQLiteDatabase database=null; // База данных
    public final static String TABLE_NAME = "ALARMTABLE"; // Название таблицы с данными о будильниках
    public final static String ID_FLD = BaseColumns._ID; // Название поля с ID будильника
    public final static String ALARMTIME_FLD = "ALARM_TIME"; // Название поля со временем будильника в миллисекундах
    public final static String REPLAY_FLD = "ALARM_REPLAY";  // Название поля с индикатором будильник ежедневный или разовый
    public final static String MELODY_FLD = "ALARM_MELODY";  // Название поля со ссылкой на мелодию
    public final static String MELODYNAME_FLD = "ALARM_MELODYNAME";  // Название поля с именем мелодии
    public final static String ONOFF_FLD = "ALARM_ONOFF";  // Название поля индикатором активен или нет будильник
    public final static String LASTTIME_FLD = "ALARM_LASTTIME";  // Название поля со временем последнего срабатывания будильника в секундах

    public AlarmSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        try {
            database = this.getWritableDatabase(); // Получаем экземпляр базы данных
        } catch (SQLException e) {
            // Здесь обрабатываем ошибку создания базы данных, если такая произошла
            Log.e(this.toString(), "Error while getting database");
            throw new Error("The end");
        }
    }

    @Override
    // Создание таблицы с будильниками
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + ID_FLD+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ALARMTIME_FLD + " INTEGER, "
                + REPLAY_FLD + " INTEGER, "
                + MELODY_FLD + " TEXT, "
                + MELODYNAME_FLD + " TEXT, "
                + ONOFF_FLD + " INTEGER, "
                + LASTTIME_FLD + " INTEGER"
                +");";
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    // Никаких дополнительных действий при изменении версии базы данных
    // просто пересоздаем таблицу с будильниками
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME); // Удаляем таблицу с будильниками, если она создана
        onCreate(database); // Создаем таблицу с будильниками
    }

    // Получение курсора на все записи в таблице с будильниками
    public Cursor getAllEntries()
    {
        String[] columnsToTake = {"*"}; // Получаем все поля таблицы
        String whereClause=null; // Без каких-то фильтров
        String[] selectionArgs=null;
        String groupBy=null; // Без группировок
        String having=null; // Без условий для агрегатных функций
        String orderBy = ALARMTIME_FLD; // Сортируем по времени будильника по возрастанию
        Cursor cursor=database.query(TABLE_NAME, columnsToTake, whereClause, selectionArgs,
                groupBy, having, orderBy );
        return cursor;
    }

    // Выполнение запроса SELECT
    public static Cursor query(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy)
    {
        return database.query(TABLE_NAME, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    // Выполнение запроса INSERT
    public static long insert(String columns, ContentValues values)
    {
        return database.insert(TABLE_NAME, columns, values);
    }

    // Выполнение запроса DELETE
    public static int delete(String where, String[] whereArgs)
    {
        return database.delete(TABLE_NAME, where, whereArgs);
    }

    // Выполнение запроса UPDATE
    public static int update(ContentValues values, String where, String[] whereArgs)
    {
        return database.update(TABLE_NAME, values, where, whereArgs);
    }
}

