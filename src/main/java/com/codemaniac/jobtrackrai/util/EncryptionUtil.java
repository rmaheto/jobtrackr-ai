package com.codemaniac.jobtrackrai.util;

import org.jasypt.util.text.AES256TextEncryptor;

public class EncryptionUtil {

  private EncryptionUtil() {}

  public static boolean isEncrypted(final String value) {
    return value.startsWith("ENC(") && value.endsWith(")");
  }

  // Encrypt a value (only if not already encrypted)
  public static String encrypt(final String secretKey, final String value) {
    if (isEncrypted(value)) {
      return value; // Skip encryption if already encrypted
    }

    final AES256TextEncryptor encryptor = new AES256TextEncryptor();
    encryptor.setPassword(secretKey);
    return "ENC(" + encryptor.encrypt(value) + ")";
  }

  // Decrypt a value
  public static String decrypt(final String secretKey, final String encryptedValue) {

    if (!isEncrypted(encryptedValue)) {
      return encryptedValue;
    }

    final String base64Encoded =
        encryptedValue.substring(4, encryptedValue.length() - 1); // Remove ENC(...)
    final AES256TextEncryptor encryptor = new AES256TextEncryptor();
    encryptor.setPassword(secretKey);
    return encryptor.decrypt(base64Encoded);
  }
}
