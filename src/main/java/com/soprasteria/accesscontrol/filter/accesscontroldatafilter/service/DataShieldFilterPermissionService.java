package com.soprasteria.accesscontrol.filter.accesscontroldatafilter.service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;

import com.soprasteria.ignis.accesscontrol.domain.datashield.DatashieldPermission;

@Stateless
public class DataShieldFilterPermissionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataShieldFilterPermissionService.class);
	@Inject
	@ConfigurationValue("thorntail.datasheildadmin_service")
	private String datashieldadminServiceUrl;

	@Retry(maxRetries = 3, maxDuration = 1000, retryOn = { WebApplicationException.class })
	public String getPermissionsByApiNameAndRole(String apiName, String subscriptionId, String uri) {
		final CountDownLatch latch = new CountDownLatch(1);
		final StringBuilder token = new StringBuilder();
		final AtomicReference<Throwable> throwable = new AtomicReference<>();
		LOGGER.info("datasheild admin service  Url {}" , datashieldadminServiceUrl);
		ClientBuilder.newClient().target(datashieldadminServiceUrl).path("/name/").path("{apiName}").path("/subscriptionId/").path("{subscriptionId}")
				.resolveTemplate("apiName", apiName).resolveTemplate("subscriptionId", subscriptionId)
				.queryParam("uri", uri).request().rx().get(DatashieldPermission.class)
				.whenCompleteAsync((r, t) -> {
					if (t != null) {
						throwable.set(t);
					} else {
						token.append(r.getPermissionData());
					}
					latch.countDown();
				});

		try {
			latch.await();
		} catch (Exception ex) {
			throw new WebApplicationException(ex, 500);
		}

		Throwable t = throwable.get();
		if (t != null) {
			throw new WebApplicationException("Failure in downstream service", t, 500);
		}
		return token.toString();
	}

}
