package com.soprasteria.accesscontrol.filter.accesscontroldatafilter.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import io.restassured.RestAssured;

@RunWith(Arquillian.class)
public class DataShieldFilterControllerTest {
	private static final String BASE_PATH_URL = "/datashield";
	private static final String FILTER_URL = "/filter";
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(8089);
	
	@Before
	public void setUp() throws Exception {
		RestAssured.baseURI = "http://localhost";
		RestAssured.basePath = BASE_PATH_URL;
		RestAssured.port = 9001;
	}

	public void setupStub() throws IOException {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("datashield-admin-response.json");
		byte[] data = new byte[inputStream.available()];
		inputStream.read(data);
		String dataString = new String(data);
		
		stubFor(get(urlEqualTo("/accesscontroldatashield/apiadmin/apidetails/name/Arquillian/subscriptionId/4b3ccbd1?uri="))
				.atPriority(1)
	            .willReturn(aResponse()
	                .withStatus(200)
	                .withHeader("Content-Type", MediaType.APPLICATION_JSON)
	                .withBody(dataString)));
	}

	@Test
	@InSequence(1)
	public void applyFilterTest() throws IOException {
		setupStub();
		
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("filter_request_body.json");
		byte[] data = new byte[inputStream.available()];
		inputStream.read(data);
		String dataString = new String(data);
		
		given()
				.contentType(MediaType.APPLICATION_JSON)
				.body(dataString)
				.queryParams("api", "/Arquillian", "subscriptionId", "4b3ccbd1")
				.when()
				.post(FILTER_URL)
				.then()
				.statusCode(HttpStatus.SC_OK);
	}
}
