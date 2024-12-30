import { Component, OnInit, Inject, PLATFORM_ID, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { TokenService } from '../../services/token.service';
import { isPlatformBrowser } from '@angular/common';
import { ProductService } from '../../services/product.service';
import { ProductComponent } from '../../product/product.component';
import { RouterModule } from '@angular/router';


interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  quantity: number;
  images: { id: string, imagePath: string, productId: string }[];
  userId: string;
}

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.css'],
})

export class UserComponent implements OnInit {
  products: Product[] = [];
  userName: string = '';
  userId: string = '';
  userRole: string = '';
  userAvatar: string = '';
  showPopup: boolean = false;
  showDeleteConfirmation = false;
  updatedUserName = "";
  updatedAvatar: string | null = null;
  // loading = true;  

  constructor(
    private apiService: ApiService,
    private tokenService: TokenService,
    public productService: ProductService,
    @Inject(PLATFORM_ID) private platformId: any
  ) {}

  openSettings() {
    this.showPopup = true;
  }

  closePopup() {
    this.showPopup = false;
  }

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.loadUserProfile();
      this.loadUserProducts();
    } else {
      console.log("Le code s’exécute côté serveur, localStorage indisponible.");
    }
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

  async loadUserProducts(): Promise<void> {
    const token = this.tokenService.getToken();

    if (!token) {
      console.error('Aucun token trouvé.');
      return;
    }

    const decodedToken = this.tokenService.decodeToken(token);

    if (!decodedToken || !decodedToken.userId) {
      console.error('Le token est invalide ou ne contient pas userId.');
      return;
    }

    const userId = decodedToken.userId;
    // console.log("the user id is: ", userId)
    
    try {
      const response: any = await fetch(`http://localhost:4000/api/products/user/${userId}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })
      
      this.products = await response.json();
      console.log('Produits récupérés :',
        this.products.forEach(product => console.log(product.images[0].imagePath))
      );

    } catch (error) {
      console.error('Erreur lors de la récupération des produits :', error);
    }
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

    this.closePopup();
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
    this.closePopup();
  }

  
  //to implement
  logout() {}


}