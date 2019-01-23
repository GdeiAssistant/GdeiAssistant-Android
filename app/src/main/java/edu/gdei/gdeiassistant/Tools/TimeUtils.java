package edu.gdei.gdeiassistant.Tools;

import java.util.Date;

public class TimeUtils {

    public enum TimeUnit {

        SECOND(0), MINUTES(1), HOUR(2), DAY(3);

        TimeUnit(Integer unit) {
            this.unit = unit;
        }

        private Integer unit;

        public Integer getUnit() {
            return unit;
        }
    }

    /**
     * 两个时间间的时间戳计算函数
     *
     * @param beginDate
     * @param endDate
     * @param timeUnit
     * @return
     */
    public static long GetTimestampDifference(Date beginDate, Date endDate, TimeUnit timeUnit) {
        if (beginDate == null || endDate == null) {
            return 0;
        }
        long millisecond = endDate.getTime() - beginDate.getTime();
        switch (timeUnit.unit) {
            case 0:
                return (millisecond / 1000);
            case 1:
                return (millisecond / (1000 * 60));
            case 2:
                return (millisecond / (1000 * 60 * 60));
            case 3:
                return (millisecond / (1000 * 60 * 60 * 24));
        }
        return 0;
    }
}
