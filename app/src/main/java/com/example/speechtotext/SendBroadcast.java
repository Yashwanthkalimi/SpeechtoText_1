package com.example.speechtotext;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.Set;

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
    AudioManager audioM = null;
    BluetoothAdapter btAdapter;
    public static Context ctx;
    BluetoothManager bMgr = null;
    private Set<BluetoothDevice> devices;
    private MyReceiver receiver;
    IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
    IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
    ToggleButton tb1 = null;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
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
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = getApplicationContext();
        this.registerReceiver(receiver, filter1);
        this.registerReceiver(receiver, filter2);
        tb1 = (ToggleButton)findViewById(R.id.toggleButton1);
        audioM = (AudioManager) getApplicationContext().getSystemService(getApplicationContext().AUDIO_SERVICE);
        bMgr =  (BluetoothManager) getApplicationContext().getSystemService(getApplicationContext().BLUETOOTH_SERVICE);
        btn1 = (Button)findViewById(R.id.btn1);
        bt2 =(Button)findViewById(R.id.btn2);
        textView1 =(EditText) findViewById(R.id.textView);
        requestForAudioPermission();
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        devices = btAdapter.getBondedDevices();
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

    public void onToggleClicked(View view) {

        boolean on = ((ToggleButton) view).isChecked();

        if (on) {
            if((MyReceiver.isBTConnected == true) || (devices.size() > 0))
            {
                audioM.setMode(audioM.MODE_IN_COMMUNICATION);
                audioM.setBluetoothScoOn(true);
                audioM.startBluetoothSco();
                audioM.setSpeakerphoneOn(false);
                Log.d("Yashwanth","Toggle Button On!");
            }
            else
            {
                tb1.setChecked(false);
                Toast.makeText(getApplicationContext(), "BT is not connected, Pls pair your device and restart the app again!", Toast.LENGTH_LONG).show();
            }

        } else {
            audioM.setMode(audioM.MODE_NORMAL);
            audioM.setBluetoothScoOn(false);
            audioM.stopBluetoothSco();
            audioM.setSpeakerphoneOn(true);
            Log.d("Yashwanth","Toggle Button Off!");

        }

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
    protected void onDestroy() {
        audioM.setMode(AudioManager.MODE_NORMAL);
        audioM.setSpeakerphoneOn(true);
        super.onDestroy();
    }
}