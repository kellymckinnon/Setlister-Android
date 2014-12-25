package me.kellymckinnon.setlister;

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
}
