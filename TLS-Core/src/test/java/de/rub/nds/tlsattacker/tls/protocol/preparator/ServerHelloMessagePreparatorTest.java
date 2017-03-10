/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2016 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.tls.protocol.preparator;

import de.rub.nds.tlsattacker.tls.constants.CipherSuite;
import de.rub.nds.tlsattacker.tls.constants.CompressionMethod;
import de.rub.nds.tlsattacker.tls.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.tls.protocol.message.ServerHelloMessage;
import de.rub.nds.tlsattacker.tls.workflow.TlsContext;
import de.rub.nds.tlsattacker.util.ArrayConverter;
import de.rub.nds.tlsattacker.util.FixedTimeProvider;
import de.rub.nds.tlsattacker.util.RandomHelper;
import de.rub.nds.tlsattacker.util.TimeHelper;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Robert Merget - robert.merget@rub.de
 */
public class ServerHelloMessagePreparatorTest {

    private ServerHelloMessage message;
    private TlsContext context;
    private ServerHelloMessagePreparator preparator;

    public ServerHelloMessagePreparatorTest() {
    }

    @Before
    public void setUp() {
        this.message = new ServerHelloMessage();
        this.context = new TlsContext();
        this.preparator = new ServerHelloMessagePreparator(context, message);
    }

    /**
     * Test of prepareHandshakeMessageContents method, of class
     * ServerHelloMessagePreparator.
     */
    @Test
    public void testPrepare() {
        RandomHelper.getRandom().setSeed(0);
        TimeHelper.setProvider(new FixedTimeProvider(12345l));
        List<CipherSuite> suiteList = new LinkedList<>();
        context.getConfig().setHighestProtocolVersion(ProtocolVersion.TLS12);
        suiteList.add(CipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA256);
        suiteList.add(CipherSuite.RFC_TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256);// use
                                                                                 // an
                                                                                 // unusual
                                                                                 // ciphersuite
                                                                                 // to
                                                                                 // make
                                                                                 // sure
                                                                                 // that
                                                                                 // this
                                                                                 // test
                                                                                 // works
                                                                                 // as
                                                                                 // expected
        suiteList.add(CipherSuite.TLS_DHE_PSK_WITH_AES_128_GCM_SHA256);
        context.setClientSupportedCiphersuites(suiteList);
        List<CipherSuite> ourSuiteList = new LinkedList<>();
        ourSuiteList.add(CipherSuite.RFC_TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256);
        List<CompressionMethod> ourCompressionList = new LinkedList<>();
        ourCompressionList.add(CompressionMethod.LZS);
        context.getConfig().setSupportedCiphersuites(ourSuiteList);
        context.getConfig().setSupportedCompressionMethods(ourCompressionList);
        context.setHighestClientProtocolVersion(ProtocolVersion.TLS11);
        List<CompressionMethod> compressionList = new LinkedList<>();
        compressionList.add(CompressionMethod.NULL);// same as ciphersuite
        compressionList.add(CompressionMethod.LZS);// same as ciphersuite
        context.setClientSupportedCompressions(compressionList);
        context.getConfig().setSessionId(new byte[] { 0, 1, 2, 3, 4, 5 });
        preparator.prepare();
        assertArrayEquals(ProtocolVersion.TLS11.getValue(), message.getProtocolVersion().getValue());
        assertArrayEquals(ArrayConverter.longToUint32Bytes(12345l), message.getUnixTime().getValue());
        System.out.println(ArrayConverter.bytesToHexString(message.getRandom().getValue()));
        assertArrayEquals(
                ArrayConverter.hexStringToByteArray("60B420BB3851D9D47ACB933DBE70399BF6C92DA33AF01D4FB770E98C"),
                message.getRandom().getValue());
        assertArrayEquals(ArrayConverter.hexStringToByteArray("000102030405"), message.getSessionId().getValue());
        assertTrue(6 == message.getSessionIdLength().getValue());
        assertTrue(message.getExtensionBytes().getValue().length == 0);
        assertTrue(0 == message.getExtensionsLength().getValue());
    }

}