import React, { useState, useEffect } from 'react';
import { getOrders } from '../services/api';

const COLUMNS = {
  new: { title: 'New Orders', statuses: ['PLACED', 'PAID'], color: 'yellow' },
  preparing: { title: 'Preparing', statuses: ['KITCHEN_PREPARING'], color: 'orange' },
  ready: { title: 'Ready for Pickup', statuses: ['OUT_FOR_DELIVERY'], color: 'green' },
};

function formatOrderId(id) {
  return `ORD-${String(id).padStart(6, '0')}`;
}

function timeAgo(dateStr) {
  if (!dateStr) return 'just now';
  const diff = Date.now() - new Date(dateStr).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return 'just now';
  if (mins === 1) return '1 min ago';
  if (mins < 60) return `${mins} min ago`;
  const hrs = Math.floor(mins / 60);
  return hrs === 1 ? '1 hr ago' : `${hrs} hr ago`;
}

function parseItems(itemStr) {
  if (!itemStr) return [];
  return itemStr.split(',').map((s) => s.trim()).filter(Boolean);
}

function isUrgent(order) {
  if (!['PLACED', 'PAID'].includes(order.status)) return false;
  if (!order.createdAt) return false;
  return Date.now() - new Date(order.createdAt).getTime() > 3 * 60 * 1000;
}

function KitchenOrderCard({ order, column }) {
  const items = parseItems(order.item);
  const urgent = isUrgent(order);

  const statusLabel = {
    PLACED: 'New',
    PAID: 'New',
    KITCHEN_PREPARING: 'Preparing',
    OUT_FOR_DELIVERY: 'Ready',
  }[order.status] || order.status;

  const actionBtn = {
    new: { label: 'Start', icon: 'chef' },
    preparing: { label: 'Mark Ready', icon: 'box' },
    ready: { label: 'Dispatched', icon: 'check' },
  }[column];

  return (
    <div className={`kanban-card${urgent ? ' urgent' : ''}`}>
      <div className="kanban-card-top">
        <span className="kanban-order-id">{formatOrderId(order.id)}</span>
        <div className="kanban-card-badges">
          {urgent && <span className="badge-urgent">🔥 URGENT</span>}
          <span className={`kanban-status-pill pill-${column}`}>{statusLabel}</span>
        </div>
      </div>
      <p className="kanban-customer">{order.customerName}</p>
      <ul className="kanban-items">
        {items.map((item, i) => (
          <li key={i}>{item}</li>
        ))}
      </ul>
      <div className="kanban-card-footer">
        <span className="kanban-time">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="10" />
            <polyline points="12 6 12 12 16 14" />
          </svg>
          {timeAgo(order.createdAt)}
        </span>
        <button type="button" className={`kanban-action btn-${column}`} title="Handled automatically by Camunda workflow">
          {actionBtn.icon === 'chef' && (
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M3 2v7c0 1.1.9 2 2 2h4a2 2 0 002-2V2" />
              <path d="M7 2v20" />
              <path d="M21 15V2a5 5 0 00-5 5v6c0 1.1.9 2 2 2h3zm0 0v7" />
            </svg>
          )}
          {actionBtn.icon === 'box' && (
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z" />
              <polyline points="3.27 6.96 12 12.01 20.73 6.96" />
              <line x1="12" y1="22.08" x2="12" y2="12" />
            </svg>
          )}
          {actionBtn.icon === 'check' && (
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M22 11.08V12a10 10 0 11-5.93-9.14" />
              <polyline points="22 4 12 14.01 9 11.01" />
            </svg>
          )}
          {actionBtn.label}
        </button>
      </div>
    </div>
  );
}

function KitchenColumn({ title, color, orders, columnKey }) {
  return (
    <div className={`kanban-column col-${color}`}>
      <div className="kanban-column-header">
        <span className={`kanban-col-dot dot-${color}`} />
        <h3>{title}</h3>
        <span className="kanban-col-count">{orders.length}</span>
      </div>
      <div className="kanban-column-body">
        {orders.length === 0 ? (
          <p className="kanban-empty">No orders</p>
        ) : (
          orders.map((order) => (
            <KitchenOrderCard key={order.id} order={order} column={columnKey} />
          ))
        )}
      </div>
    </div>
  );
}

function Kitchen() {
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetch = async () => {
      try {
        const data = await getOrders();
        setOrders(data.sort((a, b) => b.id - a.id));
        setError(null);
      } catch {
        setError('Cannot reach order-service.');
      }
    };
    fetch();
    const interval = setInterval(fetch, 2000);
    return () => clearInterval(interval);
  }, []);

  const today = new Date().toDateString();
  const completedToday = orders.filter(
    (o) => o.status === 'DELIVERED' && o.createdAt && new Date(o.createdAt).toDateString() === today
  ).length;

  const newOrders = orders.filter((o) => COLUMNS.new.statuses.includes(o.status));
  const preparing = orders.filter((o) => COLUMNS.preparing.statuses.includes(o.status));
  const ready = orders.filter((o) => COLUMNS.ready.statuses.includes(o.status));
  const hasUrgent = newOrders.some(isUrgent);

  return (
    <div className="kitchen-page">
      <div className="kitchen-page-header">
        <div>
          <h2 className="page-title">Kitchen Dashboard</h2>
          <p className="page-subtitle">Real-time order queue management</p>
        </div>
        <div className="kitchen-completed-badge">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
            <polyline points="20 6 9 17 4 12" />
          </svg>
          {completedToday} completed today
        </div>
      </div>

      {error && <p className="error-banner">{error}</p>}

      <div className="kanban-summary">
        <div className="summary-pill summary-yellow">
          <span className="summary-count">{newOrders.length}</span>
          <span className="summary-label">New</span>
        </div>
        <div className="summary-pill summary-orange">
          <span className="summary-count">{preparing.length}</span>
          <span className="summary-label">Preparing</span>
        </div>
        <div className="summary-pill summary-green">
          <span className="summary-count">{ready.length}</span>
          <span className="summary-label">Ready</span>
        </div>
      </div>

      <div className="kanban-board">
        <KitchenColumn title="New Orders" color="yellow" orders={newOrders} columnKey="new" />
        <KitchenColumn title="Preparing" color="orange" orders={preparing} columnKey="preparing" />
        <KitchenColumn title="Ready for Pickup" color="green" orders={ready} columnKey="ready" />
      </div>

      {hasUrgent && (
        <div className="kitchen-urgent-toast">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="10" />
            <polyline points="12 6 12 12 16 14" />
          </svg>
          Urgent order waiting!
        </div>
      )}
    </div>
  );
}

export default Kitchen;
