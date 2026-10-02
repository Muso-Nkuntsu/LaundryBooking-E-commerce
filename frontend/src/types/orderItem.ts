// Shape of an order item as returned by the backend (/order-item/...).
// Adjust the nested `order` and `product` fields if the backend sends different names.
export interface OrderItem {
  orderItemId: number;
  quantity: number;
  unitPrice: number;
  subtotal: number;
  order?: {
    orderId?: number | string;
    id?: number | string;
  };
  product?: {
    productId?: string;
    productName?: string;
    name?: string;
  };
}
