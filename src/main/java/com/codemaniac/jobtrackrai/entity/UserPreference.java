package com.codemaniac.jobtrackrai.entity;

import com.codemaniac.jobtrackrai.enums.ExportFormat;
import com.codemaniac.jobtrackrai.enums.FollowUpReminder;
import com.codemaniac.jobtrackrai.enums.LandingPage;
import com.codemaniac.jobtrackrai.enums.Status;
import com.codemaniac.jobtrackrai.enums.Theme;
import com.codemaniac.jobtrackrai.interceptor.AuditInterceptor;
import com.codemaniac.jobtrackrai.model.Audit;
import com.codemaniac.jobtrackrai.model.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditInterceptor.class)
public class UserPreference implements Auditable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @Enumerated(EnumType.STRING)
  private Theme theme;

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

  @Enumerated(EnumType.STRING)
  private LandingPage landingPage;

  private int itemsPerPage;
  private boolean compactView;
  private boolean showQuickStats;

  @Enumerated(EnumType.STRING)
  private Status defaultAppStatus;

  @Enumerated(EnumType.STRING)
  private FollowUpReminder followUpReminder;

  @Enumerated(EnumType.STRING)
  private ExportFormat exportFormat;

  private boolean autoSaveDrafts;

  @Embedded private Audit audit = new Audit();

  public static UserPreference defaultPreference() {
    return UserPreference.builder()
        .theme(Theme.LIGHT)
        .language("English")
        .timezone("Eastern Time (UTC-5)")
        .dateFormat("MM/DD/YYYY (US)")
        .profileVisibility("Public")
        .showContactInfo(true)
        .allowSearchIndexing(true)
        .showActivityStatus(false)
        .emailNotifications(true)
        .pushNotifications(true)
        .smsNotifications(false)
        .statusUpdates(true)
        .interviewReminders(true)
        .weeklyDigest(true)
        .marketingEmails(false)
        .landingPage(LandingPage.DASHBOARD)
        .itemsPerPage(10)
        .compactView(false)
        .showQuickStats(true)
        .defaultAppStatus(Status.APPLIED)
        .followUpReminder(FollowUpReminder.ONE_WEEK)
        .exportFormat(ExportFormat.PDF)
        .autoSaveDrafts(true)
        .build();
  }
}
