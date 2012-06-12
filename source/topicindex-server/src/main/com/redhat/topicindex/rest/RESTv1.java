package com.redhat.topicindex.rest;

import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;

import com.redhat.topicindex.entity.BlobConstants;
import com.redhat.topicindex.entity.Category;
import com.redhat.topicindex.entity.ImageFile;
import com.redhat.topicindex.entity.Project;
import com.redhat.topicindex.entity.StringConstants;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.entity.Role;
import com.redhat.topicindex.entity.TranslatedTopicData;
import com.redhat.topicindex.entity.User;
import com.redhat.topicindex.entity.PropertyTag;
import com.redhat.topicindex.entity.TranslatedTopicString;
import com.redhat.topicindex.filter.TopicFilterQueryBuilder;
import com.redhat.topicindex.filter.TranslatedTopicDataFilterQueryBuilder;
import com.redhat.topicindex.rest.collections.BaseRestCollectionV1;
import com.redhat.topicindex.rest.entities.BlobConstantV1;
import com.redhat.topicindex.rest.entities.CategoryV1;
import com.redhat.topicindex.rest.entities.ImageV1;
import com.redhat.topicindex.rest.entities.ProjectV1;
import com.redhat.topicindex.rest.entities.PropertyTagV1;
import com.redhat.topicindex.rest.entities.RoleV1;
import com.redhat.topicindex.rest.entities.StringConstantV1;
import com.redhat.topicindex.rest.entities.TagV1;
import com.redhat.topicindex.rest.entities.TopicV1;
import com.redhat.topicindex.rest.entities.TranslatedTopicStringV1;
import com.redhat.topicindex.rest.entities.TranslatedTopicV1;
import com.redhat.topicindex.rest.entities.UserV1;
import com.redhat.topicindex.rest.exceptions.InternalProcessingException;
import com.redhat.topicindex.rest.exceptions.InvalidParameterException;
import com.redhat.topicindex.rest.expand.ExpandDataTrunk;
import com.redhat.topicindex.rest.expand.ExpandDataDetails;
import com.redhat.topicindex.rest.sharedinterface.RESTInterfaceV1;
import com.redhat.topicindex.utils.Constants;

import org.jboss.resteasy.plugins.providers.atom.Feed;

/**
 * The Skynet REST interface implementation
 */
@Path("/1")
public class RESTv1 extends BaseRESTv1 implements RESTInterfaceV1
{
	/* SYSTEM FUNCTIONS */
	@Override
	@PUT
	@Path("/settings/rerenderTopic")
	@Consumes({ "*" })
	public void setRerenderTopic(@QueryParam("enabled") final Boolean enalbed)
	{
		System.setProperty(Constants.ENABLE_RENDERING_PROPERTY, enalbed == null ? null : enalbed.toString());
	}

