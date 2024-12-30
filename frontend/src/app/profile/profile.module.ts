import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ProfileRoutingModule } from './profile-routing.module';
import { UserComponent } from './user/user.component';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [UserComponent],
  imports: [
    CommonModule,
    ProfileRoutingModule,
    MatIconModule,
    FormsModule
  ]
})
export class ProfileModule { }
