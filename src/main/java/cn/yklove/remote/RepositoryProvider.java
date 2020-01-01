package cn.yklove.remote;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class RepositoryProvider {
    Logger logger = LoggerFactory.getLogger(RepositoryProvider.class);

    private String repoPath;
    private String clientPath;
    private String username;
    private String password;

    public RepositoryProvider(String repoPath, String clientPath, String username, String password) {
        this.repoPath = repoPath;
        this.clientPath = clientPath;
        this.username = username;
        this.password = password;
    }

    public Git gitClone() {
        File client = new File(clientPath);
        try {
            if (client.exists()) {
                return Git.open(client);
            }
            Git result = Git.cloneRepository()
                    .setURI(repoPath)
                    .setDirectory(client)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
                    .call();
            return result;
        } catch (Exception e) {
            logger.error("克隆仓库失败！e = {}", e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
        return null;
    }
}