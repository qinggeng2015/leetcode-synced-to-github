package cn.yklove.remote;

import lombok.Data;

import java.util.List;

/**
 * @author qinggeng
 */
@Data
public class SubmissionDetail {

    private String questionId;
    private String memory;
    private SubmissionDataBean submissionData;
    private String runtimeDistributionFormatted;
    private String enableMemoryDistribution;
    private String runtime;
    private String nonSufficientMsg;
    private String sessionId;
    private String memoryDistributionFormatted;
    private String submissionCode;
    private String editCodeUrl;
    private String getLangDisplay;
    private String checkUrl;
    private List<?> langs;

    @Data
    public static class SubmissionDataBean {

        private String input;
        private String memory;
        private String expected_output;
        private String total_correct;
        private String runtime;
        private String total_testcases;
        private String input_formatted;
        private String compare_result;
        private String code_output;

    }
}
