package com.redhat.topicindex.session;

import javax.persistence.PersistenceException;

import com.redhat.topicindex.entity.*;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;

import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.validator.InvalidStateException;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("imageFileHome")
public class ImageFileHome extends EntityHome<ImageFile> implements DisplayMessageInterface
{

	/** Serializable version identifier */
	private static final long serialVersionUID = 554234315046093282L;
	/** The message to be displayed to the user */
	private String displayMessage;
	private byte[] imageData;

	public void setImageFileImageFileId(Integer id)
	{
		setId(id);
	}

	public Integer getImageFileImageFileId()
	{
		return (Integer) getId();
	}

	@Override
	protected ImageFile createInstance()
	{
		ImageFile imageFile = new ImageFile();
		return imageFile;
	}

	public void load()
	{
		if (isIdDefined())
		{
			wire();
		}
	}

	public void wire()
	{
		getInstance();
	}

	public boolean isWired()
	{
		return true;
	}

	public ImageFile getDefinedInstance()
	{
		return isIdDefined() ? getInstance() : null;
	}

	@Override
	public String getDisplayMessage()
	{
		return displayMessage;
	}
	
	@Override
	public String getDisplayMessageAndClear()
	{
		final String retValue = this.displayMessage;
		this.displayMessage = null;
		return retValue;
	}

	public void setDisplayMessage(String displayMessage)
	{
		this.displayMessage = displayMessage;
	}

	@Override
	public String persist()
	{
		try
		{
			if (imageData != null)
				this.getInstance().setImageData(imageData);
			
			return super.persist();
		}
		catch (final InvalidStateException ex)
		{
			this.setDisplayMessage("The image requires a image file to be uploaded");
			SkynetExceptionUtilities.handleException(ex, true, "Probably a constraint violation");
		}
		catch (final PersistenceException ex)
		{
			if (ex.getCause() instanceof ConstraintViolationException)
			{
				this.setDisplayMessage("The image violated a constraint");
				SkynetExceptionUtilities.handleException(ex, true, "Probably a constraint violation");
			}
			else
			{
				this.setDisplayMessage("The image could not be saved. " + Constants.GENERIC_ERROR_INSTRUCTIONS);
				SkynetExceptionUtilities.handleException(ex, false, "Probably an error updating a Tag entity");
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error persisting a ImageFile entity");
			this.setDisplayMessage("The image could not be saved. " + Constants.GENERIC_ERROR_INSTRUCTIONS);
		}

		return null;
	}

	@Override
	public String update()
	{
		try
		{
			if (imageData != null)
				this.getInstance().setImageData(imageData);
			
			return super.update();
		}
		catch (final InvalidStateException ex)
		{
			this.setDisplayMessage("The image requires a image file to be uploaded");
			SkynetExceptionUtilities.handleException(ex, true, "Probably a constraint violation");
		}
		catch (final PersistenceException ex)
		{
			if (ex.getCause() instanceof ConstraintViolationException)
			{
				this.setDisplayMessage("The image violated a constraint");
				SkynetExceptionUtilities.handleException(ex, true, "Probably a constraint violation");
			}
			else
			{
				this.setDisplayMessage("The image could not be saved. " + Constants.GENERIC_ERROR_INSTRUCTIONS);
				SkynetExceptionUtilities.handleException(ex, false, "Probably an error updating a Tag entity");
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error persisting a ImageFile entity");
			this.setDisplayMessage("The image could not be saved. " + Constants.GENERIC_ERROR_INSTRUCTIONS);
		}

		return null;
	}

	public byte[] getImageData()
	{
		return imageData;
	}

	public void setImageData(byte[] imageData)
	{
		this.imageData = imageData;
	}

}
