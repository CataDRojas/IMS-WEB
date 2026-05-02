import { TestBed } from '@angular/core/testing';

import { Recepcion } from './recepcion';

describe('Recepcion', () => {
  let service: Recepcion;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Recepcion);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
