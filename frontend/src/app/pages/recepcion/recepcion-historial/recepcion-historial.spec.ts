import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RecepcionHistorial } from './recepcion-historial';

describe('RecepcionHistorial', () => {
  let component: RecepcionHistorial;
  let fixture: ComponentFixture<RecepcionHistorial>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RecepcionHistorial]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RecepcionHistorial);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
