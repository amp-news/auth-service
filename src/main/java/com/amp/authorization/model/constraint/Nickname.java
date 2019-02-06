package com.amp.authorization.model.constraint;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = NicknameValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Nickname {

  String message() default
      "Invalid nickname. User nickname can contain only letters, digits and underscore or hyphen symbols.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
