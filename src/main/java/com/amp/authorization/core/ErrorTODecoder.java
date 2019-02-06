package com.amp.authorization.core;

import java.io.IOException;
import java.io.Reader;

import com.amp.authorization.core.exception.ExternalResourceNotFound;
import com.amp.authorization.core.exception.ExternalResourceNotValid;
import com.amp.authorization.core.exception.ExternalServiceException;
import com.amp.authorization.model.dto.ErrorTO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;

public class ErrorTODecoder implements ErrorDecoder {

  private ObjectMapper objectMapper;

  public ErrorTODecoder() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.findAndRegisterModules();
    this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Override
  public Exception decode(String methodKey, Response response) {
    final ErrorTO error = retrieveError(response);

    switch (HttpStatus.valueOf(error.getCode())) {
      case NOT_FOUND:
        throw new ExternalResourceNotFound(error);
      case UNPROCESSABLE_ENTITY:
        throw new ExternalResourceNotValid(error);
      default:
        throw new ExternalServiceException(error);
    }
  }

  private ErrorTO retrieveError(Response response) {
    ErrorTO error = null;

    try (Reader responseReader = response.body().asReader()) {
      error = objectMapper.readValue(responseReader, ErrorTO.class);
    } catch (IOException e) {
      System.out.println(
          "Cannot read response from the external service: " + response.request().url());
    }

    return error;
  }
}
