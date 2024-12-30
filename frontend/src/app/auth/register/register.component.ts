import { Component } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { TokenService } from '../../services/token.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  registerData = {
    username: '',
    email: '',
    password: '',
    role: 'CLIENT', 
    avatar: null as File | null
  };
  
  constructor (
    private apiService: ApiService,
    private tokenService: TokenService
  ) { }

  async onRegister() {
    const formData = new FormData();
    
    formData.append('username', this.registerData.username);
    formData.append('email', this.registerData.email);
    formData.append('password', this.registerData.password);
    formData.append('role', this.registerData.role);

    if (this.registerData.avatar) {
      formData.append('file', this.registerData.avatar, this.registerData.avatar.name);
    }

    console.log("Form data before send: ", FormData);
    try {
      const data: any = await this.apiService.request(
        'users/register',
        'POST',
        formData,
        { 
          'Content-Type': 'multipart/form-data' 
        }
      );
      
      this.tokenService.saveToken(data.token);
      console.log("Token saved to lclst");

      const decodedToken = this.tokenService.decodeToken(data.token);
      console.log("The decoded token: ", decodedToken);
      
      localStorage.setItem('user', decodedToken);

      alert('Registration successful!');
    } catch (error: any) {
      console.error('Registration error:', error);
      alert(error.message);
    }
  }

  onFileChange(event: any) {
    const file = event.target.files[0];
    this.registerData.avatar = file;
  }
}