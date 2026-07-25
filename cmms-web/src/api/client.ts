import axios from 'axios';

const CREDENTIALS_KEY = 'cmms.basicAuth';

export function saveCredentials(email: string, password: string) {
  const token = btoa(`${email}:${password}`);
  localStorage.setItem(CREDENTIALS_KEY, token);
}

export function clearCredentials() {
  localStorage.removeItem(CREDENTIALS_KEY);
}

export function getCredentials(): string | null {
  return localStorage.getItem(CREDENTIALS_KEY);
}

export const api = axios.create({
  baseURL: '/api',
});

api.interceptors.request.use((config) => {
  const token = getCredentials();
  if (token) {
    config.headers.Authorization = `Basic ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      clearCredentials();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
