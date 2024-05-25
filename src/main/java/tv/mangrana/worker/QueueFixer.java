package tv.mangrana.worker;

import tv.mangrana.config.ConfigLoader;
import tv.mangrana.sonarr.Sonarr;
import tv.mangrana.sonarr.api.client.gateway.SonarrApiGateway;
import tv.mangrana.sonarr.api.schema.queue.Record;
import tv.mangrana.sonarr.api.schema.series.SonarrSerie;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static tv.mangrana.config.ConfigLoader.ProjectConfiguration.TEST_MODE;

public class QueueFixer {
    final static String IMPORT_FAILURE_BECAUSE_MATCHED_BY_ID = "Found matching series via grab history, but release was matched to series by ID. Automatic import is not possible. See the FAQ for details.";

    private final SonarrApiGateway sonarrApiGateway;
    private final FailedImportFixer.Factory fixerFactory;

    QueueFixer() {
        sonarrApiGateway = Sonarr.api();
        fixerFactory = FailedImportFixer.factory();
    }

    void fix() {
        List<Record> sonarQueue = retrieveQueueRecordsFromSonarr();
        var distinctRecords = deduplicate(sonarQueue);
        var recordsToFix = filterFailedImportsOfIdProblem(distinctRecords);
        recordsToFix.forEach(this::try2FixFailedImport);
        cleanWorkedElementsFromQueue(sonarQueue, recordsToFix);
    }

    private List<Record> retrieveQueueRecordsFromSonarr() {
        return sonarrApiGateway.getFullQueue().getRecords();
    }

    private Collection<Record> deduplicate(List<Record> repetitiveRecords) {
        var recordsByTitle = new HashMap<String, Record>();
        for (var record : repetitiveRecords)
            recordsByTitle.putIfAbsent(record.getTitle(), record);
        return recordsByTitle.values();
    }

    private List<Record> filterFailedImportsOfIdProblem(Collection<Record> records) {
        return records.stream()
                .filter(this::recordsWithImportFailureBecauseIdMatching)
                .toList();
    }

    private void try2FixFailedImport(Record record) {
        try {
            var seriesId = record.getSeriesId();
            SonarrSerie serie = sonarrApiGateway.getSerieById(seriesId);
            if (serie == null) return;

            fixerFactory
                    .newFixerFor(record, serie)
                    .fix();
        } catch (IOException e) {
            System.out.printf("!! could not fix the import %s%n", record.getTitle());
            e.printStackTrace();
        }
    }

    private void cleanWorkedElementsFromQueue(List<Record> sonarQueue, List<Record> recordsToFix) {
        List<String> workedTitles = mapRecord2Title(recordsToFix);
        List<Integer> recordIds2Delete = filterPresentTitlesFromQueue(sonarQueue, workedTitles);
        if (!ConfigLoader.isEnabled(TEST_MODE))
            sonarrApiGateway.deleteQueueElements(recordIds2Delete);
    }

    private boolean recordsWithImportFailureBecauseIdMatching(Record record) {
        return record.getStatusMessages().stream()
                .flatMap(status -> status.getMessages().stream())
                .anyMatch(IMPORT_FAILURE_BECAUSE_MATCHED_BY_ID::equals);
    }

    private List<String> mapRecord2Title(List<Record> records) {
        return records.stream()
                .map(Record::getTitle)
                .distinct()
                .toList();
    }

    private List<Integer> filterPresentTitlesFromQueue(List<Record> sonarQueue, List<String> workedTitles) {
        return sonarQueue.stream()
                .filter(r -> workedTitles.contains(r.getTitle()))
                .map(Record::getId)
                .toList();
    }
}
