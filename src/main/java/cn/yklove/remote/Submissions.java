package cn.yklove.remote;

import lombok.Data;

import java.util.List;

/**
 * @author qinggeng
 */
@Data
public class Submissions {

    private DataBean data;

    @Data
    public static class DataBean {

        private SubmissionListBean submissionList;

        @Data
        public static class SubmissionListBean {

            private String lastKey;
            private boolean hasNext;
            private String __typename;
            private List<SubmissionsBean> submissions;

            @Data
            public static class SubmissionsBean {

                private String id;
                private String statusDisplay;
                private String lang;
                private String runtime;
                private String timestamp;
                private String url;
                private String isPending;
                private String memory;
                private String __typename;

            }
        }
    }
}
