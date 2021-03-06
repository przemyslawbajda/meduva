import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute} from "@angular/router";
import {EmailService} from "../../../../service/email.service";
import {JwtStorageService, TokenUserInfo} from "../../../../service/token/jwt-storage.service";

@Component({
  selector: 'app-edit-email',
  templateUrl: './edit-email.component.html',
  styleUrls: ['./edit-email.component.css']
})
export class EditEmailComponent implements OnInit {

  form!: FormGroup;
  submitted: boolean = false;
  emailSent: boolean = false;
  sendFailed: boolean = false;
  resultInfo: string = '';
  id!: number;
  tokenUserInfo!: TokenUserInfo | null;

  constructor(
    private formBuilder: FormBuilder,
    private emailService: EmailService,
    private route: ActivatedRoute,
    private token: JwtStorageService
  ) { }

  ngOnInit(): void {
    this.form = this.formBuilder.group({
      email: new FormControl('', [
          Validators.required,
          Validators.pattern('^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$')
        ]),
    })

  }

  onSubmit(): void {
    this.submitted = true;
    if(this.route.snapshot.params.id != undefined){
      this.id = this.route.snapshot.params.id;
    }else{
      this.tokenUserInfo = this.token.getCurrentUser();
      if(this.tokenUserInfo != null){
        this.id = this.tokenUserInfo.id;
      }
    }

    this.emailService.sendEmailResetLinkMail(this.id, this.form.get('email')?.value).subscribe(
      this.sentMailMessageObserver
    )
  }

  sentMailMessageObserver = {
    next: (data: any) => {
      this.emailSent = true;
      this.resultInfo = 'Check your mailbox for email-reset link!';
      this.sendFailed=false;
    },
    error: (err: any) => {
      this.resultInfo = err.error.message;
      this.sendFailed = true;
    }
  }

}
