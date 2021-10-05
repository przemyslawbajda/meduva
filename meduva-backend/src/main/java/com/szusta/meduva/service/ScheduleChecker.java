package com.szusta.meduva.service;

import com.szusta.meduva.exception.EntityRecordNotFoundException;
import com.szusta.meduva.model.Service;
import com.szusta.meduva.model.User;
import com.szusta.meduva.model.schedule.Schedule;
import com.szusta.meduva.payload.Term;
import com.szusta.meduva.repository.RoomRepository;
import com.szusta.meduva.repository.UserRepository;
import com.szusta.meduva.repository.equipment.EquipmentItemRepository;
import com.szusta.meduva.repository.schedule.EquipmentScheduleRepository;
import com.szusta.meduva.repository.schedule.RoomScheduleRepository;
import com.szusta.meduva.repository.schedule.WorkerScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class ScheduleChecker {

    private EquipmentItemRepository equipmentItemRepository;
    private RoomRepository roomRepository;
    private UserRepository userRepository;

    private EquipmentScheduleRepository equipmentScheduleRepository;
    private RoomScheduleRepository roomScheduleRepository;
    private WorkerScheduleRepository workerScheduleRepository;

    @Autowired
    public ScheduleChecker(EquipmentItemRepository equipmentItemRepository,
                           RoomRepository roomRepository, UserRepository userRepository,
                           EquipmentScheduleRepository equipmentScheduleRepository,
                           RoomScheduleRepository roomScheduleRepository,
                           WorkerScheduleRepository workerScheduleRepository) {
        this.equipmentItemRepository = equipmentItemRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.equipmentScheduleRepository = equipmentScheduleRepository;
        this.roomScheduleRepository = roomScheduleRepository;
        this.workerScheduleRepository = workerScheduleRepository;
    }

    public Optional<Term> getTermForCurrentWorker(Service service, Calendar currentlyCheckedTime) {

        Date currentCheckStart = currentlyCheckedTime.getTime();
        Date currentCheckEnd = getIntervalEnd(currentlyCheckedTime, service.getDurationInMin());
        // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // String formattedCheckStart = dateFormat.format(currentCheckStart);
        // String formattedCheckEnd = dateFormat.format(currentCheckEnd);

        Long workerId = getCurrentUserId();
        Long serviceId = service.getId();

        // check if worker is not busy
        List<Schedule> existingWorkerEvents =
                (List<Schedule>) workerScheduleRepository.findAnyBetween(currentCheckStart, currentCheckEnd, workerId);

        if (existingWorkerEvents.isEmpty()) {
            // check for free room
            /*
            List<Room> rooms = roomRepository.findAll();
            for (Room room : rooms) {
                List<Schedule> existingRoomEvents =
                        (List<Schedule>) roomScheduleRepository.findAnyBetween()
            }

             */

            Term term = new Term(currentCheckStart, currentCheckEnd);
            return Optional.of(term);
        } else {
            return Optional.empty();
        }
    }

    private Date getIntervalEnd(Calendar currentlyCheckedTime, int serviceDurationInMinutes) {
        Calendar temp = (Calendar) currentlyCheckedTime.clone();
        temp.add(Calendar.MINUTE, serviceDurationInMinutes);
        return temp.getTime();
    }

    private Long getCurrentUserId() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByLogin(userDetails.getUsername())
                .orElseThrow(() -> {
                    String errorMsg = "Authenticated user couldn't be found in the database (login : " + userDetails.getUsername() + ")";
                    return new EntityRecordNotFoundException(errorMsg);
                });
        return user.getId();
    }
}
