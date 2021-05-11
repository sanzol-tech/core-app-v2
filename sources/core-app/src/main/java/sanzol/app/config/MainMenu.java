/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.app.config;

import static sanzol.app.config.I18nPreference.getI18nString;

import sanzol.web.component.menu.ItemModel;
import sanzol.web.component.menu.MenuModel;
import sanzol.web.component.menu.SubMenuModel;

public class MainMenu
{
	private MenuModel menuModel = new MenuModel();

	public MenuModel getMenuModel()
	{
		return menuModel;
	}

	public void setMenuModel(MenuModel menuModel)
	{
		this.menuModel = menuModel;
	}

	public static MainMenu create()
	{
		MainMenu mainMenu = new MainMenu();

		SubMenuModel subMenuUsers = new SubMenuModel(getI18nString("menu.sub.users"), "fa fa-user-friends", false);
		mainMenu.menuModel.add(subMenuUsers);

		subMenuUsers.add(new ItemModel("site/seUsers.xhtml", "fa fa-user", getI18nString("menu.seUsers"), Permissions.PERMISSION_SE_USERS, null));
		subMenuUsers.add(new ItemModel("site/seAccounts.xhtml", "fas fa-at", getI18nString("menu.seAccounts"), Permissions.PERMISSION_SE_ACCOUNTS, null));
		subMenuUsers.add(new ItemModel("site/seSectors.xhtml", "fa fa-sitemap", getI18nString("menu.seSectors"), Permissions.PERMISSION_SE_SECTORS, null));
		subMenuUsers.add(new ItemModel("site/seRoles.xhtml", "fa fa-list-alt", getI18nString("menu.seRoles"), Permissions.PERMISSION_SE_ROLES, null));
		subMenuUsers.add(new ItemModel("site/seAudit.xhtml", "fa fa-search", getI18nString("menu.seAudit"), Permissions.PERMISSION_SE_AUDIT_ALL, null));
		subMenuUsers.add(new ItemModel("site/seSessions.xhtml", "fa fa-plug", getI18nString("menu.seSessions"), Permissions.PERMISSION_SE_SESSIONS, null));
		subMenuUsers.add(new ItemModel("site/seNotificationsSent.xhtml", "fa fa-paper-plane", getI18nString("menu.seNotificationsSent"), Permissions.PERMISSION_SE_NOTIFICATIONS_SENT, null));

		SubMenuModel subMenuSignUp = new SubMenuModel(getI18nString("menu.sub.registrations"), "far fa-edit", false);
		mainMenu.menuModel.add(subMenuSignUp);

		subMenuSignUp.add(new ItemModel("site/seRegistrations.xhtml", "far fa-edit", getI18nString("menu.seRegistrations"), Permissions.PERMISSION_SE_REGISTRATIONS, null));
		subMenuSignUp.add(new ItemModel("site/seWhitelist.xhtml", "far fa-id-badge", getI18nString("menu.seWhitelist"), Permissions.PERMISSION_SE_WHITELIST, null));

		SubMenuModel subMenuSettings = new SubMenuModel(getI18nString("menu.sub.settings"), "fa fa-cog", false);
		mainMenu.menuModel.add(subMenuSettings);

		subMenuSettings.add(new ItemModel("site/sePermissions.xhtml", "fa fa-tag", getI18nString("menu.sePermissions"), Permissions.PERMISSION_SE_PERMISSIONS, null));
		subMenuSettings.add(new ItemModel("site/seProperties.xhtml", "fa fa-sliders-h", getI18nString("menu.seProperties"), Permissions.PERMISSION_SE_PROPERTIES, null));
		subMenuSettings.add(new ItemModel("site/seEmailTemplates.xhtml", "far fa-envelope", getI18nString("menu.seEmailTemplates"), Permissions.PERMISSION_SE_EMAIL_TEMPLATES, null));

		SubMenuModel subMenuSystem = new SubMenuModel(getI18nString("menu.sub.system"), "fa fa-desktop", false);
		mainMenu.menuModel.add(subMenuSystem);

		subMenuSystem.add(new ItemModel("site/seSoftVersions.xhtml", "fa fa-code-branch", getI18nString("menu.seSoftVersions"), Permissions.PERMISSION_SE_SOFT_VERSIONS, null));
		subMenuSystem.add(new ItemModel("site/seServerInfo.xhtml", "fa fa-desktop", getI18nString("menu.seServerInfo"), Permissions.PERMISSION_SE_SERVER_INFO, null));
		subMenuSystem.add(new ItemModel("site/seErrors.xhtml", "fa fa-bug", getI18nString("menu.seErrors"), Permissions.PERMISSION_SE_ERRORS, null));
		subMenuSystem.add(new ItemModel("site/about.xhtml", "fa fa-info-circle", getI18nString("menu.about"), null, null));

		return mainMenu;
	}

}
