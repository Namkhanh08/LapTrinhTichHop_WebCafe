import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import useStore from '../store/useStore';
import API from '../services/api.js';

import image1 from '../assets/img/section2/image1.png';
import image2 from '../assets/img/section2/image2.png';
import image3 from '../assets/img/section2/image3.png';
import image4 from '../assets/img/section2/image4.png';
import image5 from '../assets/img/section2/image5.png';
import { LuPercent } from "react-icons/lu";
import { BsFillTicketPerforatedFill, BsShieldCheck } from "react-icons/bs";
import { MdOutlineLocalShipping, MdNavigateBefore } from "react-icons/md";
import { IoBagCheck, IoCartOutline } from "react-icons/io5";

export default function ProductDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const addToCart = useStore((state) => state.addToCart);

  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [grindType, setGrindType] = useState(null);
  const [flavorNotes, setFlavorNotes] = useState('Original');
  const [weights, setWeight] = useState("250g");
  const [publicVouchers, setPublicVouchers] = useState([]);
  const [toastMessage, setToastMessage] = useState(null);

  // Hiển thị thông báo Toast thay cho alert() thô sơ
  const showToast = (msg) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 3000);
  };

  useEffect(() => {
    const fetchPublicVouchers = async () => {
      try {
        const res = await API.getPublicVouchers();
        setPublicVouchers(res.data);
      } catch (err) {
        console.error(err);
      }
    };
    fetchPublicVouchers();
  }, []);

  const ImageMap = {
    '/assets/img/section2/image1.png': image1,
    '/assets/img/section2/image2.png': image2,
    '/assets/img/section2/image3.png': image3,
    '/assets/img/section2/image4.png': image4,
    '/assets/img/section2/image5.png': image5,
  };

  const CategoryMap = {
    '1': 'Arabica',
    '2': 'Blend',
    '3': 'Robusta',
    '4': 'Fine Robusta',
  };

  // Tách các mảng tùy chọn an toàn từ dữ liệu API
  const flavorOptions = product?.flavorNotes ? product.flavorNotes.split(',').map(f => f.trim()) : ["whole-bean"];
  const weightOptions = product?.weight ? product.weight.split(',').map(f => f.trim()) : ["250g"];
  const grindOptions = product?.grindingOptions || [];

  useEffect(() => {
    if (flavorOptions.length > 0) setFlavorNotes(flavorOptions[0]);
  }, [product]);

  useEffect(() => {
    if (weightOptions.length > 0) setWeight(weightOptions[0]);
  }, [product]);

  useEffect(() => {
    if (grindOptions.length > 0) setGrindType(grindOptions[0]);
  }, [product]);

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        const res = await API.getProductById(id);
        const p = res.data;
        const formatted = {
          id: p.Id,
          name: p.product?.Name,
          desc: p.product?.Description,
          price: p.product?.Price,
          image: p.product?.ImageUrl,
          region: p.Region,
          process: p.Process,
          roast: p.Roast,
          flavorNotes: p.FlavorNotes,
          type: p.product?.CategoryId,
          grindingOptions: p.GrindingOption || [],
          weight: p.Weight,
          height: p.Height
        };
        setProduct(formatted);
      } catch (err) {
        console.error("Lỗi lấy sản phẩm: ", err);
        setError('Không thể tải sản phẩm. Vui lòng thử lại sau.');
      } finally {
        setLoading(false);
      }
    };
    fetchProduct();
  }, [id]);

  const handleAddToCart = async () => {
    if (!grindType) {
      showToast("⚠️ Vui lòng chọn kiểu xay trước khi thêm vào giỏ.");
      return;
    }
    try {
      await addToCart(product, quantity, grindType.Id, flavorNotes, weights);
      showToast("🎉 Đã thêm sản phẩm vào giỏ hàng thành công!");
    } catch (err) {
      console.error("Lỗi thêm vào giỏ hàng: ", err);
      showToast("❌ Không thể thêm vào giỏ hàng. Thử lại sau.");
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 font-nunito">
        <div className="text-center">
          <div className="w-10 h-10 border-4 border-stone-900 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-zinc-500 text-sm tracking-wide">Đang tải chi tiết sản phẩm...</p>
        </div>
      </div>
    );
  }

  if (error) return <div className="min-h-screen flex items-center justify-center text-red-500 bg-gray-50 font-nunito text-sm">{error}</div>;
  if (!product) return <div className="min-h-screen flex items-center justify-center text-zinc-500 bg-gray-50 font-nunito text-sm">Sản phẩm không tồn tại.</div>;

  return (
    <div className="bg-[#f5f5f5] min-h-screen py-6 font-nunito text-zinc-800 relative selection:bg-stone-900 selection:text-white">
      
      {/* Toast Notification */}
      {toastMessage && (
        <div className="fixed top-24 left-1/2 transform -translate-x-1/2 z-50 bg-zinc-900/95 text-white px-6 py-3 rounded-xl shadow-2xl backdrop-blur-sm text-sm font-medium border border-white/10 animate-fade-in tracking-wide">
          {toastMessage}
        </div>
      )}

      <div className="container mx-auto px-4 max-w-6xl">
        
        {/* Breadcrumb / Back Button */}
        <button 
          onClick={() => navigate('/shop')} 
          className="text-zinc-500 font-semibold text-xs uppercase tracking-wider mb-4 hover:text-stone-900 flex items-center gap-1 transition-colors"
        >
          <MdNavigateBefore size={18} /> Cửa hàng / Chi tiết sản phẩm / <span className="text-zinc-800 font-bold">{product.name}</span>
        </button>

        {/* Main Product Box */}
        <div className="bg-white rounded-2xl shadow-[0_4px_20px_rgba(0,0,0,0.03)] p-6 flex flex-col md:flex-row gap-8">
          
          {/* Left Column: Image Gallery (Shopee Style Aspect-Square) */}
          <div className="w-full md:w-[45%] flex flex-col gap-4">
            <div className="w-full aspect-square bg-zinc-50 border border-zinc-100 rounded-xl flex items-center justify-center p-6 relative group overflow-hidden">
              <span className="absolute top-3 left-3 bg-red-600 text-white text-[10px] uppercase font-bold tracking-widest px-2.5 py-1 rounded-2xl">
                Premium
              </span>
              <img 
                src={ImageMap[product.image] || product.image} 
                alt={product.name} 
                className="max-h-full max-w-full object-contain drop-shadow-md group-hover:scale-105 transition-transform duration-300" 
              />
            </div>
            
            {/* Cam kết chuẩn Shopee Mall */}
            <div className="grid grid-cols-3 gap-2 text-center text-[11px] text-zinc-500 font-semibold pt-3 border-t border-zinc-100">
              <div className="flex items-center justify-center gap-1"><BsShieldCheck className="text-emerald-600 text-sm"/> 100% Chính hãng</div>
              <div className="flex items-center justify-center gap-1"><IoBagCheck className="text-emerald-600 text-sm"/> Trả hàng tự do</div>
              <div className="flex items-center justify-center gap-1"><MdOutlineLocalShipping className="text-emerald-600 text-sm"/> Freeship tối đa</div>
            </div>
          </div>

          {/* Right Column: Product Core Info */}
          <div className="w-full md:w-[55%] flex flex-col justify-between">
            <div>
              {/* Product Title */}
              <h1 className="text-2xl font-bold text-zinc-900 leading-tight mb-2 tracking-tight">
                {product.name}
              </h1>
              
              {/* Short Description */}
              <p className="text-sm text-zinc-400 mb-4 font-light leading-relaxed">
                {product.desc}
              </p>

              {/* Price Block */}
              <div className="bg-zinc-50 rounded-xl p-4 mb-6 flex items-center gap-4 border border-zinc-100/80">
                <span className="text-3xl font-bold text-stone-900 tracking-tight">
                  {product.price.toLocaleString('vi-VN')}₫
                </span>
                <span className="bg-amber-500/10 text-amber-800 text-[11px] font-bold px-2.5 py-0.5 rounded border border-amber-500/15">
                  Giá độc quyền tại hệ thống
                </span>
              </div>

              {/* Shopee Voucher Row */}
              {publicVouchers.length > 0 && (
                <div className="flex items-start gap-4 text-sm mb-6 border-b border-zinc-100 pb-5">
                  <div className="text-zinc-400 w-24 shrink-0 pt-0.5 font-bold text-xs uppercase tracking-wider">Mã giảm giá</div>
                  <div className="flex flex-wrap gap-2">
                    {publicVouchers.map((voucher) => (
                      <div 
                        key={voucher.id} 
                        className="inline-flex items-center gap-1 text-xs bg-amber-500/5 border border-dashed border-amber-500/60 text-amber-800 px-3 py-1 rounded-md font-semibold shrink-0"
                      >
                        <BsFillTicketPerforatedFill size={13}/>
                        {voucher.discountType === 'percent' && `Giảm ${voucher.discountPreview}%`}
                        {voucher.discountType === 'fixed' && `${voucher.title}`}
                        {voucher.discountType === 'shipping' && `Freeship`}
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Attributes Selection Lines */}
              <div className="space-y-5 mb-6">
                
                {/* 1. Hương vị */}
                <div className="flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-4">
                  <div className="text-zinc-400 text-xs w-24 shrink-0 font-bold uppercase tracking-wider">Hương vị</div>
                  <div className="flex flex-wrap gap-2">
                    {flavorOptions.map((flavor, index) => (
                      <button
                        key={index}
                        onClick={() => setFlavorNotes(flavor)}
                        className={`text-xs px-4 py-2 rounded-lg border font-semibold transition-all ${
                          flavorNotes === flavor
                            ? 'border-accent-1 bg-accent-1/90 text-white shadow-sm'
                            : 'border-zinc-200 bg-white text-zinc-700 hover:border-zinc-400'
                        }`}
                      >
                        {flavor}
                      </button>
                    ))}
                  </div>
                </div>

                {/* 2. Khối lượng */}
                <div className="flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-4">
                  <div className="text-zinc-400 text-xs w-24 shrink-0 font-bold uppercase tracking-wider">Khối lượng</div>
                  <div className="flex flex-wrap gap-2">
                    {weightOptions.map((weight, index) => (
                      <button
                        key={index}
                        onClick={() => setWeight(weight)}
                        className={`text-xs px-4 py-2 rounded-lg border font-semibold transition-all ${
                          weights === weight
                            ? 'border-accent-1 bg-accent-1/90 text-white shadow-sm'
                            : 'border-zinc-200 bg-white text-zinc-700 hover:border-zinc-400'
                        }`}
                      >
                        {weight}
                      </button>
                    ))}
                  </div>
                </div>

                {/* 3. Kiểu xay */}
                <div className="flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-4">
                  <div className="text-zinc-400 text-xs w-24 shrink-0 font-bold uppercase tracking-wider">Kiểu xay</div>
                  <div className="flex flex-wrap gap-2">
                    {grindOptions.map((grind) => (
                      <button
                        key={grind.Id}
                        onClick={() => setGrindType(grind)}
                        className={`text-xs px-4 py-2 rounded-lg border font-semibold transition-all ${
                          grindType?.Id === grind.Id
                            ? 'border-accent-1 bg-accent-1/90 text-white shadow-sm'
                            : 'border-zinc-200 bg-white text-zinc-700 hover:border-zinc-400'
                        }`}
                      >
                        {grind.Name}
                      </button>
                    ))}
                  </div>
                </div>

                {/* 4. Số lượng đặt hàng */}
                <div className="flex items-center gap-4 pt-2">
                  <div className="text-zinc-400 text-xs w-24 shrink-0 font-bold uppercase tracking-wider">Số lượng</div>
                  <div className="flex items-center border border-zinc-200 rounded-lg bg-white overflow-hidden h-9 shadow-sm">
                    <button
                      onClick={() => setQuantity(Math.max(1, quantity - 1))}
                      className="w-9 h-full flex items-center justify-center text-zinc-500 hover:bg-zinc-50 font-bold border-r border-zinc-200 text-sm transition-colors"
                    >-</button>
                    <span className="text-sm font-bold w-12 text-center text-zinc-800">{quantity}</span>
                    <button
                      onClick={() => setQuantity(quantity + 1)}
                      className="w-9 h-full flex items-center justify-center text-zinc-500 hover:bg-zinc-50 font-bold border-l border-zinc-200 text-sm transition-colors"
                    >+</button>
                  </div>
                </div>

              </div>
            </div>

            {/* Action Buttons Area */}
            <div className="pt-6 border-t border-zinc-100">
              <div className="flex flex-col sm:flex-row gap-4">
                {/* Nút thêm vào giỏ hàng kiểu Shopee Mall */}
                <button
                  onClick={handleAddToCart}
                  className="flex-1 border border-accent-1 bg-stone-50 text-accent-1 font-bold text-sm h-12 px-6 rounded-xl hover:bg-accent-1/30 flex items-center justify-center gap-2 shadow-sm transition-all active:scale-[0.98]"
                >
                  <IoCartOutline size={18} strokeWidth={2.5}/> Thêm Vào Giỏ Hàng
                </button>
                
                {/* Mua Ngay */}
                <button
                  onClick={() => {
                    handleAddToCart();
                    navigate('/cart');
                  }}
                  className="flex-1 bg-accent-1 text-white font-bold text-sm h-12 px-6 rounded-xl hover:bg-amber-800 flex items-center justify-center shadow-md transition-all active:scale-[0.98]"
                >
                  Mua Ngay
                </button>
              </div>

              {/* Gói định kỳ tiết kiệm */}
              <div className="mt-4 flex flex-col gap-1">
                <button 
                  onClick={() => navigate('/subscription')} 
                  className="text-amber-800 text-xs font-bold hover:underline self-start flex items-center gap-1"
                >
                  💡 Tiết kiệm chi phí: Đăng ký nhận gói giao định kỳ (Ưu đãi giảm ngay 15% tổng hóa đơn)
                </button>
              </div>
            </div>

          </div>
        </div>

        {/* Technical Specification Table Bottom (Card thông số nằm dưới tách biệt) */}
        <div className="bg-white rounded-2xl shadow-[0_4px_20px_rgba(0,0,0,0.03)] p-6 md:p-8 mt-6">
          <h2 className="text-sm font-bold text-zinc-900 uppercase tracking-wider border-b border-zinc-100 pb-3 mb-4">
            Thông số kỹ thuật chi tiết
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-y-3.5 gap-x-12 text-sm max-w-4xl">
            <div className="flex border-b border-zinc-50 pb-2"><span className="text-zinc-400 w-36 font-semibold shrink-0">Giống cà phê</span><span className="font-bold text-zinc-800">{CategoryMap[product.type] || product.type}</span></div>
            <div className="flex border-b border-zinc-50 pb-2"><span className="text-zinc-400 w-36 font-semibold shrink-0">Vùng đất trồng</span><span className="font-bold text-zinc-800">{product.region}</span></div>
            <div className="flex border-b border-zinc-50 pb-2"><span className="text-zinc-400 w-36 font-semibold shrink-0">Phương pháp sơ chế</span><span className="font-bold text-zinc-800">{product.process}</span></div>
            <div className="flex border-b border-zinc-50 pb-2"><span className="text-zinc-400 w-36 font-semibold shrink-0">Mức độ rang</span><span className="font-bold text-zinc-800">{product.roast}</span></div>
            <div className="flex border-b border-zinc-50 pb-2"><span className="text-zinc-400 w-36 font-semibold shrink-0">Độ cao nông trại</span><span className="font-bold text-zinc-800">{product.height}</span></div>
            <div className="flex border-b border-zinc-50 pb-2"><span className="text-zinc-400 w-36 font-semibold shrink-0">Trọng lượng đóng gói</span><span className="font-bold text-zinc-800">{product.weight}</span></div>
            <div className="flex border-b border-zinc-50 pb-2 md:col-span-2"><span className="text-zinc-400 w-36 font-semibold shrink-0">Hương vị ghi chú</span><span className="font-bold text-stone-800">{product.flavorNotes}</span></div>
          </div>
        </div>

      </div>
    </div>
  );
}