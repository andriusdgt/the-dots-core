package com.andriusdgt.thedots.core.annotation;

import javax.validation.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = { })
@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
@Target(FIELD)
@Retention(RUNTIME)
@Min(0)
@Max(Long.MAX_VALUE)
@ReportAsSingleViolation
public @interface Range {
	@OverridesAttribute(constraint = Min.class, name = "value") long min() default Long.MIN_VALUE;

	@OverridesAttribute(constraint = Max.class, name = "value") long max() default Long.MAX_VALUE;

	String message() default "{com.andriusdgt.thedots.core.annotation.Range.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

}
