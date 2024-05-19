package tv.mangrana.worker;

import tv.mangrana.sonarr.Sonarr;
import tv.mangrana.sonarr.api.client.gateway.SonarrApiGateway;
import tv.mangrana.sonarr.api.schema.queue.Record;
import tv.mangrana.sonarr.api.schema.series.SonarrSerie;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QueueFixer {
    final static String IMPORT_FAILURE_BECAUSE_MATCHED_BY_ID = "Found matching series via grab history, but release was matched to series by ID. Automatic import is not possible. See the FAQ for details.";
    private final SonarrApiGateway sonarrApiGateway;
    private final FailedImportFixer.Factory failedImportFixerFactory;

    QueueFixer() {
        sonarrApiGateway = Sonarr.api();
        failedImportFixerFactory = FailedImportFixer.factory();
    }

    void fix() {
        List<Record> sonarQueue = retrieveQueueRecordsFromSonarr();
        Collection<Record> records = deduplicate(sonarQueue);
        List<Record> recordsToFix = filterFailedImportsOfIdProblem(records);
        recordsToFix.forEach(this::try2FixFailedImport);
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

    private void try2FixFailedImport(Record record) {
        try {
            var seriesId = record.getSeriesId();
            SonarrSerie serie = getSerieFromSonarr(seriesId);
            if (serie == null) return;

            failedImportFixerFactory
                    .newFixerFor(record, serie)
                    .fix();
        } catch (IOException e) {
            System.out.printf("!! could not fix the import %s%n", record.getTitle());
            e.printStackTrace();
        }
    }

    private SonarrSerie getSerieFromSonarr(Integer seriesId) {
        return sonarrApiGateway.getSerieById(seriesId);
    }

}
