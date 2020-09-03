import {Component, Input, OnInit} from '@angular/core';
import {UserModel} from "../../../../models/user-model";
import {ActivatedRoute, Router} from "@angular/router";
import {compareNumbers} from "@angular/compiler-cli/src/diagnostics/typescript_version";
import {FormArray, FormBuilder, FormGroup, Validators} from "@angular/forms";
import {UserService} from "../../../../service/user.service";
import {AdminService} from "../../../../service/admin.service";
import {SharingService} from "../../../../service/sharing.service";

@Component({
  selector: 'app-admin-user-form',
  templateUrl: './admin-user-form.component.html',
  styleUrls: ['./admin-user-form.component.css']
})
export class AdminUserFormComponent implements OnInit {
  userForm: FormGroup;
  details: any[];
  user: UserModel;
  loaded: boolean = false;

  user2 = {
    "Asaa": "sad"
  };

  constructor(private sharingService: SharingService, private fb: FormBuilder, private route: ActivatedRoute, private adminService: AdminService) {
    this.userForm = this.fb.group({
      shippingAddress: this.fb.group({
        city: [''],
        street: [''],
        number: [''],
        floor: [''],
        door: [''],
        country: [''],
        postcode: ['']
      }),
      username: ['', Validators.required],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', Validators.required],
      phoneNumber: [''],
      registered: ['', Validators.required],
      roles: new FormArray([], Validators.required),
      active: ['', Validators.required],

    })
  }


  ngOnInit(): void {
    this.route.paramMap.subscribe(
      paramMap => {
        const editableId = paramMap.get('id');
        if (editableId) {
          this.getUserDetails(editableId);
        }
      },
      error => console.warn(error),
    );

  }

  getUserDetails(id) {
    this.adminService.getAccount(id).subscribe(data => {
      console.log(data);
      this.user = data;
      this.sharingService.nextMessage(data);

      this.userForm.patchValue({
        username: data.username,
        firstName: data.firstName,
        lastName: data.lastName,
        email: data.email,
        phoneNumber: data.phoneNumber,
        roles: data.roles,
        registered: this.getDate(data.registered),
        active: data.active,
      });
      this.loaded = true;
    }, error => console.log(error))

  }

  getDate = (d: Date) => {
    let dt = new Date(d);
    let dtm = dt.getMonth();
    let dty = dt.getFullYear();
    let day = dt.getDay();
    return dty + "/" + dtm + "/" + day
  }

}