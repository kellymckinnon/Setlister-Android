package me.kellymckinnon.setlister;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Used to get a json String from a file
 */
class AssetReader {

  static String getStringFromFile(Context context, String filePath) throws Exception {
    final InputStream stream = context.getResources().getAssets().open(filePath);

    String ret = convertStreamToString(stream);
    stream.close();
    return ret;
  }

  private static String convertStreamToString(InputStream is) throws Exception {
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      sb.append(line).append("\n");
    }
    reader.close();
    return sb.toString();
  }
}
