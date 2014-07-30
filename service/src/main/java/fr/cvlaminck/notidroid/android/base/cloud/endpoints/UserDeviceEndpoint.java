package fr.cvlaminck.notidroid.android.base.cloud.endpoints;

import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.RequiresAuthentication;
import org.androidannotations.annotations.rest.Rest;
import org.androidannotations.api.rest.RestClientHeaders;
import org.androidannotations.api.rest.RestClientRootUrl;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import fr.cvlaminck.notidroid.cloud.client.api.devices.UserDeviceResource;

@Rest(converters = {MappingJackson2HttpMessageConverter.class})
public interface UserDeviceEndpoint
        extends RestClientRootUrl, RestClientHeaders {

    @Post(value = "/users/me/devices")
    @RequiresAuthentication
    public UserDeviceResource registerDevice(UserDeviceResource device);

}
