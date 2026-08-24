package com.orbiafly.api.modules.fixedexpense.application;

public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String message) {
    super(message);
  }
}
