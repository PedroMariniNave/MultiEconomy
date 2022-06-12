package com.zpedroo.multieconomy.objects.category;

import lombok.Data;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;

import java.util.Date;
import java.util.List;

@Data
public class Task {

    private final Category category;
    private final List<String> restockMessages;
    private final CronScheduleBuilder cronScheduleBuilder;
    private CronTrigger cronTrigger;

    public long getNextFireTimeInMillis() {
        return cronTrigger.getFireTimeAfter(new Date()).getTime();
    }
}