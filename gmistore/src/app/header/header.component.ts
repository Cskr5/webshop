import {Component, OnInit} from '@angular/core';
import {AuthService} from "../user/auth/auth.service";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {

  constructor(public authService: AuthService) { }

  ngOnInit(): void {
  }

  logout() {
    // @ts-ignore
    this.authService.logout();
  }
}