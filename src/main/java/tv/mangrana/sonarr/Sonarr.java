package tv.mangrana.sonarr;

import tv.mangrana.config.ConfigLoader;
import tv.mangrana.sonarr.api.client.gateway.SonarrApiGateway;

public class Sonarr {
    private static SonarrApiGateway service;

    public static void initService(ConfigLoader configLoader) {
        service = new SonarrApiGateway(configLoader);
    }

    public static SonarrApiGateway api() {
        return service;
    }
}
