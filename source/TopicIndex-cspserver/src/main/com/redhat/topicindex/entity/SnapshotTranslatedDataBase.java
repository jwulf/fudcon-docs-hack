package com.redhat.topicindex.entity;

import java.util.Date;

/**
 * The SnapshotTranslatedData and WorkingSnapshotTranslatedData entities serve
 * two different purposes in Skynet. SnapshotTranslatedData is designed to be
 * readonly, belongs to a SnapshotRevision, and maintains a list of
 * SnapshotTranslationString entities that represent the translations string
 * pairs. WorkingSnapshotTranslatedData is designed to be updated with the
 * current data held by Zanata, belongs to a Snapshot, and does not maintain any
 * SnapshotTranslationString entities.
 * 
 * Despite the functional differences of these two classes, the data they hold
 * is much the same, and this interface defines their common fields.
 */
public interface SnapshotTranslatedDataBase
{
	public Integer getSnapshotTranslatedDataId();

	public void setSnapshotTranslatedDataId(Integer snapshotTranslatedDataId);

	public SnapshotTopic getSnapshotTopic();

	public void setSnapshotTopic(SnapshotTopic snapshotTopic);

	public String getTranslatedXml();

	public void setTranslatedXml(String translatedXml);

	public String getTranslatedXmlRendered();

	public void setTranslatedXmlRendered(String translatedXmlRendered);

	public String getTranslationLocale();

	public void setTranslationLocale(String translationLocale);

	public Topic getEnversTopic();

	public void setEnversTopic(Topic enversTopic);

	public Date getTranslatedXmlRenderedUpdated();

	public void setTranslatedXmlRenderedUpdated(Date translatedXmlRenderedUpdated);
	
	public String getTopicTags();
	
	public String getFormattedTranslatedXmlRenderedUpdated();
}
