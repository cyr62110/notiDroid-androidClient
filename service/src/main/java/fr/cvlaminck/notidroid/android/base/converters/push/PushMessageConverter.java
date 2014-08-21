package fr.cvlaminck.notidroid.android.base.converters.push;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.InvocationTargetException;

import fr.cvlaminck.notidroid.android.api.push.messages.PushMessage;

/**
 *
 */
public abstract class PushMessageConverter<A extends PushMessage, B extends fr.cvlaminck.notidroid.cloud.client.api.push.PushMessage> {

    private Class<A> apiMessageClass = null;

    private Class<B> mqMessageClass = null;

    protected PushMessageConverter(Class<A> apiMessageClass, Class<B> mqMessageClass) {
        this.apiMessageClass = apiMessageClass;
        this.mqMessageClass = mqMessageClass;
    }

    /**
     * Convert a message received through the message broker into a message
     * that can be manipulated by third-party developers using the notidroid
     * android API
     */
    public A convert(B mqMessage) {
        final A androidMsg = instantiateAndroidApiMessage();
        androidMsg.setAppId(mqMessage.getAppId());
        androidMsg.setFrom(mqMessage.getFrom());
        androidMsg.setTo(ArrayUtils.clone(mqMessage.getTo()));
        return androidMsg;
    }

    /**
     * Convert a message sent using the notidroid android API into
     *
     */
    public B convert(A apiMessage) {
        final B mqMessage = instantiateMqMessage();
        mqMessage.setAppId(apiMessage.getAppId());
        mqMessage.setFrom(apiMessage.getFrom());
        mqMessage.setTo(ArrayUtils.clone(apiMessage.getTo()));
        return mqMessage;
    }

    /**
     * Instantiate the message class that the third-party developer
     * manipulate through the Android API.
     */
    private A instantiateAndroidApiMessage() {
        try {
            return apiMessageClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Instantiate the message class that is used to communicate
     * with the message broker.
     */
    private B instantiateMqMessage() {
        try {
            return mqMessageClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
