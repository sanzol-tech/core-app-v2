package sanzol.se.model.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "se_passwords")
public class SePassword implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer passwordId;
	private SeUser seUser;
	private String password;
	private LocalDateTime dateFrom;
	private LocalDateTime dateTo;
	private boolean isTemporal;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "se_passwords_seq")
	//@GenericGenerator(name = "se_passwords_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "se_passwords_seq") })
	@Column(name = "PASSWORD_ID", unique = true, nullable = false)
	public Integer getPasswordId()
	{
		return this.passwordId;
	}

	public void setPasswordId(Integer passwordId)
	{
		this.passwordId = passwordId;
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

	@Column(name = "PASSWORD", nullable = false, length = 200)
	public String getPassword()
	{
		return this.password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	@Column(name = "DATE_FROM", nullable = false)
	public LocalDateTime getDateFrom()
	{
		return this.dateFrom;
	}

	public void setDateFrom(LocalDateTime dateFrom)
	{
		this.dateFrom = dateFrom;
	}

	@Column(name = "DATE_TO")
	public LocalDateTime getDateTo()
	{
		return this.dateTo;
	}

	public void setDateTo(LocalDateTime dateTo)
	{
		this.dateTo = dateTo;
	}

	@Column(name = "IS_TEMPORAL", nullable = false, precision = 1, scale = 0)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	public boolean isIsTemporal()
	{
		return isTemporal;
	}

	public void setIsTemporal(boolean isTemporal)
	{
		this.isTemporal = isTemporal;
	}

}