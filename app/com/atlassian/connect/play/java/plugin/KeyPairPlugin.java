package com.atlassian.connect.play.java.plugin;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.ConfigurationException;
import com.atlassian.connect.play.java.util.Environment;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import org.apache.commons.lang3.RandomStringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import play.Application;
import play.Play;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;

import static com.atlassian.connect.play.java.util.Utils.LOGGER;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public class KeyPairPlugin extends AbstractPlugin
{
    private static final String BOUNCY_CASTLE_PROVIDER = "BC";
    private static final String RSA = "RSA";
    private static final String SHA_1_PRNG = "SHA1PRNG";
    private static final String SHA_1_WITH_RSA = "SHA1withRSA";

    static final String PUBLIC_KEY_PEM = "public-key.pem";
    static final String PRIVATE_KEY_PEM = "private-key.pem";
    static final Charset UTF_8 = Charset.forName("UTF-8");

    static
    {
        if (Security.getProvider(BOUNCY_CASTLE_PROVIDER) == null)
        {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public KeyPairPlugin(Application application)
    {
        super(application);
    }

    @Override
    public void onStart()
    {
        handleKeyPair();
        checkKeyPair();
        updateGitIgnore();
    }

    private void updateGitIgnore()
    {
        final File gitIgnore = new File(".gitignore");

        boolean hasPublicKeyIgnore = false;
        boolean hasPrivateKeyIgnore = false;
        if (gitIgnore.exists())
        {
            hasPublicKeyIgnore = hasLine(gitIgnore, PUBLIC_KEY_PEM);
            hasPrivateKeyIgnore = hasLine(gitIgnore, PRIVATE_KEY_PEM);
        }
        else
        {
            try
            {
                gitIgnore.createNewFile();
            }
            catch (IOException e)
            {
                LOGGER.error("Could not create .gitignore file.");
                LOGGER.debug("Here is why:", e);
                return;
            }
        }

        if (!hasPublicKeyIgnore)
        {
            addLine(gitIgnore, PUBLIC_KEY_PEM);
        }
        if (!hasPrivateKeyIgnore)
        {
            addLine(gitIgnore, PRIVATE_KEY_PEM);
        }
        if (!hasPrivateKeyIgnore || !hasPublicKeyIgnore)
        {
            LOGGER.info("Added key pair file to .gitignore");
        }
    }

    private void addLine(File gitIgnore, String line)
    {
        try
        {
            Files.append("\n" + line, gitIgnore, UTF_8);
        }
        catch (IOException e)
        {
            LOGGER.error("Could not append content to .gitignore");
            LOGGER.debug("Here is why:", e);
        }
    }

    private Boolean hasLine(File file, String expectedLine)
    {
        try
        {
            return Files.readLines(file, UTF_8, new HasLineProcessor(expectedLine));
        }
        catch (IOException e)
        {
            return false;
        }
    }

    private void handleKeyPair()
    {
        final String publicKey = Environment.getOptionalEnv(Environment.OAUTH_LOCAL_PUBLIC_KEY, null);
        final String privateKey = Environment.getOptionalEnv(Environment.OAUTH_LOCAL_PRIVATE_KEY, null);

        if (publicKey == null || privateKey == null)
        {
            if (AC.isDev())
            {
                LOGGER.info(format("Generating key pair for OAuth signing, into %s and %s files.", PRIVATE_KEY_PEM, PUBLIC_KEY_PEM));
                LOGGER.warn("Do NOT add those files into your VCS.");
                LOGGER.info("In production, you will have to define the two following environment variables:");
                LOGGER.info("\t* " + Environment.OAUTH_LOCAL_PUBLIC_KEY);
                LOGGER.info("\t* " + Environment.OAUTH_LOCAL_PRIVATE_KEY);

                generateKeyFiles();
            }
        }
        else
        {
            LOGGER.debug("Using defined environment properties for OAuth:");
            LOGGER.debug("\t* " + Environment.OAUTH_LOCAL_PUBLIC_KEY);
            LOGGER.debug("\t* " + Environment.OAUTH_LOCAL_PRIVATE_KEY);
        }
    }

    private void generateKeyFiles()
    {
        final File privateKeyFile = new File(PRIVATE_KEY_PEM);
        final File publicKeyFile = new File(PUBLIC_KEY_PEM);

        if (!privateKeyFile.exists() && !publicKeyFile.exists())
        {
            final AcKeyPair<CharSequence> pair = newKeyPair();
            writeKey(pair.getPrivateKey(), privateKeyFile);
            writeKey(pair.getPublicKey(), publicKeyFile);

            LOGGER.info("Generated public/private key pair.");
            LOGGER.debug(format("Public key is available at '%s'", publicKeyFile.getAbsolutePath()));
            LOGGER.debug(format("Private key is available at '%s'", privateKeyFile.getAbsolutePath()));
        }
        else if (!publicKeyFile.exists())
        {
            LOGGER.warn("Private key file exists, but couldn't find corresponding public key file. Not generating any new files.");
            LOGGER.debug(format("Private key file is at '%s'", privateKeyFile.getAbsolutePath()));
            LOGGER.debug(format("Expected public key file at '%s'", publicKeyFile.getAbsolutePath()));
        }
        else if (!privateKeyFile.exists())
        {
            LOGGER.warn("Public key file exists, but couldn't find corresponding private key file. Not generating any new files.");
            LOGGER.debug(format("Public key file is at '%s'", publicKeyFile.getAbsolutePath()));
            LOGGER.debug(format("Expected private key file at '%s'", privateKeyFile.getAbsolutePath()));
        }
        else
        {
            LOGGER.debug("Key files exists. Not generating any new files.");
            LOGGER.debug(format("Public key file is at '%s'", publicKeyFile.getAbsolutePath()));
            LOGGER.debug(format("Private key file is at '%s'", privateKeyFile.getAbsolutePath()));
        }
    }

    private void checkKeyPair()
    {
        try
        {
            final PrivateKey privateKey = getPrivateKey();
            final PublicKey publicKey = getPublicKey();

            final Signature signature = getSignature();

            final byte[] message = RandomStringUtils.random(10).getBytes();
            final byte[] sigBytes = sign(signature, privateKey, message);
            final boolean verify = verify(signature, publicKey, message, sigBytes);

            if (!verify)
            {
                throw new ConfigurationException("Checking key pair failed. It seems the public and private key don't" +
                        " belong to the same key pair. Please check your configuration. We're using the following keys," +
                        " is that what you expected?\n" +
                        AC.publicKey.get() + "\n" +
                        AC.privateKey.get());
            }
        }
        catch (InvalidKeyException | SignatureException e)
        {
            throw new ConfigurationException("Couldn't check key pair because of the following exception.", e);
        }
    }

    private boolean verify(Signature signature, PublicKey publicKey, byte[] message, byte[] signatureBytes) throws InvalidKeyException, SignatureException
    {
        signature.initVerify(publicKey);
        signature.update(message);
        return signature.verify(signatureBytes);
    }

    private byte[] sign(Signature signature, PrivateKey key, byte[] message) throws InvalidKeyException, SignatureException
    {
        signature.initSign(key, getSecureRandom());
        signature.update(message);

        return signature.sign();
    }

    private Signature getSignature() throws InvalidKeyException
    {
        final Signature signature;
        try
        {
            signature = Signature.getInstance(SHA_1_WITH_RSA, BOUNCY_CASTLE_PROVIDER);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException("Can't find algorithm: " + SHA_1_WITH_RSA, e);
        }
        catch (NoSuchProviderException e)
        {
            throw new IllegalStateException("Can't find provider: " + BOUNCY_CASTLE_PROVIDER, e);
        }
        return signature;
    }

    private PublicKey getPublicKey()
    {
        final PEMReader publicPemReader = new PEMReader(new StringReader(AC.publicKey.get()));
        try
        {
            return ((PublicKey) publicPemReader.readObject());
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Can't happen, we use a StringReader. Or have I missed something?", e);
        }
    }

    private PrivateKey getPrivateKey()
    {
        final PEMReader privatePemReader = new PEMReader(new StringReader(AC.privateKey.get()));
        try
        {
            return ((KeyPair) privatePemReader.readObject()).getPrivate();
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Can't happen, we use a StringReader. Or have I missed something?", e);
        }
    }

    private static AcKeyPair<CharSequence> newKeyPair()
    {
        return new PemKeyPair(new KeyAcKeyPair(getRsaKeyPair()));
    }

    private void writeKey(CharSequence key, File file)
    {
        LOGGER.debug(format("Writing key:\n%s\n", key));
        try
        {
            Files.write(key, file, UTF_8);
        }
        catch (IOException e)
        {
            LOGGER.error(format("There was an error writing key to '%s'", file.getAbsolutePath()), e);
        }
    }

    private static KeyPair getRsaKeyPair()
    {
        final KeyPairGenerator generator = getKeyPairGenerator();
        generator.initialize(1024, getSecureRandom());
        return generator.generateKeyPair();
    }

    private static KeyPairGenerator getKeyPairGenerator()
    {
        try
        {
            return KeyPairGenerator.getInstance(RSA, BOUNCY_CASTLE_PROVIDER);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException("Can't find algorithm: " + RSA, e);
        }
        catch (NoSuchProviderException e)
        {
            throw new IllegalStateException("Can't find provider: " + BOUNCY_CASTLE_PROVIDER, e);
        }
    }

    private static SecureRandom getSecureRandom()
    {
        try
        {
            return SecureRandom.getInstance(SHA_1_PRNG);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException("Could not find algorithm for secure random: " + SHA_1_PRNG, e);
        }
    }

    private static SecureRandom createFixedRandom()
    {
        return new FixedRand();
    }

    private static final class FixedRand extends SecureRandom
    {

        private final MessageDigest sha;
        private byte[] state;

        private FixedRand()
        {
            try
            {
                this.sha = MessageDigest.getInstance("SHA-1");
                this.state = sha.digest();
            }
            catch (NoSuchAlgorithmException e)
            {
                throw new RuntimeException("Can't find SHA-1!");
            }
        }

        public void nextBytes(byte[] bytes)
        {
            int off = 0;
            sha.update(state);

            while (off < bytes.length)
            {
                state = sha.digest();
                if (bytes.length - off > state.length)
                {
                    System.arraycopy(state, 0, bytes, off, state.length);
                }
                else
                {
                    System.arraycopy(state, 0, bytes, off, bytes.length - off);
                }

                off += state.length;
                sha.update(state);
            }
        }
    }

    private static class KeyAcKeyPair implements AcKeyPair<Key>
    {
        private final KeyPair kp;

        public KeyAcKeyPair(KeyPair kp)
        {
            this.kp = kp;
        }

        @Override
        public Key getPublicKey()
        {
            return kp.getPublic();
        }

        @Override
        public Key getPrivateKey()
        {
            return kp.getPrivate();
        }
    }

    private static final class PemKeyPair implements AcKeyPair<CharSequence>
    {
        private final AcKeyPair<Key> kp;

        private PemKeyPair(AcKeyPair<Key> kp)
        {
            this.kp = checkNotNull(kp);
        }

        @Override
        public CharSequence getPublicKey()
        {
            return getKeyAsPem(kp.getPublicKey());
        }

        @Override
        public CharSequence getPrivateKey()
        {
            return getKeyAsPem(kp.getPrivateKey());
        }

        private String getKeyAsPem(Key key)
        {
            try (Writer sw = new StringWriter())
            {
                try (PEMWriter pemWriter = new PEMWriter(sw))
                {
                    pemWriter.writeObject(key);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }

                return sw.toString();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private static class HasLineProcessor implements LineProcessor<Boolean>
    {
        private final String expectedLine;
        private boolean hasLine;

        public HasLineProcessor(String expectedLine)
        {
            this.expectedLine = expectedLine;
            hasLine = false;
        }

        @Override
        public boolean processLine(String line) throws IOException
        {
            hasLine = line.equals(expectedLine);
            return !hasLine;
        }

        @Override
        public Boolean getResult()
        {
            return hasLine;
        }
    }

    // a main to generate keys manually
    public static void main(String[] args)
    {
        final AcKeyPair<CharSequence> keyPair = newKeyPair();
        System.out.println(keyPair.getPublicKey());
        System.out.println(keyPair.getPrivateKey());
    }
}
