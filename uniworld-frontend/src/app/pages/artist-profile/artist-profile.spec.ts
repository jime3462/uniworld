import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ArtistProfile } from './artist-profile';

describe('ArtistProfile', () => {
  let component: ArtistProfile;
  let fixture: ComponentFixture<ArtistProfile>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ArtistProfile]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ArtistProfile);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
