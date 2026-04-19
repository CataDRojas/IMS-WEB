import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Location } from '@angular/common';

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

  constructor(private router: Router, private location: Location) {}

  // ESTADO DE PANTALLA
  estado: 'inicio' | 'agregar' | 'lista' | 'editar' = 'inicio';

  codigo = '';
  cantidad = 1;

  productoEncontrado: any = null;
  productoEditando: any = null;

  lista: any[] = [];

  productosMock = [
    { codigo: '123', nombre: 'Coca Cola', categoria: 'Bebidas', precio: 1000 },
    { codigo: '456', nombre: 'Pan', categoria: 'Alimentos', precio: 1500 }
  ];

  // BUSCAR PRODUCTO
  buscarProducto() {
    this.productoEncontrado = this.productosMock.find(p => p.codigo === this.codigo);

    if (!this.productoEncontrado) {
      alert('Producto no encontrado');
      return;
    }

    this.estado = 'agregar';
  }

  aumentar() {
    this.cantidad++;
  }

  disminuir() {
    if (this.cantidad > 1) this.cantidad--;
  }

  agregarProducto() {
    this.lista.push({
      ...this.productoEncontrado,
      cantidad: this.cantidad
    });

    this.reset();
    this.estado = 'lista';
  }

  seleccionarParaEditar(item: any) {
    this.productoEditando = { ...item };
    this.cantidad = item.cantidad;
    this.estado = 'editar';
  }

  modificarProducto() {
    const index = this.lista.findIndex(p => p.codigo === this.productoEditando.codigo);
    if (index !== -1) {
      this.lista[index].cantidad = this.cantidad;
    }

    this.reset();
    this.estado = 'lista';
  }

  eliminar(item: any) {
    this.lista = this.lista.filter(i => i !== item);
  }

  guardar() {
    alert('Inventario guardado (pendiente)');
  }

  finalizar() {
    alert('Inventario finalizado ✅');
    this.lista = [];
    this.estado = 'inicio';
  }

  volver() {
    this.location.back();
  }

  reset() {
    this.codigo = '';
    this.cantidad = 1;
    this.productoEncontrado = null;
    this.productoEditando = null;
  }
}