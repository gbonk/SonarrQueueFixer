package tv.mangrana.worker;

import tv.mangrana.config.ConfigLoader;
import tv.mangrana.exception.IncorrectWorkingReferencesException;
import tv.mangrana.sonarr.Sonarr;

import static tv.mangrana.config.ConfigLoader.ProjectConfiguration.TEST_MODE;

public class MainWorker {

    private final QueueFixer queueFixer;

    public static void main(String[] args) throws IncorrectWorkingReferencesException {
        var worker = new MainWorker();
        worker.work();
    }

    private MainWorker() throws IncorrectWorkingReferencesException {
        var configLoader = ConfigLoader.getLoader();
        if (ConfigLoader.isEnabled(TEST_MODE))
            System.out.println("ATTENTION! TEST MODE ENABLED. No folders will be created nor files copied.");

        Sonarr.initService(configLoader);
        queueFixer = new QueueFixer();
    }

    private void work() {
        queueFixer.fix();
    }
}
