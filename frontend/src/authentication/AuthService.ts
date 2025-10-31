import axios, { AxiosError, AxiosHeaders, AxiosResponse } from "axios";

const API_URL = process.env.REACT_APP_API_URL;

class AuthService {
  async login(email: string, password: string): Promise<AxiosResponse> {
    try {
      const response = await axios.post(API_URL + "login", {
        email,
        password,
      });
      localStorage.setItem("user", response.data.email);
      localStorage.setItem("token", response.data.token);
      return response;
    } catch (error) {
      console.error(error);
      throw error;
    }
  }

  logout() {
    localStorage.removeItem("user");
    localStorage.removeItem("token");
    window.location.href = "/login";
  }

  async register(email: string, password: string): Promise<AxiosResponse> {
    try {
      return await axios.post(API_URL + "signup", {
        email,
        password,
      });
    } catch (error) {
      console.error(error);
      throw error;
    }
  }

  getCurrentUser() {
    return localStorage.getItem("user");
  }

  getToken() {
    return localStorage.getItem("token");
  }

  private manageError(error: any) {
    if (
      error instanceof AxiosError &&
      error.response &&
      error.response.status === 401
    ) {
      console.debug(
        "Unauthorized, probably due to token expiration, logging out."
      );
      this.logout();
      return;
    }
    console.error(error);
  }

  private addToken(headers: AxiosHeaders) {
    const token = localStorage.getItem("token");
    if (token) {
      headers["Authorization"] = "Bearer " + token;
    }
  }

  async get(url: string, headers: AxiosHeaders): Promise<AxiosResponse> {
    this.addToken(headers);
    return axios
      .get(API_URL + url, { headers: headers })
      .catch((error: any) => {
        if (error instanceof AxiosError) {
          this.manageError(error);
        }
        throw error;
      });
  }

  async post(
    url: string,
    data: any,
    headers: AxiosHeaders
  ): Promise<AxiosResponse> {
    this.addToken(headers);
    try {
      return await axios.post(API_URL + url, data, { headers: headers });
    } catch (error) {
      if (error instanceof AxiosError) {
        this.manageError(error);
      }
      throw error;
    }
  }

  async put(
    url: string,
    data: any,
    headers: AxiosHeaders
  ): Promise<AxiosResponse> {
    this.addToken(headers);
    return axios
      .put(API_URL + url, data, { headers: headers })
      .catch((error: any) => {
        this.manageError(error);
        throw error;
      });
  }

  async delete(url: string, headers: AxiosHeaders): Promise<AxiosResponse> {
    this.addToken(headers);
    return axios
      .delete(API_URL + url, { headers: headers })
      .catch((error: any) => {
        this.manageError(error);
        throw error;
      });
  }
}

const service = new AuthService();
export default service;
