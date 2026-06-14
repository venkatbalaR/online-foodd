import React, { useState } from 'react';
import { useCart } from '../context/CartContext';
import { createOrder } from '../services/api';
import { formatRupee, PAYMENT_FAIL_THRESHOLD } from '../utils/currency';

function CartDrawer() {
  const { cart, isOpen, setIsOpen, removeFromCart, clearCart, totalPrice } = useCart();
  const [customerName, setCustomerName] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);

  if (!isOpen) return null;

  const handleCheckout = async () => {
    if (!customerName.trim()) {
      setMessage({ type: 'error', text: 'Please enter your name' });
      return;
    }
    if (cart.length === 0) return;

    setLoading(true);
    setMessage(null);
    try {
      const itemNames = cart.map((i) => `${i.name}${i.quantity > 1 ? ` x${i.quantity}` : ''}`).join(', ');
      await createOrder({
        customerName: customerName.trim(),
        item: itemNames,
        amount: parseFloat(totalPrice.toFixed(2)),
      });
      setMessage({ type: 'success', text: 'Order placed! Track it in the Track tab.' });
      clearCart();
      setCustomerName('');
    } catch {
      setMessage({ type: 'error', text: 'Failed to place order. Is order-service running?' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="cart-overlay" onClick={() => setIsOpen(false)}>
      <div className="cart-drawer" onClick={(e) => e.stopPropagation()}>
        <div className="cart-header">
          <h2>Your Cart</h2>
          <button className="cart-close" onClick={() => setIsOpen(false)} aria-label="Close">×</button>
        </div>

        <div className="cart-customer">
          <label htmlFor="customerName">Your Name</label>
          <input
            id="customerName"
            type="text"
            placeholder="e.g. Arjun Kumar"
            value={customerName}
            onChange={(e) => setCustomerName(e.target.value)}
          />
        </div>

        {message && <p className={`cart-message ${message.type}`}>{message.text}</p>}

        {cart.length === 0 ? (
          <p className="cart-empty">Your cart is empty. Add something delicious!</p>
        ) : (
          <>
            <ul className="cart-list">
              {cart.map((item) => (
                <li key={item.id} className="cart-item">
                  <div>
                    <strong>{item.name}</strong>
                    <span className="cart-item-qty">× {item.quantity}</span>
                  </div>
                  <div className="cart-item-actions">
                    <span className="cart-item-price">{formatRupee(item.price * item.quantity)}</span>
                    <button onClick={() => removeFromCart(item.id)} className="cart-remove">Remove</button>
                  </div>
                </li>
              ))}
            </ul>
            <div className="cart-footer">
              <div className="cart-total">
                <span>Total</span>
                <strong>{formatRupee(totalPrice)}</strong>
              </div>
              {totalPrice > PAYMENT_FAIL_THRESHOLD && (
                <p className="cart-warning">Orders over {formatRupee(PAYMENT_FAIL_THRESHOLD)} will fail payment (demo rule)</p>
              )}
              <button className="btn-checkout" onClick={handleCheckout} disabled={loading}>
                {loading ? 'Placing Order…' : 'Place Order'}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

export default CartDrawer;
