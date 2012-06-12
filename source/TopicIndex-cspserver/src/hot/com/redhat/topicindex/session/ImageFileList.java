package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("imageFileList")
public class ImageFileList extends EntityQuery<ImageFile> 
{

	/** Serializable version identifier */
	private static final long serialVersionUID = 300273665634354036L;

	private static final String EJBQL = "select imageFile from ImageFile imageFile";

	private static final String[] RESTRICTIONS = { "lower(imageFile.originalFileName) like lower(concat(#{imageFileList.imageFile.originalFileName},'%'))", };

	private ImageFile imageFile = new ImageFile();

	public ImageFileList() {
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public ImageFile getImageFile() {
		return imageFile;
	}
}
