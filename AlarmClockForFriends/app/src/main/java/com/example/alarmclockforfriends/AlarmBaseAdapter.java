package com.example.alarmclockforfriends;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

public class AlarmBaseAdapter extends BaseAdapter {

    public static AlarmBaseAdapter adapter=null; // указатель на экземпляр адаптера
    private MainActivity activity=null; // Главная активность
    private Cursor cursor=null; // Курсор таблицы с будильниками
    private AlarmSQLiteOpenHelper dbhelper=null; // База данных

    public AlarmBaseAdapter(MainActivity _activity, AlarmSQLiteOpenHelper _dbhelper)
    {
        super();
        activity=_activity;  // сохраним активность
        dbhelper=_dbhelper; // сохраним ссылку на базу данных
        cursor=dbhelper.getAllEntries(); // Получим все содержимое таблицы с будильниками
        adapter=this; // сохраним ссылку на созданный адаптер
    }

    @Override
    // Получение количества будильников в таблице
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    // Получение экземпляра будильника по номеру позиции в выборке из таблицы
    public AlarmData getItem(int position) {
        // Перемещаем курсор на позицию
        if (cursor.moveToPosition(position)) {
            // Получаем данные из записи в таблице с будильниками
            long id = cursor.getLong(0);
            long alarmtime = cursor.getLong(1);
            boolean replay = (int)cursor.getLong(2)==1;
            String melody = cursor.getString(3);
            String melodyname = cursor.getString(4);
            boolean onoff = (int)cursor.getLong(5)==1;
            long lastdate = cursor.getLong(6);
            // Создаем экземпляр будильника и возвращаем его
            AlarmData item = new AlarmData(id, alarmtime, replay, melody, melodyname, onoff, lastdate);
            return item;
        } else {
            // Возращаем null если позиция не найдена
            return null;
        }
    }

    @Override
    // Получение ID будильника по номеру позиции в выборке из таблицы
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    // Вывод будильника в список
    public View getView(int position, View convertView, ViewGroup parent) {
        // получим данные будильника из таблицы
        AlarmData item=getItem(position);
        View rowView=convertView;
        // Если разметка для вывода позиции будильника не создана ранее, то создадим ее
        if (rowView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.alarm_item, parent, false);
        }

        // Выведем время будильника в разметку
        TextView tvAlarmtime= rowView.findViewById(R.id.alarm_item_time);
        tvAlarmtime.setText(item.getAlarmTimeString());

        tvAlarmtime.setTag(item); // Сохраним в теге времени экземпляр будильника
        // установка обработчика нажатия на время будильника
        tvAlarmtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // получим из тега времени экземпляр будильника
                AlarmData item=(AlarmData)view.getTag();
                // вызовем диалог создания/изменения параметров будильника
                AlarmEditDialog dialog=new AlarmEditDialog(activity, item);
                dialog.show();
            }
        });

        // Выведем значение индикатора активен или нет в разметку
        Switch swAlarmOnOff= rowView.findViewById(R.id.alarm_item_switch_alarmonoff);
        swAlarmOnOff.setTag(item); // Сохраним в теге индикатора экземпляр будильника
        swAlarmOnOff.setChecked(item.getOnOff());
        // установка обработчика изменения состояния индикатора активности
        swAlarmOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                // получим из тега индикатора экземпляр будильника
                AlarmData itm=(AlarmData)buttonView.getTag();
                // изменим информацию о будильнике в базе данных
                updateItem(itm.getId(), itm.getAlarmTime(), itm.getReplay(),
                        itm.getMelody(), itm.getMelodyName(), isChecked, itm.getLastTime());
            }
        });

        // получим кнопку удаления из разметки
        ImageButton ibDeleteAlarm= rowView.findViewById(R.id.alarm_item_delete);
        ibDeleteAlarm.setTag(item); // Сохраним в теге кнопки удаления экземпляр будильника
        // установка обработчика нажатия на время будильника
        ibDeleteAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // получим из тега времени экземпляр будильника
                final AlarmData item=(AlarmData)view.getTag();
                // опишем диалог удаления будильника
                AlertDialog.Builder dial=new AlertDialog.Builder(activity)
                        .setTitle(activity.getString(R.string.delete_title)) // название окна
                        .setMessage(activity.getString(R.string.delete_alarm)) // текс сообщения
                        .setIcon(android.R.drawable.ic_menu_delete) // иконка окна
                        .setCancelable(false) // нельзя закрыть нажатием в экран за окном
                        // обработка нажатия на кнопку ОТМЕНА
                        .setPositiveButton(activity.getString(R.string.dialog_cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel(); // закрываем окно
                                    }
                                })
                        // обработка нажатия на кнопку ОК
                        .setNegativeButton(activity.getString(R.string.dialog_ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        removeItem(item.getId()); // удаляем будильник
                                        dialog.cancel(); // закрываем окно
                                    }
                                });
                AlertDialog removeDialog = dial.create(); // создадим диалог удаления
                removeDialog.show(); // вызовем диалог
            }
        });

        return rowView;
    }

    // Добавление записи будильника в таблицу базы данных
    public long addItem(long _alarmtime, boolean _replay, String _melody, String _melodyname,
                        boolean _onoff, long _lastdate) {
        // Заполняем поля для выполнения запроса INSERT
        ContentValues values = new ContentValues();
        values.put(AlarmSQLiteOpenHelper.ALARMTIME_FLD, _alarmtime);
        values.put(AlarmSQLiteOpenHelper.REPLAY_FLD, _replay ? 1 : 0);
        values.put(AlarmSQLiteOpenHelper.MELODY_FLD, _melody);
        values.put(AlarmSQLiteOpenHelper.MELODYNAME_FLD, _melodyname);
        values.put(AlarmSQLiteOpenHelper.ONOFF_FLD, _onoff ? 1 : 0);
        values.put(AlarmSQLiteOpenHelper.LASTTIME_FLD, _lastdate);
        // Выполняем запрос INSERT в таблице будилиьников
        long id = dbhelper.insert(null, values);
        refresh(); // Обновим список будильников на экране
        return id;
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
                new String[] {String.valueOf(_id)})) > 0;
        refresh(); // Обновим список будильников на экране
        return isUpdated;
    }

    // Удаление записи будильника из таблицы базы данных
    public boolean removeItem(long id) {
        // Выполняем запрос DELETE в таблице будилиьников
        boolean isDeleted = (dbhelper.delete(AlarmSQLiteOpenHelper.ID_FLD + "=?",
                new String[] { String.valueOf(id) })) > 0;
        refresh(); // Обновим список будильников на экране
        return isDeleted;
    }

    // обновление списка будильников
    public void refresh()
    {
        cursor=dbhelper.getAllEntries(); // Получим все содержимое таблицы с будильниками
        notifyDataSetChanged(); // Обновим список будильников на экране
    }
}

