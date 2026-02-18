"use client";

import { useTelegram } from "@/components/providers/TelegramProvider";

export default function ProfilePage() {
    const { user } = useTelegram();

    return (
        <div className="min-h-screen bg-zinc-50 px-6 pt-12 dark:bg-black">
            <header className="flex flex-col items-center">
                <div className="h-24 w-24 overflow-hidden rounded-full bg-zinc-200 ring-4 ring-white dark:bg-zinc-800 dark:ring-zinc-900">
                    {user?.photo_url ? (
                        <img src={user.photo_url} alt={user.first_name} className="h-full w-full object-cover" />
                    ) : (
                        <div className="flex h-full w-full items-center justify-center text-2xl font-black text-zinc-400">
                            {user?.first_name?.[0] || "U"}
                        </div>
                    )}
                </div>
                <h1 className="mt-6 text-2xl font-black text-zinc-900 dark:text-white">
                    {user?.first_name} {user?.last_name || ""}
                </h1>
                <p className="text-sm font-medium text-zinc-500">@{user?.username || "telegram_user"}</p>
            </header>

            <div className="mt-12 space-y-4">
                <div className="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-zinc-100 dark:bg-zinc-900 dark:ring-zinc-800">
                    <p className="text-xs font-bold uppercase tracking-widest text-zinc-400">Tier</p>
                    <p className="mt-1 font-bold text-zinc-900 dark:text-white text-lg">Premium Member</p>
                </div>
                <div className="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-zinc-100 dark:bg-zinc-900 dark:ring-zinc-800">
                    <p className="text-xs font-bold uppercase tracking-widest text-zinc-400">Language</p>
                    <p className="mt-1 font-bold text-zinc-900 dark:text-white text-lg">
                        {user?.language_code === 'en' ? 'English 🇺🇸' : user?.language_code || 'English'}
                    </p>
                </div>
            </div>

            <p className="mt-12 text-center text-xs font-medium text-zinc-400 uppercase tracking-widest">
                Asset Market v1.0.0
            </p>
        </div>
    );
}
