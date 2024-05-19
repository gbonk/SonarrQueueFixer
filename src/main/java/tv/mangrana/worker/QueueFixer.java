package tv.mangrana.worker;

import tv.mangrana.config.ConfigFileLoader;
import tv.mangrana.sonarr.api.client.gateway.SonarrApiGateway;
import tv.mangrana.sonarr.api.schema.queue.Record;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QueueFixer {
    final static String ID_IMPORT_FAILURE = "Found matching series via grab history, but release was matched to series by ID. Automatic import is not possible. See the FAQ for details.";
    private final SonarrApiGateway sonarrApiGateway;

    QueueFixer(ConfigFileLoader configFileLoader) {
        sonarrApiGateway = new SonarrApiGateway(configFileLoader);
    }

    void fix() {
        var queue = sonarrApiGateway.getFullQueue();
        List<Record> recordsWithImportFailure = queue.getRecords().stream()
                .filter(this::recordsWithImportFailure)
                .collect(Collectors.toList());
        fixFailedImports(recordsWithImportFailure);
    }

    private boolean recordsWithImportFailure(Record record) {
        return record.getStatusMessages().stream()
                .flatMap(status -> status.getMessages().stream())
                .anyMatch(ID_IMPORT_FAILURE::equals);
    }

    private void fixFailedImports(List<Record> recordsWithImportFailure) {
        Map<String, Record> recordsByTitle = new HashMap<>();
        for (var record : recordsWithImportFailure)
            recordsByTitle.putIfAbsent(record.getTitle(), record);
        System.out.println(recordsByTitle.keySet());
    }
}
