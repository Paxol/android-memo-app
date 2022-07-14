package it.passolimirko.memorandum.utils;

import android.location.Address;

public class AddressFormatter {
    public static String format(Address addr) {
        StringBuilder sb = new StringBuilder();

        if (addr.getThoroughfare() != null)
            sb.append(addr.getThoroughfare()).append(", ");

        if (addr.getSubThoroughfare() != null)
            sb.append(addr.getSubThoroughfare()).append(", ");

        if (addr.getLocality() != null)
            sb.append(addr.getLocality());

        return sb.toString();
    }
}
