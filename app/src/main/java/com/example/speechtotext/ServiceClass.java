package com.example.speechtotext;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

//import android.support.annotation.Nullable;
//import android.support.v4.content.LocalBroadcastManager;
import hu.pe.yummykart.broadcastdemo.R;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;


public class ServiceClass extends Service
{
    //  BroadcastReceiver mRegistrationBroadcastReceiver;
public EditText textView= SendBroadcast.textView1;
public static String voice2;
    private Accelerometer accelerometer;
    private Gyroscope gyroscope;
    String tokens[]= null;
    LocationFinder locationFinder;
    //  TextView tv_result;
    //  Button btn_start,btn_stop;
//    textView =(TextView)findViewById(R.id.textView);
    String listeningMsg;
    long startListeningTime;
    long pauseAndSpeakTime;
    boolean speechResultFound = false;
    boolean onReadyForSpeech = false;
    boolean continuousSpeechRecognition = true;

    final static int ERROR_TIMEOUT = 5000;
    final static int AUDIO_BEEP_DISABLED_TIMEOUT = 30000;
    final static int MAX_PAUSE_TIME = 500;
    static int PARTIAL_DELAY_TIME = 500;

    private static final int REQUEST_AUDIO_PERMISSIONS = 100;

    SpeechRecognizer speechRecognizer;
    Intent speechIntent;
    private AudioManager audioManager;
    private Handler restartDroidSpeech = new Handler();
    private Handler speechPartialResult = new Handler();
    private Span nameSpans[];

    @Override
    public void onStart(Intent intent, int startId)
    {
        super.onStart(intent, startId);
        accelerometer.register();
        //  LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter("SENDMSG"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        accelerometer= new Accelerometer(this);
        gyroscope= new Gyroscope(this);
        accelerometer.setListener(new Accelerometer.Listener() {
            @Override
            public void onTranslate(float tx, float ty, float tz) {
                if  (tx >1.0f || tx<-1.0f || ty >1.0f || ty<-1.0f || tz >1.0f || tz<-1.0f){
                    setRecognitionProgressMsg("");
                    startSpeechRecognition();
                }
//                else{
//                    mSpeechRecognizer.stopListening();
//                    editText.setHint("You will see input here");
//
//                }
            }
        });




        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        accelerometer.deregister();
        // LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    private void initSpeechProperties()
    {
        // Initializing the droid speech recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        // Initializing the speech intent
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, voice2);
        speechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
//        speechIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        // Initializing the audio Manager
        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
    }

    // Mutes (or) un mutes the audio
    private void muteAudio(Boolean mute)
    {
        try
        {
            // mute (or) un mute audio based on status
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, mute ? AudioManager.ADJUST_MUTE : AudioManager.ADJUST_UNMUTE, 0);
            }
            else
            {
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, mute);
            }
        }
        catch (Exception e)
        {
            if(audioManager == null) return;

            // un mute the audio if there is an exception
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
            }
            else
            {
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            }
        }
    }


    private void cancelSpeechOperations()
    {
        if (speechRecognizer != null)
        {
            speechRecognizer.cancel();
        }
    }


    private void closeSpeech()
    {
        if (speechRecognizer != null)
        {
            speechRecognizer.destroy();
        }

        // Removing the partial result callback handler if applicable
        speechPartialResult.removeCallbacksAndMessages(null);

        // If audio beep was muted, enabling it again
        muteAudio(true);//f
    }

    private void setRecognitionProgressMsg(String msg)
    {
        if(msg != null)
        {
            //tv_result.setText(msg);

            Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
            if(msg.contains("deactivate dnd") )
            {
                //   stopSelf();
            }
        }
    }

    private void restartDroidSpeechRecognition()
    {
        accelerometer.setListener(new Accelerometer.Listener() {
            @Override
            public void onTranslate(float tx, float ty, float tz) {
                if  (tx >1.0f || tx<-1.0f || ty >1.0f || ty<-1.0f || tz >1.0f || tz<-1.0f){
                    setRecognitionProgressMsg("");
                    startSpeechRecognition();
                }
//                else{
//                    mSpeechRecognizer.stopListening();
//                    editText.setHint("You will see input here");
//
//                }
            }
        });
    }


    public void startSpeechRecognition()
    {

        // listeningMsg="";
        setRecognitionProgressMsg(listeningMsg);

        startListeningTime = System.currentTimeMillis();
        pauseAndSpeakTime = startListeningTime;
        speechResultFound = false;

        if(speechRecognizer == null || speechIntent == null || audioManager == null)
        {
            // Initializing the droid speech properties if found not initialized
            initSpeechProperties();
        }

        speechRecognizer.setRecognitionListener(new RecognitionListener()
        {
            @Override
            public void onReadyForSpeech(Bundle params)
            {
                // If audio beep was muted, enabling it again
                muteAudio(true);//f
                onReadyForSpeech = true;
            }

            @Override
            public void onBeginningOfSpeech()
            {

            }

            @Override
            public void onRmsChanged(float rmsdB)
            {

            }

            @Override
            public void onBufferReceived(byte[] buffer)
            {

            }

            @Override
            public void onEndOfSpeech()
            {

            }

            @Override
            public void onError(int error)
            {
//
            }

            @Override
            public void onResults(Bundle results)
            {
                if(speechResultFound)
                    return;

                speechResultFound = true;

                // If audio beep was muted, enabling it again
                muteAudio(true);//f

                Boolean valid = (results != null && results.containsKey(SpeechRecognizer.RESULTS_RECOGNITION) &&
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) != null &&
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).size() > 0 &&
                        !results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0).trim().isEmpty());

                if(valid)
                {

                    String speechFinalResult = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0);
//


                    textView.setText(speechFinalResult);
                    setRecognitionProgressMsg(speechFinalResult);




//
                locationFinder=new LocationFinder(speechFinalResult,getApplicationContext());
                    try{


                        Uri uri =Uri.parse("google.navigation:q="+speechFinalResult+"&mode=l");
                        Intent intent = new Intent(Intent.ACTION_VIEW,uri);

                        intent.setPackage("com.google.android.apps.maps");

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }catch (ActivityNotFoundException e) {

                        Uri uri =Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps");

                        Intent intent = new Intent(Intent.ACTION_VIEW,uri);

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        startActivity(intent);
                    }
                    startSpeechRecognition();
                }
                    // Start droid speech recognition again
                else
                {
                    // No match found, restart droid speech recognition
                   startSpeechRecognition();
                }}


            @Override
            public void onPartialResults(Bundle partialResults)
            {
//
            }

            @Override
            public void onEvent(int eventType, Bundle params)
            {

            }
        });

        // Canceling any running droid speech operations, before listening
        cancelSpeechOperations();

        // Start Listening
        speechRecognizer.startListening(speechIntent);
    }

    public void closeSpeechOperations()
    {
        setRecognitionProgressMsg("");

        closeSpeech();
    }



}