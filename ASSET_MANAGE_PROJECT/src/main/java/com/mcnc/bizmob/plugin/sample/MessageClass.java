package com.mcnc.bizmob.plugin.sample;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessageClass extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage Message) {
        super.onMessageReceived(Message);
    }
}
