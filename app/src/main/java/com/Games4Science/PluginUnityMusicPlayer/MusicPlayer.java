package com.Games4Science.PluginUnityMusicPlayer;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.unity3d.player.UnityPlayer;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayer
{
    private static final String TAG = "[aar MUSICPLAYER]";
    private static MediaPlayer mediaPlayer;

    // --- MEDIA CONTROLS ---
    public static void Play(String path) //This method is called by my Unity's MusicManager
    {
        try {
            Activity activity = UnityPlayer.currentActivity;
            if (activity == null)
            {
                Log.e(TAG, "Play() failed: Unity activity is null!");
                return;
            }

            Uri uri = path.startsWith("content://") || path.startsWith("file://")
                    ? Uri.parse(path)
                    : Uri.parse("file://" + path);


            if (mediaPlayer == null)
            {
                mediaPlayer = new MediaPlayer();
            }
            else
            {
                mediaPlayer.reset();
            }


            //mediaPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
            );

            mediaPlayer.setDataSource(activity, uri);
            mediaPlayer.setOnPreparedListener(mp -> {
                if (mp != null)
                {
                    mp.start();
                    Log.d(TAG, "Playing: " + path);
                }
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d(TAG, "Playback completed");
                UnityPlayer.UnitySendMessage("MusicManager", "OnTrackFinished", "");
            });

            mediaPlayer.prepareAsync();
        }
        catch (Exception e)
        {
            Log.e(TAG, "Play error: " + e.getMessage());
        }
    }

    public static void Pause() //This method is called by my Unity's MusicManager
    {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Log.d(TAG, "Playback paused");
        }
    }

    public static void Resume() //This method is called by my Unity's MusicManager
    {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            Log.d(TAG, "Playback resumed");
        }
    }

    public static void StopAndDispose() //This method is called by my Unity's MusicManager
    {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException ignored) {}
            try {
                mediaPlayer.release();
            } catch (Exception ignored) {}
            mediaPlayer = null;
            Log.i(TAG, "Playback stopped and released");
        }
    }

    // --- AUDIO PICKER ---
    public static void PickAudioFile(boolean allowMultiple) //This method is called by my Unity's MusicManager
    {
        Activity activity = UnityPlayer.currentActivity;
        if (activity == null) {
            Log.e(TAG, "Unity activity is null, cannot launch file picker.");
            return;
        }

        try {
            Intent intent = new Intent(activity, AudioPickerProxyActivity.class);
            intent.putExtra("allowMultiple", allowMultiple);
            activity.startActivity(intent); // proxy handles result
            Log.d(TAG, "AudioPickerProxyActivity launched (allowMultiple=" + allowMultiple + ")");
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch AudioPickerProxyActivity: " + e.getMessage());
        }
    }

    // --- DATA ACCESSORS ---
    public static List<String> GetStoredUris(Activity context) //This method is called by my Unity's MusicManager
    {
        List<String> result = new ArrayList<>();

        try
        {
            File cacheFile = new File(context.getCacheDir(), "last_picked_uris.json");
            if (!cacheFile.exists()) {
                Log.w(TAG, "No cached URI file found.");
                return result;
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(cacheFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            JSONArray jsonArray = new JSONArray(sb.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                result.add(jsonArray.getString(i));
            }

            Log.d(TAG, "Loaded " + result.size() + " URIs from cache.");
        }
        catch (Exception e)
        {
            Log.e(TAG, "Error reading cached URIs: " + e.getMessage());
        }

        return result;
    }
}