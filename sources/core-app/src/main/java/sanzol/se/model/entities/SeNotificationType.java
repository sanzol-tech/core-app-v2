package sanzol.se.model.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "se_notifications_types")
public class SeNotificationType implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer notificationTypeId;
	private String name;
	private String icon;
	private String iconColor;

	@Id
	@Column(name = "NOTIFICATION_TYPE_ID", nullable = false)
	public Integer getNotificationTypeId()
	{
		return this.notificationTypeId;
	}

	public void setNotificationTypeId(Integer notificationTypeId)
	{
		this.notificationTypeId = notificationTypeId;
	}

	@Column(name = "NAME", nullable = false, length = 30)
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Column(name = "ICON", nullable = false, length = 100)
	public String getIcon()
	{
		return this.icon;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
	}

	@Column(name = "ICON_COLOR", length = 20)
	public String getIconColor()
	{
		return this.iconColor;
	}

	public void setIconColor(String iconColor)
	{
		this.iconColor = iconColor;
	}

}