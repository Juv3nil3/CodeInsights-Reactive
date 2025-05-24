package com.juv3nil3.icdg.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class KeycloakTokenService {

    @Autowired
    private ReactiveOAuth2AuthorizedClientService clientService;

    public Mono<String> getAccessToken() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .cast(OAuth2AuthenticationToken.class)
                .flatMap(auth ->
                        clientService.loadAuthorizedClient(
                                auth.getAuthorizedClientRegistrationId(),
                                auth.getName()
                        )
                )
                .cast(OAuth2AuthorizedClient.class)
                .map(client -> client.getAccessToken().getTokenValue());
    }
}
