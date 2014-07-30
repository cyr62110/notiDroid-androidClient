package fr.cvlaminck.notidroid.android.base.cloud.endpoints;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Rest;
import org.androidannotations.api.rest.MediaType;
import org.androidannotations.api.rest.RestClientRootUrl;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import fr.cvlaminck.notidroid.cloud.client.api.servers.ServerInformationResource;

@Rest(converters = {MappingJackson2HttpMessageConverter.class})
@Accept(MediaType.APPLICATION_JSON)
public interface ServerInformationEndpoint
    extends RestClientRootUrl {

    @Get("/public/info")
    public ServerInformationResource getServerInformation();

}
