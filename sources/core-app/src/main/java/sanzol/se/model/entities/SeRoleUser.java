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
@Table(name = "se_roles_users")
public class SeRoleUser implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer roleUserId;
	private SeRole seRole;
	private SeUser seUser;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "se_roles_users_seq")
	//@GenericGenerator(name = "se_roles_users_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "se_roles_users_seq") })
	@Column(name = "ROLE_USER_ID", unique = true, nullable = false)
	public Integer getRoleUserId()
	{
		return this.roleUserId;
	}

	public void setRoleUserId(Integer roleUserId)
	{
		this.roleUserId = roleUserId;
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
	@JoinColumn(name = "USER_ID", nullable = false)
	public SeUser getSeUser()
	{
		return this.seUser;
	}

	public void setSeUser(SeUser seUser)
	{
		this.seUser = seUser;
	}

}