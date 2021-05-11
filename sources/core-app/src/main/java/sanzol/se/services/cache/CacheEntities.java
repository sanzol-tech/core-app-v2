/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.se.services.cache;

public class CacheEntities
{
	public static void load()
	{
		SeEmailTemplatesCache.load();
		SeNotificationsTypesCache.load();
		SeRegistrationsStatesCache.load();
	}
}
