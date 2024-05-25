package tv.mangrana.worker;

import tv.mangrana.config.ConfigLoader;
import tv.mangrana.exception.IncorrectWorkingReferencesException;
import tv.mangrana.sonarr.Sonarr;
import tv.mangrana.sonarr.api.client.gateway.SonarrApiGateway;

public class MainWorker {

    private final QueueFixer queueFixer;

    public static void main(String[] args) throws IncorrectWorkingReferencesException {
        SonarrApiGateway sonarrApiGateway = configureSonarApiGateway();
        var worker = new MainWorker(sonarrApiGateway);
        worker.work();
    }

    private static SonarrApiGateway configureSonarApiGateway() {
        var configLoader = ConfigLoader.getLoader();
        Sonarr.initService(configLoader);
        return Sonarr.api();
    }

    MainWorker(SonarrApiGateway sonarGateway) throws IncorrectWorkingReferencesException {
        if (ConfigLoader.isTestMode())
            System.out.println("ATTENTION! TEST MODE ENABLED. No folders will be created nor files copied.");

        queueFixer = new QueueFixer(sonarGateway);
    }

    void work() {
        queueFixer.fix();
    }
}
