package com.example.alarmclockforfriends;

import android.app.AlertDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.util.Random;

public class AlarmDialog extends AlertDialog {

    private MainActivity activity = null; // ссылка на главную активность
    private AlarmDialog dialog=null;
    private MediaPlayer mediaPlayer=null;
    int oneNumber = -1;
    int twoNumber = -1;
    int sign = -1;
    int result = -1;
    EditText edtResult=null;

    protected AlarmDialog(MainActivity _activity, MediaPlayer _mediaPlayer) {
        super(_activity);

        activity = _activity; // сохраняем экзепляр активности
        dialog=this;
        mediaPlayer=_mediaPlayer;

        // получаем разметку диалогового окна
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.alarm_dialog, null);

        setExercise();

        String txtSign=null;
        if(sign==0)
            txtSign="+";
        else
            txtSign="*";

        TextView txtExcercise=layout.findViewById(R.id.alarmdialog_text);
        txtExcercise.setText(Integer.toString(oneNumber)+txtSign+Integer.toString(twoNumber));

        edtResult=layout.findViewById(R.id.alarmdialog_edittext);

        // вывод разметки
        setView(layout);
        setCancelable(false);

        // установка обработчика кнопки останова прослушивания мелодии
        Button buttonOK=layout.findViewById(R.id.button_alarmdialog_ok);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String res=edtResult.getText().toString();
                int resI=Integer.parseInt(res);

                if(resI==result) {
                    // остановим проигрывание мелодии
                    if (mediaPlayer != null) {
                        mediaPlayer.release();
                    }
                    mediaPlayer = null;
                }
                else
                {
                    MainActivity.alarmDialog = new AlarmDialog(activity, mediaPlayer);
                    MainActivity.alarmDialog.show();
                }
                dialog.dismiss();
            }
        });
    }

    public void setExercise()
    {
        Random random=new Random(System.currentTimeMillis());
        oneNumber = random.nextInt(8)+1;
        twoNumber = random.nextInt(8)+1;
        sign = random.nextInt(2);
        if(sign==0)
            result=oneNumber+twoNumber;
        else
            result=oneNumber*twoNumber;
    }
}
