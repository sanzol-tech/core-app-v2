package sanzol.se.model.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "se_accounts")
public class SeAccount implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer accountId;
	private String name;
	private boolean isActive;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "se_account_seq")
	//@GenericGenerator(name = "se_account_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "se_account_seq") })
	@Column(name = "ACCOUNT_ID", unique = true, nullable = false)
	public Integer getAccountId()
	{
		return this.accountId;
	}

	public void setAccountId(Integer accountId)
	{
		this.accountId = accountId;
	}

	@Column(name = "NAME", nullable = false, length = 50)
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
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