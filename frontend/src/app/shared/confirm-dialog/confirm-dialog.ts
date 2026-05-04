import { Component, Inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from "@angular/material/dialog";

@Component({
  selector: "app-confirm-dialog",
  imports: [CommonModule, MatButtonModule, MatIconModule, MatDialogModule],
  template: `
    <!-- CONTENEDOR SUPERIOR (ÍCONO Y TÍTULO CENTRADOS) -->
    <div style="display: flex; flex-direction: column; align-items: center; text-align: center; padding-top: 30px;">
      
      <mat-icon 
        [style.color]="data.colorIcono || '#f59e0b'" 
        style="font-size: 64px; width: 64px; height: 64px; margin-bottom: 15px;">
        {{ data.icono || "warning" }}
      </mat-icon>

      <h2 mat-dialog-title style="margin: 0; padding: 0 20px; color: #1e293b; font-weight: 800; font-size: 1.5rem; line-height: 1.2;">
        {{ data.titulo }}
      </h2>

    </div>

    <!-- MENSAJE DESCRIPTIVO -->
    <mat-dialog-content style="text-align: center; padding: 15px 24px 5px 24px; overflow: hidden;">
      <p style="font-size: 1.1rem; color: #64748b; margin: 0; line-height: 1.5;">
        {{ data.mensaje }}
      </p>
    </mat-dialog-content>

    <!-- BOTONES CENTRADOS -->
    <mat-dialog-actions style="display: flex; justify-content: center; gap: 15px; padding: 20px 24px 30px 24px; margin-bottom: 0;">
      
      <button mat-button mat-dialog-close style="color: #64748b; font-weight: 600; border-radius: 8px; padding: 0 20px; height: 48px;">
        {{ data.textoCancelar || "Cancelar" }}
      </button>

      <button mat-raised-button [mat-dialog-close]="true" [style.background-color]="data.colorBoton || '#ef4444'" style="color: white; font-weight: bold; border-radius: 8px; padding: 0 25px; height: 48px;">
        {{ data.textoConfirmar || "Sí, confirmar" }}
      </button>

    </mat-dialog-actions>
  `
})
export class ConfirmDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
  ) {}
}
