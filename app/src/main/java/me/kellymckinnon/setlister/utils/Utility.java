package me.kellymckinnon.setlister.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.net.Uri;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import me.kellymckinnon.setlister.R;

/** General utility class */
public class Utility {

  /**
   * Changes the format of a date in String format.
   *
   * @param date original date, as a String
   * @param originalFormat the format of the original date
   * @param newFormat the requested format
   * @return reformatted date as a String
   */
  public static String formatDate(String date, String originalFormat, String newFormat) {
    SimpleDateFormat oldDate = new SimpleDateFormat(originalFormat, Locale.getDefault());
    Date myDate = null;
    try {
      myDate = oldDate.parse(date);
    } catch (ParseException e) {
      e.printStackTrace();
    }

    if (myDate == null) {
      return "";
    }

    SimpleDateFormat newDate = new SimpleDateFormat(newFormat, Locale.getDefault());
    return newDate.format(myDate);
  }

  /**
   * Change the first letter of all words to uppercase.
   *
   * @param str string to alter
   * @return altered string
   */
  public static String capitalizeFirstLetters(String str) {
    String[] words = str.split(" ");
    StringBuilder ret = new StringBuilder();
    for (int i = 0; i < words.length; i++) {
      ret.append(Character.toUpperCase(words[i].charAt(0)));
      ret.append(words[i].substring(1));
      if (i < words.length - 1) {
        ret.append(' ');
      }
    }
    return ret.toString();
  }

  /** Check if the network is connected. */
  public static boolean isNetworkConnected(Context context) {
    if (context == null) {
      return true;
    }

    ConnectivityManager cm =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeNetwork = cm != null ? cm.getActiveNetworkInfo() : null;
    return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
  }

  public static void showAboutDialog(Context context) {
    new MaterialAlertDialogBuilder(context)
        .setTitle(R.string.about_setlister)
        .setView(R.layout.about_dialog)
        .show();
  }

  public static void startFeedbackEmail(Activity callingActivity) {
    Intent emailIntent =
        new Intent(
            Intent.ACTION_SENDTO, Uri.fromParts("mailto", callingActivity.getString(R.string.email), null));
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, callingActivity.getString(R.string.feedback_subject));
    callingActivity.startActivity(Intent.createChooser(emailIntent, "Send email..."));
  }
}
