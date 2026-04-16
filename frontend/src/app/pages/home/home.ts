import { Component } from '@angular/core';
import { NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  imports: [NgIf, RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  rolUsuario = localStorage.getItem('rol_ims');
  nombreUsuario = localStorage.getItem('nombre_ims');

}
