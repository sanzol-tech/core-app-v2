package sanzol.web.component.menu;

import java.util.ArrayList;
import java.util.List;

public class MenuModel implements MenuElement
{
	private List<MenuElement> elements = new ArrayList<MenuElement>();

	public void add(MenuElement element)
	{
		elements.add(element);
	}

	public List<MenuElement> getElements()
	{
		return elements;
	}

}
