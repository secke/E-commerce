import { Component, inject } from '@angular/core';
import { Input } from '@angular/core';
import { Product } from '../product';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductService } from '../services/product.service';

@Component({
  selector: 'app-product',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './product.component.html',
  styleUrl: './product.component.css'
})
export class ProductComponent {
  @Input() product!: Product;
  productService = inject(ProductService)
  currentImageIndex: number = 0;
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





  // getImageUrl(imagePath: string): string {
  //   if (!imagePath) return ''; // Handle null/undefined cases
    
  //   // Extract just the filename from the full path
  //   const filename = imagePath.split(/[/\\]/).pop();
    
  //   return `http://localhost:4000/media/resource/${filename}`;
  // }
}
