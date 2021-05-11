package sanzol.web.component.menu;

import java.util.ArrayList;
import java.util.List;

public class SubMenuModel implements MenuElement
{
	private String label;
	private String icon;
	private boolean active;

	private List<MenuElement> elements;

	public SubMenuModel(String label, String icon, boolean active)
	{
		this.label = label;
		this.icon = icon;
		this.active = active;

		this.elements = new ArrayList<MenuElement>();
	}

	public void add(MenuElement element)
	{
		elements.add(element);
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public String getIcon()
	{
		return icon;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	public List<MenuElement> getElements()
	{
		return elements;
	}

}
