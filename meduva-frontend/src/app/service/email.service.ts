import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class EmailService {

  constructor(private http: HttpClient) { }

  sendResetLinkMail(email: string) {
    return this.http.post(environment.API_BASE_URL + 'api/password/request', email);
  }
}
