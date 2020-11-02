package com.example.speechtotext;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.NoiseSuppressor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

//import android.support.annotation.Nullable;
//import android.support.v4.content.LocalBroadcastManager;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import static com.example.speechtotext.SendBroadcast.textView1;


public class ServiceClass extends Service
{
    //  BroadcastReceiver mRegistrationBroadcastReceiver;
//public EditText textView= SendBroadcast.textView1;
public static String voice2,a;
    private Accelerometer accelerometer;
    private Gyroscope gyroscope;
    public static String speechFinalResult;
    private NoiseSuppressor noiseSuppressor;
    private LocationFinder locationFinderl=new LocationFinder();

    String tokens[]= null;
    private LocationFinder locationFinder  = new LocationFinder();

    //  TextView tv_result;
    //  Button btn_start,btn_stop;
//    textView =(TextView)findViewById(R.id.textView);
    String listeningMsg;
    long startListeningTime;
    long pauseAndSpeakTime;
    boolean speechResultFound = false;
    boolean onReadyForSpeech = false;
    boolean continuousSpeechRecognition = true;
    public InputStream inputStream = null;
    public TokenizerModel tokenModel =null;


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
    public String[] openTokenzition(String sentence) throws IOException {
        InputStream is;
        TokenizerModel tm;
                is = getAssets().open("en-token.bin");

            TokenizerModel tokenModel = null;

                tokenModel = new TokenizerModel(is);


            TokenizerME tokenizer = new TokenizerME(tokenModel);
            return tokenizer.tokenize(sentence);
    }
    public String openNameFinder(String paragraph) throws IOException {
        InputStream is;
        TokenNameFinderModel tokenNameFinderModel;


            is = getAssets().open("en-ner-location.bin");
            tokenNameFinderModel = new TokenNameFinderModel(is);
        is.close();
            TokenNameFinder nameFinderME = new NameFinderME(tokenNameFinderModel);
        String[] tokens = openTokenzition(paragraph);
        String sd = "";
        Span sp[] = nameFinderME.find(tokens);


        String a[] = Span.spansToStrings(sp, tokens);
        StringBuilder fd = new StringBuilder();
        int l = a.length;

        for (int j = 0; j < l; j++) {
            fd = fd.append(a[j] + "\n");

        }
        sd = fd.toString();
        return sd;

    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onStart(Intent intent, int startId)
    {
        super.onStart(intent, startId);
        int N = AudioRecord.getMinBufferSize(48000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, N * 10);
        int sessionId = audioRecord.getAudioSessionId();
        NoiseSuppressor noiseSuppresor = NoiseSuppressor.create(sessionId);
        accelerometer.register();
//        try {
//            a= locationFinder.findLocation(speechFinalResult);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        ((AudioManager)getSystemService(Context.AUDIO_SERVICE)).setParameters("noise_suppression=on");
//        audioManager.setParameters("noise_suppression=on");
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
                if  (tx >6.0f || tx<-6.0f || ty >6.0f || ty<-6.0f || tz >4.0f || tz<-4.0f){
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
//        audioManager.setParameters("noise_suppression=on");
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
                if  (tx >16.0f || tx<-16.0f || ty >16.0f || ty<-16.0f || tz >16.0f || tz<-16.0f){
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


//    @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.FROYO)
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

                     speechFinalResult = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0);
//
//                    try {
//                        a=openNameFinder(speechFinalResult);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    do{
//                    if(a!=null){
//                    textView1.setText(a);}
//                    else {
//                        try {
//                            a=openNameFinder(speechFinalResult);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }}while (a==null);

                    try{
                        inputStream = getAssets().open("en-token.bin");
                        tokenModel = new TokenizerModel (inputStream);
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
//                        txt.setText (e.toString ()+" inside catch of token");
                    }

                    if(tokenModel!=null) {
                        TokenizerME tokenizer = new TokenizerME (tokenModel);
                        String paragraph = speechFinalResult;
                        System.out.println("speech result"+speechFinalResult);
                        String tokens[] = tokenizer.tokenize (speechFinalResult);
                        System.out.println("tokens "+Arrays.toString(tokens));
                        InputStream locationInputStream=null;
                        TokenNameFinderModel locationModel = null;
//
                        try {

                            locationInputStream = getAssets ( ).open ("en-ner-location.bin");
                            locationModel = new TokenNameFinderModel (locationInputStream);

                        } catch (IOException e) {
                            e.printStackTrace ( );
//                            txt.setText (e.toString ()+" inside catch of location");
                        }

                        if (locationModel != null) {
                            System.out.println("location model valid");
                            NameFinderME nameFinder = new NameFinderME (locationModel);
                            String var[]={"France"};
                            Span nameSpans[] = nameFinder.find (tokens);
                            String[] names = Span.spansToStrings(nameSpans, tokens);
                            System.out.println("namesss   "+Arrays.toString(names));
//                            System.out.println("namesss2   "+Arrays.toString(var));
                            StringBuilder S = new StringBuilder();
                            int l = names.length;
                            for (int j = 0; j < l; j++) {
                                S = S.append(names[j] + "\n");
                            }
//                            for( int i = 0; i<nameSpans.length; i++) {
//                                System.out.println("Span: "+nameSpans[i].toString());
//                            }
                            String sd = S.toString();
                            String result = null;
                            for (Span s : nameSpans){
                                result += s.toString ();}
                            System.out.println("results   "+sd);

                            textView1.setText (speechFinalResult);
                            maps(sd);

//                        else{
//                            // txt.setText ("Location model is empty");
//                        }
                    //                    textView1.setText(a);
//                        textView1.setText (locationFinder(speechFinalResult));

//                    setRecognitionProgressMsg(speechFinalResult);
                    try{


                        Uri uri = null;
                        uri = Uri.parse("google.navigation:q="+sd+"&mode=l");

                        Intent intent = new Intent(Intent.ACTION_VIEW,uri);

                        intent.setPackage("com.google.android.apps.maps");

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }catch (ActivityNotFoundException e) {

                        Uri uri =Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps");

                        Intent intent = new Intent(Intent.ACTION_VIEW,uri);

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        startActivity(intent);
                    }}



//
//                locationFinder=new LocationFinder(speechFinalResult,getApplicationContext());

//                    startSpeechRecognition();
                }
                    // Start droid speech recognition again
                }else
                {
                    // No match found, restart droid speech recognition
//                    startSpeechRecognition();
                }
            }


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





    private void maps(String result) {
        try{


            Uri uri = null;
            uri = Uri.parse("google.navigation:q="+result+"&mode=l");

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
    }
    }