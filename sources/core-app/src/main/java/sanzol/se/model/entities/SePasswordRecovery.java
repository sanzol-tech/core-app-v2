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

@Entity
@Table(name = "se_passwords_recovery")
public class SePasswordRecovery implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer passwordRecoveryId;
	private SeUser seUser;
	private LocalDateTime requestDate;
	private LocalDateTime expirationDate;
	private LocalDateTime processDate;
	private String validationCode;
	private String ipAddress;
	private String useragent;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "se_passwords_recovery_seq")
	//@GenericGenerator(name = "se_passwords_recovery_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "se_passwords_recovery_seq") })
	@Column(name = "PASSWORD_RECOVERY_ID", nullable = false)
	public Integer getPasswordRecoveryId()
	{
		return this.passwordRecoveryId;
	}

	public void setPasswordRecoveryId(Integer passwordRecoveryId)
	{
		this.passwordRecoveryId = passwordRecoveryId;
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

	@Column(name = "REQUEST_DATE", nullable = false)
	public LocalDateTime getRequestDate()
	{
		return this.requestDate;
	}

	public void setRequestDate(LocalDateTime requestDate)
	{
		this.requestDate = requestDate;
	}

	@Column(name = "EXPIRATION_DATE", nullable = false)
	public LocalDateTime getExpirationDate()
	{
		return expirationDate;
	}

	public void setExpirationDate(LocalDateTime expirationDate)
	{
		this.expirationDate = expirationDate;
	}

	@Column(name = "PROCESS_DATE")
	public LocalDateTime getProcessDate()
	{
		return this.processDate;
	}

	public void setProcessDate(LocalDateTime processDate)
	{
		this.processDate = processDate;
	}

	@Column(name = "VALIDATION_CODE", nullable = false, length = 32)
	public String getValidationCode()
	{
		return this.validationCode;
	}

	public void setValidationCode(String validationCode)
	{
		this.validationCode = validationCode;
	}

	@Column(name = "IP_ADDRESS", length = 40)
	public String getIpAddress()
	{
		return this.ipAddress;
	}

	public void setIpAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
	}

	@Column(name = "USERAGENT", length = 400)
	public String getUseragent()
	{
		return this.useragent;
	}

	public void setUseragent(String useragent)
	{
		this.useragent = useragent;
	}

}