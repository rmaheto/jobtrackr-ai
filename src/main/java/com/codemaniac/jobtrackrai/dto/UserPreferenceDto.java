package com.codemaniac.jobtrackrai.dto;

import com.codemaniac.jobtrackrai.enums.Status;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferenceDto {
  private String theme;
  private String language;
  private String timezone;
  private String dateFormat;

  private String profileVisibility;
  private boolean showContactInfo;
  private boolean allowSearchIndexing;
  private boolean showActivityStatus;

  private boolean emailNotifications;
  private boolean pushNotifications;
  private boolean smsNotifications;
  private boolean statusUpdates;
  private boolean interviewReminders;
  private boolean weeklyDigest;
  private boolean marketingEmails;

  private String landingPage;
  private int itemsPerPage;
  private boolean compactView;
  private boolean showQuickStats;

  private Status defaultAppStatus;
  private String followUpReminder;
  private String exportFormat;
  private boolean autoSaveDrafts;

  private LocalDateTime lastSaved;
}
