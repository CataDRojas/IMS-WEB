import { Component, ViewChild, ElementRef, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Router } from "@angular/router";
import { FormsModule } from "@angular/forms";
import { Location } from "@angular/common";
import { InventarioService } from "../../../services/inventario/inventario";
import { BrowserMultiFormatReader } from "@zxing/browser";
import { ConfirmDialogComponent } from '../../../shared/confirm-dialog/confirm-dialog';
import { firstValueFrom } from 'rxjs';
import { InputDialogComponent } from '../../../shared/input-dialog/input-dialog';

//angular materials
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatSelectModule } from "@angular/material/select";
import { MatOptionModule } from "@angular/material/core";
import { MatSnackBarModule, MatSnackBar } from "@angular/material/snack-bar";
import { MatDialog, MatDialogModule } from '@angular/material/dialog';

@Component({
  selector: "app-inventario-form",
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatToolbarModule,
    MatSelectModule,
    MatOptionModule,
    MatSnackBarModule,
    MatDialogModule
  ],
  templateUrl: "./inventario-form.html",
  styleUrls: ["./inventario-form.css"],
})
export class InventarioForm implements OnInit {
  @ViewChild("videoElement") videoElement!: ElementRef<HTMLVideoElement>;

  estado: "inicio" | "agregar" | "lista" = "inicio";
  codigo = "";
  cantidad = 1;
  cajas = 0;
  loteEditable = 1;

  productoEncontrado: any = null;
  productoEditando: any = null;

  inventariosLocales: any[] = [];
  inventariosBD: any[] = [];

  inventarioActual: any = null;
  lugares: any[] = [];
  lugarSeleccionado: any = null;

  escanerAbierto = false;
  codeReader = new BrowserMultiFormatReader();
  controlesCamara: any;

  constructor(
    private router: Router,
    private location: Location,
    private inventarioService: InventarioService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog //
  ) {}

  mapTipoToUI(tipo: string): string {
    return tipo === "AJUSTE" ? "CONTEO" : tipo;
  }

  mapUIToTipo(label: string): string {
    switch (label) {
      case "ENTRADA":
        return "ENTRADA";
      case "CONTEO":
        return "AJUSTE";
      default:
        return label;
    }
  }

  ngOnInit() {
    this.cargarLugares();
    this.cargarBorradoresBD();
    this.cargarLocales();
  }

  mostrarMensaje(mensaje: string, tipo: 'exito' | 'error' | 'info' | 'alerta' = 'info') {
    this.snackBar.open(mensaje, "Cerrar", {
      duration: 3500,
      horizontalPosition: "center",
      verticalPosition: "bottom",
      panelClass: [`snackbar-${tipo}`],
    });
  }

  cargarLugares() {
    this.inventarioService.obtenerLugaresActivos().subscribe({
      next: (data: any[]) => (this.lugares = data),
      error: (err: any) => console.error("Error lugares:", err),
    });
  }

  cargarLocales() {
    const saved = localStorage.getItem("inventarios_ims_local");
    this.inventariosLocales = saved ? JSON.parse(saved) : [];
  }

  cargarBorradoresBD() {
    this.inventarioService.obtenerBorradores().subscribe({
      next: (movimientos: any[]) => {
        this.inventariosBD = movimientos.map((m) => ({
          nombre:
            m.movimientoDescripcion?.replace("Inventario: ", "") ||
            "Sin nombre",
          movimientoId: m.movimientoId,
          tipo: m.movimientoTipo,
          esBD: true,
          lista: (m.detalles || []).map((d: any) => ({
            productoId: d.productoId,
            productoNombre: d.productoNombre,
            cantidadAgregada:
              d.movimientoDetalleCantidad *
              d.movimientoDetalleUnidadesPorPaquete,
            cajasAgregadas: d.movimientoDetalleCantidad,
            productoCantidadLote: d.movimientoDetalleUnidadesPorPaquete,
            lugarId: d.movimientoLugarId,
            lugarNombre: d.movimientoLugarNombre || "",
            productoPrecio: d.movimientoDetallePrecioBase,
          })),
          fecha: m.movimientoFechaCreacion,
        }));
      },
      error: (err) => console.error("Error cargando borradores BD:", err),
    });
  }

