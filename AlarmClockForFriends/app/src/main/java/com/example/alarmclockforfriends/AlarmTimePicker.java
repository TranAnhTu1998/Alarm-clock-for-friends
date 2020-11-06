package com.example.alarmclockforfriends;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class AlarmTimePicker extends DialogFragment implements
        TimePickerDialog.OnTimeSetListener{

    private AlarmEditDialog dialog=null; // диалог, откуда был вызван диалог выбор времени
    private long dialogTime=-1; // значение времени в диалоге

    // создаем отдельный метод установки значения диалога, откуда был вызван диалог выбора времени,
    // а также времени
    // поскольку внести изменения в конструктор невозможно
    public void setDialog(AlarmEditDialog dialog_, long _dialogTime){
        dialog=dialog_;
        dialogTime=_dialogTime;
    }

    @Override
    // создание диалога выбора времени
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Установка значения времени в диалоге
        GregorianCalendar mCalendar=(GregorianCalendar)GregorianCalendar.getInstance();
        mCalendar.setTime(new Date(dialogTime));
        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = mCalendar.get(Calendar.MINUTE);

        // Создание диалога выбора времени
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    // Обработка результатов выбора времени
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // получаем результат в миллисекуднах
        GregorianCalendar mCalendar=(GregorianCalendar)GregorianCalendar.getInstance();
        mCalendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
        mCalendar.set(Calendar.MINUTE,minute);
        // передаем результат в диалог создания/изменения параметров будильника
        dialog.setAlarmTime(mCalendar.getTimeInMillis());
    }
}
