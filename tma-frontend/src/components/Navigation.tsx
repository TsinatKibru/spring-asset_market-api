"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "./providers/AuthProvider";
import { Home, Heart, MessageSquare, User, LayoutDashboard, ShieldCheck } from "lucide-react";

interface NavItem {
    name: string;
    href: string;
    icon: any;
    badge?: boolean;
}

const navItems: NavItem[] = [
    { name: "Home", href: "/", icon: Home },
    { name: "Saved", href: "/saved", icon: Heart },
    { name: "Messages", href: "/messages", icon: MessageSquare },
    { name: "Profile", href: "/profile", icon: User },
];

export default function Navigation() {
    const pathname = usePathname();
    const { user, isAuthenticated } = useAuth();

    // Defensive check for roles
    const roles = Array.isArray(user?.roles) ? user.roles : [];
    const isAdmin = roles.some((r: string) =>
        r.toUpperCase() === "ROLE_ADMIN" || r.toUpperCase() === "ADMIN"
    );

    const items = [...navItems];
    if (isAdmin) {
        // Insert Merchant tab at the second position
        items.splice(1, 0, {
            name: "Merchant",
            href: "/merchant/dashboard",
            icon: LayoutDashboard,
            badge: true
        });
    }

    // Hide navigation on specific routes (e.g., detail pages, edit pages, add pages)
    const hideNavRoutes = [
        "/property/",
        "/merchant/property/",
    ];

    const shouldHide = hideNavRoutes.some(route => pathname.includes(route));

    if (shouldHide) return null;

    return (
        <nav className="fixed bottom-0 left-0 right-0 z-50 border-t border-zinc-200 bg-white/90 backdrop-blur-xl dark:border-zinc-800 dark:bg-black/90 pb-safe">
            <div className="mx-auto flex max-w-lg items-center justify-around py-4 px-6 pb-8">
                {items.map((item) => {
                    const isActive = pathname === item.href;
                    const isMerchant = item.name === "Merchant";
                    const Icon = item.icon;

                    return (
                        <Link
                            key={item.name}
                            href={item.href}
                            className={`relative flex flex-col items-center gap-1.5 transition-all active:scale-90 ${isActive
                                ? (isMerchant ? "text-indigo-600 dark:text-indigo-400" : "text-zinc-900 dark:text-white")
                                : "text-zinc-400 hover:text-zinc-600 dark:text-zinc-500 dark:hover:text-zinc-300"
                                }`}
                        >
                            <Icon size={22} strokeWidth={isActive ? 2.5 : 2} />
                            <span className={`text-[10px] font-black uppercase tracking-tighter ${isActive ? "opacity-100" : "opacity-60"}`}>
                                {item.name}
                            </span>
                            {isMerchant && (
                                <span className="absolute -top-1 -right-1 flex h-2 w-2 rounded-full bg-indigo-500 shadow-sm" />
                            )}
                        </Link>
                    );
                })}
            </div>
        </nav>
    );
}
