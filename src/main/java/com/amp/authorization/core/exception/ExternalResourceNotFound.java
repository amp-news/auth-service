package com.amp.authorization.core.exception;

import com.amp.authorization.model.dto.ErrorTO;

public class ExternalResourceNotFound extends ExternalServiceException {

  public ExternalResourceNotFound(ErrorTO errorTO) {
    super(errorTO);
  }
}
