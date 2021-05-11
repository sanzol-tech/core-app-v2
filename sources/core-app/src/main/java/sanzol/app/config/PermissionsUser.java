/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.app.config;

import java.io.Serializable;
import java.util.Map;

public final class PermissionsUser implements Serializable
{
	private static final long serialVersionUID = 1L;

	private int seAdmin = Permissions.LEVEL_DENIED;
	private int seAccounts = Permissions.LEVEL_DENIED;
	private int seAudit = Permissions.LEVEL_DENIED;
	private int seEmailTemplates = Permissions.LEVEL_DENIED;
	private int seErrors = Permissions.LEVEL_DENIED;
	private int seNotificationsInbox = Permissions.LEVEL_DENIED;
	private int seNotificationsSent = Permissions.LEVEL_DENIED;
	private int sePermissions = Permissions.LEVEL_DENIED;
	private int seProperties = Permissions.LEVEL_DENIED;
	private int seRegistrations = Permissions.LEVEL_DENIED;
	private int seRoles = Permissions.LEVEL_DENIED;
	private int seSectors = Permissions.LEVEL_DENIED;
	private int seServerInfo = Permissions.LEVEL_DENIED;
	private int seSessions = Permissions.LEVEL_DENIED;
	private int seSoftVersions = Permissions.LEVEL_DENIED;
	private int seUsers = Permissions.LEVEL_DENIED;
	private int seWhitelist = Permissions.LEVEL_DENIED;

	public PermissionsUser(Map<Integer, Integer> mapPermissions)
	{
		if (mapPermissions == null)
		{
			return;
		}

		seAdmin = ifnull(mapPermissions.get(Permissions.PERMISSION_SE_ADMIN), Permissions.LEVEL_DENIED);
		seAccounts = ifnull(mapPermissions.get(Permissions.PERMISSION_SE_ACCOUNTS), Permissions.LEVEL_DENIED);
		seAudit = ifnull(mapPermissions.get(Permissions.PERMISSION_SE_AUDIT), Permissions.LEVEL_DENIED);
		seEmailTemplates = ifnull(mapPermissions.get(Permissions.PERMISSION_SE_EMAIL_TEMPLATES), Permissions.LEVEL_DENIED);
		seErrors = ifnull(mapPermissions.get(Permissions.PERMISSION_SE_ERRORS), Permissions.LEVEL_DENIED);
		seNotificationsInbox = ifnull(mapPermissions.get(Permissions.PERMISSION_SE_NOTIFICATIONS_INBOX), Permissions.LEVEL_DENIED);
		seNotificationsSent = ifnull(mapPermissions.get(Permissions.PERMISSION_SE_NOTIFICATIONS_SENT), Permissions.LEVEL_DENIED);
		sePermissions = ifnull(mapPermissions.get(Permissions.PERMISSION_SE_PERMISSIONS), Permissions.LEVEL_DENIED);
		seProperties = ifnull(mapPermissions.get(Permissions.PERMISSION_SE_PROPERTIES), Permissions.LEVEL_DENIED);
		seRegistrations = ifnull(mapPermissions.get(Permissions.PERMISSION_SE_REGISTRATIONS), Permissions.LEVEL_DENIED);
		seRoles = ifnull(mapPermissions.get(Permissions.PERMISSION_SE_ROLES), Permissions.LEVEL_DENIED);
		seSectors = ifnull(mapPermissions.get(Permissions.PERMISSION_SE_SECTORS), Permissions.LEVEL_DENIED);
		seServerInfo = ifnull(mapPermissions.get(Permissions.PERMISSION_SE_SERVER_INFO), Permissions.LEVEL_DENIED);
		seSessions = ifnull(mapPermissions.get(Permissions.PERMISSION_SE_SESSIONS), Permissions.LEVEL_DENIED);
		seSoftVersions = ifnull(mapPermissions.get(Permissions.PERMISSION_SE_SOFT_VERSIONS), Permissions.LEVEL_DENIED);
		seUsers = ifnull(mapPermissions.get(Permissions.PERMISSION_SE_USERS), Permissions.LEVEL_DENIED);
		seWhitelist = ifnull(mapPermissions.get(Permissions.PERMISSION_SE_WHITELIST), Permissions.LEVEL_DENIED);
	}

	private static int ifnull(Integer value, int defaultValue)
	{
		if (value == null)
		{
			return defaultValue;
		}
		return value;
	}

	public int getSeAdmin()
	{
		return seAdmin;
	}

	public int getSeAccounts()
	{
		return seAccounts;
	}

	public int getSeAudit()
	{
		return seAudit;
	}

	public int getSeEmailTemplates()
	{
		return seEmailTemplates;
	}

	public int getSeErrors()
	{
		return seErrors;
	}

	public int getSeNotificationsInbox()
	{
		return seNotificationsInbox;
	}

	public int getSeNotificationsSent()
	{
		return seNotificationsSent;
	}

	public int getSePermissions()
	{
		return sePermissions;
	}

	public int getSeProperties()
	{
		return seProperties;
	}

	public int getSeRegistrations()
	{
		return seRegistrations;
	}

	public int getSeRoles()
	{
		return seRoles;
	}

	public int getSeSectors()
	{
		return seSectors;
	}

	public int getSeServerInfo()
	{
		return seServerInfo;
	}

	public int getSeSessions()
	{
		return seSessions;
	}

	public int getSeSoftVersions()
	{
		return seSoftVersions;
	}

	public int getSeUsers()
	{
		return seUsers;
	}

	public int getSeWhitelist()
	{
		return seWhitelist;
	}

}
