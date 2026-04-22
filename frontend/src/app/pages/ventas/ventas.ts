import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-ventas',
  imports: [],
  templateUrl: './ventas.html',
  styleUrl: './ventas.css',
})
export class Ventas {
  constructor(private router: Router) {}

  goHome() {
    this.router.navigate(['/home']);
  }

}
