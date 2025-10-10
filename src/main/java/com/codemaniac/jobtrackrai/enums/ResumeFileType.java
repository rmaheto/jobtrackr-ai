package com.codemaniac.jobtrackrai.enums;

public enum ResumeFileType {
  PDF, DOCX;

  public static ResumeFileType fromMimeType(final String mimeType) {
    return switch (mimeType) {
      case "application/pdf" -> PDF;
      case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> DOCX;
      default -> throw new IllegalArgumentException("Unsupported mime type: " + mimeType);
    };
  }
}

