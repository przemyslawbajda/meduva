import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";

export interface ItemRoom {
  itemId: number,
  roomId: number
}

export interface NewModelRequest {
  modelName: string,
  itemCount: number,
  servicesIds: number[],
  itemRooms: ItemRoom[]
}

@Component({
  selector: 'app-new-model',
  templateUrl: './new-model.component.html',
  styleUrls: ['./new-model.component.css']
})
export class NewModelComponent implements OnInit {

  modelFormGroup!: FormGroup;
  secondFormGroup: any;
  isLinear: boolean = true;

  constructor(
    private formBuilder: FormBuilder,
  ) { }

  ngOnInit(): void {
    this.modelFormGroup = this.formBuilder.group({
      modelName : new FormControl('', [
        Validators.required
      ]),
      itemCount: new FormControl('', [
        Validators.required,
        Validators.min(1)
      ])
    });
  }

  // TODO:
  //  1. check if model with given name already exists
  saveModelWithItems() {

    // TODO: populate arrays with appropriate key-value pairs
    let itemRooms: ItemRoom[] = [];
    let servicesIds: number[] = [];

    let newModelReuqest: NewModelRequest = {
      modelName: this.modelFormGroup.controls.modelName.value,
      itemCount: this.modelFormGroup.controls.itemCount.value,
      servicesIds: servicesIds,
      itemRooms: itemRooms
    };

    // TODO: send request
  }
}
