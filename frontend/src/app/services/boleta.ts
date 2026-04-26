import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class BoletaService {

  constructor() {}

  // 🔥 PUBLIC ENTRY POINT (what your component calls)
  print(boleta: any): void {
    this.generarBoleta(boleta);
  }

  // =========================
  // CORE GENERATOR
  // =========================
  private generarBoleta(boleta: any): void {

    const win = window.open('', '_blank', 'width=400,height=600');

    if (!win) {
      alert('No se pudo abrir la ventana de impresión');
      return;
    }

    win.document.write(`
      <html>
        <head>
          <title>Boleta</title>
          <style>
            body {
              font-family: monospace;
              width: 80mm;
              margin: 0;
              padding: 10px;
            }

            h3 {
              margin: 0;
              text-align: center;
            }

            hr {
              border: none;
              border-top: 1px dashed black;
              margin: 6px 0;
            }

            .right {
              text-align: right;
            }

            .bold {
              font-weight: bold;
            }

            .item {
              margin-bottom: 6px;
            }
          </style>
        </head>
        <body>

          ${this.buildHTML(boleta)}

          <script>
            window.onload = function() {
              setTimeout(() => window.print(), 200);
            }
          </script>

        </body>
      </html>
    `);

    win.document.close();
  }

  // =========================
  // HTML BUILDER
  // =========================
  private buildHTML(b: any): string {

    return `
      <h3>${b.empresa?.nombre || 'EMPRESA'}</h3>
      <div>RUT: ${b.empresa?.run || '-'}</div>
      <div>${b.empresa?.direccion || ''}</div>

      <hr>

      <div>Folio: ${b.movimientoId}</div>
      <div>Fecha: ${new Date(b.fecha).toLocaleString()}</div>

      <hr>

      ${(b.detalles || []).map((i: any) => `
        <div class="item">
          <div>${i.nombre}</div>
          <div>${i.cantidad} x ${this.formatNumber(i.precioUnitario)}</div>
          ${i.descuento > 0 ? `<div>Desc: -${this.formatNumber(i.descuento)}</div>` : ''}
          <div class="right bold">${this.formatNumber(i.total)}</div>
        </div>
      `).join('')}

      <hr>

      <div>Subtotal: ${this.formatNumber(b.subtotal || 0)}</div>
      ${b.descuentoGlobal > 0 ? `<div>Descuento: -${this.formatNumber(b.descuentoGlobal)}</div>` : ''}
      <div>IVA: ${this.formatNumber(b.iva || 0)}</div>

      <h3>Total: ${this.formatNumber(b.total || 0)}</h3>

      <div>Método: ${b.metodoPago}</div>
    `;
  }

  // =========================
  // FORMATTER
  // =========================
  private formatNumber(value: number): string {
    return new Intl.NumberFormat('es-CL').format(value || 0);
  }
}