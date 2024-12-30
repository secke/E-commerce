import { inject, Injectable } from '@angular/core';
import { Product } from '../product';
import { TokenService } from './token.service';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  urlBase:string = "http://localhost:4000/api";
  tokenService = inject(TokenService)
  constructor() { }

  async getProducts():Promise<Product[]> {
    console.log('DATA FROM SERVER: ')
    const data = await fetch(`${this.urlBase}/products`);
    // console.log('DATA RECEIVED SERVER: ', await data.text())
    return (await data.json()) ?? [];
  }
  getImageUrl(imagePath: string): string {
    if (!imagePath) return '';
    
    const filename = imagePath.split(/[/\\]/).pop();
    
    return `http://localhost:4000/media/resource/${filename}`;
  }

  async getProductById(id: string): Promise<Product> {
    const resp = await fetch(`${this.urlBase}/products/getById/${id}`);
    return (await resp.json()) ?? {};
  }

  async getProductByUserId(): Promise<Product[]> {
    const resp = await fetch(`${this.urlBase}/users/get`, {
      headers: {'Authorization': 'Bearer '+this.tokenService.getToken()}
    });
    return (await resp.json()) ?? [];
  }
// ############# This section must refactor the code by allowing the components to use the service directly instead of 
// ############# implementing their own code
  // // Navigate to the previous image
  // previousImage(product: Product, currentImageIndex: number): void {
  //   if (product && product.images && product.images.length > 0) {
  //     currentImageIndex =
  //       (currentImageIndex - 1 + product.images.length) %
  //       product.images.length;
  //   }
  // }

  // // Navigate to the next image
  // nextImage(product: Product, currentImageIndex: number): void {
  //   if (product && product.images && product.images.length > 0) {
  //     currentImageIndex =
  //     (currentImageIndex + 1) % product.images.length;
  //     console.log("clicked------------------------", currentImageIndex)
  //   }
  // }
}
