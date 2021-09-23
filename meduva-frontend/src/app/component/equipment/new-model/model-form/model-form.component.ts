import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {EquipmentService} from "../../../../service/equipment.service";
import {EquipmentItem} from "../../../../model/equipment";

@Component({
  selector: 'app-model-form',
  templateUrl: './model-form.component.html',
  styleUrls: ['./model-form.component.css']
})
export class ModelFormComponent implements OnInit {

  modelFormGroup!: FormGroup;
  modelName: string = '';
  successMessage: string = '';
  modelNotAvailableErr: string = '';

  eqItems: EquipmentItem[] = [];
  @Output() eqItemsEmitter = new EventEmitter<EquipmentItem[]>();
  @Output() modelNameEmitter = new EventEmitter<string>();
  @Output() stepCompletionEmitter = new EventEmitter<boolean>();

  constructor(
    private formBuilder: FormBuilder,
    private equipmentService: EquipmentService,
  ) { }

  ngOnInit(): void {
    this.modelFormGroup = this.formBuilder.group({
      modelName : new FormControl('', [
        Validators.required
      ]),
      itemCount: new FormControl('',[
        Validators.required,
        Validators.min(1),
      ])
    });
  }

  validateForm(): void {
    this.modelName = this.modelFormGroup.controls.modelName.value;

    this.equipmentService.doesModelExistByName(this.modelName).subscribe(
      doesExist => {
        if (!doesExist) {
          this.generateItems();
          this.completeStep();
          this.modelNotAvailableErr = ''
          this.successMessage = 'Success! Move to the next step';
        } else {
          this.blockStep();
          this.successMessage = '';
          this.modelNotAvailableErr = 'Model name already exists';
        }
      },
      err => {
        this.blockStep();
        console.log(err);
      }
    );
  }

  generateItems(): void {
    this.modelName = this.modelFormGroup.controls.modelName.value;
    this.modelName = this.prepareModelName(this.modelName);
    let itemCount: number = this.modelFormGroup.controls.itemCount.value;

    this.eqItems = [];
    for(let i = 1; i <= itemCount; i++)
    {
      let eqItem: EquipmentItem = {
        id: i,
        name: this.modelName + '_' + i
      }
      this.eqItems.push(eqItem);
    }
  }

  private prepareModelName(modelName: string): string {
    return modelName.split(' ').join('_');
  }

  completeStep(): void {
    this.modelNameEmitter.emit(this.modelName);
    this.eqItemsEmitter.emit(this.eqItems);
    this.stepCompletionEmitter.emit(true);
  }

  blockStep(): void {
    this.stepCompletionEmitter.emit(false);
  }
}
