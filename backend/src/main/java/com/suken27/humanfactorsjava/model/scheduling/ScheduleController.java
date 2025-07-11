package com.suken27.humanfactorsjava.model.scheduling;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.slack.api.bolt.App;
import com.suken27.humanfactorsjava.model.controller.ModelController;
import com.suken27.humanfactorsjava.model.dto.TeamDto;
import com.suken27.humanfactorsjava.slack.SlackBlockBuilder;

@Controller
public class ScheduleController {

    @Autowired
    private Scheduler scheduler;

    public void scheduleJob(String name, String cronExpression, ModelController modelController, App slackApp, SlackBlockBuilder slackBlockBuilder, TeamDto team, String teamManagerEmail) throws SchedulerException {
        scheduler.deleteJob(new JobKey(name));
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("modelController", modelController);
        jobDataMap.put("slackApp", slackApp);
        jobDataMap.put("slackBlockBuilder", slackBlockBuilder);
        jobDataMap.put("team", team);
        jobDataMap.put("teamManagerEmail", teamManagerEmail);
        JobDetail jobDetail = createJobDetail(name, jobDataMap);
        CronTrigger trigger = createTrigger(jobDetail, cronExpression);
        scheduler.scheduleJob(jobDetail, trigger);
    }

    private JobDetail createJobDetail(String name, JobDataMap jobDataMap) {
        return JobBuilder.newJob(QuestionSendingJob.class).storeDurably().withIdentity(name).setJobData(jobDataMap).build();
    }

    private CronTrigger createTrigger(JobDetail jobDetail, String cronExpression) {
        return TriggerBuilder.newTrigger().forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), jobDetail.getKey().getGroup())
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).build();
    }

}
