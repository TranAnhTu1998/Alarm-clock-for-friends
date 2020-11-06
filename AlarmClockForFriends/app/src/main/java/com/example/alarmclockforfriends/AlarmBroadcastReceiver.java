package com.example.alarmclockforfriends;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.PowerManager;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    static String ALARM_TAG = "mathalarmlock:wakelocktag";
    static String ALARM_INTENT_TAG = "DO_ALARM";
    static final int ALARM_ID = 9999;
    static final int ALARM_FREQUENCY = 1;
    private AlarmSQLiteOpenHelper dbhelper = null;
    private Cursor cursor = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Получаем управление над состоянием телефона
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        // Получаем управление над функцией пробуждения
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, ALARM_TAG);

        wl.acquire(); // Блокируем поток
        doAlarm(context, intent); // Обрабатываем будильники
        wl.release(); // Разблокируем поток
    }

    // Старт обработки службы сообщений
    public static void startAlarm(Context context) {
        // Получаем управление над службой будильников
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Формируем намерение с командой старта
        Intent intent = new Intent(ALARM_INTENT_TAG);
        intent.putExtra("Command", "Start");
        PendingIntent sender = PendingIntent.getBroadcast(context, ALARM_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //Отменяем существующий обработчик
        alarmManager.cancel(sender);
        //Запускаем новый обработчик
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + ALARM_FREQUENCY * 1000L, sender);
    }

    // Остановка тайминга
    public static void stopAlarm(Context context) {
        // Получаем управление над службой будильников
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Формируем намерение с командой остановки
        Intent intent = new Intent(ALARM_INTENT_TAG);
        intent.putExtra("Command", "Stop");
        PendingIntent sender = PendingIntent.getBroadcast(context, ALARM_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //Отменяем существующий обработчик
        alarmManager.cancel(sender);
        //Посылаем сообщение с командой полной остановки
        context.sendBroadcast(intent);
    }

    public void doAlarm(Context context, Intent _intent) {
        String comm = _intent.getStringExtra("Command");
        if (comm != null)
            if (comm.equals("Start")) {
                //Получаем доступ к базе данных
                dbhelper = new AlarmSQLiteOpenHelper(context);
                //Получаем доступ к таблице будильников
                cursor = dbhelper.getAllEntries();
                //Получаем количество будильников
                int alarm_num = cursor.getCount();
                //Получаем текущий час и минуты
                GregorianCalendar mCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
                long currTime = mCalendar.getTime().getTime(); // время будильника=текущее время в миллисекундах
                int currHour = mCalendar.get(Calendar.HOUR_OF_DAY);
                int currMinute = mCalendar.get(Calendar.MINUTE);
                //Цикл по всем будильникам, пока не найдем один подходящий,
                //либо пока не пройдем все
                boolean alarm_end = false;
                int alarm_pos = 0;
                while (!alarm_end) {
                    //Пока не вышли за число всех будильников
                    if (alarm_num > alarm_pos) {
                        AlarmData item = getItem(alarm_pos);
                        int alarmHour = item.getAlarmHour();
                        int alarmMinute = item.getAlarmMinute();
                        //Если будильник активен
                        if (item.getOnOff())
                            //Если текущее время равно времени будильника
                            if ((currHour == alarmHour) && (currMinute == alarmMinute))
                                //Если будильник еще не срабатывал,
                                // либо включен режим ежедневного повтора и прошли уже сутки
                                if ((item.getLastTime() == -1) ||
                                        (item.getReplay() && (currTime - item.getLastTime()) >= 86400000)) {
                                    //Устанавливаем во времени последнего срабатывания
                                    //текушее время
                                    item.setLastDate(currTime);
                                    //сбрасываем индиктор активности если будильник без повтора
                                    if (!item.getReplay()) item.setOnOff(false);
                                    updateItem(item.getId(), item.getAlarmTime(), item.getReplay(),
                                            item.getMelody(), item.getMelodyName(), item.getOnOff(),
                                            item.getLastTime());
                                    if (MainActivity.alarmAdapter != null)
                                        MainActivity.alarmAdapter.refresh();

                                    // Запустим проигрываетель с выбранной мелодией
                                    try {
                                        MainActivity.mediaPlayer = new MediaPlayer();
                                        MainActivity.mediaPlayer.setDataSource(context, Uri.parse(item.getMelody()));
                                        MainActivity.mediaPlayer.prepare();
                                        MainActivity.mediaPlayer.start();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    Intent intent = new Intent(context, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.setClass(context, MainActivity.class);
                                    context.startActivity(intent);



                                    alarm_end = true;
                                }
                    } else alarm_end = true;
                    alarm_pos++;
                }
                startAlarm(context);
            } else if (comm.equals("Stop")) {
                // Получаем управление над службой будильников
                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                // Формируем намерение с командой полной остановки
                Intent intent0 = new Intent(ALARM_INTENT_TAG);
                intent0.putExtra("Command", "Close");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_ID,
                        intent0, PendingIntent.FLAG_UPDATE_CURRENT);
                //Отменяем существующий обработчик
                am.cancel(pendingIntent);
            }
    }

    // Получение экземпляра будильника по номеру позиции в выборке из таблицы
    public AlarmData getItem(int position) {
        // Перемещаем курсор на позицию
        if (cursor.moveToPosition(position)) {
            // Получаем данные из записи в таблице с будильниками
            long id = cursor.getLong(0);
            long alarmtime = cursor.getLong(1);
            boolean replay = (int) cursor.getLong(2) == 1;
            String melody = cursor.getString(3);
            String melodyname = cursor.getString(4);
            boolean onoff = (int) cursor.getLong(5) == 1;
            long lastdate = cursor.getLong(6);
            // Создаем экземпляр будильника и возвращаем его
            AlarmData item = new AlarmData(id, alarmtime, replay, melody, melodyname, onoff, lastdate);
            return item;
        } else {
            // Возращаем null если позиция не найдена
            return null;
        }
    }

    // Изменение записи будильника в таблице базы данных
    public boolean updateItem(long _id, long _alarmtime, boolean _replay, String _melody,
                              String _melodyname, boolean _onoff, long _lastdate) {
        // Заполняем поля для выполнения запроса UPDATE
        ContentValues values = new ContentValues();
        values.put(AlarmSQLiteOpenHelper.ALARMTIME_FLD, _alarmtime);
        values.put(AlarmSQLiteOpenHelper.REPLAY_FLD, _replay ? 1 : 0);
        values.put(AlarmSQLiteOpenHelper.MELODY_FLD, _melody);
        values.put(AlarmSQLiteOpenHelper.MELODYNAME_FLD, _melodyname);
        values.put(AlarmSQLiteOpenHelper.ONOFF_FLD, _onoff ? 1 : 0);
        values.put(AlarmSQLiteOpenHelper.LASTTIME_FLD, _lastdate);
        // Выполняем запрос UPDATE в таблице будилиьников
        boolean isUpdated = (dbhelper.update(values,
                AlarmSQLiteOpenHelper.ID_FLD + "=?",
                new String[]{String.valueOf(_id)})) > 0;
        return isUpdated;
    }
}
