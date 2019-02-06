package com.amp.authorization.core.exception;

import java.util.List;

import com.amp.authorization.model.dto.ErrorTO;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ExternalServiceException extends RuntimeException {

  private HttpStatus status;
  private String path;
  private List<String> messages;

  public ExternalServiceException(ErrorTO errorTO) {
    this.status = HttpStatus.valueOf(errorTO.getCode());
    this.path = errorTO.getPath();
    this.messages = errorTO.getMessages();
  }
}
