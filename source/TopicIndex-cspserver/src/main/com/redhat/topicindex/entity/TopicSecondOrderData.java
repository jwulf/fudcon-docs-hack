package com.redhat.topicindex.entity;


import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.envers.Audited;
import org.hibernate.validator.Length;

import com.redhat.topicindex.entity.base.AuditedEntity;

@Audited
@Entity
@Table(name = "TopicSecondOrderData")
public class TopicSecondOrderData extends AuditedEntity<TopicSecondOrderData> implements java.io.Serializable
{
	private static final long serialVersionUID = 3393132758855818345L;
	private Integer topicSecondOrderDataId;
	private String topicHTMLView;
	private String topicXMLErrors;
	
	public TopicSecondOrderData()
	{
		super(TopicSecondOrderData.class);
	}
	
	public TopicSecondOrderData(final Integer topicSecondOrderDataId)
	{
		super(TopicSecondOrderData.class);
		this.topicSecondOrderDataId = topicSecondOrderDataId;
	}
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "TopicSecondOrderDataID")
    public Integer getTopicSecondOrderDataId()
	{
		return this.topicSecondOrderDataId;
	}
	
	public void setTopicSecondOrderDataId(final Integer topicSecondOrderDataId)
	{
		this.topicSecondOrderDataId = topicSecondOrderDataId;
	}
	
	// @Column(name = "TopicHTMLView", length = 65535)
	@Column(name = "TopicHTMLView", columnDefinition = "TEXT")
	@Length(max = 65535)
	public String getTopicHTMLView()
	{
		return this.topicHTMLView;
	}

	public void setTopicHTMLView(final String topicHTMLView)
	{
		this.topicHTMLView = topicHTMLView;
	}
	
	// @Column(name = "TopicXMLErrors", length = 65535)
	@Column(name = "TopicXMLErrors", columnDefinition = "TEXT")
	@Length(max = 65535)
	public String getTopicXMLErrors()
	{
		return this.topicXMLErrors;
	}

	public void setTopicXMLErrors(final String topicXMLErrors)
	{
		this.topicXMLErrors = topicXMLErrors;
	}

	@Override
	@Transient
	public Integer getId()
	{
		return this.topicSecondOrderDataId;
	}
}
