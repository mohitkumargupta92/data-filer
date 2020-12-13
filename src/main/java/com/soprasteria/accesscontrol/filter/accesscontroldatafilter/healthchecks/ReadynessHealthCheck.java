package com.soprasteria.accesscontrol.filter.accesscontroldatafilter.healthchecks;

import javax.ejb.Stateless;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@Stateless
@Readiness
public class ReadynessHealthCheck implements HealthCheck {

	@Override
	public HealthCheckResponse call() {
		return HealthCheckResponse.builder().name("Readyness-Check").up().build();
	}
}
