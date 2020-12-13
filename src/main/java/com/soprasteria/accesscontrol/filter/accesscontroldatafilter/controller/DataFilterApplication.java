package com.soprasteria.accesscontrol.filter.accesscontroldatafilter.controller;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/datashield") //swagger will pick the base context as datashield instead of /
public class DataFilterApplication extends Application {
}
