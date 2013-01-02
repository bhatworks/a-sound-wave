package com.bhatworks.sound.wave;

import static android.media.AudioFormat.CHANNEL_OUT_STEREO;
import static android.media.AudioFormat.ENCODING_PCM_16BIT;
import static android.media.AudioManager.STREAM_MUSIC;
import static android.media.AudioTrack.MODE_STREAM;
import static java.lang.Math.PI;

import org.json.JSONException;
import org.json.JSONObject;

import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.FloatMath;
import android.util.Log;

public final class SoundPlayer extends AsyncTask<SoundPlayer.Properties, Integer, Boolean> {
	
	private static final String CLASS = SoundPlayer.class.getSimpleName();
	public static final Message STOP_MESSAGE = new Message();
	
	private AudioTrack track;
	private Handler handler;
	
	@Override
	protected Boolean doInBackground(Properties... params) {
		Properties property;
		try {
			property = params[0];
		} catch (Exception e) {
			property = new Properties();
		}
		Log.d(CLASS, "Sound property: " + property);
		short[] buffer = new short[property.getMaxBufferSize()];
		int audioBufferSize = property.getAudioBufferSize();
		int samplingFreq = property.getSamplingFrequency();
		float freq = property.getFrequency();
		
		this.track = new AudioTrack(STREAM_MUSIC, samplingFreq, CHANNEL_OUT_STEREO,
				ENCODING_PCM_16BIT, audioBufferSize, MODE_STREAM);
		
		Log.d(CLASS, "track: " + this.track);
		
		// cache the constant before the loop as it increases performance
		double equationConst = 2.0 * PI * freq / samplingFreq;
		
		Log.d(CLASS, "equation constant: " + equationConst);
		
		this.track.play();
		
		int tMax = property.getMaxTime();
		
		while (!isCancelled()) {
			for (int t = property.getMinTime(); t < tMax && !isCancelled(); t++) {
				buffer[t] = (short) (FloatMath.cos((float) (t * equationConst)) * Short.MAX_VALUE);
			}
			// write to the audio buffer.... and start all over again!
			this.track.write(buffer, 0, tMax);
		}
		
		Log.d(CLASS, "releasing buffers");
		this.track.release();
		
		Log.d(CLASS, "completed playing");
		
		if (this.handler != null) {
			this.handler.dispatchMessage(STOP_MESSAGE);
		}
		
		return true;
	}
	
	@Override
	protected void onCancelled() {
		if (track.getState() != AudioTrack.STATE_UNINITIALIZED) {
			if (track.getPlayState() != AudioTrack.PLAYSTATE_STOPPED) {
				if (track.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
					this.track.pause();
				}
				this.track.flush();
			}
			this.track.stop();
		}
		super.onCancelled();
	}
	
	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	
	public static final class Properties {
		private int frequency = 800;
		private int samplingFrequency = 44100;
		private int minTime = 0;
		private int maxTime = 200;
		private int audioBufferSize = 1024;
		private int maxBufferSize = 2048;
		
		public Properties() {
		}
		
		public Properties(int frequency, int minTime, int maxTime) {
			super();
			this.frequency = frequency;
			this.minTime = minTime;
			this.maxTime = maxTime;
		}
		
		public Properties(int frequency, int samplingFrequency, int minTime, int maxTime) {
			super();
			this.frequency = frequency;
			this.samplingFrequency = samplingFrequency;
			this.minTime = minTime;
			this.maxTime = maxTime;
		}
		
		public Properties(int frequency, int samplingFrequency, int minTime, int maxTime,
				int minBufferSize, int maxBufferSize) {
			super();
			this.frequency = frequency;
			this.samplingFrequency = samplingFrequency;
			this.minTime = minTime;
			this.maxTime = maxTime;
			this.audioBufferSize = minBufferSize;
			this.maxBufferSize = maxBufferSize;
		}
		
		public int getFrequency() {
			return frequency;
		}
		
		public void setFrequency(int frequency) {
			this.frequency = frequency;
		}
		
		public int getSamplingFrequency() {
			return samplingFrequency;
		}
		
		public void setSamplingFrequency(int samplingFrequency) {
			this.samplingFrequency = samplingFrequency;
		}
		
		public int getMinTime() {
			return minTime;
		}
		
		public void setMinTime(int minTime) {
			this.minTime = minTime;
		}
		
		public int getMaxTime() {
			return maxTime;
		}
		
		public void setMaxTime(int maxTime) {
			this.maxTime = maxTime;
		}
		
		public int getAudioBufferSize() {
			return audioBufferSize;
		}
		
		public void setAudioBufferSize(int minBufferSize) {
			this.audioBufferSize = minBufferSize;
		}
		
		public int getMaxBufferSize() {
			return maxBufferSize;
		}
		
		public void setMaxBufferSize(int maxBufferSize) {
			this.maxBufferSize = maxBufferSize;
		}
		
		@Override
		public String toString() {
			try {
				return new JSONObject().put("frequency", frequency)
						.put("samplingFrequency", samplingFrequency).put("minTime", minTime)
						.put("maxTime", maxTime).put("audioBufferSize", audioBufferSize)
						.put("maxBufferSize", maxBufferSize).toString(2);
			} catch (JSONException e) {
				return "had parsing error";
			}
		}
		
	}
}