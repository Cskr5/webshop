import {Component, OnInit} from '@angular/core';
import {Product} from "../product";
import {ProductService} from "../../utils/product-service";

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.css']
})
export class ProductListComponent implements OnInit {

  products: Array<Product>;

  constructor(private productService: ProductService) { }

  ngOnInit(): void {
    this.products = new Array<Product>();

    this.productService.getActiveProducts().subscribe(
      data => {
        this.products = data;
        console.log(data);
      }, error => console.log(error)
    );
  }

  calculateDiscountedPrice(product: Product): number {
    let actualProduct = this.products.find(prod => prod === product);
    return (actualProduct.price / 100) * (100 - actualProduct.discount);
  }
}
