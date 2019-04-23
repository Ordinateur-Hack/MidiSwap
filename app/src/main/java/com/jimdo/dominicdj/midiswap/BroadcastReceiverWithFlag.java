package com.jimdo.dominicdj.midiswap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

// see https://stackoverflow.com/a/29836639
public abstract class BroadcastReceiverWithFlag extends BroadcastReceiver {

    private boolean isRegistered = false;

    /**
     * Register this receiver, but only if it is not already registered.
     *
     * @param context
     * @param filter
     * @return see {@link Context#registerReceiver(BroadcastReceiver, IntentFilter)}
     */
    public Intent register(Context context, IntentFilter filter) {
        try {
            return !isRegistered ? context.registerReceiver(this, filter) : null;
        } finally {
            isRegistered = true;
        }
    }

    /**
     * Unregister this receiver.
     *
     * @param context
     * @return if it has been registered before or not
     */
    public boolean unregister(Context context) {
        // we can unregister, even if receiver isn't registered (to be really save) // TODO: really?
        context.unregisterReceiver(this);
        try {
            return isRegistered;
        } finally {
            isRegistered = false;
        }
    }

    /**
     * @param context
     * @return if this receiver is registered on the given context
     */
    public boolean isRegistered(Context context) {
        return isRegistered;
    }

}
