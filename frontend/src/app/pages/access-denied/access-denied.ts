import { Component } from '@angular/core';
import { Router } from '@angular/router';

//angular materials
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-access-denied',
  imports: [MatButtonModule, MatIconModule],
  templateUrl: './access-denied.html',
  styleUrl: './access-denied.css',
})
export class AccessDenied {

  constructor(private router: Router) {}

  goHome() {
    this.router.navigate(['/home']);
  }
}