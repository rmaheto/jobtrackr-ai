package com.codemaniac.jobtrackrai.security;

import com.codemaniac.jobtrackrai.entity.User;
import com.codemaniac.jobtrackrai.enums.Feature;
import com.codemaniac.jobtrackrai.exception.ForbiddenException;
import com.codemaniac.jobtrackrai.service.CurrentUserService;
import com.codemaniac.jobtrackrai.service.UserFeatureService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class FeatureGuardAspect {

  private final UserFeatureService userFeatureService;
  private final CurrentUserService currentUserService;

  @Around("@annotation(requiresFeature) || @within(requiresFeature)")
  public Object enforceFeatures(
      final ProceedingJoinPoint joinPoint, final RequiresFeature requiresFeature) throws Throwable {

    final User user = currentUserService.getCurrentUser();
    final Set<Feature> userFeatures = userFeatureService.getFeatures(user);

    final Feature[] requiredFeatures = requiresFeature.value();

    for (final Feature feature : requiredFeatures) {
      if (!userFeatures.contains(feature)) {
        throw new ForbiddenException("Feature required: " + feature.getDisplayName());
      }
    }

    return joinPoint.proceed();
  }
}
