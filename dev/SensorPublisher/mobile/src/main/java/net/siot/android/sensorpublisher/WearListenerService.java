package net.siot.android.sensorpublisher;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.wearable.MessageEvent;

/**
 * Created by Sathesh on 15.12.15.
 * Message listener. Processes messages received from a paired android wear device.
 */
public class WearListenerService extends com.google.android.gms.wearable.WearableListenerService {

    public static final String START_PHONESENSORPUB = "startPhoneSensorPublisher";

    /**
     * Listens to messages sent by a wearable device. Starts the PhoneSensorPublisher activity if it is not already started.
     * Creates an intent container which is passed to the main activity (PhoneSensorPublisher) through the LocalBroadcastManager.
     * @param messageEvent received message event
     */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra("path", messageEvent.getPath());
        intent.putExtra("value", new String(messageEvent.getData()));
        intent.putExtra("nodeId", messageEvent.getSourceNodeId());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        if (messageEvent.getPath().equals(START_PHONESENSORPUB)) {

            Intent startIntent;
            startIntent = new Intent(this, PhoneSensorPublisherActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        }

    }
}

