package in.arjsna.audiorecorder.playback;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import in.arjsna.audiorecorder.R;
import in.arjsna.audiorecorder.db.RecordingItem;
import in.arjsna.audiorecorder.theme.ThemeHelper;
import in.arjsna.audiorecorder.theme.ThemedDialogFragment;
import in.arjsna.audiorecorder.theme.ThemedFab;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class PlaybackFragment extends ThemedDialogFragment {

  private static final String LOG_TAG = "PlaybackFragment";

  private static final String ARG_ITEM = "recording_item";
  private RecordingItem item;

  private final Handler mHandler = new Handler();

  private MediaPlayer mMediaPlayer = null;

  private AppCompatSeekBar mSeekBar = null;
  private ThemedFab mPlayButton = null;
  private TextView mCurrentProgressTextView = null;
  private TextView mFileLengthTextView = null;

  //stores whether or not the mediaplayer is currently playing audio
  private boolean isPlaying = false;

  //stores minutes and seconds of the length of the file.
  private long minutes = 0;
  private long seconds = 0;

  public PlaybackFragment newInstance(RecordingItem item) {
    PlaybackFragment f = new PlaybackFragment();
    Bundle b = new Bundle();
    b.putParcelable(ARG_ITEM, item);
    f.setArguments(b);

    return f;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    item = getArguments().getParcelable(ARG_ITEM);

    long itemDuration = item.getLength();
    minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
    seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration) - TimeUnit.MINUTES.toSeconds(minutes);
  }

  @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {

    Dialog dialog = super.onCreateDialog(savedInstanceState);

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_media_playback, null);

    TextView mFileNameTextView = view.findViewById(R.id.file_name_text_view);
    mFileLengthTextView = view.findViewById(R.id.file_length_text_view);
    mCurrentProgressTextView = view.findViewById(R.id.current_progress_text_view);

    mSeekBar = view.findViewById(R.id.seekbar);
    ColorFilter filter = new LightingColorFilter(getResources().getColor(R.color.primary),
        getResources().getColor(R.color.primary));
    mSeekBar.getProgressDrawable().setColorFilter(filter);
    mSeekBar.getThumb().setColorFilter(filter);

    mSeekBar.setOnSeekBarChangeListener(new AppCompatSeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mMediaPlayer != null && fromUser) {
          mMediaPlayer.seekTo(progress);
          mHandler.removeCallbacks(mRunnable);

          long minutes = TimeUnit.MILLISECONDS.toMinutes(mMediaPlayer.getCurrentPosition());
          long seconds = TimeUnit.MILLISECONDS.toSeconds(mMediaPlayer.getCurrentPosition())
              - TimeUnit.MINUTES.toSeconds(minutes);
          mCurrentProgressTextView.setText(
              String.format(getString(R.string.play_time_format), minutes, seconds));

          updateSeekBar();
        } else if (mMediaPlayer == null && fromUser) {
          prepareMediaPlayerFromPoint(progress);
          updateSeekBar();
        }
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
        if (mMediaPlayer != null) {
          // remove message Handler from updating progress bar
          mHandler.removeCallbacks(mRunnable);
        }
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
        if (mMediaPlayer != null) {
          mHandler.removeCallbacks(mRunnable);
          mMediaPlayer.seekTo(seekBar.getProgress());

          long minutes = TimeUnit.MILLISECONDS.toMinutes(mMediaPlayer.getCurrentPosition());
          long seconds = TimeUnit.MILLISECONDS.toSeconds(mMediaPlayer.getCurrentPosition())
              - TimeUnit.MINUTES.toSeconds(minutes);
          mCurrentProgressTextView.setText(
              String.format(getString(R.string.play_time_format), minutes, seconds));
          updateSeekBar();
        }
      }
    });

    mPlayButton = view.findViewById(R.id.fab_play);
    mPlayButton.setOnClickListener(v -> {
      onPlay(isPlaying);
      isPlaying = !isPlaying;
    });

    mFileNameTextView.setText(item.getName());
    mFileLengthTextView.setText(
        String.format(getString(R.string.play_time_format), minutes, seconds));

    builder.setView(view);

    // request a window without the title
    dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

    return builder.create();
  }

  @Override public void onStart() {
    super.onStart();

    //set transparent background
    Window window = getDialog().getWindow();
    window.setBackgroundDrawableResource(android.R.color.transparent);

    //disable buttons from dialog
    AlertDialog alertDialog = (AlertDialog) getDialog();
    alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
    alertDialog.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(false);
    alertDialog.getButton(Dialog.BUTTON_NEUTRAL).setEnabled(false);
  }

  @Override public void onPause() {
    super.onPause();

    if (mMediaPlayer != null) {
      stopPlaying();
    }
  }

  @Override public void onDestroy() {
    super.onDestroy();

    if (mMediaPlayer != null) {
      stopPlaying();
    }
  }

  // Play start/stop
  private void onPlay(boolean isPlaying) {
    if (!isPlaying) {
      //currently MediaPlayer is not playing audio
      if (mMediaPlayer == null) {
        startPlaying(); //start from beginning
      } else {
        resumePlaying(); //resume the currently paused MediaPlayer
      }
    } else {
      //pause the MediaPlayer
      pausePlaying();
    }
  }

  private void startPlaying() {
    mPlayButton.setImageResource(R.drawable.ic_media_pause);
    mMediaPlayer = new MediaPlayer();

    try {
      mMediaPlayer.setDataSource(item.getFilePath());
      mMediaPlayer.prepare();
      mSeekBar.setMax(mMediaPlayer.getDuration());

      mMediaPlayer.setOnPreparedListener(mp -> mMediaPlayer.start());
    } catch (IOException e) {
      Log.e(LOG_TAG, "prepare() failed");
    }

    mMediaPlayer.setOnCompletionListener(mp -> stopPlaying());

    updateSeekBar();

    //keep screen on while playing audio
    getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  }

  private void prepareMediaPlayerFromPoint(int progress) {
    //set mediaPlayer to start from middle of the audio file

    mMediaPlayer = new MediaPlayer();

    try {
      mMediaPlayer.setDataSource(item.getFilePath());
      mMediaPlayer.prepare();
      mSeekBar.setMax(mMediaPlayer.getDuration());
      mMediaPlayer.seekTo(progress);

      mMediaPlayer.setOnCompletionListener(mp -> stopPlaying());
    } catch (IOException e) {
      Log.e(LOG_TAG, "prepare() failed");
    }

    //keep screen on while playing audio
    getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  }

  private void pausePlaying() {
    mPlayButton.setImageResource(R.drawable.ic_media_play);
    mHandler.removeCallbacks(mRunnable);
    mMediaPlayer.pause();
  }

  private void resumePlaying() {
    mPlayButton.setImageResource(R.drawable.ic_media_pause);
    mHandler.removeCallbacks(mRunnable);
    mMediaPlayer.start();
    updateSeekBar();
  }

  private void stopPlaying() {
    mPlayButton.setImageResource(R.drawable.ic_media_play);
    mHandler.removeCallbacks(mRunnable);
    mMediaPlayer.stop();
    mMediaPlayer.reset();
    mMediaPlayer.release();
    mMediaPlayer = null;

    mSeekBar.setProgress(mSeekBar.getMax());
    isPlaying = !isPlaying;

    mCurrentProgressTextView.setText(mFileLengthTextView.getText());
    mSeekBar.setProgress(mSeekBar.getMax());

    //allow the screen to turn off again once audio is finished playing
    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  }

  //updating mSeekBar
  private final Runnable mRunnable = () -> {
    if (mMediaPlayer != null) {

      int mCurrentPosition = mMediaPlayer.getCurrentPosition();
      Log.i("Seekbar", " " + mCurrentPosition);
      mSeekBar.setProgress(mCurrentPosition);

      long minutes = TimeUnit.MILLISECONDS.toMinutes(mCurrentPosition);
      long seconds =
          TimeUnit.MILLISECONDS.toSeconds(mCurrentPosition) - TimeUnit.MINUTES.toSeconds(minutes);
      mCurrentProgressTextView.setText(
          String.format(getString(R.string.play_time_format), minutes, seconds));

      updateSeekBar();
    }
  };

  private void updateSeekBar() {
    mHandler.postDelayed(mRunnable, 1000);
  }

  @Override public void refreshTheme(ThemeHelper themeHelper) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      this.mSeekBar.setThumbTintList(ColorStateList.valueOf(themeHelper.getAccentColor()));
      this.mSeekBar.setProgressTintList(ColorStateList.valueOf(themeHelper.getAccentColor()));
    }
    this.mPlayButton.setBackgroundTintList(ColorStateList.valueOf(themeHelper.getAccentColor()));
  }
}
