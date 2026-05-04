import { TestBed } from '@angular/core/testing';

import { PagoTarjeta } from './pago-tarjeta';

describe('PagoTarjeta', () => {
  let service: PagoTarjeta;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PagoTarjeta);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
