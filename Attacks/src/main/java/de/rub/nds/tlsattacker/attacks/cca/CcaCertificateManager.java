/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2020 Ruhr University Bochum, Paderborn University,
 * and Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.attacks.cca;

import de.rub.nds.asn1.Asn1Encodable;
import de.rub.nds.asn1.encoder.Asn1EncoderForX509;
import de.rub.nds.asn1.model.Asn1Sequence;
import de.rub.nds.asn1.model.KeyInfo;
import de.rub.nds.asn1tool.xmlparser.Asn1XmlContent;
import de.rub.nds.asn1tool.xmlparser.XmlParser;
import de.rub.nds.tlsattacker.attacks.impl.Attacker;
import de.rub.nds.tlsattacker.core.certificate.PemUtil;
import de.rub.nds.tlsattacker.core.config.delegate.CcaDelegate;
import de.rub.nds.tlsattacker.core.constants.NamedGroup;
import de.rub.nds.tlsattacker.core.crypto.keys.*;
import de.rub.nds.x509attacker.keyfilemanager.KeyFileManager;
import de.rub.nds.x509attacker.keyfilemanager.KeyFileManagerException;
import de.rub.nds.x509attacker.linker.Linker;
import de.rub.nds.x509attacker.xmlsignatureengine.XmlSignatureEngine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECPoint;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static de.rub.nds.tlsattacker.core.certificate.PemUtil.readPrivateKey;
import static de.rub.nds.tlsattacker.core.certificate.PemUtil.readPublicKey;
import static de.rub.nds.x509attacker.X509Attacker.*;

public class CcaCertificateManager {

    private static Logger LOGGER = LogManager.getLogger();

    private static CcaCertificateManager reference = null;
    private final Map<CcaCertificateType, CcaCertificateChain> certificateKeyMap = new HashMap<>();
    private CcaDelegate ccaDelegate = null;

    public CcaCertificateManager(CcaDelegate ccaDelegate) {
        this.init(ccaDelegate);
    }