  async iniciarNuevo() {
    const dialogRef = this.dialog.open(InputDialogComponent, {
      width: '400px',
      data: {
        titulo: 'Nuevo Inventario',
        label: 'Nombre del inventario',
        placeholder: 'Ej: Conteo Bodega Principal',
        textoConfirmar: 'Comenzar a contar'
      }
    });

    const nombre = await firstValueFrom(dialogRef.afterClosed());

    if (!nombre) return;

    this.inventarioActual = {
      nombre,
      tipo: "AJUSTE",
      lista: [],
      esBD: false,
      fecha: new Date(),
    };
    this.inventariosLocales.push(this.inventarioActual);
    this.guardarLocales();
    this.estado = "agregar";
  }

  seleccionarInventario(inv: any) {
    this.inventarioActual = {
      ...inv,
      tipo: inv.tipo || "ENTRADA",
      esBD: inv.esBD ?? false,
    };

    this.estado = "lista";
  }

  buscarProducto() {
    if (!this.codigo) return;
    this.inventarioService.buscarProductoPorCodigo(this.codigo).subscribe({
      next: (producto: any) => {
        this.productoEncontrado = producto;
        this.loteEditable = producto.productoCantidadLote || 1;
        this.cantidad = 1;
        this.cajas = 0;
        this.lugarSeleccionado = null;
        this.estado = "agregar";
      },
      error: () => this.mostrarMensaje("❌ Producto no encontrado.", "error"),
    });
  }

  calcularPorCajas() {
    if (this.cajas > 0) {
      this.cantidad = this.cajas * this.loteEditable;
    }
  }

  calcularPorUnidades() {
    if (this.loteEditable > 0) {
      this.cajas = Math.floor(this.cantidad / this.loteEditable);
    }
  }

  agregarProducto() {
    if (!this.lugarSeleccionado)
      return this.mostrarMensaje('⚠️ Debes seleccionar una ubicación primero.', 'alerta');

    const indexExistente = this.inventarioActual.lista.findIndex(
      (p: any) =>
        p.productoId === this.productoEncontrado.productoId &&
        p.lugarId === this.lugarSeleccionado.movimientoLugarId,
    );

    const lote = this.cantidad;

    const itemData = {
      ...this.productoEncontrado,
      cantidadAgregada: this.cantidad,
      cajasAgregadas: 1,
      productoCantidadLote: this.cantidad,
      lugarId: this.lugarSeleccionado.movimientoLugarId,
      lugarNombre: this.lugarSeleccionado.movimientoLugarDescripcion,
    };

    if (this.productoEditando) {
      const idx = this.inventarioActual.lista.indexOf(this.productoEditando);
      this.inventarioActual.lista[idx] = itemData;
      this.productoEditando = null;
    } else if (indexExistente !== -1) {
      const existente = this.inventarioActual.lista[indexExistente];
      existente.cantidadAgregada += this.cantidad;
      existente.cajasAgregadas = 1;
      existente.productoCantidadLote = existente.cantidadAgregada;
    } else {
      this.inventarioActual.lista.push(itemData);
    }

    this.guardarLocales();
    this.reset();
    this.estado = "lista";
  }

  seleccionarParaEditar(item: any) {
    this.productoEncontrado = { ...item };
    this.cantidad = item.cantidadAgregada;
    this.loteEditable = item.productoCantidadLote;
    this.cajas = Math.floor(this.cantidad / this.loteEditable);
    this.lugarSeleccionado = this.lugares.find(
      (l) => l.movimientoLugarId === item.lugarId,
    );
    this.productoEditando = item;
    this.estado = "agregar";
  }

