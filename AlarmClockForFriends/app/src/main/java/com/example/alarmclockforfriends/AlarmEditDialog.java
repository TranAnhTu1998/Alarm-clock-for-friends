package com.example.alarmclockforfriends;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;


public class AlarmEditDialog extends AlertDialog {

    private MainActivity activity = null; // ссылка на главную активность
    private AlarmEditDialog dialog = null; // текущий диалог
    private AlarmData item = null; // экземпляр создаваемого/редактируемого будильника
    private Button buttonAlarmTime = null; // кнопка изменения времени будильника
    private CheckBox checkboxAlarmReplay = null; // переключатель ежедневного повтора
    private Button buttonAlarmMelody = null; // кнопка смены мелодии
    private Switch switchAlarmOnOff = null; // переключатель активен или нет будильник
    private ImageButton buttonAlarmMelodyPlay = null; // кнопка запуска прослушивания мелодии
    private ImageButton buttonAlarmMelodyStop = null; // кнопка останова прослушивания мелодии
    private long alarmtime = -1; // значение текущего выбранного времени
    private MediaPlayer mp = null; // медиаплейер для прослушивания мелодии

    protected AlarmEditDialog(MainActivity _activity, AlarmData _item) {
        super(_activity);

        activity = _activity; // сохраняем экзепляр активности
        dialog = this; // сохраняем экзепляр текущего диалога
        item = _item; // сохраняем экземпляр данных о будильнике

        // получаем разметку диалогового окна
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.alarm_edit, null);

        // получаем ссылки на элементы разметки
        buttonAlarmTime = (Button) layout.findViewById(R.id.button_alarmtime);
        checkboxAlarmReplay = (CheckBox) layout.findViewById(R.id.checkBox_alarmReplay);
        buttonAlarmMelody = (Button) layout.findViewById(R.id.button_alarmmelody);
        switchAlarmOnOff = (Switch) layout.findViewById(R.id.switch_alarmonoff);
        buttonAlarmMelodyPlay = (ImageButton) layout.findViewById(R.id.button_alarmmelody_play);
        buttonAlarmMelodyStop = (ImageButton) layout.findViewById(R.id.button_alarmmelody_stop);

        // заполняем данные по-умолчанию значениями для создания нового будильника
        GregorianCalendar mCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
        alarmtime = mCalendar.getTime().getTime(); // время будильника=текущее время в миллисекундах
        boolean alarmreplay = false; // индикатор повтора=не повторять
        String alarmmelody = ""; // ссылка на мелодию=пусто
        String alarmmelodyname = activity.getString(R.string.edit_alarm_nomelody); // название мелодии="нет мелодии"
        boolean alarmonoff = true; // индикатор активности=активен
        int dialog_title = R.string.new_alarm_title; // заголовок диалогового окна
        // скрываем кнопки запуска/останова прослушивания мелодии
        buttonAlarmMelodyPlay.setVisibility(View.INVISIBLE);
        buttonAlarmMelodyStop.setVisibility(View.INVISIBLE);

        // если переданный на вход конструктору экземпляр будильника не пустой,
        // то заполняем данные из записи о будильнике
        if (item != null) {
            alarmtime = item.getAlarmTime();
            alarmreplay = item.getReplay();
            alarmmelody = item.getMelody();
            alarmmelodyname = item.getMelodyName();
            alarmonoff = item.getOnOff();
            dialog_title = R.string.edit_alarm_title;
            // показываем кнопку запуска прослушивания мелодии
            buttonAlarmMelodyPlay.setVisibility(View.VISIBLE);
        }

        setAlarmTime(alarmtime); // установка значения кнопки времени
        checkboxAlarmReplay.setChecked(alarmreplay); // установка значения индикатора повтора
        buttonAlarmMelody.setTag(alarmmelody); // сохраним ссылку на мелодию в тег кнопки изменения мелодии
        buttonAlarmMelody.setText(alarmmelodyname); // установка текста кнопки изменения мелодии
        switchAlarmOnOff.setChecked(alarmonoff); // установка нидикатора активности

        setTitle(dialog_title); // установка заголовка диалогового окна

