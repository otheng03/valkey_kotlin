package valkey.kotlin.kotlin.config

fun initConfigValues() {
    for (standardConfig in standardConfigs) {
        TODO("registerConfigValue")
    }
    /*
    configs = dictCreate(&sdsHashDictType);
    dictExpand(configs, sizeof(static_configs) / sizeof(standardConfig));
    for (standardConfig *config = static_configs; config->name != NULL; config++) {
        if (config->interface.init) config->interface.init(config);
        /* Add the primary config to the dictionary. */
        int ret = registerConfigValue(config->name, config, 0);
        serverAssert(ret);

        /* Aliases are the same as their primary counter parts, but they
         * also have a flag indicating they are the alias. */
        if (config->alias) {
            int ret = registerConfigValue(config->alias, config, ALIAS_CONFIG);
            serverAssert(ret);
        }
    }
    */
}

fun initServerConfig() {
    initConfigValues()
}
