package fr.cvlaminck.notidroid.android.base.services.push.runnables;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;

import fr.cvlaminck.notidroid.android.base.converters.push.PushMessageConverters;
import fr.cvlaminck.notidroid.android.base.services.push.NotidroidPushNotificationService;
import fr.cvlaminck.notidroid.android.base.services.push.callbacks.OnMessageReceivedCallbackList;
import fr.cvlaminck.notidroid.cloud.client.api.push.PushMessage;

/**
 * Runnable that will convert the message received and then call registered
 * callbacks using the OnMessageReceivedCallbackList.
 */
public class CallOnMessageReceivedCallbacksRunnable
        implements Runnable {

    private ObjectMapper objectMapper = null;

    private MqttMessage message = null;

    private PushMessageConverters pushMessageConverters = null;

    private OnMessageReceivedCallbackList onMessageReceivedCallbacks = null;

    public CallOnMessageReceivedCallbacksRunnable(ObjectMapper objectMapper, MqttMessage message,
                                                  PushMessageConverters pushMessageConverters, OnMessageReceivedCallbackList onMessageReceivedCallbacks) {
        this.objectMapper = objectMapper;
        this.message = message;
        this.pushMessageConverters = pushMessageConverters;
        this.onMessageReceivedCallbacks = onMessageReceivedCallbacks;
    }

    @Override
    public void run() {
        try {
            //First we deserialize the message
            final PushMessage pushMessage = objectMapper.readValue(message.getPayload(), PushMessage.class);
            if(NotidroidPushNotificationService.DEBUG)
                Log.d(NotidroidPushNotificationService.TAG, String.format("Message received from the message broker {type='%s'; appId=%d; from=%d; to=[%s]}",
                        pushMessage.getClass().getSimpleName(), pushMessage.getAppId(), pushMessage.getFrom(),
                        StringUtils.join(pushMessage.getTo(), ',')));

            //Then we convert it into a Android PushMessage
            final fr.cvlaminck.notidroid.android.api.push.messages.PushMessage apiPushMessage =
                    pushMessageConverters.convert(pushMessage);

            //And we call the callbacks
            onMessageReceivedCallbacks.onMessageReceived(pushMessage.getAppId(), apiPushMessage);
        } catch (IOException e) {
            //TODO Handle deserialization exception. For now print stack trace
            e.printStackTrace();
        }
    }

}
