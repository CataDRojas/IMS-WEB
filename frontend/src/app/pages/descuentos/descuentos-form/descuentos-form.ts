import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { DescuentosService, Descuento } from '../../../services/descuento/descuento';

@Component({
  selector: 'app-descuentos-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './descuentos-form.html'
})
export class DescuentosFormComponent implements OnInit {

  form!: FormGroup;
  id?: number;
  loading = false;

  tipos = ['FLAT', 'PORCENTAJE', 'MULTIPLICATIVO'];

  constructor(
    private fb: FormBuilder,
    private service: DescuentosService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {

    this.form = this.fb.group({
      descuentoNombre: [''],
      descuentoTipo: ['PORCENTAJE'],
      descuentoValor: [0],
      descuentoActivo: [true]
    });

    const param = this.route.snapshot.paramMap.get('id');
    this.id = param ? Number(param) : undefined;

    if (this.id) {
      this.loading = true;

      this.service.getById(this.id).subscribe({
        next: (data) => {
          this.form.patchValue(data);
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
    }
  }

  save(): void {

    const payload: Descuento = this.form.value;

    const request$ = this.id
      ? this.service.update(this.id, payload)
      : this.service.create(payload);

    request$.subscribe({
      next: () => this.router.navigate(['/descuentos']),
      error: () => {
        // optional UI error handling later
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/descuentos']);
  }

  goHome() {
    this.router.navigate(['/home']);
  }
}