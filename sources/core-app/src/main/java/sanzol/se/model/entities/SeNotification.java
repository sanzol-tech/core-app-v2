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
@Table(name = "se_notifications")
public class SeNotification implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer notificationId;
	private SeNotificationType seNotificationType;
	private String message;
	private LocalDateTime notificationDate;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "se_notifications_seq")
	//@GenericGenerator(name = "se_notifications_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "se_notifications_seq") })
	@Column(name = "NOTIFICATION_ID", nullable = false)
	public Integer getNotificationId()
	{
		return this.notificationId;
	}

	public void setNotificationId(Integer notificationId)
	{
		this.notificationId = notificationId;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "NOTIFICATION_TYPE_ID", nullable = false)
	public SeNotificationType getSeNotificationType()
	{
		return this.seNotificationType;
	}

	public void setSeNotificationType(SeNotificationType seNotificationType)
	{
		this.seNotificationType = seNotificationType;
	}

	@Column(name = "MESSAGE", nullable = false, length = 4000)
	public String getMessage()
	{
		return this.message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	@Column(name = "NOTIFICATION_DATE", nullable = false)
	public LocalDateTime getNotificationDate()
	{
		return this.notificationDate;
	}

	public void setNotificationDate(LocalDateTime notificationDate)
	{
		this.notificationDate = notificationDate;
	}

}