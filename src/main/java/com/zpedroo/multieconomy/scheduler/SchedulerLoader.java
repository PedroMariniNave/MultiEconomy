package com.zpedroo.multieconomy.scheduler;

import com.zpedroo.multieconomy.managers.DataManager;
import com.zpedroo.multieconomy.objects.category.Category;
import com.zpedroo.multieconomy.objects.category.Task;
import com.zpedroo.multieconomy.utils.color.Colorize;
import org.bukkit.configuration.file.FileConfiguration;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.List;

public class SchedulerLoader extends SchedulerExecutor {

    private static SchedulerLoader instance;
    public static SchedulerLoader getInstance() { return instance; }

    private final SchedulerFactory factory = new StdSchedulerFactory();

    public SchedulerLoader() {
        instance = this;
    }

    public void startAllCategoriesScheduler() throws SchedulerException {
        Scheduler scheduler = factory.getScheduler();
        scheduler.start();

        for (Category category : DataManager.getInstance().getCache().getCategories().values()) {
            Task task = buildCategoryTask(category);
            if (task == null) continue;

            String identifier = category.getFile().getName().replace(".yml", "");

            JobDetail detail = JobBuilder.newJob(SchedulerExecutor.class).withIdentity(identifier).usingJobData("task", identifier).build();
            CronTrigger trigger = TriggerBuilder.newTrigger().withSchedule(task.getCronScheduleBuilder()).forJob(detail).build();

            scheduler.scheduleJob(detail, trigger);
            tasks.put(identifier, task);

            task.setCronTrigger(trigger);
            category.setTask(task);
        }
    }

    public void stopAllSchedulers() throws SchedulerException {
        Scheduler scheduler = factory.getScheduler();
        scheduler.clear();
    }

    private Task buildCategoryTask(Category category) {
        FileConfiguration file = category.getFileConfiguration();
        String restockDate = file.getString("Settings.restock-date", null);
        if (restockDate == null || restockDate.isEmpty()) return null;

        List<String> restockMessages = Colorize.getColored(file.getStringList("Settings.restock-messages"));
        String formattedDate = parseDate(restockDate);

        return new Task(category, restockMessages, CronScheduleBuilder.cronSchedule(formattedDate));
    }

    private String parseDate(String date) {
        String[] split = date.split(":");

        String rawDayValue = split[0];
        String day = rawDayValue.equalsIgnoreCase("EVERYDAY") ? "*" : rawDayValue.substring(0, 3);

        String hour = split[1];
        String minute = split[2];

        String dateModel = "0 M H ? * D";
        return dateModel.replace("M", minute).replace("H", hour).replace("D", day);
    }
}