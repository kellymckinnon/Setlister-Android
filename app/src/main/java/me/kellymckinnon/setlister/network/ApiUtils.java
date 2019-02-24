package me.kellymckinnon.setlister.network;

/**
 * Utility class to get an instance of either a SetlistFMService or a Spotify service.
 */
public class ApiUtils {
    public static final String SETLIST_FM_BASE_URL = "https://api.setlist.fm/rest/1.0/";

    public static SetlistFMService getSetlistFMService() {
        return RetrofitClient.getClient(SETLIST_FM_BASE_URL).create(SetlistFMService.class);
    }
}
