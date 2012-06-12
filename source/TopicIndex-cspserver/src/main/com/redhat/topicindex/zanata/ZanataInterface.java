package com.redhat.topicindex.zanata;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.zanata.common.LocaleId;
import org.zanata.rest.client.IFixedTranslationResources;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;

public class ZanataInterface
{
	public static boolean getZanataResourceExists(final String id)
	{
		try
		{
			final ZanataDetails details = new ZanataDetails();
			final String URI = details.getUrl();

			final IFixedTranslationResources client = ProxyFactory.create(IFixedTranslationResources.class, URI);
			final ClientResponse<Resource> response = client.getResource(id);

			final Status status = Response.Status.fromStatusCode(response.getStatus());
			
			return status == Response.Status.OK;

		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error using the Zanata REST interface");
		}

		return false;
	}
	
	public static Resource getZanataResource(final String id)
	{
		try
		{
			final ZanataDetails details = new ZanataDetails();
			final String URI = details.getUrl();

			final IFixedTranslationResources client = ProxyFactory.create(IFixedTranslationResources.class, URI);
			final ClientResponse<Resource> response = client.getResource(id);

			final Status status = Response.Status.fromStatusCode(response.getStatus());
			
			if (status == Response.Status.OK)
			{
				final Resource entity = response.getEntity();
				return entity;
			}
			else
			{
				System.out.println("REST call to getResource() did not complete successfully. HTTP response code was " + status.getStatusCode() + ". Reason was " + status.getReasonPhrase());
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error using the Zanata REST interface");
		}

		return null;
	}

	public static void createFile(final Resource resource)
	{
		try
		{
			final ZanataDetails details = new ZanataDetails();
			final String URI = details.getUrl();

			final IFixedTranslationResources client = ProxyFactory.create(IFixedTranslationResources.class, URI);
			final ClientResponse<String> response = client.createResource(details.getUsername(), details.getToken(), resource);
			
			final Status status = Response.Status.fromStatusCode(response.getStatus());
			
			if (status == Response.Status.CREATED)
			{
				final String entity = response.getEntity();
				if (entity.trim().length() != 0)
					System.out.println(entity);
			}
			else
			{
				System.out.println("REST call to createResource() did not complete successfully. HTTP response code was " + status.getStatusCode() + ". Reason was " + status.getReasonPhrase());
			}
			
			
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error using the Zanata REST interface");
		}
	}

	public static TranslationsResource getTranslations(final String id, final LocaleId locale)
	{
		try
		{
			final ZanataDetails details = new ZanataDetails();
			final String URI = details.getUrl();

			final IFixedTranslationResources client = ProxyFactory.create(IFixedTranslationResources.class, URI);
			final ClientResponse<TranslationsResource> response = client.getTranslations(id, locale);

			if (Response.Status.fromStatusCode(response.getStatus()) == Response.Status.OK)
			{
				final TranslationsResource retValue = response.getEntity();
				return retValue;
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error using the Zanata REST interface");
		}

		return null;
	}
	
	public static TranslationsResource deleteResource(final String id)
	{
		try
		{
			final ZanataDetails details = new ZanataDetails();
			final String URI = details.getUrl();

			final IFixedTranslationResources client = ProxyFactory.create(IFixedTranslationResources.class, URI);
			final ClientResponse<String> response = client.deleteResource(details.getUsername(), details.getToken(), id);
			
			final Status status = Response.Status.fromStatusCode(response.getStatus());
			
			if (status == Response.Status.OK)
			{
				final String entity = response.getEntity();
				if (entity.trim().length() != 0)
					System.out.println(entity);
			}
			else
			{
				System.out.println("REST call to deleteResource() did not complete successfully. HTTP response code was " + status.getStatusCode() + ". Reason was " + status.getReasonPhrase());
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error using the Zanata REST interface");
		}

		return null;
	}
}

/**
 * A utility class to pull out the Zanata details from the system properties
 */
class ZanataDetails
{
	private String server;
	private String project;
	private String version;
	private String username;
	private String token;
	private String url;

	public ZanataDetails()
	{
		this.server = System.getProperty(Constants.ZANATA_SERVER_PROPERTY);
		this.project = System.getProperty(Constants.ZANATA_PROJECT_PROPERTY);
		this.version = System.getProperty(Constants.ZANATA_PROJECT_VERSION_PROPERTY);
		this.username = System.getProperty(Constants.ZANATA_USERNAME_PROPERTY);
		this.token = System.getProperty(Constants.ZANATA_TOKEN_PROPERTY);
		this.url = server + "/seam/resource/restv1/projects/p/" + project + "/iterations/i/" + version + "/r";
	}

	public String getServer()
	{
		return server;
	}

	public void setServer(String server)
	{
		this.server = server;
	}

	public String getProject()
	{
		return project;
	}

	public void setProject(String project)
	{
		this.project = project;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getToken()
	{
		return token;
	}

	public void setToken(String token)
	{
		this.token = token;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}
}
