import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

//angular materials
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-not-found',
  imports: [RouterModule, MatButtonModule, MatIconModule],
  templateUrl: './not-found.html',
  styleUrl: './not-found.css',
})
export class NotFound {

}
