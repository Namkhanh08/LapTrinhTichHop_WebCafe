import React, { useEffect, useMemo, useState } from 'react';
import { Archive, Plus, X } from 'lucide-react';
import useStore from '../../store/useStore';

export default function Inventory() {
  const inventory = useStore(state => state.inventory);
  const fetchInventory = useStore(state => state.fetchInventory);
  const restockItem = useStore(state => state.restockItem);
  const [restockForm, setRestockForm] = useState(null);
  const [restockAmount, setRestockAmount] = useState('');

  useEffect(() => {
    fetchInventory();
  }, [fetchInventory]);

  const lowStock = useMemo(
    () => inventory.filter(item => Number(item.available) <= Number(item.reorderLevel)),
    [inventory]
  );

  const normalStock = useMemo(
    () => inventory.filter(item => Number(item.available) > Number(item.reorderLevel)),
    [inventory]
  );

  const handleRestock = async (event, id) => {
    event.preventDefault();
    if (!restockAmount || Number(restockAmount) <= 0) return;

    if (window.confirm(`Xác nhận nhập thêm ${restockAmount} cho mã ${id}?`)) {
      try {
        await restockItem(id, restockAmount);
        setRestockForm(null);
        setRestockAmount('');
        alert('Nhập kho thành công!');
      } catch (error) {
        console.error(error);
        alert(error.response?.data?.error || 'Nhập kho thất bại.');
      }
    }
  };

  const renderItems = (items, highlightLow = false) => (
    <div className="space-y-4 font-nunito">
      {items.map(item => (
        <div key={item.id} className="flex justify-between items-center pb-4 border-b border-gray-50 last:border-0">
          <div>
            <div className="font-bold text-primary">{item.name}</div>
            <div className="text-xs text-gray-500">Mã: {item.productId} · Kho: {item.location || '-'}</div>
            <div className="text-xs text-gray-400">Đã giữ: {item.reserved} {item.unit}</div>
          </div>
          <div className="text-right flex flex-col items-end gap-2">
            <div className={`font-bold text-xl ${highlightLow ? 'text-orange-500' : 'text-primary'}`}>
              {item.available} / {item.quantity} {item.unit}
            </div>

            {restockForm === item.id ? (
              <form onSubmit={(event) => handleRestock(event, item.id)} className="flex items-center gap-2">
                <input
                  type="number"
                  autoFocus
                  value={restockAmount}
                  onChange={(event) => setRestockAmount(event.target.value)}
                  className="w-20 px-2 py-1 text-sm border border-primary rounded-md outline-none"
                  placeholder="+ SL"
                />
                <button type="submit" className="bg-primary text-white p-1 rounded hover:bg-accent-1"><Plus size={16} /></button>
                <button type="button" onClick={() => setRestockForm(null)} className="text-gray-400 p-1 hover:text-red-500"><X size={16} /></button>
              </form>
            ) : (
              <button
                onClick={() => setRestockForm(item.id)}
                className="text-xs text-accent-1 font-bold flex items-center hover:bg-accent-1/10 px-2 py-1 rounded transition-colors"
              >
                <Plus size={12} className="mr-1" /> Nhập thêm
              </button>
            )}
          </div>
        </div>
      ))}
    </div>
  );

  return (
    <div className="animate-fade-in">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
        <div>
          <h1 className="font-montserrat font-bold text-2xl">Quản Lý Tồn Kho</h1>
          <p className="font-nunito text-primary/60 text-sm mt-1">Dữ liệu lấy trực tiếp từ inventory service.</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <h2 className="font-montserrat font-bold text-lg mb-6 flex items-center gap-2">
            <Archive size={20} className="text-green-600" /> Sản Phẩm Trong Kho
          </h2>
          {renderItems(normalStock)}
        </div>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <h2 className="font-montserrat font-bold text-lg mb-6 flex items-center gap-2">
            <Archive size={20} className="text-orange-600" /> Cảnh Báo Tồn Kho
          </h2>
          {lowStock.length > 0 ? renderItems(lowStock, true) : (
            <div className="text-sm text-gray-400 font-nunito">Không có mặt hàng dưới ngưỡng đặt lại.</div>
          )}
        </div>
      </div>
    </div>
  );
}
