package com.szusta.meduva.service;

import com.szusta.meduva.exception.EntityRecordNotFoundException;
import com.szusta.meduva.model.User;
import com.szusta.meduva.model.WorkHours;
import com.szusta.meduva.model.schedule.WorkerSchedule;
import com.szusta.meduva.model.schedule.status.WorkerStatus;
import com.szusta.meduva.model.schedule.status.enums.EWorkerStatus;
import com.szusta.meduva.payload.TimeRange;
import com.szusta.meduva.repository.ServiceRepository;
import com.szusta.meduva.repository.UserRepository;
import com.szusta.meduva.repository.WorkHoursRepository;
import com.szusta.meduva.repository.schedule.worker.WorkerScheduleRepository;
import com.szusta.meduva.repository.schedule.worker.WorkerStatusRepository;
import com.szusta.meduva.util.TimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
public class WorkManager {

    UserRepository userRepository;
    ServiceRepository serviceRepository;
    WorkHoursRepository workHoursRepository;
    WorkerStatusRepository workerStatusRepository;
    WorkerScheduleRepository workerScheduleRepository;

    TermGenerator termGenerator;

    @Autowired
    public WorkManager(UserRepository userRepository,
                       ServiceRepository serviceRepository,
                       WorkHoursRepository workHoursRepository,
                       WorkerStatusRepository workerStatusRepository,
                       WorkerScheduleRepository workerScheduleRepository,
                       TermGenerator termGenerator) {
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.workHoursRepository = workHoursRepository;
        this.workerStatusRepository = workerStatusRepository;
        this.workerScheduleRepository = workerScheduleRepository;
        this.termGenerator = termGenerator;
    }

    public com.szusta.meduva.model.Service[] getWorkerServices(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityRecordNotFoundException("User not found with id : " + userId));

        Set<com.szusta.meduva.model.Service> serviceSet = user.getServices();

        com.szusta.meduva.model.Service[] serviceIdTable = new com.szusta.meduva.model.Service[serviceSet.size()];

