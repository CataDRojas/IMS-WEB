import { Component, OnInit } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

import { ConfiguracionService } from '../../services/configuracion/configuracion';

@Component({
  selector: 'app-configuracion-sistema',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './configuracion-sistema.html',
  styleUrl: './configuracion-sistema.css',
})
export class ConfiguracionSistema implements OnInit {

  configForm!: FormGroup;
  loading = false;

  editando = false;

  constructor(
    private fb: FormBuilder,
    private configService: ConfiguracionService,
    private location: Location
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.cargarConfiguracion();
  }

  // =========================
  // FORM INIT
  // =========================
  private initForm() {
    this.configForm = this.fb.group({
      empresaNombre: ['', Validators.required],
      empresaDireccion: ['', Validators.required],
      empresaRun: [''],
      empresaDV: [''],
      iva: [0, [Validators.required, Validators.min(0), Validators.max(100)]]
    });
  }

  // helper for template
  get ivaControl() {
    return this.configForm.get('iva');
  }

  // =========================
  // toggle
  // =========================
  activarEdicion() {
    this.editando = true;
  }

  cancelarEdicion() {
    this.editando = false;
    this.cargarConfiguracion();
  }

  // =========================
  // LOAD CONFIG
  // =========================
  cargarConfiguracion() {
    this.loading = true;

    this.configService.getConfiguracion().subscribe({
      next: (config: any) => {

        this.configForm.patchValue({
          empresaNombre: config?.empresaNombre ?? '',
          empresaDireccion: config?.empresaDireccion ?? '',
          empresaRun: config?.empresaRun ?? '',
          empresaDV: config?.empresaDV ?? '',
          iva: config?.iva ?? 0
        });

        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  // =========================
  // SAVE
  // =========================
  guardar() {

    if (this.configForm.invalid) {
      this.configForm.markAllAsTouched(); // show validation errors
      return;
    }

    const payload = {
      configuracionId: 1,
      ...this.configForm.value
    };

    this.loading = true;

    this.configService.saveConfiguracion(payload).subscribe({
      next: () => {

        this.editando = false;
        this.cargarConfiguracion();

        this.loading = false;
      },

      error: () => {
        // no backend error handling anymore
        this.loading = false;
      }
    });
  }

  // =========================
  // NAV
  // =========================
  volver() {
    this.location.back();
  }
}