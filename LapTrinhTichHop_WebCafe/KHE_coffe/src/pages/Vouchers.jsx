import React, { useEffect, useState } from 'react';
import API from '../services/api';
import { BsFillTicketPerforatedFill } from "react-icons/bs";
import { MdOutlineLocalShipping } from "react-icons/md";
import { LuPercent } from "react-icons/lu";

export default function Vouchers() {
  const [vouchers, setVouchers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchVouchers = async () => {
      try {
        const res = await API.getVouchersAdmin();
        const items = Array.isArray(res.data)
          ? res.data
          : (res.data?.voucher || res.data?.items || []);
        setVouchers(items);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchVouchers();
  }, []);

  if (loading) return <div className="min-h-screen flex items-center justify-center font-nunito text-lg">Đang tải danh sách voucher...</div>;

  return (
    <div className="bg-white min-h-screen py-16">
      <div className="container mx-auto px-6 lg:px-12 max-w-7xl">
        <h1 className="font-montserrat font-black text-4xl text-primary mb-2 text-center uppercase">Kho Voucher của REVO</h1>
        <p className="font-nunito text-primary/70 mb-12 text-center">Thu thập mã giảm giá để nhận được ưu đãi tốt nhất cho ly cà phê của bạn.</p>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {vouchers.map((voucher) => (
            <div key={voucher.id} className="bg-pinky-gray rounded-3xl p-6 shadow-md hover:shadow-xl transition-all hover:-translate-y-1 relative overflow-hidden border-2 border-accent-1/20">
              <div className="absolute top-0 right-0 bg-accent-1 text-white px-4 py-1 rounded-bl-xl font-bold font-nunito text-sm">
                Mã: {voucher.code}
              </div>
              <h3 className="font-montserrat font-bold text-xl text-primary mb-2 mt-4">{voucher.title}</h3>
              <p className="font-nunito text-sm text-primary/70 mb-4">{voucher.description}</p>
              
              <div className="flex items-center gap-2 font-bold text-accent-1 bg-white p-3 rounded-xl mb-4 shadow-sm">
                {(voucher.discountType || voucher.DiscountType) === 'percentage' || (voucher.discountType || voucher.DiscountType) === 'percent' ? (
                  <><BsFillTicketPerforatedFill size={20}/> Giảm {voucher.discountValue || voucher.DiscountValue}%</>
                ) : (voucher.discountType || voucher.DiscountType) === 'fixed' ? (
                  <><BsFillTicketPerforatedFill size={20}/> Giảm {(voucher.discountValue || voucher.DiscountValue || 0).toLocaleString('vi-VN')} đ</>
                ) : (
                  <><MdOutlineLocalShipping size={20}/> Miễn phí vận chuyển</>
                )}
              </div>
              
              <p className="font-nunito text-xs text-primary/50">Áp dụng cho đơn từ: {(voucher.minOrderValue || voucher.MinOrderValue || 0).toLocaleString('vi-VN')}đ</p>
              <p className="font-nunito text-xs text-red-400 mt-1">Hạn sử dụng: {new Date(voucher.endDate || voucher.EndDate).toLocaleDateString('vi-VN')}</p>
            </div>
          ))}
          
          {vouchers.length === 0 && (
            <div className="col-span-full text-center text-gray-500 font-nunito">Hiện chưa có voucher nào. Vui lòng quay lại sau!</div>
          )}
        </div>
      </div>
    </div>
  );
}
