import React, { useState } from 'react';
import { formatRupee } from '../utils/currency';
import { CATEGORY_FALLBACK } from '../data/foodImages';

const BADGES = {
  bestseller: { label: 'Bestseller', className: 'badge-bestseller', icon: '🔥' },
  popular: { label: 'Popular', className: 'badge-popular', icon: '⭐' },
};

function FoodCard({ item, onAdd }) {
  const badge = item.badge ? BADGES[item.badge] : null;
  const fallback = CATEGORY_FALLBACK[item.category] || CATEGORY_FALLBACK['Rice & Biryani'];
  const [imgSrc, setImgSrc] = useState(item.image);

  return (
    <article className="food-card">
      <div className="food-card-image">
        <img
          src={imgSrc}
          alt={item.name}
          loading="lazy"
          onError={() => { if (imgSrc !== fallback) setImgSrc(fallback); }}
        />
        <div className="food-badges">
          {badge && (
            <span className={`food-badge ${badge.className}`}>
              {badge.icon} {badge.label}
            </span>
          )}
          {item.vegan && (
            <span className="food-badge badge-vegan">
              🌿 Pure Veg
            </span>
          )}
        </div>
      </div>
      <div className="food-card-body">
        <div className="food-card-title-row">
          <h3>{item.name}</h3>
          <span className="food-price">{formatRupee(item.price)}</span>
        </div>
        {item.restaurant && (
          <p className="food-restaurant">📍 {item.restaurant} · {item.location}</p>
        )}
        <p className="food-description">{item.description}</p>
        <div className="food-meta">
          <span className="meta-item">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="#f97316" stroke="none">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
            </svg>
            {item.rating}
          </span>
          <span className="meta-item">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="10" />
              <polyline points="12 6 12 12 16 14" />
            </svg>
            {item.prepTime} min
          </span>
          <span className="meta-item meta-cal">{item.calories} cal</span>
        </div>
        <div className="food-card-footer">
          <button className="btn-add" onClick={() => onAdd(item)}>Add</button>
        </div>
      </div>
    </article>
  );
}

export default FoodCard;
