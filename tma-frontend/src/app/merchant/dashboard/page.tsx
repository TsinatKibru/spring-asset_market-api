"use client";

import { useEffect, useState } from "react";
import { useApi } from "@/components/hooks/useApi";
import { useTelegram } from "@/components/providers/TelegramProvider";
import { LayoutDashboard, Calendar, Inbox, Plus, Package } from "lucide-react";
import Link from "next/link";
import Image from "next/image";
import { useRouter } from "next/navigation";

export default function MerchantDashboard() {
    const { fetchWithAuth, postWithAuth } = useApi();
    const { webApp } = useTelegram();
    const router = useRouter();
    const [stats, setStats] = useState({ activeListings: 0, pendingViewings: 0 });
    const [requests, setRequests] = useState<any[]>([]);
    const [properties, setProperties] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState<'requests' | 'properties'>('requests');

    useEffect(() => {
        const loadDashboard = async () => {
            try {
                // Fetch Merchant Viewings (Requests)
                const requestsData = await fetchWithAuth("/viewings/merchant?size=50");
                setRequests(requestsData.content);

                // Fetch Properties (My Properties)
                const propsData = await fetchWithAuth("/properties?size=50");
                setProperties(propsData.content);

                setStats({
                    activeListings: propsData.totalElements,
                    pendingViewings: requestsData.content.filter((r: any) => r.status === "PENDING").length
                });
            } catch (error) {
                console.error("Dashboard load failed:", error);
            } finally {
                setLoading(false);
            }
        };

        loadDashboard();
    }, [fetchWithAuth]);

    const handleUpdateStatus = async (requestId: number, newStatus: string) => {
        webApp.HapticFeedback.impactOccurred('medium');
        try {
            await fetchWithAuth(`/viewings/${requestId}/status?status=${newStatus}`, {
                method: "PATCH"
            });
            webApp.showConfirm(`Viewing ${newStatus.toLowerCase()} successfully.`);
            // Refresh requests
            const requestsData = await fetchWithAuth("/viewings/merchant?size=50");
            setRequests(requestsData.content);
        } catch (error) {
            webApp.showAlert("Failed to update status.");
        }
    };

    const handleDeleteProperty = async (propertyId: number, title: string) => {
        webApp.HapticFeedback.impactOccurred('heavy');
        const confirmed = await new Promise((resolve) => {
            webApp.showPopup({
                title: "Confirm Deletion",
                message: `Are you sure you want to remove "${title}"? This action cannot be undone.`,
                buttons: [
                    { id: "delete", type: "destructive", text: "Delete Listing" },
                    { type: "cancel" }
                ]
            }, (btnId) => resolve(btnId === 'delete'));
        });

        if (!confirmed) return;

        try {
            await fetchWithAuth(`/properties/${propertyId}`, {
                method: "DELETE"
            });
            webApp.showConfirm("Listing deleted.");
            // Refresh properties
            const propsData = await fetchWithAuth("/properties?size=50");
            setProperties(propsData.content);
            setStats(prev => ({ ...prev, activeListings: propsData.totalElements }));
        } catch (error) {
            webApp.showAlert("Failed to delete property.");
        }
    };

    if (loading) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-white dark:bg-black">
                <div className="h-8 w-8 animate-spin rounded-full border-4 border-zinc-200 border-t-zinc-900 dark:border-zinc-800 dark:border-t-white" />
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-zinc-50 pb-32 dark:bg-black">
            {/* Header */}
            <div className="bg-white px-6 pt-12 pb-8 shadow-sm dark:bg-zinc-900">
                <div className="flex items-center justify-between">
                    <div>
                        <h1 className="text-3xl font-black tracking-tight text-zinc-900 dark:text-white">Merchant Hub</h1>
                        <p className="mt-2 text-zinc-500 dark:text-zinc-400">Manage your luxury estate business</p>
                    </div>
                </div>

                {/* Quick Stats */}
                <div className="mt-8 grid grid-cols-2 gap-4">
                    <div className="rounded-2xl bg-zinc-50 p-6 dark:bg-zinc-800/50">
                        <p className="text-[10px] font-black uppercase tracking-widest text-zinc-400">Active Listings</p>
                        <p className="mt-1 text-2xl font-black text-zinc-900 dark:text-white">{stats.activeListings}</p>
                    </div>
                    <div className="rounded-2xl bg-amber-50 p-6 dark:bg-amber-900/10">
                        <p className="text-[10px] font-black uppercase tracking-widest text-amber-500/60">Pending Viewings</p>
                        <p className="mt-1 text-2xl font-black text-amber-600 dark:text-amber-500">{stats.pendingViewings}</p>
                    </div>
                </div>

                {/* Tabs */}
                <div className="mt-8 flex rounded-2xl bg-zinc-100 p-1 dark:bg-zinc-800">
                    <button
                        onClick={() => setActiveTab('requests')}
                        className={`flex-1 rounded-xl py-3 text-xs font-black uppercase tracking-wider transition-all ${activeTab === 'requests'
                            ? "bg-white text-zinc-900 shadow-sm dark:bg-zinc-700 dark:text-white"
                            : "text-zinc-400"
                            }`}
                    >
                        Requests
                    </button>
                    <button
                        onClick={() => setActiveTab('properties')}
                        className={`flex-1 rounded-xl py-3 text-xs font-black uppercase tracking-wider transition-all ${activeTab === 'properties'
                            ? "bg-white text-zinc-900 shadow-sm dark:bg-zinc-700 dark:text-white"
                            : "text-zinc-400"
                            }`}
                    >
                        Properties
                    </button>
                </div>
            </div>

            {/* Content Area */}
            <div className="px-6 mt-8">
                {activeTab === 'requests' ? (
                    <div className="space-y-4">
                        <div className="flex items-center justify-between mb-2">
                            <h2 className="text-xl font-black text-zinc-900 dark:text-white">Viewing Requests</h2>
                            <span className="text-xs font-bold text-zinc-400">{requests.length} Total</span>
                        </div>
                        {requests.length === 0 ? (
                            <div className="rounded-3xl border border-zinc-100 bg-white p-12 text-center dark:border-zinc-800 dark:bg-zinc-900">
                                <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-2xl bg-zinc-50 dark:bg-zinc-800">
                                    <Inbox className="text-zinc-300 dark:text-zinc-600" />
                                </div>
                                <p className="mt-4 text-sm font-bold text-zinc-400">No requests yet</p>
                            </div>
                        ) : (
                            requests.map((req) => (
                                <div key={req.id} className="rounded-2xl bg-white p-5 shadow-sm dark:bg-zinc-900 ring-1 ring-zinc-100 dark:ring-zinc-800">
                                    <div className="flex items-start justify-between">
                                        <div>
                                            <p className="text-[10px] font-black uppercase tracking-widest text-zinc-400">Property</p>
                                            <h3 className="mt-1 font-bold text-zinc-900 dark:text-white">{req.propertyTitle}</h3>
                                            <p className="mt-2 text-sm text-zinc-500 dark:text-zinc-400">
                                                From: <span className="font-bold text-zinc-700 dark:text-zinc-300">@{req.username}</span>
                                            </p>
                                        </div>
                                        <span className={`rounded-full px-2 py-1 text-[8px] font-black uppercase tracking-tighter ${req.status === 'PENDING' ? 'bg-amber-100 text-amber-600 dark:bg-amber-900/20 dark:text-amber-500' :
                                            req.status === 'APPROVED' ? 'bg-emerald-100 text-emerald-600 dark:bg-emerald-900/20 dark:text-emerald-500' :
                                                'bg-zinc-100 text-zinc-500 dark:bg-zinc-800'
                                            }`}>
                                            {req.status}
                                        </span>
                                    </div>

                                    <div className="mt-4 flex items-center gap-2 text-[10px] font-bold text-zinc-400 bg-zinc-50 dark:bg-zinc-800/50 p-2 rounded-lg">
                                        <Calendar size={12} />
                                        <span>{new Date(req.requestedAt).toLocaleString()}</span>
                                    </div>

                                    {req.status === 'PENDING' && (
                                        <div className="mt-6 flex gap-3">
                                            <button
                                                onClick={() => handleUpdateStatus(req.id, 'APPROVED')}
                                                className="flex-1 rounded-xl bg-zinc-900 py-3 text-xs font-black text-white active:scale-95 transition-transform dark:bg-white dark:text-black"
                                            >
                                                Approve
                                            </button>
                                            <button
                                                onClick={() => handleUpdateStatus(req.id, 'REJECTED')}
                                                className="flex-1 rounded-xl bg-zinc-100 py-3 text-xs font-black text-zinc-500 active:scale-95 transition-transform dark:bg-zinc-800 dark:text-zinc-400"
                                            >
                                                Reject
                                            </button>
                                        </div>
                                    )}
                                </div>
                            ))
                        )}
                    </div>
                ) : (
                    <div className="space-y-6">
                        {/* Category Architect Entry Point */}
                        <div
                            onClick={() => router.push('/merchant/categories')}
                            className="group relative overflow-hidden rounded-3xl bg-zinc-900 p-6 shadow-xl transition-all active:scale-[0.98] dark:bg-white"
                        >
                            <div className="flex items-center justify-between">
                                <div className="flex items-center gap-4">
                                    <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-zinc-800 text-white dark:bg-zinc-100 dark:text-black">
                                        <Package size={24} />
                                    </div>
                                    <div>
                                        <h3 className="text-lg font-black text-white dark:text-black">Asset Type Manager</h3>
                                        <p className="text-[10px] font-bold uppercase tracking-widest text-zinc-400">Configure Schemas & Categories</p>
                                    </div>
                                </div>
                                <div className="h-8 w-8 rounded-full border border-white/10 flex items-center justify-center text-white dark:border-black/10 dark:text-black">
                                    <Plus size={16} />
                                </div>
                            </div>
                        </div>

                        <div className="flex items-center justify-between mb-2">
                            <h2 className="text-xl font-black text-zinc-900 dark:text-white">My Properties</h2>
                            <button
                                onClick={() => router.push('/merchant/property/add')}
                                className="flex items-center gap-2 rounded-full bg-zinc-900 px-4 py-2 text-[10px] font-black uppercase tracking-wider text-white dark:bg-white dark:text-black"
                            >
                                <Plus size={14} /> Add Property
                            </button>
                        </div>
                        {properties.length === 0 ? (
                            <div className="rounded-3xl border border-zinc-100 bg-white p-12 text-center dark:border-zinc-800 dark:bg-zinc-900">
                                <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-2xl bg-zinc-50 dark:bg-zinc-800">
                                    <Plus className="text-zinc-300 dark:text-zinc-600" />
                                </div>
                                <p className="mt-4 text-sm font-bold text-zinc-400">No properties listed yet</p>
                            </div>
                        ) : (
                            properties.map((prop) => (
                                <div key={prop.id} className="group relative overflow-hidden rounded-3xl bg-white p-4 shadow-sm ring-1 ring-zinc-100 dark:bg-zinc-900 dark:ring-zinc-800">
                                    <div className="flex gap-4">
                                        <div className="relative h-20 w-20 flex-shrink-0 overflow-hidden rounded-2xl bg-zinc-100 dark:bg-zinc-800">
                                            {prop.imageUrls?.[0] ? (
                                                <Image src={prop.imageUrls[0]} alt={prop.title} fill className="object-cover" />
                                            ) : (
                                                <div className="flex h-full w-full items-center justify-center">
                                                    <Package size={20} className="text-zinc-300 dark:text-zinc-600" />
                                                </div>
                                            )}
                                        </div>
                                        <div className="flex-1">
                                            <div className="flex items-center justify-between">
                                                <span className="text-[8px] font-black uppercase tracking-widest text-zinc-400">{prop.categoryName}</span>
                                                <span className="text-sm font-black text-zinc-900 dark:text-white">${prop.price.toLocaleString()}</span>
                                            </div>
                                            <h3 className="mt-1 font-bold text-zinc-900 dark:text-white line-clamp-1">{prop.title}</h3>
                                            <p className="mt-1 text-[10px] text-zinc-500 line-clamp-1">{prop.location}</p>

                                            <div className="mt-3 flex gap-2">
                                                <button
                                                    onClick={() => router.push(`/merchant/property/${prop.id}/edit`)}
                                                    className="flex-1 rounded-lg bg-zinc-50 py-2 text-[8px] font-black uppercase tracking-widest text-zinc-500 dark:bg-zinc-800"
                                                >
                                                    Edit
                                                </button>
                                                <button
                                                    onClick={() => handleDeleteProperty(prop.id, prop.title)}
                                                    className="flex-1 rounded-lg bg-red-50 py-2 text-[8px] font-black uppercase tracking-widest text-red-400 dark:bg-red-900/10"
                                                >
                                                    Delete
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}
