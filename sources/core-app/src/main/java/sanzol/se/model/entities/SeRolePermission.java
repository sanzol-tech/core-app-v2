package sanzol.se.model.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "se_roles_permissions")
public class SeRolePermission implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer rolePermissionId;
	private SeRole seRole;
	private SePermission sePermission;
	private int permissionLevel;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "se_roles_permissions_seq")
	//@GenericGenerator(name = "se_roles_permissions_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "se_roles_permissions_seq") })
	@Column(name = "ROLE_PERMISSION_ID", unique = true, nullable = false)
	public Integer getRolePermissionId()
	{
		return this.rolePermissionId;
	}

	public void setRolePermissionId(Integer rolePermissionId)
	{
		this.rolePermissionId = rolePermissionId;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ROLE_ID", nullable = false)
	public SeRole getSeRole()
	{
		return seRole;
	}

	public void setSeRole(SeRole seRole)
	{
		this.seRole = seRole;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "PERMISSION_ID", nullable = false)
	public SePermission getSePermission()
	{
		return sePermission;
	}

	public void setSePermission(SePermission sePermission)
	{
		this.sePermission = sePermission;
	}

	@Column(name = "PERMISSION_LEVEL", nullable = false, precision = 9, scale = 0)
	public int getPermissionLevel()
	{
		return permissionLevel;
	}

	public void setPermissionLevel(int permissionLevel)
	{
		this.permissionLevel = permissionLevel;
	}

}