module com.udacity.catpoint.SecurityService {
    requires com.udacity.catpoint.ImageService;
    requires com.miglayout.core;
    requires com.miglayout.swing;
    requires com.google.gson;
    requires java.desktop;
    requires java.prefs;
    requires com.google.common;
    opens com.udacity.catpoint.SecurityService.application;
    opens com.udacity.catpoint.SecurityService.data;
    opens com.udacity.catpoint.SecurityService.service;

}