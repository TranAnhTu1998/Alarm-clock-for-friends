package com.example.alarmclockforfriends;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    public static MainActivity activity=null; // Главная активность
    private ListView alarmListView=null; // Список будильников
    private AlarmSQLiteOpenHelper alarmDBhelper=null; // База данных с будильниками
    protected static AlarmBaseAdapter alarmAdapter=null; // Адаптер получения и вывода будильников
    private AlarmEditDialog dialog=null; // Экземпляр активного диалога редактирования будильника
    private static int PICKFILE_RESULT_CODE=1001; // Результат операции получения ссылки на мелодию
    protected static AlarmDialog alarmDialog=null;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;// результат получения разрешения на доступ
    public static MediaPlayer mediaPlayer=null;
    private static final String APP_PREFERENCES = "alarmsettings";
    private static final String BACKGROUND_FLAG = "background_flag";
    private SharedPreferences mSettings;
    private boolean fBackground=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Сохраняем текущую активность
        activity = this;
        // Подключаемся к базе данных
        alarmDBhelper = new AlarmSQLiteOpenHelper(activity);
        // Получаем ListView списка будильников в главной разметке
        alarmListView = findViewById(R.id.alarmListView);
        // Создаем адаптер
        alarmAdapter = new AlarmBaseAdapter(this, alarmDBhelper);
        // Привязываем адаптер к ListView списка будильников
        alarmListView.setAdapter(alarmAdapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlarmEditDialog dialog = new AlarmEditDialog(activity, null);
                dialog.show();
            }
        });

        checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        fBackground = mSettings.getBoolean(BACKGROUND_FLAG, false);
        stopService(new Intent(activity, AlarmService.class));
        if(fBackground)
        {
            startService(new Intent(activity, AlarmService.class));
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if((mediaPlayer!=null)&&(alarmDialog==null)) {
            alarmDialog = new AlarmDialog(this, mediaPlayer);
            alarmDialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        MenuItem item=menu.getItem(0);
        item.setChecked(fBackground);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.background) {
            fBackground=!item.isChecked();
            item.setChecked(fBackground);
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putBoolean(BACKGROUND_FLAG, fBackground);
            editor.apply();
            stopService(new Intent(activity, AlarmService.class));
            if(fBackground)
            {
                startService(new Intent(activity, AlarmService.class));
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    // получение результатов из других активностей
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // если результат положительный
        if (resultCode == Activity.RESULT_OK)
        {
            // если это результат активности по выбору мелодии
            if(requestCode==PICKFILE_RESULT_CODE)
            {
                Uri uriMelody = data.getData(); // получим ссылку на мелодию из переданных данных
                dialog.setMelody(uriMelody); // передадим ссылку на мелодию в диалог создания/редактирования будильника
            }
        }
    }

    // запуск намерения для выбора мелодии из хранилища устройства
    public void onGetMusic(AlarmEditDialog dialog_)
    {
        dialog=dialog_; // сохранияем экземпляр диалога, откуда был вызван метод
        // Создаем намерение выбора мелодии из хранилища
        Intent chooseAudio = new Intent(Intent.ACTION_GET_CONTENT);
        chooseAudio.addCategory(Intent.CATEGORY_OPENABLE);
        chooseAudio.setType("audio/*");
        // вызываем активность выбора мелодии с возвратом результата
        startActivityForResult( Intent.createChooser(chooseAudio, getString(R.string.edit_choose_melody)), PICKFILE_RESULT_CODE );
    }

    // проверка наличия разрешения на доступ к внешнему хранилищу
    public static boolean checkPermission(final Context context, String permission, int rescode) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    permission) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,permission)) {
                    showPermissionDialog("External storage", context,
                            permission, rescode);
                } else {
                    ActivityCompat.requestPermissions(
                            (Activity) context,
                            new String[] { permission },
                            rescode);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    // диалоговое окно для запроса разрешения на доступ к внешнему хранилищу
    public static void showPermissionDialog(final String msg, final Context context,
                                            final String permission, final int rescode) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                rescode);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }
}


