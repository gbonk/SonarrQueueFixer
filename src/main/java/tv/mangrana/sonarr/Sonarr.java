package tv.mangrana.sonarr;

import tv.mangrana.config.ConfigFileLoader;
import tv.mangrana.sonarr.api.client.gateway.SonarrApiGateway;

public class Sonarr {
    private static SonarrApiGateway service;

    public static void initService(ConfigFileLoader configFileLoader) {
        service = new SonarrApiGateway(configFileLoader);
    }

    public static SonarrApiGateway api() {
        return service;
    }
}
