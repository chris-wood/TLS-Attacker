/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2017 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.core.workflow.action;

import de.rub.nds.tlsattacker.core.exceptions.WorkflowExecutionException;
import de.rub.nds.tlsattacker.core.state.Aliasable;
import de.rub.nds.tlsattacker.core.state.State;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * TlsAction that can be executed in a WorkflowTrace. The TlsAction is the basic
 * building block for WorkflowTraces. A WorkflowTrace is a list of TLSActions.
 * Executing a WorkflowTrace means iterating through this list and calling
 * execute() on each TlsAction.
 *
 * @author Robert Merget - robert.merget@rub.de
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class TlsAction implements Serializable, Aliasable {

    protected static final Logger LOGGER = LogManager.getLogger(TlsAction.class.getName());
    private static final boolean EXECUTED_DEFAULT = false;

    private Boolean executed = null;

    // Whether the action is executed in a workflow with a single connection
    // or not. Useful to decide which information can be stripped in filter().
    @XmlTransient
    private Boolean singleConnectionWorkflow = true;

    // Use LinkedHashSet to get constant order for serialization.
    @XmlTransient
    private final Set<String> aliases = new LinkedHashSet<>();

    public TlsAction() {
    }

    public TlsAction(String alias) {
        this.aliases.add(alias);
    }

    public TlsAction(Collection<? extends String> aliases) {
        this.aliases.addAll(aliases);
    }

    public TlsAction(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
    }

    public boolean isExecuted() {
        if (executed == null) {
            return EXECUTED_DEFAULT;
        }
        return executed;
    }

    public void setExecuted(Boolean executed) {
        this.executed = executed;
    }

    public Boolean isSingleConnectionWorkflow() {
        return singleConnectionWorkflow;
    }

    public void setSingleConnectionWorkflow(Boolean singleConnectionWorkflow) {
        this.singleConnectionWorkflow = singleConnectionWorkflow;
    }

    public abstract void execute(State state) throws WorkflowExecutionException, IOException;

    public abstract void reset();

    /**
     * Add default values and initialize empty fields.
     */
    public abstract void normalize();

    /**
     * Add default values from given defaultAction and initialize empty fields.
     */
    public abstract void normalize(TlsAction defaultAction);

    /**
     * Filter empty fields and default values.
     */
    public abstract void filter();

    /**
     * Filter empty fields and default values given in defaultAction.
     */
    public abstract void filter(TlsAction defaultAction);

    /**
     * Check that the Action got executed as planned.
     */
    public abstract boolean executedAsPlanned();

    public boolean isMessageAction() {
        return this instanceof MessageAction;
    }

    @Override
    public String getFirstAlias() {
        return getAllAliases().iterator().next();
    }

    @Override
    public Set<String> getAllAliases() {
        return Collections.unmodifiableSet(aliases);
    }

    protected void setAlias(String alias) {
        aliases.clear();
        aliases.add(alias);
    }

    @Override
    public boolean containsAllAliases(Collection<String> aliases) {
        return this.aliases.containsAll(aliases);
    };

    @Override
    public boolean containsAlias(String alias) {
        return this.aliases.contains(alias);
    };

    @Override
    public String aliasesToString() {
        StringBuilder sb = new StringBuilder();
        for (String alias : getAllAliases()) {
            sb.append(alias).append(",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }

    public String toCompactString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        if (!getAllAliases().isEmpty()) {
            sb.append(" [").append(aliasesToString()).append("]");
        }
        return sb.toString();
    }

}
