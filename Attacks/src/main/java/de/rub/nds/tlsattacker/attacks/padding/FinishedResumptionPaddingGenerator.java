
package de.rub.nds.tlsattacker.attacks.padding;

import de.rub.nds.tlsattacker.attacks.constants.PaddingRecordGeneratorType;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.RunningModeType;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import de.rub.nds.tlsattacker.core.record.Record;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import de.rub.nds.tlsattacker.core.workflow.action.GenericReceiveAction;
import de.rub.nds.tlsattacker.core.workflow.action.SendAction;
import de.rub.nds.tlsattacker.core.workflow.factory.WorkflowConfigurationFactory;
import de.rub.nds.tlsattacker.core.workflow.factory.WorkflowTraceType;
import java.util.LinkedList;
import java.util.List;

public class FinishedResumptionPaddingGenerator extends PaddingVectorGenerator {

    public FinishedResumptionPaddingGenerator(PaddingRecordGeneratorType type) {
        super(type);
    }

    @Override
    public List<WorkflowTrace> getPaddingOracleVectors(Config config) {
        List<WorkflowTrace> traceList = new LinkedList<>();
        for (Record record : recordGenerator.getRecords(config.getDefaultSelectedCipherSuite(),
                config.getDefaultSelectedProtocolVersion())) {
            WorkflowTrace trace = new WorkflowConfigurationFactory(config).createWorkflowTrace(
                    WorkflowTraceType.FULL_RESUMPTION, RunningModeType.CLIENT);
            SendAction sendAction = (SendAction) trace.getLastSendingAction();
            LinkedList<AbstractRecord> recordList = new LinkedList<>();
            recordList.add(new Record(config));
            recordList.add(record);
            sendAction.setRecords(recordList);
            trace.addTlsAction(new GenericReceiveAction());
            traceList.add(trace);
        }
        return traceList;
    }

}
