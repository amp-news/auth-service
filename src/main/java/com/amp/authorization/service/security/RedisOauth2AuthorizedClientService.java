package com.amp.authorization.service.security;

import java.util.Base64;
import java.util.Optional;

import com.amp.authorization.model.dto.AccountTO;
import com.amp.authorization.model.security.auth.external.GoogleOauth2User;
import com.amp.authorization.model.security.auth.external.OAuth2AccessTokenWrapper;
import com.amp.authorization.repository.security.RedisRepository;
import com.amp.authorization.service.proxy.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class RedisOauth2AuthorizedClientService implements OAuth2AuthorizedClientService {

  private AccountService accountService;
  private ClientRegistrationRepository clientRegistrationRepository;
  private RedisRepository<OAuth2AccessTokenWrapper, String> oAuth2AuthorizedClientRepository;

  @Autowired
  public RedisOauth2AuthorizedClientService(
      AccountService accountService,
      ClientRegistrationRepository clientRegistrationRepository,
      RedisRepository<OAuth2AccessTokenWrapper, String> oAuth2AuthorizedClientRepository) {
    this.accountService = accountService;
    this.clientRegistrationRepository = clientRegistrationRepository;
    this.oAuth2AuthorizedClientRepository = oAuth2AuthorizedClientRepository;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(
      String clientRegistrationId, String principalName) {
    final ClientRegistration registration =
        this.clientRegistrationRepository.findByRegistrationId(clientRegistrationId);

    return (T)
        new OAuth2AuthorizedClient(
            registration,
            principalName,
            oAuth2AuthorizedClientRepository
                .load(getIdentifier(registration, principalName))
                .getAccessToken());
  }

  @Override
  public void saveAuthorizedClient(
      OAuth2AuthorizedClient authorizedClient, Authentication authentication) {
    processExternalUserSignIn((OAuth2User) authentication.getPrincipal());

    oAuth2AuthorizedClientRepository.save(
        getIdentifier(authorizedClient.getClientRegistration(), authentication.getName()),
        new OAuth2AccessTokenWrapper(authorizedClient.getAccessToken()));
  }

  @Override
  public void removeAuthorizedClient(String clientRegistrationId, String principalName) {
    final ClientRegistration registration =
        this.clientRegistrationRepository.findByRegistrationId(clientRegistrationId);

    oAuth2AuthorizedClientRepository.remove(getIdentifier(registration, principalName));
  }

  private String getIdentifier(ClientRegistration registration, String principalName) {
    String identifier = "[" + registration.getRegistrationId() + "][" + principalName + "]";
    return Base64.getEncoder().encodeToString(identifier.getBytes());
  }

  private void processExternalUserSignIn(OAuth2User principal) {
    if (principal instanceof GoogleOauth2User) {
      processGoogleUserSignIn((GoogleOauth2User) principal);
    }
  }

  private void processGoogleUserSignIn(GoogleOauth2User googleOauth2User) {
    final Optional<AccountTO> localAccount =
        accountService.loadByEmail(googleOauth2User.getEmail());

    if (localAccount.isPresent()) {
      googleOauth2User.updateFromLocal(localAccount.get());
    } else {
      Long id =
          accountService.save(
              AccountTO.builder()
                  .nickname(googleOauth2User.getNickname())
                  .password("")
                  .firstName(googleOauth2User.getFirstName())
                  .lastName(googleOauth2User.getLastName())
                  .email(googleOauth2User.getEmail())
                  .role(googleOauth2User.getRole())
                  .status(googleOauth2User.getStatus())
                  .build());

      googleOauth2User.setLocalId(id);
    }
  }
}
