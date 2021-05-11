package sanzol.web.component.menu;

import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

public class RenderMenu
{
	private Map<Integer, Integer> mapPermissions;

	public RenderMenu(Map<Integer, Integer> mapPermissions)
	{
		this.mapPermissions = mapPermissions;
	}

	public String render(MenuModel menuModel)
	{
		return renderSubMenu(menuModel.getElements()).toString();
	}

	private final String HtmlSubMenuItem
		= "<li class=\"sidebar-menuitem\">"
		+ "<a href=\"#\" class=\"menuLink%s\">"
		+ "<i class=\"%s\"></i>"
		+ "<span class=\"menu-text\">%s</span>"
		+ "<i class=\"menu-arrow fa fa-chevron-down\"></i>"
		+ "</a>"
		+ "<ul class=\"sidebar-submenu-container\" style=\"display: %s;\">" + "%s" + "</ul>"
		+ "</li>\r\n";

	private final String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();

	private final String htmlMnuItem
		= "\t<li class=\"sidebar-menuitem\">"
		+ "<span>"
		+ "<a href=\"" + contextPath + "/%s\" class=\"menuLink\">"
		+ "<i class=\"%s\"></i>"
		+ "<span class=\"menu-text-group\">"
		+ "<span class=\"menu-text\">%s</span>" + "</span>"
		+ "</a>"
		+ "</span>"
		+ "</li>\r\n";

	public StringBuilder renderSubMenu(List<MenuElement> elements)
	{
		StringBuilder sb = new StringBuilder();
		for (MenuElement mnuEle : elements)
		{
			if (mnuEle instanceof SubMenuModel)
			{
				SubMenuModel subMenu = (SubMenuModel) mnuEle;
				StringBuilder subMenuContent = renderSubMenu(subMenu.getElements());
				if (subMenuContent.length() > 0)
				{
					if (subMenu.isActive())
					{
						sb.append(String.format(HtmlSubMenuItem, " active", subMenu.getIcon(), subMenu.getLabel(), "block", subMenuContent));
					}
					else
					{
						sb.append(String.format(HtmlSubMenuItem, "", subMenu.getIcon(), subMenu.getLabel(), "none", subMenuContent));
					}
				}
			}
			else if (mnuEle instanceof ItemModel)
			{
				ItemModel item = (ItemModel) mnuEle;
				sb.append(renderItem(item));
			}
		}
		return sb;
	}

	public StringBuilder renderItem(ItemModel itemModel)
	{
		StringBuilder sb = new StringBuilder();
		if (itemModel instanceof ItemModel)
		{
			ItemModel item = (ItemModel) itemModel;

			if (item.getPermission() == null || mapPermissions.containsKey(item.getPermission()))
			{
				sb.append(String.format(htmlMnuItem, item.getOutcome(), item.getIcon(), item.getLabel()));
			}
		}
		return sb;
	}

}
