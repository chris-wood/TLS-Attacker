/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2016 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.rub.nds.tlsscanner.report.check;

import de.rub.nds.tlsscanner.exception.UnloadableConfigException;
import java.io.File;
import java.util.HashMap;

/**
 *
 * @author Robert Merget - robert.merget@rub.de
 */
public class CheckConfigCache {

    private File pathToConfig;

    private HashMap<CheckType, CheckConfig> cacheMap;

    private CheckConfigCache() {
        cacheMap = new HashMap<>();
        pathToConfig = new File("../resources/scanner/config_en/");
    }

    public synchronized File getPathToConfig() {
        return pathToConfig;
    }

    public synchronized void setPathToConfig(File pathToConfig) {
        this.pathToConfig = pathToConfig;
    }

    public synchronized static CheckConfigCache getInstance() {
        return CheckConfigCacheHolder.INSTANCE;
    }

    private static class CheckConfigCacheHolder {

        private static final CheckConfigCache INSTANCE = new CheckConfigCache();
    }

    public synchronized CheckConfig getCheckConfig(CheckType type) {
        CheckConfig config = cacheMap.get(type);
        if (config == null) {
            config = CheckConfigSerializer.deserialize(new File(pathToConfig.getAbsolutePath() + "/"
                    + getConfigFileName(type)));
            cacheMap.put(type, config);
        }
        return config;
    }

    private String getConfigFileName(CheckType type) {
        switch (type) {
            case CERTIFICATE_EXPIRED:
                return "certificate_expired.xml";
            case CERTIFICATE_NOT_VALID_YET:
                return "certificate_not_valid_yet.xml";
            case CERTIFICATE_REVOKED:
                return "certificate_revoked.xml";
            case CERTIFICATE_SENT_BY_SERVER:
                return "certificate_sent_by_server.xml";
            case CERTIFICATE_WEAK_HASH_FUNCTION:
                return "certificate_weak_hash_function.xml";
            case CERTIFICATE_WEAK_SIGN_ALGORITHM:
                return "certificate_weak_sign_algo.xml";
            case CIPHERSUITEORDER_ENFORCED:
                return "ciphersuiteorder_enforced.xml";
            case CIPHERSUITE_ANON:
                return "ciphersuite_anon.xml";
            case CIPHERSUITE_CBC:
                return "ciphersuite_cbc.xml";
            case CIPHERSUITE_EXPORT:
                return "ciphersuite_export.xml";
            case CIPHERSUITE_NULL:
                return "ciphersuite_null.xml";
            case CIPHERSUITE_RC4:
                return "ciphersuite_rc4.xml";
            case PROTOCOLVERSION_SSL2:
                return "protocolversion_ssl2.xml";
            case PROTOCOLVERSION_SSL3:
                return "protocolversion_ssl3.xml";
            default:
                throw new UnloadableConfigException("Could not convert CheckType \"" + type.name()
                        + "\" to a config Filename");
        }
    }
}
