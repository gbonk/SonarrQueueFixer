package tv.mangrana.worker;

import tv.mangrana.config.ConfigLoader;
import tv.mangrana.sonarr.Sonarr;
import tv.mangrana.sonarr.api.client.gateway.SonarrApiGateway;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SonarrDeferredRefresher {
    private static final ScheduledExecutorService deferredRefresher = new ScheduledThreadPoolExecutor(1);
    private final SonarrApiGateway sonarrApiGateway = Sonarr.api();
    public void refreshSeries(Set<Integer> seriesToRefresh) {
        for (var serieId : seriesToRefresh) {
            Runnable refreshTask = () -> sonarrApiGateway.refreshSerie(serieId);
            if (!ConfigLoader.isTestMode())
                deferredRefresher.schedule(refreshTask, 5, TimeUnit.SECONDS);
        }
    }
}
