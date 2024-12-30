import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ProductService } from '../services/product.service';
import { Product } from '../product';

@Component({
  selector: 'app-product-details',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './product-details.component.html',
  styleUrl: './product-details.component.css'
})
export class ProductDetailsComponent {
  route: ActivatedRoute = inject(ActivatedRoute);
  productService = inject(ProductService);
  product: Product | undefined;
  currentImageIndex: number = 0;

  constructor() {
    const productId = this.route.snapshot.params['id'];
    this.productService.getProductById(productId).then(product => {
      this.product = product;
      console.log("THE RETRIEVED PRODUCT IS: ", this.product);
    })
  }

  // Navigate to the previous image
  previousImage(): void {
    if (this.product && this.product.images && this.product.images.length > 0) {
      this.currentImageIndex =
        (this.currentImageIndex - 1 + this.product.images.length) %
        this.product.images.length;
    }
  }

  // Navigate to the next image
  nextImage(): void {
    if (this.product && this.product.images && this.product.images.length > 0) {
      this.currentImageIndex =
        (this.currentImageIndex + 1) % this.product.images.length;
    }
  }
}
