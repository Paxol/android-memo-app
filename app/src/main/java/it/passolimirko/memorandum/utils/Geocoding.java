package it.passolimirko.memorandum.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class Geocoding {
    private static final String TAG = "GEOCODER";

    private static final FixedCache<LatLng, String> cache = new FixedCache<>(100);
    private static final int MAX_RESULTS = 10;

    public static String getAddressForPosition(LatLng position, Context context) throws IOException {
        if (cache.containsKey(position)) {
            Log.v(TAG, "Found in cache");
            return cache.get(position);
        }

        List<Address> results = new Geocoder(context).getFromLocation(position.latitude, position.longitude, 1);
        if (results != null && results.size() > 0) {
            Address result = results.get(0);

            String address = AddressFormatter.format(result);
            cache.put(position, address);

            Log.v(TAG, "Resolved and cached");
            return address;
        }

        Log.v(TAG, "Not resolved");
        return null;
    }

    public static String getAddressForPosition(double latitude, double longitude, Context context) throws IOException {
        try {
            return getAddressForPosition(new LatLng(latitude, longitude), context);
        } catch (Exception e) {
            Log.v(TAG, "Exception");
            throw e;
        }
    }

    public static List<Address> searchAddressesForLocation(String location, Context context) throws IOException {
        return new Geocoder(context).getFromLocationName(location, MAX_RESULTS);
    }
}
