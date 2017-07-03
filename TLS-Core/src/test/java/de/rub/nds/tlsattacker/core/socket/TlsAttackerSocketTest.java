/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2017 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.core.socket;

import de.rub.nds.modifiablevariable.util.ArrayConverter;
import de.rub.nds.tlsattacker.core.config.delegate.GeneralDelegate;
import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.record.layer.BlobRecordLayer;
import de.rub.nds.tlsattacker.core.record.layer.TlsRecordLayer;
import de.rub.nds.tlsattacker.core.unittest.helper.FakeTransportHandler;
import de.rub.nds.tlsattacker.core.workflow.DefaultWorkflowExecutor;
import de.rub.nds.tlsattacker.core.workflow.TlsConfig;
import de.rub.nds.tlsattacker.core.workflow.TlsContext;
import de.rub.nds.tlsattacker.core.workflow.action.executor.ActionExecutor;
import de.rub.nds.tlsattacker.core.workflow.action.executor.DefaultActionExecutor;
import de.rub.nds.tlsattacker.core.workflow.factory.WorkflowTraceType;
import de.rub.nds.tlsattacker.transport.ConnectionEnd;
import de.rub.nds.tlsattacker.transport.SimpleTransportHandler;
import java.io.IOException;
import java.net.Socket;
import java.security.Security;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Logger;
import org.apache.logging.log4j.Level;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * //TODO
 *
 * @author Robert Merget - robert.merget@rub.de
 */
public class TlsAttackerSocketTest {

    private TlsAttackerSocket socket;

    private TlsContext context;

    private FakeTransportHandler transportHandler;

    public TlsAttackerSocketTest() {
    }

    @Before
    public void setUp() {
        context = new TlsContext();
        context.setSelectedProtocolVersion(ProtocolVersion.TLS12);
        transportHandler = new FakeTransportHandler();
        context.setTransportHandler(transportHandler);
        socket = new TlsAttackerSocket(context);
        context.setRecordLayer(new TlsRecordLayer(context));

    }

    /**
     * Test of sendRawBytes method, of class TlsAttackerSocket.
     */
    @Test
    public void testSendRawBytes() throws Exception {
        socket.sendRawBytes(new byte[]{1, 2, 3});
        assertArrayEquals(new byte[]{1, 2, 3}, transportHandler.getSendByte());
    }

    /**
     * Test of recieveRawBytes method, of class TlsAttackerSocket.
     */
    @Test
    public void testRecieveRawBytes() throws Exception {
        transportHandler.setFetchableByte(new byte[]{1, 2, 3});
        byte[] received = socket.recieveRawBytes();
        assertArrayEquals(new byte[]{1, 2, 3}, received);
    }

    /**
     * Test of send method, of class TlsAttackerSocket.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testSend_String() throws IOException {
        socket.send("test");
        byte[] sentBytes = transportHandler.getSendByte();
        assertArrayEquals(sentBytes, ArrayConverter.concatenate(new byte[]{0x17,0x03,0x03,0x00,0x04},"test".getBytes()));
    }

    /**
     * Test of send method, of class TlsAttackerSocket.
     */
    @Test
    public void testSend_byteArr() {
        socket.send(new byte[]{0,1,2,3});
        byte[] sentBytes = transportHandler.getSendByte();
        assertArrayEquals(sentBytes, new byte[]{0x17,0x03,0x03,0x00,0x04,0,1,2,3});
    }

    /**
     * Test of receiveBytes method, of class TlsAttackerSocket.
     */
    @Test
    public void testReceiveBytes() throws Exception {
        transportHandler.setFetchableByte(new byte[]{0x17,0x03,0x03,0x00,0x03,8,8,8});
        byte[] receivedBytes =socket.receiveBytes();
        assertArrayEquals(receivedBytes,new byte[]{8,8,8});
    }

    /**
     * Test of receiveString method, of class TlsAttackerSocket.
     */
    @Test
    public void testReceiveString() throws Exception {
        transportHandler.setFetchableByte(ArrayConverter.concatenate(new byte[]{0x17,0x03,0x03,0x00,0x04},"test".getBytes()));
        String receivedString =socket.receiveString();
        assertEquals("test",receivedString);
    }

}