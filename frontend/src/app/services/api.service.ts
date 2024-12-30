import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})

export class ApiService {
  private baseUrl = 'http://localhost:4000/api'; 

  constructor() {}

  /**
   * Permet de configurer dynamiquement le `baseUrl`.
   * @param url Nouvelle URL de base.
   */
  setBaseUrl(url: string): void {
    this.baseUrl = url;
  }

  /**
   * Méthode générique pour effectuer des appels API avec `fetch`.
   * @param endpoint Chemin relatif de l'API (ex : 'users/login').
   * @param method Méthode HTTP (GET, POST, PUT, DELETE).
   * @param body (optionnel) Données pour les requêtes POST ou PUT.
   * @param headers (optionnel) En-têtes HTTP supplémentaires.
   * @returns Une Promise contenant la réponse JSON ou une erreur.
   */

  request(
    endpoint: string, 
    method: string, 
    body?: any,
    headers: any = {}
  ): Promise<any> {
    const url = `${this.baseUrl}/${endpoint}`;
    
    const requestOptions: RequestInit = {
      method: method,
      body: body instanceof FormData ? body : JSON.stringify(body)
    };
  
    // Only add Content-Type if not FormData
    if (body && method !== 'GET' && method !== 'HEAD') {
      if (!(body instanceof FormData)) {
        requestOptions.headers = {
          'Content-Type': 'application/json',
          ...headers
        };
      }
    }
  
    return fetch(url, requestOptions)
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.json();
      });
  }
}
