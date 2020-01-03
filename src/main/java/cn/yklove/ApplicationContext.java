package cn.yklove;

import cn.yklove.remote.*;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author qinggeng
 */
public class ApplicationContext {

    private static Logger logger = LoggerFactory.getLogger(ApplicationContext.class);

    private Config config = new Config();

    private LeetCode leetCode;

    private Git repository;

    public ApplicationContext(Properties properties) {
        initProperties(properties);
        initLeetCode();
    }

    private void initLeetCode() {
        leetCode = new LeetCode(config.getLeetCodeBaseUrl());
    }

    public void start() {

        boolean login = leetCode.login(config.getUsername(), config.getPassword());
        if (!login) {
            logger.error("登录失败，请检查账号密码是否正确！");
            System.exit(0);
        }

        cloneGit();

        // 检查目录
        File source = new File(config.getGitPath() + "/" + config.getLeetCodeSource());
        if (!source.exists()) {
            logger.info("创建目录{}", source.getAbsolutePath());
            source.mkdir();
        }

        // 执行任务

        // 获取题目列表、获取提交记录、保存提交记录、推送代码
        try {
            Problems problems = leetCode.getProblems();
            for (Problems.StatStatusPairsBean stat_status_pair : problems.getStat_status_pairs()) {
                TimeUnit.SECONDS.sleep(1);
                logger.info("题目id:{} 题目名称:{}", stat_status_pair.getStat().getFrontend_question_id(), stat_status_pair.getStat().getQuestion__title());
                String fileName = stat_status_pair.getStat().getFrontend_question_id() + "." + stat_status_pair.getStat().getQuestion__title();
                File file = new File(config.getGitPath() + "/" + config.getLeetCodeSource() + "/" + fileName);
                if (file.exists()) {
                    logger.info("题目id:{} 题目名称:{}在仓库中已存在", stat_status_pair.getStat().getFrontend_question_id(), stat_status_pair.getStat().getQuestion__title());
                    continue;
                }
                Submissions submissions = leetCode.getSubmissions(stat_status_pair.getStat().getQuestion__title_slug());
                for (Submissions.DataBean.SubmissionListBean.SubmissionsBean submission : submissions.getData().getSubmissionList().getSubmissions()) {
                    if ("Accepted".equals(submission.getStatusDisplay())) {
                        logger.info("题目id:{} 题目名称:{}存在正确提交", stat_status_pair.getStat().getFrontend_question_id(), stat_status_pair.getStat().getQuestion__title());
                        SubmissionDetail submissionDetail = leetCode.getSubmissionDetail(submission.getId());
                        file.mkdir();
                        file = new File(config.getGitPath() + "/" + config.getLeetCodeSource() + "/" + fileName + "/" + stat_status_pair.getStat().getQuestion__title() + ".txt");
                        file.createNewFile();
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(("/**\n").getBytes());
                        fos.write((" * status: " + submission.getStatusDisplay()+"\n").getBytes());
                        fos.write((" *\n").getBytes());
                        fos.write((" * time: " + submission.getRuntime()+"\n").getBytes());
                        fos.write((" *\n").getBytes());
                        fos.write((" * memory: " + submission.getMemory()+"\n").getBytes());
                        fos.write((" */\n\n").getBytes());
                        fos.write(submissionDetail.getSubmissionCode().getBytes());
                        break;
                    }
                }
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            logger.error("获取题目列表失败! e = {}", e.getMessage());
        }

        try {
            logger.info("开始提交到git");
            repository.add().addFilepattern(config.getLeetCodeSource()).call();
            repository.commit().setMessage(DateFormat.getDateInstance().format(new Date())).call();
            repository.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(config.getGitUsername(), config.getGitPassword())).call();
        } catch (GitAPIException e) {
            logger.error("git发生错误 e = {}", e);
        }

        // 启动定时器 ，定时执行任务


    }

    private void cloneGit() {
        RepositoryProvider git = new RepositoryProvider(config.getGitRepository(), config.getGitPath(),
                config.getGitUsername(), config.getGitPassword());
        this.repository = git.gitClone();
    }

    private void initProperties(Properties properties) {
        String username = properties.getProperty("leetcode.username");
        if (StringUtils.isBlank(username)) {
            logger.error("leetcode.username配置为空！");
            System.exit(0);
        }
        String password = properties.getProperty("leetcode.password");
        if (StringUtils.isBlank(password)) {
            logger.error("leetcode.password配置为空");
            System.exit(0);
        }
        String gitRepository = properties.getProperty("git.repository");
        if (StringUtils.isBlank(gitRepository)) {
            logger.error("git.repository配置为空！");
            System.exit(0);
        }
        String gitUsername = properties.getProperty("git.username");
        if (StringUtils.isBlank(gitUsername)) {
            logger.error("git.username配置为空！");
            System.exit(0);
        }
        String gitPassword = properties.getProperty("git.password");
        if (StringUtils.isBlank(gitPassword)) {
            logger.error("git.password配置为空！");
            System.exit(0);
        }
        config.setUsername(username);
        config.setPassword(password);
        config.setGitRepository(gitRepository);
        config.setGitUsername(gitUsername);
        config.setGitPassword(gitPassword);
        if (StringUtils.isNotBlank(properties.getProperty("leetcode.url"))) {
            config.setLeetCodeBaseUrl(properties.getProperty("leetcode.url"));
        }
        if (StringUtils.isNotBlank(properties.getProperty("leetcode.source"))) {
            config.setLeetCodeSource(properties.getProperty("leetcode.source"));
        }
    }
}
