import { Component } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { TokenService } from '../../services/token.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})

export class LoginComponent {
  loginData = {
    name: '',
    password: '',
  };

  constructor(
    private apiService: ApiService,
    private tokenService: TokenService,
    private router: Router
  ) {}

  async onLogin() {

    try {
      const data: any = await this.apiService.request(
        'users/login',
        'POST',
        this.loginData 
      );
      console.log('Connexion réussie !', data);

      const token = data.token;
      this.tokenService.saveToken(token);

      const decodedToken = this.tokenService.decodeToken(token);
      console.log('Token décrypté :', decodedToken);

      this.router.navigate(['/']);
    } catch (error: any) {
      console.error('Erreur de connexion :', error);
      alert(error.message);
    }
  }
}
