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
@Table(name = "se_registrations_events")
public class SeRegistrationEvent implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer registrationEventId;
	private SeRegistrationEvent prevEvent;
	private SeRegistration seRegistration;
	private SeRegistrationState seRegistrationState;
	private LocalDateTime eventDate;
	private LocalDateTime expirationDate;
	private String ipAddress;
	private String useragent;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "se_registrations_events_seq")
	//@GenericGenerator(name = "se_registrations_events_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "se_registrations_events_seq") })
	@Column(name = "REGISTRATION_EVENT_ID", nullable = false)
	public Integer getRegistrationEventId()
	{
		return this.registrationEventId;
	}

	public void setRegistrationEventId(Integer registrationEventId)
	{
		this.registrationEventId = registrationEventId;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "PREV_REGISTRATION_EVENT_ID")
	public SeRegistrationEvent getPrevEvent()
	{
		return prevEvent;
	}

	public void setPrevEvent(SeRegistrationEvent prevEvent)
	{
		this.prevEvent = prevEvent;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "REGISTRATION_ID", nullable = false)
	public SeRegistration getSeRegistration()
	{
		return this.seRegistration;
	}

	public void setSeRegistration(SeRegistration seRegistration)
	{
		this.seRegistration = seRegistration;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "REGISTRATION_STATE_ID", nullable = false)
	public SeRegistrationState getSeRegistrationState()
	{
		return this.seRegistrationState;
	}

	public void setSeRegistrationState(SeRegistrationState seRegistrationState)
	{
		this.seRegistrationState = seRegistrationState;
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

	@Column(name = "EXPIRATION_DATE")
	public LocalDateTime getExpirationDate()
	{
		return expirationDate;
	}

	public void setExpirationDate(LocalDateTime expirationDate)
	{
		this.expirationDate = expirationDate;
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