    private static String extractXMLCertificateSubject(String certificateInputDirectory, String rootCertificate) {
        // Register XmlClasses and Types
        registerXmlClasses();
        registerTypes();

        CcaFileManager ccaFileManager = CcaFileManager.getReference(certificateInputDirectory);

        // Load X.509 root certificate and get Subject principal
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                    ccaFileManager.getFileContent(rootCertificate));
            X509Certificate x509Certificate = (X509Certificate) certificateFactory
                    .generateCertificate(byteArrayInputStream);
            X500Principal x500PrincipalSubject = x509Certificate.getSubjectX500Principal();
            byte[] encodedSubject = x500PrincipalSubject.getEncoded();
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : encodedSubject) {
                stringBuilder.append(String.format("%02x", b));
            }
            return stringBuilder.toString();

        } catch (CertificateException ce) {
            LOGGER.error("Error while either instantiating X.509 CertificateFactory or generating certificate from "
                    + "fileInputStream. " + ce);
            return null;
        }
    }

    public void init(CcaDelegate ccaDelegate) {
        this.ccaDelegate = ccaDelegate;
        for (CcaCertificateType ccaCertificateType : CcaCertificateType.values()) {
            if (ccaCertificateType.getRequiresCaCertAndKeys()) {
                this.certificateKeyMap.put(ccaCertificateType, generateCertificateListFromXML(ccaCertificateType));
            } else if (ccaCertificateType.getRequiresCertificate()) {
                CcaCertificateChain ccaCertificateChain = new CcaCertificateChain();
                ccaCertificateChain.appendEncodedCertificate(ccaDelegate.getClientCertificate());
                this.certificateKeyMap.put(ccaCertificateType, ccaCertificateChain);
            } else {
                CcaCertificateChain ccaCertificateChain = new CcaCertificateChain();
                ccaCertificateChain.appendEncodedCertificate(new byte[0]);
                this.certificateKeyMap.put(ccaCertificateType, ccaCertificateChain);
            }
        }
    }

    public CcaCertificateChain getCertificateChain(CcaCertificateType ccaCertificateType) {
        if (this.certificateKeyMap.containsKey(ccaCertificateType)) {
            return this.certificateKeyMap.get(ccaCertificateType);
        } else {
            LOGGER.error("Entry for " + ccaCertificateType + " is not available in CcaCertificateManager!");
        }
        return null;
    }

    private CcaCertificateChain generateCertificateListFromXML(CcaCertificateType ccaCertificateType) {

        // Declare variables for later use
        String keyName = null;
        String pubKeyName = null;
        String keyType = null;
        Boolean readKey = false;
        String rootCertificate = ccaCertificateType.toString().split("_")[0].toLowerCase() + ".pem";

        String keyDirectory = ccaDelegate.getKeyDirectory() + "/";
        String certificateInputDirectory = ccaDelegate.getCertificateInputDirectory() + "/";
        String certificateOutputDirectory = ccaDelegate.getCertificateOutputDirectory() + "/";

        KeyFileManager keyFileManager = KeyFileManager.getReference();
        try {
            keyFileManager.init(keyDirectory);
        } catch (KeyFileManagerException kfme) {
            LOGGER.error("Failed to initialize KeyFileManager. " + kfme);
        }

        String xmlSubject = extractXMLCertificateSubject(certificateInputDirectory, rootCertificate);

        InputStream inputStream = Attacker.class.getResourceAsStream("/xmlcerts/" + ccaCertificateType.toString()
                + ".xml");
        String xmlString = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();

        if (xmlString == null) {
            return null;
        }

        xmlString = replacePlaceholders(xmlString, rootCertificate, xmlSubject);

        XmlParser xmlParser = new XmlParser(xmlString);
        Asn1XmlContent asn1XmlContent = xmlParser.getAsn1XmlContent();
        Map<String, Asn1Encodable> identifierMap = xmlParser.getIdentifierMap();
        Linker linker = new Linker(identifierMap);

        XmlSignatureEngine xmlSignatureEngine = new XmlSignatureEngine(linker, identifierMap);
        xmlSignatureEngine.computeSignatures();

        List<Asn1Encodable> certificates = asn1XmlContent.getAsn1Encodables();

        CcaCertificateChain ccaCertificateChain = new CcaCertificateChain();
        for (int i = 0; i < certificates.size(); i++) {
            Asn1Encodable certificate = certificates.get(i);
            ccaCertificateChain.appendEncodedCertificate(Asn1EncoderForX509.encodeForCertificate(linker, certificate));
            if (certificate instanceof Asn1Sequence && readKey == false) {
                keyName = ((KeyInfo) ((Asn1Sequence) certificate).getChildren().get(0)).getKeyFile();
                pubKeyName = ((KeyInfo) ((Asn1Sequence) certificate).getChildren().get(0)).getPubKeyFile();
                keyType = ((Asn1Sequence) certificate).getChildren().get(0).getAttribute("keyType");
                readKey = true;
            }
        }

        if (setLeafCertificateKeys(ccaCertificateChain, keyName, pubKeyName, keyType, keyFileManager) == false) {
            return null;
        }

        saveCertificateChainToFile(certificateOutputDirectory, certificates, ccaCertificateChain);

        return ccaCertificateChain;
    }

    /**
     *
     * @param xmlString
     *            Content of the XML file describing the certificate chain.
     * @param rootCertificateKeyName
     *            Name of the root certificates key.
     * @param rootCaSubject
     *            ASN.1 Subject of the root certificate encoded as a hex string.
     * @return The xmlString in which the placeholder for the issuer (which is
     *         the root CA) has been replaced with the hex string encoding the
     *         root CAs subject. Additionally, the key placeholder has been
     *         replaced with the filename of the keyfile of the root CA
     *         certificate.
     */
    private String replacePlaceholders(String xmlString, String rootCertificateKeyName, String rootCaSubject) {
        String needle = "<asn1RawBytes identifier=\"issuer\" type=\"RawBytes\" placeholder=\"replace_me\"><value>";
        String replacement = "<asn1RawBytes identifier=\"issuer\" type=\"RawBytes\"><value>";
        xmlString = xmlString.replace(needle, replacement + rootCaSubject);
        xmlString = xmlString.replace("replace_me_im_a_dummy_key", rootCertificateKeyName);
        return xmlString;
    }

    /**
     * This is a wrapper function to write a generated certificate chain to
     * disk. This is needed since X.509-Attacker still uses a two dimensional
     * byte array for encoded certificates rather than a LinkedList.
     *
     * @param outputDirectory
     * @param certificates
     * @param ccaCertificateChain
     */
    private void saveCertificateChainToFile(String outputDirectory, List<Asn1Encodable> certificates,
            CcaCertificateChain ccaCertificateChain) {
        byte[][] encodedCertificates = new byte[certificates.size()][];
        for (int i = 0; i < ccaCertificateChain.getEncodedCertificates().size(); i++) {
            encodedCertificates[i] = ccaCertificateChain.getEncodedCertificates().get(i);
        }
        try {
            writeCertificates(outputDirectory, certificates, encodedCertificates);
        } catch (IOException ioe) {
            LOGGER.error("Couldn't write certificates to output directory. " + ioe);
        }
    }

    /**
     * Based on the provided parameters this function adds the correct Custom
     * Private/Public Keys to the certificate chain.
     *
     * @param ccaCertificateChain
     * @param keyName
     * @param pubKeyName
     * @param keyType
     * @param keyFileManager
     * @return Boolean indicating if an error occured.
     */
    private boolean setLeafCertificateKeys(CcaCertificateChain ccaCertificateChain, String keyName, String pubKeyName,
            String keyType, KeyFileManager keyFileManager) {
        CustomPrivateKey customPrivateKey;
        CustomPublicKey customPublicKey;
        byte[] keyBytes;
        byte[] pubKeyBytes;
        PrivateKey privateKey;
        CcaCertificateKeyType ccaCertificateKeyType = CcaCertificateKeyType.fromJavaName(keyType.toLowerCase());
        try {
            switch (ccaCertificateKeyType) {
                case RSA:
                    keyBytes = keyFileManager.getKeyFileContent(keyName);

                    privateKey = readPrivateKey(new ByteArrayInputStream(keyBytes));
                    BigInteger modulus = ((RSAPrivateKey) privateKey).getModulus();
                    BigInteger d = ((RSAPrivateKey) privateKey).getPrivateExponent();
                    customPrivateKey = new CustomRSAPrivateKey(modulus, d);

                    pubKeyBytes = keyFileManager.getKeyFileContent(pubKeyName);

                    PublicKey publicKey = PemUtil.readPublicKey(new ByteArrayInputStream(pubKeyBytes));
                    customPublicKey = new CustomRsaPublicKey(((RSAPublicKey) publicKey).getPublicExponent(), modulus);
                    break;
                case DH:
                    keyBytes = keyFileManager.getKeyFileContent(keyName);
                    privateKey = readPrivateKey(new ByteArrayInputStream(keyBytes));

                    pubKeyBytes = keyFileManager.getKeyFileContent(pubKeyName);
                    publicKey = readPublicKey(new ByteArrayInputStream(pubKeyBytes));

                    BigInteger y = ((DHPublicKey) publicKey).getY();
                    BigInteger x = ((DHPrivateKey) privateKey).getX();
                    BigInteger p = ((DHPrivateKey) privateKey).getParams().getP();
                    BigInteger g = ((DHPrivateKey) privateKey).getParams().getG();
                    customPrivateKey = new CustomDHPrivateKey(x, p, g);
                    customPublicKey = new CustomDhPublicKey(p, g, y);
                    break;
                case DSA:
                    keyBytes = keyFileManager.getKeyFileContent(keyName);
                    privateKey = readPrivateKey(new ByteArrayInputStream(keyBytes));
                    publicKey = readPublicKey(new ByteArrayInputStream(keyBytes));

                    BigInteger y2 = ((DSAPublicKey) publicKey).getY();
                    BigInteger x2 = ((DSAPrivateKey) privateKey).getX();
                    BigInteger primeP = ((DSAPrivateKey) privateKey).getParams().getP();
                    BigInteger primeQ = ((DSAPrivateKey) privateKey).getParams().getQ();
                    BigInteger generator = ((DSAPrivateKey) privateKey).getParams().getG();
                    customPrivateKey = new CustomDSAPrivateKey(x2, primeP, primeQ, generator);
                    customPublicKey = new CustomDsaPublicKey(primeP, primeQ, generator, y2);
                    break;
                case ECDSA:
                    keyBytes = keyFileManager.getKeyFileContent(keyName);
                    privateKey = readPrivateKey(new ByteArrayInputStream(keyBytes));
                    pubKeyBytes = keyFileManager.getKeyFileContent(pubKeyName);
                    publicKey = readPublicKey(new ByteArrayInputStream(pubKeyBytes));

                    ECPoint x3 = ((ECPublicKey) publicKey).getW();
                    BigInteger pKey = ((ECPrivateKey) privateKey).getS();
                    NamedGroup nGroup = NamedGroup.getNamedGroup((ECPrivateKey) privateKey);
                    customPrivateKey = new CustomECPrivateKey(pKey, nGroup);
                    customPublicKey = new CustomEcPublicKey(x3.getAffineX(), x3.getAffineY(), nGroup);
                    break;
                default:
                    LOGGER.error("Unknown or unsupported value for keyType attribute of keyInfo in XMLCertificate.");
                    return false;
            }
        } catch (IOException ioe) {
            LOGGER.error("IOException occurred while preparing PrivateKey. " + ioe);
            return false;
        } catch (KeyFileManagerException kfme) {
            LOGGER.error("Couldn't read key from KeyFileManager. " + kfme);
            return false;
        }
        ccaCertificateChain.setLeafCertificatePrivateKey(customPrivateKey);
        ccaCertificateChain.setLeafCertificatePublicKey(customPublicKey);
        return true;
    }

}