  eliminarIndividual(item: any) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        titulo: 'Eliminar Producto',
        mensaje: `¿Eliminar ${item.productoNombre} del conteo?`,
        textoConfirmar: 'Sí, eliminar',
        colorBoton: '#ef4444',
        icono: 'delete',
        colorIcono: '#ef4444'
      }
    });

    dialogRef.afterClosed().subscribe(resultado => {
      if (resultado) {
        this.inventarioActual.lista = this.inventarioActual.lista.filter(
          (i: any) => i !== item,
        );
        this.guardarLocales();
      }
    });
  }

  async eliminarInventarioLocal(inv: any, confirmar: boolean = true) {
    if (confirmar) {
      const dialogRef = this.dialog.open(ConfirmDialogComponent, {
        width: '400px',
        data: {
          titulo: 'Borrar Borrador Local',
          mensaje: `¿Borrar el borrador "${inv.nombre}"? Se perderán los datos no guardados.`,
          textoConfirmar: 'Sí, borrar',
          colorBoton: '#ef4444', // Rojo
          icono: 'delete_sweep',
          colorIcono: '#ef4444'
        }
      });
      
      const confirmado = await firstValueFrom(dialogRef.afterClosed());
      if (!confirmado) return;
    }

    this.inventariosLocales = this.inventariosLocales.filter(
      (i) => i.nombre !== inv.nombre,
    );
    this.guardarLocales();
    if (this.inventarioActual?.nombre === inv.nombre) {
      this.inventarioActual = null;
      this.estado = "inicio";
    }
  }

  eliminarInventarioBD(inv: any) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        titulo: 'Eliminar Inventario',
        mensaje: `¿Borrar el inventario guardado "${inv.nombre}"? Esto no podrá recuperarse.`,
        textoConfirmar: 'Sí, borrar',
        colorBoton: '#ef4444'
      }
    });

    dialogRef.afterClosed().subscribe(resultado => {
      if (resultado) {
        this.inventarioService.eliminarMovimiento(inv.movimientoId).subscribe({
          next: () => {
             this.inventariosBD = this.inventariosBD.filter(i => i !== inv);
          }
        });
      }
    });
  }

  guardarLocales() {
    localStorage.setItem(
      "inventarios_ims_local",
      JSON.stringify(this.inventariosLocales),
    );
  }

  async guardarBorrador() {
    if (!this.inventarioActual || this.inventarioActual.lista.length === 0) {
      this.mostrarMensaje('❌ No hay productos para guardar.', 'error');
      return;
    }

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        titulo: 'Guardar Borrador',
        mensaje: `¿Guardar "${this.inventarioActual.nombre}" como borrador? Podrás continuarlo después desde cualquier dispositivo.`,
        textoConfirmar: 'Sí, guardar',
        colorBoton: '#3b82f6',
        icono: 'save',
        colorIcono: '#3b82f6'
      }
    });

    const confirmado = await firstValueFrom(dialogRef.afterClosed());
    if (!confirmado) return;

    try {
      await this.inventarioService
        .guardarBorrador(
          this.inventarioActual.nombre,
          this.inventarioActual.lista,
        )
        .toPromise();

      this.mostrarMensaje('💾 Borrador guardado. Puedes continuarlo desde cualquier dispositivo.', 'info');

      this.inventariosLocales = this.inventariosLocales.filter(
        (i) => i !== this.inventarioActual,
      );
      this.guardarLocales();
      this.inventarioActual = null;
      this.estado = "inicio";
      this.cargarBorradoresBD();
    } catch (err: any) {
      console.error("Error:", err);
      this.mostrarMensaje('❌ Error al guardar el borrador.', 'error');
    }
  }

  async finalizar() {
    if (!this.inventarioActual || this.inventarioActual.lista.length === 0) {
      this.mostrarMensaje(
        "❌ No hay productos en el inventario actual.",
        "error",
      );
      return;
    }

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        titulo: 'Confirmar Inventario',
        mensaje: `¿Finalizar y confirmar el inventario "${this.inventarioActual.nombre}"? Esto sumará y actualizará el stock.`,
        textoConfirmar: 'Sí, confirmar',
        colorBoton: '#10b981',
        icono: 'check_circle',
        colorIcono: '#10b981'
      }
    });

    const confirmado = await firstValueFrom(dialogRef.afterClosed());
    
    if (!confirmado) return;

    try {
      let movimientoId: number;

      if (this.inventarioActual.esBD) {
        movimientoId = this.inventarioActual.movimientoId;
      } else {
        const res: any = await this.inventarioService
          .finalizarInventario(
            this.inventarioActual.nombre,
            this.inventarioActual.lista,
          )
          .toPromise();
        movimientoId = res.movimientoId;
      }

      await this.inventarioService
        .confirmarMovimiento(movimientoId)
        .toPromise();

      this.mostrarMensaje(
        "✅ Inventario confirmado y stock actualizado.",
        "exito",
      );

      if (this.inventarioActual.esBD) {
        this.inventariosBD = this.inventariosBD.filter(
          (i) => i.movimientoId !== this.inventarioActual.movimientoId,
        );
      } else {
        this.inventariosLocales = this.inventariosLocales.filter(
          (i) => i.nombre !== this.inventarioActual.nombre,
        );
        this.guardarLocales();
      }

      this.inventarioActual = null;
      this.estado = "inicio";
    } catch (err: any) {
      console.error("Error:", err);
      this.mostrarMensaje('❌ Error al procesar.', 'error');
    }
  }

  volver() {
    if (this.estado === "agregar") {
      this.estado = "lista";
      this.reset();
      return;
    }
    
    if (this.estado === "lista") {
      if (!this.inventarioActual?.esBD) {
        
        const dialogRef = this.dialog.open(ConfirmDialogComponent, {
          width: '400px',
          data: {
            titulo: 'Salir sin guardar',
            mensaje: '¿Salir sin guardar? Se perderán los productos agregados.',
            textoConfirmar: 'Sí, salir y borrar',
            colorBoton: '#f59e0b',
            icono: 'warning',
            colorIcono: '#f59e0b'
          }
        });

        dialogRef.afterClosed().subscribe(resultado => {
          if (resultado) {
            this.eliminarInventarioLocal(this.inventarioActual, false);
          }
        });
        
        return;
      }
      
      this.estado = "inicio";
      this.inventarioActual = null;
      return;
    }
    
    if (this.estado === "inicio") {
      this.router.navigate(["/home"]);
    }
  }

  aumentar() {
    this.cantidad++;
    this.calcularPorUnidades();
  }
  disminuir() {
    if (this.cantidad > 1) {
      this.cantidad--;
      this.calcularPorUnidades();
    }
  }

  reset() {
    this.codigo = "";
    this.cantidad = 1;
    this.cajas = 0;
    this.productoEncontrado = null;
    this.productoEditando = null;
  }

  abrirEscaner() {
    this.escanerAbierto = true;
    setTimeout(() => this.iniciarCamara(), 100);
  }

  iniciarCamara() {
    this.codeReader
      .decodeFromVideoDevice(
        undefined,
        this.videoElement.nativeElement,
        (result: any) => {
          if (result) {
            this.codigo = result.getText();
            this.cerrarEscaner();
            this.buscarProducto();
          }
        },
      )
      .then((c: any) => (this.controlesCamara = c))
      .catch((err) => console.error("Error cámara:", err));
  }

  cerrarEscaner() {
    this.escanerAbierto = false;
    if (this.controlesCamara) {
      this.controlesCamara.stop();
      this.controlesCamara = null;
    }
  }

  irAHistorial() {
    this.router.navigate(["/inventario/historial"]);
  }
}
