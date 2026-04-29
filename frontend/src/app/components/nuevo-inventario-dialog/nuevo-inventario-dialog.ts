import { Component } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-nuevo-inventario-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatOptionModule,
    MatCardModule
  ],
  template: `
    <h2 style="margin: 0 0 15px 0;">Nuevo inventario</h2>

    <mat-form-field appearance="outline" style="width: 100%">
      <mat-label>Nombre</mat-label>
      <input matInput [(ngModel)]="nombre" />
    </mat-form-field>

    <mat-form-field appearance="outline" style="width: 100%">
      <mat-label>Tipo</mat-label>
      <mat-select [(ngModel)]="tipo">
        <mat-option value="ENTRADA">ENTRADA</mat-option>
        <mat-option value="AJUSTE">AJUSTE</mat-option>
      </mat-select>
    </mat-form-field>

    <div style="display:flex; justify-content:flex-end; gap:10px; margin-top:15px;">
      <button mat-button (click)="cerrar()">Cancelar</button>
      <button mat-raised-button color="primary" (click)="crear()" [disabled]="!nombre || !tipo">
        Crear
      </button>
    </div>
  `
})
export class NuevoInventarioDialogComponent {
  nombre = '';
  tipo: 'ENTRADA' | 'AJUSTE' = 'ENTRADA';

  constructor(private dialogRef: MatDialogRef<NuevoInventarioDialogComponent>) {}

  cerrar() {
    this.dialogRef.close();
  }

  crear() {
    this.dialogRef.close({
      nombre: this.nombre.trim(),
      tipo: this.tipo
    });
  }
}