	@Override
	@GET
	@Path("/expanddatatrunk/get/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public ExpandDataTrunk getJSONExpandTrunkExample() throws InvalidParameterException, InternalProcessingException
	{
		return new ExpandDataTrunk(new ExpandDataDetails("collectionname"));
	}

	/* BLOBCONSTANTS FUNCTIONS */
	/*		JSONP FUNCTIONS */	
	@Override
	@GET
	@Path("/blobconstant/get/jsonp/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPBlobConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONBlobConstant(id, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@GET
	@Path("/blobconstants/get/jsonp/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPBlobConstants(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONBlobConstants(expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@PUT
	@Path("/blobconstant/put/jsonp")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public String updateJSONPBlobConstant(@QueryParam("expand") final String expand, final BlobConstantV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(updateJSONBlobConstant(expand, dataObject)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@PUT
	@Path("/blobconstants/put/jsonp")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public String updateJSONPBlobConstants(@QueryParam("expand") final String expand, final BaseRestCollectionV1<BlobConstantV1> dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(updateJSONBlobConstants(expand, dataObjects)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@POST
	@Path("/blobconstant/post/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public String createJSONPBlobConstant(@QueryParam("expand") final String expand, final BlobConstantV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(createJSONBlobConstant(expand, dataObject)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}


	@Override
	@POST
	@Path("/blobconstants/post/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public String createJSONPBlobConstants(@QueryParam("expand") final String expand, final BaseRestCollectionV1<BlobConstantV1> dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(createJSONBlobConstants(expand, dataObjects)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@DELETE
	@Path("/blobconstant/delete/jsonp/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String deleteJSONPBlobConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(deleteJSONBlobConstant(id, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@DELETE
	@Path("/blobconstants/delete/jsonp/{ids}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String deleteJSONPBlobConstants(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(deleteJSONBlobConstants(ids, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}
	
	/*		JSON FUNCTIONS */	
	@Override
	@GET
	@Path("/blobconstant/get/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BlobConstantV1 getJSONBlobConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (id == null)
			throw new InvalidParameterException("The id parameter can not be null");

		return getJSONResource(BlobConstants.class, new BlobConstantV1Factory(), id, expand);
	}

	@Override
	@GET
	@Path("/blobconstants/get/json/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<BlobConstantV1> getJSONBlobConstants(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		return getJSONResources(BlobConstants.class, new BlobConstantV1Factory(), BaseRESTv1.BLOBCONSTANTS_EXPANSION_NAME, expand);
	}

	@Override
	@PUT
	@Path("/blobconstant/put/json")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public BlobConstantV1 updateJSONBlobConstant(@QueryParam("expand") final String expand, final BlobConstantV1 dataObject) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObject == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		if (dataObject.getId() == null)
			throw new InvalidParameterException("The dataObject.getId() parameter can not be null");

		final BlobConstantV1Factory factory = new BlobConstantV1Factory();
		return updateEntity(BlobConstants.class, dataObject, factory, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@PUT
	@Path("/blobconstants/put/json")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public BaseRestCollectionV1<BlobConstantV1> updateJSONBlobConstants(@QueryParam("expand") final String expand, final BaseRestCollectionV1<BlobConstantV1> dataObjects) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObjects == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		if (dataObjects.getItems() == null)
			throw new InvalidParameterException("The dataObjects.getItems() parameter can not be null");

		final BlobConstantV1Factory factory = new BlobConstantV1Factory();
		return updateEntities(BlobConstants.class, dataObjects, factory, BaseRESTv1.BLOBCONSTANTS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	@Override
	@POST
	@Path("/blobconstant/post/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public BlobConstantV1 createJSONBlobConstant(@QueryParam("expand") final String expand, final BlobConstantV1 dataObject) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObject == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		final BlobConstantV1Factory factory = new BlobConstantV1Factory();
		return createEntity(BlobConstants.class, dataObject, factory, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@POST
	@Path("/blobconstants/post/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public BaseRestCollectionV1<BlobConstantV1> createJSONBlobConstants(@QueryParam("expand") final String expand, final BaseRestCollectionV1<BlobConstantV1> dataObjects) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObjects == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		if (dataObjects.getItems() == null)
			throw new InvalidParameterException("The dataObjects.getItems() parameter can not be null");

		final BlobConstantV1Factory factory = new BlobConstantV1Factory();
		return createEntities(BlobConstants.class, dataObjects, factory, BaseRESTv1.BLOBCONSTANTS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	@Override
	@DELETE
	@Path("/blobconstant/delete/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BlobConstantV1 deleteJSONBlobConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (id == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		final BlobConstants dbEntity = deleteEntity(BlobConstants.class, id);
		return new BlobConstantV1Factory().createRESTEntityFromDBEntity(dbEntity, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@DELETE
	@Path("/blobconstants/delete/json/{ids}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<BlobConstantV1> deleteJSONBlobConstants(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (ids == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		final Set<String> dbEntityIds = ids.getMatrixParameters().keySet();

		final BlobConstantV1Factory factory = new BlobConstantV1Factory();
		final List<BlobConstants> dbEntities = deleteEntities(BlobConstants.class, dbEntityIds, factory);
		return new RESTDataObjectCollectionFactory<BlobConstantV1, BlobConstants>().create(factory, dbEntities, BaseRESTv1.BLOBCONSTANTS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	/* PROJECT FUNCTIONS */
	/*		JSONP FUNCTIONS */
	@Override
	@GET
	@Path("/project/get/jsonp/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPProject(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONProject(id, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@GET
	@Path("/projects/get/jsonp/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPProjects(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONProjects(expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@PUT
	@Path("/project/put/jsonp")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public String updateJSONPProject(@QueryParam("expand") final String expand, final ProjectV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(updateJSONProject(expand, dataObject)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@PUT
	@Path("/projects/put/jsonp")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public String updateJSONPProjects(@QueryParam("expand") final String expand, final BaseRestCollectionV1<ProjectV1> dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(updateJSONProjects(expand, dataObjects)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@POST
	@Path("/project/post/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public String createJSONPProject(@QueryParam("expand") final String expand, final ProjectV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(createJSONProject(expand, dataObject)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@POST
	@Path("/projects/post/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public String createJSONPProjects(@QueryParam("expand") final String expand, final BaseRestCollectionV1<ProjectV1> dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(createJSONProjects(expand, dataObjects)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@DELETE
	@Path("/project/delete/jsonp/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String deleteJSONPProject(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(deleteJSONProject(id, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@DELETE
	@Path("/projects/delete/jsonp/{ids}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String deleteJSONPProjects(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(deleteJSONProjects(ids, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}
	
	/*		JSON FUNCTIONS */	
	@Override
	@GET
	@Path("/project/get/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public ProjectV1 getJSONProject(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (id == null)
			throw new InvalidParameterException("The id parameter can not be null");

		return getJSONResource(Project.class, new ProjectV1Factory(), id, expand);
	}

	@Override
	@GET
	@Path("/projects/get/json/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<ProjectV1> getJSONProjects(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		return getJSONResources(Project.class, new ProjectV1Factory(), BaseRESTv1.PROJECTS_EXPANSION_NAME, expand);
	}

	@Override
	@PUT
	@Path("/project/put/json")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public ProjectV1 updateJSONProject(@QueryParam("expand") final String expand, final ProjectV1 dataObject) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObject == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		if (dataObject.getId() == null)
			throw new InvalidParameterException("The dataObject.getId() parameter can not be null");

		final ProjectV1Factory factory = new ProjectV1Factory();
		return updateEntity(Project.class, dataObject, factory, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@PUT
	@Path("/projects/put/json")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public BaseRestCollectionV1<ProjectV1> updateJSONProjects(@QueryParam("expand") final String expand, final BaseRestCollectionV1<ProjectV1> dataObjects) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObjects == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		if (dataObjects.getItems() == null)
			throw new InvalidParameterException("The dataObjects.getItems() parameter can not be null");

		final ProjectV1Factory factory = new ProjectV1Factory();
		return updateEntities(Project.class, dataObjects, factory, BaseRESTv1.PROJECTS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	@Override
	@POST
	@Path("/project/post/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public ProjectV1 createJSONProject(@QueryParam("expand") final String expand, final ProjectV1 dataObject) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObject == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		final ProjectV1Factory factory = new ProjectV1Factory();
		return createEntity(Project.class, dataObject, factory, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@POST
	@Path("/projects/post/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public BaseRestCollectionV1<ProjectV1> createJSONProjects(@QueryParam("expand") final String expand, final BaseRestCollectionV1<ProjectV1> dataObjects) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObjects == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		if (dataObjects.getItems() == null)
			throw new InvalidParameterException("The dataObjects.getItems() parameter can not be null");

		final ProjectV1Factory factory = new ProjectV1Factory();
		return createEntities(Project.class, dataObjects, factory, BaseRESTv1.PROJECTS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	@Override
	@DELETE
	@Path("/project/delete/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public ProjectV1 deleteJSONProject(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (id == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		final Project dbEntity = deleteEntity(Project.class, id);
		return new ProjectV1Factory().createRESTEntityFromDBEntity(dbEntity, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@DELETE
	@Path("/projects/delete/json/{ids}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<ProjectV1> deleteJSONProjects(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (ids == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		final Set<String> dbEntityIds = ids.getMatrixParameters().keySet();

		final ProjectV1Factory factory = new ProjectV1Factory();
		final List<Project> dbEntities = deleteEntities(Project.class, dbEntityIds, factory);
		return new RESTDataObjectCollectionFactory<ProjectV1, Project>().create(factory, dbEntities, BaseRESTv1.PROJECTS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	/* PROPERYTAG FUNCTIONS */
	/*		JSONP FUNCTIONS */	
	@Override
	@GET
	@Path("/propertytag/get/jsonp/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPPropertyTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONPropertyTag(id, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@GET
	@Path("/propertytags/get/jsonp/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPPropertyTags(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONPropertyTags(expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@PUT
	@Path("/propertytag/put/jsonp")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public String updateJSONPPropertyTag(@QueryParam("expand") final String expand, final PropertyTagV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(updateJSONPropertyTag(expand, dataObject)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@PUT
	@Path("/propertytags/put/jsonp")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public String updateJSONPPropertyTags(@QueryParam("expand") final String expand, final BaseRestCollectionV1<PropertyTagV1> dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(updateJSONPropertyTags(expand, dataObjects)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@POST
	@Path("/propertytag/post/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public String createJSONPPropertyTag(@QueryParam("expand") final String expand, final PropertyTagV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(createJSONPropertyTag(expand, dataObject)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@POST
	@Path("/propertytags/post/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public String createJSONPPropertyTags(@QueryParam("expand") final String expand, final BaseRestCollectionV1<PropertyTagV1> dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(createJSONPropertyTags(expand, dataObjects)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@DELETE
	@Path("/propertytag/delete/jsonp/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String deleteJSONPPropertyTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(deleteJSONPropertyTag(id, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@DELETE
	@Path("/propertytags/delete/jsonp/{ids}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String deleteJSONPPropertyTags(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(deleteJSONPropertyTags(ids, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}
	
	/*		JSON FUNCTIONS */	
	@Override
	@GET
	@Path("/propertytag/get/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public PropertyTagV1 getJSONPropertyTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (id == null)
			throw new InvalidParameterException("The id parameter can not be null");

		return getJSONResource(PropertyTag.class, new PropertyTagV1Factory(), id, expand);
	}

	@Override
	@GET
	@Path("/propertytags/get/json/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<PropertyTagV1> getJSONPropertyTags(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		return getJSONResources(PropertyTag.class, new PropertyTagV1Factory(), BaseRESTv1.PROPERTYTAGS_EXPANSION_NAME, expand);
	}

	@Override
	@PUT
	@Path("/propertytag/put/json")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public PropertyTagV1 updateJSONPropertyTag(@QueryParam("expand") final String expand, final PropertyTagV1 dataObject) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObject == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		if (dataObject.getId() == null)
			throw new InvalidParameterException("The dataObject.getId() parameter can not be null");

		final PropertyTagV1Factory factory = new PropertyTagV1Factory();
		return updateEntity(PropertyTag.class, dataObject, factory, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@PUT
	@Path("/propertytags/put/json")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public BaseRestCollectionV1<PropertyTagV1> updateJSONPropertyTags(@QueryParam("expand") final String expand, final BaseRestCollectionV1<PropertyTagV1> dataObjects) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObjects == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		if (dataObjects.getItems() == null)
			throw new InvalidParameterException("The dataObjects.getItems() parameter can not be null");

		final PropertyTagV1Factory factory = new PropertyTagV1Factory();
		return updateEntities(PropertyTag.class, dataObjects, factory, BaseRESTv1.PROPERTYTAGS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	@Override
	@POST
	@Path("/propertytag/post/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public PropertyTagV1 createJSONPropertyTag(@QueryParam("expand") final String expand, final PropertyTagV1 dataObject) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObject == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		final PropertyTagV1Factory factory = new PropertyTagV1Factory();
		return createEntity(PropertyTag.class, dataObject, factory, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@POST
	@Path("/propertytags/post/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public BaseRestCollectionV1<PropertyTagV1> createJSONPropertyTags(@QueryParam("expand") final String expand, final BaseRestCollectionV1<PropertyTagV1> dataObjects) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObjects == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		if (dataObjects.getItems() == null)
			throw new InvalidParameterException("The dataObjects.getItems() parameter can not be null");

		final PropertyTagV1Factory factory = new PropertyTagV1Factory();
		return createEntities(PropertyTag.class, dataObjects, factory, BaseRESTv1.PROPERTYTAGS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	@Override
	@DELETE
	@Path("/propertytag/delete/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public PropertyTagV1 deleteJSONPropertyTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (id == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		final PropertyTag dbEntity = deleteEntity(PropertyTag.class, id);
		return new PropertyTagV1Factory().createRESTEntityFromDBEntity(dbEntity, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@DELETE
	@Path("/propertytags/delete/json/{ids}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<PropertyTagV1> deleteJSONPropertyTags(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (ids == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		final Set<String> dbEntityIds = ids.getMatrixParameters().keySet();

		final PropertyTagV1Factory factory = new PropertyTagV1Factory();
		final List<PropertyTag> dbEntities = deleteEntities(PropertyTag.class, dbEntityIds, factory);
		return new RESTDataObjectCollectionFactory<PropertyTagV1, PropertyTag>().create(factory, dbEntities, BaseRESTv1.PROPERTYTAGS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	/* ROLE FUNCTIONS */
	/*		JSONP FUNCTIONS */	
	@Override
	@GET
	@Path("/role/get/jsonp/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPRole(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONRole(id, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@GET
	@Path("/roles/get/jsonp/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPRoles(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONRoles(expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@PUT
	@Path("/role/put/jsonp")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public String updateJSONPRole(@QueryParam("expand") final String expand, final RoleV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(updateJSONRole(expand, dataObject)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@PUT
	@Path("/roles/put/jsonp")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public String updateJSONPRoles(@QueryParam("expand") final String expand, final BaseRestCollectionV1<RoleV1> dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(updateJSONRoles(expand, dataObjects)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@POST
	@Path("/role/post/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public String createJSONPRole(@QueryParam("expand") final String expand, final RoleV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(createJSONRole(expand, dataObject)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@POST
	@Path("/roles/post/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public String createJSONPRoles(@QueryParam("expand") final String expand, final BaseRestCollectionV1<RoleV1> dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(createJSONRoles(expand, dataObjects)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@DELETE
	@Path("/role/delete/jsonp/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String deleteJSONPRole(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(deleteJSONRole(id, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@DELETE
	@Path("/roles/delete/jsonp/{ids}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String deleteJSONPRoles(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(deleteJSONRoles(ids, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}
	
	/*		JSON FUNCTIONS */
	@Override
	@GET
	@Path("/role/get/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public RoleV1 getJSONRole(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (id == null)
			throw new InvalidParameterException("The id parameter can not be null");

		return getJSONResource(Role.class, new RoleV1Factory(), id, expand);
	}

	@Override
	@GET
	@Path("/roles/get/json/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<RoleV1> getJSONRoles(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		return getJSONResources(Role.class, new RoleV1Factory(), BaseRESTv1.ROLES_EXPANSION_NAME, expand);
	}

	@Override
	@PUT
	@Path("/role/put/json")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public RoleV1 updateJSONRole(@QueryParam("expand") final String expand, final RoleV1 dataObject) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObject == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		if (dataObject.getId() == null)
			throw new InvalidParameterException("The dataObject.getId() parameter can not be null");

		final RoleV1Factory factory = new RoleV1Factory();
		return updateEntity(Role.class, dataObject, factory, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@PUT
	@Path("/roles/put/json")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public BaseRestCollectionV1<RoleV1> updateJSONRoles(@QueryParam("expand") final String expand, final BaseRestCollectionV1<RoleV1> dataObjects) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObjects == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		if (dataObjects.getItems() == null)
			throw new InvalidParameterException("The dataObjects.getItems() parameter can not be null");

		final RoleV1Factory factory = new RoleV1Factory();
		return updateEntities(Role.class, dataObjects, factory, BaseRESTv1.ROLES_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	@Override
	@POST
	@Path("/role/post/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public RoleV1 createJSONRole(@QueryParam("expand") final String expand, final RoleV1 dataObject) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObject == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		final RoleV1Factory factory = new RoleV1Factory();
		return createEntity(Role.class, dataObject, factory, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@POST
	@Path("/roles/post/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public BaseRestCollectionV1<RoleV1> createJSONRoles(@QueryParam("expand") final String expand, final BaseRestCollectionV1<RoleV1> dataObjects) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObjects == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		if (dataObjects.getItems() == null)
			throw new InvalidParameterException("The dataObjects.getItems() parameter can not be null");

		final RoleV1Factory factory = new RoleV1Factory();
		return createEntities(Role.class, dataObjects, factory, BaseRESTv1.ROLES_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	@Override
	@DELETE
	@Path("/role/delete/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public RoleV1 deleteJSONRole(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (id == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		final Role dbEntity = deleteEntity(Role.class, id);
		return new RoleV1Factory().createRESTEntityFromDBEntity(dbEntity, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@DELETE
	@Path("/roles/delete/json/{ids}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<RoleV1> deleteJSONRoles(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (ids == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		final Set<String> dbEntityIds = ids.getMatrixParameters().keySet();

		final RoleV1Factory factory = new RoleV1Factory();
		final List<Role> dbEntities = deleteEntities(Role.class, dbEntityIds, factory);
		return new RESTDataObjectCollectionFactory<RoleV1, Role>().create(factory, dbEntities, BaseRESTv1.ROLES_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	/* TRANSLATEDTOPIC FUNCTIONS */
	/*		JSONP FUNCTIONS */	
	@Override
	@GET
	@Path("/translatedtopic/get/jsonp/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPTranslatedTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONTranslatedTopic(id, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@GET
	@Path("/translatedtopics/get/jsonp/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPTranslatedTopics(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONTranslatedTopics(expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@PUT
	@Path("/translatedtopic/put/jsonp")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public String updateJSONPTranslatedTopic(@QueryParam("expand") final String expand, final TranslatedTopicV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(updateJSONTranslatedTopic(expand, dataObject)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@PUT
	@Path("/translatedtopics/put/jsonp")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public String updateJSONPTranslatedTopics(@QueryParam("expand") final String expand, final BaseRestCollectionV1<TranslatedTopicV1> dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(updateJSONTranslatedTopics(expand, dataObjects)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@POST
	@Path("/translatedtopic/post/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public String createJSONPTranslatedTopic(@QueryParam("expand") final String expand, final TranslatedTopicV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(createJSONTranslatedTopic(expand, dataObject)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@POST
	@Path("/translatedtopics/post/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public String createJSONPTranslatedTopics(@QueryParam("expand") final String expand, final BaseRestCollectionV1<TranslatedTopicV1> dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(createJSONTranslatedTopics(expand, dataObjects)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@DELETE
	@Path("/translatedtopic/delete/jsonp/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String deleteJSONPTranslatedTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(deleteJSONTranslatedTopic(id, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@DELETE
	@Path("/translatedtopics/delete/jsonp/{ids}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String deleteJSONPTranslatedTopics(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(deleteJSONTranslatedTopics(ids, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}
	
	/*		JSON FUNCTIONS */	
	@Override
	@GET
	@Path("/translatedtopic/get/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public TranslatedTopicV1 getJSONTranslatedTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (id == null)
			throw new InvalidParameterException("The id parameter can not be null");

		return getJSONResource(TranslatedTopicData.class, new TranslatedTopicV1Factory(), id, expand);
	}
	
	@Override
	@GET
	@Path("/translatedtopics/get/json/{query}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<TranslatedTopicV1> getJSONTranslatedTopicsWithQuery(@PathParam("query") PathSegment query, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		return getJSONTopicsFromQuery(query.getMatrixParameters(), new TranslatedTopicDataFilterQueryBuilder(), new TranslatedTopicV1Factory(), TRANSLATEDTOPICS_EXPANSION_NAME, expand);
	}

	@Override
	@GET
	@Path("/translatedtopics/get/json/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<TranslatedTopicV1> getJSONTranslatedTopics(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		return getJSONResources(TranslatedTopicData.class, new TranslatedTopicV1Factory(), BaseRESTv1.TRANSLATEDTOPICS_EXPANSION_NAME, expand);
	}
	
	/*@Override
	@GET
	@Path("/translatedtopics/get/json/{query}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<TopicV1> getJSONTranslatedTopicsWithQuery(@PathParam("query") PathSegment query, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		return getJSONTopicsFromQuery(query.getMatrixParameters(), new TopicV1Factory(), TOPICS_EXPANSION_NAME, expand);
	}*/

	@Override
	@PUT
	@Path("/translatedtopic/put/json")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public TranslatedTopicV1 updateJSONTranslatedTopic(@QueryParam("expand") final String expand, final TranslatedTopicV1 dataObject) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObject == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		if (dataObject.getId() == null)
			throw new InvalidParameterException("The dataObject.getId() parameter can not be null");

		final TranslatedTopicV1Factory factory = new TranslatedTopicV1Factory();
		return updateEntity(TranslatedTopicData.class, dataObject, factory, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@PUT
	@Path("/translatedtopics/put/json")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public BaseRestCollectionV1<TranslatedTopicV1> updateJSONTranslatedTopics(@QueryParam("expand") final String expand, final BaseRestCollectionV1<TranslatedTopicV1> dataObjects) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObjects == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		if (dataObjects.getItems() == null)
			throw new InvalidParameterException("The dataObjects.getItems() parameter can not be null");

		final TranslatedTopicV1Factory factory = new TranslatedTopicV1Factory();
		return updateEntities(TranslatedTopicData.class, dataObjects, factory, BaseRESTv1.TRANSLATEDTOPICS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	@Override
	@POST
	@Path("/translatedtopic/post/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public TranslatedTopicV1 createJSONTranslatedTopic(@QueryParam("expand") final String expand, final TranslatedTopicV1 dataObject) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObject == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		final TranslatedTopicV1Factory factory = new TranslatedTopicV1Factory();
		return createEntity(TranslatedTopicData.class, dataObject, factory, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@POST
	@Path("/translatedtopics/post/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public BaseRestCollectionV1<TranslatedTopicV1> createJSONTranslatedTopics(@QueryParam("expand") final String expand, final BaseRestCollectionV1<TranslatedTopicV1> dataObjects) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObjects == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		if (dataObjects.getItems() == null)
			throw new InvalidParameterException("The dataObjects.getItems() parameter can not be null");

		final TranslatedTopicV1Factory factory = new TranslatedTopicV1Factory();
		return createEntities(TranslatedTopicData.class, dataObjects, factory, BaseRESTv1.TRANSLATEDTOPICS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	@Override
	@DELETE
	@Path("/translatedtopic/delete/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public TranslatedTopicV1 deleteJSONTranslatedTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (id == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		final TranslatedTopicData dbEntity = deleteEntity(TranslatedTopicData.class, id);
		return new TranslatedTopicV1Factory().createRESTEntityFromDBEntity(dbEntity, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@DELETE
	@Path("/translatedtopics/delete/json/{ids}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<TranslatedTopicV1> deleteJSONTranslatedTopics(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (ids == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		final Set<String> dbEntityIds = ids.getMatrixParameters().keySet();

		final TranslatedTopicV1Factory factory = new TranslatedTopicV1Factory();
		final List<TranslatedTopicData> dbEntities = deleteEntities(TranslatedTopicData.class, dbEntityIds, factory);
		return new RESTDataObjectCollectionFactory<TranslatedTopicV1, TranslatedTopicData>().create(factory, dbEntities, BaseRESTv1.TRANSLATEDTOPICS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	/* STRINGCONSTANT FUNCTIONS */
	/*		JSONP FUNCTIONS */
	@Override
	@GET
	@Path("/stringconstant/get/jsonp/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPStringConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONStringConstant(id, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@GET
	@Path("/stringconstants/get/jsonp/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPStringConstants(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONStringConstants(expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@PUT
	@Path("/stringconstant/put/jsonp")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public String updateJSONPStringConstant(@QueryParam("expand") final String expand, final StringConstantV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(updateJSONStringConstant(expand, dataObject)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@PUT
	@Path("/stringconstants/put/jsonp")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public String updateJSONPStringConstants(@QueryParam("expand") final String expand, final BaseRestCollectionV1<StringConstantV1> dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(updateJSONStringConstants(expand, dataObjects)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@POST
	@Path("/stringconstant/post/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public String createJSONPStringConstant(@QueryParam("expand") final String expand, final StringConstantV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(createJSONStringConstant(expand, dataObject)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@POST
	@Path("/stringconstants/post/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public String createJSONPStringConstants(@QueryParam("expand") final String expand, final BaseRestCollectionV1<StringConstantV1> dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(createJSONStringConstants(expand, dataObjects)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@DELETE
	@Path("/stringconstant/delete/jsonp/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String deleteJSONPStringConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(deleteJSONStringConstant(id, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@DELETE
	@Path("/stringconstants/delete/jsonp/{ids}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String deleteJSONPStringConstants(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(deleteJSONStringConstants(ids, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}
	
	/*		JSON FUNCTIONS */
	@Override
	@GET
	@Path("/stringconstant/get/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public StringConstantV1 getJSONStringConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (id == null)
			throw new InvalidParameterException("The id parameter can not be null");

		return getJSONResource(StringConstants.class, new StringConstantV1Factory(), id, expand);
	}

	@Override
	@GET
	@Path("/stringconstants/get/json/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<StringConstantV1> getJSONStringConstants(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		return getJSONResources(StringConstants.class, new StringConstantV1Factory(), BaseRESTv1.STRINGCONSTANTS_EXPANSION_NAME, expand);
	}

	@Override
	@PUT
	@Path("/stringconstant/put/json")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public StringConstantV1 updateJSONStringConstant(@QueryParam("expand") final String expand, final StringConstantV1 dataObject) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObject == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		if (dataObject.getId() == null)
			throw new InvalidParameterException("The dataObject.getId() parameter can not be null");

		final StringConstantV1Factory factory = new StringConstantV1Factory();
		return updateEntity(StringConstants.class, dataObject, factory, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@PUT
	@Path("/stringconstants/put/json")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public BaseRestCollectionV1<StringConstantV1> updateJSONStringConstants(@QueryParam("expand") final String expand, final BaseRestCollectionV1<StringConstantV1> dataObjects) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObjects == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		if (dataObjects.getItems() == null)
			throw new InvalidParameterException("The dataObjects.getItems() parameter can not be null");

		final StringConstantV1Factory factory = new StringConstantV1Factory();
		return updateEntities(StringConstants.class, dataObjects, factory, BaseRESTv1.STRINGCONSTANTS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	@Override
	@POST
	@Path("/stringconstant/post/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public StringConstantV1 createJSONStringConstant(@QueryParam("expand") final String expand, final StringConstantV1 dataObject) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObject == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		final StringConstantV1Factory factory = new StringConstantV1Factory();
		return createEntity(StringConstants.class, dataObject, factory, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@POST
	@Path("/stringconstants/post/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public BaseRestCollectionV1<StringConstantV1> createJSONStringConstants(@QueryParam("expand") final String expand, final BaseRestCollectionV1<StringConstantV1> dataObjects) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObjects == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		if (dataObjects.getItems() == null)
			throw new InvalidParameterException("The dataObjects.getItems() parameter can not be null");

		final StringConstantV1Factory factory = new StringConstantV1Factory();
		return createEntities(StringConstants.class, dataObjects, factory, BaseRESTv1.STRINGCONSTANTS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	@Override
	@DELETE
	@Path("/stringconstant/delete/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public StringConstantV1 deleteJSONStringConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (id == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		final StringConstants dbEntity = deleteEntity(StringConstants.class, id);
		return new StringConstantV1Factory().createRESTEntityFromDBEntity(dbEntity, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@DELETE
	@Path("/stringconstants/delete/json/{ids}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<StringConstantV1> deleteJSONStringConstants(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (ids == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		final Set<String> dbEntityIds = ids.getMatrixParameters().keySet();

		final StringConstantV1Factory factory = new StringConstantV1Factory();
		final List<StringConstants> dbEntities = deleteEntities(StringConstants.class, dbEntityIds, factory);
		return new RESTDataObjectCollectionFactory<StringConstantV1, StringConstants>().create(factory, dbEntities, BaseRESTv1.STRINGCONSTANTS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	/* USER FUNCTIONS */
	/*		JSONP FUNCTIONS */
	@Override
	@GET
	@Path("/user/get/jsonp/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPUser(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONUser(id, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@GET
	@Path("/users/get/jsonp/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPUsers(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONUsers(expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@PUT
	@Path("/user/put/jsonp")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public String updateJSONPUser(@QueryParam("expand") final String expand, final UserV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(updateJSONUser(expand, dataObject)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@PUT
	@Path("/users/put/jsonp")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public String updateJSONPUsers(@QueryParam("expand") final String expand, final BaseRestCollectionV1<UserV1> dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(updateJSONUsers(expand, dataObjects)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@POST
	@Path("/user/post/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public String createJSONPUser(@QueryParam("expand") final String expand, final UserV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(createJSONUser(expand, dataObject)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@POST
	@Path("/users/post/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public String createJSONPUsers(@QueryParam("expand") final String expand, final BaseRestCollectionV1<UserV1> dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(createJSONUsers(expand, dataObjects)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@DELETE
	@Path("/user/delete/jsonp/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String deleteJSONPUser(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(deleteJSONUser(id, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@DELETE
	@Path("/users/delete/jsonp/{ids}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String deleteJSONPUsers(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(deleteJSONUsers(ids, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	
	/*		JSON FUNCTIONS */
	@Override
	@GET
	@Path("/user/get/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public UserV1 getJSONUser(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (id == null)
			throw new InvalidParameterException("The id parameter can not be null");

		return getJSONResource(User.class, new UserV1Factory(), id, expand);
	}

	@Override
	@GET
	@Path("/users/get/json/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<UserV1> getJSONUsers(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		return getJSONResources(User.class, new UserV1Factory(), BaseRESTv1.USERS_EXPANSION_NAME, expand);
	}

	@Override
	@PUT
	@Path("/user/put/json")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public UserV1 updateJSONUser(@QueryParam("expand") final String expand, final UserV1 dataObject) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObject == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		if (dataObject.getId() == null)
			throw new InvalidParameterException("The dataObject.getId() parameter can not be null");

		final UserV1Factory factory = new UserV1Factory();
		return updateEntity(User.class, dataObject, factory, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@PUT
	@Path("/users/put/json")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public BaseRestCollectionV1<UserV1> updateJSONUsers(@QueryParam("expand") final String expand, final BaseRestCollectionV1<UserV1> dataObjects) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObjects == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		if (dataObjects.getItems() == null)
			throw new InvalidParameterException("The dataObjects.getItems() parameter can not be null");

		final UserV1Factory factory = new UserV1Factory();
		return updateEntities(User.class, dataObjects, factory, BaseRESTv1.USERS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	@Override
	@POST
	@Path("/user/post/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public UserV1 createJSONUser(@QueryParam("expand") final String expand, final UserV1 dataObject) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObject == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		final UserV1Factory factory = new UserV1Factory();
		return createEntity(User.class, dataObject, factory, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@POST
	@Path("/users/post/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public BaseRestCollectionV1<UserV1> createJSONUsers(@QueryParam("expand") final String expand, final BaseRestCollectionV1<UserV1> dataObjects) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObjects == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		if (dataObjects.getItems() == null)
			throw new InvalidParameterException("The dataObjects.getItems() parameter can not be null");

		final UserV1Factory factory = new UserV1Factory();
		return createEntities(User.class, dataObjects, factory, BaseRESTv1.USERS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	@Override
	@DELETE
	@Path("/user/delete/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public UserV1 deleteJSONUser(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (id == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		final User dbEntity = deleteEntity(User.class, id);
		return new UserV1Factory().createRESTEntityFromDBEntity(dbEntity, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@DELETE
	@Path("/users/delete/json/{ids}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<UserV1> deleteJSONUsers(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (ids == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		final Set<String> dbEntityIds = ids.getMatrixParameters().keySet();

		final UserV1Factory factory = new UserV1Factory();
		final List<User> dbEntities = deleteEntities(User.class, dbEntityIds, factory);
		return new RESTDataObjectCollectionFactory<UserV1, User>().create(factory, dbEntities, BaseRESTv1.USERS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	/* TRANSLATEDTOPICSTING FUNCTIONS */
	@Override
	@GET
	@Path("/translatedtopicstring/get/jsonp/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPTranslatedTopicString(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONTranslatedTopicString(id, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}
	
	@Override
	@GET
	@Path("/translatedtopicstrings/get/jsonp/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPTranslatedTopicStrings(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONTranslatedTopicStrings(expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}
	
	@PUT
	@Path("/translatedtopicstring/put/jsonp")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public String updateJSONPTranslatedTopicString(@QueryParam("expand") final String expand, final TranslatedTopicStringV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(updateJSONTranslatedTopicString(expand, dataObject)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}
	
	@PUT
	@Path("/translatedtopicstrings/put/jsonp")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public String updateJSONPTranslatedTopicStrings(@QueryParam("expand") final String expand, final BaseRestCollectionV1<TranslatedTopicStringV1> dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(updateJSONTranslatedTopicStrings(expand, dataObjects)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}
	
	@DELETE
	@Path("/translatedtopicstring/delete/jsonp/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String deleteJSONPTranslatedTopicString(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(deleteJSONTranslatedTopicString(id, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}
	
	@DELETE
	@Path("/translatedtopicstrings/delete/jsonp/{ids}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String deleteJSONPTranslatedTopicStrings(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(deleteJSONTranslatedTopicStrings(ids, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	
	@Override
	@GET
	@Path("/translatedtopicstring/get/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public TranslatedTopicStringV1 getJSONTranslatedTopicString(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (id == null)
			throw new InvalidParameterException("The id parameter can not be null");

		return getJSONResource(TranslatedTopicString.class, new TranslatedTopicStringV1Factory(), id, expand);
	}
	


	@Override
	@GET
	@Path("/translatedtopicstrings/get/json/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<TranslatedTopicStringV1> getJSONTranslatedTopicStrings(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		return getJSONResources(TranslatedTopicString.class, new TranslatedTopicStringV1Factory(), BaseRESTv1.TRANSLATEDTOPICSTRINGS_EXPANSION_NAME, expand);
	}

	@Override
	@PUT
	@Path("/translatedtopicstring/put/json")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public TranslatedTopicStringV1 updateJSONTranslatedTopicString(@QueryParam("expand") final String expand, final TranslatedTopicStringV1 dataObject) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObject == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		if (dataObject.getId() == null)
			throw new InvalidParameterException("The dataObject.getId() parameter can not be null");

		final TranslatedTopicStringV1Factory factory = new TranslatedTopicStringV1Factory();
		return updateEntity(TranslatedTopicString.class, dataObject, factory, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@PUT
	@Path("/translatedtopicstrings/put/json")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public BaseRestCollectionV1<TranslatedTopicStringV1> updateJSONTranslatedTopicStrings(@QueryParam("expand") final String expand, final BaseRestCollectionV1<TranslatedTopicStringV1> dataObjects) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObjects == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		if (dataObjects.getItems() == null)
			throw new InvalidParameterException("The dataObjects.getItems() parameter can not be null");

		final TranslatedTopicStringV1Factory factory = new TranslatedTopicStringV1Factory();
		return updateEntities(TranslatedTopicString.class, dataObjects, factory, BaseRESTv1.TRANSLATEDTOPICSTRINGS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	@Override
	@DELETE
	@Path("/translatedtopicstring/delete/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public TranslatedTopicStringV1 deleteJSONTranslatedTopicString(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (id == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		final TranslatedTopicString dbEntity = deleteEntity(TranslatedTopicString.class, id);
		return new TranslatedTopicStringV1Factory().createRESTEntityFromDBEntity(dbEntity, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@DELETE
	@Path("/translatedtopicstrings/delete/json/{ids}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<TranslatedTopicStringV1> deleteJSONTranslatedTopicStrings(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (ids == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		final Set<String> dbEntityIds = ids.getMatrixParameters().keySet();

		final TranslatedTopicStringV1Factory factory = new TranslatedTopicStringV1Factory();
		final List<TranslatedTopicString> dbEntities = deleteEntities(TranslatedTopicString.class, dbEntityIds, factory);
		return new RESTDataObjectCollectionFactory<TranslatedTopicStringV1, TranslatedTopicString>().create(factory, dbEntities, BaseRESTv1.TRANSLATEDTOPICSTRINGS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	/* TAG FUNCTIONS */
	/*		JSONP FUNCTIONS */	
	@Override
	@GET
	@Path("/tag/get/jsonp/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONTag(id, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@GET
	@Path("/tags/get/jsonp/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPTags(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONTags(expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@PUT
	@Path("/tag/put/jsonp")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public String updateJSONPTag(@QueryParam("expand") final String expand, final TagV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(updateJSONTag(expand, dataObject)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@PUT
	@Path("/tags/put/jsonp")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public String updateJSONPTags(@QueryParam("expand") final String expand, final BaseRestCollectionV1<TagV1> dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(updateJSONTags(expand, dataObjects)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@POST
	@Path("/tag/post/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public String createJSONPTag(@QueryParam("expand") final String expand, final TagV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(createJSONTag(expand, dataObject)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@POST
	@Path("/tags/post/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public String createJSONPTags(@QueryParam("expand") final String expand, final BaseRestCollectionV1<TagV1> dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(createJSONTags(expand, dataObjects)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@DELETE
	@Path("/tag/delete/jsonp/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String deleteJSONPTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(deleteJSONTag(id, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@DELETE
	@Path("/tags/delete/jsonp/{ids}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String deleteJSONPTags(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(deleteJSONTags(ids, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}
	
	/*		JSON FUNCTIONS */	
	@Override
	@GET
	@Path("/tag/get/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public TagV1 getJSONTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (id == null)
			throw new InvalidParameterException("The id parameter can not be null");

		return getJSONResource(Tag.class, new TagV1Factory(), id, expand);
	}

	@Override
	@GET
	@Path("/tags/get/json/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<TagV1> getJSONTags(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		return getJSONResources(Tag.class, new TagV1Factory(), BaseRESTv1.TAGS_EXPANSION_NAME, expand);
	}

	@Override
	@PUT
	@Path("/tag/put/json")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public TagV1 updateJSONTag(@QueryParam("expand") final String expand, final TagV1 dataObject) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObject == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		if (dataObject.getId() == null)
			throw new InvalidParameterException("The dataObject.getId() parameter can not be null");

		final TagV1Factory factory = new TagV1Factory();
		return updateEntity(Tag.class, dataObject, factory, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@PUT
	@Path("/tags/put/json")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public BaseRestCollectionV1<TagV1> updateJSONTags(@QueryParam("expand") final String expand, final BaseRestCollectionV1<TagV1> dataObjects) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObjects == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		if (dataObjects.getItems() == null)
			throw new InvalidParameterException("The dataObjects.getItems() parameter can not be null");

		final TagV1Factory factory = new TagV1Factory();
		return updateEntities(Tag.class, dataObjects, factory, BaseRESTv1.TAGS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	@POST
	@Path("/tag/post/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public TagV1 createJSONTag(@QueryParam("expand") final String expand, final TagV1 dataObject) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObject == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		final TagV1Factory factory = new TagV1Factory();
		return createEntity(Tag.class, dataObject, factory, this.getBaseUrl(), JSON_URL, expand);
	}

	@POST
	@Path("/tags/post/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public BaseRestCollectionV1<TagV1> createJSONTags(@QueryParam("expand") final String expand, final BaseRestCollectionV1<TagV1> dataObjects) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObjects == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		if (dataObjects.getItems() == null)
			throw new InvalidParameterException("The dataObjects.getItems() parameter can not be null");

		final TagV1Factory factory = new TagV1Factory();
		return createEntities(Tag.class, dataObjects, factory, BaseRESTv1.TAGS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	@DELETE
	@Path("/tag/delete/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public TagV1 deleteJSONTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (id == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		final Tag dbEntity = deleteEntity(Tag.class, id);
		return new TagV1Factory().createRESTEntityFromDBEntity(dbEntity, this.getBaseUrl(), JSON_URL, expand);
	}

	@DELETE
	@Path("/tags/delete/json/{ids}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<TagV1> deleteJSONTags(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (ids == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		final Set<String> dbEntityIds = ids.getMatrixParameters().keySet();

		final TagV1Factory factory = new TagV1Factory();
		final List<Tag> dbEntities = deleteEntities(Tag.class, dbEntityIds, factory);
		return new RESTDataObjectCollectionFactory<TagV1, Tag>().create(factory, dbEntities, BaseRESTv1.TAGS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	/* CATEGORY FUNCTIONS */
	/*		JSONP FUNCTIONS */
	@Override
	@GET
	@Path("/category/get/jsonp/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONCategory(id, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@GET
	@Path("/categories/get/jsonp/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPCategories(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONCategories(expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}


	@Override
	@PUT
	@Path("/category/put/jsonp")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public String updateJSONPCategory(@QueryParam("expand") final String expand, final CategoryV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(updateJSONCategory(expand, dataObject)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}


	@Override
	@PUT
	@Path("/categories/put/jsonp")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public String updateJSONPCategories(@QueryParam("expand") final String expand, final BaseRestCollectionV1<CategoryV1> dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(updateJSONCategories(expand, dataObjects)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@POST
	@Path("/category/post/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public String createJSONPCategory(@QueryParam("expand") final String expand, final CategoryV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(createJSONCategory(expand, dataObject)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@POST
	@Path("/categories/post/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public String createJSONPCategories(@QueryParam("expand") final String expand, final BaseRestCollectionV1<CategoryV1> dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(createJSONCategories(expand, dataObjects)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@DELETE
	@Path("/category/delete/jsonp/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String deleteJSONPCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(deleteJSONCategory(id, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@DELETE
	@Path("/categories/delete/jsonp/{ids}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String deleteJSONPCategories(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(deleteJSONCategories(ids, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}
	
	/*		JSON FUNCTIONS */	
	@Override
	@GET
	@Path("/categories/get/json/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<CategoryV1> getJSONCategories(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		return getJSONResources(Category.class, new CategoryV1Factory(), CATEGORIES_EXPANSION_NAME, expand);
	}

	@Override
	@GET
	@Path("/category/get/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public CategoryV1 getJSONCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (id == null)
			throw new InvalidParameterException("The id parameter can not be null");

		return getJSONResource(Category.class, new CategoryV1Factory(), id, expand);
	}

	@Override
	@PUT
	@Path("/category/put/json")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public CategoryV1 updateJSONCategory(@QueryParam("expand") final String expand, final CategoryV1 dataObject) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObject == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		if (dataObject.getId() == null)
			throw new InvalidParameterException("The dataObject.getId() parameter can not be null");

		final CategoryV1Factory factory = new CategoryV1Factory();
		return updateEntity(Category.class, dataObject, factory, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@PUT
	@Path("/categories/put/json")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public BaseRestCollectionV1<CategoryV1> updateJSONCategories(@QueryParam("expand") final String expand, final BaseRestCollectionV1<CategoryV1> dataObjects) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObjects == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		if (dataObjects.getItems() == null)
			throw new InvalidParameterException("The dataObjects.getItems() parameter can not be null");

		final CategoryV1Factory factory = new CategoryV1Factory();
		return updateEntities(Category.class, dataObjects, factory, BaseRESTv1.CATEGORIES_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	@Override
	@POST
	@Path("/category/post/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public CategoryV1 createJSONCategory(@QueryParam("expand") final String expand, final CategoryV1 dataObject) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObject == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		final CategoryV1Factory factory = new CategoryV1Factory();
		return createEntity(Category.class, dataObject, factory, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@POST
	@Path("/categories/post/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public BaseRestCollectionV1<CategoryV1> createJSONCategories(@QueryParam("expand") final String expand, final BaseRestCollectionV1<CategoryV1> dataObjects) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObjects == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		if (dataObjects.getItems() == null)
			throw new InvalidParameterException("The dataObjects.getItems() parameter can not be null");

		final CategoryV1Factory factory = new CategoryV1Factory();
		return createEntities(Category.class, dataObjects, factory, BaseRESTv1.CATEGORIES_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	@Override
	@DELETE
	@Path("/category/delete/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public CategoryV1 deleteJSONCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (id == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		final Category dbEntity = deleteEntity(Category.class, id);
		return new CategoryV1Factory().createRESTEntityFromDBEntity(dbEntity, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@DELETE
	@Path("/categories/delete/json/{ids}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<CategoryV1> deleteJSONCategories(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (ids == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		final Set<String> dbEntityIds = ids.getMatrixParameters().keySet();

		final CategoryV1Factory factory = new CategoryV1Factory();
		final List<Category> dbEntities = deleteEntities(Category.class, dbEntityIds, factory);
		return new RESTDataObjectCollectionFactory<CategoryV1, Category>().create(factory, dbEntities, BaseRESTv1.CATEGORIES_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	/* IMAGE FUNCTIONS */
	/*		JSONP FUNCTIONS */	
	@Override
	@GET
	@Path("/image/get/jsonp/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPImage(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONImage(id, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@GET
	@Path("/images/get/jsonp/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPImages(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONImages(expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@PUT
	@Path("/image/put/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public String updateJSONPImage(@QueryParam("expand") final String expand, final ImageV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(updateJSONImage(expand, dataObject)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@PUT
	@Path("/images/put/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public String updateJSONPImages(@QueryParam("expand") final String expand, final BaseRestCollectionV1<ImageV1> dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(updateJSONImages(expand, dataObjects)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@POST
	@Path("/image/post/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public String createJSONPImage(@QueryParam("expand") final String expand, final ImageV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(createJSONImage(expand, dataObject)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@POST
	@Path("/images/post/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public String createJSONPImages(@QueryParam("expand") final String expand, final BaseRestCollectionV1<ImageV1> dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(createJSONImages(expand, dataObjects)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@DELETE
	@Path("/image/delete/jsonp/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String deleteJSONPImage(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(deleteJSONImage(id, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@DELETE
	@Path("/images/delete/jsonp/{ids}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String deleteJSONPImages(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(deleteJSONImages(ids, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}
	
	/*		JSON FUNCTIONS */	
	@Override
	@GET
	@Path("/image/get/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public ImageV1 getJSONImage(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (id == null)
			throw new InvalidParameterException("The id parameter can not be null");

		return getJSONResource(ImageFile.class, new ImageV1Factory(), id, expand);
	}

	@GET
	@Path("/images/get/json/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<ImageV1> getJSONImages(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		/*
		 * Construct a collection with the given expansion name. The user will
		 * have to expand the collection to get the details of the items in it.
		 */
		return getJSONResources(ImageFile.class, new ImageV1Factory(), IMAGES_EXPANSION_NAME, expand);
	}

	@PUT
	@Path("/image/put/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public ImageV1 updateJSONImage(@QueryParam("expand") final String expand, final ImageV1 dataObject) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObject == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		if (dataObject.getId() == null)
			throw new InvalidParameterException("The dataObject.getId() parameter can not be null");

		final ImageV1Factory factory = new ImageV1Factory();
		return updateEntity(ImageFile.class, dataObject, factory, this.getBaseUrl(), JSON_URL, expand);
	}

	@PUT
	@Path("/images/put/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public BaseRestCollectionV1<ImageV1> updateJSONImages(@QueryParam("expand") final String expand, final BaseRestCollectionV1<ImageV1> dataObjects) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObjects == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		if (dataObjects.getItems() == null)
			throw new InvalidParameterException("The dataObjects.getItems() parameter can not be null");

		final ImageV1Factory factory = new ImageV1Factory();
		return updateEntities(ImageFile.class, dataObjects, factory, BaseRESTv1.IMAGES_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	@POST
	@Path("/image/post/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public ImageV1 createJSONImage(@QueryParam("expand") final String expand, final ImageV1 dataObject) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObject == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		final ImageV1Factory factory = new ImageV1Factory();
		return createEntity(ImageFile.class, dataObject, factory, this.getBaseUrl(), JSON_URL, expand);
	}

	@POST
	@Path("/images/post/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public BaseRestCollectionV1<ImageV1> createJSONImages(@QueryParam("expand") final String expand, final BaseRestCollectionV1<ImageV1> dataObjects) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObjects == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		if (dataObjects.getItems() == null)
			throw new InvalidParameterException("The dataObjects.getItems() parameter can not be null");

		final ImageV1Factory factory = new ImageV1Factory();
		return createEntities(ImageFile.class, dataObjects, factory, BaseRESTv1.IMAGES_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	@DELETE
	@Path("/image/delete/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public ImageV1 deleteJSONImage(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (id == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		final ImageFile dbEntity = deleteEntity(ImageFile.class, id);
		return new ImageV1Factory().createRESTEntityFromDBEntity(dbEntity, this.getBaseUrl(), JSON_URL, expand);
	}

	@DELETE
	@Path("/images/delete/json/{ids}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<ImageV1> deleteJSONImages(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (ids == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		final Set<String> dbEntityIds = ids.getMatrixParameters().keySet();

		final ImageV1Factory factory = new ImageV1Factory();
		final List<ImageFile> dbEntities = deleteEntities(ImageFile.class, dbEntityIds, factory);
		return new RESTDataObjectCollectionFactory<ImageV1, ImageFile>().create(factory, dbEntities, BaseRESTv1.IMAGES_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	/* TOPIC FUNCTIONS */
	/*		JSONP FUNCTIONS */	
	@Override
	@GET
	@Path("/topics/get/jsonp/{query}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPTopicsWithQuery(@PathParam("query") PathSegment query, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONTopicsFromQuery(query.getMatrixParameters(), new TopicFilterQueryBuilder(), new TopicV1Factory(), TOPICS_EXPANSION_NAME, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}
	
	@Override
	@GET
	@Path("/topics/get/jsonp/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPTopics(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONTopics(expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@GET
	@Path("/topic/get/jsonp/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String getJSONPTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(getJSONTopic(id, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@PUT
	@Path("/topic/put/jsonp")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public String updateJSONPTopic(@QueryParam("expand") final String expand, final TopicV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(updateJSONTopic(expand, dataObject)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@PUT
	@Path("/topics/put/jsonp")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public String updateJSONPTopics(@QueryParam("expand") final String expand, final BaseRestCollectionV1<TopicV1> dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(updateJSONTopics(expand, dataObjects)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@POST
	@Path("/topic/post/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public String createJSONPTopic(@QueryParam("expand") final String expand, final TopicV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(createJSONTopic(expand, dataObject)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@POST
	@Path("/topics/post/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public String createJSONPTopics(@QueryParam("expand") final String expand, final BaseRestCollectionV1<TopicV1> dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(createJSONTopics(expand, dataObjects)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@DELETE
	@Path("/topic/delete/jsonp/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String deleteJSONPTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(deleteJSONTopic(id, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}

	@Override
	@DELETE
	@Path("/topics/delete/jsonp/{ids}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public String deleteJSONPTopics(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException
	{
		if (callback == null)
			throw new InvalidParameterException("The callback parameter can not be null");
		
		try
		{
			return wrapJsonInPadding(callback, convertObjectToJSON(deleteJSONTopics(ids, expand)));
		}
		catch (final Exception ex)
		{
			throw new InternalProcessingException("Could not marshall return value into JSON");
		}
	}
	
	/*		JSON FUNCTIONS */	
	@Override
	@GET
	@Path("/topics/get/json/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<TopicV1> getJSONTopics(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		return getJSONResources(Topic.class, new TopicV1Factory(), TOPICS_EXPANSION_NAME, expand);
	}

	@Override
	@GET
	@Path("/topics/get/json/{query}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<TopicV1> getJSONTopicsWithQuery(@PathParam("query") PathSegment query, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		return getJSONTopicsFromQuery(query.getMatrixParameters(), new TopicFilterQueryBuilder(), new TopicV1Factory(), TOPICS_EXPANSION_NAME, expand);
	}

	@Override
	@GET
	@Path("/topics/get/atom/{query}")
	@Produces(MediaType.APPLICATION_ATOM_XML)
	@Consumes({ "*" })
	public Feed getATOMTopicsWithQuery(@PathParam("query") PathSegment query, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		final BaseRestCollectionV1<TopicV1> topics = getJSONTopicsFromQuery(query.getMatrixParameters(), new TopicFilterQueryBuilder(), new TopicV1Factory(), TOPICS_EXPANSION_NAME, expand);
		return this.convertTopicsIntoFeed(topics, "Topic Query (" + topics.getSize() + " items)");
	}

	@Override
	@GET
	@Path("/topics/get/xml/all")
	@Produces(MediaType.TEXT_XML)
	@Consumes({ "*" })
	public BaseRestCollectionV1<TopicV1> getXMLTopics(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		return getXMLResources(Topic.class, new TopicV1Factory(), TOPICS_EXPANSION_NAME, expand);
	}

	@Override
	@GET
	@Path("/topic/get/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public TopicV1 getJSONTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		assert id != null : "The id parameter can not be null";

		return getJSONResource(Topic.class, new TopicV1Factory(), id, expand);
	}
	
	@Override
	@GET
	@Path("/topic/get/json/{id}/r/{rev}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public TopicV1 getJSONTopicRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		assert id != null : "The id parameter can not be null";

		return getJSONResource(Topic.class, new TopicV1Factory(), id, revision, expand);
	}

	@Override
	@GET
	@Path("/topic/get/xml/{id}")
	@Produces(MediaType.TEXT_XML)
	@Consumes({ "*" })
	public TopicV1 getXMLTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		assert id != null : "The id parameter can not be null";

		return getXMLResource(Topic.class, new TopicV1Factory(), id, expand);
	}
	
	@Override
	@GET
	@Path("/topic/get/xml/{id}/r/{rev}")
	@Produces(MediaType.TEXT_XML)
	@Consumes({ "*" })
	public TopicV1 getXMLTopicRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		assert id != null : "The id parameter can not be null";

		return getXMLResource(Topic.class, new TopicV1Factory(), id, revision, expand);
	}

	@Override
	@GET
	@Path("/topic/get/xml/{id}/xml")
	@Produces(MediaType.TEXT_XML + ";charset=\"UTF-8\"")
	@Consumes({ "*" })
	public String getXMLTopicXML(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		assert id != null : "The id parameter can not be null";

		return getXMLResource(Topic.class, new TopicV1Factory(), id, expand).getXml();
	}

	@Override
	@GET
	@Path("/topic/get/xml/{id}/xmlContainedIn")
	@Produces(MediaType.TEXT_XML)
	@Consumes({ "*" })
	public String getXMLTopicXMLContained(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("container") final String containerName) throws InvalidParameterException, InternalProcessingException
	{
		assert id != null : "The id parameter can not be null";
		assert containerName != null : "The containerName parameter can not be null";

		return getXMLResource(Topic.class, new TopicV1Factory(), id, expand).getXMLWithNewContainer(containerName);
	}

	@Override
	@GET
	@Path("/topic/get/xml/{id}/xmlNoContainer")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes({ "*" })
	public String getXMLTopicXMLNoContainer(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("includeTitle") final Boolean includeTitle) throws InvalidParameterException, InternalProcessingException
	{
		assert id != null : "The id parameter can not be null";

		final String retValue = getXMLResource(Topic.class, new TopicV1Factory(), id, expand).getXMLWithNoContainer(includeTitle);
		return retValue;
	}
	
	@Override
	@GET
	@Path("/topic/get/html/{id}/html")
	@Produces(MediaType.APPLICATION_XHTML_XML)
	@Consumes({ "*" })
	public String getHTMLTopicHTML(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		assert id != null : "The id parameter can not be null";

		return getXMLResource(Topic.class, new TopicV1Factory(), id, expand).getHtml();
	}
	
	@Override
	@GET
	@Path("/topic/get/html/{id}/r/{rev}/html")
	@Produces(MediaType.APPLICATION_XHTML_XML)
	@Consumes({ "*" })
	public String getHTMLTopicRevisionHTML(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		assert id != null : "The id parameter can not be null";

		return getXMLResource(Topic.class, new TopicV1Factory(), id, revision, expand).getHtml();
	}


	@Override
	@PUT
	@Path("/topic/put/json")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public TopicV1 updateJSONTopic(@QueryParam("expand") final String expand, final TopicV1 dataObject) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObject == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		if (dataObject.getId() == null)
			throw new InvalidParameterException("The dataObject.getId() parameter can not be null");

		final TopicV1Factory factory = new TopicV1Factory();
		return updateEntity(Topic.class, dataObject, factory, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@PUT
	@Path("/topics/put/json")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public BaseRestCollectionV1<TopicV1> updateJSONTopics(@QueryParam("expand") final String expand, final BaseRestCollectionV1<TopicV1> dataObjects) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObjects == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		if (dataObjects.getItems() == null)
			throw new InvalidParameterException("The dataObjects.getItems() parameter can not be null");

		final TopicV1Factory factory = new TopicV1Factory();
		return updateEntities(Topic.class, dataObjects, factory, BaseRESTv1.TOPICS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	@Override
	@POST
	@Path("/topic/post/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public TopicV1 createJSONTopic(@QueryParam("expand") final String expand, final TopicV1 dataObject) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObject == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		final TopicV1Factory factory = new TopicV1Factory();
		return createEntity(Topic.class, dataObject, factory, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@POST
	@Path("/topics/post/json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.APPLICATION_JSON })
	public BaseRestCollectionV1<TopicV1> createJSONTopics(@QueryParam("expand") final String expand, final BaseRestCollectionV1<TopicV1> dataObjects) throws InvalidParameterException, InternalProcessingException
	{
		if (dataObjects == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		if (dataObjects.getItems() == null)
			throw new InvalidParameterException("The dataObjects.getItems() parameter can not be null");

		final TopicV1Factory factory = new TopicV1Factory();
		return createEntities(Topic.class, dataObjects, factory, BaseRESTv1.TOPICS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}

	@Override
	@DELETE
	@Path("/topic/delete/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public TopicV1 deleteJSONTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (id == null)
			throw new InvalidParameterException("The dataObject parameter can not be null");

		final Topic dbEntity = deleteEntity(Topic.class, id);
		return new TopicV1Factory().createRESTEntityFromDBEntity(dbEntity, this.getBaseUrl(), JSON_URL, expand);
	}

	@Override
	@DELETE
	@Path("/topics/delete/json/{ids}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ "*" })
	public BaseRestCollectionV1<TopicV1> deleteJSONTopics(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException
	{
		if (ids == null)
			throw new InvalidParameterException("The dataObjects parameter can not be null");

		final Set<String> dbEntityIds = ids.getMatrixParameters().keySet();

		final TopicV1Factory factory = new TopicV1Factory();
		final List<Topic> dbEntities = deleteEntities(Topic.class, dbEntityIds, factory);
		return new RESTDataObjectCollectionFactory<TopicV1, Topic>().create(factory, dbEntities, BaseRESTv1.TOPICS_EXPANSION_NAME, JSON_URL, expand, getBaseUrl());
	}
}
