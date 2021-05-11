package sanzol.web.component.menu;

public class ItemModel implements MenuElement
{
	private String outcome;
	private String icon;
	private String label;
	private Integer permission;
	private Integer permissionLevel;

	public ItemModel(String outcome, String icon, String label, Integer permission, Integer permissionLevel)
	{
		this.outcome = outcome;
		this.icon = icon;
		this.label = label;
		this.permission = permission;
		this.permissionLevel = permissionLevel;
	}

	public String getOutcome()
	{
		return outcome;
	}

	public void setOutcome(String outcome)
	{
		this.outcome = outcome;
	}

	public String getIcon()
	{
		return icon;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public Integer getPermission()
	{
		return permission;
	}

	public void setPermission(Integer permission)
	{
		this.permission = permission;
	}

	public Integer getPermissionLevel()
	{
		return permissionLevel;
	}

	public void setPermissionLevel(Integer permissionLevel)
	{
		this.permissionLevel = permissionLevel;
	}

}
