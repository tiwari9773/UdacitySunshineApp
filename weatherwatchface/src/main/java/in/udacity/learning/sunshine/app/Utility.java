package in.udacity.learning.sunshine.app;

/**
 * Created by Lokesh on 15-08-2016.
 */
public class Utility {

    public static boolean isValidText(String text) {
        if (text == null || text.isEmpty())
            return false;
        return true;
    }
}
