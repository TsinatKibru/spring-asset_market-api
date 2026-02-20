"use client";

import { useTelegram } from "@/components/providers/TelegramProvider";
import { useAuth } from "@/components/providers/AuthProvider";
import { Shield, MapPin, Globe, CreditCard } from "lucide-react";

export default function ProfilePage() {
    const { user: tgUser } = useTelegram();
    const { user: authUser, isLoading } = useAuth();

    const roles = Array.isArray(authUser?.roles) ? authUser.roles : [];
    const isAdmin = roles.some((r: string) =>
        r.toUpperCase() === "ROLE_ADMIN" || r.toUpperCase() === "ADMIN"
    );

    return (
        <div className="min-h-screen bg-zinc-50 px-6 pt-12 pb-32 dark:bg-black">
            <header className="flex flex-col items-center">
                <div className="relative">
                    <div className="h-24 w-24 overflow-hidden rounded-full bg-zinc-200 ring-4 ring-white shadow-xl dark:bg-zinc-800 dark:ring-zinc-900">
                        {tgUser?.photo_url ? (
                            <img src={tgUser.photo_url} alt={tgUser.first_name} className="h-full w-full object-cover" />
                        ) : (
                            <div className="flex h-full w-full items-center justify-center text-2xl font-black text-zinc-400">
                                {tgUser?.first_name?.[0] || "U"}
                            </div>
                        )}
                    </div>
                    {isAdmin && (
                        <div className="absolute -bottom-1 -right-1 flex h-8 w-8 items-center justify-center rounded-full bg-indigo-600 text-white shadow-lg ring-2 ring-white dark:ring-zinc-900">
                            <Shield size={16} fill="currentColor" />
                        </div>
                    )}
                </div>

                <div className="mt-6 text-center">
                    <h1 className="text-2xl font-black text-zinc-900 dark:text-white">
                        {tgUser?.first_name} {tgUser?.last_name || ""}
                    </h1>
                    <p className="text-sm font-bold text-zinc-400">@{tgUser?.username || "telegram_user"}</p>
                </div>

                {isAdmin && (
                    <div className="mt-3 flex items-center gap-1.5 rounded-full bg-indigo-50 px-4 py-1.5 dark:bg-indigo-900/30">
                        <span className="text-[10px] font-black uppercase tracking-widest text-indigo-600 dark:text-indigo-400">
                            Merchant Account
                        </span>
                    </div>
                )}
            </header>

            <div className="mt-12 space-y-4">
                <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-zinc-100 transition-all active:scale-[0.98] dark:bg-zinc-900 dark:ring-zinc-800">
                    <div className="flex items-center gap-4">
                        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-zinc-50 text-zinc-400 dark:bg-zinc-800">
                            <CreditCard size={20} />
                        </div>
                        <div className="flex-1">
                            <p className="text-[10px] font-black uppercase tracking-widest text-zinc-400">Membership</p>
                            <p className="font-bold text-zinc-900 dark:text-white">
                                {isAdmin ? 'Merchant Partner' : 'Standard Member'}
                            </p>
                        </div>
                    </div>
                </div>

                <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-zinc-100 transition-all active:scale-[0.98] dark:bg-zinc-900 dark:ring-zinc-800">
                    <div className="flex items-center gap-4">
                        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-zinc-50 text-zinc-400 dark:bg-zinc-800">
                            <Globe size={20} />
                        </div>
                        <div className="flex-1">
                            <p className="text-[10px] font-black uppercase tracking-widest text-zinc-400">Region / Tenant</p>
                            <p className="font-bold text-zinc-900 dark:text-white truncate">
                                {authUser?.tenantId || "Default Marketplace"}
                            </p>
                        </div>
                    </div>
                </div>

                {isAdmin && (
                    <div className="rounded-2xl bg-indigo-600 p-5 shadow-xl transition-all active:scale-[0.98]">
                        <div className="flex items-center gap-4 text-white">
                            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-white/20">
                                <Shield size={20} />
                            </div>
                            <div className="flex-1">
                                <p className="text-[10px] font-black uppercase tracking-widest opacity-60">Status</p>
                                <p className="font-bold">Administrator Access Active</p>
                            </div>
                        </div>
                    </div>
                )}
            </div>

            <div className="mt-12 space-y-2 text-center text-[10px] font-black uppercase tracking-widest text-zinc-300 dark:text-zinc-600">
                <p>Asset Market v1.0.0</p>
                <p>User ID: {authUser?.id || '...'}</p>
            </div>
        </div>
    );
}
