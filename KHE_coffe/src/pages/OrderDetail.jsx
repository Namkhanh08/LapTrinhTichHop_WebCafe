import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    ChevronLeft, MapPin, Phone, User, FileText,
    Check, Truck, PackageCheck, HelpCircle
} from 'lucide-react';
import useStore from '../store/useStore';
import { IoCheckmarkCircle } from "react-icons/io5";
import { GrRadialSelected } from "react-icons/gr";
import { FaShippingFast } from "react-icons/fa";
import { TbMoneybagMoveBack } from "react-icons/tb";

export default function OrderDetails() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { fetchOrderById, cancelOrder, fetchOrders } = useStore();
    const [order, setOrder] = useState(null);
    const [loading, setLoading] = useState(true);
    const [isTrackingOpen, setIsTrackingOpen] = useState(false); // Điều khiển đóng/mở hành trình chi tiết

    // Hàm chuyển đổi hình thức xay cà phê
    const translateGrind = (type) => {
        switch (type) {
            case 1: return "Nguyên Hạt";
            case 2: return "Pha Phin";
            case 3: return "Pha Máy";
            case 4: return "Ủ Lạnh";
            case 5: return "Kiểu Pháp";
            default: return type;
        }
    };

    useEffect(() => {
        const getDetail = async () => {
            setLoading(true);
            try {
                const data = await fetchOrderById(id);
                await fetchOrders(); // Cập nhật lại danh sách tổng quan trong store nếu cần
                setOrder(data);
            } catch (error) {
                console.error("Lỗi lấy dữ liệu chi tiết đơn hàng:", error);
            } finally {
                setLoading(false);
            }
        };
        getDetail();
    }, [id]);

    if (loading) return <div className="p-20 text-center font-bold">Đang tải dữ liệu đơn hàng...</div>;
    if (!order) return <div className="p-20 text-center font-bold text-red-500">Không tìm thấy đơn hàng #{id}</div>;

    // Xử lý tính toán ngày tháng
    const orderDate = new Date(order.OrderDate);
    const estimatedDate = new Date(orderDate);
    estimatedDate.setDate(orderDate.getDate() + 5); // Dự kiến giao hàng sau 5 ngày

    const formatDate = (date) => {
        return date.toLocaleDateString('vi-VN');
    };

    // 1. Các bước hiển thị trên thanh ngang (Tổng quan UI)
    const steps = [
        { id: 'Chờ xử lý', label: 'Đã đặt hàng', icon: <Check size={18} /> },
        { id: 'Đã xác nhận', label: 'Đã xác nhận', icon: <Check size={18} /> },
        { id: 'Đang giao', label: 'Đang giao', icon: <Truck size={18} /> },
        { id: 'Hoàn thành', label: 'Hoàn thành', icon: <PackageCheck size={18} /> },
    ];

    // 2. Các bước hiển thị trong Accordion dọc (Chi tiết Shipper)
    const steps_shipper = [
        { id: 'Chờ xử lý', label: 'Đã đặt hàng', icon: <Check size={18} /> },
        { id: 'Đã xác nhận', label: 'Đang chuẩn bị hàng', icon: <Check size={18} /> },
        { id: 'Đang trung chuyển', label: 'Đang trung chuyển', icon: <Truck size={18} /> },
        { id: 'Shipper đã nhận', label: 'Đã lấy đơn hàng', icon: <Check size={18} /> },
        { id: 'Đang giao', label: 'Đang trên đường giao', icon: <Truck size={18} /> },
        { id: 'Hoàn thành', label: 'Đã giao thành công', icon: <PackageCheck size={18} /> },
    ];

    // 3. Quy đổi trạng thái từ DB về trạng thái hiển thị của thanh ngang tổng quan
    const getMappedStatus = (status) => {
        if (status === 'Chờ thanh toán' || status === 'Đã thanh toán' ) return 'Chờ xử lý';
        if (['Shipper đã nhận', 'Đang trung chuyển', 'Đang giao'].includes(status)) {
            return 'Đang giao';
        }
        return status;
    };

    const mappedStatus = getMappedStatus(order.Status);

    // Xác định vị trí các index đang chạy để active CSS hiệu ứng
    const currentStepIndex = steps.findIndex(s => s.id === mappedStatus);
    const currentShipperStepIndex = steps_shipper.findIndex(s => {
        if (s.id === 'Chờ xử lý') return order.Status === 'Chờ xử lý' || order.Status === 'Chờ thanh toán' || order.Status === 'Đã thanh toán';
        return s.id === order.Status;
    });

    const isCancelled = order.Status === 'Đã hủy';

    const handleCancel = async (orderId) => {
        if (window.confirm("Bạn có chắc chắn muốn hủy đơn hàng này không?")) {
            try {
                await cancelOrder(orderId);
                setOrder(prev => ({ ...prev, Status: 'Đã hủy' }));
                alert("Hủy đơn hàng thành công!");
            } catch (error) {
                alert(error.response?.data || "Hủy đơn hàng thất bại, vui lòng thử lại!");
            }
        }
    };

    const handlePayment = (orderId, totalAmount) => {
        navigate(`/checkout/payment/${orderId}`, { state: { amount: totalAmount } });
    };

    const handleEdit = (orderId) => {
        navigate(`/orders/edit/${orderId}`);
    };

    const handleReorder = () => {
        // Thực hiện thêm lại các sản phẩm vào giỏ hàng nếu cần thiết ở đây
        navigate('/shop');
    };

    // Phân quyền hiển thị nút bấm dựa trên trạng thái hiện tại
    const canPay = order.Status === "Chờ thanh toán" && order.PaymentMethod === "VNPAY";
    const canEdit = ["Chờ thanh toán", "Chờ xử lý", "Đã xác nhận"].includes(order.Status);
    const canCancel = ["Chờ thanh toán", "Chờ xử lý", "Đã xác nhận"].includes(order.Status);
    const canReorder = ["Hoàn thành", "Đã hủy"].includes(order.Status);

    return (
        <div className="min-h-screen bg-white py-2 text-[#2D3748] pb-20">
            <div className="container mx-auto px-4 max-w-6xl">
                
                {/* Header điều hướng */}
                <div className="flex justify-between items-center mb-16">
                    <button
                        onClick={() => navigate('/orders')}
                        className="flex items-center gap-2 text-gray-500 hover:text-accent-1 font-bold transition-colors"
                    >
                        <ChevronLeft size={20} /> Đơn hàng của bạn
                    </button>
                    <HelpCircle size={20} className="text-gray-400 cursor-pointer" />
                </div>

                {/* Khối Thông báo thành công */}
                <div className="flex items-center justify-center gap-2 mb-16">
                    <IoCheckmarkCircle size={30} className="text-green-500" />
                    <span className="font-bold tracking-wide text-2xl uppercase">Đặt hàng thành công</span>
                </div>

                {/* Tiêu đề chính & Trạng thái Badge */}
                <div className="flex flex-col sm:flex-row justify-between items-start sm:items-end gap-4 mb-8">
                    <div>
                        <h1 className="text-3xl font-bold text-[#1A202C] font-nunito">Chi tiết đơn hàng #{order.Id}</h1>
                        <p className="text-gray-400 mt-1 text-left">
                            Ngày đặt: {formatDate(orderDate)}
                        </p>
                        <p className="text-gray-400 mt-1 text-left">
                            Thời gian nhận dự kiến: <span className='text-accent-1 font-semibold'>{formatDate(orderDate)} - {formatDate(estimatedDate)}</span>
                        </p>
                    </div>
                    <div className={`px-4 py-1.5 rounded-full text-sm font-bold border flex items-center gap-2 ${isCancelled ? 'bg-red-50 text-red-500 border-red-100' : 'bg-accent-1 text-white border-orange-100'}`}>
                        <GrRadialSelected size={15} />
                        <span>{['Đang trung chuyển', 'Shipper đã nhận', 'Đang giao'].includes(order.Status) ? 'Đang giao' : order.Status}</span>
                    </div>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                    
                    {/* CỘT TRÁI: Tiến trình & Danh sách sản phẩm */}
                    <div className="lg:col-span-2 space-y-6">

                        {/* 1. Tiến trình vận chuyển dạng NGANG */}
                        <div className="bg-white p-8 rounded-2xl shadow-lg border border-gray-100">
                            <h2 className="text-xl font-bold mb-10 text-left">Trạng thái vận chuyển</h2>
                            {!isCancelled ? (
                                <div className="relative flex justify-between">
                                    <div className="absolute top-5 left-6 right-6 h-[2px] bg-gray-100 z-0"></div>
                                    <div
                                        className="absolute top-5 left-6 right-6 h-[2px] bg-accent-1 z-0 transition-all duration-500"
                                        style={{ width: `${currentStepIndex <= 0 ? 0 : (currentStepIndex / (steps.length - 1)) * 100}%` }}
                                    ></div>

                                    {steps.map((step, idx) => {
                                        const active = idx <= currentStepIndex;
                                        return (
                                            <div key={step.id} className="relative z-10 flex flex-col items-center">
                                                <div className={`w-10 h-10 rounded-full flex items-center justify-center border-4 ${active ? 'bg-accent-1 border-[#F3F0ED] text-white' : 'bg-white border-white text-gray-300 shadow-sm'}`}>
                                                    {step.icon}
                                                </div>
                                                <p className={`mt-3 text-sm font-bold ${active ? 'text-[#1A202C]' : 'text-gray-300'}`}>{step.label}</p>
                                            </div>
                                        );
                                    })}
                                </div>
                            ) : (
                                <div className="py-4 text-red-500 font-bold italic text-left">Đơn hàng này đã bị hủy bỏ bới người dùng.</div>
                            )}
                            <div className='text-justify pt-6 font-nunito text-sm text-primary/80 flex items-start gap-3 border-t border-gray-50 mt-6'> 
                                <FaShippingFast size={22} className="text-accent-1 shrink-0 mt-0.5" /> 
                                <span>Dịch vụ <strong>Đảm bảo giao hàng đúng hạn</strong> cam kết đơn hàng sẽ được giao chậm nhất vào ngày {formatDate(estimatedDate)}. Nhận ngay voucher đền bù nếu đơn hàng đến muộn hơn thời gian trên.</span>
                            </div>
                        </div>

                        {/* 2. Accordion hành trình chi tiết shipper (DỌC) */}
                        <div className="bg-white rounded-2xl shadow-lg border border-gray-100 overflow-hidden transition-all duration-300">
                            <button
                                onClick={() => !isCancelled && setIsTrackingOpen(!isTrackingOpen)}
                                disabled={isCancelled}
                                className={`w-full p-6 flex items-center justify-between transition-colors border-b border-gray-50 ${isCancelled ? 'bg-gray-50 cursor-not-allowed' : 'bg-gray-50/50 hover:bg-gray-50'}`}
                            >
                                <div className="flex items-center gap-3">
                                    <Truck size={22} className={!isCancelled ? 'text-green-500' : 'text-gray-400'} />
                                    <div className="text-left font-nunito">
                                        <h2 className="text-lg font-bold text-[#1A202C]">Theo dõi đơn hàng chi tiết</h2>
                                        <p className="text-xs text-gray-400">
                                            {!isCancelled ? 'Bấm vào để xem hành trình di chuyển thực tế của shipper' : 'Không khả dụng cho đơn hàng đã hủy'}
                                        </p>
                                    </div>
                                </div>
                                {!isCancelled && (
                                    <div className={`transform transition-transform duration-300 text-gray-400 ${isTrackingOpen ? 'rotate-180' : ''}`}>
                                        <ChevronLeft size={20} className="-rotate-90" />
                                    </div>
                                )}
                            </button>

                            <div className={`transition-all duration-300 ease-in-out ${isTrackingOpen ? 'max-h-[1200px] p-6 md:p-8 opacity-100' : 'max-h-0 opacity-0 pointer-events-none'}`}>
                                {!isCancelled && (
                                    <div className="relative flex flex-col font-nunito max-w-2xl mx-auto py-4">
                                        {/* Trục dọc nền xám */}
                                        <div className="absolute top-3 bottom-3 left-6 sm:left-1/2 sm:-translate-x-1/2 w-[2px] bg-gray-100 z-0"></div>

                                        {/* Trục dọc tiến trình xanh chạy theo trạng thái */}
                                        <div
                                            className="absolute top-3 left-6 sm:left-1/2 sm:-translate-x-1/2 w-[2px] bg-green-500 z-0 transition-all duration-500"
                                            style={{
                                                height: currentShipperStepIndex <= 0
                                                    ? '0%'
                                                    : `${(currentShipperStepIndex / (steps_shipper.length - 1)) * 100}%`,
                                                maxHeight: 'calc(100% - 32px)'
                                            }}
                                        ></div>

                                        {/* Render các bước chi tiết dọc */}
                                        <div className="space-y-8 relative z-10">
                                            {steps_shipper.map((step, idx) => {
                                                const isPassedOrCurrent = idx <= currentShipperStepIndex;
                                                const isCurrent = idx === currentShipperStepIndex;

                                                return (
                                                    <div key={step.id} className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2 sm:gap-0 relative pl-12 sm:pl-0">
                                                        
                                                        {/* Khối ngày tháng bên trái */}
                                                        <div className="w-full sm:w-[45%] text-left sm:text-right sm:pr-8">
                                                            {isPassedOrCurrent ? (
                                                                <p className={`text-xs font-bold ${isCurrent ? 'text-green-500' : 'text-gray-400'}`}>
                                                                    {idx === 0 ? formatDate(orderDate) : 'Đang cập nhật'}
                                                                </p>
                                                            ) : (
                                                                <p className="text-xs text-gray-300 italic">Chưa thực hiện</p>
                                                            )}
                                                        </div>

                                                        {/* Điểm nút tròn ở giữa */}
                                                        <div className="absolute left-3 sm:relative sm:left-auto flex items-center justify-center w-6 h-6 sm:mx-auto bg-white rounded-full">
                                                            <div className={`rounded-full transition-all duration-300 ${isCurrent
                                                                    ? 'w-4 h-4 bg-green-500 ring-4 ring-green-100 animate-pulse'
                                                                    : isPassedOrCurrent
                                                                        ? 'w-3 h-3 bg-green-500'
                                                                        : 'w-3 h-3 bg-gray-200 border-2 border-white shadow-sm'
                                                            }`} />
                                                        </div>

                                                        {/* Nội dung trạng thái bên phải */}
                                                        <div className="w-full sm:w-[45%] sm:pl-8 text-left">
                                                            <div className="flex items-center gap-2">
                                                                <h4 className={`text-sm font-black transition-colors ${isCurrent ? 'text-green-600 text-base' : isPassedOrCurrent ? 'text-gray-700' : 'text-gray-400'}`}>
                                                                    {step.label}
                                                                </h4>
                                                                {isCurrent && (
                                                                    <span className="text-[9px] uppercase font-black bg-green-100 text-green-700 px-1.5 py-0.5 rounded tracking-wider">
                                                                        Mới nhất
                                                                    </span>
                                                                )}
                                                            </div>

                                                            {isPassedOrCurrent && (
                                                                <div className="mt-1 text-xs text-gray-500 leading-relaxed">
                                                                    {step.id === 'Chờ xử lý' && "Đơn hàng đã được ghi nhận trên hệ thống."}
                                                                    {step.id === 'Đã xác nhận' && "Hệ thống kiểm tra thành công, kho đang đóng gói hàng hóa."}
                                                                    {step.id === 'Shipper đã nhận' && "Đơn vị vận chuyển đối tác đã lấy hàng khỏi kho."}
                                                                    {step.id === 'Đang trung chuyển' && "Đơn hàng đang điều phối qua các bưu cục tổng."}
                                                                    {step.id === 'Đang giao' && (
                                                                        <div className="mt-2 p-2.5 bg-gray-50 rounded-xl border border-gray-100 text-[11px] text-gray-600 space-y-0.5 max-w-xs shadow-sm">
                                                                            <p>Tài xế giao hàng: <span className="font-bold text-gray-800">Vũ Nam Khánh</span></p>
                                                                            <p className="text-blue-600 font-medium">SĐT liên hệ: +84974233552</p>
                                                                            <p className='text-red-500'>Hotline hỗ trợ nhanh: <span className="font-bold text-gray-800">1900 1000</span></p>
                                                                        </div>
                                                                    )}
                                                                    {step.id === 'Hoàn thành' && (
                                                                        <div className="max-w-[200px] rounded-xl overflow-hidden border border-gray-100 shadow-sm bg-white mt-2">
                                                                            <img
                                                                                src="https://images.unsplash.com/photo-1566576721346-d4a3b4eaeb55?q=80&w=200"
                                                                                alt="Hình ảnh giao hàng thành công"
                                                                                className="w-full h-24 object-cover"
                                                                            />
                                                                        </div>
                                                                    )}
                                                                </div>
                                                            )}
                                                        </div>
                                                    </div>
                                                );
                                            })}
                                        </div>
                                    </div>
                                )}
                            </div>
                        </div>

                        {/* 3. Bảng danh sách các sản phẩm */}
                        <div className="bg-white rounded-2xl shadow-lg border border-gray-100 overflow-hidden">
                            <div className="p-6">
                                <h2 className="text-xl font-bold text-left">Sản phẩm đã chọn</h2>
                            </div>
                            <div className="overflow-x-auto">
                                <table className="w-full text-left border-collapse">
                                    <thead>
                                        <tr className="border-b border-gray-100 bg-gray-50/50">
                                            <th className="px-6 py-4 font-bold text-sm text-accent-1">Sản phẩm</th>
                                            <th className="px-6 py-4 text-center font-bold text-sm text-accent-1">Số lượng</th>
                                            <th className="px-6 py-4 text-right font-bold text-sm text-accent-1">Đơn giá</th>
                                            <th className="px-6 py-4 text-right font-bold text-sm text-accent-1">Tổng cộng</th>
                                        </tr>
                                    </thead>
                                    <tbody className="divide-y divide-gray-100">
                                        {order.OrderDetails && order.OrderDetails.map((item, idx) => (
                                            <tr key={idx} className="hover:bg-gray-50/30 transition-colors">
                                                <td className="px-6 py-6">
                                                    <div className="flex items-center gap-4">
                                                        <div className="w-16 h-16 bg-[#F7F7F7] rounded-xl p-2 flex items-center justify-center shrink-0">
                                                            <img src={item.Product?.ImageUrl} alt={item.Product?.Name} className="max-h-full object-contain" />
                                                        </div>
                                                        <div className="text-left">
                                                            <p className="font-bold text-[#1A202C]">{item.Product?.Name}</p>
                                                            <div className="text-[11px] text-gray-400 mt-1 space-y-0.5">
                                                                <p>Vị: {item.FlavorNotes || 'Mặc định'}</p>
                                                                <p>Xay: {translateGrind(item.GrindingOptionId)}</p>
                                                                <p>Trọng lượng: {item.Weight || 'Không rõ'}</p>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td className="px-6 py-6 text-center font-bold text-gray-700">{item.Quantity}</td>
                                                <td className="px-6 py-6 text-right text-gray-500">{(item.UnitPrice || 0).toLocaleString()}₫</td>
                                                <td className="px-6 py-6 text-right font-black text-[#1A202C]">{((item.UnitPrice || 0) * item.Quantity).toLocaleString()}₫</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                    {/* CỘT PHẢI: Địa chỉ giao nhận & Hoá đơn thanh toán */}
                    <div className="space-y-6">
                        
                        {/* Khối Thông tin giao hàng */}
                        <div className="bg-white p-6 rounded-2xl shadow-lg border border-gray-100">
                            <h3 className="font-bold mb-6 text-[#1A202C] text-left text-lg">Thông tin nhận hàng</h3>
                            <div className="space-y-5">
                                <div className="flex gap-4 text-left">
                                    <User size={18} className="text-accent-1 shrink-0 mt-0.5" />
                                    <div>
                                        <p className="text-sm font-bold text-gray-800">{order.ReceiverName || "Chưa cập nhật danh tính"}</p>
                                        <p className="text-[11px] font-bold tracking-wider text-accent-1 uppercase">Người nhận</p>
                                    </div>
                                </div>
                                <div className="flex gap-4 text-left">
                                    <Phone size={18} className="text-accent-1 shrink-0 mt-0.5" />
                                    <div>
                                        <p className="text-sm font-bold text-gray-800">{order.ReceiverPhone || "Chưa có số điện thoại"}</p>
                                        <p className="text-[11px] font-bold tracking-wider text-accent-1 uppercase">Số điện thoại</p>
                                    </div>
                                </div>
                                <div className="flex gap-4 text-left">
                                    <MapPin size={18} className="text-accent-1 shrink-0 mt-0.5" />
                                    <div>
                                        <p className="text-sm font-bold leading-relaxed text-gray-800">
                                            {order.ShippingDetailAddress ?
                                                `${order.ShippingDetailAddress}, ${order.ShippingWard}, ${order.ShippingDistrict}, ${order.ShippingProvince}`
                                                : "Chưa ghi nhận địa chỉ giao hàng"}
                                        </p>
                                        <p className="text-[11px] font-bold tracking-wider text-accent-1 uppercase">Địa chỉ giao nhận</p>
                                    </div>
                                </div>
                            </div>

                            {/* Ghi chú giao hàng */}
                            <div className="mt-6 p-4 bg-[#F1F5F9] rounded-xl flex gap-3 items-start text-left">
                                <FileText size={16} className="text-accent-1 mt-0.5 shrink-0" />
                                <div>
                                    <p className="text-xs italic text-gray-500">"{order.ShippingNote || 'Không có ghi chú thêm cho shipper'}"</p>
                                    <p className="text-[11px] font-bold mt-1 text-accent-1 uppercase tracking-wide">Ghi chú</p>
                                </div>
                            </div>
                        </div>

                        {/* Khối Hoá đơn tổng giá & Cụm nút thao tác xử lý */}
                        <div className="bg-white p-6 rounded-2xl shadow-lg border border-gray-100 font-nunito">
                            <h3 className="font-bold mb-4 text-left text-lg">Chi tiết thanh toán</h3>
                            <div className="space-y-3 text-sm border-b border-gray-100 pb-4">
                                <div className="flex justify-between text-gray-400">
                                    <span>Tạm tính giá gốc</span>
                                    <span className="font-bold text-gray-600">{(order.TotalAmount || 0).toLocaleString()}₫</span>
                                </div>
                                <div className="flex justify-between text-gray-400">
                                    <span>Phí giao hàng toàn quốc</span>
                                    <span className="font-bold text-gray-600">30.000₫</span>
                                </div>
                                <div className="flex justify-between text-green-500">
                                    <span>Voucher giảm giá áp dụng</span>
                                    <span className="font-bold">- {(order.DiscountAmount || 0).toLocaleString()}₫</span>
                                </div>
                            </div>
                            <div className="flex justify-between items-center pt-4">
                                <span className="font-bold text-gray-800">Tổng tiền cuối cùng</span>
                                <span className="text-xl font-black text-[#1A202C]">{(order.FinalAmount || 0).toLocaleString()}₫</span>
                            </div>

                            {/* Các nút sự kiện động dựa trên Status */}
                            <div className="mt-8 space-y-3">
                                {canPay && (
                                    <button
                                        onClick={() => handlePayment(order.Id, order.TotalAmount)}
                                        className="w-full font-bold bg-green-600 text-white py-3.5 rounded-xl uppercase tracking-widest text-xs hover:bg-green-700 shadow-md transition-all transform hover:-translate-y-0.5"
                                    >
                                        Tiến hành Thanh toán (VNPAY)
                                    </button>
                                )}

                                {canEdit && (
                                    <button
                                        onClick={() => handleEdit(order.Id)}
                                        className="w-full font-bold bg-[#2B6CB0] text-white py-3.5 rounded-xl uppercase tracking-widest text-xs hover:bg-[#2C5282] shadow-md transition-all transform hover:-translate-y-0.5"
                                    >
                                        Chỉnh sửa thông tin nhận hàng
                                    </button>
                                )}

                                {canCancel && (
                                    <button
                                        onClick={() => handleCancel(order.Id)}
                                        className="w-full border-2 text-red-500 border-red-500 py-3.5 rounded-xl font-bold uppercase tracking-widest text-xs hover:bg-red-500 hover:text-white transition-all transform hover:-translate-y-0.5"
                                    >
                                        Yêu cầu hủy đơn hàng
                                    </button>
                                )}

                                {canReorder && (
                                    <button
                                        onClick={handleReorder}
                                        className="w-full border-2 text-amber-900 border-amber-900 py-3.5 rounded-xl font-bold uppercase tracking-widest text-xs hover:bg-amber-900 hover:text-white transition-all transform hover:-translate-y-0.5"
                                    >
                                        Mua lại đơn hàng này
                                    </button>
                                )}
                            </div>
                        </div>

                        {/* Khối Hỗ trợ đổi trả đi kèm icon TbMoneybagMoveBack hoàn chỉnh ở cuối */}
                        {order.Status === "Hoàn thành" && (
                            <div className='flex items-center justify-center gap-2 text-center text-sm font-bold text-green-600 bg-green-50 p-4 rounded-2xl border border-green-100 shadow-sm animate-fade-in'>
                                <TbMoneybagMoveBack size={22} className="shrink-0" />
                                <span>Đơn hàng thỏa mãn chính sách bảo hộ: Hỗ trợ trả hàng/hoàn tiền miễn phí trong vòng 7 ngày đầu.</span>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}