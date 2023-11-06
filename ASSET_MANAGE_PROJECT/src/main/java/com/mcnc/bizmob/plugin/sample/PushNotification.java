package com.mcnc.bizmob.plugin.sample;

public class PushNotification {
    private NotificationData notificationData;
    private String to;

    public PushNotification(NotificationData notificationData, String to) {
        this.notificationData = notificationData;
        this.to = to;
    }

    public NotificationData getNotificationData() {
        return notificationData;
    }

    public void setNotificationData(NotificationData notificationData) {
        this.notificationData = notificationData;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
