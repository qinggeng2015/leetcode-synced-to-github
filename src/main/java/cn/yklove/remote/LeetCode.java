package cn.yklove.remote;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author qinggeng
 */
public class LeetCode {

    private static Logger logger = LoggerFactory.getLogger(LeetCode.class);

    private final String baseUrl;

    private Map<String, String> cookie;

    public LeetCode(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private String loginUrl = "/accounts/login/";

    private String problemsetAllUrl = "/problemset/all/";

    private String submissionUrl = "/api/submissions/?offset=0&limit=20&lastkey=";

    private String problemsAllUrl = "/api/problems/all/";

    private String graphqlUrl = "/graphql";

    private String submissionsDetailUrl = "/submissions/detail/";

    public boolean login(String username, String password) {

        Connection.Response response = null;
        try {
            response = Jsoup
                    .connect(baseUrl + problemsetAllUrl)
                    .method(Connection.Method.GET)
                    .header("referer", baseUrl + problemsetAllUrl)
                    .header("path", problemsetAllUrl)
                    .followRedirects(true)
                    .execute();
            Map<String, String> cookies = response.cookies();
            response = Jsoup.connect(baseUrl + loginUrl)
                    .cookies(cookies)
                    .data("csrfmiddlewaretoken", getToken(cookies))
                    .data("login", username)
                    .data("password", password)
                    .data("next", "/problems")
                    .header("referer", baseUrl + loginUrl)
                    .header("path", loginUrl)
                    .header("x-csrftoken", getToken(cookies))
                    .method(Connection.Method.POST)
                    .followRedirects(false)
                    .execute();
        } catch (IOException e) {
            logger.error("无法连接到leetcode！url = {}", baseUrl + problemsAllUrl);
            System.exit(0);
        }
        int loginResponseCode = response.statusCode();
        String loginResponseBody = response.body();
        if (loginResponseCode == 302 && StringUtils.isBlank(loginResponseBody)) {
            this.cookie = response.cookies();
            return true;
        }
        return false;
    }

    public Problems getProblems() throws IOException {
        Connection.Response response = Jsoup
                .connect(baseUrl + problemsAllUrl)
                .method(Connection.Method.GET)
                .header("referer", baseUrl + problemsAllUrl)
                .header("path", problemsAllUrl)
                .header("x-csrftoken", getToken(this.cookie))
                .cookies(this.cookie)
                .execute();
        int status = response.statusCode();
        String body = response.body();
        if (200 == status && StringUtils.isNotBlank(body)) {
            Problems problems = JSON.parseObject(body, Problems.class);
            return problems;
        }
        throw new RuntimeException("error");
    }

    public Submissions getSubmissions(String name) throws IOException {
        String graphql = "{\"operationName\":\"Submissions\",\"variables\":{\"offset\":0,\"limit\":20,\"lastKey\":null,\"questionSlug\":\""+name+"\"},\"query\":\"query Submissions($offset: Int!, $limit: Int!, $lastKey: String, $questionSlug: String!) {\\n  submissionList(offset: $offset, limit: $limit, lastKey: $lastKey, questionSlug: $questionSlug) {\\n    lastKey\\n    hasNext\\n    submissions {\\n      id\\n      statusDisplay\\n      lang\\n      runtime\\n      timestamp\\n      url\\n      isPending\\n      memory\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}";
        Connection.Response response = Jsoup
                .connect(baseUrl + graphqlUrl)
                .method(Connection.Method.POST)
                .header("Content-Type", "application/json")
                .header("referer", baseUrl + graphqlUrl)
                .header("path", graphqlUrl)
                .header("Accept", "application/json")
                .header("x-csrftoken", getToken(this.cookie))
                .cookies(this.cookie)
                .requestBody(graphql)
                .ignoreContentType(true)
                .followRedirects(false)
                .execute();
        int status = response.statusCode();
        String body = response.body();
        if (200 == status && StringUtils.isNotBlank(body)) {
            Submissions submissions = JSON.parseObject(body, Submissions.class);
            return submissions;
        }
        throw new RuntimeException();
    }

    public SubmissionDetail getSubmissionDetail(String id) throws IOException {
        Connection.Response response = Jsoup
                .connect(baseUrl + submissionsDetailUrl + id)
                .method(Connection.Method.GET)
                .header("referer", baseUrl + submissionsDetailUrl + id)
                .header("path", submissionsDetailUrl + id)
                .header("x-csrftoken", getToken(this.cookie))
                .cookies(this.cookie)
                .execute();
        int status = response.statusCode();
        String body = response.body();
        if (200 == status && StringUtils.isNotBlank(body)) {
            String pageData = StringUtils.substringBetween(body, "var pageData =", "if (isNaN(pageData.submissionData.status_code))");
            if (StringUtils.isBlank(pageData)) {
                throw new RuntimeException("error");
            }
            pageData = pageData.replaceAll("(\\r\\n|\\r|\\n|\\n\\r)", "");
            pageData = pageData.substring(0, pageData.length() - 1);
            pageData = pageData.replaceAll("status_code: parseInt\\('\\d+', \\d+\\),", "");
            SubmissionDetail submissionDetail = JSON.parseObject(pageData, SubmissionDetail.class);
            return submissionDetail;
        }
        throw new RuntimeException("error");
    }

    private static String getToken(Map<String, String> cookies) {
        for (Map.Entry<String, String> cookie : cookies.entrySet()) {
            if ("csrftoken".equals(cookie.getKey())) {
                return cookie.getValue();
            }
        }
        return null;
    }

}
