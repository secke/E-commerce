import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { ProductComponent } from './product/product.component';
import { ProductDetailsComponent } from './product-details/product-details.component';
import { UserDashboardComponent } from './user-dashboard/user-dashboard.component';
import { NewProductComponent } from './new-product/new-product.component';

export const routes: Routes = [
  { path: 'auth', loadChildren: () => import('./auth/auth.module').then(m => m.AuthModule) },
  { path: 'profile', loadChildren: () => import('./profile/profile.module').then(m => m.ProfileModule) },
  {
    path: '',
    component: HomeComponent,
    title: 'Home page',
  },
  {
    path: 'details/:id',
    component: ProductDetailsComponent,
    title: 'Product details',
  },
  {
    path: 'myProducts',
    component: UserDashboardComponent,
    title: 'User products',
  },
  {
    path: 'newProduct/:userId',
    component: NewProductComponent,
    title: 'New Product',
  },
  // Redirection par défaut vers "/auth/login"
  // { path: '', redirectTo: 'auth/login', pathMatch: 'full' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
