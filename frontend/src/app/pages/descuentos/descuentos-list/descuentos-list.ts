import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms'; // Añadido ReactiveForms

// ANGULAR MATERIALS
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { DescuentosService, Descuento } from '../../../services/descuento/descuento';

@Component({
  selector: 'app-descuentos-list',
  standalone: true,
  imports: [
    CommonModule, 
    ReactiveFormsModule, // Necesario para el modal
    MatIconModule, 
    MatButtonModule, 
    MatExpansionModule, 
    MatProgressSpinnerModule
  ],
  templateUrl: './descuentos-list.html',
  styleUrls: ['./descuentos-list.css']
})
export class DescuentosListComponent implements OnInit {
  descuentos: Descuento[] = [];
  loading = false;
  rolUsuario = localStorage.getItem('rol_ims') ?? 'Invitado';
  
  // Lógica para el Modal
  form!: FormGroup;
  mostrarFormulario = false;
  descuentoActualId?: number;

  constructor(
    private service: DescuentosService,
    private router: Router,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.load();
  }

  esAdmin(): boolean {
  return this.rolUsuario === 'ADMIN';
}

  initForm(): void {
    this.form = this.fb.group({
      descuentoNombre: ['', Validators.required],
      descuentoTipo: ['PORCENTAJE', Validators.required],
      descuentoValor: [0, [Validators.required, Validators.min(0)]],
      descuentoValorSecundario: [null],
      descuentoActivo: [true]
    });

    // UX Hook para limpiar valor secundario
    this.form.get('descuentoTipo')?.valueChanges.subscribe(tipo => {
      if (tipo !== 'MULTIPLICATIVO') {
        this.form.patchValue({ descuentoValorSecundario: null });
      }
    });
  }

  load(): void {
    this.loading = true;
    this.service.getAll().subscribe({
      next: (data) => {
        this.descuentos = data;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  // Cambiamos navegación por apertura de modal
  create(): void {
    this.descuentoActualId = undefined;
    this.form.reset({ descuentoTipo: 'PORCENTAJE', descuentoActivo: true, descuentoValor: 0 });
    this.mostrarFormulario = true;
  }

  edit(id: number): void {
    this.descuentoActualId = id;
    this.loading = true;
    this.service.getById(id).subscribe({
      next: (data) => {
        this.form.patchValue(data);
        this.mostrarFormulario = true;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  save(): void {
    if (this.form.invalid) return;
    const payload: Descuento = this.form.value;
    const request$ = this.descuentoActualId
      ? this.service.update(this.descuentoActualId, payload)
      : this.service.create(payload);

    request$.subscribe({
      next: () => {
        this.mostrarFormulario = false;
        this.load();
      }
    });
  }

  cancelar(): void {
    this.mostrarFormulario = false;
  }

  delete(id: number): void {
    if (!id || !confirm('¿Eliminar descuento?')) return;
    this.service.delete(id).subscribe(() => this.load());
  }

  goHome() { this.router.navigate(['/home']); }

  getValorDisplay(descuento: Descuento): string {
    if (!descuento) return '—';

    if (descuento.descuentoTipo === 'PORCENTAJE') {
      return `${descuento.descuentoValor}%`;
    }

    if (descuento.descuentoTipo === 'FLAT') {
      return `$${descuento.descuentoValor}`;
    }

    // Para MULTIPLICATIVO u otros
    return `x${descuento.descuentoValor}`;
  }

  getTipoLabel(tipo: string): string {
    switch (tipo) {
      case 'FLAT': return 'Monto fijo';
      case 'PORCENTAJE': return 'Porcentaje';
      case 'MULTIPLICATIVO': return 'Multiplicador';
      default: return tipo;
    }
  }
}