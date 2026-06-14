import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { CartProvider } from './context/CartContext';
import Layout from './components/Layout';
import OrderFood from './pages/OrderFood';
import Track from './pages/Track';
import Kitchen from './pages/Kitchen';

function App() {
  return (
    <CartProvider>
      <Router>
        <Routes>
          <Route element={<Layout />}>
            <Route path="/" element={<OrderFood />} />
            <Route path="/track" element={<Track />} />
            <Route path="/kitchen" element={<Kitchen />} />
          </Route>
        </Routes>
      </Router>
    </CartProvider>
  );
}

export default App;
