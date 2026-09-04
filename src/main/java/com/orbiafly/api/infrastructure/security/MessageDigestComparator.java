package com.orbiafly.api.infrastructure.security;

import java.security.MessageDigest;

final class MessageDigestComparator {

  private MessageDigestComparator() {
  }

  static boolean matches(String expected, String actual) {
    return MessageDigest.isEqual(expected.getBytes(), actual.getBytes());
  }
}
