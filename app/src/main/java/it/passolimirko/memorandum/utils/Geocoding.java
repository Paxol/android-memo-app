package it.passolimirko.memorandum.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class Geocoding {
    private static final String TAG = "GEOCODER";

    private static final FixedCache<LatLng, String> cache = new FixedCache<>(100);

    public static String getAddressForLocation(LatLng loc, Context context) throws IOException {
        if (cache.containsKey(loc)) {
            Log.v(TAG, "Found in cache");
            return cache.get(loc);
        }

        List<Address> result = new Geocoder(context).getFromLocation(loc.latitude, loc.longitude, 1);
        if (result != null && result.size() > 0) {
            String locality = result.get(0).getLocality();

            cache.put(loc, locality);

            Log.v(TAG, "Resolved and put in cache");
            return locality;
        }

        Log.v(TAG, "Not resolved");
        return null;
    }

    public static String getAddressForLocation(double latitude, double longitude, Context context) throws IOException {
        try {
            return getAddressForLocation(new LatLng(latitude, longitude), context);
        } catch (Exception e) {
            Log.v(TAG, "Exception");
            throw e;
        }
    }
}
