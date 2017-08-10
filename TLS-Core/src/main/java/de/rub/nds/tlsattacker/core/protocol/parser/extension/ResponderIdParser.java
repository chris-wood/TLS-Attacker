/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2017 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.core.protocol.parser.extension;

import de.rub.nds.tlsattacker.core.constants.ExtensionByteLength;
import de.rub.nds.tlsattacker.core.protocol.message.extension.certificatestatusrequestitemv2.ResponderId;
import de.rub.nds.tlsattacker.core.protocol.parser.Parser;

/**
 *
 * @author Matthias Terlinde <matthias.terlinde@rub.de>
 */
public class ResponderIdParser extends Parser<ResponderId> {

    public ResponderIdParser(int startposition, byte[] array) {
        super(startposition, array);
    }

    @Override
    public ResponderId parse() {
        ResponderId id = new ResponderId();
        id.setIdLength(parseIntField(ExtensionByteLength.CERTIFICATE_STATUS_REQUEST_V2_RESPONDER_ID));
        id.setId(parseByteArrayField(id.getIdLength().getValue()));
        return id;
    }

}