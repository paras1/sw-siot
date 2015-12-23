package net.siot.android.sensorpublisher;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.wearable.MessageEvent;

/**
 * Created by Sathesh on 15.12.15.
 * Message listener. Processes messages received from the companion android mobile device.
 */
public class MobileListenerService extends com.google.android.gms.wearable.WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra("path", messageEvent.getPath());
        intent.putExtra("value", new String(messageEvent.getData()));
        intent.putExtra("nodeId", messageEvent.getSourceNodeId());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);


    }
}

