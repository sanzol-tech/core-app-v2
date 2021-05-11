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
import javax.persistence.Transient;

import sanzol.util.UserAgentUtils;

@Entity
@Table(name = "se_audit")
public class SeAudit implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer auditId;
	private SeUser seUser;
	private LocalDateTime eventDate;
	private LocalDateTime auditDate;
	private String eventName;
	private String entityName;
	private String entityId;
	private String parentId;
	private String oldEntity;
	private String newEntity;
	private String detail;
	private String context;
	private String sessionId;
	private String ipAddress;
	private String useragent;
	private SeSoftVersion seSoftVersion;
	private String serverName;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "se_audit_seq")
	//@GenericGenerator(name = "se_audit_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "se_audit_seq") })
	@Column(name = "AUDIT_ID", unique = true, nullable = false)
	public Integer getAuditId()
	{
		return this.auditId;
	}

	public void setAuditId(Integer auditId)
	{
		this.auditId = auditId;
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

	@Column(name = "EVENT_DATE", nullable = false)
	public LocalDateTime getEventDate()
	{
		return this.eventDate;
	}

	public void setEventDate(LocalDateTime eventDate)
	{
		this.eventDate = eventDate;
	}

	@Column(name = "AUDIT_DATE", nullable = false)
	public LocalDateTime getAuditDate()
	{
		return this.auditDate;
	}

	public void setAuditDate(LocalDateTime auditDate)
	{
		this.auditDate = auditDate;
	}

	@Column(name = "EVENT_NAME", nullable = false, length = 100)
	public String getEventName()
	{
		return this.eventName;
	}

	public void setEventName(String eventName)
	{
		this.eventName = eventName;
	}

	@Column(name = "ENTITY_NAME", length = 100)
	public String getEntityName()
	{
		return this.entityName;
	}

	public void setEntityName(String entityName)
	{
		this.entityName = entityName;
	}

	@Column(name = "ENTITY_ID", length = 80)
	public String getEntityId()
	{
		return this.entityId;
	}

	public void setEntityId(String entityId)
	{
		this.entityId = entityId;
	}

	@Column(name = "PARENT_ID", length = 80)
	public String getParentId()
	{
		return this.parentId;
	}

	public void setParentId(String parentId)
	{
		this.parentId = parentId;
	}

	@Column(name = "OLD_ENTITY", length = 65535)
	public String getOldEntity()
	{
		return this.oldEntity;
	}

	public void setOldEntity(String oldEntity)
	{
		this.oldEntity = oldEntity;
	}

	@Column(name = "NEW_ENTITY", length = 65535)
	public String getNewEntity()
	{
		return this.newEntity;
	}

	public void setNewEntity(String newEntity)
	{
		this.newEntity = newEntity;
	}

	@Column(name = "DETAIL", length = 65535)
	public String getDetail()
	{
		return this.detail;
	}

	public void setDetail(String detail)
	{
		this.detail = detail;
	}

	@Column(name = "CONTEXT", length = 200)
	public String getContext()
	{
		return this.context;
	}

	public void setContext(String context)
	{
		this.context = context;
	}

	@Column(name = "SESSION_ID", length = 40)
	public String getSessionId()
	{
		return this.sessionId;
	}

	public void setSessionId(String sessionId)
	{
		this.sessionId = sessionId;
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

	@Column(name = "USERAGENT", length = 200)
	public String getUseragent()
	{
		return this.useragent;
	}

	public void setUseragent(String useragent)
	{
		this.useragent = useragent;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "SOFT_VERSION_ID", nullable = false)
	public SeSoftVersion getSeSoftVersion()
	{
		return this.seSoftVersion;
	}

	public void setSeSoftVersion(SeSoftVersion seSoftVersion)
	{
		this.seSoftVersion = seSoftVersion;
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

	// ------------------------------------------------------------------------------------------------

	@Transient
	public String getBrowser()
	{
		return UserAgentUtils.getInstance().getBrowser(useragent);
	}

	@Transient
	public String getOperatingSystem()
	{
		return UserAgentUtils.getInstance().getOperatingSystem(useragent);
	}

}