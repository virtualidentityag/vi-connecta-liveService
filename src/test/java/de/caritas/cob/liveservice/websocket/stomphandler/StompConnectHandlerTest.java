package de.caritas.cob.liveservice.websocket.stomphandler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.liveservice.websocket.exception.InvalidAccessTokenException;
import de.caritas.cob.liveservice.websocket.registry.SocketUserRegistry;
import de.caritas.cob.liveservice.websocket.service.KeycloakTokenObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.common.VerificationException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;

class StompConnectHandlerTest {

  private MockedStatic<MessageHeaderAccessor> mockedMessageHeaderAccessor;

  @InjectMocks
  private StompConnectHandler stompConnectHandler;

  @Mock
  private KeycloakTokenObserver keycloakTokenObserver;

  @Mock
  private SocketUserRegistry socketUserRegistry;

  @Mock
  private MessageHeaders messageHeaders;

  @Mock
  private StompHeaderAccessor stompHeaderAccessor;

  @Mock
  private Message<?> message;

  @BeforeEach
  void setUp() throws Exception {
    mockedMessageHeaderAccessor = mockStatic(MessageHeaderAccessor.class);
    try (var mocks = MockitoAnnotations.openMocks(this)) {
      when(messageHeaders.get(anyString())).thenReturn("header");
      when(message.getHeaders()).thenReturn(messageHeaders);
      mockedMessageHeaderAccessor.when(() -> MessageHeaderAccessor.getAccessor(any(Message.class),
              eq(StompHeaderAccessor.class)))
          .thenReturn(stompHeaderAccessor);
    }
  }

  @AfterEach
  void tearDownStaticMocks() {
    mockedMessageHeaderAccessor.closeOnDemand();
  }

  @Test
  void supportedStompCommand_Should_returnConnect() {
    var command = this.stompConnectHandler.supportedStompCommand();

    assertThat(command, is(StompCommand.CONNECT));
  }

  @Test
  void handle_Should_useNoServices_When_messageIsNull() {
    this.stompConnectHandler.handle(null);

    verifyNoInteractions(this.keycloakTokenObserver, this.socketUserRegistry);
  }

  @Test
  void handle_Should_throwInvalidAccessTokenException_When_tokenIsInvalid()
      throws VerificationException {
    when(this.stompHeaderAccessor.getFirstNativeHeader(anyString())).thenReturn("accessToken");
    when(this.keycloakTokenObserver.observeUserId(anyString()))
        .thenThrow(new VerificationException());

    assertThrows(InvalidAccessTokenException.class, () -> this.stompConnectHandler.handle(this.message));
  }

  @Test
  void handle_Should_useAllServices_When_tokenIsValid() throws VerificationException {
    when(this.stompHeaderAccessor.getFirstNativeHeader(anyString())).thenReturn("accessToken");

    this.stompConnectHandler.handle(this.message);

    verify(this.keycloakTokenObserver, times(1)).observeUserId("accessToken");
    verify(this.socketUserRegistry, times(1)).addUser(any());
  }

}
