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
@Table(name = "se_sectors_users")
public class SeSectorUser implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer sectorUserId;
	private SeSector seSector;
	private SeUser seUser;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "se_sectors_users_seq")
	//@GenericGenerator(name = "se_sectors_users_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "se_sectors_users_seq") })
	@Column(name = "SECTOR_USER_ID", unique = true, nullable = false)
	public Integer getSectorUserId()
	{
		return this.sectorUserId;
	}

	public void setSectorUserId(Integer sectorUserId)
	{
		this.sectorUserId = sectorUserId;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "SECTOR_ID", nullable = false)
	public SeSector getSeSector()
	{
		return seSector;
	}

	public void setSeSector(SeSector seSector)
	{
		this.seSector = seSector;
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