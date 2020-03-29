
// Author: Pierce Brooks

package com.piercelbrooks.common;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ValidCertificate extends X509Certificate {
    private static final String TAG = "PLB-ValidCert";
    private static final long RANGE = TimeUnit.SECONDS.convert(2, TimeUnit.DAYS);

    private Date now;

    public ValidCertificate(Date now) {
        this.now = now;
    }

    @Override
    public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {

    }

    @Override
    public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {

    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public BigInteger getSerialNumber() {
        return null;
    }

    @Override
    public Principal getIssuerDN() {
        return null;
    }

    @Override
    public Principal getSubjectDN() {
        return null;
    }

    @Override
    public Date getNotBefore() {
        return new Date(now.getTime()-RANGE);
    }

    @Override
    public Date getNotAfter() {
        return new Date(now.getTime()+RANGE);
    }

    @Override
    public byte[] getTBSCertificate() throws CertificateEncodingException {
        return new byte[0];
    }

    @Override
    public byte[] getSignature() {
        return new byte[0];
    }

    @Override
    public String getSigAlgName() {
        return null;
    }

    @Override
    public String getSigAlgOID() {
        return null;
    }

    @Override
    public byte[] getSigAlgParams() {
        return new byte[0];
    }

    @Override
    public boolean[] getIssuerUniqueID() {
        return new boolean[0];
    }

    @Override
    public boolean[] getSubjectUniqueID() {
        return new boolean[0];
    }

    @Override
    public boolean[] getKeyUsage() {
        return new boolean[0];
    }

    @Override
    public int getBasicConstraints() {
        return 0;
    }

    @Override
    public byte[] getEncoded() throws CertificateEncodingException {
        return new byte[0];
    }

    @Override
    public void verify(PublicKey publicKey) throws CertificateException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {

    }

    @Override
    public void verify(PublicKey publicKey, String s) throws CertificateException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {

    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public PublicKey getPublicKey() {
        return null;
    }

    @Override
    public boolean hasUnsupportedCriticalExtension() {
        return false;
    }

    @Override
    public Set<String> getCriticalExtensionOIDs() {
        return null;
    }

    @Override
    public Set<String> getNonCriticalExtensionOIDs() {
        return null;
    }

    @Override
    public byte[] getExtensionValue(String s) {
        return new byte[0];
    }
}
