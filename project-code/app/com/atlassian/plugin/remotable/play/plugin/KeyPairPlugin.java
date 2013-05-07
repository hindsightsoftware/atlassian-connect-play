package com.atlassian.plugin.remotable.play.plugin;

import com.atlassian.plugin.remotable.play.util.Environment;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import play.Application;
import play.Play;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static com.atlassian.plugin.remotable.play.util.Utils.LOGGER;
import static java.lang.String.format;

public class KeyPairPlugin extends AbstractPlugin
{
    static final String PUBLIC_KEY_PEM = "public-key.pem";
    static final String PRIVATE_KEY_PEM = "private-key.pem";
    static final Charset UTF_8 = Charset.forName("UTF-8");

    public KeyPairPlugin(Application application)
    {
        super(application);
    }

    @Override
    public void onStart()
    {
        handleKeyPair();
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
            if (Play.isDev())
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
            final Ap3KeyPair<CharSequence> pair = new Base64KeyPair(new ByteArrayKeyPair(new KeyAp3KeyPair(getRsaKeyPair())));
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

    private KeyPair getRsaKeyPair()
    {
        final KeyPairGenerator generator;
        try
        {
            generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(1024, createFixedRandom());
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("Can't find RSA!");
        }
        return generator.generateKeyPair();
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

    private static class KeyAp3KeyPair implements Ap3KeyPair<Key>
    {
        private final KeyPair kp;

        public KeyAp3KeyPair(KeyPair kp)
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

    private static final class ByteArrayKeyPair implements Ap3KeyPair<byte[]>
    {
        private final Ap3KeyPair<Key> kp;

        private ByteArrayKeyPair(Ap3KeyPair<Key> kp)
        {
            this.kp = kp;
        }

        @Override
        public byte[] getPublicKey()
        {
            return getEncoded(kp.getPublicKey());
        }

        @Override
        public byte[] getPrivateKey()
        {
            final Key privateKey = kp.getPrivateKey();
            return getEncoded(privateKey);
        }

        private byte[] getEncoded(Key key)
        {
            return key.getEncoded();
        }
    }

    private static final class Base64KeyPair implements Ap3KeyPair<CharSequence>
    {
        private final Ap3KeyPair<byte[]> kp;

        private Base64KeyPair(Ap3KeyPair<byte[]> kp)
        {
            this.kp = kp;
        }

        @Override
        public CharSequence getPublicKey()
        {
            return encode(kp.getPublicKey());
        }

        @Override
        public CharSequence getPrivateKey()
        {
            return encode(kp.getPrivateKey());
        }

        private String encode(byte[] key)
        {
            return BaseEncoding.base64().encode(key);
        }
    }

    private static final class WithDelimitorsKeyPair implements Ap3KeyPair<CharSequence>
    {
        private final Ap3KeyPair<CharSequence> kp;

        private WithDelimitorsKeyPair(Ap3KeyPair<CharSequence> kp)
        {
            this.kp = kp;
        }

        @Override
        public CharSequence getPublicKey()
        {
            return "-----BEGIN PUBLIC KEY-----\n" + format(kp.getPublicKey()) + "\n-----END PUBLIC KEY-----";
        }

        @Override
        public CharSequence getPrivateKey()
        {
            final CharSequence privateKey = kp.getPrivateKey();
            return "-----BEGIN RSA PRIVATE KEY-----\n" + format(privateKey) + "\n-----END RSA PRIVATE KEY-----";
        }

        private String format(CharSequence key)
        {
            return Joiner.on('\n').join(Splitter.fixedLength(65).split(key));
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
}
