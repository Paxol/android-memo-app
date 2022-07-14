package it.passolimirko.memorandum.utils;

import android.content.Context;

import java.io.IOException;
import java.util.List;

import it.passolimirko.memorandum.room.models.Memo;

public class MemoUtils {
    public static boolean checkLocation(Memo m, Context context) throws IOException {
        boolean updated = false;

        if (m.location == null || m.location.isEmpty()) {
            if (m.latitude == null || m.longitude == null) return updated;

            String loc = Geocoding.getAddressForPosition(m.latitude, m.longitude, context);
            m.location = loc;

            if (loc != null && !loc.isEmpty())
                updated = true;
        }

        return updated;
    }

    public static boolean checkLocation(List<Memo> memos, Context context) throws IOException {
        boolean updated = false;

        for (Memo m : memos) {
            updated = checkLocation(m, context) || updated;
        }

        return updated;
    }
}
