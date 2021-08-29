package com.example.musicplayer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class playerActivity extends AppCompatActivity {


    private ObjectAnimator anim;
    ImageView playbtn , btnnxt , btnprv , btnff, btnfr;
    TextView txtsname , txtsstart , txtsstop;
    SeekBar seekBar;
    BarVisualizer visualizer;
    ImageView imageView;

    String sname;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;
    Thread updateseekbar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home);{
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(visualizer != null){
            visualizer.release();
        }
        super.onDestroy();
//        mediaPlayer.stop();
//        updateseekbar.interrupt();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        ImageView someImage = (ImageView) findViewById(R.id.imageview);
        anim = ObjectAnimator.ofFloat(someImage, "rotation", 0, 360);

        anim.setDuration(7000);
        anim.setRepeatCount(ObjectAnimator.INFINITE);
        anim.setRepeatMode(ObjectAnimator.RESTART);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        btnprv = findViewById(R.id.btnprv);
        btnnxt = findViewById(R.id.btnnxt);
        playbtn = findViewById(R.id.playbtn);
        btnff= findViewById(R.id.btnff);
        btnfr = findViewById(R.id.btnfr);
        txtsname= findViewById(R.id.txtsn);
        txtsstop= findViewById(R.id.txtsstop);
        txtsstart= findViewById(R.id.txtsstart);
        seekBar = findViewById(R.id.seekbar);
        visualizer = findViewById(R.id.blast);
//        imageView = findViewById(R.id.imageview);


        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String songName = i.getStringExtra("songname");
        position = bundle.getInt("pos",0);

        txtsname.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        sname = mySongs.get(position).getName();
        txtsname.setText(sname);

        mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();
        startAnimation(imageView);
        updateseekbar = new Thread(){
            @Override
            public void run() {
                super.run();
                int totalDuration = mediaPlayer.getDuration();
                int currentposition = 0;
                while ( currentposition < totalDuration){
                    try {
                        sleep(500);
                        currentposition= mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentposition);
                    }
                    catch (InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }
                }
            }
        };

        seekBar.setMax(mediaPlayer.getDuration());
        updateseekbar.start();
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.white),PorterDuff.Mode.SRC_IN);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime = createTime(mediaPlayer.getDuration());
        txtsstop.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                txtsstart.setText(currentTime);
                handler.postDelayed(this,delay);
            }
        },delay);


        playbtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    playbtn.setBackgroundResource(R.drawable.ic_play);
                    pauseAnimation(imageView);
                    mediaPlayer.pause();
                }
                else {
                    playbtn.setBackgroundResource(R.drawable.ic_pause_);
                    resumeAnimation(imageView);
                    mediaPlayer.start();
                }
            }
        });

        btnnxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();

                position = ((position+1)%mySongs.size());
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),u);

                sname = mySongs.get(position).getName();
                txtsname.setText(sname);
                mediaPlayer.start();

                playbtn.setBackgroundResource(R.drawable.ic_pause_);
                startAnimation(imageView);

                int audiosessionID = mediaPlayer.getAudioSessionId();
                if(audiosessionID != -1){
                    visualizer.setAudioSessionId(audiosessionID);
                }

            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                btnnxt.performClick();
            }
        });

        int audiosessionID = mediaPlayer.getAudioSessionId();
        if(audiosessionID != -1){
            visualizer.setAudioSessionId(audiosessionID);
        }

        btnprv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();

                position = ((position-1)<0)?(mySongs.size()-1):(position-1);
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),u);

                sname = mySongs.get(position).getName();
                txtsname.setText(sname);
                mediaPlayer.start();

                playbtn.setBackgroundResource(R.drawable.ic_pause_);
                startAnimation(imageView);

                int audiosessionID = mediaPlayer.getAudioSessionId();
                if(audiosessionID != -1){
                    visualizer.setAudioSessionId(audiosessionID);
                }

            }
        });

        btnff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });

        btnfr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }
            }
        });
    }

//    public void startAnimation(View view){
//        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView,"rotation",0f,360f);
//        animator.setDuration(7000);
//        AnimatorSet animatorSet = new AnimatorSet();
//        animatorSet.playTogether(animator);
//        animatorSet.start();
//        animator.setRepeatCount(ObjectAnimator.INFINITE);
//    }

    public void startAnimation(View view) {
        anim.start();
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void pauseAnimation(View view) {
        anim.pause();
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void resumeAnimation(View view) {
        anim.resume();
    }

    public  String createTime(int duration){
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        time += min+":";
        if(sec<10)
        {
            time +="0" ;
        }
        time += sec;

        return time;
    }
}