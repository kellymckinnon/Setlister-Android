package me.kellymckinnon.setlister;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kelly on 12/24/14.
 */
public class Utility {

    /**
     * Changes the format of a date in String format.
     *
     * @param date original date, as a String
     * @param originalFormat the format of the original date
     * @param newFormat the requested format
     *
     * @return reformatted date as a String
     */
    public static String formatDate(String date, String originalFormat, String newFormat) {
        SimpleDateFormat oldDate = new SimpleDateFormat(originalFormat);
        Date myDate = null;
        try {
            myDate = oldDate.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat newDate = new SimpleDateFormat(newFormat);
        return newDate.format(myDate);
    }

    /**
     * Change the first letter of all words to uppercase.
     * @param str string to alter
     *
     * @return altered string
     */
    public static String capitalizeFirstLetters(String str) {
        String[] words = str.split(" ");
        StringBuilder ret = new StringBuilder();
        for(int i = 0; i < words.length; i++) {
            ret.append(Character.toUpperCase(words[i].charAt(0)));
            ret.append(words[i].substring(1));
            if(i < words.length - 1) {
                ret.append(' ');
            }
        }
        return ret.toString();
    }
}
