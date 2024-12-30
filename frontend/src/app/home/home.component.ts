import { Component, inject } from '@angular/core';
import { ProductService } from '../services/product.service';
import { Product } from '../product';
import { ProductComponent } from '../product/product.component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, ProductComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {
   productService = inject(ProductService);
   myProds: Product[] = [];
    constructor() {
      this.productService.getProducts().then((allProducts:Product[])=>{
        // console.log(allProducts);
        this.myProds = allProducts;
        // console.log("ALL THE PRODUCTS FROM SERVER: ", this.myProds)
      });
    }
}
