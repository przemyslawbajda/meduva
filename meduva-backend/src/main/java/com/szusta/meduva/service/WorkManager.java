package com.szusta.meduva.service;

import com.szusta.meduva.exception.EntityRecordNotFoundException;
import com.szusta.meduva.model.User;
import com.szusta.meduva.model.WorkHours;
import com.szusta.meduva.payload.WorkHoursPayload;
import com.szusta.meduva.repository.ServiceRepository;
import com.szusta.meduva.repository.UserRepository;
import com.szusta.meduva.repository.WorkHoursRepository;
import com.szusta.meduva.util.TimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
public class WorkManager {

    UserRepository userRepository;
    ServiceRepository serviceRepository;
    WorkHoursRepository workHoursRepository;

    ScheduleChecker scheduleChecker;

    @Autowired
    public WorkManager(UserRepository userRepository,
                       ServiceRepository serviceRepository,
                       WorkHoursRepository workHoursRepository,
                       ScheduleChecker scheduleChecker) {
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.workHoursRepository = workHoursRepository;
        this.scheduleChecker = scheduleChecker;
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
    public WorkHours setWorkHours(User worker, WorkHoursPayload workHoursPayload) {

        Date newWorkStartTime = workHoursPayload.getStartTime();
        Date newWorkEndTime = workHoursPayload.getEndTime();
        boolean collidingVisitsExist =
                hasVisitsBefore(newWorkStartTime, worker)
                && hasVisitsAfter(newWorkEndTime, worker);

        if (!collidingVisitsExist) {
            Date dayStart = TimeUtils.getDayStart(newWorkStartTime);
            Date dayEnd = TimeUtils.getDayEnd(newWorkStartTime);
            workHoursRepository.deleteBetween(dayStart, dayEnd);

            WorkHours workHours = new WorkHours(newWorkStartTime, newWorkEndTime);
            workHours.setWorker(worker);
            return workHoursRepository.save(workHours);
        } else {
            throw new RuntimeException("Cannot set work hours - visits exist before or after requested work hours");
        }
    }

    private boolean hasVisitsBefore(Date newWorkStartTime, User worker) {
        Date dayStart = TimeUtils.getDayStart(newWorkStartTime);
        return !scheduleChecker.isWorkerFreeBeetween(dayStart, newWorkStartTime, worker);
    }

    private boolean hasVisitsAfter(Date newWorkEndTime, User worker) {
        Date dayEnd = TimeUtils.getDayEnd(newWorkEndTime);
        return !scheduleChecker.isWorkerFreeBeetween(newWorkEndTime, dayEnd, worker);
    }
}
