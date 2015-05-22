package nl.yenlo.ddld.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Describes a {@link Crawler}'s configuration.
 * 
 * @author Philipp Gayret
 * 
 */
// TODO: Locking system so that crawlers can be marked as `active` and this whole thing works in a clustered setup
@Entity
public class Crawler implements Serializable {

	public enum CrawlType {
		ALL, PDF, WORD
	}

	public enum CrawlFrequencyType {
		MINUTELY, HOURLY, DAILY, WEEKLY, MONTHLY;
	}

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column
	private Integer id;
	@ManyToOne
	@JsonIgnore
	private User owner;
	@Column
	private String title;
	@Column
	private String crawlRoot;
	@Column
	private Integer crawlDepth;
	@Column
	private Boolean crawlExternal;
	@Column
	@Enumerated(EnumType.STRING)
	private CrawlType crawlType;
	@Column
	private CrawlFrequencyType crawlFrequencyType;
	@Column
	private Integer crawlFrequency;
	@Column
	private String crawlFilter;
	@Column
	private Long crawlLast;
	@Column
	private Long crawlScheduled;

	public Integer getId() {
		return this.id;
	}

	public User getOwner() {
		return this.owner;
	}

	public String getTitle() {
		return this.title;
	}

	public String getCrawlRoot() {
		return this.crawlRoot;
	}

	public Integer getCrawlDepth() {
		return this.crawlDepth;
	}

	public Boolean getCrawlExternal() {
		return this.crawlExternal;
	}

	public CrawlType getCrawlType() {
		return this.crawlType;
	}

	public CrawlFrequencyType getCrawlFrequencyType() {
		return this.crawlFrequencyType;
	}

	public Integer getCrawlFrequency() {
		return this.crawlFrequency;
	}

	public String getCrawlFilter() {
		return this.crawlFilter;
	}

	@JsonIgnore
	public Collection<String> getCrawlFilterArray() {
		Collection<String> set = new HashSet<String>();
		if (this.crawlFilter != null) {
			for (String item : this.crawlFilter.split(",")) {
				set.add(item.trim());
			}
		}
		return set;
	}

	/**
	 * 
	 * @return timestamp of last started run
	 */
	public Long getCrawlLast() {
		return this.crawlLast;
	}

	/**
	 * 
	 * @return timestamp of next scheduled run
	 */
	public Long getCrawlScheduled() {
		return this.crawlScheduled;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setCrawlRoot(String crawlRoot) {
		this.crawlRoot = crawlRoot;
	}

	public void setCrawlDepth(Integer crawlDepth) {
		this.crawlDepth = crawlDepth;
	}

	public void setCrawlExternal(Boolean crawlExternal) {
		this.crawlExternal = crawlExternal;
	}

	public void setCrawlType(CrawlType crawlType) {
		this.crawlType = crawlType;
	}

	public void setCrawlFrequencyType(CrawlFrequencyType crawlFrequencyType) {
		this.crawlFrequencyType = crawlFrequencyType;
	}

	public void setCrawlFrequency(Integer crawlFrequency) {
		this.crawlFrequency = crawlFrequency;
	}

	public void setCrawlFilter(String crawlFilter) {
		this.crawlFilter = crawlFilter;
	}

	public void setCrawlLast(Long crawlLast) {
		this.crawlLast = crawlLast;
	}

	public void setCrawlScheduled(Long crawlScheduled) {
		this.crawlScheduled = crawlScheduled;
	}

	/**
	 * Reschedules {@link #crawlScheduled} based on {@link #crawlLast}, {@link #crawlFrequencyType} and the {@link #crawlFrequency} fields.
	 */
	public void reschedule() {
		Calendar calendar = Calendar.getInstance();
		if (this.crawlLast != null) {
			calendar.setTimeInMillis(this.crawlLast);
			switch (this.crawlFrequencyType) {
			case MINUTELY:
				calendar.add(Calendar.MINUTE, this.crawlFrequency);
				break;
			case HOURLY:
				calendar.add(Calendar.HOUR, this.crawlFrequency);
				break;
			case DAILY:
				calendar.add(Calendar.DATE, this.crawlFrequency);
				break;
			case WEEKLY:
				calendar.add(Calendar.DATE, 7);
				break;
			case MONTHLY:
				calendar.add(Calendar.MONTH, 1);
				break;
			}
		}
		this.setCrawlLast(Calendar.getInstance().getTimeInMillis());
		this.setCrawlScheduled(calendar.getTimeInMillis());
	}

}
