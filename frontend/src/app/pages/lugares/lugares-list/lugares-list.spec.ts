import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LugaresList } from './lugares-list';

describe('LugaresList', () => {
  let component: LugaresList;
  let fixture: ComponentFixture<LugaresList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LugaresList]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LugaresList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
