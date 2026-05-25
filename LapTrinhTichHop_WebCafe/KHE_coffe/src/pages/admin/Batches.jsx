import React, { useEffect, useState } from 'react';
import { Plus, History, X } from 'lucide-react';
import useStore from '../../store/useStore';

const emptyBatch = {
  productId: '1',
  productName: 'REVO Morning',
  quantity: 1,
  roastDate: new Date().toISOString().slice(0, 10),
  roastLevel: 'Medium',
  originRegion: '',
  processMethod: '',
  notes: '',
  status: 'roasting',
  createdBy: 'Admin Revo'
};

const statusLabels = {
  roasting: 'Đang rang',
  cooling: 'Làm nguội',
  quality_check: 'Kiểm định',
  packaging: 'Đóng gói',
  completed: 'Hoàn thành',
  rejected: 'Loại bỏ'
};

export default function Batches() {
  const { batches, products, fetchBatches, fetchProducts, createBatch, updateBatchStatus } = useStore();
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState(emptyBatch);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    fetchBatches();
    fetchProducts();
  }, [fetchBatches, fetchProducts]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    if (name === 'productId') {
      const product = products.find(p => String(p.Id || p.id) === String(value));
      setForm(current => ({
        ...current,
        productId: value,
        productName: product?.Name || product?.name || current.productName
      }));
      return;
    }
    setForm(current => ({ ...current, [name]: value }));
  };

  const handleCreate = async (event) => {
    event.preventDefault();
    setSaving(true);
    try {
      await createBatch({
        ...form,
        quantity: Number(form.quantity)
      });
      setShowModal(false);
      setForm(emptyBatch);
      alert('Đã ghi lô rang mới.');
    } catch (error) {
      console.error(error);
      alert(error.response?.data?.error || 'Ghi lô rang thất bại.');
    } finally {
      setSaving(false);
    }
  };

  const handleStatusChange = async (batch, status) => {
    try {
      await updateBatchStatus(batch.id, status);
    } catch (error) {
      console.error(error);
      alert(error.response?.data?.error || 'Cập nhật trạng thái thất bại.');
    }
  };

  return (
    <div className="animate-fade-in">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
        <div>
          <h1 className="font-montserrat font-bold text-2xl">Quản Lý Lô Rang</h1>
          <p className="font-nunito text-primary/60 text-sm mt-1">Dữ liệu lấy trực tiếp từ batch service.</p>
        </div>
        <button
          onClick={() => setShowModal(true)}
          className="bg-primary text-white px-4 py-2 rounded-xl font-bold font-nunito flex items-center gap-2 hover:bg-accent-1 transition-colors whitespace-nowrap"
        >
          <Plus size={18} /> Ghi Lô Rang Mới
        </button>
      </div>

      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
        <h2 className="font-montserrat font-bold text-lg mb-6 flex items-center gap-2">
          <History size={20} className="text-accent-1" /> Lịch sử rang gần đây
        </h2>
        <div className="overflow-x-auto">
          <table className="w-full text-left font-nunito text-sm whitespace-nowrap">
            <thead className="bg-pinky-gray/50 text-gray-600 font-bold border-y border-gray-100">
              <tr>
                <th className="px-4 py-4">Mã Lô</th>
                <th className="px-4 py-4">Ngày Rang</th>
                <th className="px-4 py-4">Sản Phẩm</th>
                <th className="px-4 py-4 text-center">Mức Rang</th>
                <th className="px-4 py-4 text-right">Khối Lượng</th>
                <th className="px-4 py-4">Người Tạo</th>
                <th className="px-4 py-4 text-center">Trạng Thái</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {(batches || []).map(batch => (
                <tr key={batch.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-4 py-4 font-bold text-primary">{batch.batchCode}</td>
                  <td className="px-4 py-4">{batch.roastDate}</td>
                  <td className="px-4 py-4 font-bold">{batch.productName}</td>
                  <td className="px-4 py-4 text-center">
                    <span className="bg-gray-100 text-gray-600 px-2 py-1 rounded text-xs font-bold">{batch.roastLevel}</span>
                  </td>
                  <td className="px-4 py-4 text-right font-bold text-accent-1">{batch.quantity} kg</td>
                  <td className="px-4 py-4">{batch.createdBy || '-'}</td>
                  <td className="px-4 py-4 text-center">
                    <select
                      value={batch.status}
                      onChange={(event) => handleStatusChange(batch, event.target.value)}
                      className="px-3 py-1 rounded-full text-xs font-bold bg-blue-50 text-blue-700 outline-none"
                    >
                      {Object.entries(statusLabels).map(([value, label]) => (
                        <option key={value} value={value}>{label}</option>
                      ))}
                    </select>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {showModal && (
        <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center p-4">
          <form onSubmit={handleCreate} className="bg-white rounded-2xl p-6 w-full max-w-2xl space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="font-montserrat font-bold text-2xl">Ghi Lô Rang Mới</h2>
              <button type="button" onClick={() => setShowModal(false)} className="text-gray-400 hover:text-red-500">
                <X size={22} />
              </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <select name="productId" value={form.productId} onChange={handleChange} className="border p-3 rounded-xl">
                {(products || []).map(product => (
                  <option key={product.Id || product.id} value={product.Id || product.id}>
                    {product.Name || product.name}
                  </option>
                ))}
              </select>
              <input name="quantity" value={form.quantity} onChange={handleChange} type="number" min="1" required placeholder="Khối lượng kg" className="border p-3 rounded-xl" />
              <input name="roastDate" value={form.roastDate} onChange={handleChange} type="date" required className="border p-3 rounded-xl" />
              <input name="roastLevel" value={form.roastLevel} onChange={handleChange} required placeholder="Mức rang" className="border p-3 rounded-xl" />
              <input name="originRegion" value={form.originRegion} onChange={handleChange} placeholder="Vùng nguyên liệu" className="border p-3 rounded-xl" />
              <input name="processMethod" value={form.processMethod} onChange={handleChange} placeholder="Sơ chế" className="border p-3 rounded-xl" />
              <textarea name="notes" value={form.notes} onChange={handleChange} placeholder="Ghi chú" className="border p-3 rounded-xl md:col-span-2" rows="3" />
            </div>

            <button disabled={saving} className="w-full bg-primary text-white py-3 rounded-xl font-bold hover:bg-accent-1 disabled:opacity-60">
              {saving ? 'Đang lưu...' : 'Lưu lô rang'}
            </button>
          </form>
        </div>
      )}
    </div>
  );
}
