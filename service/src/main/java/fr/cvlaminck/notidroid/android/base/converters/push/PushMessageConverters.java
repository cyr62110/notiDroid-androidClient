package fr.cvlaminck.notidroid.android.base.converters.push;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import fr.cvlaminck.notidroid.android.api.push.messages.PushMessage;
import fr.cvlaminck.notidroid.android.api.push.messages.TextPushMessage;

/**
 *
 */
@EBean
public class PushMessageConverters {

    @Bean
    protected TextPushMessageConverter textPushMessageConverter;

    public PushMessage convert(fr.cvlaminck.notidroid.cloud.client.api.push.PushMessage message) {
        if(message instanceof fr.cvlaminck.notidroid.cloud.client.api.push.TextPushMessage)
            return textPushMessageConverter.convert((fr.cvlaminck.notidroid.cloud.client.api.push.TextPushMessage) message);
        else {
            throw new IllegalArgumentException("Cannot convert messages of type '" + message.getClass().getSimpleName() + "'. Those are not supported by the installed API version.");
        }
    }

    public fr.cvlaminck.notidroid.cloud.client.api.push.PushMessage convert(PushMessage message) {
        switch (message.getContentType()) {
            case TEXT:
                return textPushMessageConverter.convert((TextPushMessage) message);
            default:
                throw new IllegalArgumentException("Cannot convert messages of type '" + message.getContentType().name() + "'. Those are not supported by the installed API version.");
        }
    }

}
