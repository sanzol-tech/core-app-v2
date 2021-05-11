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
@Table(name = "se_notifications_users")
public class SeNotificationUser implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer notificationUserId;
	private SeNotification seNotification;
	private SeUser seUser;
	private LocalDateTime readedDate;
	private LocalDateTime deletedDate;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "se_notifications_users_seq")
	//@GenericGenerator(name = "se_notifications_users_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "se_notifications_users_seq") })
	@Column(name = "NOTIFICATION_USER_ID", nullable = false)
	public Integer getNotificationUserId()
	{
		return this.notificationUserId;
	}

	public void setNotificationUserId(Integer notificationUserId)
	{
		this.notificationUserId = notificationUserId;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "NOTIFICATION_ID", nullable = false)
	public SeNotification getSeNotification()
	{
		return this.seNotification;
	}

	public void setSeNotification(SeNotification seNotification)
	{
		this.seNotification = seNotification;
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

	@Column(name = "READED_DATE")
	public LocalDateTime getReadedDate()
	{
		return this.readedDate;
	}

	public void setReadedDate(LocalDateTime readedDate)
	{
		this.readedDate = readedDate;
	}

	@Column(name = "DELETED_DATE")
	public LocalDateTime getDeletedDate()
	{
		return this.deletedDate;
	}

	public void setDeletedDate(LocalDateTime deletedDate)
	{
		this.deletedDate = deletedDate;
	}

}