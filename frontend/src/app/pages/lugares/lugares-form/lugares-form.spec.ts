import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LugaresForm } from './lugares-form';

describe('LugaresForm', () => {
  let component: LugaresForm;
  let fixture: ComponentFixture<LugaresForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LugaresForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LugaresForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
