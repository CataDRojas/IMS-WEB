import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-input-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, MatButtonModule, MatIcon, MatInputModule, MatFormFieldModule, MatDialogModule],
  styles: [`
    /* =========================================
       COLORES DEL INPUT
    ========================================= */
    ::ng-deep .custom-input.mat-focused .mdc-notched-outline__leading,
    ::ng-deep .custom-input.mat-focused .mdc-notched-outline__notch,
    ::ng-deep .custom-input.mat-focused .mdc-notched-outline__trailing {
      border-color: #52b3ac !important;
      border-width: 2px !important;
    }

    ::ng-deep .custom-input.mat-focused .mdc-floating-label {
      color: #52b3ac !important;
    }

    ::ng-deep .custom-input input {
      caret-color: #52b3ac !important;
    }

    /* =========================================
       BOTÓN DE CONFIRMAR 
    ========================================= */
    .btn-confirmar {
      background-color: #52b3ac !important;
      color: white !important;
      border-radius: 8px !important;
      padding: 0 25px !important;
      height: 48px !important;
      font-weight: bold !important;
      transition: background-color 0.2s ease-in-out;
    }

    .btn-confirmar:hover:not([disabled]) {
      background-color: #429690 !important;
    }

    .btn-confirmar:disabled {
      background-color: #cbd5e1 !important;
      color: #f1f5f9 !important;
    }

    /* =========================================
       BOTÓN DE CANCELAR
    ========================================= */
    .btn-cancelar {
      color: #429690 !important;
      font-weight: 600 !important;
      border-radius: 8px !important;
      padding: 0 20px !important;
      height: 48px !important;
      background-color: transparent !important;
      transition: all 0.2s ease-in-out;
    }

    .btn-cancelar:hover {
      background-color: rgba(66, 150, 144, 0.08) !important; 
      color: #31756f !important; 
    }
  `],
  template: `
    <div style="display: flex; flex-direction: column; align-items: center; text-align: center; padding-top: 30px;">
      
      <div style="background-color: #cff3f1; border-radius: 50%; width: 64px; height: 64px; display: flex; align-items: center; justify-content: center; margin-bottom: 15px;">
        <mat-icon style="color: #429690; font-size: 32px; width: 32px; height: 32px;">
          {{ data.icono || 'edit_document' }}
        </mat-icon>
      </div>

      <h2 mat-dialog-title style="margin: 0; padding: 0 20px; color: #1e293b; font-weight: 800; font-size: 1.5rem; line-height: 1.2;">
        {{ data.titulo }}
      </h2>
    </div>
    
    <mat-dialog-content style="padding: 20px 24px 5px 24px; overflow: hidden;">
      
      <mat-form-field appearance="outline" class="custom-input" style="width: 100%;">
        <mat-label>{{ data.label }}</mat-label>
        <input matInput [(ngModel)]="valor" [placeholder]="data.placeholder || ''" (keyup.enter)="confirmar()" autofocus>
      </mat-form-field>

    </mat-dialog-content>

    <mat-dialog-actions style="display: flex; justify-content: center; gap: 15px; padding: 10px 24px 30px 24px; margin-bottom: 0;">
      
      <button mat-button mat-dialog-close class="btn-cancelar">
        Cancelar
      </button>

      <button mat-raised-button (click)="confirmar()" [disabled]="!valor" class="btn-confirmar">
        {{ data.textoConfirmar || 'Aceptar' }}
      </button>

    </mat-dialog-actions>
  `
})
export class InputDialogComponent {
  valor: string = '';

  constructor(
    public dialogRef: MatDialogRef<InputDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {}

  confirmar() {
    if (this.valor.trim()) {
      this.dialogRef.close(this.valor.trim());
    }
  }
}