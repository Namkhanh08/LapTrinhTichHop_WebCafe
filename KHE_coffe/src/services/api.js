import axios from "axios";

// Tất cả đều đi qua Gateway
const api = axios.create({
    baseURL: "http://localhost:5096",
    headers: {
        "Content-Type": "application/json"
    }
});

// Tự động đính kèm JWT token vào mọi request
api.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

const API = {

    // AUTH
    login: (data) => api.post("/auth/login", data),

    // PRODUCTS
    getProducts: () => api.get("/products"),
    getProductById: (id) => api.get(`/products/${id}`),

    // CARTS
    getCart: () => api.get("/carts"),
    addToCart: (data) => api.post("/carts/add", data),

    updateCartItem: (data) =>
        api.put("/carts/update", data),

    removeCartItem: (
        productId,
        grindingOptionId,
        flavorNotes,
        weight
    ) =>
        api.delete("/carts/remove", {
            data: {
                productId,
                grindingOptionId,
                flavorNotes,
                weight
            }
        }),

    // ORDERS
    getMyOrders: () => api.get("/orders"),

    fetchAllOrdersAdmin: (
        page = 1,
        searchTerm = "",
        status = "all"
    ) =>
        api.get("/orders/admin/all", {
            params: { page, searchTerm, status }
        }),

    updateOrderStatus: (id, status) =>
        api.put(`/orders/${id}/status`, null, {
            params: { status }
        }),

    confirmOrder: (id) =>
        api.put(`/orders/${id}/confirm`),

    getOrderById: (id) =>
        api.get(`/orders/${id}`),

    createOrder: (data) =>
        api.post("/orders", data),

    cancelOrder: (orderId) =>
        api.put(`/orders/${orderId}/cancel`),

    updateOrder: (id, data) =>
        api.put(`/orders/${id}`, data),

    // DASHBOARD
    getDashboard: () =>
        api.get("/dashboard"),

    // VOUCHERS
    getVouchersAdmin: () =>
        api.get("/vouchers"),

    getAvailableVouchers: (data) =>
        api.post("/vouchers/available", data),

    getPublicVouchers: () =>
        api.get('/vouchers/public'),

    createVoucher: (data) =>
        api.post("/vouchers", data),

    updateVoucher: (id, data) =>
        api.put(`/vouchers/${id}`, data),

    deleteVoucher: (id) =>
        api.delete(`/vouchers/${id}`),

    toggleVoucher: (id) =>
        api.patch(`/vouchers/${id}/toggle`),

};

console.log("API OBJECT:", API);

export default API;