        // установка обработчика кнопки изменения времени
        buttonAlarmTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Создаем диалог выбора времени
                AlarmTimePicker timeFragment = new AlarmTimePicker();
                // Передаем в диалог выбора экземпляр текущего диалога и времени
                timeFragment.setDialog(dialog, alarmtime);
                // Вызываем диалог выбора времени
                timeFragment.show(activity.getFragmentManager(), "timePicker");
            }
        });

        // установка обработчика кнопки выбора мелодии
        buttonAlarmMelody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Если сейчас уже проигрывается мелодия, то остановим ее прослушивание
                if (mp != null) {
                    System.out.println("buttonAlarmMelofy.onClick.if");
                    mp.release();
                }
                mp = null;
                // Вызовем диалог выбора мелодии
                System.out.println("buttonAlarmMelody.onClick.noIf");
                activity.onGetMusic(dialog);
            }
        });

        // установка обработчика кнопки запуска прослушивания мелодии
        buttonAlarmMelodyPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // Получим ссылку на выбранную мелодию
                    System.out.println("buttonAlarMelodyPlayClickListener.buttonAlarmMelody.getTag() = " + buttonAlarmMelody.getTag());
                    String path=buttonAlarmMelody.getTag().toString();
                    // Запустим проигрываетель с выбранной мелодией

                    mp = new MediaPlayer();
                    mp.setDataSource(path);
                    mp.prepare();
                    mp.start(); // старт воспроизведения
                    // Скроем кнопку проигрывания мелодии
                    buttonAlarmMelodyPlay.setVisibility(View.INVISIBLE);
                    // Покажем кнопку останова проигрывания мелодии
                    buttonAlarmMelodyStop.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // установка обработчика кнопки останова прослушивания мелодии
        buttonAlarmMelodyStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // остановим проигрывание мелодии
                if (mp != null) {
                    mp.release();
                }
                mp = null;
                // Покажем кнопку проигрывания мелодии
                buttonAlarmMelodyPlay.setVisibility(View.VISIBLE);
                // Скроем кнопку останова проигрывания мелодии
                buttonAlarmMelodyStop.setVisibility(View.INVISIBLE);
            }
        });

        // установка обработчика закрытия диалога
        dialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                // Если сейчас уже проигрывается мелодия, то остановим ее прослушивание
                if (mp != null) {
                    mp.release();
                    mp = null;
                }
            }
        });

        // вывод разметки
        setView(layout);

        // установка кнопки ОК (сохранение параметров будильника)
        setButton(DialogInterface.BUTTON_POSITIVE, activity.getString(R.string.dialog_ok),
                new OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        // получим значение выбранного времени будильника
                        long alarm_time = -1;
                        if (buttonAlarmTime != null)
                            alarm_time = Long.parseLong(buttonAlarmTime.getTag().toString());
                        // получим значение индикатора повтора
                        boolean alarm_replay = false;
                        if (checkboxAlarmReplay != null)
                            alarm_replay = checkboxAlarmReplay.isChecked();
                        // получим значение ссылки на выбранную мелодию и ее название
                        String alarm_melody = null;
                        String alarm_melodyname = null;
                        if (buttonAlarmMelody != null) {
                            alarm_melody = buttonAlarmMelody.getTag().toString();
                            //alarm_melody = "C:\\Users\\Tran Anh Tu\\Desktop\\NheNhang.mp3";
                            alarm_melodyname = buttonAlarmMelody.getText().toString();
                        }
                        // получим значение индикатора активности
                        boolean alarm_onoff = false;
                        if (switchAlarmOnOff != null)
                            alarm_onoff = switchAlarmOnOff.isChecked();

                        // заполним значениями по-умолчанию параметры id будильника и даты его последнего срабатывания
                        long itemId = -1;
                        long itemLastDate = -1;
                        // если режим редактирования,
                        // то возьмем параметры id будильника и даты его последнего срабатывания
                        // из данных о будильнике
                        if (item != null) {
                            itemId = item.getId();
                        }

                        // проверим, выбрана ли мелодия
                        // если нет, то установим флаг ошибки
                        boolean error = false;
                        if ((alarm_melody == null) || (alarm_melody.isEmpty())) {
                            error = true;
                        }

                        AlarmEditDialog _next_dialog = null; // повторный диалог в случае ошибки

                        // если нет ошибки
                        if (!error) {
                            // если есть id будильника (редактирование существуещего будильника)
                            if (itemId > -1) {
                                // изменяем в базе данных информацию о существуещем будильнике
                                updateObject(itemId, alarm_time, alarm_replay, alarm_melody,
                                        alarm_melodyname, alarm_onoff, itemLastDate);
                            } else {
                                // если режим создания, то добавляем в базе данных информацию о новом будильнике
                                addObject(alarm_time, alarm_replay, alarm_melody, alarm_melodyname,
                                        alarm_onoff, itemLastDate);
                            }
                        } else {
                            // если не выбрана мелодия то выводим соответствующее сообщение,
                            String error_text = activity.getString(R.string.edit_alarm_error);
                            Toast.makeText(activity, error_text, Toast.LENGTH_LONG).show();
                            // открываем новый диалог и передаем ему уже введенные параметры
                            // открытие нового диалога необходимо, так как текущий закроется
                            _next_dialog = new AlarmEditDialog(activity, new AlarmData(itemId, alarm_time,
                                    alarm_replay, alarm_melody, alarm_melodyname, alarm_onoff, itemLastDate));
                            _next_dialog.show();
                        }

                        dialog.dismiss(); // закрываем текущий диалог

                    }
                });

        // установка кнопки ОТМЕНА (отказ от сохранение параметров будильника)
        setButton(DialogInterface.BUTTON_NEGATIVE, activity.getString(R.string.dialog_cancel),
                new OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss(); // закрываем текущий диалог
                    }
                });
    }

    // установка значения кнопки выбора времени
    public void setAlarmTime(long _alarmtime) {
        // создаем и устанавливаем календарь в соответствии с переданным значением времени
        alarmtime = _alarmtime;
        GregorianCalendar mCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
        mCalendar.setTime(new Date(alarmtime));
        // создаем строку вывода значения кнопки в формате HH:MM
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String timeForButton = timeFormat.format(mCalendar.getTime());
        buttonAlarmTime.setText(timeForButton); // устанавливаем текст кнопки
        buttonAlarmTime.setTag(alarmtime); // запонимаем значени времени в теге кнопки
    }

    // установка значения кнопки выбора мелодии
    public void setMelody(Uri uriMelody) {
        String nameMelody = activity.getString(R.string.edit_alarm_nomelody); // умолчанию мелодия не выбрана
        String pathMelody = null; // умолчанию мелодия не выбрана
        // если переданная ссылка на мелодию не пустая, то получаем имя файла с мелодией
        if (uriMelody != null) {
            // получаем значение параметров файла с мелодией по ссылке
            Cursor cursor = null;
            try {
                // заполняем параметры запроса
                final String docId = DocumentsContract.getDocumentId(uriMelody);
                final String[] split = docId.split(":");
                final String type = split[0];
                final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String selection = null;
                String[] selectionArgs = null;
                selection = "_id=?";
                selectionArgs = new String[]{split[1]};
                String[] projection = {MediaStore.Images.Media.DATA,MediaStore.Images.Media.DISPLAY_NAME};
                // выполняем запрос к данным
                cursor = activity.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                int nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                // перемещаемся в начало и получаем данные о файле
                if (cursor.moveToFirst()) {
                    pathMelody=cursor.getString(column_index); // получаем путь к файлу
                    nameMelody = cursor.getString(nameIndex); // получаем имя файла
                }
            } catch (Exception e) {
            } finally {
                if (cursor != null)   cursor.close();
            }
        }

        // установка текста кнопки с выбором мелодии равным имени найденного файла
        // либо "нет мелодии", если мелодия еще не выбрана
        buttonAlarmMelody.setText(nameMelody);
        // если мелодия выбрана
        if (uriMelody != null) {
            // запоминаем ссылку на мелодию в теге кнопки выбора мелодии
            buttonAlarmMelody.setTag(pathMelody);
            // открываем кнопку проигрывания мелодии
            buttonAlarmMelodyPlay.setVisibility(View.VISIBLE);
            // Скроем кнопку останова проигрывания мелодии
            buttonAlarmMelodyStop.setVisibility(View.INVISIBLE);
        } else {
            // если мелодия не выбрана, то очистим тег у кнопки выбора мелодии
            buttonAlarmMelody.setTag("");
            // скроем кнопку проигрывания мелодии
            buttonAlarmMelodyPlay.setVisibility(View.VISIBLE);
            // Скроем кнопку останова проигрывания мелодии
            buttonAlarmMelodyStop.setVisibility(View.INVISIBLE);
        }
    }

    // добавление нового будильника
    public void addObject(long _alarmtime, boolean _replay, String _melody,
                          String _alarm_melodyname, boolean _onoff, long _lastdate) {
        // вызовем соответствующий метод в нашем адаптере
        AlarmBaseAdapter.adapter.addItem(_alarmtime, _replay, _melody, _alarm_melodyname,
                _onoff, _lastdate);
    }

    // изменение существующего будильника
    public void updateObject(long _id, long _alarmtime, boolean _replay, String _melody,
                             String _alarm_melodyname, boolean _onoff, long _lastdate) {
        // вызовем соответствующий метод в нашем адаптере
        AlarmBaseAdapter.adapter.updateItem(_id, _alarmtime, _replay, _melody,
                _alarm_melodyname, _onoff, _lastdate);
    }
}