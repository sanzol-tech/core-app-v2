package sanzol.se.model.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "se_roles")
public class SeRole implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer roleId;
	private String name;
	private String detail;
	private boolean isActive;
	private Set<SeRoleUser> lstSeRolesUsers = new HashSet<SeRoleUser>(0);
	private Set<SeRolePermission> lstSeRolesPermissions = new HashSet<SeRolePermission>(0);

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "se_roles_seq")
	//@GenericGenerator(name = "se_roles_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "se_roles_seq") })
	@Column(name = "ROLE_ID", unique = true, nullable = false)
	public Integer getRoleId()
	{
		return this.roleId;
	}

	public void setRoleId(Integer roleId)
	{
		this.roleId = roleId;
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

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "seRole")
	public Set<SeRoleUser> getLstSeRolesUsers()
	{
		return lstSeRolesUsers;
	}

	public void setLstSeRolesUsers(Set<SeRoleUser> lstSeRolesUsers)
	{
		this.lstSeRolesUsers = lstSeRolesUsers;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "seRole")
	public Set<SeRolePermission> getLstSeRolesPermissions()
	{
		return lstSeRolesPermissions;
	}

	public void setLstSeRolesPermissions(Set<SeRolePermission> lstSeRolesPermissions)
	{
		this.lstSeRolesPermissions = lstSeRolesPermissions;
	}

}