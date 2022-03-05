

package com.andryr.guitartuner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.app.Activity.*;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TunerActivity extends AppCompatActivity {

    private static final String TAG = TunerActivity.class.getCanonicalName();//мусор?

    public static final String STATE_NEEDLE_POS = "needle_pos";
    public static final String STATE_PITCH_INDEX = "pitch_index";
    public static final String STATE_LAST_FREQ = "last_freq";
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 443;


    private Tuning mTuning;// m - бесполезная вещь рекомендованная гуглом
    private AudioProcessor mAudioProcessor;//?
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();// потока для записи звука
    private NeedleView mNeedleView; //указатель
    private TuningView mTuningView;
    private TextView mFrequencyView;

    private boolean mProcessing = false;

    private int mPitchIndex;
    private float mLastFreq;


    @Override
    protected void onStart() {
        super.onStart();
        if(Utils.checkPermission(this, Manifest.permission.RECORD_AUDIO)) {
            startAudioProcessing();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mProcessing) {
            mAudioProcessor.stop();
            mProcessing = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void requestPermissions() {
        if (!Utils.checkPermission(this, Manifest.permission.RECORD_AUDIO)) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {

                DialogUtils.showPermissionDialog(this, getString(R.string.permission_record_audio), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(TunerActivity.this,
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                PERMISSION_REQUEST_RECORD_AUDIO);
                    }
                });

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        PERMISSION_REQUEST_RECORD_AUDIO);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startAudioProcessing();
                }
                break;
            }

        }
    }

    private void startAudioProcessing() {
        if (mProcessing)
            return;


        mAudioProcessor = new AudioProcessor(); //проверить можно ли объединить init с конструктором
        mAudioProcessor.init();
        mAudioProcessor.setPitchDetectionListener(new AudioProcessor.PitchDetectionListener() {
            @Override
            public void onPitchDetected(final float freq, double avgIntensity) {

                final int index = mTuning.closestPitchIndex(freq); //определяет индекс тона
                final Pitch pitch = mTuning.pitches[index];
                double interval = 1200 * Utils.log2(freq / pitch.frequency); // преобразование герцев в центы
                final float needlePos = (float) (interval / 100);
                final boolean goodPitch = Math.abs(interval) < 5.0; //вспомогательная переменная для отображения галочки
                runOnUiThread(new Runnable() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void run() {
                        mTuningView.setSelectedIndex(index, true);
                        mNeedleView.setTickLabel(0.0F, String.format("%.02fHz", pitch.frequency));// вывод частоты записи
                        mNeedleView.animateTip(needlePos);
                        mFrequencyView.setText(String.format("%.02fHz", freq));


                        final View goodPitchView = findViewById(R.id.good_pitch_view);//переделать
                        if (goodPitchView != null) {
                            if (goodPitch) {
                                if (goodPitchView.getVisibility() != View.VISIBLE) {
                                    Utils.reveal(goodPitchView);//отображаем или не отображаем галочку
                                }
                            } else if (goodPitchView.getVisibility() == View.VISIBLE) {
                                Utils.hide(goodPitchView);
                            }
                        }
                    }
                });

                mPitchIndex = index;
                mLastFreq = freq;

            }
        });
        mProcessing = true;
        mExecutor.execute(mAudioProcessor);// старт отдельного потока для записи звука
    }



    @Override
    protected void onPause() {
        super.onPause();
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.setupActivityTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//сохроняет экран включенным
        mTuning = Tuning.getTuning(this, Preferences.getString(this, getString(R.string.pref_tuning_key), getString(R.string.standard_tuning_val)));//инициализация тюнинга, в зависимости от выбранного строя

        mNeedleView = (NeedleView) findViewById(R.id.pitch_needle_view);//насйтрока отображения указателя
        mNeedleView.setTickLabel(-1.0F, "-100c");
        mNeedleView.setTickLabel(0.0F, String.format("%.02fHz", mTuning.pitches[0].frequency));
        mNeedleView.setTickLabel(1.0F, "+100c");

        int primaryTextColor = Utils.getAttrColor(this, android.R.attr.textColorPrimary);

        mTuningView = (TuningView) findViewById(R.id.tuning_view);
        mTuningView.setTuning(mTuning);


        mFrequencyView = (TextView) findViewById(R.id.frequency_view);
        mFrequencyView.setText(String.format("%.02fHz", mTuning.pitches[0].frequency));

        ImageView goodPitchView = (ImageView) findViewById(R.id.good_pitch_view);//галочка
        goodPitchView.setColorFilter(primaryTextColor);
        requestPermissions();//удалить?

    }
    //бесполезный мусор
    /*@Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putFloat(STATE_NEEDLE_POS, mNeedleView.getTipPos());
        outState.putInt(STATE_PITCH_INDEX, mPitchIndex);
        outState.putFloat(STATE_LAST_FREQ, mLastFreq);
        super.onSaveInstanceState(outState);
    }
    //бесполезный мусор
    @SuppressLint("DefaultLocale")
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mNeedleView.setTipPos(savedInstanceState.getFloat(STATE_NEEDLE_POS));
        int pitchIndex = savedInstanceState.getInt(STATE_PITCH_INDEX);
        mNeedleView.setTickLabel(0.0F, String.format("%.02fHz", mTuning.pitches[pitchIndex].frequency));
        mTuningView.setSelectedIndex(pitchIndex);
        mFrequencyView.setText(String.format("%.02fHz", savedInstanceState.getFloat(STATE_LAST_FREQ)));
    }*/

    //настройки нужно будет перенести
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                NavUtils.showSettingsActivity(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //убрать
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }


}
