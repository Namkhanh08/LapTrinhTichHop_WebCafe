import React, { useEffect, useMemo, useState } from 'react';
import { Plus, Edit2, Trash2, Search, X } from 'lucide-react';
import useStore from '../../store/useStore';

const emptyForm = {
  name: '',
  description: '',
  price: '',
  category_id: 1,
  type: 'arabica',
  region: '',
  altitude: '',
  processing_method: '',
  roast_level: 'medium',
  flavor_notes: '',
  image_url: '/assets/img/section2/image1.png',
  stock: 0,
  is_active: true
};

export default function AdminProducts() {
  const {
    products,
    fetchProducts,
    createProduct,
    updateProduct,
    deleteProduct
  } = useStore();
  const [searchTerm, setSearchTerm] = useState('');
  const [editingProduct, setEditingProduct] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    fetchProducts();
  }, [fetchProducts]);

  const filteredProducts = useMemo(() => {
    const term = searchTerm.trim().toLowerCase();
    if (!term) return products || [];
    return (products || []).filter((product) =>
      [product.Name, product.name, product.type, product.category_name]
        .filter(Boolean)
        .some((value) => value.toString().toLowerCase().includes(term))
    );
  }, [products, searchTerm]);

  const openCreate = () => {
    setEditingProduct(null);
    setForm(emptyForm);
    setShowModal(true);
  };

  const openEdit = (product) => {
    setEditingProduct(product);
    setForm({
      name: product.Name || product.name || '',
      description: product.Description || product.description || '',
      price: product.Price || product.price || '',
      category_id: product.CategoryId || product.category_id || 1,
      type: product.type || 'arabica',
      region: product.region || '',
      altitude: product.altitude || '',
      processing_method: product.processing_method || product.process || '',
      roast_level: product.roast_level || product.roast || 'medium',
      flavor_notes: product.FlavorNotes || product.flavor_notes || product.flavorNotes || '',
      image_url: product.ImageUrl || product.image_url || product.image || '',
      stock: product.Stock ?? product.stock ?? 0,
      is_active: product.is_active ?? product.isActive ?? true
    });
    setShowModal(true);
  };

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;
    setForm((current) => ({
      ...current,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSave = async (event) => {
    event.preventDefault();
    setSaving(true);
    try {
      const payload = {
        ...form,
        price: Number(form.price),
        category_id: Number(form.category_id),
        stock: Number(form.stock),
        process_method: form.processing_method
      };

      if (editingProduct) {
        await updateProduct(editingProduct.Id || editingProduct.id, payload);
        alert('Cập nhật sản phẩm thành công.');
      } else {
        await createProduct(payload);
        alert('Tạo sản phẩm thành công.');
      }

      setShowModal(false);
      setEditingProduct(null);
      setForm(emptyForm);
    } catch (error) {
      console.error(error);
      alert(error.response?.data?.message || error.response?.data?.error || 'Lưu sản phẩm thất bại.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (product) => {
    const id = product.Id || product.id;
    if (!window.confirm(`Xóa sản phẩm ${product.Name || product.name}?`)) return;
    try {
      await deleteProduct(id);
      alert('Đã xóa sản phẩm.');
    } catch (error) {
      console.error(error);
      alert(error.response?.data?.message || error.response?.data?.error || 'Xóa sản phẩm thất bại.');
    }
  };

  const productStatus = (product) => {
    const stock = product.Stock ?? product.stock ?? 0;
    if (stock <= 0) return 'Hết hàng';
    if (stock < 100) return 'Sắp hết';
    return 'Còn hàng';
  };

  return (
    <div className="animate-fade-in">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
        <h1 className="font-montserrat font-bold text-2xl">Danh Mục Sản Phẩm</h1>
        <div className="flex gap-4 w-full md:w-auto">
          <div className="relative flex-1 md:w-64">
            <input
              type="text"
              placeholder="Tìm tên sản phẩm..."
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
              className="w-full pl-10 pr-4 py-2 bg-white border border-gray-200 rounded-xl focus:outline-none focus:border-primary font-nunito text-sm"
            />
            <Search size={18} className="absolute left-3 top-2.5 text-gray-400" />
          </div>
          <button
            onClick={openCreate}
            className="bg-primary text-white px-4 py-2 rounded-xl font-bold font-nunito flex items-center gap-2 hover:bg-accent-1 transition-colors whitespace-nowrap"
          >
            <Plus size={18} /> Thêm Sản Phẩm
          </button>
        </div>
      </div>

      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left font-nunito text-sm whitespace-nowrap">
            <thead className="bg-gray-50 text-gray-600 font-bold border-b border-gray-100">
              <tr>
                <th className="px-6 py-4">Mã SP</th>
                <th className="px-6 py-4">Tên Sản Phẩm</th>
                <th className="px-6 py-4">Loại Cà Phê</th>
                <th className="px-6 py-4 text-right">Đơn Giá</th>
                <th className="px-6 py-4 text-center">Tồn Kho</th>
                <th className="px-6 py-4 text-center">Trạng Thái</th>
                <th className="px-6 py-4 text-center">Hành Động</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {filteredProducts.map((product) => {
                const status = productStatus(product);
                return (
                  <tr key={product.Id || product.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-6 py-4 font-bold text-gray-500">{product.Id || product.id}</td>
                    <td className="px-6 py-4 font-bold text-primary">{product.Name || product.name}</td>
                    <td className="px-6 py-4">{product.type || product.category_name || '-'}</td>
                    <td className="px-6 py-4 font-bold text-accent-1 text-right">
                      {(product.Price || product.price || 0).toLocaleString('vi-VN')}đ
                    </td>
                    <td className="px-6 py-4 text-center font-bold">{product.Stock ?? product.stock ?? 0}</td>
                    <td className="px-6 py-4 text-center">
                      <span className={`px-3 py-1 rounded-full text-xs font-bold ${
                        status === 'Còn hàng' ? 'bg-green-100 text-green-600' :
                        status === 'Sắp hết' ? 'bg-orange-100 text-orange-600' :
                        'bg-red-100 text-red-600'
                      }`}>
                        {status}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center justify-center gap-2">
                        <button
                          onClick={() => openEdit(product)}
                          className="p-1.5 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                          title="Chỉnh sửa"
                        >
                          <Edit2 size={18} />
                        </button>
                        <button
                          onClick={() => handleDelete(product)}
                          className="p-1.5 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                          title="Xóa"
                        >
                          <Trash2 size={18} />
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {showModal && (
        <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center p-4">
          <form onSubmit={handleSave} className="bg-white rounded-2xl p-6 w-full max-w-3xl space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="font-montserrat font-bold text-2xl">
                {editingProduct ? 'Chỉnh Sửa Sản Phẩm' : 'Thêm Sản Phẩm'}
              </h2>
              <button type="button" onClick={() => setShowModal(false)} className="text-gray-400 hover:text-red-500">
                <X size={22} />
              </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <input name="name" value={form.name} onChange={handleChange} required placeholder="Tên sản phẩm" className="border p-3 rounded-xl" />
              <input name="price" value={form.price} onChange={handleChange} required type="number" min="1" placeholder="Giá" className="border p-3 rounded-xl" />
              <input name="type" value={form.type} onChange={handleChange} placeholder="Loại" className="border p-3 rounded-xl" />
              <input name="category_id" value={form.category_id} onChange={handleChange} type="number" min="1" placeholder="Mã danh mục" className="border p-3 rounded-xl" />
              <input name="region" value={form.region} onChange={handleChange} placeholder="Vùng trồng" className="border p-3 rounded-xl" />
              <input name="altitude" value={form.altitude} onChange={handleChange} placeholder="Độ cao" className="border p-3 rounded-xl" />
              <input name="processing_method" value={form.processing_method} onChange={handleChange} placeholder="Phương pháp sơ chế" className="border p-3 rounded-xl" />
              <select name="roast_level" value={form.roast_level} onChange={handleChange} className="border p-3 rounded-xl">
                <option value="light">Light</option>
                <option value="medium">Medium</option>
                <option value="medium-dark">Medium Dark</option>
                <option value="dark">Dark</option>
              </select>
              <input name="stock" value={form.stock} onChange={handleChange} type="number" min="0" placeholder="Tồn kho" className="border p-3 rounded-xl" />
              <input name="image_url" value={form.image_url} onChange={handleChange} placeholder="Ảnh" className="border p-3 rounded-xl" />
              <textarea name="description" value={form.description} onChange={handleChange} placeholder="Mô tả" className="border p-3 rounded-xl md:col-span-2" rows="3" />
              <textarea name="flavor_notes" value={form.flavor_notes} onChange={handleChange} placeholder="Hương vị, cách nhau bằng dấu phẩy" className="border p-3 rounded-xl md:col-span-2" rows="2" />
            </div>

            <button disabled={saving} className="w-full bg-primary text-white py-3 rounded-xl font-bold hover:bg-accent-1 disabled:opacity-60">
              {saving ? 'Đang lưu...' : 'Lưu sản phẩm'}
            </button>
          </form>
        </div>
      )}
    </div>
  );
}
