package com.szusta.meduva.service.equipment;

import com.szusta.meduva.exception.EntityRecordNotFoundException;
import com.szusta.meduva.model.equipment.EquipmentItem;
import com.szusta.meduva.model.schedule.EquipmentSchedule;
import com.szusta.meduva.model.schedule.status.EquipmentStatus;
import com.szusta.meduva.model.schedule.status.enums.EEquipmentStatus;
import com.szusta.meduva.payload.TimeRange;
import com.szusta.meduva.repository.schedule.equipment.EquipmentScheduleRepository;
import com.szusta.meduva.repository.schedule.equipment.EquipmentStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ItemScheduleManager {

    private EquipmentStatusRepository equipmentStatusRepository;
    private EquipmentScheduleRepository equipmentScheduleRepository;

    @Autowired
    public ItemScheduleManager(EquipmentStatusRepository equipmentStatusRepository,
                               EquipmentScheduleRepository equipmentScheduleRepository) {
        this.equipmentStatusRepository = equipmentStatusRepository;
        this.equipmentScheduleRepository = equipmentScheduleRepository;
    }

    public EquipmentSchedule setUnavailability(EquipmentItem eqItem, TimeRange allDay) {
        Long eqStatusId = EEquipmentStatus.EQUIPMENT_UNAVAILABLE.getValue();
        EquipmentStatus eqStatus = equipmentStatusRepository.findById(eqStatusId)
                .orElseThrow(() -> new EntityRecordNotFoundException("Cannot set item unavailability: equipment status not found in DB with id : " + eqStatusId));

        EquipmentSchedule equipmentSchedule = new EquipmentSchedule(eqItem, allDay, eqStatus);
        return equipmentScheduleRepository.save(equipmentSchedule);
    }
}
