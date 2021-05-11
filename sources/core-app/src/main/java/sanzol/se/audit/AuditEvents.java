/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: January 2021
 *
 */
package sanzol.se.audit;

public class AuditEvents
{
	// Sessions
	public static final String LOGIN = "LOGIN";
	public static final String LOGOUT = "LOGOUT";
	public static final String KILL_SESSION = "KILL_SESSION";

	// Pages
	public static final String PAGE_LOAD = "PAGE_LOAD";
	public static final String SELECT_FOR_EDIT = "SELECT_FOR_EDIT";
	public static final String EXPORT_XLSX = "EXPORT_XLSX";
	public static final String EXPORT_CSV = "EXPORT_CSV";
	public static final String EXPORT_PDF = "EXPORT_PDF";

	// Tables
	public static final String SELECT = "SELECT";
	public static final String INSERT = "INSERT";
	public static final String UPDATE = "UPDATE";
	public static final String DELETE = "DELETE";

	// Others
	public static final String PASSWORD_CHANGED = "PASSWORD_CHANGED";
	public static final String PASSWORD_RESETED = "PASSWORD_RESETED";
	public static final String PASSWORD_RECOVERY = "PASSWORD_RECOVERY";
	public static final String UNLOCK_USER = "UNLOCK_USER";

}
