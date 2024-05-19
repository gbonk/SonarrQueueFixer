package tv.mangrana.worker;

import tv.mangrana.config.ConfigFileLoader;
import tv.mangrana.exception.IncorrectWorkingReferencesException;
import tv.mangrana.sonarr.api.client.gateway.SonarrApiGateway;

public class MainWorker {
    private final SonarrApiGateway sonarrApiGateway;

    public MainWorker() throws IncorrectWorkingReferencesException {
        ConfigFileLoader configFileLoader = new ConfigFileLoader();
        sonarrApiGateway = new SonarrApiGateway(configFileLoader);
    }

    public static void main(String[] args) throws IncorrectWorkingReferencesException {
        var worker = new MainWorker();
        var queue = worker.sonarrApiGateway.getQueue();
        System.out.println(queue);
    }
}
