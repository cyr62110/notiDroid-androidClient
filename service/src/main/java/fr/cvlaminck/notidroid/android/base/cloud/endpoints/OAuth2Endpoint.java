package fr.cvlaminck.notidroid.android.base.cloud.endpoints;

import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.RequiresAuthentication;
import org.androidannotations.annotations.rest.Rest;
import org.androidannotations.api.rest.RestClientHeaders;
import org.androidannotations.api.rest.RestClientRootUrl;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import fr.cvlaminck.notidroid.cloud.client.api.oauth2.OAuth2AccessToken;

@Rest(converters = {MappingJackson2HttpMessageConverter.class})
public interface OAuth2Endpoint
    extends RestClientRootUrl, RestClientHeaders {

    @Get(value = "/token?grant_type=client_credentials")
    @RequiresAuthentication
    public OAuth2AccessToken getToken();

}
