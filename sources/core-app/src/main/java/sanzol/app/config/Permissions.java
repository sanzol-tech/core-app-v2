/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.app.config;

public final class Permissions
{
	public static final int LEVEL_DENIED = 0;
	public static final int LEVEL_READ_ONLY = 1;
	public static final int LEVEL_ALLOW = 2;

	public static final int PERMISSION_SE_ADMIN = 1;
	public static final int PERMISSION_SE_AUDIT = 2;
	public static final int PERMISSION_SE_AUDIT_ALL = 3;
	public static final int PERMISSION_SE_ERRORS = 4;
	public static final int PERMISSION_SE_SESSIONS = 5;
	public static final int PERMISSION_SE_ACCOUNTS = 13;
	public static final int PERMISSION_SE_USERS = 6;
	public static final int PERMISSION_SE_NOTIFICATIONS_INBOX = 14;
	public static final int PERMISSION_SE_NOTIFICATIONS_SENT = 15;
	public static final int PERMISSION_SE_PERMISSIONS = 7;
	public static final int PERMISSION_SE_ROLES = 8;
	public static final int PERMISSION_SE_SECTORS = 9;
	public static final int PERMISSION_SE_SERVER_INFO = 10;
	public static final int PERMISSION_SE_SOFT_VERSIONS = 11;
	public static final int PERMISSION_SE_PROPERTIES = 12;
	public static final int PERMISSION_SE_EMAIL_TEMPLATES = 16;
	public static final int PERMISSION_SE_REGISTRATIONS = 17;
	public static final int PERMISSION_SE_WHITELIST = 18;

	private Permissions()
	{
		// Hide constructor
	}

}
