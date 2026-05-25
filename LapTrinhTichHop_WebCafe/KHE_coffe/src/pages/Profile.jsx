import { useEffect, useState } from "react";
import API from "../services/api";
import useStore from "../store/useStore";

export default function Profile() {
    const user = useStore((state) => state.user);
    const setUser = useStore((state) => state.setUser);
    const [profile, setProfile] = useState(null);
    const [form, setForm] = useState({ name: "", phone: "", address: "" });
    const [passwordForm, setPasswordForm] = useState({
        currentPassword: "",
        newPassword: "",
        confirmPassword: ""
    });
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [message, setMessage] = useState("");

    useEffect(() => {
        if (!user) {
            setLoading(false);
            return;
        }

        API.getProfile()
            .then((res) => {
                const data = res.data.user;
                setProfile(data);
                setForm({
                    name: data.name || data.fullName || "",
                    phone: data.phone || "",
                    address: data.address || ""
                });
            })
            .catch((err) => {
                console.error("Load profile failed:", err);
                setMessage("Khong tai duoc thong tin tai khoan.");
            })
            .finally(() => setLoading(false));
    }, [user]);

    const handleProfileSubmit = async (event) => {
        event.preventDefault();
        setSaving(true);
        setMessage("");

        try {
            const res = await API.updateProfile(form);
            const updated = res.data.user;
            setProfile(updated);
            setUser({
                ...user,
                name: updated.name || updated.fullName,
                phone: updated.phone,
                address: updated.address
            });
            setMessage("Da cap nhat thong tin ca nhan.");
        } catch (err) {
            const msg = err.response?.data?.errors?.[0] || err.response?.data?.error || "Cap nhat that bai.";
            setMessage(msg);
        } finally {
            setSaving(false);
        }
    };

    const handlePasswordSubmit = async (event) => {
        event.preventDefault();
        setSaving(true);
        setMessage("");

        try {
            await API.changePassword(passwordForm);
            setPasswordForm({ currentPassword: "", newPassword: "", confirmPassword: "" });
            setMessage("Da doi mat khau.");
        } catch (err) {
            const msg = err.response?.data?.errors?.[0] || err.response?.data?.error || "Doi mat khau that bai.";
            setMessage(msg);
        } finally {
            setSaving(false);
        }
    };

    if (!user) {
        return <div className="p-10 text-center">Vui long dang nhap</div>;
    }

    if (loading) {
        return <div className="p-10 text-center">Dang tai...</div>;
    }

    return (
        <div className="min-h-screen bg-[#F5EFE6] pt-28 px-6 md:px-12">
            <div className="max-w-5xl mx-auto space-y-6">
                <div className="bg-white rounded-2xl shadow-lg p-6 flex items-center gap-6">
                    <div className="w-20 h-20 rounded-full bg-primary flex items-center justify-center text-white text-2xl font-bold">
                        {(profile?.name || user.name || "U").charAt(0)}
                    </div>

                    <div>
                        <h2 className="text-2xl font-bold text-primary">
                            {profile?.name || user.name}
                        </h2>
                        <p className="text-gray-500">
                            {user.role === "Admin" ? "Admin" : "Customer"}
                        </p>
                        <p className="text-gray-500">
                            Diem thuong: {profile?.loyaltyPoints ?? 0}
                        </p>
                    </div>
                </div>

                {message && (
                    <div className="bg-white rounded-xl shadow p-4 text-primary">
                        {message}
                    </div>
                )}

                <form onSubmit={handleProfileSubmit} className="bg-white rounded-2xl shadow-lg p-8 space-y-5">
                    <h3 className="text-xl font-semibold text-primary border-b pb-2">
                        Thong tin ca nhan
                    </h3>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                        <label className="space-y-2">
                            <span className="block text-sm font-semibold text-gray-700">Ho ten</span>
                            <input
                                className="w-full border rounded-lg px-4 py-3"
                                value={form.name}
                                onChange={(e) => setForm({ ...form, name: e.target.value })}
                            />
                        </label>

                        <label className="space-y-2">
                            <span className="block text-sm font-semibold text-gray-700">So dien thoai</span>
                            <input
                                className="w-full border rounded-lg px-4 py-3"
                                value={form.phone}
                                onChange={(e) => setForm({ ...form, phone: e.target.value })}
                            />
                        </label>

                        <label className="md:col-span-2 space-y-2">
                            <span className="block text-sm font-semibold text-gray-700">Dia chi giao hang</span>
                            <textarea
                                className="w-full border rounded-lg px-4 py-3 min-h-24"
                                value={form.address}
                                onChange={(e) => setForm({ ...form, address: e.target.value })}
                            />
                        </label>
                    </div>

                    <button
                        className="bg-primary text-white px-5 py-3 rounded-lg disabled:opacity-60"
                        disabled={saving}
                    >
                        {saving ? "Dang luu..." : "Luu thong tin"}
                    </button>
                </form>

                <form onSubmit={handlePasswordSubmit} className="bg-white rounded-2xl shadow-lg p-8 space-y-5">
                    <h3 className="text-xl font-semibold text-primary border-b pb-2">
                        Doi mat khau
                    </h3>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
                        <input
                            className="border rounded-lg px-4 py-3"
                            type="password"
                            placeholder="Mat khau hien tai"
                            value={passwordForm.currentPassword}
                            onChange={(e) => setPasswordForm({ ...passwordForm, currentPassword: e.target.value })}
                            required
                        />
                        <input
                            className="border rounded-lg px-4 py-3"
                            type="password"
                            placeholder="Mat khau moi"
                            value={passwordForm.newPassword}
                            onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
                            required
                        />
                        <input
                            className="border rounded-lg px-4 py-3"
                            type="password"
                            placeholder="Nhap lai mat khau moi"
                            value={passwordForm.confirmPassword}
                            onChange={(e) => setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })}
                            required
                        />
                    </div>

                    <button
                        className="bg-primary text-white px-5 py-3 rounded-lg disabled:opacity-60"
                        disabled={saving}
                    >
                        {saving ? "Dang luu..." : "Doi mat khau"}
                    </button>
                </form>
            </div>
        </div>
    );
}
