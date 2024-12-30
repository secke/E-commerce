import { Component, inject, OnInit } from '@angular/core';
import { ProductService } from '../services/product.service';
import { TokenService } from '../services/token.service';
import { Product } from '../product';
import { CommonModule } from '@angular/common';
import { ProductComponent } from '../product/product.component';
import { RouterModule } from '@angular/router';
import { NewProductComponent } from '../new-product/new-product.component';
import { NewProduct } from '../new-product';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [CommonModule, ProductComponent, RouterModule, NewProductComponent, MatIconModule, FormsModule],
  templateUrl: './user-dashboard.component.html',
  styleUrl: './user-dashboard.component.css'
})

export class UserDashboardComponent implements OnInit{

  userName: string = '';
  userId: string = '';
  userRole: string = '';
  userAvatar: string = '';
  showPopup: boolean = false;
  showDeleteConfirmation = false;
  updatedUserName = "";
  updatedAvatar: string | null = null;

  productService = inject(ProductService);
  tokenService = inject(TokenService);
  userProducts: Product[] = [];
  token: any = this.tokenService.decodeToken(this.tokenService.getToken() || '');
  isPopupVisible = false;

  constructor() {
    if (this.token) {
      // console.log('TOKEN: ', this.token.userId);
      this.userId = this.token.userId;
      this.productService.getProductByUserId().then((userProducts)=> {
        this.userProducts = userProducts;
        console.log("All user products: ", this.userProducts);
      })
    }
  }

  ngOnInit(): void {
      this.loadUserProfile();
  }

  openPopup() {
    this.isPopupVisible = true;
  }

  closePopup() {
    this.isPopupVisible = false;
  }

  onProductCreated(product: NewProduct) {
    product.userId = this.userId;
    console.log('Product Created:', product);
    this.closePopup();

    // Save the product or update the product list here
  }


  async loadUserProfile(): Promise<void> {

    const token = this.tokenService.getToken();
    if (!token) {
      console.error('Aucun token trouvé.');
      return;
    }

    const decodedToken = this.tokenService.decodeToken(token);
    if (!decodedToken) {
      console.error('Token invalide.');
      return;
    }

    console.log("decodedToken id: ", decodedToken);

    try {
      const response: any = await fetch(`http://localhost:4000/api/users/${decodedToken.userId}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })
      
      const resp = await response.json();
      // this.userAvatar = URL.createObjectURL(blob);
      console.log('Avatar récupéré :', resp);

      // this.userAvatar = resp.avatar || '/assets/default-avatar.png';
    } catch (error) {
      console.error('Erreur lors de la récupération de l\'avatar :', error);
      this.userAvatar = '/assets/default-avatar.jpg';
    }
    
    console.log('Token décrypté :', decodedToken);
    this.userName = decodedToken.sub || 'Utilisateur';
    this.userId = decodedToken.userId || '';
    this.userRole = decodedToken.role?.[0]?.authority || 'N/A';
    this.userAvatar = "http://localhost:4000/api/users/Avatars/" + decodedToken.avatar || '/assets/default-avatar.png';
  }

    //to implement
    async changeUserAvatar() {
      const token = this.tokenService.getToken();
      if (!token) {
        return;
      }
  
      try {
        const response: any = await fetch(`http://localhost:4000/api/users/${this.userId}`, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            avatar: this.userAvatar,
          })
        })
        
        //to implement
        this.userAvatar = this.userAvatar;
  
      } catch (error) {
        console.error('Error changeUserAvatar :', error);
      }
    }
  
    async updateUserName() {
      console.log('Updated Username:', this.updatedUserName);
  
      const token = this.tokenService.getToken();
      if (!token) {
        return;
      }
  
      console.log("Request body:", JSON.stringify({ username: this.updatedUserName }));
  
      try {
        const response: any = await fetch(`http://localhost:4000/api/users/${this.userId}`, {
          method: 'PATCH',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          },
  
          body: JSON.stringify({ username: this.updatedUserName })
        })
  
        const data: any = await response.json();
        
        console.log("data with new token: ", data);
  
        console.log("decoded new token: ", this.tokenService.decodeToken(data.token));
        
        
        this.tokenService.saveToken(data.token);
  
      } catch (error) {
        console.error('Error changeUserInfos :', error);
      }
  
      this.closePopup2();
    }
    
    onAvatarSelected(event: Event) {
      const file = (event.target as HTMLInputElement).files?.[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = () => this.updatedAvatar = reader.result as string;
        reader.readAsDataURL(file);
      }
    }
  
    confirmDeleteAccount() {
      this.showDeleteConfirmation = true;
    }
    
    closeDeleteConfirmation() {
      this.showDeleteConfirmation = false;
    }
    
    async deleteAccount() {
      console.log('Account Deleted');
  
      const token = this.tokenService.getToken();
      if (!token) {
        console.error('Aucun token trouvé.');
        return;
      }
  
      const response = await fetch(`http://localhost:4000/api/users/${this.userId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
        }
      })
  
      const data = await response;
      if (data) {
        this.tokenService.clearToken()
      }
      
      this.closeDeleteConfirmation();
      this.closePopup2();
    }

    openSettings() {
      this.showPopup = true;
    }

    closePopup2() {
      this.showPopup = false;
    }
}
