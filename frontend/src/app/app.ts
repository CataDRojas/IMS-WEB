import { Component } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';

//Angular material
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet,
    MatIconModule, 
    MatButtonModule, 
    MatToolbarModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected title = 'IMS-WEB';

  constructor(public router: Router) {}

  irAlHome() {
    this.router.navigate(['/home']);
  }
}
