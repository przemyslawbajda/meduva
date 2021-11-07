import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {CalendarEvent, CalendarView} from "angular-calendar";
import {MatDialog} from "@angular/material/dialog";
import {ActivatedRoute} from "@angular/router";
import {DayDialogComponent} from "../../dialog/day-dialog/day-dialog.component";
import {User} from "../../../../model/user";
import {UserService} from "../../../../service/user.service";
import {ScheduleService, TimeRange, WeekBoundaries, WorkHours, WorkSchedule} from "../../../service/schedule.service";
import {createAbsenceHoursEvent, createOffWorkHoursEvent} from "../../../util/event/creation";
import {MatSnackBar} from "@angular/material/snack-bar";

@Component({
  selector: 'app-worker-schedule',
  changeDetection: ChangeDetectionStrategy.Default, // if strange bugs will occur, try using .OnPush
  templateUrl: './worker-schedule.component.html',
  styleUrls: ['./worker-schedule.component.css']
})
export class WorkerScheduleComponent implements OnInit {

  view: CalendarView = CalendarView.Week;
  viewDate: Date = new Date();

  events: CalendarEvent[] = [];

  clickedDate!: Date;
  clickedColumn!: number;

  worker!: User;

  firstDayOfWeek!: Date;
  lastDayOfWeek!: Date;

  dayStartHour: number = 6;
  dayEndHour: number = 20;

  constructor(
    private activatedRoute: ActivatedRoute,
    private dialog: MatDialog,
    private userService: UserService,
    private scheduleService: ScheduleService,
    ) {
  }

  ngOnInit(): void {
    let workerId = this.activatedRoute.snapshot.params.id;
    this.userService.getUserDetails(workerId).subscribe(
      worker => {
        this.worker = worker;
        this.prepareWeekEvents();
      }
    );
  }

  public prepareWeekEvents() {
    this.events = [];
    this.setFirstAndLastDayOfWeek();
    this.prepareWeeklyOffWorkHours();
  }

  private setFirstAndLastDayOfWeek() {
    let currDate = new Date(this.viewDate);
    let firstDayOfWeekNumber = currDate.getDate() - currDate.getDay();
    this.firstDayOfWeek = new Date(currDate.setDate(firstDayOfWeekNumber));
    this.lastDayOfWeek = new Date(currDate.setDate(this.firstDayOfWeek.getDate() + 6));
  }

  private prepareWeeklyOffWorkHours(): void {
    let weekBoundaries: WeekBoundaries = {
      firstWeekDay: this.firstDayOfWeek,
      lastWeekDay: this.lastDayOfWeek
    };

    this.scheduleService.getWeeklyOffWorkHours(this.worker.id, weekBoundaries).subscribe(
    (weeklyOffWorkHours: WorkHours[]) => {
      this.updateWorkHoursEvents(weeklyOffWorkHours);
      this.prepareWeeklyAbsenceHours();
    });
  }

  private prepareWeeklyAbsenceHours(): void {
    let weekBoundaries: WeekBoundaries = {
      firstWeekDay: this.firstDayOfWeek,
      lastWeekDay: this.lastDayOfWeek
    };

    this.scheduleService.getWeeklyAbsenceHours(this.worker.id, weekBoundaries).subscribe(
      (weeklyAbsenceHours: WorkSchedule[]) => {
        console.log(weeklyAbsenceHours);
        this.updateAbsenceHoursEvents(weeklyAbsenceHours);
      }
    );
  }

  private updateWorkHoursEvents(weeklyOffWorkHours: WorkHours[]) {
    let newEvents = this.events;
    this.events = [];
    weeklyOffWorkHours.forEach(offWorkHours => {
      newEvents.push(
        createOffWorkHoursEvent(offWorkHours.startTime, offWorkHours.endTime));
    });
    this.events = [...newEvents];
  }

  private updateAbsenceHoursEvents(weeklyAbsenceHours: WorkSchedule[]) {
    let newEvents = this.events;
    this.events = [];
    weeklyAbsenceHours.forEach(absenceHours => {
      console.log(absenceHours);
      newEvents.push(
        createAbsenceHoursEvent(absenceHours.timeFrom, absenceHours.timeTo)
      );
    });
    this.events = [...newEvents];
  }

  openDayDialog(date: Date) {
    this.clickedDate = date;

    const dayDialog = this.dialog.open(DayDialogComponent, {
      width: '500px',
      panelClass: 'my-dialog',
      data: { date: this.clickedDate,
              workerId: this.worker.id}
    });

    dayDialog.afterClosed().subscribe(
      result => {
        /*if (result.event == 'WORK_HOURS') {
          let workHoursToSave: WorkHours = result.data;
          this.saveWorkHours(workHoursToSave);
        } else if(result.event == 'ABSENCE_HOURS'){
          let absenceHoursToSave: TimeRange = result.data;
          this.saveAbsenceHours(absenceHoursToSave);
        }*/

        switch(result.event){
          case 'WORK_HOURS':
            let workHoursToSave: WorkHours = result.data;
            this.saveWorkHours(workHoursToSave);
            break;
          case 'ABSENCE_HOURS':
            let absenceHoursToSave: TimeRange = result.data;
            this.saveAbsenceHours(absenceHoursToSave);
            break;
          case 'DELETE_ABSENCE_HOURS':
            let absenceDayDate: Date = result.data;
            this.deleteDailyAbsenceHours(absenceDayDate);
            break;
        }
      }
    );
  }

  private saveWorkHours(workHoursToSave: WorkHours) {
    this.scheduleService.saveWorkHours(this.worker.id, workHoursToSave).subscribe(
      workHours => {
        this.prepareWeekEvents();
      }, err => {
        console.log(err);
      }
    );
  }

  private saveAbsenceHours(absenceHoursToSave: TimeRange){
    this.scheduleService.saveAbsenceHours(this.worker.id, absenceHoursToSave).subscribe(
      data => {
        this.prepareWeekEvents();
      }, err => {
        console.log(err);
      }
    );

  }

  private deleteDailyAbsenceHours(absenceDayDate: Date){
    this.scheduleService.deleteDailyAbsenceHours(this.worker.id, absenceDayDate).subscribe(
      data => {
        this.prepareWeekEvents();
      }, err => {
        console.log(err);
      }
    )
  }

  eventClick($event: { event: CalendarEvent<any>; sourceEvent: any }) {

  }


}
