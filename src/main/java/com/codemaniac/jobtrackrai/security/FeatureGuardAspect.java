package com.codemaniac.jobtrackrai.security;

import com.codemaniac.jobtrackrai.entity.User;
import com.codemaniac.jobtrackrai.enums.Feature;
import com.codemaniac.jobtrackrai.exception.ForbiddenException;
import com.codemaniac.jobtrackrai.service.CurrentUserService;
import com.codemaniac.jobtrackrai.service.UserFeatureService;
import java.lang.reflect.Method;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class FeatureGuardAspect {

  private final UserFeatureService userFeatureService;
  private final CurrentUserService currentUserService;

  @Around(
      "@annotation(com.codemaniac.jobtrackrai.security.RequiresFeature) || "
          + "@within(com.codemaniac.jobtrackrai.security.RequiresFeature)")
  public Object enforceFeatures(final ProceedingJoinPoint joinPoint) throws Throwable {

    final User user = currentUserService.getCurrentUser();
    final Set<Feature> userFeatures = userFeatureService.getFeatures(user);

    final RequiresFeature requiresFeature = resolveRequiresFeature(joinPoint);

    if (requiresFeature == null) {
      return joinPoint.proceed();
    }

    for (final Feature feature : requiresFeature.value()) {
      if (!userFeatures.contains(feature)) {
        log.debug("User {} does not have required feature {}", user.getId(), feature);
        throw new ForbiddenException("Plan upgrade required to " + feature.getDisplayName());
      }
    }

    return joinPoint.proceed();
  }

  private RequiresFeature resolveRequiresFeature(final ProceedingJoinPoint joinPoint) {

    final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    final Method method = signature.getMethod();

    final RequiresFeature annotation = method.getAnnotation(RequiresFeature.class);
    if (annotation != null) {
      return annotation;
    }

    return joinPoint.getTarget().getClass().getAnnotation(RequiresFeature.class);
  }
}
