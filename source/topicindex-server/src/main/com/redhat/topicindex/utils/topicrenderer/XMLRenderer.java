package com.redhat.topicindex.utils.topicrenderer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.xml.transform.TransformerException;

import com.redhat.ecs.commonutils.XSLTUtilities;
import com.redhat.ecs.commonutils.ZipUtilities;
import com.redhat.topicindex.utils.EntityUtilities;

/**
 * This class provides a way to convert docbook to HTML.
 */
public class XMLRenderer
{
	/**
	 * This is the ID of the BlobConstant record that holds the docbook XSL ZIP
	 * archive downloaded from
	 * http://sourceforge.net/projects/docbook/files/docbook-xsl/
	 */
	private static final Integer DOCBOOK_ZIP_ID = 6;
	/**
	 * This is the ID of the BlobConstant record that holds the DocBook DTD ZIP
	 * archive downloaded from
	 * http://www.docbook.org/xml/4.5/docbook-xml-4.5.zip
	 */
	private static final Integer DOCBOOK_DTD_ZIP_FILE = 8;
	/**
	 * This is the URL of the xsl files imported by the html.xsl file below. We
	 * use this as the system id when using a URIResolver to allow us to track
	 * the context in which files are imported.
	 */
	private static final String DOCBOOK_XSL_URL = "http://docbook.sourceforge.net/release/xsl/current/";

	private static final String DOCBOOK_XSL_SYSTEMID = "http://docbook.sourceforge.net/release/xsl/current/xhtml/docbook.xsl";

	private static final String DOCBOOK_DTD_SYSTEMID = "http://www.oasis-open.org/docbook/xml/4.5/";

	private static Map<String, byte[]> docbookFiles = null;
	private static Map<String, String> parameters = null;

	private synchronized static void initialize(final EntityManager entityManager)
	{
		if (docbookFiles == null)
		{
			System.out.println("Initializing XMLRenderer");

			/* set the global parameters required for building the docbook */
			parameters = Collections.unmodifiableMap(new HashMap<String, String>());

			final byte[] docbookZip = EntityUtilities.loadBlobConstant(entityManager, DOCBOOK_ZIP_ID);
			final byte[] docbookDTDZip = EntityUtilities.loadBlobConstant(entityManager, DOCBOOK_DTD_ZIP_FILE);

			if (docbookZip != null)
			{
				/* load the xsl files from the docbook xsl package */
				docbookFiles = ZipUtilities.mapZipFile(docbookZip, DOCBOOK_XSL_URL, "docbook-xsl-1.76.1/");
				ZipUtilities.mapZipFile(docbookDTDZip, docbookFiles, DOCBOOK_DTD_SYSTEMID, "");
				
				/* make the collection read only for the threads */
				docbookFiles = Collections.unmodifiableMap(docbookFiles);
			}
		}
	}

	public static String transformDocbook(final EntityManager entityManager, final String xml) throws TransformerException
	{
		initialize(entityManager);

		if (xml != null && docbookFiles != null && docbookFiles.containsKey(DOCBOOK_XSL_SYSTEMID))
			return XSLTUtilities.transformXML(xml, new String(docbookFiles.get(DOCBOOK_XSL_SYSTEMID)), DOCBOOK_XSL_SYSTEMID, docbookFiles, parameters);

		return null;
	}
}
