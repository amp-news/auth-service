package com.amp.authorization.core.exception;

import com.amp.authorization.model.dto.ErrorTO;

public class ExternalResourceNotValid extends ExternalServiceException {

  public ExternalResourceNotValid(ErrorTO errorTO) {
    super(errorTO);
  }
}
