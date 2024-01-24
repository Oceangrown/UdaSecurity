module com.udacity.catpoint.ImageService {
    exports com.udacity.catpoint.ImageService to com.udacity.catpoint.SecurityService;
    requires org.slf4j;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.services.rekognition;
    requires java.desktop;

}