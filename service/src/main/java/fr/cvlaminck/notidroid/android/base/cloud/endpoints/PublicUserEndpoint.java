package fr.cvlaminck.notidroid.android.base.cloud.endpoints;

import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.Rest;
import org.androidannotations.api.rest.RestClientRootUrl;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import fr.cvlaminck.notidroid.cloud.client.api.users.UserResource;
import fr.cvlaminck.notidroid.cloud.client.api.users.UserWithCredentialsResource;

@Rest(converters = {MappingJackson2HttpMessageConverter.class})
public interface PublicUserEndpoint
        extends RestClientRootUrl {

    @Post(value = "/public/users")
    public UserResource createOnlineAccount(UserWithCredentialsResource userInformation);

}
