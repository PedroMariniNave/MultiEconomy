package com.zpedroo.multieconomy.scheduler;

import com.zpedroo.multieconomy.objects.category.Category;
import com.zpedroo.multieconomy.objects.category.CategoryItem;
import com.zpedroo.multieconomy.objects.category.Task;
import org.bukkit.Bukkit;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.util.HashMap;
import java.util.Map;

public class SchedulerExecutor implements Job {

    protected static final Map<String, Task> tasks = new HashMap<>(8);

    @Override
    public void execute(JobExecutionContext execution) {
        Task task = tasks.get(execution.getMergedJobDataMap().getString("task"));
        if (task == null) return;

        Category category = task.getCategory();
        if (category == null) return;

        for (CategoryItem categoryItem : category.getItems()) {
            categoryItem.setStockAmount(categoryItem.getMaxStock());
        }

        for (String msg : task.getRestockMessages()) {
            Bukkit.broadcastMessage(msg);
        }
    }
}