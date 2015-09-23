package org.molgenis.rdconnect;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class IdCardBiobankServiceImpl implements IdCardBiobankService
{
	public final static String REGBBS_URL = "http://catalogue.rd-connect.eu/api/jsonws/BiBBoxCommonServices-portlet.logapi/regbbs";
	public final static String REGBB_URL_PREFIX = "http://catalogue.rd-connect.eu/api/jsonws/BiBBoxCommonServices-portlet.logapi/regbb/organization-id/";
	public final static String REGBBS_ORGANIZATIONID = "OrganizationID";

	private final DataService dataService;

	@Autowired
	public IdCardBiobankServiceImpl(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	public JsonObject getResourceAsJsonObject(String url)
	{
		try
		{
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(url);
			request.addHeader("content-type", "application/json");
			HttpResponse result = httpClient.execute(request);
			String toExtract = EntityUtils.toString(result.getEntity(), "UTF-8");
			JsonParser parser = new JsonParser();
			return parser.parse(toExtract).getAsJsonObject();
		}
		catch (IOException ex)
		{
			throw new MolgenisDataException("Hackathon error message");
		}
	}

	public JsonArray getResourceAsJsonArray(String url)
	{
		try
		{
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(url);
			request.addHeader("content-type", "application/json");
			HttpResponse result = httpClient.execute(request);
			String toExtract = EntityUtils.toString(result.getEntity(), "UTF-8");
			JsonParser parser = new JsonParser();
			return parser.parse(toExtract).getAsJsonArray();
		}
		catch (IOException ex)
		{
			throw new MolgenisDataException("Hackathon error message");
		}
	}

	public Set<String> getIdCardBiobanksOrgnizationIds()
	{
		JsonArray resource = this.getResourceAsJsonArray(REGBBS_URL);
		return StreamSupport.stream(resource.spliterator(), false)
				.map(j -> j.getAsJsonObject().get(REGBBS_ORGANIZATIONID).getAsString()).collect(Collectors.toSet());
	}

	@Override
	public Iterable<Entity> getIdCardBiobanks(Iterable<String> ids)
	{
		return StreamSupport.stream(ids.spliterator(), false).map(e -> this.getIdCardBiobank(e))
				.collect(Collectors.toList());
	}

	@Override
	public Iterable<Entity> getIdCardBiobanks()
	{
		return this.getIdCardBiobanks(this.getIdCardBiobanksOrgnizationIds());
	}

	@Override
	public Entity getIdCardBiobank(String id)
	{
		JsonObject root = this.getResourceAsJsonObject(REGBB_URL_PREFIX + id);
		EntityMetaData emd = dataService.getEntityMetaData("rdconnect_regbb");

		MapEntity regbbMapEntity = new MapEntity(emd);

		regbbMapEntity.set("OrganizationID", root.getAsJsonPrimitive("OrganizationID").getAsInt());
		regbbMapEntity.set("type", root.getAsJsonPrimitive("type").getAsString());
		regbbMapEntity.set("lso_listed_in", this.paseToListMapEntity("rdconnect_lso_listed_in", "also_liste_in", root.getAsJsonArray("lso listed in")));
		regbbMapEntity.set("url", this.paseToListMapEntity("rdconnect_url", "url", root.getAsJsonArray("url")));
		
		/**
		 * "main contact" entity
		 */
		regbbMapEntity.set("title", root.getAsJsonObject("main contact").getAsJsonPrimitive("title").getAsString());
		regbbMapEntity.set("first_name",
				root.getAsJsonObject("main contact").getAsJsonPrimitive("first name").getAsString());
		regbbMapEntity.set("email", root.getAsJsonObject("main contact").getAsJsonPrimitive("email").getAsString());
		regbbMapEntity.set("last_name",
				root.getAsJsonObject("main contact").getAsJsonPrimitive("last name").getAsString());


		// Example format "Mon Jan 05 18:02:13 GMT 2015"
		final String datetimePattern = "EEE MMM dd HH:mm:ss z yyyy";
		SimpleDateFormat datetimeFormat = new SimpleDateFormat(datetimePattern);

		try
		{
			regbbMapEntity.set("last_activities",
					datetimeFormat.parseObject(root.getAsJsonPrimitive("last activities").getAsString()));
		}
		catch (ParseException e)
		{
			throw new MolgenisDataException("failed to parse the 'last activities' property", e);
		}

		try
		{
			regbbMapEntity.set("date_of_inclusion",
					datetimeFormat.parseObject(root.getAsJsonPrimitive("date of inclusion").getAsString()));
		}
		catch (ParseException e)
		{
			throw new MolgenisDataException("failed to parse the 'last activities' property", e);
		}

		regbbMapEntity.set("email", root.getAsJsonObject("address").getAsJsonPrimitive("street2").getAsString());
		regbbMapEntity.set("name_of_host_institution",
				root.getAsJsonObject("address").getAsJsonPrimitive("name of host institution")
				.getAsString());
		regbbMapEntity.set("zip", root.getAsJsonObject("address").getAsJsonPrimitive("zip").getAsString());
		regbbMapEntity.set("street1", root.getAsJsonObject("address").getAsJsonPrimitive("street1").getAsString());
		regbbMapEntity.set("country", root.getAsJsonObject("country").getAsJsonPrimitive("country").getAsString());
		regbbMapEntity.set("city", root.getAsJsonObject("city").getAsJsonPrimitive("city").getAsString());

		regbbMapEntity.set("ID", root.getAsJsonPrimitive("ID").getAsString());
		regbbMapEntity.set("type_of_host_institution", root.getAsJsonPrimitive("type of host institution")
				.getAsString());
		regbbMapEntity.set("target_population", root.getAsJsonPrimitive("target population").getAsString());

		return regbbMapEntity;
	}

	private List<MapEntity> paseToListMapEntity(String entityName, String attributeName, JsonArray jsonArray)
	{
		EntityMetaData emd = dataService.getEntityMetaData(entityName);
		List<MapEntity> mapEntityList = new ArrayList<MapEntity>();
		jsonArray.spliterator().forEachRemaining(
				e -> mapEntityList.add(this.paseToMapEntity(emd, attributeName, e.getAsString())));
		return mapEntityList;
	}

	private MapEntity paseToMapEntity(EntityMetaData entityMetaData, String attributeName, String value)
	{
		MapEntity entity = new MapEntity(entityMetaData);
		entity.set(attributeName, value);
		return entity;
	}
}
