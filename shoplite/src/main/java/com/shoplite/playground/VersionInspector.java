package com.shoplite.playground;

import org.apache.catalina.util.ServerInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.SpringVersion;
import org.springframework.stereotype.Component;

@Component
public class VersionInspector implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(VersionInspector.class);

    @Override
    public void run(String... args) throws Exception {
        log.info("[BOM] Spring Boot       : {}", SpringBootVersion.getVersion());
        log.info("[BOM] Spring Framework  : {}", SpringVersion.getVersion());
        log.info("[BOM] Embedded Tomcat   : {}", ServerInfo.getServerNumber());
        log.info("[BOM] commons-lang3     : {}", versionOf(StringUtils.class));
    }

    private String versionOf(Class<?> type) {
        String version = type.getPackage().getImplementationVersion();
        return version != null ? version : "n/a (no version in jar manifest)";
    }
}
