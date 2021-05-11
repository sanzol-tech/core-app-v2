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
@Table(name = "se_errors")
public class SeError implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer errorId;
	private SeSoftVersion softVersion;
	private SeUser seUser;
	private LocalDateTime errorDate;
	private String context;
	private String contextDetail;
	private String exClass;
	private String exMessage;
	private String exStackTrace;
	private String serverName;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "se_errors_seq")
	//@GenericGenerator(name = "se_errors_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "se_errors_seq") })
	@Column(name = "ERROR_ID", unique = true, nullable = false)
	public Integer getErrorId()
	{
		return this.errorId;
	}

	public void setErrorId(Integer errorId)
	{
		this.errorId = errorId;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "SOFT_VERSION_ID", nullable = false)
	public SeSoftVersion getSoftVersion()
	{
		return softVersion;
	}

	public void setSoftVersion(SeSoftVersion softVersion)
	{
		this.softVersion = softVersion;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "USER_ID")
	public SeUser getSeUser()
	{
		return seUser;
	}

	public void setSeUser(SeUser seUser)
	{
		this.seUser = seUser;
	}

	@Column(name = "ERROR_DATE", nullable = false)
	public LocalDateTime getErrorDate()
	{
		return this.errorDate;
	}

	public void setErrorDate(LocalDateTime errorDate)
	{
		this.errorDate = errorDate;
	}

	@Column(name = "CONTEXT", nullable = false, length = 200)
	public String getContext()
	{
		return context;
	}

	public void setContext(String context)
	{
		this.context = context;
	}

	@Column(name = "CONTEXT_DETAIL", length = 2000)
	public String getContextDetail()
	{
		return this.contextDetail;
	}

	public void setContextDetail(String contextDetail)
	{
		this.contextDetail = contextDetail;
	}

	@Column(name = "EX_CLASS", nullable = false, length = 200)
	public String getExClass()
	{
		return this.exClass;
	}

	public void setExClass(String exClass)
	{
		this.exClass = exClass;
	}

	@Column(name = "EX_MESSAGE", length = 2000)
	public String getExMessage()
	{
		return this.exMessage;
	}

	public void setExMessage(String exMessage)
	{
		this.exMessage = exMessage;
	}

	@Column(name = "EX_STACK_TRACE", length = 65535)
	public String getExStackTrace()
	{
		return this.exStackTrace;
	}

	public void setExStackTrace(String exStackTrace)
	{
		this.exStackTrace = exStackTrace;
	}

	@Column(name = "SERVER_NAME", length = 40)
	public String getServerName()
	{
		return this.serverName;
	}

	public void setServerName(String serverName)
	{
		this.serverName = serverName;
	}

}
