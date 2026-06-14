import React, { useState, useEffect } from 'react';
import { getOrders } from '../services/api';
import { formatRupee } from '../utils/currency';

const STATUS_STEPS = ['PLACED', 'PAID', 'KITCHEN_PREPARING', 'OUT_FOR_DELIVERY', 'DELIVERED'];

function getStepState(step, status) {
  if (status === 'CANCELLED') return step === 'PLACED' ? 'active' : 'pending';
  const currentIdx = STATUS_STEPS.indexOf(status);
  const stepIdx = STATUS_STEPS.indexOf(step);
  if (currentIdx === -1) return 'pending';
  if (stepIdx < currentIdx) return 'done';
  if (stepIdx === currentIdx) return 'active';
  return 'pending';
}

function Track() {
  const [orders, setOrders] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetch = async () => {
      try {
        const data = await getOrders();
        const sorted = data.sort((a, b) => b.id - a.id);
        setOrders(sorted);
        setSelectedId((prev) => prev ?? sorted[0]?.id ?? null);
        setError(null);
      } catch {
        setError('Cannot reach order-service. Is it running on port 8081?');
      }
    };
    fetch();
    const interval = setInterval(fetch, 2000);
    return () => clearInterval(interval);
  }, []);

  const order = orders.find((o) => o.id === selectedId);

  return (
    <div className="track-page">
      <h2 className="page-title">Track Order</h2>
      <p className="page-subtitle">Live status from Camunda workflow · polls every 2s</p>

      {error && <p className="error-banner">{error}</p>}

      {orders.length === 0 && !error ? (
        <p className="empty-state">No orders yet. Place one from the Order tab.</p>
      ) : order && (
        <>
          <div className="order-picker">
            {orders.map((o) => (
              <button
                key={o.id}
                className={`order-pill${o.id === selectedId ? ' active' : ''}`}
                onClick={() => setSelectedId(o.id)}
              >
                #{o.id} · {o.customerName}
              </button>
            ))}
          </div>

          <div className="track-card">
            <div className="track-status-header">
              <div>
                <span className="track-order-id">Order #{order.id}</span>
                <p className="track-order-item">{order.item} · {formatRupee(order.amount)}</p>
              </div>
              <span className={`status-badge status-${(order.status || '').toLowerCase().replace('_', '-')}`}>
                {order.status}
              </span>
            </div>

            {order.status === 'CANCELLED' ? (
              <p className="track-cancelled">Payment failed — order was cancelled by the workflow.</p>
            ) : (
              <div className="track-steps">
                {STATUS_STEPS.map((step, i) => (
                  <div key={step} className={`track-step ${getStepState(step, order.status)}`}>
                    <div className="step-indicator">
                      <div className="step-dot" />
                      {i < STATUS_STEPS.length - 1 && <div className="step-line" />}
                    </div>
                    <span className="step-label">{step.replace(/_/g, ' ')}</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}

export default Track;
