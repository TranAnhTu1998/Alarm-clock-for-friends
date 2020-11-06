package com.example.alarmclockforfriends;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class AlarmData {

    private long id=0; // ID записи в таблице будильников
    private long alarmtime=0; // Дата будильника
    private boolean replay=false; // Индикатор ежедневный или разовый
    private String melody=null; // Ссылка на мелодию
    private String melodyname=null; // Название мелодии
    private boolean onoff=false; // Индикатор будильник активен или нет
    private long lasttime=0; // Время последнего срабатывания будильника

    // Создание экземпляра будильника
    public AlarmData(long _id, long _alarmtime, boolean _replay, String _melody, String _melodyname, boolean _onoff, long _lasttime) {
        id=_id;
        alarmtime=_alarmtime;
        replay=_replay;
        melody=_melody;
        melodyname=_melodyname;
        onoff=_onoff;
        lasttime=_lasttime;
    }

    // получение id будильника
    public long getId() {
        return id;
    }

    // получение времени будильника
    public long getAlarmTime() {
        return alarmtime;
    }

    // получение строки времени будильника в формате HH:MM
    public String getAlarmTimeString() {

        GregorianCalendar mCalendar=(GregorianCalendar)GregorianCalendar.getInstance();
        mCalendar.setTime(new Date(alarmtime));
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String timeString = timeFormat.format(mCalendar.getTime());
        return timeString;
    }

    // получение часа будильника
    public int getAlarmHour() {
        GregorianCalendar mCalendar=(GregorianCalendar)GregorianCalendar.getInstance();
        mCalendar.setTime(new Date(alarmtime));
        return mCalendar.get(Calendar.HOUR_OF_DAY);
    }

    // получение минут будильника
    public int getAlarmMinute() {
        GregorianCalendar mCalendar=(GregorianCalendar)GregorianCalendar.getInstance();
        mCalendar.setTime(new Date(alarmtime));
        return mCalendar.get(Calendar.MINUTE);
    }

    // получение индикатора ежедневного повтора
    public boolean getReplay() {
        return replay;
    }

    // получение ссылки на мелодию
    public String getMelody() {
        return melody;
    }

    // получение названия мелодии
    public String getMelodyName() {
        return melodyname;
    }

    // получение индикатора активен или нет будильник
    public boolean getOnOff() {
        return onoff;
    }

    // получение времени последнего срабатывания
    public long getLastTime() {
        return lasttime;
    }

    // установка индикатора активности
    public void setOnOff(boolean _onoff) {
        onoff=_onoff;
    }

    // установка времени последнего срабатывания
    public void setLastDate(long _lasttime) {
        lasttime=_lasttime;
    }

}

