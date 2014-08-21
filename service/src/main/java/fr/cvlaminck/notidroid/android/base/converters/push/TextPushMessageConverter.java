package fr.cvlaminck.notidroid.android.base.converters.push;

import org.androidannotations.annotations.EBean;

import fr.cvlaminck.notidroid.android.api.push.messages.TextPushMessage;

/**
 *
 */
@EBean
public class TextPushMessageConverter
    extends PushMessageConverter<TextPushMessage, fr.cvlaminck.notidroid.cloud.client.api.push.TextPushMessage> {

    public TextPushMessageConverter() {
        super(TextPushMessage.class, fr.cvlaminck.notidroid.cloud.client.api.push.TextPushMessage.class);
    }

    @Override
    public TextPushMessage convert(fr.cvlaminck.notidroid.cloud.client.api.push.TextPushMessage mqMessage) {
        final TextPushMessage apiMessage = super.convert(mqMessage);
        final String text = (mqMessage.getText() != null) ? new String(mqMessage.getText()) : null;
        apiMessage.setText(text);
        return apiMessage;
    }

    @Override
    public fr.cvlaminck.notidroid.cloud.client.api.push.TextPushMessage convert(TextPushMessage apiMessage) {
        final fr.cvlaminck.notidroid.cloud.client.api.push.TextPushMessage mqMessage = super.convert(apiMessage);
        final String text = (apiMessage.getText() != null) ? new String(mqMessage.getText()) : null;
        mqMessage.setText(text);
        return mqMessage;
    }
}
