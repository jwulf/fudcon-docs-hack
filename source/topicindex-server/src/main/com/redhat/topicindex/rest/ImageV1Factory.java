package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import com.redhat.topicindex.entity.ImageFile;
import com.redhat.topicindex.rest.entities.BaseRESTEntityV1;
import com.redhat.topicindex.rest.entities.ImageV1;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;

class ImageV1Factory extends RESTDataObjectFactory<ImageV1, ImageFile>
{
	ImageV1Factory()
	{
		super(ImageFile.class);
	}
	
	@Override
	ImageV1 createRESTEntityFromDBEntity(final ImageFile entity, final String baseUrl, final String dataType, final ExpandDataTrunk expand, final boolean isRevision, final boolean expandParentReferences)
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";
		
		final ImageV1 retValue = new ImageV1();
		
		final List<String> expandOptions = new ArrayList<String>();
		if (!isRevision)
			expandOptions.add(BaseRESTEntityV1.REVISIONS_NAME);
		retValue.setExpand(expandOptions);
		
		retValue.setId(entity.getImageFileId());
		retValue.setFilename(entity.getOriginalFileName());
		retValue.setDescription(entity.getDescription());
		retValue.setImageData(entity.getImageData());
		retValue.setImageDataBase64(entity.getImageDataBase64());
		retValue.setThumbnail(entity.getThumbnailData());
		
		if (!isRevision)
		{
			retValue.setRevisions(new RESTDataObjectCollectionFactory<ImageV1, ImageFile>().create(new ImageV1Factory(), entity, entity.getRevisions(), BaseRESTEntityV1.REVISIONS_NAME, dataType, expand, baseUrl));
		}
		
		retValue.setLinks(baseUrl, BaseRESTv1.IMAGE_URL_NAME, dataType, retValue.getId());
		
		return retValue;
	}

	@Override
	void syncDBEntityWithRESTEntity(final EntityManager entityManager, final ImageFile entity, final ImageV1 dataObject)
	{
		if (dataObject.isParameterSet(ImageV1.DESCRIPTION_NAME))
			entity.setDescription(dataObject.getDescription());
		if (dataObject.isParameterSet(ImageV1.FILENAME_NAME))
			entity.setOriginalFileName(dataObject.getFilename());
		if (dataObject.isParameterSet(ImageV1.IMAGEDATA_NAME))
			entity.setImageData(dataObject.getImageData());
		
		entityManager.persist(entity);
	}
}
