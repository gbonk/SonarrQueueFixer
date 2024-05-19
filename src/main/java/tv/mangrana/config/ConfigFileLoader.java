package tv.mangrana.config;

import tv.mangrana.exception.IncorrectWorkingReferencesException;

public class ConfigFileLoader extends CommonConfigFileLoader<ConfigFileLoader.ProjectConfiguration> {

    private static final String CONFIG_FILE = "SonarrFixerConfig.yml";

    public ConfigFileLoader() throws IncorrectWorkingReferencesException {
        super(ProjectConfiguration.class);
    }

    public enum ProjectConfiguration {
        UPLOADS_PATHS
    }

    @Override
    protected String getConfigFileName() {
        return CONFIG_FILE;
    }

}
