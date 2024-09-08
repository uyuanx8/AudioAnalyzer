package com.example.audioanalyzer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import org.jtransforms.fft.DoubleFFT_1D;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int SamplingRate = 44100; // 44.1 kHz

    private AudioRecord audioRecord;
    TextView infoTextView;

    AudioTrack audioTrack;

    AudioDataVisualizer audioVisualizer;
    FFTVisualizer fftVisualizer;

    DoubleFFT_1D fftTransform;
    //직접 구현을 왜하지? :)

    boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startButton = findViewById(R.id.start_button);
        infoTextView = findViewById(R.id.info_text);
        audioVisualizer = findViewById(R.id.audio_visualizer);
        fftVisualizer = findViewById(R.id.fft_visualizer);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording){
                    if (checkPermission()) {
                        startRecording();
                        isRecording = true;
                        startButton.setText("Stop");
                    }
                }
                else{
                    stopRecording();
                    isRecording = false;
                    startButton.setText("Start");
                }
            }
        });
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);

            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecording();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    //get audio recording
    private void startRecording() {
        int bufferSize = AudioRecord.getMinBufferSize(SamplingRate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        int f = 1;
        while(bufferSize > f){
            f = f<<1;
        }
        final int fftSize = f;
        fftTransform = new DoubleFFT_1D(fftSize);


        audioRecord = null;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SamplingRate,
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SamplingRate,
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, SamplingRate, AudioTrack.MODE_STREAM);

        if (audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e("Audio", "AudioRecord initialization failed");
            return;
        }

//        audioTrack.play();
        audioRecord.startRecording();

        // Start a thread to continuously read audio data and display information
        new Thread(new Runnable() {
            @Override
            public void run() {
                short[] buffer = new short[bufferSize];
                double[] bufferDb = new double[fftSize];
                int min = 0;
                int max = 0;
                while (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    int readBytes = audioRecord.read(buffer, 0, bufferSize);
                    bufferDb = new double[fftSize<<1];
                    if (readBytes > 0) {
                        min = 0;
                        max = 0;
                        for (int i=0;i<buffer.length;i++){
                            bufferDb[i] = (double)buffer[i] / Short.MAX_VALUE;
                            if(buffer[i] > max) { max = buffer[i]; }
                            if(buffer[i] < min) { min = buffer[i]; }
                        }
                        fftTransform.realForwardFull(bufferDb);
                    }
                    updateInfoTextView(max + " " + min + "\n" + calculateRMS(buffer));
                    updateChart(buffer, bufferDb);
                }
            }
        }).start();
    }
    //stop
    private void stopRecording() {
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }
    
    //데이터 어케저케 하는곳
    private double calculateRMS(short[] buffer) {
        double sum = 0;
        for (short b : buffer) {
            sum += Math.pow(b, 2);
        }

        return Math.sqrt(sum / buffer.length);
    }

    private void updateInfoTextView(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                infoTextView.setText(text);
            }
        });
    }
    private void updateChart(final short[] buffer, final double[] bufferDb){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                audioVisualizer.setList(buffer);
                fftVisualizer.setList(bufferDb);
            }
        });
    }
}