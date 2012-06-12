package com.redhat.topicindex.entity;

import static javax.persistence.GenerationType.IDENTITY;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.swing.ImageIcon;

import org.apache.commons.codec.binary.Base64;
import org.hibernate.envers.Audited;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.redhat.topicindex.entity.base.AuditedEntity;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.utils.SVGIcon;

@Entity
@Audited
@Table(name = "ImageFile")
public class ImageFile extends AuditedEntity<ImageFile> implements java.io.Serializable
{
	private static String SVG_MIME_TYPE = "image/svg+xml";
	private static String JPG_MIME_TYPE = "image/jpeg";
	private static String GIF_MIME_TYPE = "image/gif";
	private static String PNG_MIME_TYPE = "image/png";

	/** The dimensions of the generated thumbnail */
	private static final int THUMBNAIL_SIZE = 64;
	private static final long serialVersionUID = -3885332582642450795L;
	private Integer imageFileId;
	private String originalFileName;
	private byte[] imageData;
	private byte[] thumbnail;
	private byte[] imageDataBase64;
	private String description;

	public ImageFile()
	{
		super(ImageFile.class);
	}

	public ImageFile(final Integer imageFileId, final String originalFileName, final byte[] imageData)
	{
		super(ImageFile.class);
		this.imageFileId = imageFileId;
		this.originalFileName = originalFileName;
		this.imageData = imageData;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ImageFileID", unique = true, nullable = false)
	public Integer getImageFileId()
	{
		return this.imageFileId;
	}

	public void setImageFileId(final Integer imageFileId)
	{
		this.imageFileId = imageFileId;
	}

	@Column(name = "OriginalFileName", nullable = false, length = 512)
	@NotNull
	@Length(max = 512)
	public String getOriginalFileName()
	{
		return this.originalFileName;
	}

	public void setOriginalFileName(final String originalFileName)
	{
		this.originalFileName = originalFileName;
	}

	@Column(name = "ImageDataBase64", nullable = false)
	@NotNull
	public byte[] getImageDataBase64()
	{
		return this.imageDataBase64;
	}

	public void setImageDataBase64(final byte[] imageDataBase64)
	{
		this.imageDataBase64 = imageDataBase64;
	}

	@Transient
	public String getImageDataBase64String()
	{
		return this.imageDataBase64 == null ? "" : new String(this.imageDataBase64);
	}

	@Column(name = "ImageData", nullable = false)
	@NotNull
	public byte[] getImageData()
	{
		return this.imageData;
	}

	public void setImageData(final byte[] imageData)
	{
		this.imageData = imageData;
	}

	@Column(name = "ThumbnailData", nullable = false)
	@NotNull
	public byte[] getThumbnailData()
	{
		return this.thumbnail;
	}

	public void setThumbnailData(final byte[] thumbnail)
	{
		this.thumbnail = thumbnail;
	}

	@Transient
	public String getThumbnailDataString()
	{
		return this.thumbnail == null ? "" : new String(this.thumbnail);
	}

	@SuppressWarnings("unused")
	@PrePersist
	@PreUpdate
	private void updateImageData()
	{
		this.thumbnail = createImage(true);
		this.imageDataBase64 = createImage(false);
	}

	// @Column(name = "Description", length = 65535)
	@Column(name = "Description", columnDefinition = "TEXT")
	@Length(max = 65535)
	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(final String description)
	{
		this.description = description;
	}

	private byte[] createImage(final boolean resize)
	{
		if (this.imageData == null)
			return null;
		
		try
		{
			BufferedImage outImage = null;
			
			if (this.getMimeType().equals(SVG_MIME_TYPE))
			{
				SVGIcon svgIcon = null;
				if (resize)
					svgIcon = new SVGIcon(new ByteArrayInputStream(this.imageData), THUMBNAIL_SIZE, THUMBNAIL_SIZE);
				else
					svgIcon = new SVGIcon(new ByteArrayInputStream(this.imageData));
				
				
				outImage = new BufferedImage(svgIcon.getIconWidth(), svgIcon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
				final Graphics2D g2d = outImage.createGraphics();
				
				svgIcon.setBackgroundColour(Color.WHITE);
				svgIcon.paintIcon(null, g2d, 0, 0);
				g2d.dispose();
			}
			else
			{
				final ImageIcon imageIcon = new ImageIcon(this.imageData);
				final Image inImage = imageIcon.getImage();

				double scale = 1.0d;
				if (resize)
				{
					/*
					 * the final image will be at most THUMBNAIL_SIZE pixels
					 * high and/or wide
					 */
					final double heightScale = (double) THUMBNAIL_SIZE / (double) inImage.getHeight(null);
					final double widthScale = (double) THUMBNAIL_SIZE / (double) inImage.getWidth(null);
					scale = Math.min(heightScale, widthScale);
				}
				
				final int newWidth = (int) (imageIcon.getIconWidth() * scale);
				final int newHeight = (int) (imageIcon.getIconHeight() * scale);
				outImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
				final Graphics2D g2d = outImage.createGraphics();

				final AffineTransform tx = new AffineTransform();

				if (scale < 1.0d)
					tx.scale(scale, scale);

				g2d.drawImage(inImage, tx, null);
				g2d.dispose();
			}
			
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(outImage, "JPG", baos);
			final byte[] bytesOut = baos.toByteArray();

			return Base64.encodeBase64(bytesOut);

		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "An error creating an image thumbnail");
		}

		return null;
	}

	@Transient
	public String getDocbookFileName()
	{
		if (this.originalFileName != null && this.imageFileId != null)
		{
			final int extensionIndex = this.originalFileName.lastIndexOf(".");
			if (extensionIndex != -1)
				return this.imageFileId + this.originalFileName.substring(extensionIndex);
		}

		return "";
	}

	@Transient
	public String getMimeType()
	{
		final int lastPeriodIndex = this.originalFileName.lastIndexOf(".");
		if (lastPeriodIndex != -1 && lastPeriodIndex < this.originalFileName.length() - 1)
		{
			final String extension = this.originalFileName.substring(lastPeriodIndex + 1);
			if (extension.equalsIgnoreCase("JPG"))
				return JPG_MIME_TYPE;
			if (extension.equalsIgnoreCase("GIF"))
				return GIF_MIME_TYPE;
			if (extension.equalsIgnoreCase("PNG"))
				return PNG_MIME_TYPE;
			if (extension.equalsIgnoreCase("SVG"))
				return SVG_MIME_TYPE;
		}

		return "application/octet-stream";
	}

	@Transient
	public String getImageDataString()
	{
		if (imageData == null)
			return "";
		return new String(imageData);
	}

	@Override
	@Transient
	public Integer getId()
	{
		return this.imageFileId;
	}

}
