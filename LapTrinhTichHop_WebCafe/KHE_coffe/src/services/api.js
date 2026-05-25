import axios from "axios";

const API_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

// Tất cả đều đi qua Gateway
const api = axios.create({
    baseURL: API_URL,
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
    register: (data) => api.post("/auth/register", data),
    getProfile: () => api.get("/auth/profile"),
    updateProfile: (data) => api.put("/auth/profile", data),
    changePassword: (data) => api.post("/auth/change-password", data),

    // PRODUCTS
    getProducts: () => api.get("/products"),
    getProductById: (id) => api.get(`/products/${id}`),
    createProduct: (data) => api.post("/products", data),
    updateProduct: (id, data) => api.put(`/products/${id}`, data),
    deleteProduct: (id) => api.delete(`/products/${id}`),

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

    // INVENTORY
    getInventory: () =>
        api.get("/inventory"),

    updateInventoryItem: (id, data) =>
        api.put(`/inventory/${id}`, data),

    // BATCHES
    getBatches: () =>
        api.get("/batches"),

    createBatch: (data) =>
        api.post("/batches", data),

    updateBatchStatus: (id, status) =>
        api.put(`/batches/${id}/status`, { status }),

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

    toggleVoucher: (id, active) =>
        api.patch(`/vouchers/${id}/toggle`, null, {
            params: { active }
        }),

};

console.log("API OBJECT:", API);

export default API;
