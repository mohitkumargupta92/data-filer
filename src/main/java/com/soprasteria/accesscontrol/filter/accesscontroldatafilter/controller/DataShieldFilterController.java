package com.soprasteria.accesscontrol.filter.accesscontroldatafilter.controller;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soprasteria.accesscontrol.filter.accesscontroldatafilter.service.DataShieldFilterDataService;
import com.soprasteria.accesscontrol.filter.accesscontroldatafilter.service.DataShieldFilterPermissionService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;

@Path("/")
@SwaggerDefinition(
        info = @Info(description = "Crud Operations for Datashield Filter API", version = "1.0.0", title = "Datashield Filter", termsOfService = "http://igntapp28x.ign.aws.ext.ssg/terms.html",
                contact = @Contact(name = "Access Control Platform", email = "accesscontrolplatform.org", url = "http://igntapp28x.ign.aws.ext.ssg/"),
                license = @License(name = "Wildfly", url = "Apache License, Version 2.0")),
        consumes = { "application/json" }, produces = { "application/json" }, schemes = { SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS })
@Api(value = "/datashield", tags = "datashield")
public class DataShieldFilterController {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataShieldFilterController.class);
	@Inject
	private DataShieldFilterPermissionService dataShieldFilterPermissionService;
	
	@Inject
	private DataShieldFilterDataService dataShieldFilterDataService;

	@Timed(name = "applyFilterTime", description = "Monitor the time applyFilter Method takes", unit = MetricUnits.MILLISECONDS, absolute = true)
	@POST
	@Path("/filter")
	@ApiOperation(value = "Apply response filter", notes = "Return the filtered API response to the Access Control Platform.", response = Response.class)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response applyFilter(String filterData, @QueryParam("api") String api,
			@QueryParam("subscriptionId") String subscriptionId) throws IOException {
		Instant start = Instant.now();
		String context = api.split("/").length > 0 ? api.split("/")[1] : api;
		String uri = api.replace("/" + context, "");
		LOGGER.info("call datasheildadmin to fetch permissions for context {}, uri {}, subscriberid {} and data {} ", context, uri, subscriptionId, filterData);
		String filterPermissionData = dataShieldFilterPermissionService.getPermissionsByApiNameAndRole(context,
		        subscriptionId, uri);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode filteredDataNode = dataShieldFilterDataService.removeFilterProperties(filterData, filterPermissionData);
		LOGGER.info("filtered  data {} ", mapper.writeValueAsString(filteredDataNode));
		Instant end = Instant.now();
		Duration res = Duration.between(start, end);
		LOGGER.info("total time taken by datasheild filter to filter data {} ", res.toMillis());
		return Response.status(Response.Status.OK).entity(mapper.writeValueAsString(filteredDataNode)).build();
	}

}
