package tv.mangrana.config;

import tv.mangrana.exception.IncorrectWorkingReferencesException;

public class ConfigFileLoader extends CommonConfigFileLoader<ConfigFileLoader.ProjectConfiguration> {

    private static final String CONFIG_FILE = "SonarrFixerConfig.yml";
    private static ConfigFileLoader service;

    private ConfigFileLoader() throws IncorrectWorkingReferencesException {
        super(ProjectConfiguration.class);
    }

    public static ConfigFileLoader getLoader() throws IncorrectWorkingReferencesException {
        if (service==null)
            service = new ConfigFileLoader();
        return service;
    }

    public enum ProjectConfiguration {
        UPLOADS_PATHS
    }

    @Override
    protected String getConfigFileName() {
        return CONFIG_FILE;
    }

}
