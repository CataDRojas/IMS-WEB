import { TestBed } from '@angular/core/testing';

import { Lugar } from './lugar';

describe('Lugar', () => {
  let service: Lugar;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Lugar);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
