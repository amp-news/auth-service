package com.amp.authorization.core;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import feign.FeignException;
import feign.Response;
import feign.codec.Decoder;

import static org.apache.commons.lang3.math.NumberUtils.toLong;

public class ExternalResourceDecoder implements Decoder {

  private static final String LOCATION_HEADER = "Location";

  private final Decoder decoder;

  public ExternalResourceDecoder(Decoder decoder) {
    this.decoder = decoder;
  }

  @Override
  public Object decode(Response response, Type type) throws IOException, FeignException {
    Object decodedObject = decoder.decode(response, type);

    if (Objects.isNull(decodedObject)) {
      decodedObject = tryRetrieveObjectId(response).orElse(null);
    }

    return decodedObject;
  }

  private Optional<Long> tryRetrieveObjectId(Response response) {
    Collection<String> values = response.headers().get(LOCATION_HEADER);
    if (!Objects.isNull(values)) {
      String baseUrl = response.request().url() + "/";
      String idString = values.iterator().next().replace(baseUrl, "").trim();

      Long id = toLong(idString);
      if (id != 0L) {
        return Optional.of(id);
      }
    }

    return Optional.empty();
  }
}
