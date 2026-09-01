import { useEffect, useState } from "react";

import { orderItemService } from "../../services/orderItemService";
import type { OrderItem } from "../../types/orderItem";

function OrderItems() {
  const [orderItems, setOrderItems] =
    useState<OrderItem[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState<string | null>(null);


  useEffect(() => {

    const loadOrderItems = async () => {

      try {
        setLoading(true);

        const data =
          await orderItemService.getAllOrderItems();

        setOrderItems(data);

      } catch (error) {

        setError(
          error instanceof Error
            ? error.message
            : "Failed to load order items"
        );

      } finally {
        setLoading(false);
      }
    };

    loadOrderItems();

  }, []);


  // Calculate total for all displayed items

  const total = orderItems.reduce(
    (sum, item) =>
      sum + item.subtotal,
    0
  );


  if (loading) {
    return (
      <div>
        <h1>Order Items</h1>
        <p>Loading order items...</p>
      </div>
    );
  }


  return (
    <div>

      <h1>Order Items</h1>

      {error && (
        <p>
          {error}
        </p>
      )}


      {orderItems.length === 0 ? (

        <p>No order items found.</p>

      ) : (

        <>
          <table>

            <thead>

              <tr>
                <th>Order ID</th>
                <th>Item</th>
                <th>Quantity</th>
                <th>Unit Price</th>
                <th>Subtotal</th>
              </tr>

            </thead>


            <tbody>

              {orderItems.map((item) => (

                <tr
                  key={item.orderItemId}
                >

                  <td>
                    {item.order?.orderId ||
                      item.order?.id ||
                      "N/A"}
                  </td>

                  <td>
                    {item.product?.productName ||
                      item.product?.name ||
                      "Unknown Product"}
                  </td>

                  <td>
                    {item.quantity}
                  </td>

                  <td>
                    R{item.unitPrice.toFixed(2)}
                  </td>

                  <td>
                    R{item.subtotal.toFixed(2)}
                  </td>

                </tr>

              ))}

            </tbody>

          </table>


          <h2>
            Total: R{total.toFixed(2)}
          </h2>

        </>
      )}

    </div>
  );
}

export default OrderItems;