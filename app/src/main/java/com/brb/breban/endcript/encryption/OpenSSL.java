package com.brb.breban.endcript.encryption;

/**
 * Created by breban on 26.05.2017.
 */


import android.util.Base64;
import android.util.Log;

import com.brb.breban.endcript.util.SharedPreferencesUtils;

import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;
import org.spongycastle.crypto.AsymmetricBlockCipher;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.engines.RSAEngine;
import org.spongycastle.crypto.params.AsymmetricKeyParameter;
import org.spongycastle.crypto.params.RSAKeyParameters;
import org.spongycastle.crypto.util.PrivateKeyFactory;
import org.spongycastle.crypto.util.PublicKeyFactory;
import org.spongycastle.openssl.PEMDecryptorProvider;
import org.spongycastle.openssl.PEMEncryptedKeyPair;
import org.spongycastle.openssl.PEMEncryptor;
import org.spongycastle.openssl.PEMParser;
import org.spongycastle.openssl.PEMWriter;
import org.spongycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.spongycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.spongycastle.openssl.jcajce.JcePEMEncryptorBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

public class OpenSSL {

    private static OpenSSL instance = null;
    private AsymmetricBlockCipher decryptingCipher = null;
    private static final String PRIVATE_FILE_NAME= "Private.asc";

    private KeyPair keyPair;
    private String privateKeyPassword = "";

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }
    private OpenSSL() {
    }

    public static OpenSSL getInstance() {
        if (instance == null) {
            instance = new OpenSSL();
        }
        return instance;
    }

    public KeyPair getKeyPair(){
        if(keyPair == null){
            try {
                keyPair = loadKeyPairKeyFromFile(PRIVATE_FILE_NAME , privateKeyPassword);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return keyPair;
    }

    private void initDecryptionCipher() throws IOException {
        if (keyPair == null) {
            this.keyPair = loadKeyPairKeyFromFile(PRIVATE_FILE_NAME ,
                    privateKeyPassword);
        }
        AsymmetricKeyParameter privateKeyParameter = (AsymmetricKeyParameter) PrivateKeyFactory
                .createKey(this.keyPair.getPrivate().getEncoded());
        this.decryptingCipher = new RSAEngine();
        decryptingCipher = new org.spongycastle.crypto.encodings.OAEPEncoding(
                decryptingCipher);
        decryptingCipher.init(false, privateKeyParameter);
    }

    public KeyPair generateKeys(int keysize) throws NoSuchAlgorithmException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(keysize, new SecureRandom());
        return gen.generateKeyPair();
    }

    public void createKeyPair(int keysize) throws NoSuchAlgorithmException,
            IOException {
        this.keyPair = generateKeys(keysize);
        exportKeysToFile(keyPair, "Private.asc", this.privateKeyPassword);
    }

    public void exportKeysToFile(KeyPair keyPair, String privateFile, String password) throws IOException {
        PEMWriter privatepemWriter = new PEMWriter(new FileWriter(new File(SharedPreferencesUtils.getInstance().getContext().getFilesDir(), "Private.asc"), false));

        PEMEncryptor pemEncryptor = (new JcePEMEncryptorBuilder("AES-256-CBC"))
                .build(password.toCharArray());

        privatepemWriter.writeObject(keyPair, pemEncryptor);
        privatepemWriter.close();
    }

    public String exportPublicKeyToString(PublicKey publicKey)
            throws IOException {
        StringWriter stringWritter = new StringWriter();
        PEMWriter privatepemWriter = new PEMWriter(stringWritter);
        privatepemWriter.writeObject(publicKey);
        privatepemWriter.close();
        return new String(stringWritter.getBuffer());
    }

    public String exportPrivateKeyToString(PrivateKey privateKey)
            throws IOException {
        StringWriter stringWritter = new StringWriter();
        PEMWriter privatepemWriter = new PEMWriter(stringWritter);
        privatepemWriter.writeObject(privateKey);
        privatepemWriter.close();
        return new String(stringWritter.getBuffer());
    }

    public PublicKey createPublicKeyFromString(String content)
            throws IOException, NoSuchAlgorithmException,
            InvalidKeySpecException {
        PEMParser pemParser = new PEMParser(new StringReader(content));
        Object object = pemParser.readObject();
        pemParser.close();
        return createPublicKey(object);
    }

    public PublicKey loadPublicKeyFromString(String pubkey) throws IOException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        PEMParser pemParser = new PEMParser(new StringReader(pubkey));
        Object object = pemParser.readObject();
        pemParser.close();
        return createPublicKey(object);
    }

    private PublicKey createPublicKey(Object object) throws IOException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        if (object instanceof SubjectPublicKeyInfo) {
            SubjectPublicKeyInfo pubKeyInfo = (SubjectPublicKeyInfo) object;

            RSAKeyParameters rsa = (RSAKeyParameters) PublicKeyFactory
                    .createKey(pubKeyInfo);
            RSAPublicKeySpec rsaSpec = new RSAPublicKeySpec(rsa.getModulus(),
                    rsa.getExponent());
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(rsaSpec);
        }
        return null;
    }

    public KeyPair loadKeyPairKeyFromFile(String filePath, String passphase)
            throws IOException {
        PEMParser pemParser = new PEMParser(new FileReader(new File(SharedPreferencesUtils.getInstance().getContext().getFilesDir(), filePath)));

        Object object = pemParser.readObject();
        pemParser.close();
        PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder()
                .build(passphase.toCharArray());
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter()
                .setProvider("SC");

        if (object instanceof PEMEncryptedKeyPair) {
            return converter.getKeyPair(((PEMEncryptedKeyPair) object)
                    .decryptKeyPair(decProv));
        }
        return null;
    }

    public String encrypt(PublicKey pubKey, String inputData) {

        String encryptedData = null;
        try {

            AsymmetricKeyParameter publicKey = (AsymmetricKeyParameter) PublicKeyFactory
                    .createKey(pubKey.getEncoded());
            AsymmetricBlockCipher e = new RSAEngine();
            e = new org.spongycastle.crypto.encodings.OAEPEncoding(e);
            e.init(true, publicKey);

            byte[] messageBytes = inputData.getBytes();
            byte[] hexEncodedCipher = e.processBlock(messageBytes, 0,
                    messageBytes.length);

            encryptedData = new String(Base64.encode(hexEncodedCipher,
                    Base64.DEFAULT));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return encryptedData;
    }

    public String decrypt(String encryptedData) throws IOException,
            InvalidCipherTextException {
        if (decryptingCipher == null) {
            initDecryptionCipher();
        }
        byte[] messageBytes = Base64.decode(encryptedData, Base64.DEFAULT);
        byte[] hexDecodedCipher = decryptingCipher.processBlock(messageBytes,
                0, messageBytes.length);

        return new String(hexDecodedCipher);
    }
}
