package de.caritas.cob.liveservice.api.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserRole {
  USER("user"),
  CONSULTANT("consultant"),
  JITSI_TECHNICAL("jitsi-technical");

  private final String value;
}
