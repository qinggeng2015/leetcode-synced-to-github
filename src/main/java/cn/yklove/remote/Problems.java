package cn.yklove.remote;

import lombok.Data;

import java.util.List;

/**
 * @author qinggeng
 */
@Data
public class Problems {

    private String user_name;
    private int num_solved;
    private int num_total;
    private int ac_easy;
    private int ac_medium;
    private int ac_hard;
    private int frequency_high;
    private int frequency_mid;
    private String category_slug;
    private List<StatStatusPairsBean> stat_status_pairs;

    @Data
    public static class StatStatusPairsBean {
        private StatBean stat;
        private Object status;
        private DifficultyBean difficulty;
        private boolean paid_only;
        private boolean is_favor;
        private int frequency;
        private int progress;

        @Data
        public static class StatBean {

            private int question_id;
            private String question__title;
            private String question__title_slug;
            private boolean question__hide;
            private int total_acs;
            private int total_submitted;
            private int total_column_articles;
            private String frontend_question_id;
            private boolean is_new_question;

        }

        @Data
        public static class DifficultyBean {

            private int level;

        }
    }
}
