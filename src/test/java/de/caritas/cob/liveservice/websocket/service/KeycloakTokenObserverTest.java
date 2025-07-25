package de.caritas.cob.liveservice.websocket.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.rotation.AdapterTokenVerifier;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

class KeycloakTokenObserverTest {

  private MockedStatic<KeycloakDeploymentBuilder> mockedKeycloakDeploymentBuilder;

  private MockedStatic<AdapterTokenVerifier> mockedAdapterTokenVerifier;

  @InjectMocks
  private KeycloakTokenObserver keycloakTokenObserver;

  @Mock
  private KeycloakSpringBootProperties keycloakSpringBootProperties;

  @BeforeEach
  void setUpStaticMocks() throws Exception {
    try (var mocks = MockitoAnnotations.openMocks(this)) {
      mockedKeycloakDeploymentBuilder = mockStatic(KeycloakDeploymentBuilder.class);
      mockedAdapterTokenVerifier = mockStatic(AdapterTokenVerifier.class);
    }
  }

  @AfterEach
  void tearDownStaticMocks() {
    mockedAdapterTokenVerifier.closeOnDemand();
    mockedKeycloakDeploymentBuilder.closeOnDemand();
  }

  @Test
  void observeUserId_Should_throwVerificationException_When_tokenIsNull() {
    assertThrows(VerificationException.class, () -> this.keycloakTokenObserver.observeUserId(null));
  }

  @Test
  void observeUserId_Should_throwVerificationException_When_tokenIsEmpty() {
    assertThrows(VerificationException.class, () -> this.keycloakTokenObserver.observeUserId(""));
  }

  @Test
  void observeUserId_Should_returnUserId_When_tokenIsValid()
      throws VerificationException {
    AccessToken accessToken = new AccessToken();
    accessToken.setOtherClaims("userId", "validId");
    mockedAdapterTokenVerifier.when(() -> AdapterTokenVerifier.verifyToken(any(), any())).thenReturn(accessToken);

    String userId = this.keycloakTokenObserver.observeUserId("valid token");
    assertThat(userId, is("validId"));
  }

}
