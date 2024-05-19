package tv.mangrana.worker;

import tv.mangrana.config.ConfigFileLoader;
import tv.mangrana.sonarr.api.client.gateway.SonarrApiGateway;
import tv.mangrana.sonarr.api.schema.queue.Record;
import tv.mangrana.sonarr.api.schema.series.SonarrSerie;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QueueFixer {
    final static String IMPORT_FAILURE_BECAUSE_MATCHED_BY_ID = "Found matching series via grab history, but release was matched to series by ID. Automatic import is not possible. See the FAQ for details.";
    private final SonarrApiGateway sonarrApiGateway;

    QueueFixer(ConfigFileLoader configFileLoader) {
        sonarrApiGateway = new SonarrApiGateway(configFileLoader);
    }

    void fix() {
        List<Record> sonarQueue = retrieveQueueRecordsFromSonarr();
        Collection<Record> records = deduplicate(sonarQueue);
        List<Record> recordsToFix = filterFailedImportsOfIdProblem(records);
        recordsToFix.forEach(this::fixFailedImport);
    }

    private List<Record> retrieveQueueRecordsFromSonarr() {
        return sonarrApiGateway.getFullQueue().getRecords();
    }

    private Collection<Record> deduplicate(List<Record> repetitiveRecords) {
        Map<String, Record> recordsByTitle = new HashMap<>();
        repetitiveRecords.forEach(record ->
                recordsByTitle.putIfAbsent(record.getTitle(), record));
        return recordsByTitle.values();
    }

    private List<Record> filterFailedImportsOfIdProblem(Collection<Record> records) {
        return records.stream()
                .filter(this::recordsWithImportFailureBecauseIdMatching)
                .collect(Collectors.toList());
    }

    private boolean recordsWithImportFailureBecauseIdMatching(Record record) {
        return record.getStatusMessages().stream()
                .flatMap(status -> status.getMessages().stream())
                .anyMatch(IMPORT_FAILURE_BECAUSE_MATCHED_BY_ID::equals);
    }

    private void fixFailedImport(Record record) {
        var seriesId = record.getSeriesId();
        SonarrSerie serie = getSerieFromSonarr(seriesId);
        FailedImportFixer
                .of(record, serie)
                .fix();
    }

    private SonarrSerie getSerieFromSonarr(Integer seriesId) {
        return sonarrApiGateway.getSerieById(seriesId);
    }

}
