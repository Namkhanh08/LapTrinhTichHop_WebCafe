import React, { useState, useEffect } from 'react';
import {
  Settings,
  Truck,
  RotateCcw,
  X,
  ShieldCheck,
  AlertTriangle,
  Layers,
  Crown,
  Coffee,
  Sparkles,
  ArrowRight,
  CreditCard,
  History
} from 'lucide-react';

import useStore from '../store/useStore';

const AVAILABLE_FLAVORS = [
  'Chocolate Đậm',
  'Arabica Ethiopia',
  'Caramel Hazelnut',
  'Moka Truyền Thống',
  'Cold Brew Blend',
  'Robusta Premium'
];

const AVAILABLE_GRINDS = [
  'Pha máy Espresso',
  'Pha phin Việt Nam',
  'Cold Brew',
  'Pour Over V60',
  'Nguyên hạt',
  'French Press'
];

export default function MultiSubscriptionPortal() {

  // =========================================================
  // STORE
  // =========================================================
  const {
    subscriptions = [],
    fetchSubscriptions,
    toggleSkipSubscription,
    cancelSubscription,
    updateSubscriptionConfig
  } = useStore();
  const user = useStore((state) => state.user);

  // =========================================================
  // STATES
  // =========================================================
  const [activeSubId, setActiveSubId] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalData, setModalData] = useState(null);
  const [toastMessage, setToastMessage] = useState('');

  // =========================================================
  // FETCH DATA
  // =========================================================
  useEffect(() => {
    fetchSubscriptions();
  }, []);

  // =========================================================
  // AUTO SELECT FIRST SUB
  // =========================================================
  useEffect(() => {
    if (subscriptions.length > 0 && !activeSubId) {
      setActiveSubId(subscriptions[0].id);
    }
  }, [subscriptions, activeSubId]);

  // =========================================================
  // CURRENT SUB
  // =========================================================
  const currentSub = subscriptions.find(
    (sub) => sub.id === activeSubId
  );

  if (!currentSub) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#FAF9F5] font-nunito">
        <div className="bg-white border border-[#EAE6DF] rounded-[32px] p-12 max-w-sm text-center shadow-[0_16px_40px_rgba(0,0,0,0.02)]">
          <div className="w-12 h-12 bg-[#F4EFE6] text-[#8C7650] rounded-full flex items-center justify-center mx-auto mb-4 animate-spin">
            <Coffee size={20} strokeWidth={1.5} />
          </div>
          <h2 className="text-xl font-extrabold text-[#1F1E1C] mb-2 tracking-tight">
            Khởi tạo không gian...
          </h2>
          <p className="text-xs text-[#8A857C] font-light">
            Đang chuẩn bị trải nghiệm đặc quyền cao cấp của bạn.
          </p>
        </div>
      </div>
    );
  }

  // =========================================================
  // TOAST
  // =========================================================
  const triggerToast = (msg) => {
    setToastMessage(msg);
    setTimeout(() => {
      setToastMessage('');
    }, 3000);
  };

  // =========================================================
  // TOGGLE SKIP
  // =========================================================
  const handleToggleSkip = async (id) => {
    try {
      await toggleSkipSubscription(id);
      triggerToast('Cập nhật trạng thái thành công');
    } catch (error) {
      console.error(error);
      triggerToast('Có lỗi xảy ra trong quá trình xử lý');
    }
  };

  // =========================================================
  // CANCEL SUB
  // =========================================================
  const handleCancelSub = async (id) => {
    const confirmCancel = window.confirm(
      'Hành động này sẽ chấm dứt đặc quyền gói định kỳ. Bạn có chắc chắn muốn hủy không?'
    );
    if (!confirmCancel) return;

    try {
      await cancelSubscription(id);
      triggerToast('Đã dừng gói dịch vụ thành công');
    } catch (error) {
      console.error(error);
      triggerToast('Hủy gói thất bại, vui lòng liên hệ bộ phận CSKH');
    }
  };

  // =========================================================
  // OPEN CONFIG MODAL
  // =========================================================
  const openConfigModal = (sub) => {
    setModalData({ ...sub });
    setIsModalOpen(true);
  };

  // =========================================================
  // SAVE CONFIG
  // =========================================================
  const handleSaveConfig = async () => {
    try {
      await updateSubscriptionConfig(
        modalData.id,
        {
          flavor: modalData.flavor,
          grindType: modalData.grindType
        }
      );
      setIsModalOpen(false);
      triggerToast('Tuyệt tác nghệ thuật vị giác đã được cập nhật');
    } catch (error) {
      console.error(error);
      triggerToast('Cập nhật cấu hình thất bại');
    }
  };

  // =========================================================
  // LOADING / EMPTY
  // =========================================================
  if (!subscriptions || subscriptions.length === 0) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#FAF9F5] font-nunito">
        <div className="bg-white border border-[#EAE6DF] rounded-[32px] p-12 text-center shadow-[0_16px_40px_rgba(0,0,0,0.03)] max-w-md">
          <Crown className="text-[#C5A880] w-10 h-10 mx-auto mb-4 stroke-[1.2]" />
          <h2 className="text-2xl text-[#1F1E1C] font-extrabold tracking-tight mb-2">
            Chưa tìm thấy thẻ Đặc Quyền
          </h2>
          <p className="text-sm text-[#8A857C] font-light max-w-xs mx-auto">
            Hiện tại quý khách chưa sở hữu gói đăng ký hạt thượng hạng nào từ REVO.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#FAF9F5] text-[#1F1E1C] antialiased pb-24 relative select-none selection:bg-[#C5A880]/20 font-nunito">
      
      {/* INJECT KEYFRAMES ANIMATIONS FOR LUXURY EFFECTS */}
      <style>{`
        @keyframes fadeIn {
          from { opacity: 0; transform: translate(-50%, -10px); }
          to { opacity: 1; transform: translate(-50%, 0); }
        }
        @keyframes scaleIn {
          from { opacity: 0; transform: scale(0.97); }
          to { opacity: 1; transform: scale(1); }
        }
        .animate-fade-in { animation: fadeIn 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
        .animate-scale-in { animation: scaleIn 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
      `}</style>

      {/* BACKGROUND DECORATIONS (LUXURY GLOW) */}
      <div className="absolute top-0 right-0 w-[500px] h-[500px] bg-[#C5A880]/5 rounded-full blur-[120px] pointer-events-none" />
      <div className="absolute bottom-12 left-0 w-[400px] h-[400px] bg-[#8C7650]/4 rounded-full blur-[100px] pointer-events-none" />

      {/* ========================================================= */}
      {/* TOAST NOTIFICATION */}
      {/* ========================================================= */}
      {toastMessage && (
        <div className="fixed top-8 left-1/2 -translate-x-1/2 bg-[#1A1917] text-[#EAE6DF] px-6 py-4 rounded-2xl shadow-[0_24px_50px_rgba(0,0,0,0.15)] z-50 flex items-center gap-3.5 text-xs tracking-wider border border-white/5 animate-fade-in backdrop-blur-xl">
          <div className="w-5 h-5 rounded-full bg-[#C5A880]/20 flex items-center justify-center text-[#C5A880]">
            <ShieldCheck size={14} />
          </div>
          <span className="font-bold tracking-wide uppercase">{toastMessage}</span>
        </div>
      )}

      {/* ========================================================= */}
      {/* ELITE HEADER */}
      {/* ========================================================= */}
      <header className="sticky top-0 bg-white/70 backdrop-blur-md border-b border-[#EAE6DF]/60 py-5 px-8 z-40 transition-all shadow-[0_2px_20px_rgba(0,0,0,0.01)]">
        <div className="max-w-7xl mx-auto flex justify-between items-center">
          
          <div className="flex items-center gap-2.5 group">
            <div className="w-8 h-8 bg-[#1F1E1C] rounded-xl flex items-center justify-center text-[#C5A880] font-extrabold tracking-tighter text-xs shadow-inner">
              R
            </div>
            <div className="flex flex-col text-left">
              <span className="font-extrabold text-sm text-[#1F1E1C] tracking-[0.2em] leading-tight">
                REVO
              </span>
              <span className="text-[9px] font-black text-[#C5A880] tracking-widest uppercase mt-0.5">
                THƯƠNG HIỆU COFFE DÀNH RIÊNG CHO BẠN
              </span>
            </div>
          </div>

          <div className="text-xs flex items-center gap-4">
            <div className="h-4 w-[1px] bg-[#EAE6DF]" />
            <div className="flex items-center gap-2.5 bg-[#1F1E1C] text-white px-4 py-2 rounded-full shadow-sm">
              <Crown size={12} className="text-[#C5A880]" fill="#C5A880" />
              <span className="font-bold text-[11px] tracking-wide text-[#EAE6DF]">
                {user.name}
              </span>
            </div>
          </div>

        </div>
      </header>

      {/* ========================================================= */}
      {/* MAIN PORTAL SPACE */}
      {/* ========================================================= */}
      <main className="max-w-7xl mx-auto px-6 mt-12 relative z-10">
        
        {/* HERO TITLE BLOCK */}
        <div className="mb-12 flex flex-col md:flex-row md:items-end justify-between gap-4 border-b border-[#EAE6DF]/60 pb-8">
          <div className="text-left">
            <div className="flex items-center gap-2 text-[#C5A880] text-xs font-black tracking-widest uppercase mb-2">
              <Sparkles size={14} /> Danh mục
            </div>
            <h1 className="text-3xl md:text-4xl font-black text-[#1F1E1C] tracking-tight">
              Quản Lý Thẻ Đăng Ký Định Kỳ
            </h1>
          </div>
          <div className="bg-[#F4EFE6] border border-[#E6DEC2]/40 rounded-2xl px-5 py-3.5 text-left md:text-right shrink-0">
            <p className="text-[11px] font-black uppercase text-[#8A857C] tracking-widest">Đặc quyền sở hữu</p>
            <p className="text-xl font-extrabold text-[#4E412C] mt-0.5">
              {subscriptions.length} Tuyệt tác gói hương vị
            </p>
          </div>
        </div>

        {/* WORKSPACE LAYOUT */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
          
          {/* ========================================================= */}
          {/* LEFT COLUMN: CURATED SIDEBAR LIST */}
          {/* ========================================================= */}
          <div className="lg:col-span-4 space-y-4">
            <div className="text-[10px] font-black text-[#8A857C] uppercase tracking-[0.2em] px-2 flex items-center gap-2">
              <Layers size={12} className="text-[#C5A880]" />
              Danh mục gói dịch vụ
            </div>

            <div className="space-y-3">
              {subscriptions.map((sub) => {
                const isSelected = sub.id === activeSubId;
                return (
                  <button
                    key={sub.id}
                    onClick={() => setActiveSubId(sub.id)}
                    className={`
                      w-full text-left p-5 rounded-2xl border transition-all duration-300 relative flex flex-col gap-3 group/card
                      ${isSelected
                        ? 'bg-[#1F1E1C] border-[#1F1E1C] shadow-[0_20px_35px_rgba(31,30,28,0.12)] translate-y-[-2px]'
                        : 'bg-white hover:bg-[#FDFDFD] border-[#EAE6DF] shadow-[0_4px_20px_rgba(0,0,0,0.01)] hover:border-[#C5A880]/40'
                      }
                    `}
                  >
                    <div className="flex justify-between items-center w-full">
                      <span className={`text-[10px] font-mono tracking-wider px-2 py-0.5 rounded-md ${
                        isSelected ? 'bg-white/10 text-[#C5A880]' : 'bg-[#FAF9F5] text-[#8A857C]'
                      }`}>
                        ID: {sub.id}
                      </span>

                      {sub.status === 'ACTIVE' && (
                        <span className={`text-[10px] uppercase font-black tracking-widest flex items-center gap-1.5 ${
                          isSelected ? 'text-emerald-400' : 'text-emerald-600'
                        }`}>
                          <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" /> Active
                        </span>
                      )}
                      {sub.status === 'SKIPPED' && (
                        <span className="text-[10px] uppercase font-black tracking-widest text-[#C5A880] flex items-center gap-1.5">
                          <span className="w-1.5 h-1.5 rounded-full bg-[#C5A880]" /> Skipped
                        </span>
                      )}
                      {sub.status === 'CANCELLED' && (
                        <span className="text-[10px] uppercase font-black tracking-widest text-rose-500 flex items-center gap-1.5">
                          ✕ Cancelled
                        </span>
                      )}
                    </div>

                    <h4 className={`
                      text-base font-extrabold truncate pr-6 transition-colors
                      ${isSelected ? 'text-white' : 'text-[#1F1E1C] group-hover/card:text-[#8C7650]'}
                    `}>
                      {sub.productName}
                    </h4>

                    <div className={`
                      flex justify-between items-center text-xs mt-1 border-t pt-3
                      ${isSelected ? 'border-white/10 text-white/50' : 'border-[#FAF9F5] text-[#8A857C]'}
                    `}>
                      <span className="font-medium tracking-wide">{sub.frequency}</span>
                      <span className={`font-black text-sm ${isSelected ? 'text-[#C5A880]' : 'text-[#1F1E1C]'}`}>
                        {(sub.price).toLocaleString('vi-VN')}đ
                      </span>
                    </div>

                    {/* Interactive Arrow Indicator */}
                    <div className={`absolute right-4 top-1/2 -translate-y-1/2 transition-all opacity-0 group-hover/card:opacity-100 ${
                      isSelected ? 'text-[#C5A880] translate-x-0' : 'text-[#8A857C] -translate-x-1'
                    }`}>
                      <ArrowRight size={14} />
                    </div>
                  </button>
                );
              })}
            </div>
          </div>

          {/* ========================================================= */}
          {/* RIGHT COLUMN: LUXURIOUS DETAILS AREA */}
          {/* ========================================================= */}
          <div className="lg:col-span-8 space-y-6 text-left">
            
            {/* CARD 1: OVERVIEW HERO */}
            <div className="bg-white rounded-[32px] border border-[#EAE6DF] shadow-[0_12px_40px_rgba(0,0,0,0.015)] p-8 relative overflow-hidden transition-all">
              
              {/* Premium top border detail */}
              <div className="absolute top-0 left-0 w-full h-[3px] bg-gradient-to-r from-[#8C7650] via-[#C5A880] to-[#EAE6DF]" />

              <div className="flex flex-col sm:flex-row sm:justify-between sm:items-start gap-4 mb-8">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-[10px] text-[#8A857C] font-mono tracking-wider bg-[#FAF9F5] border border-[#EAE6DF] px-2.5 py-1 rounded-md">
                      #{currentSub.id}
                    </span>
                    <span className="text-xs text-[#8A857C] font-bold tracking-wide">
                      Chu kỳ: {currentSub.frequency}
                    </span>
                  </div>

                  <h3 className="text-2xl font-black text-[#1F1E1C] mt-3 tracking-tight">
                    {currentSub.productName}
                  </h3>
                </div>

                <div>
                  {currentSub.status === 'ACTIVE' && (
                    <span className="bg-emerald-50/60 text-emerald-700 border border-emerald-100/60 px-4 py-2 rounded-xl text-xs font-black flex items-center gap-2">
                      <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
                      Hệ thống hoạt động
                    </span>
                  )}
                  {currentSub.status === 'SKIPPED' && (
                    <span className="bg-[#FAF6EE] text-[#A68953] border border-[#EFE8DC] px-4 py-2 rounded-xl text-xs font-black flex items-center gap-2">
                      ⚠️ Đang tạm hoãn kỳ tới
                    </span>
                  )}
                  {currentSub.status === 'CANCELLED' && (
                    <span className="bg-rose-50 text-rose-700 border border-rose-100 px-4 py-2 rounded-xl text-xs font-black flex items-center gap-2">
                      ✕ Đã đóng hoàn toàn
                    </span>
                  )}
                </div>
              </div>

              {/* TASTE PROFILE MATRIX */}
              <div className="grid grid-cols-3 gap-4 bg-[#FAF9F5] p-5 rounded-2xl border border-[#EAE6DF]/60 mb-6 text-xs">
                <div className="space-y-1">
                  <span className="text-[10px] text-[#8A857C] uppercase font-black tracking-widest block">
                    Cấu hình hương vị
                  </span>
                  <span className="font-black text-[#1F1E1C] block text-sm">
                    {currentSub.flavor}
                  </span>
                </div>

                <div className="space-y-1 border-l border-[#EAE6DF] pl-4">
                  <span className="text-[10px] text-[#8A857C] uppercase font-black tracking-widest block">
                    Kỹ thuật xay
                  </span>
                  <span className="font-black text-[#1F1E1C] block text-sm">
                    {currentSub.grindType}
                  </span>
                </div>

                <div className="space-y-1 border-l border-[#EAE6DF] pl-4">
                  <span className="text-[10px] text-[#8A857C] uppercase font-black tracking-widest block">
                    Khối lượng tịnh
                  </span>
                  <span className="font-black text-[#8C7650] block text-sm">
                    {currentSub.weight} <span className="text-xs font-normal text-[#8A857C]">x{currentSub.quantity}</span>
                  </span>
                </div>
              </div>

              {/* TAILOR ACTION BUTTON */}
              {currentSub.status !== 'CANCELLED' && (
                <button
                  onClick={() => openConfigModal(currentSub)}
                  className="w-full bg-white hover:bg-[#1F1E1C] border border-[#1F1E1C] text-[#1F1E1C] hover:text-white text-xs font-black py-3.5 px-4 rounded-xl flex items-center justify-center gap-2 transition-all duration-300 tracking-wider uppercase shadow-sm"
                >
                  <Settings size={14} strokeWidth={2.5} />
                  Tinh chỉnh mẻ rang nghệ thuật
                </button>
              )}
            </div>

            {/* TWIN GRID SECTION */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              
              {/* CARD 2: LOGISTICS FLOW */}
              <div className="bg-white rounded-[28px] border border-[#EAE6DF] shadow-[0_8px_30px_rgba(0,0,0,0.01)] p-6 flex flex-col justify-between min-h-[190px]">
                <h4 className="font-black text-xs text-[#8A857C] mb-4 uppercase tracking-widest flex items-center gap-2">
                  <Truck size={14} className="text-[#C5A880]" />
                  Hành trình vận hành nghệ nhân
                </h4>

                {currentSub.status === 'CANCELLED' ? (
                  <p className="text-xs text-[#8A857C] font-light py-8 text-center italic">
                    Gói dịch vụ cao cấp đã khép lại.
                  </p>
                ) : currentSub.status === 'SKIPPED' ? (
                  <div className="bg-[#FAF6EE] border border-[#EFE8DC] rounded-xl p-4 text-xs text-[#8C7650] flex gap-2.5 items-start">
                    <AlertTriangle size={15} className="shrink-0 mt-0.5 text-[#C5A880]" />
                    <span className="font-medium leading-relaxed">
                      Lịch trình giao hàng tiếp theo đang được tạm ngưng theo nguyện vọng của quý khách.
                    </span>
                  </div>
                ) : (
                  <div className="relative flex items-center justify-between px-3 py-4 mt-2">
                    <div className="absolute left-0 right-0 top-[29px] h-[1px] bg-[#EAE6DF] z-0" />
                    <div className="absolute left-0 right-1/2 top-[29px] h-[1.5px] bg-[#1F1E1C] z-0" />

                    <div className="z-10 flex flex-col items-center">
                      <div className="w-6 h-6 rounded-full bg-[#1F1E1C] text-[#C5A880] flex items-center justify-center text-[10px] font-black shadow-md">
                        ✓
                      </div>
                      <span className="text-[10px] font-black text-[#1F1E1C] mt-2 tracking-wide">
                        Ký kết
                      </span>
                    </div>

                    <div className="z-10 flex flex-col items-center">
                      <div className="w-6 h-6 rounded-full bg-[#1F1E1C] text-white flex items-center justify-center text-[10px] font-black shadow-lg ring-4 ring-[#C5A880]/20 animate-pulse">
                        2
                      </div>
                      <span className="text-[10px] font-black text-[#8C7650] mt-2 tracking-wide">
                        Rang mẻ cổ điển
                      </span>
                    </div>

                    <div className="z-10 flex flex-col items-center">
                      <div className="w-6 h-6 rounded-full bg-[#FAF9F5] border border-[#EAE6DF] text-[#8A857C] flex items-center justify-center text-[10px] font-bold">
                        3
                      </div>
                      <span className="text-[10px] font-bold text-[#8A857C] mt-2 tracking-wide">
                        Giao hỏa tốc
                      </span>
                    </div>
                  </div>
                )}
              </div>

              {/* CARD 3: SUBSCRIPTION FINANCE */}
              <div className="bg-white rounded-[28px] border border-[#EAE6DF] shadow-[0_8px_30px_rgba(0,0,0,0.01)] p-6 space-y-4">
                <h4 className="font-black text-xs uppercase tracking-widest text-[#8A857C] flex items-center gap-2">
                  <CreditCard size={14} className="text-[#C5A880]" />
                  Cấu trúc thanh toán
                </h4>

                <div className="space-y-3 text-xs">
                  <div className="flex justify-between items-center">
                    <span className="text-[#8A857C] font-medium">Thẻ đặc quyền:</span>
                    <span className="font-mono bg-[#1F1E1C] text-[#C5A880] px-3 py-1 rounded-md text-[10px] font-black tracking-widest">
                      {currentSub.status === 'CANCELLED' ? 'N/A' : currentSub.cardInfo}
                    </span>
                  </div>

                  <div className="flex justify-between items-center border-t border-[#FAF9F5] pt-3">
                    <span className="text-[#8A857C] font-medium">Ngày gia hạn kế tiếp:</span>
                    <span className="font-black text-[#1F1E1C]">
                      {currentSub.status === 'ACTIVE'
                        ? currentSub.nextBilling
                        : currentSub.status === 'SKIPPED'
                          ? 'Đang hoãn định kỳ'
                          : 'Đã hoàn tất chu kỳ'
                      }
                    </span>
                  </div>

                  <div className="border-t border-[#EAE6DF]/60 pt-3 flex justify-between items-end">
                    <span className="text-[10px] font-black text-[#8A857C] uppercase tracking-wider">
                      Chi phí định kỳ:
                    </span>
                    <span className="font-black text-xl text-[#8C7650]">
                      {(currentSub.price).toLocaleString('vi-VN')}₫
                    </span>
                  </div>
                </div>
              </div>

            </div>

            {/* HISTORY & CRISIS ACTIONS GRID */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              
              {/* CARD 4: ACTIVITY LOG */}
              <div className="bg-white rounded-[28px] border border-[#EAE6DF] shadow-[0_8px_30px_rgba(0,0,0,0.01)] p-6">
                <h4 className="font-black text-xs text-[#8A857C] mb-3 uppercase tracking-widest flex items-center gap-2">
                  <History size={14} className="text-[#C5A880]" />
                  Biên niên sử hoạt động
                </h4>

                <div className="text-xs bg-[#FAF9F5] p-4 rounded-xl border border-[#EAE6DF]/40">
                  <p className="font-black text-[#1F1E1C] leading-relaxed">
                    {currentSub.history}
                  </p>
                  <p className="text-[#8A857C] text-[10px] mt-2 font-bold tracking-wide">
                    Tiêu chuẩn: {currentSub.weight} • mẻ rang hương vị {currentSub.flavor}
                  </p>
                </div>
              </div>

              {/* CARD 5: ELITE STRATEGIC ACTIONS */}
              <div className="bg-white rounded-[28px] border border-[#EAE6DF] shadow-[0_8px_30px_rgba(0,0,0,0.01)] p-5 flex flex-col justify-center gap-3">
                {currentSub.status !== 'CANCELLED' ? (
                  <>
                    <button
                      onClick={() => handleToggleSkip(currentSub.id)}
                      className={`
                        w-full py-3 px-4 rounded-xl text-xs font-black transition-all duration-300 flex items-center justify-center gap-2 border uppercase tracking-wider
                        ${currentSub.status === 'SKIPPED'
                          ? 'bg-[#8C7650] text-white border-[#8C7650] hover:bg-[#736140] shadow-md'
                          : 'bg-[#FAF6EE] hover:bg-[#EFE8DC] border-[#E6DEC2] text-[#4E412C]'
                        }
                      `}
                    >
                      <RotateCcw size={13} strokeWidth={2.5} />
                      {currentSub.status === 'SKIPPED' ? 'Tái kích hoạt đặc quyền' : 'Tạm hoãn kỳ giao tới'}
                    </button>

                    <button
                      onClick={() => handleCancelSub(currentSub.id)}
                      className="w-full bg-white hover:bg-rose-50/50 text-rose-600 border border-transparent hover:border-rose-100 py-3 px-4 rounded-xl text-xs font-black transition-all duration-200 uppercase tracking-widest"
                    >
                      Hủy gói dịch vụ Private
                    </button>
                  </>
                ) : (
                  <div className="text-center py-6">
                    <p className="text-xs text-[#8A857C] font-light italic">
                      Tài khoản Premium này đã đóng hoàn toàn.
                    </p>
                  </div>
                )}
              </div>

            </div>

          </div>
        </div>
      </main>

      {/* ========================================================= */}
      {/* SELECTION MODAL: ULTRA PREMIUM BRANDING */}
      {/* ========================================================= */}
      {isModalOpen && modalData && (
        <div className="fixed inset-0 bg-[#1F1E1C]/60 backdrop-blur-md z-50 flex items-center justify-center p-4">
          <div className="bg-white w-full max-w-lg rounded-[32px] p-8 shadow-[0_30px_70px_rgba(0,0,0,0.25)] border border-[#EAE6DF] relative font-nunito animate-scale-in">
            
            {/* CLOSE ACTION */}
            <button
              onClick={() => setIsModalOpen(false)}
              className="absolute top-6 right-6 text-[#8A857C] hover:text-[#1F1E1C] p-2 rounded-full bg-[#FAF9F5] transition-colors"
            >
              <X size={16} />
            </button>

            {/* MODAL HEADER */}
            <div className="mb-6 text-left pb-4 border-b border-[#EAE6DF]/60">
              <span className="text-[9px] font-black text-[#C5A880] uppercase tracking-[0.2em]">Cân chỉnh hương vị đặc quyền</span>
              <h3 className="text-xl font-black text-[#1F1E1C] mt-1">Cấu Hình Công Thức Mẻ Rang</h3>
              <p className="text-[11px] font-mono text-[#8A857C] mt-0.5">Mã số thẻ: {modalData.id}</p>
            </div>

            {/* SELECTION 1: FLAVOR PROFILE */}
            <div className="mb-6 text-left">
              <label className="block text-[10px] font-black text-[#8A857C] uppercase tracking-[0.15em] mb-3">
                1. Chọn biên độ hương vị hạt
              </label>

              <div className="grid grid-cols-2 gap-2 text-xs">
                {AVAILABLE_FLAVORS.map((f) => {
                  const isMatch = modalData.flavor === f;
                  return (
                    <button
                      key={f}
                      onClick={() => setModalData((prev) => ({ ...prev, flavor: f }))}
                      className={`
                        p-3.5 rounded-xl text-left border font-bold transition-all duration-200 truncate
                        ${isMatch
                          ? 'border-[#1F1E1C] bg-[#1F1E1C] text-white shadow-md'
                          : 'border-[#EAE6DF] bg-white text-[#4E4D4A] hover:border-[#C5A880]'
                        }
                      `}
                    >
                      {f}
                    </button>
                  );
                })}
              </div>
            </div>

            {/* SELECTION 2: GRIND OPTIONS */}
            <div className="mb-8 text-left">
              <label className="block text-[10px] font-black text-[#8A857C] uppercase tracking-[0.15em] mb-3">
                2. Chọn cấu trúc độ mịn máy xay
              </label>

              <div className="grid grid-cols-2 gap-2 text-xs">
                {AVAILABLE_GRINDS.map((g) => {
                  const isMatch = modalData.grindType === g;
                  return (
                    <button
                      key={g}
                      onClick={() => setModalData((prev) => ({ ...prev, grindType: g }))}
                      className={`
                        p-3.5 rounded-xl text-left border font-bold transition-all duration-200 truncate
                        ${isMatch
                          ? 'border-[#1F1E1C] bg-[#1F1E1C] text-white shadow-md'
                          : 'border-[#EAE6DF] bg-white text-[#4E4D4A] hover:border-[#C5A880]'
                        }
                      `}
                    >
                      {g}
                    </button>
                  );
                })}
              </div>
            </div>

            {/* CONFIRM CONCIERGE SAVE BUTTON */}
            <button
              onClick={handleSaveConfig}
              className="w-full bg-[#1F1E1C] hover:bg-[#8C7650] text-white font-black py-4 px-4 rounded-xl text-xs uppercase tracking-[0.2em] transition-all duration-300 shadow-lg"
            >
              Lưu cấu trúc & thông báo hệ thống
            </button>

          </div>
        </div>
      )}
    </div>
  );
}