import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Location } from '@angular/common';
import { InventarioService } from '../../../services/inventario/inventario';

// Angular Material
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatToolbarModule } from '@angular/material/toolbar';

@Component({
  selector: 'app-inventario-form',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatToolbarModule
  ],
  templateUrl: './inventario-form.html',
  styleUrls: ['./inventario-form.css'],
})
export class InventarioForm {

  constructor(
    private router: Router, 
    private location: Location,
    private inventarioService: InventarioService // Inyectamos el servicio
  ) {}

  estado: 'inicio' | 'agregar' | 'lista' | 'editar' = 'inicio';

  codigo = '';
  cantidad = 1;

  productoEncontrado: any = null;
  productoEditando: any = null;

  lista: any[] = [];

  // BUSCAR PRODUCTO REAL EN BD
  buscarProducto() {
    if (!this.codigo) return;

    this.inventarioService.buscarProductoPorCodigo(this.codigo).subscribe({
      next: (producto) => {
        this.productoEncontrado = producto;
        this.estado = 'agregar';
      },
      error: (err) => {
        console.error(err);
        alert('❌ Producto no encontrado en la base de datos. Revisa el código.');
      }
    });
  }

  aumentar() { this.cantidad++; }
  disminuir() { if (this.cantidad > 1) this.cantidad--; }

  agregarProducto() {
    this.lista.push({
      ...this.productoEncontrado, // Guardamos todo el objeto producto real
      cantidadAgregada: this.cantidad // Usamos nombre distinto para no pisar el stock
    });

    this.reset();
    this.estado = 'lista';
  }

  seleccionarParaEditar(item: any) {
    this.productoEditando = { ...item };
    this.cantidad = item.cantidadAgregada;
    this.estado = 'editar';
  }

  modificarProducto() {
    const index = this.lista.findIndex(p => p.productoCodigo === this.productoEditando.productoCodigo);
    if (index !== -1) {
      this.lista[index].cantidadAgregada = this.cantidad;
    }
    this.reset();
    this.estado = 'lista';
  }

  eliminar(item: any) {
    this.lista = this.lista.filter(i => i !== item);
  }

  guardar() {
    alert('Borrador guardado localmente (se podría implementar localStorage aquí)');
  }

  // EL GUARDADO REAL EN MYSQL
  finalizar() {
    if (this.lista.length === 0) {
      alert('⚠️ Agrega al menos un producto antes de finalizar.');
      return;
    }

    // 1. Armamos la cabecera del Movimiento
    const nuevoMovimiento = {
      movimientoTipo: 'ENTRADA', // Ajustar si después le pones un select
      movimientoEstado: 'CONFIRMADO',
      movimientoDescripcion: 'Ingreso de inventario desde interfaz',
      movimientoMetodoPago: 'EFECTIVO' // Requisito obligatorio de tu backend
    };

    // 2. Creamos la cabecera
    this.inventarioService.crearMovimientoCabecera(nuevoMovimiento).subscribe({
      next: (movimientoCreado) => {
        const movId = movimientoCreado.movimientoId;
        let detallesProcesados = 0;

        // 3. Recorremos la lista y creamos los detalles uno por uno
        this.lista.forEach(item => {
          const detalle = {
            movimientoDetalleCantidad: item.cantidadAgregada,
            movimientoDetalleUnidadesPorPaquete: 1, // Exigencia de la validación Java
            movimientoDetalleDescripcion: 'Ingresado por sistema',
            producto: { productoId: item.productoId } // Match con el ID real
          };

          this.inventarioService.crearDetalle(movId, detalle).subscribe({
            next: () => {
              detallesProcesados++;
              // Verificamos si terminamos de guardar toda la lista
              if (detallesProcesados === this.lista.length) {
                alert('✅ ¡Inventario guardado con éxito en la Base de Datos!');
                this.lista = [];
                this.estado = 'inicio';
              }
            },
            error: (err) => console.error('Error al guardar detalle:', err)
          });
        });
      },
      error: (err) => {
        console.error('Error al crear cabecera:', err);
        alert('❌ Hubo un error al crear el movimiento de cabecera.');
      }
    });
  }

  volver() { this.location.back(); }

  reset() {
    this.codigo = '';
    this.cantidad = 1;
    this.productoEncontrado = null;
    this.productoEditando = null;
  }
}