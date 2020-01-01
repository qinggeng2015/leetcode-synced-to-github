package cn.yklove;

import lombok.Data;

/**
 * @author qinggeng
 */
@Data
public class Config {

    private String username;

    private String password;

    private String leetCodeBaseUrl = "https://leetcode-cn.com";

    private String leetCodeSource = "solutions";

    private String gitRepository;

    private String gitUsername;

    private String gitPath = "git";

    private String gitPassword;
}
