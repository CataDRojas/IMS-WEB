import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class BoletaService {

  constructor() {}

  print(boleta: any): void {
    this.generarBoleta(boleta);
  }

  private generarBoleta(boleta: any): void {
    const win = window.open('', '_blank', 'width=400,height=600');

    if (!win) {
      alert('No se pudo abrir la ventana de impresión');
      return;
    }

    win.document.write(`
      <html>
        <head>
          <title>Ticket de Venta</title>
          <style>
            * { box-sizing: border-box; }
            body {
              font-family: 'Courier New', Courier, monospace;
              width: 80mm; /* Ancho de impresora térmica estándar */
              margin: 0;
              padding: 5mm; 
              color: #000;
              font-size: 12px;
              line-height: 1.4;
            }
            .text-center { text-align: center; }
            h2 { font-size: 18px; margin: 0 0 5px 0; text-transform: uppercase; font-weight: bold; }
            p { margin: 0; }
            hr.dashed { border: none; border-top: 1px dashed #000; margin: 10px 0; }
            
            .header-info p { margin-bottom: 3px; }
            
            .item-row { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px; }
            .item-name { flex: 1; padding-right: 5px; word-break: break-word; }
            .item-qty-price { color: #444; font-size: 11px; display: inline-block; margin-top: 2px;}
            .item-total { font-weight: bold; }
            
            .totals-wrapper { margin-top: 10px; }
            .total-row { display: flex; justify-content: space-between; margin-bottom: 4px; }
            .total-row.final { font-size: 16px; font-weight: bold; border-top: 1px solid #000; padding-top: 5px; margin-top: 5px; }
            
            .footer-msg { margin-top: 20px; font-size: 11px; color: #444; }

            /* Ocultar elementos extra al imprimir */
            @media print {
              @page { margin: 0; }
              body { margin: 0; padding: 0; }
            }
          </style>
        </head>
        <body>
          ${this.buildHTML(boleta)}
          <script>
            window.onload = function() {
              setTimeout(() => window.print(), 300);
            }
          </script>
        </body>
      </html>
    `);

    win.document.close();
  }

  private buildHTML(b: any): string {
    return `
      <div class="text-center">
        <h2>${b.empresa?.nombre || 'EMPRESA'}</h2>
        <p>RUT: ${b.empresa?.rut || '-'}</p>
        <p>${b.empresa?.direccion || ''}</p>
      </div>

      <hr class="dashed">

      <div class="header-info">
        <p><strong>Folio:</strong> ${b.folio || b.movimientoId}</p>
        <p><strong>Fecha:</strong> ${new Date(b.fecha).toLocaleString()}</p>
      </div>

      <hr class="dashed">

      <div class="items-list">
        ${(b.items || []).map((i: any) => `
          <div class="item-row">
            <div class="item-name">
              ${i.nombre} <br>
              <span class="item-qty-price">${i.cantidad} x $${this.formatNumber(i.precioUnitario)}</span>
              ${i.descuento > 0 ? `<br><span class="item-qty-price">Desc: -$${this.formatNumber(i.descuento)}</span>` : ''}
            </div>
            <div class="item-total">$${this.formatNumber(i.total)}</div>
          </div>
        `).join('')}
      </div>

      <hr class="dashed">

      <div class="totals-wrapper">
        <div class="total-row"><span>Subtotal:</span> <span>$${this.formatNumber(b.subtotal || 0)}</span></div>
        ${b.descuentoGlobal > 0 ? `<div class="total-row"><span>Descuento:</span> <span>-$${this.formatNumber(b.descuentoGlobal)}</span></div>` : ''}
        <div class="total-row"><span>IVA:</span> <span>$${this.formatNumber(b.iva || 0)}</span></div>
        
        <div class="total-row final"><span>TOTAL:</span> <span>$${this.formatNumber(b.total || 0)}</span></div>
      </div>

      <hr class="dashed">

      <div class="text-center">
        <p>Método de Pago: <strong>${b.metodoPago}</strong></p>
        ${b.vuelto > 0 ? `<p>Vuelto: <strong>$${this.formatNumber(b.vuelto)}</strong></p>` : ''}
      </div>

      <div class="footer-msg text-center">
        <p>¡Gracias por su compra!</p>
        <p>Conserve esta boleta como comprobante</p>
      </div>
    `;
  }

  private formatNumber(value: number): string {
    return new Intl.NumberFormat('es-CL').format(value || 0);
  }
}