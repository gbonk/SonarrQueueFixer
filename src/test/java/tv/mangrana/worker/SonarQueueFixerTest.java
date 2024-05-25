package tv.mangrana.worker;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import tv.mangrana.config.ConfigLoader;
import tv.mangrana.sonarr.api.client.gateway.SonarrApiGateway;
import tv.mangrana.sonarr.api.schema.queue.Record;
import tv.mangrana.sonarr.api.schema.queue.StatusMessage;
import tv.mangrana.sonarr.api.schema.series.SonarrSerie;

import java.util.List;

import static org.mockito.Mockito.*;
import static tv.mangrana.worker.QueueFixer.IMPORT_FAILURE_BECAUSE_MATCHED_BY_ID;

public class SonarQueueFixerTest {

    private SonarrApiGateway sonarrApiGateway;

    private MainWorker mainWorker;

    @BeforeMethod
    public void setup() {
        ConfigLoader.weAreUnitTesting();
        sonarrApiGateway = mock(SonarrApiGateway.class, RETURNS_DEEP_STUBS);
        mainWorker = new MainWorker(sonarrApiGateway);
    }

    @Test //just for debugging purpose
    public void whenDestinationFolderDoesNotExist_AndIsTemporary_shouldCreateIt_AndHardlinkFiles() {
        prepareSonarrMockQueue();
        prepareSonarrMockSerie();

        mainWorker.work();

        //asserting nothing, sorry (too stuff would be needed to be mocked, and it's not worthy at this moment)
    }

    private void prepareSonarrMockQueue() {
        var record = new Record()
                .withSeriesId(getRandomInteger())
                .withTitle("Star Trek (1987) S01 [1080p]")
                .withOutputPath("./test/torrents/Star Trek (1987) S01 [1080p]")
                .withStatusMessages(prepareWarningMessages());
        when(sonarrApiGateway.getFullQueue().getRecords()).thenReturn(List.of(record));
    }

    private int getRandomInteger() {
        return Double.valueOf(Math.random()*100).intValue();
    }

    private List<StatusMessage> prepareWarningMessages() {
        return List.of(new StatusMessage()
                .withMessages(List.of(IMPORT_FAILURE_BECAUSE_MATCHED_BY_ID)));
    }

    private void prepareSonarrMockSerie() {
        SonarrSerie serie = new SonarrSerie()
                .withPath("./test/sonarr_uploads/Star Trek (1987) {tvdb-71470}");
        when(sonarrApiGateway.getSerieById(anyInt())).thenReturn(serie);
    }

}