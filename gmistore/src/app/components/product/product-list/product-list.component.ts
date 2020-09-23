import {ChangeDetectorRef, Component, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ProductModel} from "../../../models/product-model";
import {ProductService} from "../../../service/product-service";
import {CartService} from "../../../service/cart-service";
import {Subscription} from "rxjs";
import {MatSnackBar, MatSnackBarHorizontalPosition, MatSnackBarVerticalPosition} from "@angular/material/snack-bar";
import {Title} from "@angular/platform-browser";
import {SideNavComponent} from "../../side-nav/side-nav.component";
import {ActivatedRoute, ParamMap, Router} from "@angular/router";
import {SpinnerService} from "../../../service/spinner-service.service";
import {MatDialogRef} from "@angular/material/dialog";
import {LoadingSpinnerComponent} from "../../loading-spinner/loading-spinner.component";
import {FormBuilder, FormGroup} from "@angular/forms";
import {MatPaginator, PageEvent} from "@angular/material/paginator";
import {PagedProductListModel} from "../../../models/product/paged-product-list.model";

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.css'],
})
export class ProductListComponent implements OnInit, OnDestroy {

  @Input() products: Array<ProductModel>;
  @ViewChild(MatPaginator) paginator: MatPaginator;

  filterForm: FormGroup = this.fb.group({
    minimumPrice: [null],
    maximumPrice: [null]
  });

  numberOfProducts = 0;
  pageIndex: number = 0;
  pageSize: number = 10;
  pageSizeOptions: Array<number> = [10, 20, 50];

  spinner: MatDialogRef<LoadingSpinnerComponent> = this.spinnerService.start();
  horizontalPosition: MatSnackBarHorizontalPosition = 'center';
  verticalPosition: MatSnackBarVerticalPosition = 'bottom';

  category: string;
  categoryDisplayName: string;

  minPrice: number = 1;
  maxPrice: number = 1;

  minimumPrice: number = 1;
  maximumPrice: number = 1;

  productsSubscription: Subscription;
  addToCartSubscription: Subscription;
  paramsSubscription: Subscription;

  constructor(private productService: ProductService, private cartService: CartService,
              private snackBar: MatSnackBar, private titleService: Title,
              private sideNavComponent: SideNavComponent, private activatedRoute: ActivatedRoute,
              private spinnerService: SpinnerService, private fb: FormBuilder,
              private router: Router, private cdRef: ChangeDetectorRef) {
    this.products = new Array<ProductModel>();
  }

  ngOnInit(): void {
    this.titleService.setTitle("Termékek - GMI Store");
    this.paramsSubscription = this.activatedRoute.queryParamMap.subscribe(
      (params: ParamMap) => {
        this.spinnerService.stop(this.spinner);
        this.category = params.get('category');
        this.pageIndex = Number(params.get('pageIndex'));
        this.pageSize = Number(params.get('pageSize'));
        this.fetchProductsByCategory();
      }, (error) => {
        console.log(error);
      }, () => {
      }
    );
  }

  private fetchProductsByCategory() {
    if (this.category) {
      this.spinner = this.spinnerService.start();
      this.productsSubscription = this.productService.getProductsByCategory(
        this.category, this.pageIndex, this.pageSize
      )
        .subscribe(
          (response: PagedProductListModel) => {
            this.products = response.products;
            this.categoryDisplayName = response.categoryDisplayName;
            this.numberOfProducts = response.totalElements;
            this.setMinAndMaxPrices();
            this.spinnerService.stop(this.spinner);
          }, error => {
            console.log(error)
            this.spinnerService.stop(this.spinner);
          }
        )
    }
  }

  setMinAndMaxPrices() {
    this.products.forEach(
      (product: ProductModel) => {
        if (product.price > this.maximumPrice) {
          this.maximumPrice = product.price;
          this.maxPrice = product.price;
        }
        if (product.price < this.minimumPrice) {
          this.minimumPrice = product.price;
          this.minPrice = product.price;
        }
      }
    );
  }

  paginationEventHandler($event: PageEvent) {
    this.pageSize = $event.pageSize;
    this.pageIndex = $event.pageIndex;
    this.router.navigate(['.'], {
      relativeTo: this.activatedRoute,
      queryParams: {
        category: this.category,
        pageIndex: this.pageIndex,
        pageSize: this.pageSize
      }
    });
  }

  calculateDiscountedPrice(product: ProductModel): number {
    let actualProduct = this.products.find(prod => prod === product);
    return (actualProduct.price / 100) * (100 - actualProduct.discount);
  }

  addToCart(id: number) {
    this.spinner = this.spinnerService.start();
    this.addToCartSubscription = this.cartService.addProduct(id).subscribe(
      (response) => {
        if (response) {
          this.openSnackBar('A termék a kosárba került!');
          this.sideNavComponent.updateItemsInCart(0);
        } else {
          this.openSnackBar("A kért mennyiség nincs készleten!");
        }
        this.spinnerService.stop(this.spinner);
      }, (error) => {
        console.log(error);
        this.spinnerService.stop(this.spinner);
        this.openSnackBar("Valami hiba történt!");
      }
    )
  }

  openSnackBar(message: string) {
    this.snackBar.open(message, 'OK', {
      duration: 2000,
      horizontalPosition: this.horizontalPosition,
      verticalPosition: this.verticalPosition,
    });
  }

  formatLabel(value: number) {
    if (value >= 1000) {
      return Math.round(value / 1000) + 'k';
    }
    return value;
  }

  updateMinimumPrice(minPriceElement: HTMLInputElement) {
    minPriceElement.value = this.minimumPrice.toString();
  }

  updateMaximumPrice(maxPriceElement: HTMLInputElement) {
    maxPriceElement.value = this.maximumPrice.toString();
  }

  ngOnDestroy() {
    this.productsSubscription.unsubscribe();
    this.paramsSubscription.unsubscribe();
    if (this.addToCartSubscription) {
      this.addToCartSubscription.unsubscribe();
    }
  }

  detectChanges() {
    this.cdRef.detectChanges();
  }
}
