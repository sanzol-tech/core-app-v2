package sanzol.se.model.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "se_permissions")
public class SePermission implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer permissionId;
	private String name;
	private String detail;
	private String category;
	private int userLevel;
	private boolean isUserDefined;
	private boolean isActive;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "se_permissions_seq")
	//@GenericGenerator(name = "se_permissions_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "se_permissions_seq") })
	@Column(name = "PERMISSION_ID", unique = true, nullable = false)
	public Integer getPermissionId()
	{
		return this.permissionId;
	}

	public void setPermissionId(Integer permissionId)
	{
		this.permissionId = permissionId;
	}

	@Column(name = "NAME", nullable = false, length = 40)
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Column(name = "DETAIL", length = 200)
	public String getDetail()
	{
		return this.detail;
	}

	public void setDetail(String detail)
	{
		this.detail = detail;
	}

	@Column(name = "CATEGORY", length = 20)
	public String getCategory()
	{
		return this.category;
	}

	public void setCategory(String category)
	{
		this.category = category;
	}

	@Column(name = "USER_LEVEL", nullable = false, precision = 9, scale = 0)
	public int getUserLevel()
	{
		return this.userLevel;
	}

	public void setUserLevel(int userLevel)
	{
		this.userLevel = userLevel;
	}

	@Column(name = "IS_USER_DEFINED", nullable = false, precision = 1, scale = 0)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	public boolean isIsUserDefined()
	{
		return this.isUserDefined;
	}

	public void setIsUserDefined(boolean isUserDefined)
	{
		this.isUserDefined = isUserDefined;
	}

	@Column(name = "IS_ACTIVE", nullable = false, precision = 1, scale = 0)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	public boolean isIsActive()
	{
		return this.isActive;
	}

	public void setIsActive(boolean isActive)
	{
		this.isActive = isActive;
	}

}