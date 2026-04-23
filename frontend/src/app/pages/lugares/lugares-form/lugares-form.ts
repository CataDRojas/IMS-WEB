import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { LugarService, MovimientoLugar } from '../../../services/lugar/lugar';

@Component({
  selector: 'app-lugares-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './lugares-form.html'
})
export class LugaresForm implements OnInit {
  lugarForm: FormGroup;
  isEditMode: boolean = false;
  lugarId: number | null = null;

  constructor(
    private fb: FormBuilder,
    private lugarService: LugarService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    // Nombres en minúscula inicial para que Angular y Java hablen el mismo idioma
    this.lugarForm = this.fb.group({
      movimientoLugarDescripcion: ['', Validators.required],
      movimientoLugarActivo: [true]
    });
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEditMode = true;
      this.lugarId = Number(idParam);
      this.cargarDatosLugar(this.lugarId);
    }
  }

  cargarDatosLugar(id: number): void {
    this.lugarService.getLugarById(id).subscribe({
      next: (lugar: MovimientoLugar) => {
        this.lugarForm.patchValue({
          movimientoLugarDescripcion: lugar.movimientoLugarDescripcion,
          movimientoLugarActivo: lugar.movimientoLugarActivo
        });
      },
      error: (err: any) => console.error('Error al cargar el lugar', err)
    });
  }

  guardarLugar(): void {
    if (this.lugarForm.invalid) {
      this.lugarForm.markAllAsTouched();
      return;
    }

    const formData: MovimientoLugar = this.lugarForm.value;
    
    // Si estamos editando, le agregamos el ID al objeto antes de mandarlo
    if (this.isEditMode && this.lugarId) {
      formData.movimientoLugarId = this.lugarId;
    }

    this.lugarService.guardarLugar(formData).subscribe({
      next: () => {
        // Agregamos una alerta para que sepas que todo salió bien
        alert('¡Lugar guardado con éxito!');
        this.router.navigate(['/lugares']);
      },
      error: (err: any) => {
        console.error('Error al guardar', err);
        // Si el backend rechaza el guardado, ahora te vas a enterar
        alert('❌ Hubo un problema al guardar. Revisa la consola (F12) para ver el error del servidor.');
      }
    });
  }
}