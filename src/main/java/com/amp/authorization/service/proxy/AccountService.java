package com.amp.authorization.service.proxy;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;

import com.amp.authorization.model.dto.AccountTO;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "accounts-service")
public interface AccountService {

  @RequestLine("GET /users/{id}")
  AccountTO loadById(@Param("id") Long id);

  @Headers("Content-Type: application/json")
  @RequestLine("POST /users/secure")
  Long save(AccountTO dto);

  @RequestLine("GET /users/secure?search={filter}")
  List<AccountTO> loadByFilter(@NotNull @Param("filter") String filter);

  default Optional<AccountTO> loadByEmail(@NotNull String email) {
    List<AccountTO> accounts =
        loadByFilter(
            Base64.getUrlEncoder().encodeToString(String.format("email==%s", email).getBytes()));

    return accounts.isEmpty() ? Optional.empty() : Optional.of(accounts.get(0));
  }

  default boolean existsByEmail(@NotNull String email) {
    return loadByEmail(email).isPresent();
  }
}
