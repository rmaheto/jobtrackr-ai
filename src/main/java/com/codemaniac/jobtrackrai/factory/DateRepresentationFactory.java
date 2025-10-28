package com.codemaniac.jobtrackrai.factory;

import com.codemaniac.jobtrackrai.dto.DateRepresentation;
import com.codemaniac.jobtrackrai.entity.UserPreference;
import jakarta.annotation.Nonnull;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DateRepresentationFactory {

  private static final String MMDDYY = "MMM dd, yyyy";

  private static final Map<String, String> DATE_FORMAT_MAP =
      Map.ofEntries(
          Map.entry("MM/DD/YYYY", "MM/dd/yyyy"),
          Map.entry("DD/MM/YYYY", "dd/MM/yyyy"),
          Map.entry("YYYY-MM-DD", "yyyy-MM-dd"),
          Map.entry("MMM DD, YYYY", MMDDYY));

  public DateRepresentation create(final Instant instant, final UserPreference pref) {
    if (instant == null) return null;

    if (pref == null) {
      log.warn("No user preferences found. Using defaults.");
      return createWithDefaults(instant);
    }

    final ZoneId zone = resolveZone(pref.getTimezone());
    final Locale locale = resolveLocale(pref.getLanguage());
    final String pattern = resolveDateFormat(pref.getDateFormat());

    final ZonedDateTime zoned = instant.atZone(zone);

    final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern).withLocale(locale);

    final DateTimeFormatter dateTimeFormatter =
        DateTimeFormatter.ofPattern(pattern + " hh:mm a").withLocale(locale);

    return new DateRepresentation(
        instant.toString(),
        zoned.toString(),
        safeFormat(dateTimeFormatter, zoned),
        safeFormat(dateFormatter, zoned),
        computeRelative(instant));
  }

  public LocalDate parseFrontendLocalDate(@Nonnull final String input) {
    if (input.isBlank()) {
      return LocalDate.now();
    }

    // Try ISO format first (the most common frontend date format)
    try {
      return LocalDate.parse(input, DateTimeFormatter.ISO_LOCAL_DATE);
    } catch (final Exception e) {
      // Try a few common alternatives for safety
      for (final String pattern : List.of("MM/dd/yyyy", "dd/MM/yyyy", "yyyy-MM-dd", MMDDYY)) {
        try {
          return LocalDate.parse(input, DateTimeFormatter.ofPattern(pattern));
        } catch (final Exception ignored) {
          // ignored
        }
      }
      log.warn("Could not parse frontend date '{}', defaulting to LocalDate.now()", input);
      return LocalDate.now();
    }
  }

  private DateRepresentation createWithDefaults(@Nonnull final Instant instant) {
    final ZonedDateTime zoned = instant.atZone(ZoneId.systemDefault());
    final DateTimeFormatter dateTime = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
    final DateTimeFormatter date = DateTimeFormatter.ofPattern(MMDDYY);

    return new DateRepresentation(
        instant.toString(),
        zoned.toString(),
        dateTime.format(zoned),
        date.format(zoned),
        computeRelative(instant));
  }

  private ZoneId resolveZone(final String tz) {
    if (tz == null || tz.isBlank()) return ZoneId.systemDefault();
    try {
      if (tz.contains("(")) {
        log.warn("Invalid timezone '{}', using system default.", tz);
        return ZoneId.systemDefault();
      }
      return ZoneId.of(tz.trim());
    } catch (final Exception e) {
      log.warn("Exception thrown while trying to resolve timezone '{}', using system default.", tz);
      return ZoneId.systemDefault();
    }
  }

  private Locale resolveLocale(final String lang) {
    if (lang == null || lang.isBlank()) return Locale.ENGLISH;
    try {
      return switch (lang.trim().toLowerCase(Locale.ROOT)) {
        case "english" -> Locale.ENGLISH;
        case "spanish" -> new Locale("es");
        case "french" -> new Locale("fr");
        case "german" -> new Locale("de");
        case "italian" -> new Locale("it");
        case "portuguese" -> new Locale("pt");
        case "chinese" -> Locale.CHINESE;
        case "japanese" -> Locale.JAPANESE;
        default -> Locale.forLanguageTag(lang);
      };
    } catch (final Exception e) {
      log.warn("Invalid language '{}', defaulting to English.", lang);
      return Locale.ENGLISH;
    }
  }

  private String resolveDateFormat(final String uiFormat) {
    if (uiFormat == null || uiFormat.isBlank()) return MMDDYY;
    final String javaPattern = DATE_FORMAT_MAP.get(uiFormat.trim().toUpperCase());
    if (javaPattern != null) return javaPattern;
    log.warn("Unknown date format '{}', defaulting to 'MMM dd, yyyy'", uiFormat);
    return MMDDYY;
  }

  private String safeFormat(
      @Nonnull final DateTimeFormatter formatter, @Nonnull final ZonedDateTime zoned) {
    try {
      return formatter.format(zoned);
    } catch (final Exception e) {
      return zoned.toString();
    }
  }

  private String computeRelative(final Instant instant) {
    final Duration diff = Duration.between(instant, Instant.now());
    final long minutes = diff.toMinutes();
    if (minutes < 1) return "just now";
    if (minutes < 60) return minutes + " minutes ago";
    final long hours = diff.toHours();
    if (hours < 24) return hours + " hours ago";
    final long days = diff.toDays();
    return days + " days ago";
  }
}
