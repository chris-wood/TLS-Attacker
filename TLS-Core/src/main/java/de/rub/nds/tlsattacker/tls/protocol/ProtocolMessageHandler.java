/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2016 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.tls.protocol;

import de.rub.nds.tlsattacker.tls.exceptions.ConfigurationException;
import de.rub.nds.tlsattacker.tls.protocol.handshake.ServerHelloMessage;
import de.rub.nds.tlsattacker.tls.protocol.handshake.handler.ParserResult;
import de.rub.nds.tlsattacker.tls.protocol.parser.Parser;
import de.rub.nds.tlsattacker.tls.protocol.parser.ServerHelloMessageParser;
import de.rub.nds.tlsattacker.tls.protocol.preparator.Preparator;
import de.rub.nds.tlsattacker.tls.protocol.preparator.ServerHelloMessagePreparator;
import de.rub.nds.tlsattacker.tls.protocol.serializer.Serializer;
import de.rub.nds.tlsattacker.tls.protocol.serializer.ServerHelloMessageSerializer;
import de.rub.nds.tlsattacker.tls.workflow.TlsConfig;
import de.rub.nds.tlsattacker.tls.workflow.TlsContext;
import de.rub.nds.tlsattacker.util.ArrayConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Juraj Somorovsky <juraj.somorovsky@rub.de>
 * @param <Message>
 */
public abstract class ProtocolMessageHandler<Message extends ProtocolMessage> {

    protected static final Logger LOGGER = LogManager.getLogger(ProtocolMessageHandler.class);

    /**
     * tls context
     */
    protected final TlsContext tlsContext;

    /**
     *
     * @param tlsContext
     */
    public ProtocolMessageHandler(TlsContext tlsContext) {
        // ProtocolController protocolController =
        // ProtocolController.getInstance();
        this.tlsContext = tlsContext;
        if (tlsContext == null) {
            throw new ConfigurationException("TLS Context is not configured yet");
        }
    }

    /**
     * Prepare message for sending. This method invokes before and after method
     * hooks.
     *
     * @return message in bytes
     */
    public byte[] prepareMessage(Message message) {
        beforePrepareMessageAction();
        byte[] ret = prepareMessageAction(message);
        ret = afterPrepareMessageAction(ret);
        return ret;
    }

    /**
     * Parse incoming message bytes and return a pointer to the last processed
     * byte + 1. This pointer is then used by further protocol message handler.
     * This method invokes before and after method hooks.
     *
     * @param message
     * @param pointer
     * @return pointer to the next protocol message in the byte array, if any
     * message following, i.e. lastProcessedBytePointer + 1
     */
    public ParserResult parseMessage(byte[] message, int pointer) {
        LOGGER.debug("Parsing message from " + pointer + " :" + ArrayConverter.bytesToHexString(message));
        byte[] hookedMessage = beforeParseMessageAction(message, pointer);
        ParserResult result = parseMessageAction(hookedMessage, pointer);
        result.setParserPosition(afterParseMessageAction(result.getParserPosition()));
        return result;
    }

    /**
     * Prepare message for sending
     *
     * @param message
     * @return message in bytes
     */
    protected final byte[] prepareMessageAction(Message message) {
        Preparator preparator = getPreparator(message);
        preparator.prepare();
        adjustTLSContext(message);
        Serializer serializer = getSerializer(message);
        message.setCompleteResultingMessage(serializer.serialize());
        return message.getCompleteResultingMessage().getValue();
    }

    protected abstract Parser getParser(byte[] message, int pointer);

    protected abstract Preparator getPreparator(Message message);

    protected abstract Serializer getSerializer(Message message);

    /**
     * Adjusts the TLS Context according to the received or sending
     * ProtocolMessage
     *
     * @param message
     */
    protected abstract void adjustTLSContext(Message message);

    /**
     * Parse incoming message bytes and return a pointer to the last processed
     * byte. This pointer is then used by further protocol message handler.
     *
     * @param message
     * @param pointer
     * @return
     */
    protected final ParserResult parseMessageAction(byte[] message, int pointer) {
        Parser<Message> parser = getParser(message,pointer);
        Message parsedMessage = parser.parse();
        adjustTLSContext(parsedMessage);
        return new ParserResult(parsedMessage, parser.getPointer());
    }

    /**
     * Implementation hook, which allows the handlers to invoke specific methods
     * before the prepareMessageAction is executed
     */
    protected void beforePrepareMessageAction() {
    }

    /**
     * Implementation hook, which allows the handlers to invoke specific methods
     * after the prepareMessageAction is executed
     *
     * @return
     */
    protected byte[] afterPrepareMessageAction(byte[] ret) {
        return ret;
    }

    /**
     * Implementation hook, which allows the handlers to invoke specific methods
     * before the parseMessageAction is executed
     *
     * @param message
     * @param pointer
     * @return
     */
    protected byte[] beforeParseMessageAction(byte[] message, int pointer) {
        return message;
    }

    /**
     * Implementation hook, which allows the handlers to invoke specific methods
     * after the parseMessageAction is executed
     *
     * @param ret
     * @return
     */
    protected int afterParseMessageAction(int ret) {
        return ret;
    }
}
