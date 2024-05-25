package tv.mangrana.worker;

import tv.mangrana.config.ConfigLoader;
import tv.mangrana.sonarr.Sonarr;
import tv.mangrana.sonarr.api.client.gateway.SonarrApiGateway;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SonarrDeferredRefresher {
    private final SonarrApiGateway sonarrApiGateway;
    private final ScheduledExecutorService deferredRefresher;
    private final Set<Integer> seriesToRefresh;

    static Factory factory() {
        return new Factory();
    }
    static class Factory {
        SonarrDeferredRefresher forSeriesSet(Set<Integer> seriesToRefresh) {
            return new SonarrDeferredRefresher(seriesToRefresh);
        }
    }
    private SonarrDeferredRefresher(Set<Integer> seriesToRefresh) {
        sonarrApiGateway = Sonarr.api();
        deferredRefresher  = new ScheduledThreadPoolExecutor(1);
        this.seriesToRefresh = seriesToRefresh;
    }

    public void refresh() {
        for (var serieId : seriesToRefresh) {
            Runnable refreshTask = () -> sonarrApiGateway.refreshSerie(serieId);
            if (!ConfigLoader.isTestMode())
                deferredRefresher.schedule(refreshTask, 5, TimeUnit.SECONDS);
        }
        deferredRefresher.shutdown();
    }
}
