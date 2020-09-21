import {OrderItemModel} from "./orderItemModel";
import {AddressModel} from "../address-model";
import {OrderUserDetailsModel} from "./orderUserDetailsModel";

export interface OrderDetails {
  generatedUniqueId: string;
  status: string;
  items: OrderItemModel[];
  shippingMethod?: string;
  deliveryMode?: string;
  paymentMethod: string;
  deliveryAddress: AddressModel;
  invoiceAddress: AddressModel;
  deliveryCost: number;
  totalPrice: number;
  orderedAt: Date;
  expectedDeliveryDate: Date;
  deliveredAt: Date;
  user: OrderUserDetailsModel;
}