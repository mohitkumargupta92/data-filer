package com.soprasteria.accesscontrol.filter.accesscontroldatafilter.healthchecks;

import java.io.File;

import javax.ejb.Stateless;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

@Stateless
@Liveness
public class DiskspaceHealthCheck implements HealthCheck {

	@Override
	public HealthCheckResponse call() {
		File fileCDrive = new File("/");
		return HealthCheckResponse.builder().name("diskSpace")
				.withData("totalSpace", Math.round(fileCDrive.getTotalSpace() / Math.pow(1024, 2)) + " MB")
				.withData("freeSpace", Math.round(fileCDrive.getFreeSpace() / Math.pow(1024, 2)) + " MB")
				.state(fileCDrive.getFreeSpace() > 100 ? true : false).build();
	}

}
