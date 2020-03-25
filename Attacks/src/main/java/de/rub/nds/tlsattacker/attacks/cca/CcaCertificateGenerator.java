/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2017 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.attacks.cca;

import de.rub.nds.modifiablevariable.util.ArrayConverter;
import de.rub.nds.modifiablevariable.util.Modifiable;
import de.rub.nds.tlsattacker.core.certificate.CertificateKeyPair;
import de.rub.nds.tlsattacker.core.config.delegate.CcaDelegate;
import de.rub.nds.tlsattacker.core.crypto.keys.CustomPrivateKey;
import de.rub.nds.tlsattacker.core.crypto.keys.CustomPublicKey;
import de.rub.nds.tlsattacker.core.protocol.message.CertificateMessage;
import de.rub.nds.tlsattacker.core.protocol.message.cert.CertificatePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.tls.Certificate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CcaCertificateGenerator {

    /**
     *
     * @param ccaDelegate
     * @param ccaCertificateType
     * @return
     */
    public static CertificateMessage generateCertificate(CcaDelegate ccaDelegate, CcaCertificateType ccaCertificateType) {
        CertificateMessage certificateMessage = new CertificateMessage();
        if (ccaCertificateType != null) {
            switch (ccaCertificateType) {
                case CLIENT_INPUT:
                    List<CertificatePair> certificatePairsList = new LinkedList<>();
                    CertificatePair certificatePair = new CertificatePair(ccaDelegate.getClientCertificate());
                    certificatePairsList.add(certificatePair);
                    certificateMessage.setCertificatesList(certificatePairsList);
                    break;
                case EMPTY:
                    certificateMessage.setCertificatesListBytes(Modifiable.explicit(new byte[0]));
                    break;
                case ROOTv1_CAv3_LEAFv1_nLEAF_RSAv3:
                case ROOTv1_CAv3_LEAFv2_nLEAF_RSAv3:
                case ROOTv3_CAv3_NameConstraints_LEAF_RSAv3:
                case ROOTv3_CAv3_CaFalse_LEAF_RSAv3:
                case ROOTv3_CAv3_KeyUsageDigitalSignatures_LEAF_RSAv3:
                case ROOTv3_CAv3_KeyUsageNothing_LEAF_RSAv3:
                case ROOTv3_CAv3_LEAF_RSAv1:
                case ROOTv3_CAv3_LEAF_RSAv2:
                case ROOTv3_CAv3_LEAF_RSAv3:
                case ROOTv3_CAv3_LEAF_RSAv3__RDN_difference:
                case ROOTv3_CAv3_LEAF_RSAv3_expired:
                case ROOTv3_CAv3_LEAF_RSAv3_extendedKeyUsageCodeSign:
                case ROOTv3_CAv3_LEAF_RSAv3_extendedKeyUsageServerAuth:
                case ROOTv3_CAv3_LEAF_RSAv3_NotYetValid:
                case ROOTv3_CAv3_LEAF_RSAv3_UnknownCritExt:
                case ROOTv3_CAv3_LEAFv1_nLEAF_RSAv3:
                case ROOTv3_CAv3_LEAFv2_nLEAF_RSAv3:
                case ROOTv3_CAv3_NoBasicConstraints_LEAF_RSAv3:
                case ROOTv3_CAv3_NoKeyUsage_LEAF_RSAv3:
                case ROOTv3_CAv3_ZeroPathLen_CAv3_LEAF_RSAv3:
                case ROOTv3_CAv3_CAv3_PathLoop:
                case ROOTv3_CAv3_LEAF_RSAv3_UnknownExt:
                case ROOTv3_CAv3_LEAF_RSAv3_CaTrue:
                case ROOTv3_CAv3_LEAF_RSAv3_KeyUsageDigitalSignatures:
                case ROOTv3_CAv3_LEAF_RSAv3_KeyUsageNothing:
                case ROOTv3_CAv3_MalformedNameConstraints_LEAF_RSAv3:
                case ROOTv3_CAv3_LEAF_RSAv3_AdditionalCertAfterChain:
                case ROOTv3_CAv3_LEAF_RSAv3_SelfSigned:
                case ROOTv3_CAv3_LEAF_RSAv3_EmptySigned:
                case ROOTv3_CAv3_LEAF_RSAv3_AdditionalCertAfterLeaf:
                case ROOTv3_CAv3_LEAF_RSAv3_CertPolicy:
                case ROOTv3_CAv3_LEAF_RSAv3_NullSigned:
                case ROOTv3_CAv3_LEAF_RSAv3_MalformedAlgorithmParameters:
                case ROOTv3_CAv3_NameConstraints_LEAF_RSAv3_SANCrit:
                case ROOTv3_CAv3_NameConstraints_LEAF_RSAv3_SAN2Crit:
                case ROOTv3_CAv3_NameConstraints_LEAF_RSAv3_SAN:
                case ROOTv3_CAv3_NameConstraints_LEAF_RSAv3_SAN2:
                case ROOTv3_CAv3_LEAF_RSAv3_CRLDistributionPoints:
                case ROOTv3_NewFakeChain_ROOTv3_CAv3_LEAF_RSAv3:
                case ROOTv3_CAv3_LEAF_RSAvNeg1:
                case ROOTv3_CAv3_LEAF_RSAvNeg1_nLeaf_RSAv3:
                case ECROOTv3_CAv3_LEAF_ECv3:
                case ECROOTv3_CAv3CustomCurve_LEAF_ECv3:
                case ECROOTv3_Curveball_CAv3_LEAF_ECv3:
                case ROOTv3_CAv3_LEAF_RSAv1_UniqueIdentifiers:
                case ROOTv3_CAv3_LEAF_RSAv3_MismatchingAlgorithmParameters:
                case ROOTv3_CAv3_LEAF_RSAv3_MismatchingAlgorithms1:
                case ROOTv3_CAv3_LEAF_RSAv3_MismatchingAlgorithms2:
                case ECROOTv3_CAv3_LEAF_ECv3_GarbageParameters:
                case DSAROOTv3_CAv3_LEAF_DSAv3:
                case ROOTv3_debug:
                    certificateMessage = generateCertificateMessage(ccaDelegate, ccaCertificateType);
                    break;
                default:
                    break;
            }
        }
        return certificateMessage;
    }

    /**
     *
     * TODO: ASN.1 Parsing bugs for inspiration
     *
     */
    private static CertificateMessage generateCertificateMessage(CcaDelegate ccaDelegate,
            CcaCertificateType ccaCertificateType) {

        Logger LOGGER = LogManager.getLogger();

        // Declare variables for later use
        CertificateMessage certificateMessage = new CertificateMessage();
        List<CertificatePair> certificatePairList = new LinkedList<>();
        CertificatePair certificatePair;
        byte[] encodedLeafCertificate;
        byte[][] encodedCertificates;
        CertificateKeyPair certificateKeyPair;

        CcaCertificateManager ccaCertificateManager = CcaCertificateManager.getReference(ccaDelegate);
        Map.Entry entry = ccaCertificateManager.getCertificateList(ccaCertificateType);

        encodedCertificates = (byte[][]) entry.getKey();
        encodedLeafCertificate = encodedCertificates[0];
        for (byte[] certificate : encodedCertificates) {
            if (certificate.length > 0) {
                certificatePair = new CertificatePair(certificate);
                certificatePairList.add(certificatePair);
            }
        }
        certificateMessage.setCertificatesList(certificatePairList);
        // Parse leaf certificate for CertificateKeyPair
        Certificate certificate = parseCertificate(encodedLeafCertificate.length, encodedLeafCertificate);

        try {
            certificateKeyPair = new CertificateKeyPair(encodedLeafCertificate, certificate,
                    (PrivateKey) ((Map.Entry<CustomPrivateKey, CustomPublicKey>) entry.getValue()).getKey(),
                    (PublicKey) ((Map.Entry<CustomPrivateKey, CustomPublicKey>) entry.getValue()).getValue());
        } catch (IOException ioe) {
            LOGGER.error("IOE while creating CertificateKeyPair");
            return null;
        }
        certificateMessage.setCertificateKeyPair(certificateKeyPair);

        return certificateMessage;
    }

    private static Certificate parseCertificate(int lengthBytes, byte[] bytesToParse) {
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(ArrayConverter.concatenate(
                    ArrayConverter.intToBytes(lengthBytes + 3, 3), ArrayConverter.intToBytes(lengthBytes, 3),
                    bytesToParse));
            return Certificate.parse(stream);
        } catch (Exception E) {
            return null;
        }
    }

}
