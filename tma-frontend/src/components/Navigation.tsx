"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "./providers/AuthProvider";

import { Home, Heart, MessageSquare, User, LayoutDashboard } from "lucide-react";

const navItems = [
    { name: "Home", href: "/", icon: Home },
    { name: "Saved", href: "/saved", icon: Heart },
    { name: "Messages", href: "/messages", icon: MessageSquare },
    { name: "Profile", href: "/profile", icon: User },
];

export default function Navigation() {
    const pathname = usePathname();
    const { user } = useAuth();
    const isAdmin = user?.roles?.includes("ROLE_ADMIN");

    const items = [...navItems];
    if (isAdmin) {
        items.splice(1, 0, { name: "Merchant", href: "/merchant/dashboard", icon: LayoutDashboard });
    }

    return (
        <nav className="fixed bottom-0 left-0 right-0 z-50 border-t border-zinc-200 bg-white/80 backdrop-blur-lg dark:border-zinc-800 dark:bg-black/80">
            <div className="mx-auto flex max-w-lg items-center justify-around py-4 px-6 pb-8">
                {items.map((item) => {
                    const isActive = pathname === item.href;
                    const Icon = item.icon;
                    return (
                        <Link
                            key={item.name}
                            href={item.href}
                            className={`flex flex-col items-center gap-1.5 transition-all active:scale-90 ${isActive
                                ? "text-zinc-900 dark:text-white"
                                : "text-zinc-400 hover:text-zinc-600 dark:text-zinc-500 dark:hover:text-zinc-300"
                                }`}
                        >
                            <Icon size={22} strokeWidth={isActive ? 2.5 : 2} />
                            <span className={`text-[10px] font-black uppercase tracking-tighter ${isActive ? "opacity-100" : "opacity-60"}`}>
                                {item.name}
                            </span>
                        </Link>
                    );
                })}
            </div>
        </nav>
    );
}
