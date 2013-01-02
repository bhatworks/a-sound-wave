package com.bhatworks.sound.wave;

import static java.lang.Integer.parseInt;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.bhatworks.sound.wave.SoundPlayer.Properties;

public class Main extends Activity {
	
	// for logging
	public static final String CLASS = Main.class.getSimpleName();
	public static final String PREFERENCE_FILE = "com.bhatworks.sound.wave.frequency";
	
	private SoundPlayer player;
	private ToggleButton btnMedia;
	private EditText txtFrequency;
	
	private Timer timer;
	private int frequency;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		btnMedia = (ToggleButton) findViewById(R.id.btn_media);
		txtFrequency = (EditText) findViewById(R.id.txt_freq);
		btnMedia.setText(R.string.btn_play);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Log.i(CLASS, "loading preferences");
		SharedPreferences preferences = getSharedPreferences(PREFERENCE_FILE, MODE_PRIVATE);
		frequency = preferences.getInt("frequency", 1000);
		txtFrequency.setText(String.valueOf(frequency));
		Log.d(CLASS, "frequency:" + frequency);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		try {
			frequency = parseInt(txtFrequency.getText().toString());
		} catch (NumberFormatException e) {
		}
		Log.i(CLASS, "frequency:" + frequency);
		Editor editor = getSharedPreferences(PREFERENCE_FILE, MODE_PRIVATE).edit();
		editor.putInt("frequency", frequency);
		editor.commit();
		Log.i(CLASS, "saved preferences");
	}
	
	public void onMediaClick(View v) {
		if (player == null) {
			try {
				frequency = parseInt(txtFrequency.getText().toString());
				player = new SoundPlayer();
				player.execute(new Properties[] { new SoundPlayer.Properties(frequency, 0, 200) });
				Log.i(CLASS, "Player started");
				btnMedia.setText(R.string.btn_stop);
				timer = new Timer();
				timer.schedule(new TimerTask() {
					
					@Override
					public void run() {
						if (!player.getStatus().equals(AsyncTask.Status.FINISHED)) {
							player.cancel(true);
						}
						Log.i(CLASS, "Timer expired, stopped player");
					}
				}, 5000);
				player.setHandler(new Handler(new Handler.Callback() {
					
					@Override
					public boolean handleMessage(Message msg) {
						if (msg == SoundPlayer.STOP_MESSAGE) {
							btnMedia.setText(R.string.btn_play);
						}
						return true;
					}
				}));
			} catch (NumberFormatException e) {
				Log.e(CLASS, "problem caused by user", e);
			}
		} else {
			player.cancel(true);
			Log.i(CLASS, "Player stopped");
			timer.cancel();
			timer.purge();
			player = null;
			btnMedia.setText(R.string.btn_play);
		}
	}
}