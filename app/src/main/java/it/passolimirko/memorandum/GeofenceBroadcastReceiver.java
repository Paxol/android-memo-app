package it.passolimirko.memorandum;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("GeofenceBroadcastRece", "Received intent");

        // Start service to get location
        Intent serviceIntent = new Intent(context,GeofenceNotificationService.class);
        context.startService(serviceIntent);
    }
}
