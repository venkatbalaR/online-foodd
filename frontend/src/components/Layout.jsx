import React from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import CartDrawer from './CartDrawer';

function Layout() {
  return (
    <div className="app-shell">
      <header className="top-header">
        <div className="header-brand">
          <div className="brand-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z" />
              <line x1="3" y1="6" x2="21" y2="6" />
              <path d="M16 10a4 4 0 01-8 0" />
            </svg>
          </div>
          <div>
            <h1 className="brand-name">FoodFlow</h1>
            <p className="brand-tagline">Chennai · Microservices · Spring Boot · Camunda</p>
          </div>
        </div>
        <div className="header-status">
          <span className="status-dot" />
          All systems online
        </div>
      </header>

      <main className="main-area">
        <Outlet />
      </main>

      <nav className="bottom-nav">
        <NavLink to="/" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`} end>
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z" />
            <line x1="3" y1="6" x2="21" y2="6" />
            <path d="M16 10a4 4 0 01-8 0" />
          </svg>
          <span>Order</span>
        </NavLink>
        <NavLink to="/track" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}>
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="10" r="3" />
            <path d="M12 21.7C17.3 17 20 13 20 10a8 8 0 10-16 0c0 3 2.7 7 8 11.7z" />
          </svg>
          <span>Track</span>
        </NavLink>
        <NavLink to="/kitchen" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}>
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M3 2v7c0 1.1.9 2 2 2h4a2 2 0 002-2V2" />
            <path d="M7 2v20" />
            <path d="M21 15V2a5 5 0 00-5 5v6c0 1.1.9 2 2 2h3zm0 0v7" />
          </svg>
          <span>Kitchen</span>
        </NavLink>
      </nav>

      <button className="help-fab" aria-label="Help" title="Orders over ₹2,500 fail payment">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <circle cx="12" cy="12" r="10" />
          <path d="M9.09 9a3 3 0 015.83 1c0 2-3 3-3 3" />
          <line x1="12" y1="17" x2="12.01" y2="17" />
        </svg>
      </button>

      <CartDrawer />
    </div>
  );
}

export default Layout;
