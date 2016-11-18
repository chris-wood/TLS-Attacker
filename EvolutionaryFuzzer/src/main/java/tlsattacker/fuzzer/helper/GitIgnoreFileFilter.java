/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2016 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package tlsattacker.fuzzer.helper;

import java.io.File;
import java.io.FilenameFilter;
import java.util.logging.Logger;

/**
 * A FileFilter that ignores .gitignore files
 * 
 * @author Robert Merget - robert.merget@rub.de
 */
public class GitIgnoreFileFilter implements FilenameFilter {

    /**
     * Accepts all files which don't equal ".gitignore"
     * 
     * @param dir
     *            Directory the File is in
     * @param name
     *            Name of the File
     * @return True if the file does not equal ".gitignore"
     */
    @Override
    public boolean accept(File dir, String name) {
        return !name.equals(".gitignore");
    }

    private static final Logger LOG = Logger.getLogger(GitIgnoreFileFilter.class.getName());

}
