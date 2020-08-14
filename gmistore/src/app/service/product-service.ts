import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ProductModel} from "../models/product-model";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private productsUrl = environment.apiUrl + 'api/products/';
  private imageUploadUrl = environment.apiUrl + 'api/images/upload';

  constructor(private httpClient: HttpClient) {
  }

  addProduct(product: ProductModel): Observable<any> {
    return this.httpClient.post(this.productsUrl, product);
  }

  getProductBySlug(slug: string): Observable<ProductModel> {
    return this.httpClient.get<ProductModel>(this.productsUrl + slug);
  }

  getActiveProducts(): Observable<ProductModel[]> {
    return this.httpClient.get<ProductModel[]>(this.productsUrl);
  }

  getProductCategories(): Observable<String[]> {
    return this.httpClient.get<String[]>(this.productsUrl + 'categories');
  }

  updateProduct(product: ProductModel, slug: string): Observable<any> {
    return this.httpClient.put(this.productsUrl + slug, product);
  }

  public async uploadImage(image: File) {
    const uploadData = new FormData();
    uploadData.append('picture', image);

    return await this.httpClient.post(this.imageUploadUrl, uploadData).toPromise();
  }
}
