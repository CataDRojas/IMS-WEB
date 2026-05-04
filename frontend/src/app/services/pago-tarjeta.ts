import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class PagoTarjeta {

  constructor() {}

  procesarPago(venta: any) {

    const resumen = this.buildResumen(venta);

    alert(
`💳 PAGO CON TARJETA

No implementado aún.
Dependería del sistema externo (Transbank, GetNet, etc.)

-------------------------
DETALLE VENTA
-------------------------
${resumen}
`
    );
  }

  private buildResumen(venta: any): string {

    let lineas = '';

    if (venta?.detalles?.length) {
      lineas = venta.detalles.map((d: any) =>
        `- ${d.productoNombre} x${d.movimientoDetalleCantidad} = ${d.movimientoDetallePrecioTotal}`
      ).join('\n');
    }

    return `
Total: ${venta.totalFinal}
Método: ${venta.metodoPago}

${lineas}
`;
  }
}