package in.arjsna.audiorecorder.theme;

import android.content.Context;
import android.support.v4.app.Fragment;

public abstract class ThemedFragment extends Fragment implements Themed {

  ThemeHelper themeHelper;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
        /*if (mContext instanceof ThemedActivity)
            themeHelper = ((ThemedActivity) mContext).getThemeHelper();*/
    themeHelper = ThemeHelper.getInstance(getContext());
  }

  @Override
  public void onResume() {
    super.onResume();
    themeHelper.updateTheme();
    refreshTheme(themeHelper);
  }

  public ThemeHelper getThemeHelper() {
    return themeHelper;
  }

  public int getPrimaryColor() {
    return themeHelper.getPrimaryColor();
  }

  public int getAccentColor() {
    return themeHelper.getAccentColor();
  }

  public Theme getBaseTheme() {
    return themeHelper.getBaseTheme();
  }

  public int getBackgroundColor() {
    return themeHelper.getBackgroundColor();
  }

  public int getCardBackgroundColor() {
    return themeHelper.getCardBackgroundColor();
  }

  public int getTextColor() {
    return themeHelper.getTextColor();
  }
}