        int ItemInTableCounter=0;
        for (com.szusta.meduva.model.Service s : serviceSet) {
            serviceIdTable[ItemInTableCounter++]=s;
        }
        return serviceIdTable;
    }

    @Transactional
    public User assignServicesToWorker(Long userId, Long[] servicesId){
        User user = userRepository.findById(userId).orElseThrow(()-> new EntityRecordNotFoundException("User not found with id : " + userId));

        Set<com.szusta.meduva.model.Service> serviceSet= new HashSet<>();

        for(Long s : servicesId){
            com.szusta.meduva.model.Service serv = serviceRepository.findById(s).orElseThrow(()-> new EntityRecordNotFoundException("Service not found with id : " + s));
            serviceSet.add(serv);
        }

        user.setServices(serviceSet);

        return userRepository.save(user);
    }

    @Transactional
    public WorkHours setWorkHours(User worker, Date newWorkStartTime, Date newWorkEndTime) {
        boolean collidingVisitsExist =
                hasVisitsBefore(newWorkStartTime, worker)
                && hasVisitsAfter(newWorkEndTime, worker);

        if (!collidingVisitsExist) {
            deleteWorkHoursAt(newWorkStartTime, worker);
            WorkHours workHours = new WorkHours(newWorkStartTime, newWorkEndTime);
            workHours.setWorker(worker);
            return workHoursRepository.save(workHours);
        } else {
            throw new RuntimeException("Cannot set work hours - visits exist before or after requested work hours");
        }
    }

    @Transactional
    public WorkerSchedule setAbsenceHours(User worker, Date newAbsenceStartTime, Date newAbsenceEndTime) {
        boolean collidingVisitsExist =
                hasVisitsBefore(newAbsenceStartTime, worker)
                        && hasVisitsAfter(newAbsenceEndTime, worker);

        if(!collidingVisitsExist){
            WorkerSchedule workerSchedule = new WorkerSchedule(worker, newAbsenceStartTime, newAbsenceEndTime);
            WorkerStatus workerStatus = workerStatusRepository.getById(EWorkerStatus.WORKER_ABSENT.getValue());
            workerSchedule.setWorkerStatus(workerStatus);
            return workerScheduleRepository.save(workerSchedule);
        } else {
            throw new RuntimeException("Cannot set absence hours - visits exist before or after requested absence hours");
        }
    }

    private boolean hasVisitsBefore(Date newWorkStartTime, User worker) {
        Date dayStart = TimeUtils.getDayStart(newWorkStartTime);
        return !termGenerator.isWorkerFreeBeetween(dayStart, newWorkStartTime, worker);
    }

    private boolean hasVisitsAfter(Date newWorkEndTime, User worker) {
        Date dayEnd = TimeUtils.getDayEnd(newWorkEndTime);
        return !termGenerator.isWorkerFreeBeetween(newWorkEndTime, dayEnd, worker);
    }

    private void deleteWorkHoursAt(Date dateTime, User worker) {
        Date dayStart = TimeUtils.getDayStart(dateTime);
        Date dayEnd = TimeUtils.getDayEnd(dateTime);
        workHoursRepository.deleteByWorkerIdBetween(worker.getId(), dayStart, dayEnd);
    }

    public List<WorkHours> getWeeklyWorkHours(User worker, Date firstWeekDay, Date lastWeekDay) {
        firstWeekDay = TimeUtils.getDayStart(firstWeekDay);
        lastWeekDay = TimeUtils.getDayEnd(lastWeekDay);
        return workHoursRepository.getAllByWorkerIdBetween(worker.getId(), firstWeekDay, lastWeekDay);
    }

    public List<TimeRange> getWeeklyOffWorkHours(User worker, Date firstWeekDay, Date lastWeekDay) {

        List<WorkHours> weeklyWorkHours = getWeeklyWorkHours(worker, firstWeekDay, lastWeekDay);
        List<TimeRange> weeklyOffWorkHours = convertToOffWorkHours(weeklyWorkHours);

        List<TimeRange> allDayOffWorkHours = getAllDayOffWeeklyWorkHours(worker, firstWeekDay);
        weeklyOffWorkHours.addAll(allDayOffWorkHours);

        return weeklyOffWorkHours;
    }

    private List<TimeRange> convertToOffWorkHours(List<WorkHours> weeklyWorkHours) {
        List<TimeRange> weeklyOffWorkHours = new ArrayList<>();

        weeklyWorkHours.forEach(workHours -> {
            Date dayStart = TimeUtils.getDayStart(workHours.getStartTime());
            Date dayEnd = TimeUtils.getDayEnd(workHours.getStartTime());
            TimeRange timeBeforeWork = new TimeRange(dayStart, workHours.getStartTime());
            TimeRange timeAfterWork = new TimeRange(workHours.getEndTime(), dayEnd);

            weeklyOffWorkHours.add(timeBeforeWork);
            weeklyOffWorkHours.add(timeAfterWork);
        });

        return weeklyOffWorkHours;
    }

    private List<TimeRange> getAllDayOffWeeklyWorkHours(User worker, Date firstWeekDay) {

        List<TimeRange> allDayOffWorkHours = new ArrayList<>();
        Calendar calendar = getFirstWeekDayStart(firstWeekDay);
        for (int dayOfWeek = 1; dayOfWeek < 8; ++dayOfWeek)
        {
            Date currentDayStart = calendar.getTime();
            Date currentDayEnd = TimeUtils.getDayEnd(currentDayStart);

            if (!hasWorkHours(worker, currentDayStart, currentDayEnd)) {
                allDayOffWorkHours.add(
                        new TimeRange(currentDayStart, currentDayEnd)
                );
            }
            calendar.add(Calendar.DATE, 1);
        }

        return allDayOffWorkHours;
    }

    private Calendar getFirstWeekDayStart(Date firstWeekDay) {
        Calendar calendar = Calendar.getInstance();
        Date firstWeekDayStart  = TimeUtils.getDayStart(firstWeekDay);
        calendar.setTime(firstWeekDayStart);
        return calendar;
    }

    private boolean hasWorkHours(User worker, Date start, Date end) {
        List<WorkHours> workHours = workHoursRepository.getAllByWorkerIdBetween(worker.getId(), start, end);
        return !workHours.isEmpty();
    }


}
