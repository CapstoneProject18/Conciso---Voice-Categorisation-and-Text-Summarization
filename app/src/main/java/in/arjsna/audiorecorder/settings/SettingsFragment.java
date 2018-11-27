package in.arjsna.audiorecorder.settings;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.orhanobut.hawk.Hawk;
import in.arjsna.audiorecorder.R;
import in.arjsna.audiorecorder.libs.SettingBasic;
import in.arjsna.audiorecorder.theme.ColorsSetting;
import in.arjsna.audiorecorder.theme.ThemeHelper;
import in.arjsna.audiorecorder.theme.ThemedActivity;
import in.arjsna.audiorecorder.theme.ThemedFragment;

public class SettingsFragment extends ThemedFragment {

  private View rootView;
  private SettingBasic themeSetting;
  private ThemedActivity parent;
  private SettingBasic rateApp;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    rootView = inflater.inflate(R.layout.fragment_settings, container, false);
    parent = (ThemedActivity) getActivity();
    initViews();
    bindEvents();
    return rootView;
  }

  private void bindEvents() {
    themeSetting.setOnClickListener(v -> {
      final int originalColor = getPrimaryColor();
      new ColorsSetting((ThemedActivity) getActivity()).chooseColor(R.string.primary_color,
          new ColorsSetting.ColorChooser() {
            @Override
            public void onColorSelected(ColorsSetting.SelectedColor color) {
              Hawk.put(getString(R.string.preference_primary_color), color.colorPrimary);
              Hawk.put(getString(R.string.preference_accent_color), color.colorPrimary);
              Hawk.put(getString(R.string.preference_layer_colors), color.shades);
              parent.updateTheme();
              parent.updateUiElements();
            }

            @Override
            public void onDialogDismiss() {
              Hawk.put(getString(R.string.preference_primary_color), originalColor);
              Hawk.put(getString(R.string.preference_accent_color), originalColor);
              parent.updateTheme();
              parent.updateUiElements();
            }

            @Override
            public void onColorChanged(int color) {
              Hawk.put(getString(R.string.preference_primary_color), color);
              Hawk.put(getString(R.string.preference_accent_color), color);
              parent.updateTheme();
              parent.updateUiElements();
            }
          }, getPrimaryColor());
    });

  }

  private void initViews() {
    themeSetting = rootView.findViewById(R.id.theme_settings);

  }


  @Override public void refreshTheme(ThemeHelper themeHelper) {

  }
}
