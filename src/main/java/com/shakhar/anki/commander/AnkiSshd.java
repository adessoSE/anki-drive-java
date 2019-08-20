package com.shakhar.anki.commander;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

public class AnkiSshd {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnkiSshd.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        SshServer sshd = SshServer.setUpDefaultServer();
        sshd.setPort(6000);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(Paths.get("hostkey.ser")));
        sshd.setPasswordAuthenticator(((username, password, session) -> password.equals(username + "pass")));
        sshd.setShellFactory(() -> new AnkiShell());
        sshd.start();
        LOGGER.info("SSH Server started");
        Thread.sleep(Long.MAX_VALUE);
    }
}
