package com.codemaniac.jobtrackrai.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class PaginationHelper {

  private PaginationHelper() {}

  public static Pageable normalizePageable(final Pageable pageable) {
    final int adjustedPage = Math.max(0, pageable.getPageNumber() - 1);
    return PageRequest.of(adjustedPage, pageable.getPageSize(), pageable.getSort());
  }
}
