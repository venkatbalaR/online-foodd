import React, { useState } from 'react';
import CategoryFilter from '../components/CategoryFilter';
import FoodCard from '../components/FoodCard';
import { CATEGORIES, MENU_ITEMS } from '../data/menuData';
import { useCart } from '../context/CartContext';

function OrderFood() {
  const [activeCategory, setActiveCategory] = useState('All');
  const { addToCart, totalItems, setIsOpen } = useCart();

  const filtered = activeCategory === 'All'
    ? MENU_ITEMS
    : MENU_ITEMS.filter((item) => item.category === activeCategory);

  return (
    <div className="order-page">
      <div className="page-header">
        <div>
          <h2 className="page-title">Order Food</h2>
          <p className="page-subtitle">Authentic Chennai flavours, delivered hot</p>
        </div>
        <button className="btn-cart" onClick={() => setIsOpen(true)}>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="9" cy="21" r="1" />
            <circle cx="20" cy="21" r="1" />
            <path d="M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6" />
          </svg>
          Cart
          {totalItems > 0 && <span className="cart-badge">{totalItems}</span>}
        </button>
      </div>

      <CategoryFilter categories={CATEGORIES} active={activeCategory} onChange={setActiveCategory} />

      <div className="food-grid">
        {filtered.map((item) => (
          <FoodCard key={item.id} item={item} onAdd={addToCart} />
        ))}
      </div>
    </div>
  );
}

export default OrderFood;
