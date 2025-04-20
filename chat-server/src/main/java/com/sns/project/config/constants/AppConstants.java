package com.sns.project.config.constants;


public class AppConstants {

  public static class Auth {
    public static final int CACHE_DURATION_MINUTES = 30000000;
  }

  public static class PasswordReset {
    public static final int EXPIRATION_MINUTES = 30;
    public static final String RESET_PASSWORD_PATH = "/reset-password?token=";
  }

  public static class User {
    public static final int SESSION_TIMEOUT_HOURS = 24;
  }

  public static class Notification {
    public static final int MAX_QUEUE_SIZE = 1000;
  }

}
