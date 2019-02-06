package com.amp.authorization.model.dto;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@JsonInclude(NON_NULL)
public class ErrorTO {

  private Integer code;
  private String description;
  private Instant timestamp;
  private String path;
  private List<String> messages;

  @JsonIgnore
  public ErrorTO(HttpStatus httpStatus) {
    this.code = httpStatus.value();
    this.description = httpStatus.getReasonPhrase();
    this.timestamp = Instant.now();
    this.path = fromCurrentRequest().build().toUri().getPath();
  }

  @JsonIgnore
  public ErrorTO(String message, HttpStatus httpStatus) {
    this(httpStatus);
    this.messages = Collections.singletonList(message);
  }
}
