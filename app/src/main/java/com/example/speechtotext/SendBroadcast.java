package com.example.speechtotext;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import hu.pe.yummykart.broadcastdemo.R;

//import android.support.v4.content.LocalBroadcastManager;
//import android.support.v7.app.AppCompatActivity;

public class SendBroadcast extends AppCompatActivity
{

    private static final int REQUEST_AUDIO_PERMISSIONS = 100;
  static public EditText textView1;
//  static public String voice1;
    Button btn1,bt2;
    ImageButton language;
    private void showChangeLanguageDialog() {
        final String[] listitems = {"English-INDIA", "English-US", "English-UK", "हिन्दी", "മലയാളം", "தமிழ்", "తెలుగు",
                "ಕನ್ನಡ", "español", "French", "मराठी"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(SendBroadcast.this);
        mBuilder.setTitle("Choose Language..");
        mBuilder.setSingleChoiceItems(listitems, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    ServiceClass.voice2 = "en-IN";
                    setlocale ("en-IN");
                    recreate();
                }
                else if (i == 1) {
                    ServiceClass.voice2 = "en-US";
                    setlocale( "en-US");
                    recreate();
                }else if (i == 2) {
                    ServiceClass.voice2 = "en-GB";
                    setlocale( "en-GB");
                    recreate();
                }else if (i == 3) {
                    ServiceClass.voice2 = "hi-IN";
                    setlocale( "hi-IN");
                    recreate();
                }else if (i == 4) {
                    ServiceClass.voice2 = "ml-IN";
                    setlocale( "ml-IN");
                    recreate();
                }else if (i == 5) {
                    ServiceClass.voice2 = "ta-IN";
                    setlocale( "ta-IN");
                    recreate();
                }else if (i == 6) {
                    ServiceClass.voice2 = "te-IN";
                    setlocale( "te-IN");
                    recreate();
                }
                else if (i == 7) {
                    ServiceClass.voice2 = "kn-IN";
                    setlocale( "kn-IN");
                    recreate();
                }
                else if (i == 8) {
                    ServiceClass.voice2 = "es-ES";
                    setlocale( "es-ES");
                    recreate();
                }else if (i == 9) {
                    ServiceClass.voice2 = "fr";
                    setlocale( "fr");
                    recreate();
                }else if (i == 10) {
                    ServiceClass.voice2 = "mr-IN";
                    setlocale( "mr-IN");
                    recreate();
                }
                dialogInterface.dismiss();
            }
        });
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    private void setlocale(String lang) {
        Locale locale =new Locale(lang);
        Locale.setDefault(locale);
        Configuration configuration =new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration,getBaseContext().getResources().getDisplayMetrics());
        SharedPreferences.Editor editor = getSharedPreferences("Settings",MODE_PRIVATE).edit();
        editor.putString("My_Lang",lang);
        editor.apply();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn1 = (Button)findViewById(R.id.btn1);
        bt2 =(Button)findViewById(R.id.btn2);
        textView1 =(EditText) findViewById(R.id.textView);
        requestForAudioPermission();
        language = (ImageButton) findViewById(R.id.imageView2);
        language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangeLanguageDialog();
            }
        });


        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
               /* Intent intent = new Intent("SENDMSG");
                intent.putExtra("message", "broadcast received");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);*/
                Intent intent = new Intent(getApplicationContext(), ServiceClass.class);
                startService(intent);

            }
        });
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ServiceClass.class);
                stopService(intent);
            }
        });
    }

    public boolean checkForAudioPermissions(Context context)
    {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestForAudioPermission()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_PERMISSIONS);
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}