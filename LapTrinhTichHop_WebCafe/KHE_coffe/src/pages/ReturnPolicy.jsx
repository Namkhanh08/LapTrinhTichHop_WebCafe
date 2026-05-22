import React from 'react';
import { IoIosArrowRoundBack } from "react-icons/io";
import { Link } from 'react-router-dom';

export default function ReturnPolicy() {
  return (
    <div className="bg-pinky-gray min-h-screen py-16">
      <div className="container mx-auto px-6 lg:px-12 max-w-4xl bg-white p-10 md:p-16 rounded-[40px] shadow-2xl">
        <Link to="/" className="inline-flex items-center gap-2 text-primary/70 hover:text-accent-1 font-nunito font-bold mb-8 transition-all">
          <IoIosArrowRoundBack size={28} /> Về trang chủ
        </Link>
        
        <h1 className="font-montserrat font-black text-4xl text-primary mb-8 border-b-4 border-accent-1 inline-block pb-2">CHÍNH SÁCH TRẢ HÀNG & HOÀN TIỀN</h1>
        
        <div className="space-y-8 font-nunito text-primary/80 leading-relaxed text-lg">
          <section>
            <h2 className="font-bold text-2xl text-accent-1 mb-3">1. Điều kiện trả hàng</h2>
            <p>Tại REVO Coffee, chúng tôi luôn mong muốn mang đến những hạt cà phê chất lượng nhất. Nếu bạn không hài lòng, bạn có thể yêu cầu trả hàng trong vòng <strong>07 ngày</strong> kể từ ngày nhận hàng với các điều kiện sau:</p>
            <ul className="list-disc pl-6 mt-3 space-y-2">
              <li>Sản phẩm bị lỗi do nhà sản xuất (bao bì rách, hở van màng, cà phê bị mốc).</li>
              <li>Sản phẩm giao không đúng với đơn đặt hàng.</li>
              <li>Sản phẩm chưa qua sử dụng, còn nguyên tem mác và bao bì gốc.</li>
            </ul>
          </section>

          <section>
            <h2 className="font-bold text-2xl text-accent-1 mb-3">2. Quy trình trả hàng & Hoàn tiền</h2>
            <ol className="list-decimal pl-6 mt-3 space-y-2">
              <li>Liên hệ với bộ phận CSKH qua số Hotline: <strong>1900 1234</strong> hoặc email <strong>support@revocoffee.vn</strong> để thông báo lý do trả hàng.</li>
              <li>Đóng gói sản phẩm cẩn thận và gửi về địa chỉ: 123 Đường Cà Phê, Quận 1, TP. HCM.</li>
              <li>Sau khi nhận và kiểm tra sản phẩm, REVO sẽ tiến hành hoàn tiền qua phương thức chuyển khoản ngân hàng hoặc ví điện tử trong vòng 3-5 ngày làm việc.</li>
            </ol>
          </section>

          <section>
            <h2 className="font-bold text-2xl text-accent-1 mb-3">3. Phí vận chuyển</h2>
            <p>REVO Coffee sẽ chi trả 100% phí vận chuyển chiều đi và chiều về nếu sản phẩm bị lỗi từ phía chúng tôi. Trong các trường hợp khác (như thay đổi ý định), khách hàng sẽ tự chịu phí vận chuyển.</p>
          </section>

          <section className="bg-accent-1/10 p-6 rounded-2xl border border-accent-1/20 mt-8">
            <h3 className="font-bold text-xl text-primary mb-2">Bạn cần hỗ trợ thêm?</h3>
            <p>Đừng ngần ngại gọi cho chúng tôi nếu bạn có bất kỳ thắc mắc nào về chất lượng cà phê. REVO luôn lắng nghe và sẵn sàng giải quyết để đảm bảo trải nghiệm thưởng thức của bạn luôn trọn vẹn.</p>
          </section>
        </div>
      </div>
    </div>
  );
}
