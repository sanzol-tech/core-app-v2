package sanzol.se.model.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "se_registrations_states")
public class SeRegistrationState implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer registrationStateId;
	private String name;
	private String detail;
	private String icon;
	private String iconColor;
	private String expirationExpression;
	private boolean isFinal;

	@Id
	@Column(name = "REGISTRATION_STATE_ID", nullable = false)
	public Integer getRegistrationStateId()
	{
		return this.registrationStateId;
	}

	public void setRegistrationStateId(Integer registrationStateId)
	{
		this.registrationStateId = registrationStateId;
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

	@Column(name = "DETAIL", length = 500)
	public String getDetail()
	{
		return this.detail;
	}

	public void setDetail(String detail)
	{
		this.detail = detail;
	}

	@Column(name = "ICON", length = 100)
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

	@Column(name = "EXPIRATION_EXPRESSION", length = 20)
	public String getExpirationExpression()
	{
		return this.expirationExpression;
	}

	public void setExpirationExpression(String expirationExpression)
	{
		this.expirationExpression = expirationExpression;
	}

	@Column(name = "IS_FINAL", nullable = false, precision = 1, scale = 0)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	public boolean isIsFinal()
	{
		return this.isFinal;
	}

	public void setIsFinal(boolean isFinal)
	{
		this.isFinal = isFinal;
	}

}