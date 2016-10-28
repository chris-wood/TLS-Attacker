/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2016 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0 http://www.apache.org/licenses/LICENSE-2.0
 */
package tlsattacker.fuzzer.analyzer;

import tlsattacker.fuzzer.config.analyzer.IsTimeoutRuleConfig;
import tlsattacker.fuzzer.config.EvolutionaryFuzzerConfig;
import tlsattacker.fuzzer.result.Result;
import tlsattacker.fuzzer.testvector.TestVectorSerializer;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;

/**
 * A rule which records TestVectors that caused a timeout.
 * 
 * @author Robert Merget - robert.merget@rub.de
 */
public class IsTimeoutRule extends Rule {

    /**
     * The number of TestVectors that this rule applied to
     */
    private int found = 0;

    /**
     * The configuration object for this rule
     */
    private IsTimeoutRuleConfig config;

    public IsTimeoutRule(EvolutionaryFuzzerConfig evoConfig) {
	super(evoConfig, "is_timeout.rule");
	File f = new File(evoConfig.getAnalyzerConfigFolder() + configFileName);
	if (f.exists()) {
	    config = JAXB.unmarshal(f, IsTimeoutRuleConfig.class);
	}
	if (config == null) {
	    config = new IsTimeoutRuleConfig();
	    writeConfig(config);
	}
	prepareConfigOutputFolder();
    }

    /**
     * The rule apples if the TestVector is considered as timedout
     * @param result Result to analyze
     * @return True if the TestVector did timeout
     */
    @Override
    public boolean applies(Result result) {
	return result.didTimeout();
    }

    /**
     * Stores the Testvector
     * @param result Result to analyze
     */
    @Override
    public void onApply(Result result) {
	found++;
	File f = new File(evoConfig.getOutputFolder() + config.getOutputFolder() + result.getId());
	try {
	    result.getVector().getTrace().setDescription("WorkflowTrace did Timeout!");
	    f.createNewFile();
	    TestVectorSerializer.write(f, result.getVector());
	} catch (JAXBException | IOException E) {
	    LOG.log(Level.SEVERE,
		    "Could not write Results to Disk! Does the Fuzzer have the rights to write to "
			    + f.getAbsolutePath(), E);
	}
    }

    /**
     * Do nothing
     * @param result Result to analyze
     */
    @Override
    public void onDecline(Result result) {
    }

     /**
     * Generates a status report
     * @return
     */
    @Override
    public String report() {
	if (found > 0) {
	    return "Found " + found + " Traces which caused the Server to Timeout\n";
	} else {
	    return null;
	}
    }

    @Override
    public IsTimeoutRuleConfig getConfig() {
	return config;
    }

    private static final Logger LOG = Logger.getLogger(IsTimeoutRule.class.getName());
}
