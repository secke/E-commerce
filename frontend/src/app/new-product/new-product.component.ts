import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NewProduct } from '../new-product';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-new-product',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './new-product.component.html',
  styleUrl: './new-product.component.css'
})
export class NewProductComponent {
  @Input() isPopupVisible: boolean = false;
  @Output() close = new EventEmitter<void>();
  @Output() productCreated = new EventEmitter<NewProduct>();
  // @Input() userId: string = '';


  newProduct: NewProduct = {
    description: '',
    images: [] as File[],
    name: '',
    price: 0,
    quantity: 0,
    userId: '',
  };

  uploadedImages: { file: File; previewUrl: string }[] = [];

  closePopup() {
    this.close.emit();

  }

  onImageUpload(event: Event) {
    const files = (event.target as HTMLInputElement).files;
    if (files) {
      Array.from(files).forEach((file) => {
        const reader = new FileReader();
        reader.onload = () => {
          this.uploadedImages.push({ file, previewUrl: reader.result as string });
        };
        reader.readAsDataURL(file);
      });
    }
  }

  onSubmit() {
    this.newProduct.images = this.uploadedImages.map((image) => image.file);

    console.log('New Product:', this.newProduct);
    this.productCreated.emit(this.newProduct);
    this.closePopup();
    // Clear form fields
    this.newProduct = {
      description: '',
      images: [],
      name: '',
      price: 0,
      quantity: 0,
      userId: '',
    };
    this.uploadedImages = [];
  }
}
