package sanzol.se.model.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "se_sectors")
public class SeSector implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer sectorId;
	private String name;
	private String detail;
	private SeSector seSectorPrev;
	private boolean isActive;
	private Set<SeSectorUser> lstSeSectorUsers = new HashSet<SeSectorUser>(0);

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "se_sectors_seq")
	//@GenericGenerator(name = "se_sectors_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "se_sectors_seq") })
	@Column(name = "SECTOR_ID", unique = true, nullable = false)
	public Integer getSectorId()
	{
		return this.sectorId;
	}

	public void setSectorId(Integer sectorId)
	{
		this.sectorId = sectorId;
	}

	@Column(name = "NAME", nullable = false, length = 40)
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Column(name = "DETAIL", length = 200)
	public String getDetail()
	{
		return this.detail;
	}

	public void setDetail(String detail)
	{
		this.detail = detail;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "SECTOR_PREV")
	public SeSector getSeSectorPrev()
	{
		return seSectorPrev;
	}

	public void setSeSectorPrev(SeSector seSectorPrev)
	{
		this.seSectorPrev = seSectorPrev;
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

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "seSector")
	public Set<SeSectorUser> getLstSeSectorUsers()
	{
		return lstSeSectorUsers;
	}

	public void setLstSeSectorUsers(Set<SeSectorUser> lstSeSectorUsers)
	{
		this.lstSeSectorUsers = lstSeSectorUsers;
	}

	// ------------------------------------------------------------------------------------------------

	@Transient
	public String getLargeName()
	{
		return getPathName(2, " - ");
	}

	@Transient
	public String getPathName(Integer level, String separator)
	{
		String pathName = name;
		SeSector prev = seSectorPrev;

		while ((level == null || level - 1 > 0) && prev != null)
		{
			pathName = prev.getName() + separator + pathName;
			prev = prev.getSeSectorPrev();
			level--;
		}

		return pathName;
	}

}