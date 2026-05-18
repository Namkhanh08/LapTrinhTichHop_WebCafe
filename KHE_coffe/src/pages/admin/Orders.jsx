import React, { useEffect, useState } from 'react';
import useStore from '../../store/useStore';
import { Search, Filter, Truck, CheckCircle, XCircle, Check, Package } from 'lucide-react';
import { FaArrowRight } from "react-icons/fa";

export default function AdminOrders() {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [currentPage, setCurrentPage] = useState(1);
  const cancelOrder = useStore((state) => state.cancelOrder);

  // Lấy dữ liệu và action từ store
  const {
    orders,
    totalItems,
    fetchAllOrdersAdmin,
    updateOrderStatus,
    updateOrder
  } = useStore();

  // Re-fetch khi thay đổi filter hoặc trang
  useEffect(() => {
    fetchAllOrdersAdmin(currentPage, searchTerm, statusFilter);
  }, [currentPage, statusFilter, searchTerm]);

  const handleUpdateStatus = async (id, newStatus) => {
    try {
      await updateOrderStatus(id, newStatus);

      await fetchAllOrdersAdmin(
        currentPage,
        searchTerm,
        statusFilter
      );

    } catch (error) {
      console.error(error);
      alert("Cập nhật trạng thái thất bại!");
    }
  };

  const handleConfirmAll = async () => {
    const pendingOrders = orders.filter(
      o => o.Status === 'Chờ xử lý'
    );
    if (pendingOrders.length === 0) {
      return alert("Không có đơn hàng nào chờ xử lý.");
    }
    if (
      window.confirm(
        `Xác nhận tất cả ${pendingOrders.length} đơn hàng?`
      )
    ) {
      try {
        await Promise.all(
          pendingOrders.map(order =>
            updateOrderStatus(
              order.Id,
              'Đã xác nhận'
            )
          )
        );
        await fetchAllOrdersAdmin(
          currentPage,
          searchTerm,
          statusFilter
        );

        alert("Đã duyệt tất cả đơn!");

      } catch (err) {

        console.error(err);

        alert("Duyệt đơn thất bại!");
      }
    }
  };

  const handleCancel = async (orderId) => {
    if (window.confirm("Bạn có chắc chắn muốn hủy đơn hàng này?")) {
      try {
        await cancelOrder(orderId);
        alert("Hủy đơn hàng thành công!");
      } catch (error) {
        alert(error.response?.data || "Hủy đơn thất bại!");
      }
    }
  };

  const translateGrind = (type) => {
    switch (type) {
      case 1: return "Nguyên Hạt";
      case 2: return "Phan Phin";
      case 3: return "Pha Máy";
      case 4: return "Ủ Lạnh";
      case 5: return "Kiểu Pháp";
      default: return type;
    }
  }

  const handleSearchChange = (e) => {
    setSearchTerm(e.target.value);
    setCurrentPage(1);
  };

  const handleFilterChange = (e) => {
    setStatusFilter(e.target.value);
    setCurrentPage(1);
  };

  return (
    <div className="p-6 animate-fade-in">
      {/* Header & Controls */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
        <div>
          <h1 className="font-montserrat font-bold text-2xl text-gray-800">Quản Lý Đơn Hàng</h1>
          <p className="text-gray-500 text-sm font-nunito flex items-center gap-2">
            Quy trình: <span className="text-blue-500">Xác nhận</span> <FaArrowRight size={10} /> <span className="text-indigo-500">Giao hàng</span>
          </p>
        </div>

        <div className="flex flex-wrap gap-3 w-full md:w-auto">
          <button
            onClick={handleConfirmAll}
            className="flex items-center gap-2 px-4 py-2 bg-green-600 text-white rounded-xl hover:bg-green-700 transition-all text-sm font-bold shadow-sm"
          >
            <CheckCircle size={18} /> Duyệt nhanh đơn chờ
          </button>

          <div className="relative flex-1 md:w-64">
            <input
              type="text"
              placeholder="Mã đơn, khách hàng..."
              value={searchTerm}
              onChange={handleSearchChange}

              className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-xl focus:ring-2 focus:ring-primary/20 outline-none"
            />
            <Search size={18} className="absolute left-3 top-2.5 text-gray-400" />
          </div>

          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-4 py-2 bg-white border border-gray-200 rounded-xl outline-none text-sm"
          >
            <option value="all">Tất cả trạng thái</option>
            <option value="Chờ xử lý">Chờ xử lý</option>
            <option value="Đã xác nhận">Đã xác nhận</option>
            <option value="Đang giao">Đang giao</option>
            <option value="Hoàn thành">Hoàn thành</option>
            <option value="Đã hủy">Đã hủy</option>
          </select>
        </div>
      </div>

      {/* Table Section */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left font-nunito text-sm">
            <thead className="bg-gray-50 text-gray-600 font-bold border-b">
              <tr>
                <th className="px-6 py-4">Mã Đơn</th>
                <th className="px-6 py-4">Khách Hàng</th>
                <th className="px-6 py-4">Sản Phẩm</th>
                <th className="px-6 py-4 text-right">Tổng Tiền</th>
                <th className="px-6 py-4 text-center">Trạng Thái</th>
                <th className="px-6 py-4 text-center">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {orders.map(order => (
                <tr key={order.Id} className="hover:bg-gray-50/50 transition-colors">
                  <td className="px-6 py-4 font-bold text-primary">#{order.Id}</td>
                  <td className="px-6 py-4">
                    <div className="font-bold">{order.ReceiverName}</div>
                    <div className="text-xs text-gray-400">{order.ReceiverPhone}</div>
                  </td>
                  <td className="px-6 py-4">
                    <div className="space-y-1">
                      {order.OrderDetails && order.OrderDetails.length > 0 ? (
                        order.OrderDetails.map((item, idx) => (
                          <div key={idx} className="flex flex-col mb-2 border-b border-gray-50 last:border-0 pb-1">
                            <div className="flex items-center gap-1 text-xs">
                              <Package size={20} className="text-accent-1" />
                              <span className="font-medium text-gray-800">
                                {item.Product?.Name || "Sản phẩm không xác định"}
                              </span>
                              <span className="text-gray-500 font-bold ml-1">x{item.Quantity}</span>
                            </div>
                            {/* Hiển thị thêm tùy chọn nếu có để admin dễ xử lý */}
                            <div className="text-[10px] text-gray-400 ml-4">
                              {item.Weight && <span>{item.Weight} | </span>}
                              {item.FlavorNotes && <span>{item.FlavorNotes} | </span>}
                              {translateGrind(item.GrindingOptionId) && <span>{translateGrind(item.GrindingOptionId)}</span>}
                            </div>
                          </div>
                        ))
                      ) : (
                        <span className="text-red-400 italic text-xs">Không có chi tiết đơn hàng</span>
                      )}
                    </div>
                  </td>
                  <td className="px-6 py-4 font-bold text-right">
                    {order.FinalAmount.toLocaleString()}₫
                    <div className='text-accent-1/70'>{order.PaymentMethod}</div>
                  </td>
                  <td className="px-6 py-4 text-center">
                    <StatusBadge status={order.Status} />
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex justify-center gap-2">
                      {order.Status === 'Chờ xử lý' && (
                        <ActionButton icon={<Check size={16} />} color="blue" onClick={() => handleUpdateStatus(order.Id, 'Đã xác nhận')} title="Xác nhận" />
                      )}
                      {order.Status === 'Đã xác nhận' && (
                        <ActionButton icon={<Truck size={16} />} color="indigo" onClick={() => handleUpdateStatus(order.Id, 'Đang giao')} title="Giao hàng" />
                      )}
                      {order.Status === 'Đang giao' && (
                        <ActionButton icon={<CheckCircle size={16} />} color="green" onClick={() => handleUpdateStatus(order.Id, 'Hoàn thành')} title="Hoàn thành" />
                      )}
                      {!['Hoàn thành', 'Đang giao', 'Đã hủy'].includes(order.Status) && (
                        <ActionButton icon={<XCircle size={16} />} color="red" onClick={() => handleUpdateStatus(order.Id, 'Đã hủy')} title="Hủy đơn" />
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        <div className="p-4 border-t flex justify-between items-center bg-gray-50/50">
          <span className="text-gray-500 italic">Tổng cộng: {totalItems} đơn hàng</span>
          <div className="flex gap-2">
            <button
              onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
              disabled={currentPage === 1}
              className="px-4 py-1.5 border rounded-lg bg-white disabled:opacity-50 hover:bg-gray-50 transition-colors"
            >
              Trước
            </button>
            <div className="px-4 py-1.5 bg-primary text-white rounded-lg font-bold">{currentPage}</div>
            <button
              onClick={() => setCurrentPage(prev => prev + 1)}
              disabled={currentPage * 10 >= totalItems}
              className="px-4 py-1.5 border rounded-lg bg-white disabled:opacity-50 hover:bg-gray-50 transition-colors"
            >
              Sau
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

// Sub-component cho Badge trạng thái để code sạch hơn
const StatusBadge = ({ status }) => {
  const styles = {
    'Chờ thanh toán': 'bg-orange-100 text-orange-600',
    'Chờ xử lý': 'bg-blue-100 text-blue-600',
    'Đã xác nhận': 'bg-purple-100 text-purple-600',
    'Đang giao': 'bg-indigo-100 text-indigo-600',
    'Hoàn thành': 'bg-green-100 text-green-600',
    'Đã hủy': 'bg-red-100 text-red-600',
  };
  return (
    <span className={`px-3 py-1 rounded-full text-[11px] font-bold uppercase tracking-wider ${styles[status] || 'bg-gray-100 text-gray-600'}`}>
      {status}
    </span>
  );
};

// Sub-component cho Button thao tác
const ActionButton = ({ icon, color, onClick, title }) => {
  const colors = {
    blue: 'bg-blue-50 text-blue-600 hover:bg-blue-600',
    indigo: 'bg-indigo-50 text-indigo-600 hover:bg-indigo-600',
    green: 'bg-green-50 text-green-600 hover:bg-green-600',
    red: 'bg-red-50 text-red-600 hover:bg-red-600',
  };
  return (
    <button
      onClick={onClick}
      title={title}
      className={`p-2 rounded-lg transition-all hover:text-white ${colors[color]}`}
    >
      {icon}
    </button>
  );
};