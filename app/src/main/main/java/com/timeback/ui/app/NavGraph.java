package com.timeback.ui.app;

/**
 * Navigation 경로 상수 정의.
 * Navigation Component의 destination ID 대신 문자열 상수를 관리한다.
 */
public final class NavGraph {

    private NavGraph() {}

    public static final class Routes {
        public static final String PERMISSION = "permission";
        public static final String HOME = "home";
        public static final String TIMELINE = "timeline";
        public static final String APPS = "apps";
        public static final String RECOVERY = "recovery";
        public static final String GOALS = "goals";
        public static final String REPORT = "report";
        public static final String DATA_MANAGEMENT = "data_management";

        private Routes() {}
    }
}
