package com.onejane.elasticsearch.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @program: springboot-elasticsearch
 * @description: 日期基本工具类
 * @author: OneJane
 * @create: 2020-01-13 09:19
 **/
public class DateUtil {
    /**
     * 获取随机日期
     * @param beginDate 起始日期
     * @param endDate 结束日期
     * @return
     */
    public static Date randomDate(String beginDate,String endDate){
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date start = format.parse(beginDate);
            Date end = format.parse(endDate);

            if(start.getTime() >= end.getTime()){
                return null;
            }

            long date = random(start.getTime(),end.getTime());

            return new Date(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static long random(long begin,long end){
        long rtn = begin + (long)(Math.random() * (end - begin));
        if(rtn == begin || rtn == end){
            return random(begin,end);
        }
        return rtn;
    }
}
